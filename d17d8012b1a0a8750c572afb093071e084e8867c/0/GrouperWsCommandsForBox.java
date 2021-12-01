/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import edu.internet2.middleware.grouperClient.api.GcFindGroups;
import edu.internet2.middleware.grouperClient.api.GcGetMembers;
import edu.internet2.middleware.grouperClient.api.GcMessageAcknowledge;
import edu.internet2.middleware.grouperClient.api.GcMessageReceive;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.beans.WsFindGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsMessage;
import edu.internet2.middleware.grouperClient.ws.beans.WsMessageResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsQueryFilter;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;


/**
 *
 */
public class GrouperWsCommandsForBox {

  /**
   * 
   */
  public GrouperWsCommandsForBox() {
  }

  /**
   * 
   * @param groupName
   * @return the list of users
   */
  public static Set<String> retrieveGrouperMembershipsForGroup(String groupName) {

    
    //users that are supposed to be in box (do this at top so it doesnt affect the timing here
    Map<String, String[]> usersAllowedToBeInBox = GrouperWsCommandsForBox.retrieveGrouperUsers();

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    debugMap.put("method", "retrieveGrouperMembershipsForGroup");
    debugMap.put("groupName", groupName);
    long startTime = System.nanoTime();
    try {
    
      String subjectAttributeForBoxUsername = GrouperBoxUtils.configSubjectAttributeForBoxUsername();
      
      Set<String> grouperUsernamesInGroup = new TreeSet<String>();

      GcGetMembers gcGetMembers = new GcGetMembers().addGroupName(groupName);
        
      // request extra attributes in WS call
      if (!GrouperClientUtils.equals("id", subjectAttributeForBoxUsername)) {
        gcGetMembers.addSubjectAttributeName(subjectAttributeForBoxUsername);
      }

      for (String sourceId : GrouperBoxUtils.configSourcesForSubjects()) {
        gcGetMembers.addSourceId(sourceId);
      }
      
      if (GrouperClientConfig.retrieveConfig().propertyValueBoolean("grouperBox.grouperWs.autopage", true)) {
        gcGetMembers.assignAutopage(true);
      }
      
      WsGetMembersResults wsGetMembersResults = gcGetMembers.execute();

      WsGetMembersResult wsGetMembersResult = wsGetMembersResults.getResults()[0];
        
      WsSubject[] wsSubjects = wsGetMembersResult.getWsSubjects();
      
      debugMap.put("originalMemberCount", GrouperClientUtils.length(wsSubjects));

      String[] attributeNames = wsGetMembersResults.getSubjectAttributeNames();

      int unresolvableCount = 0;
      int notAllowedInBoxCount = 0;
      
      //get usernames from grouper
      for (WsSubject wsSubject : GrouperClientUtils.nonNull(wsSubjects, WsSubject.class)) {
        String subjectPrefix = null;
        if (GrouperClientUtils.equals("id", subjectAttributeForBoxUsername)) {
          subjectPrefix = wsSubject.getId();
        } else {
          subjectPrefix = GrouperClientUtils.subjectAttributeValue(wsSubject, attributeNames, subjectAttributeForBoxUsername);
        }
        if (GrouperClientUtils.isBlank(subjectPrefix)) {
          //i guess this is ok
          debugMap.put("subjectBlankAttribute_" + wsSubject.getSourceId() + "_" + wsSubject.getId(), subjectAttributeForBoxUsername);
          unresolvableCount++;
        } else {
          String boxUserName = subjectPrefix
              + GrouperClientUtils.defaultIfBlank(GrouperClientConfig.retrieveConfig().propertyValueString("grouperBox.subjectIdSuffix"), "");
          if (usersAllowedToBeInBox == null || usersAllowedToBeInBox.containsKey(boxUserName)) {
            grouperUsernamesInGroup.add(boxUserName);
          } else {
            notAllowedInBoxCount++;
          }
        }
      }

      debugMap.put("finalMemberCount", GrouperClientUtils.length(grouperUsernamesInGroup));
      debugMap.put("unresolvableCount", unresolvableCount);
      debugMap.put("notAllowedInBoxCount", notAllowedInBoxCount);
      return grouperUsernamesInGroup;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperBoxLog.boxLog(debugMap, startTime);
    }

  }
  
  /**
   * @return list of groups never null
   */
  public static List<WsGroup> retrieveGrouperGroups() {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    debugMap.put("method", "retrieveGrouperGroups");
    long startTime = System.nanoTime();
    try {
    
      //# put groups in here which go to box, the name in box will be the extension here
      String grouperBoxFolderName = GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperBox.folder.name.withBoxGroups");

      debugMap.put("grouperBoxFolderName", grouperBoxFolderName);

      WsQueryFilter wsQueryFilter = new WsQueryFilter();
      wsQueryFilter.setQueryFilterType("FIND_BY_STEM_NAME");
      wsQueryFilter.setStemName(grouperBoxFolderName);
      wsQueryFilter.setStemNameScope("ONE_LEVEL");
      
      WsFindGroupsResults wsFindGroupsResults = new GcFindGroups().assignQueryFilter(wsQueryFilter).execute();
    
      WsGroup[] wsGroupsArray = wsFindGroupsResults.getGroupResults();

      debugMap.put("numberOfGroups", GrouperClientUtils.length(wsGroupsArray));

      @SuppressWarnings("unchecked")
      List<WsGroup> grouperGroups = wsGroupsArray == null ? new ArrayList<WsGroup>() : (List<WsGroup>)GrouperClientUtils.toList(wsGroupsArray);
      return grouperGroups;
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperBoxLog.boxLog(debugMap, startTime);
    }

    
  }

  /**
   * cache of users in grouper
   */
  private static ExpirableCache<Boolean, Map<String, String[]>> retrieveGrouperUsersCache = null;
  
  /**
   * lazy load return the cache of users
   * @return the cache
   */
  private static ExpirableCache<Boolean, Map<String, String[]>> retrieveGrouperUsersCache() {
    if (retrieveGrouperUsersCache == null) {
      int expireMinutes = GrouperClientConfig.retrieveConfig().propertyValueInt("grouperBox.cacheGrouperUsersForMinutes", 60);
      retrieveGrouperUsersCache = new ExpirableCache(expireMinutes);
    }
    return retrieveGrouperUsersCache;
  }
  
  /**
   * return null if not configured to have such a group, otherwise the cached members of the group.  note, this is not a copy of the
   * cache, it can be edited
   * @return a map of boxUserName to array of [subjectId, sourceId, boxSubjectAttributeValue, boxUserName]
   */
  public static Map<String, String[]> retrieveGrouperUsers() {
    
    Map<String, String[]> result = retrieveGrouperUsersCache().get(Boolean.TRUE);
    if (result != null) {
      return result;
    }

    String boxGrouperRequireGroupName = GrouperClientConfig.retrieveConfig().propertyValueString("grouperBox.requireGroup");
    if (GrouperClientUtils.isBlank(boxGrouperRequireGroupName)) {
      return null;
    }

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    long startTimeNanos = System.nanoTime();

    debugMap.put("method", "retrieveGrouperUsers");

    try {
      result = new HashMap<String, String[]>();
      
      GcGetMembers gcGetMembers = new GcGetMembers().addGroupName(boxGrouperRequireGroupName);
      
      for (String sourceId : GrouperBoxUtils.configSourcesForSubjects()) {
        gcGetMembers.addSourceId(sourceId);
      }
  
      String configSubjectAttributeForBoxUsername = GrouperBoxUtils.configSubjectAttributeForBoxUsername();
      
      debugMap.put("configSubjectAttributeForBoxUsername", configSubjectAttributeForBoxUsername);
      
      if (GrouperClientUtils.equals("id", configSubjectAttributeForBoxUsername)) {
  
        // in the utils method below this is what it expects
        configSubjectAttributeForBoxUsername = "subject__id"; 
      } else {
        gcGetMembers.addSubjectAttributeName(configSubjectAttributeForBoxUsername);
      }
      
      if (GrouperClientConfig.retrieveConfig().propertyValueBoolean("grouperBox.grouperWs.autopage", true)) {
        gcGetMembers.assignAutopage(true);
      }

      WsGetMembersResults wsGetMembersResults = gcGetMembers.execute();
      WsGetMembersResult wsGetMembersResult = wsGetMembersResults.getResults()[0];
      
      debugMap.put("resultSize", GrouperClientUtils.length(wsGetMembersResult.getWsSubjects()));

      String subjectSuffix = GrouperClientUtils.defaultIfBlank(GrouperClientConfig.retrieveConfig().propertyValueString("grouperBox.subjectIdSuffix"), "");

      debugMap.put("subjectSuffix", subjectSuffix);

      for (WsSubject wsSubject: GrouperClientUtils.nonNull(wsGetMembersResult.getWsSubjects(), WsSubject.class)) {
        String usernameValueWithoutSuffix = GrouperClientUtils.subjectAttributeValue(wsSubject, wsGetMembersResults.getSubjectAttributeNames(), configSubjectAttributeForBoxUsername);
        String boxUsername = usernameValueWithoutSuffix + subjectSuffix;
        String[] userArray = new String[]{wsSubject.getId(), wsSubject.getSourceId(), usernameValueWithoutSuffix, boxUsername};
        result.put(boxUsername, userArray);
      }
      
      retrieveGrouperUsersCache().put(Boolean.TRUE, result);
    } finally {
      GrouperBoxLog.boxLog(debugMap, startTimeNanos);
    }
    
    return result;
  }

  /**
   * 
   * @return the messages
   */
  public static WsMessage[] grouperReceiveMessages() {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    long startTimeNanos = System.nanoTime();

    debugMap.put("method", "grouperReceiveMessages");

    try {
      String messageSystemName = GrouperClientConfig.retrieveConfig()
          .propertyValueStringRequired("grouperBox.messaging.systemName");

      debugMap.put("messageSystemName", messageSystemName);
      
      String messageQueueName = GrouperClientConfig.retrieveConfig()
          .propertyValueStringRequired("grouperBox.messaging.queueName");

      debugMap.put("messageQueueName", messageQueueName);

      WsMessageResults wsMessageResults = new GcMessageReceive()
        .assignMessageSystemName(messageSystemName).assignQueueOrTopicName(messageQueueName).execute();

      debugMap.put("checkMessagesWsResultCode", wsMessageResults.getResultMetadata().getResultCode());
      debugMap.put("messageCount", GrouperClientUtils.length(wsMessageResults.getMessages()));

      return wsMessageResults.getMessages();
    } finally {
      GrouperBoxLog.boxLog(debugMap, startTimeNanos);
    }

  }
  
  /**
   * @param ids
   * @param acknowledgeType mark_as_processed, return_to_queue, return_to_end_of_queue,  send_to_another_queue
   */
  public static void grouperAcknowledgeMessages(Set<String> ids, String acknowledgeType) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    long startTimeNanos = System.nanoTime();

    debugMap.put("method", "grouperAcknowledgeMessages");
    debugMap.put("numberOfIds", GrouperClientUtils.length(ids));
    debugMap.put("acknowledgeType", acknowledgeType);

    try {
      String messageSystemName = GrouperClientConfig.retrieveConfig()
          .propertyValueStringRequired("grouperBox.messaging.systemName");

      debugMap.put("messageSystemName", messageSystemName);
      
      String messageQueueName = GrouperClientConfig.retrieveConfig()
          .propertyValueStringRequired("grouperBox.messaging.queueName");

      debugMap.put("messageQueueName", messageQueueName);


      GcMessageAcknowledge successMessageAcknowledge = null;
      successMessageAcknowledge = new GcMessageAcknowledge()
        .assignMessageSystemName(messageSystemName).assignQueueOrTopicName(messageQueueName).assignAcknowledgeType(acknowledgeType);
      
      for (String id : ids) {
        //mark message as processed
        successMessageAcknowledge.addMessageId(id);
      }
      
      successMessageAcknowledge.execute();
      
    } finally {
      GrouperBoxLog.boxLog(debugMap, startTimeNanos);
    }

  }

}

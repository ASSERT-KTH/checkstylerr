/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperBox;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;


/**
 *
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class GrouperBoxFullRefresh implements Job {

  /** when was last full refresh started */
  private static long lastFullRefreshStart = -1L;
  
  /**
   * when was last full refresh started
   * @return the lastFullRefreshStart
   */
  public static long getLastFullRefreshStart() {
    return lastFullRefreshStart;
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    fullRefreshLogic();
  }
  
  /**
   * change log temp to change log
   */
  public static final String GROUPER_DUO_FULL_REFRESH = "CHANGE_LOG_grouperBoxFullRefresh";

  /** logger */
  private static final Log LOG = LogFactory.getLog(GrouperBoxFullRefresh.class);

  /**
   * 
   */
  public GrouperBoxFullRefresh() {
  }

  /**
   * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
   */
  public void execute(JobExecutionContext context) throws JobExecutionException {

    fullRefreshLogic();

    
  }

  /**
   * if full refresh is in progress
   */
  private static boolean fullRefreshInProgress = false;
  
  
  /**
   * if full refresh is in progress
   * @return the fullRefreshInProgress
   */
  public static boolean isFullRefreshInProgress() {
    return fullRefreshInProgress;
  }

  /**
   * wait for full refresh to end
   */
  public static void waitForFullRefreshToEnd() {
    while (isFullRefreshInProgress()) {
      GrouperClientUtils.sleep(1000);
    }
  }
  /**
   * full refresh logic
   */
  public static void fullRefreshLogic() {
    GrouperBoxFullRefreshResults grouperBoxFullRefreshResults = fullRefreshLogicWithResult();
    if (!GrouperClientUtils.isBlank(grouperBoxFullRefreshResults.getError())) {
      throw new RuntimeException(grouperBoxFullRefreshResults.getError());
    }
  }
  
  /**
   * full refresh logic
   * @return results
   */
  public static GrouperBoxFullRefreshResults fullRefreshLogicWithResult() {
    
    GrouperBoxFullRefreshResults grouperRemedyFullRefresh = new GrouperBoxFullRefreshResults();

    fullRefreshInProgress = true;
    
    GrouperBoxMessageConsumer.waitForIncrementalRefreshToEnd();
    
    //give a tiny bit of buffer
    lastFullRefreshStart = System.currentTimeMillis() - 500;

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    long startTimeNanos = System.nanoTime();

    debugMap.put("method", "fullRefreshLogic");

    //lets enter a log entry so it shows up as error in the db    
    long startedMillis = System.currentTimeMillis();
    
    try {

      @SuppressWarnings("unchecked")
      List<WsGroup> grouperGroups = GrouperWsCommandsForBox.retrieveGrouperGroups();
      
      //take out include/exclude etc
      Iterator<WsGroup> iterator = grouperGroups.iterator();
      
      {
        int invalidGroupNameCount = 0;
        
        while (iterator.hasNext()) {
          WsGroup current = iterator.next();
          if (!GrouperBoxUtils.validBoxGroupName(current.getName())) {
            iterator.remove();
            invalidGroupNameCount++;
          }
        }
  
        debugMap.put("grouperGroupNameCount", grouperGroups.size());
        if (invalidGroupNameCount > 0) {
          debugMap.put("invalidGrouperGroupNameCount", invalidGroupNameCount);
        }
      }
      
      //make a map from group extension
      Map<String, WsGroup> grouperGroupExtensionToGroupMap = new TreeMap<String, WsGroup>();
      
      for (WsGroup group : grouperGroups) {
        grouperGroupExtensionToGroupMap.put(group.getExtension(), group);
      }
      
      //get groups from box
      Map<String, GrouperBoxGroup> boxGroupNameToGroupMap = GrouperBoxCommands.retrieveBoxGroups();

      debugMap.put("boxGroupCount", boxGroupNameToGroupMap.size());

      debugMap.put("millisGetData", System.currentTimeMillis() - startedMillis);
      long startedUpdateData = System.currentTimeMillis();

      boolean needsGroupRefresh = false;
      
      int insertCount = 0;
      int deleteCount = 0;
      int unresolvableCount = 0;
      int totalCount = 0;
      
      //which groups are in box and not in grouper?
      Set<String> groupExtensionsInBoxNotInGrouper = new TreeSet<String>(boxGroupNameToGroupMap.keySet());
      groupExtensionsInBoxNotInGrouper.removeAll(grouperGroupExtensionToGroupMap.keySet());
      
      for (String groupExtensionToRemove : groupExtensionsInBoxNotInGrouper) {
        GrouperBoxGroup grouperBoxGroup = boxGroupNameToGroupMap.get(groupExtensionToRemove);
        if (GrouperBoxCommands.deleteBoxGroup(grouperBoxGroup, false)) {
        
          deleteCount++;
          debugMap.put("deleteBoxGroup_" + groupExtensionToRemove, true);
          
          needsGroupRefresh = true;
        }
      }

      //loop through groups in grouper
      for (String groupExtensionInGrouper : grouperGroupExtensionToGroupMap.keySet()) {
        
        GrouperBoxGroup groupInBox = boxGroupNameToGroupMap.get(groupExtensionInGrouper);
        
        if (groupInBox == null) {
          //create box group
          GrouperBoxCommands.createBoxGroup(groupExtensionInGrouper, false);
          needsGroupRefresh = true;
          debugMap.put("createBoxGroup_" + groupExtensionInGrouper, true);
          insertCount++;
        }
      }

      if (needsGroupRefresh) {
        //lets get them again if some were created
        boxGroupNameToGroupMap = GrouperBoxCommands.retrieveBoxGroups();
      }
      
      //loop through groups in grouper
      for (String groupExtensionInGrouper : grouperGroupExtensionToGroupMap.keySet()) {
        
        WsGroup grouperGroup = grouperGroupExtensionToGroupMap.get(groupExtensionInGrouper);
        
        GrouperBoxGroup grouperBoxGroup = boxGroupNameToGroupMap.get(groupExtensionInGrouper);
        
        Map<String, GrouperBoxUser> boxMemberUsernameToUser = grouperBoxGroup.getMemberUsers();
                    
        Set<String> grouperUsernamesInGroup = GrouperWsCommandsForBox.retrieveGrouperMembershipsForGroup(grouperGroup.getName());

        debugMap.put("grouperSubjectCount_" + grouperGroup.getExtension(), grouperUsernamesInGroup.size());
        totalCount += grouperUsernamesInGroup.size();
        
        //see which users are not in Box
        Set<String> grouperUsernamesNotInBox = new TreeSet<String>(grouperUsernamesInGroup);
        grouperUsernamesNotInBox.removeAll(boxMemberUsernameToUser.keySet());

        debugMap.put("additions_" + grouperGroup.getExtension(), grouperUsernamesNotInBox.size());

        int userCountNotInBox = 0;
        
        //add to box
        for (String grouperUsername : grouperUsernamesNotInBox) {
          
          GrouperBoxUser grouperBoxUser = GrouperBoxUser.retrieveUsers().get(grouperUsername);
          
          if (grouperBoxUser == null) {
            userCountNotInBox++;
          } else {
            insertCount++;
            grouperBoxGroup.assignUserToGroup(grouperBoxUser, false);
          }
        }

        debugMap.put("userCountDoesntExistInBox_" + grouperGroup.getExtension(), userCountNotInBox);

        //see which users are not in box
        Set<String> boxUsernamesNotInGrouper = new TreeSet<String>(boxMemberUsernameToUser.keySet());
        boxUsernamesNotInGrouper.removeAll(grouperUsernamesInGroup);

        debugMap.put("removes_" + grouperGroup.getExtension(), boxUsernamesNotInGrouper.size());

        //remove from box
        for (String boxUsername : boxUsernamesNotInGrouper) {
          GrouperBoxUser grouperBoxUser = boxMemberUsernameToUser.get(boxUsername);
          GrouperBoxCommands.removeUserFromBoxGroup(grouperBoxUser, grouperBoxGroup, false);
          deleteCount++;
        }
        
      }
      
      //lets reconcile which users are in box but not supposed to be
      Map<String, String[]> usersAllowedToBeInBox = GrouperWsCommandsForBox.retrieveGrouperUsers();
      if (usersAllowedToBeInBox != null) {
        
        Map<String, GrouperBoxUser> grouperBoxUsers = GrouperBoxUser.retrieveUsers();

        for (GrouperBoxUser grouperBoxUser : grouperBoxUsers.values()) {
          GrouperBoxCommands.deprovisionOrUndeprovision(grouperBoxUser, debugMap);
        }
        
      }
      
      grouperRemedyFullRefresh.setDeleteCount(deleteCount);
      grouperRemedyFullRefresh.setInsertCount(insertCount);
      grouperRemedyFullRefresh.setTotalCount(totalCount);
      grouperRemedyFullRefresh.setUnresolvableCount(unresolvableCount);
      grouperRemedyFullRefresh.setMillisGetData((int)(System.currentTimeMillis() - startedUpdateData));
      grouperRemedyFullRefresh.setMillis((int)(System.currentTimeMillis() - startedMillis));

      debugMap.put("millisLoadData", grouperRemedyFullRefresh.getMillisGetData());
      debugMap.put("millis", grouperRemedyFullRefresh.getMillis());
      
      debugMap.put("insertCount", grouperRemedyFullRefresh.getInsertCount());
      debugMap.put("deleteCount", grouperRemedyFullRefresh.getDeleteCount());
      debugMap.put("unresolvableCount", grouperRemedyFullRefresh.getUnresolvableCount());
      debugMap.put("totalCount", grouperRemedyFullRefresh.getTotalCount());
      
    } catch (RuntimeException e) {
      final String exceptionFullSync = GrouperClientUtils.getFullStackTrace(e);
      debugMap.put("exception", exceptionFullSync);
      String errorMessage = "Problem running box full sync";
      grouperRemedyFullRefresh.setError(errorMessage + "\n" + exceptionFullSync);
      LOG.error(errorMessage, e);
    } finally {
      GrouperBoxLog.boxLog(debugMap, startTimeNanos);
      fullRefreshInProgress = false;
    }

    return grouperRemedyFullRefresh;
    
  }

  /**
   * 
   */
  public static class GrouperBoxFullRefreshResults {
    
    /**
     * error
     */
    private String error;
    
    
    /**
     * @return the error
     */
    public String getError() {
      return this.error;
    }

    
    /**
     * @param error1 the error to set
     */
    public void setError(String error1) {
      this.error = error1;
    }

    /**
     * 
     */
    private int millisGetData;
    
    /**
     * 
     */
    private int millis;
    
    /**
     * 
     */
    private int insertCount;
    
    /**
     * 
     */
    private int deleteCount;
    
    /**
     * 
     */
    private int unresolvableCount;
    
    /**
     * 
     */
    private int totalCount;
    
    /**
     * @return the millisLoadData
     */
    public int getMillisGetData() {
      return this.millisGetData;
    }
    
    /**
     * @param millisGetData1 the millisLoadData to set
     */
    public void setMillisGetData(int millisGetData1) {
      this.millisGetData = millisGetData1;
    }
    
    /**
     * @return the millis
     */
    public int getMillis() {
      return this.millis;
    }
    
    /**
     * @param millis1 the millis to set
     */
    public void setMillis(int millis1) {
      this.millis = millis1;
    }
    
    /**
     * @return the insertCount
     */
    public int getInsertCount() {
      return this.insertCount;
    }
    
    /**
     * @param insertCount1 the insertCount to set
     */
    public void setInsertCount(int insertCount1) {
      this.insertCount = insertCount1;
    }
    
    /**
     * @return the deleteCount
     */
    public int getDeleteCount() {
      return this.deleteCount;
    }
    
    /**
     * @param deleteCount1 the deleteCount to set
     */
    public void setDeleteCount(int deleteCount1) {
      this.deleteCount = deleteCount1;
    }
    
    /**
     * @return the unresolvableCount
     */
    public int getUnresolvableCount() {
      return this.unresolvableCount;
    }
    
    /**
     * @param unresolvableCount1 the unresolvableCount to set
     */
    public void setUnresolvableCount(int unresolvableCount1) {
      this.unresolvableCount = unresolvableCount1;
    }
    
    /**
     * @return the totalCount
     */
    public int getTotalCount() {
      return this.totalCount;
    }
    
    /**
     * @param totalCount1 the totalCount to set
     */
    public void setTotalCount(int totalCount1) {
      this.totalCount = totalCount1;
    }
    
    
  }
  
}

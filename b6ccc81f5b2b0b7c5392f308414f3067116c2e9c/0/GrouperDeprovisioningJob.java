/**
 * Copyright 2018 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.internet2.middleware.grouper.app.deprovisioning;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.quartz.DisallowConcurrentExecution;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignable;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.EmailObject;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * deprovisioning daemon
 */
@DisallowConcurrentExecution
public class GrouperDeprovisioningJob extends OtherJobBase {
  


  /**
   * group that users who are allowed to deprovision other users are in
   * @param affiliation deprovi
   * @return the group name
   */
  public static String retrieveDeprovisioningManagersMustBeInGroupName(String affiliation) {
    
    //  # e.g. managersWhoCanDeprovision_<affiliationName>
    //  # e.g. usersWhoHaveBeenDeprovisioned_<affiliationName>
    //  deprovisioning.systemFolder = $$grouper.rootStemForBuiltinObjects$$:deprovisioning
    
    return GrouperDeprovisioningSettings.deprovisioningStemName() + ":managersWhoCanDeprovision_" + affiliation;

  }

  /**
   * group name which has been deprovisioned
   * @param affiliation
   * @return the group name
   */
  public static String retrieveGroupNameWhichHasBeenDeprovisioned(String affiliation) {
    
    //  # e.g. managersWhoCanDeprovision_<affiliationName>
    //  # e.g. usersWhoHaveBeenDeprovisioned_<affiliationName>
    //  deprovisioning.systemFolder = $$grouper.rootStemForBuiltinObjects$$:deprovisioning
    
    return GrouperDeprovisioningSettings.deprovisioningStemName() + ":usersWhoHaveBeenDeprovisioned_" + affiliation;
  }

  /**
   * run the daemon
   * @param args
   */
  public static void main(String[] args) {
    runDaemonStandalone();
  }

  /**
   * run standalone
   */
  public static void runDaemonStandalone() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Hib3GrouperLoaderLog hib3GrouperLoaderLog = new Hib3GrouperLoaderLog();
    
    hib3GrouperLoaderLog.setHost(GrouperUtil.hostname());
    String jobName = "OTHER_JOB_deprovisioningDaemon";

    hib3GrouperLoaderLog.setJobName(jobName);
    hib3GrouperLoaderLog.setJobType(GrouperLoaderType.OTHER_JOB.name());
    hib3GrouperLoaderLog.setStatus(GrouperLoaderStatus.STARTED.name());
    hib3GrouperLoaderLog.store();
    
    OtherJobInput otherJobInput = new OtherJobInput();
    otherJobInput.setJobName(jobName);
    otherJobInput.setHib3GrouperLoaderLog(hib3GrouperLoaderLog);
    otherJobInput.setGrouperSession(grouperSession);
    new GrouperDeprovisioningJob().run(otherJobInput);
  }

  /**
   * get map of email addresses to email objects for stem attributes 
   * @param attributeDef
   * @return
   */
  private Map<String, Set<EmailObject>> buildDeprovisioningStemEmails() {
  
    //TODO only look at direct assignments?  are there duplicate assignments?
    Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory().getAttributeAssign().findAttributeAssignments(
        AttributeAssignType.stem,
        null, GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameBase().getId(), null, 
        null, null, null, 
        null, 
        Boolean.TRUE, false);
    
    Map<String, Set<EmailObject>> emails = new HashMap<String, Set<EmailObject>>();
    
    for (AttributeAssign attributeAssign: attributeAssigns) {
      
      String deprovisioningSendEmail = attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameSendEmail().getName());
      
//      Map<String, Set<EmailObject>> localEmailMap = stemDeprovisioningProcessHelper(attributeAssign.getOwnerStem(), attributeAssign);
//
//      boolean sendEmailAttributeValue = GrouperUtil.booleanValue(deprovisioningSendEmail, true);
//      
//      // skip sending email for this attribute assign
//      if (!sendEmailAttributeValue) {
//        LOG.debug("For "+attributeAssign.getOwnerStem().getDisplayName()+" deprovisioningSendEmail attribute is not set to true so skipping sending email.");
//        continue;
//      }
//
//      if (sendEmailAttributeValue) {
//        mergeEmailObjects(emails, localEmailMap);
//      }
    }
    
    return emails;
    
  }

  /**
   * get the list of recently deprovisioned users
   * @param affiliation 
   * @return the list of members
   */
  public static Set<Member> retrieveRecentlyDeprovisionedUsers(final String affiliation) {
    
    //switch over to admin so attributes work
    return (Set<Member>)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        Group deprovisionedGroup = GroupFinder.findByName(grouperSession, retrieveGroupNameWhichHasBeenDeprovisioned(affiliation), true);
        
        Set<Member> members = deprovisionedGroup.getMembers();
        return members;
      }
    });

  }
  
  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(GrouperDeprovisioningJob.class);
  
  /**
   * @see edu.internet2.middleware.grouper.app.loader.OtherJobBase#run(edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput)
   */
  @Override
  public OtherJobOutput run(OtherJobInput otherJobInput) {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    if (!GrouperDeprovisioningSettings.deprovisioningEnabled()) {
      return null;
    }
    
    //update metadata
    GrouperDeprovisioningLogic.updateDeprovisioningMetadata(StemFinder.findRootStem(grouperSession));
    
    GrouperDeprovisioningEmailService emailService = new GrouperDeprovisioningEmailService();
    emailService.sendEmailForAllAffiliations(otherJobInput.getGrouperSession());
    return null;
  }
  
  /**
   * if this is in cache it means null and it is in cache
   */
  private static final AttributeAssign nullAttributeAssign = new AttributeAssign();
  
  /**
   * 
   * @param attributeAssignable
   * @return the id
   */
  private static String retrieveAttributeAssignableId(AttributeAssignable attributeAssignable) {
    if (attributeAssignable == null) {
      return null;
    }
    if (attributeAssignable instanceof Group) {
      return ((Group)attributeAssignable).getId();
    }
    if (attributeAssignable instanceof Stem) {
      return ((Stem)attributeAssignable).getId();
    }
    throw new RuntimeException("Not expecting object type: " + attributeAssignable.getClass() + ", " + attributeAssignable);
  }
  
  /**
   * 
   * @param attributeAssignable
   * @return the id
   */
  private static Stem retrieveAttributeAssignableParentStem(AttributeAssignable attributeAssignable) {
    if (attributeAssignable == null) {
      return null;
    }
    if (attributeAssignable instanceof Group) {
      return ((Group)attributeAssignable).getParentStem();
    }
    if (attributeAssignable instanceof Stem) {
      return ((Stem)attributeAssignable).getParentStem();
    }
    throw new RuntimeException("Not expecting object type: " + attributeAssignable.getClass() + ", " + attributeAssignable);
  }
  
  /**
   * take a stem attribute assign and process it, make sure child metadata is correct
   * @param stem is the stem the attribute is on
   * @param stemAttributeAssign
   * @return the email objects
   */
  public static Map<String, Set<EmailObject>> stemDeprovisioningProcessHelper(Stem stem, AttributeAssign stemAttributeAssign) {
    
    Map<String, Set<EmailObject>> emails = new HashMap<String, Set<EmailObject>>();
    
    String affiliation = stemAttributeAssign.getAttributeValueDelegate()
        .retrieveValueString(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAffiliation().getName());
    
    String stemHasDeprovisioningString = "false";

    if (stemAttributeAssign != null) {

      stemHasDeprovisioningString = stemAttributeAssign.getAttributeValueDelegate()
          .retrieveValueString(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameDeprovision().getName());
        
      // it needs this if it doesnt have it (from earlier upgrade)
      if (StringUtils.isBlank(stemHasDeprovisioningString)) {
        
        stemAttributeAssign.getAttributeValueDelegate().assignValueString(GrouperDeprovisioningAttributeNames
            .retrieveAttributeDefNameDeprovision().getName(), "true");
        stemHasDeprovisioningString = "true";
      }

    }

    String attestationStemScope = stemAttributeAssign == null ? Scope.SUB.name() : 
      stemAttributeAssign.getAttributeValueDelegate().retrieveValueString(
          GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameDeprovision().getName());

    // go through each group and check if they have their own deprovisioning attribute and use them if they are present.
    // if not, then use the stem attributes.
    Scope scope = GrouperUtil.defaultIfNull(Scope.valueOfIgnoreCase(attestationStemScope, false), Scope.SUB);
        
    Set<Group> childGroups = stem.getChildGroups(scope);
    
    for (Group group: childGroups) {
      
      AttributeAssign groupAttributeAssign = group.getAttributeDelegate().retrieveAssignment(null, 
          GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameBase(), false, false);
              
      if (groupAttributeAssign == null) {
        groupAttributeAssign = group.getAttributeDelegate().assignAttribute(
            GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameBase()).getAttributeAssign();
      }
      
      String directAssignmentString = groupAttributeAssign.getAttributeValueDelegate()
          .retrieveValueString(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameDirectAssignment().getName());
      
      if (StringUtils.isBlank(directAssignmentString)) {
        groupAttributeAssign.getAttributeValueDelegate().assignValueString(
            GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameDirectAssignment().getName(), "false");
        directAssignmentString = "false";
      }

      // group has direct attestation, don't use stem attributes at all.  This will be in group assignment calculations
      if (GrouperUtil.booleanValue(directAssignmentString, false)) { 
        continue;
      }
//TODO needs affiliation
      //start at stem and look for assignment, needs affiliation
      AttributeAssignable attributeAssignable = group.getParentStem().getAttributeDelegate()
        .getAttributeOrAncestorAttribute(
            GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameBase().getName(), false);

      //there is no direct assignment and no stem with attestation
      if (attributeAssignable == null) {

// TODO
//        groupAttributeAssign.getAttributeValueDelegate().assignValueString(
//            retrieveAttributeDefNameHasAttestation().getName(), "false");
//        groupAttributeAssign.getAttributeDelegate().removeAttribute(retrieveAttributeDefNameCalculatedDaysLeft());
        continue;
        
      }
      
//      //make sure its the right stem that has the assignment
//      if (!StringUtils.equals(((Stem)attributeAssignable).getName(), stem.getName())) {
//        continue;
//      }
//
//      //make sure group is in sync with stem
//      groupAttributeAssign.getAttributeValueDelegate().assignValueString(retrieveAttributeDefNameHasAttestation().getName(), stemHasDeprovisioningString);
//      
//      if (!GrouperUtil.booleanValue(stemHasDeprovisioningString, true)) {
//        continue;
//      }
//
//      Set<AttributeAssign> singleGroupAttributeAssign = new HashSet<AttributeAssign>();
//      singleGroupAttributeAssign.add(groupAttributeAssign);
//      
//      // skip sending email for this attribute assign
//      Map<String, Set<EmailObject>> buildAttestationGroupEmails = buildAttestationGroupEmails(stemAttributeAssign, singleGroupAttributeAssign);
//     
//      mergeEmailObjects(emails, buildAttestationGroupEmails);

    }
    
    return emails;
  }
  
  class EmailPerPerson {
    private List<CustomEmail> customEmails;
    private StandardEmail standardEmail;
  }
  
  class CustomEmail {
    private String subject;
    private String body;
  }
  
  class StandardEmail {
    private int groupsCount;
    private int stemsCount;
    private int attributeDefCount;
  }

}

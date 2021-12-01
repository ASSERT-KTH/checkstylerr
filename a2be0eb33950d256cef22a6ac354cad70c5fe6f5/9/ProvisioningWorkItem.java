package edu.internet2.middleware.grouper.pspng;

/*******************************************************************************
 * Copyright 2015 Internet2
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
 ******************************************************************************/

import java.util.*;
import java.util.logging.Level;

import edu.internet2.middleware.grouper.changeLog.*;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.subject.Subject;


/**
 * This is a wrapper for the event-triggering data item used to drive PSP.
 * Besides insulating PSP code from whether it is being triggered by Grouper's
 * ChangeLog process or by a Messaging process, this wrapper also keeps track
 * of the status and additional data associated with this trigger event.
 * 
 * For instance, LDAP-based provisioners store the ldap modifications that are
 * necessary to process a trigger event within the events themselves. At the end
 * of a batch of events, all the ldap modifications are pulled together into as
 * few ldap modification operations as possible.
 *  
 * 
 * @author Bert Bee-Lindgren
 *
 */
public class ProvisioningWorkItem {
  final private static Logger LOG = LoggerFactory.getLogger(Provisioner.class);

  public enum WORK_ITEM_COMMAND {FULL_SYNC_GROUP, REMOVE_EXTRA_GROUPS, HANDLE_CHANGELOG_ENTRY};

  protected final WORK_ITEM_COMMAND command;
  protected final ChangeLogEntry work;
  protected String groupName;

  protected DateTime asOfDate;
  protected Boolean success=null;
  protected String status=null;
  protected String statusMessage=null;


  /**
   * A place where information can be cached between the start/provision/finish 
   * phases of a provisioning batch
   */
  protected Map<String, Object> provisioningData = new HashMap<String,Object>();
  

  /**
   * Create a work item that just holds the groupName without the overhead of  
   * a changelog item.
   * 
   * This is used when Provisioner code is used in FullSync processes.
   * @param command - What command does this Item represent
   * @param group
   */
  protected ProvisioningWorkItem(WORK_ITEM_COMMAND command, GrouperGroupInfo group, DateTime asOfDate) {
    this.command = command;
    this.work = null;
    this.asOfDate = asOfDate;

    if ( group != null )
      this.groupName=group.getName();
    else
      this.groupName=null;
  }
  
  
  public ProvisioningWorkItem(ChangeLogEntry work) {
    this.command = WORK_ITEM_COMMAND.HANDLE_CHANGELOG_ENTRY;
    this.work=work;
    this.asOfDate = new DateTime(work.getCreatedOn().getTime());
  }

  public static ProvisioningWorkItem createForFullSync(GrouperGroupInfo grouperGroupInfo, DateTime asOfDate) {
    return new ProvisioningWorkItem(WORK_ITEM_COMMAND.FULL_SYNC_GROUP, grouperGroupInfo, asOfDate);
  }

  public static ProvisioningWorkItem createForGroupCleanup(DateTime asOfDate) {
    return new ProvisioningWorkItem(WORK_ITEM_COMMAND.REMOVE_EXTRA_GROUPS, null, asOfDate);
  }



  public ChangeLogEntry getChangelogEntry() {
    return work;
  }
  
  /**
   * Update the status of a work item.
   * 
   * @param logLevel A java.util.logging Level (INFO, WARNING, SEVERE (aka error)). This is 
   * used because slf4j 1.6.1 does not define such constants
   * @param status
   * @param statusMessageFormat
   * @param statusMessageArgs
   */
  private void setStatus(Level logLevel, String status, String statusMessageFormat, Object... statusMessageArgs)
  {
    this.status=status;
    this.statusMessage = String.format(statusMessageFormat, statusMessageArgs);
    
    String msg;
    if ( success ) {
      msg = "Work item handled: {}";
    }
    else {
      msg = "Work item not handled; {}";
    }
      
    // Convert the j.u.logging constants into slf4j log methods
    if ( logLevel == Level.INFO ) {
      LOG.info(msg, this);
    }
    else if ( logLevel == Level.WARNING ) {
      LOG.warn(msg, this);
    }
    else if ( logLevel == Level.SEVERE ) {
      LOG.error(msg, this);
    }
    else {
      LOG.debug(msg, this);
    }
  }

  public void markAsSkippedAndWarn(String statusMessageFormat, Object... statusMessageArgs)
  {
    success = true;
    setStatus(Level.WARNING, "done", statusMessageFormat, statusMessageArgs);
  }

  public void markAsSkipped(String statusMessageFormat, Object... statusMessageArgs)
  {
    if ( success != null ) {
      return;
    }

    success = true;
    setStatus(Level.FINE, "done", statusMessageFormat, statusMessageArgs);
  }

  public void markAsSuccess(String statusMessageFormat, Object... statusMessageArgs)
  {
    success = true;
    setStatus(Level.INFO, "done", statusMessageFormat, statusMessageArgs);
  }
  
  public void markAsFailure(String statusMessageFormat, Object... statusMessageArgs)
  {
    success = false;
    setStatus(Level.SEVERE, "failed", statusMessageFormat, statusMessageArgs);
  }
  
  /** 
   * Has this work item been processed?
   * @return true when this item should not be processed further
   */
  public boolean hasBeenProcessed() {
    // success ivar remains null until work item is processed and marked as success/failure
    return success != null;
  }
  
  
  public String getGroupName() {
    if ( groupName != null )
      return groupName;
    else if ( getChangelogEntry() == null )
      return null;
    
    groupName = ChangelogHandlingConfig.getGroupName(getChangelogEntry());

    return groupName;
  }
  
  public GrouperGroupInfo getGroupInfo(Provisioner provisioner) {
	  String groupName = getGroupName();
	  if ( groupName == null ) {
	    LOG.debug("Group name not found in work item: {}", this);
            return null;
          }

	  GrouperGroupInfo result = provisioner.getGroupInfo(this);

	  LOG.debug("WorkItem {} is related to Group: {}", this, result);
	  return result;
  }

  /**
   * Return the idIndex from the changelog entry
   * @return
   */
  public Long getGroupIdIndex() {
    return  ChangelogHandlingConfig.getGroupId(getChangelogEntry());
  }

  public String getAttributeName() {
    return ChangelogHandlingConfig.getAttributeName(getChangelogEntry());
  }

  
  private String getSubjectId() {
    return ChangelogHandlingConfig.getSubjectId(getChangelogEntry());
  }

  private String getSubjectSourceId() {
    return ChangelogHandlingConfig.getSubjectSource(getChangelogEntry());
  }
  
  public Subject getSubject(Provisioner provisioner) {
    if ( getChangelogEntry() == null )
      return null;
    
    final String subjectId = getSubjectId();
    final String sourceId = getSubjectSourceId();

    if ( subjectId == null || sourceId == null )
      return null;
    
    Subject subject = provisioner.getSubject(subjectId, sourceId);

    return subject;
  }

  public boolean isSubjectUnresolvable(Provisioner provisioner) {
    if ( getSubjectId()==null || getSubjectSourceId()==null ) {
      // This work item does not contain a subject reference at all
      return false;
    }

    // We know that this work item is supposed to reference a subject.
    // Can we find it??
    if ( getSubject(provisioner)==null ) {
      LOG.warn("Subject not found: {}@{}", getSubjectId(), getSubjectSourceId());
      return true;
    }

    // The subject was found. All is good.
    return false;
  }


  public void putProvisioningData(String key, Object value)
  {
    provisioningData.put(key, value);
  }
  
  public Object getProvisioningDataValue(String key)
  {
    return provisioningData.get(key);
  }

  public void addValueToProvisioningData(String key, Object value) {
    List<Object> valueArray = (List) provisioningData.get(key);
    
    if ( valueArray == null )
    {
      synchronized (provisioningData) {
        // Double check to see if another thread might have created the array
        if ( !provisioningData.containsKey(key) )
          provisioningData.put(key, new ArrayList<Object>());
        
        valueArray = (List) provisioningData.get(key);
      }
    }
    
    valueArray.add(value);
  }
  
  public List<Object> getProvisioningDataValues(String key) {
    return (List<Object>) provisioningData.get(key);
  }

  public boolean wasSuccessful() {
    if ( success == null )
      return false;
    
    return success;
  }
  
  public boolean wasError() {
    return !wasSuccessful();
  }

  public String getStatusMessage() {
    return statusMessage;
  }
  
  @Override
  public String toString() {
    ToStringBuilder tsb = new ToStringBuilder(this)
    .append("done", hasBeenProcessed() )
    .append("successful", success)
    .append("msg", statusMessage);
    
    if ( work == null )
      tsb.append("cmd", command);
    else {
      String groupName = getGroupName();
      String subjectId = getSubjectId();
      String subjectSourceId = getSubjectSourceId();
      
      tsb.append("clog", String.format("clog #%d / %s", work.getSequenceNumber(), work.getChangeLogType()));
      if ( groupName != null )
        tsb.append("group", groupName);
      if ( subjectId != null )
        tsb.append("subject", String.format("%s@%s", subjectId, subjectSourceId));
    }
    return tsb.toString();
  }


  public String getMdcLabel() {
    if ( work != null )
      return String.format("%d/", work.getSequenceNumber());
    else
      return String.format("%s/", command);
  }


  /**
   * Does the embedded changelog entry match the given type?
   * @param type
   * @return
   */
  public boolean matchesChangelogType(ChangeLogTypeBuiltin type) {
    if ( getChangelogEntry() == null ) {
      return false;
    }

    return getChangelogEntry().getChangeLogType().equalsCategoryAndAction(type);
  }


  /**
   * Does the embedded changelog entry have a type contained in the given collection of types?
   * @param types
   * @return
   */
  public boolean matchesChangelogType(Collection<ChangeLogTypeBuiltin> types) {
    return ChangelogHandlingConfig.containsChangelogEntryType(types, getChangelogEntry());
  }
}

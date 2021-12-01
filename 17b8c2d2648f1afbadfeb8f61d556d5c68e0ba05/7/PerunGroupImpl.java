package cz.metacentrum.perun.ldapc.model.impl;


import static org.springframework.ldap.query.LdapQueryBuilder.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.Name;
import javax.naming.directory.ModificationItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.support.LdapNameBuilder;

import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.VosManager;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.ldapc.model.PerunAttribute;
import cz.metacentrum.perun.ldapc.model.PerunGroup;
import cz.metacentrum.perun.ldapc.model.PerunResource;
import cz.metacentrum.perun.ldapc.model.PerunUser;
import cz.metacentrum.perun.ldapc.model.PerunVO;

public class PerunGroupImpl extends AbstractPerunEntry<Group> implements PerunGroup {

	private final static Logger log = LoggerFactory.getLogger(PerunGroupImpl.class);

	@Autowired
	private PerunVO vo;
	@Autowired
	@Lazy
	private PerunUser user;
	@Autowired
	private PerunResource perunResource;

	@Override
	protected List<String> getDefaultUpdatableAttributes() {
		return Arrays.asList(
				PerunAttribute.PerunAttributeNames.ldapAttrCommonName,
				PerunAttribute.PerunAttributeNames.ldapAttrPerunUniqueGroupName,
				PerunAttribute.PerunAttributeNames.ldapAttrDescription);
	}

	@Override
	protected List<PerunAttribute<Group>> getDefaultAttributeDescriptions() {
		return 	Arrays.asList(
				new PerunAttributeDesc<>(
						PerunAttribute.PerunAttributeNames.ldapAttrCommonName, 
						PerunAttribute.REQUIRED, 
						(PerunAttribute.SingleValueExtractor<Group>)(group, attrs) -> group.getName()
						),
				new PerunAttributeDesc<>(
						PerunAttribute.PerunAttributeNames.ldapAttrPerunGroupId, 
						PerunAttribute.REQUIRED,
						(PerunAttribute.SingleValueExtractor<Group>)(group, attrs) -> String.valueOf(group.getId())
						),
				new PerunAttributeDesc<>(
						PerunAttribute.PerunAttributeNames.ldapAttrPerunUniqueGroupName,
						PerunAttributeDesc.REQUIRED,
						(PerunAttribute.SingleValueExtractor<Group>)(group, attrs) -> vo.getVoShortName(group.getVoId()) + ":" + group.getName()
						),					
				new PerunAttributeDesc<>(
						PerunAttribute.PerunAttributeNames.ldapAttrPerunVoId,
						PerunAttributeDesc.REQUIRED,
						(PerunAttribute.SingleValueExtractor<Group>)(group, attrs) -> String.valueOf(group.getVoId())
						),
				new PerunAttributeDesc<>(
						PerunAttribute.PerunAttributeNames.ldapAttrDescription,
						PerunAttributeDesc.OPTIONAL,
						(PerunAttribute.SingleValueExtractor<Group>)(group, attrs) -> group.getDescription()
						),
				new PerunAttributeDesc<>(
						PerunAttribute.PerunAttributeNames.ldapAttrPerunParentGroup,
						PerunAttributeDesc.OPTIONAL,
						(PerunAttribute.SingleValueExtractor<Group>)(group, attrs) -> group.getParentGroupId() == null ? null : this.addBaseDN(this.getEntryDN(String.valueOf(group.getVoId()), String.valueOf(group.getParentGroupId()))).toString()
						// PerunAttributeNames.ldapAttrPerunGroupId + "=" + group.getParentGroupId().toString() + "," + PerunAttributeNames.ldapAttrPerunVoId + "=" + group.getVoId() + "," + ldapProperties.getLdapBase()
						),
				new PerunAttributeDesc<>(
						PerunAttribute.PerunAttributeNames.ldapAttrPerunParentGroupId,
						PerunAttributeDesc.OPTIONAL,
						(PerunAttribute.SingleValueExtractor<Group>)(group, attrs) -> group.getParentGroupId() == null ? null : group.getParentGroupId().toString()
						)
				);

	}

	public void addGroup(Group group) throws InternalErrorException {
		addEntry(group);
	}

	public void addGroupAsSubGroup(Group group, Group parentGroup) throws InternalErrorException {
		//This method has the same implementation like 'addGroup'
		addGroup(group);
	}

	public void removeGroup(Group group) throws InternalErrorException {
		Name groupDN = buildDN(group);
		Name fullGroupDN = this.addBaseDN(groupDN);
		DirContextOperations groupEntry = findByDN(groupDN);
		String[] uniqueMembers = groupEntry.getStringAttributes(PerunAttribute.PerunAttributeNames.ldapAttrUniqueMember);
		if(uniqueMembers != null)
			for(String memberDN: uniqueMembers) {
				DirContextOperations memberEntry = user.findByDN(LdapNameBuilder.newInstance(memberDN).build());
				memberEntry.removeAttributeValue(PerunAttribute.PerunAttributeNames.ldapAttrMemberOf, fullGroupDN.toString());
				ldapTemplate.modifyAttributes(memberEntry);
			}
		
		deleteEntry(group);
	}

	@Override
	public void updateGroup(Group group) throws InternalErrorException {
		modifyEntry(group);
	}

	public void addMemberToGroup(Member member, Group group) throws InternalErrorException {
		//Add member to group
		Name groupDN = buildDN(group);
		DirContextOperations groupEntry = findByDN(groupDN);
		Name memberDN = user.getEntryDN(String.valueOf(member.getUserId()));
		Name fullMemberDN = addBaseDN(memberDN);
		if(isMember(groupEntry, fullMemberDN)) return;
		
		groupEntry.addAttributeValue(PerunAttribute.PerunAttributeNames.ldapAttrUniqueMember, fullMemberDN.toString());
		ldapTemplate.modifyAttributes(groupEntry);
		
		//Add member to vo if this group is membersGroup
		if(group.getName().equals(VosManager.MEMBERS_GROUP) && group.getParentGroupId() == null) {
			//Add info to vo
			vo.addMemberToVO(group.getVoId(), member);
		}
		//Add group info to member
		// user->add('memberOf' => groupDN)
		DirContextOperations userEntry = findByDN(memberDN);
		userEntry.addAttributeValue(PerunAttribute.PerunAttributeNames.ldapAttrMemberOf, addBaseDN(groupDN).toString());
		ldapTemplate.modifyAttributes(userEntry);
	}

	public void removeMemberFromGroup(Member member, Group group) throws InternalErrorException {
		//Remove member from group
		Name groupDN = buildDN(group);
		DirContextOperations groupEntry = findByDN(groupDN);
		Name memberDN = user.getEntryDN(String.valueOf(member.getUserId()));
		Name fullMemberDN = addBaseDN(memberDN);
		if(!isMember(groupEntry, fullMemberDN)) return;
		
		groupEntry.removeAttributeValue(PerunAttribute.PerunAttributeNames.ldapAttrUniqueMember, fullMemberDN.toString());
		ldapTemplate.modifyAttributes(groupEntry);

		//Remove member from vo if this group is membersGroup
		if(group.getName().equals(VosManager.MEMBERS_GROUP) && group.getParentGroupId() == null) {
			//Remove info from vo
			vo.removeMemberFromVO(group.getVoId(), member);
		}
		//Remove group info from member
		DirContextOperations userEntry = findByDN(memberDN);
		userEntry.removeAttributeValue(PerunAttribute.PerunAttributeNames.ldapAttrMemberOf, addBaseDN(groupDN).toString());
		ldapTemplate.modifyAttributes(userEntry);
	}

	protected void doSynchronizeMembers(DirContextOperations groupEntry, List<Member> members) {
		List<Name> memberList = new ArrayList<Name>(members.size());
		for (Member member: members) {
			memberList.add(addBaseDN(user.getEntryDN(String.valueOf(member.getUserId()))));
		}
		groupEntry.setAttributeValues(PerunAttribute.PerunAttributeNames.ldapAttrUniqueMember, memberList.stream().map( name -> name.toString() ).toArray(String[]::new));
	}
	
	protected void doSynchronizeResources(DirContextOperations groupEntry, List<Resource> resources) {
		groupEntry.setAttributeValues(PerunAttribute.PerunAttributeNames.ldapAttrAssignedToResourceId, resources.stream().map( resource -> String.valueOf(resource.getId())).toArray(String[]::new));
	}
	
	@Override
	public void synchronizeGroup(Group group, List<Member> members, List<Resource> resources) throws InternalErrorException {
		SyncOperation syncOp = beginSynchronizeEntry(group);
		doSynchronizeMembers(syncOp.getEntry(), members);
		doSynchronizeResources(syncOp.getEntry(), resources);
		commitSyncOperation(syncOp);
	}

	@Override
	public void synchronizeMembers(Group group, List<Member> members) {
		DirContextOperations groupEntry = findByDN(buildDN(group));
		doSynchronizeMembers(groupEntry, members);
		ldapTemplate.modifyAttributes(groupEntry);
		// user attributes are set when synchronizing users
	}

	@Override
	public void synchronizeResources(Group group, List<Resource> resources) {
		DirContextOperations groupEntry = findByDN(buildDN(group));
		doSynchronizeResources(groupEntry, resources);
		ldapTemplate.modifyAttributes(groupEntry);
	}

	public boolean isMember(Member member, Group group) {
		DirContextOperations groupEntry = findByDN(buildDN(group));
		Name userDN = addBaseDN(user.getEntryDN(String.valueOf(member.getUserId())));
		return isMember(groupEntry, userDN);
	}


	@Deprecated
	public List<String> getAllUniqueMembersInGroup(int groupId, int voId) {
		Pattern userIdPattern = Pattern.compile("[0-9]+");
		List<String> uniqueMembers = new ArrayList<String>();
		DirContextOperations groupEntry = findById(String.valueOf(groupId), String.valueOf(voId));
		String[] uniqueGroupInformation = groupEntry.getStringAttributes(PerunAttribute.PerunAttributeNames.ldapAttrUniqueMember);
		if(uniqueGroupInformation != null) {
			for(String s: uniqueGroupInformation) {
				Matcher userIdMatcher = userIdPattern.matcher(s);
				if(userIdMatcher.find()) 
					uniqueMembers.add(s.substring(userIdMatcher.start(), userIdMatcher.end()));
			}
		}
		return uniqueMembers;
	}

	@Override
	protected void mapToContext(Group group, DirContextOperations context) throws InternalErrorException {
		context.setAttributeValue("objectclass", PerunAttribute.PerunAttributeNames.objectClassPerunGroup);
		mapToContext(group, context, getAttributeDescriptions());
	}

	@Override
	protected Name buildDN(Group group) {
		return getEntryDN(String.valueOf(group.getVoId()), String.valueOf(group.getId()));
	}
	
	/**
	 * Get Group DN using VoId and GroupId.
	 *
	 * @param voId vo id
	 * @param groupId group id
	 * @return DN in String
	 */
	@Override
	public Name getEntryDN(String ...id) {
		return LdapNameBuilder.newInstance()
				.add(PerunAttribute.PerunAttributeNames.ldapAttrPerunVoId, id[0])
				.add(PerunAttribute.PerunAttributeNames.ldapAttrPerunGroupId, id[1])
				.build();
	}

	private boolean isMember(DirContextOperations groupEntry, Name userDN) {
		String[] memberOfInformation = groupEntry.getStringAttributes(PerunAttribute.PerunAttributeNames.ldapAttrUniqueMember);
		if(memberOfInformation != null) {
			for(String s: memberOfInformation) {
				Name memberDN = LdapNameBuilder.newInstance(s).build();
				if(memberDN.compareTo(userDN) == 0)
					// TODO should probably cross-check the user.memberOf attribute
					return true;
			}
		}
		return false;
	}

	@Override
	public List<Name> listEntries() throws InternalErrorException {
		return ldapTemplate.search(query().
				where("objectclass").is(PerunAttribute.PerunAttributeNames.objectClassPerunGroup),
				getNameMapper());
	}


}

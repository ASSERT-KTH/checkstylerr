package org.slc.sli.common.ldap;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.ldap.NameAlreadyBoundException;
import org.springframework.ldap.NamingException;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.OrFilter;
import org.springframework.stereotype.Component;

import javax.naming.Name;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import java.util.*;

/**
 * implementation of LdapService interface for basic CRUD and search operations on LDAP directory
 *
 * @author dliu
 *
 */

@Component
public class LdapServiceImpl implements LdapService {
    private static final Logger LOG = LoggerFactory.getLogger(LdapServiceImpl.class);

    public static final String OBJECTCLASS = "objectclass";
    @Autowired
    private LdapTemplate ldapTemplate;

    @Value("${sli.simple-idp.userSearchAttribute}")
    private String userSearchAttribute;

    @Value("${sli.simple-idp.userObjectClass}")
    private String userObjectClass;

    @Value("${sli.simple-idp.groupSearchAttribute}")
    private String groupSearchAttribute;

    @Value("${sli.simple-idp.groupObjectClass}")
    private String groupObjectClass;

    private static final String USER_ID_NUMBER = "500";
    private static final String GROUP_ID_NUMBER = "113";
    private static final String LOGIN_SHELL = "/sbin/nologin";

    @SuppressWarnings("rawtypes")
    @Override
    public User getUser(String realm, String uid) {
        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter(OBJECTCLASS, userObjectClass)).and(new EqualsFilter(userSearchAttribute, uid));
        DistinguishedName dn = new DistinguishedName("ou=" + realm);
        User user;
        try {
            List userList = ldapTemplate.search(dn, filter.toString(), SearchControls.SUBTREE_SCOPE, new String[] {
                    "*", CREATE_TIMESTAMP, MODIFY_TIMESTAMP }, new UserContextMapper());
            if (userList == null || userList.size() == 0) {
                throw new EmptyResultDataAccessException(1);
            } else if (userList.size() > 1) {
                throw new IncorrectResultSizeDataAccessException("User must be unique", 1);
            }
            user = (User) userList.get(0);
            user.setUid(uid);
            user.setGroups(getGroupNames(getUserGroups(realm, uid)));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
        return user;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<Group> getUserGroups(String realm, String uid) {
        DistinguishedName dn = new DistinguishedName("ou=" + realm);
        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter(OBJECTCLASS, groupObjectClass)).and(new EqualsFilter(groupSearchAttribute, uid));
        List<Group> groups = ldapTemplate.search(dn, filter.toString(), new GroupContextMapper());
        return groups;
    }

    @Override
    public void removeUser(String realm, String uid) {
        Collection<Group> groups = getUserGroups(realm, uid);
        User oldUser = getUser(realm, uid);
        if (oldUser != null) {
            ldapTemplate.unbind(buildUserDN(realm, oldUser.getCn()));
        }

        if (groups != null && groups.size() > 0) {
            for (Group group : groups) {
                removeUserFromGroup(realm, group, oldUser);
            }
        }
    }

    @Override
    public String createUser(String realm, User user) throws NameAlreadyBoundException {
        user.setCn(user.getUid());
        ldapTemplate.bind(createUserContext(realm, user));
        List<String> groupNames = user.getGroups();
        if (groupNames != null && groupNames.size() > 0) {
            for (String groupName : groupNames) {
                Group group = getGroup(realm, groupName);
                addUserToGroup(realm, group, user);
            }
        }
        return user.getUid();
    }

    @Override
    public boolean updateUser(String realm, User user) {
        User oldUser = getUser(realm, user.getUid());
        if (oldUser == null) {
            return false;
        }
        Collection<Group> oldGroups = getUserGroups(realm, user.getUid());
        DirContextAdapter context = (DirContextAdapter) ldapTemplate.lookupContext(buildUserDN(realm, oldUser.getCn()));
        boolean isCreate = false;
        mapUserToContext(context, user, isCreate);
        ldapTemplate.modifyAttributes(context);
        Collection<String> newGroupNames = user.getGroups();
        Collection<String> oldGroupNames = getGroupNames(oldGroups);

        if (oldGroups != null && oldGroups.size() > 0) {
            for (Group oldGroup : oldGroups) {
                if (!newGroupNames.contains(oldGroup.getGroupName())) {
                    removeUserFromGroup(realm, oldGroup, user);
                }
            }
        }

        if (newGroupNames != null && newGroupNames.size() > 0) {
            for (String newGroupName : newGroupNames) {
                if (!oldGroupNames.contains(newGroupName)) {
                    Group newGroup = getGroup(realm, newGroupName);
                    addUserToGroup(realm, newGroup, user);
                }
            }
        }
        return true;
    }

    @Override
    public Collection<User> findUsersByGroups(String realm, final Collection<String> allowedGroupNames,
                                              final Collection<String> disallowedGroupNames, String tenant, Collection<String> edorgs) {

        Collection<String> allowed = allowedGroupNames;
        Collection<String> disallowed = disallowedGroupNames;

        if (allowed == null) {
            allowed = new LinkedList<String>();
        }
        if (disallowed == null) {
            disallowed = new LinkedList<String>();
        }

        Set<String> allowedUsers = new HashSet<String>();

        Map<String, List<String>> uidToGroupsMap = new HashMap<String, List<String>>();
        for (String groupName : allowed) {
            Group group = getGroup(realm, groupName);
            if (group != null) {
                List<String> memberUids = group.getMemberUids();

                if (memberUids != null && memberUids.size() > 0) {
                    for (String memberUid : memberUids) {
                        if (uidToGroupsMap.containsKey(memberUid)) {
                            uidToGroupsMap.get(memberUid).add(groupName);
                        } else {
                            List<String> uidGroupNames = new ArrayList<String>();
                            uidGroupNames.add(groupName);
                            uidToGroupsMap.put(memberUid, uidGroupNames);
                        }
                        allowedUsers.add(memberUid);
                    }
                }
            }
        }

        for (String groupName : disallowed) {
            Group group = getGroup(realm, groupName);
            if (group != null) {
                allowedUsers.removeAll(group.getMemberUids());
            }
        }

        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter(OBJECTCLASS, userObjectClass));
        OrFilter orFilter = new OrFilter();
        for (String uid : allowedUsers) {
            orFilter.or(new EqualsFilter(userSearchAttribute, uid));
        }
        filter.and(orFilter);
        DistinguishedName dn = new DistinguishedName("ou=" + realm);
        @SuppressWarnings("unchecked")
        Collection<User> users = (ldapTemplate.search(dn, filter.toString(), SearchControls.SUBTREE_SCOPE,
              new String[] { "*", CREATE_TIMESTAMP, MODIFY_TIMESTAMP }, new UserContextMapper()));
        for (User user : users) {
            user.setGroups(uidToGroupsMap.get(user.getUid()));
        }

        if (tenant != null) {
            users = filterByTenant(users, tenant);
        }

        if (edorgs != null) {
            users = filterByEdorgs(users, edorgs);
        }

        return users;
    }

    @Override
    public Collection<User> findUsersByGroups(String realm, Collection<String> groupNames) {
        return findUsersByGroups(realm, groupNames, null, null, null);
    }

    @Override
    public Collection<User> findUsersByGroups(String realm, Collection<String> groupNames, String tenant) {
        return findUsersByGroups(realm, groupNames, null, tenant, null);
    }

    @Override
    public Collection<User> findUsersByAttributes(String realm, Collection<String> attributes) {
        return null;
    }

    @Override
    public Group getGroup(String realm, String groupName) {
        DistinguishedName dn = new DistinguishedName("ou=" + realm);
        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter(OBJECTCLASS, groupObjectClass)).and(new EqualsFilter("cn", groupName));
        try {
            return (Group) ldapTemplate.searchForObject(dn, filter.toString(), new GroupContextMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public Collection<User> findUsersByGroups(String realm, Collection<String> groupNames, String tenant,
                                              Collection<String> edorgs) {
        return findUsersByGroups(realm, groupNames, null, tenant, edorgs);
    }

    @Override
    public boolean addUserToGroup(String realm, Group group, User user) {
        return toggleUserInGroup(realm, group, user, DirContext.ADD_ATTRIBUTE);
    }

    private boolean toggleUserInGroup(String realm, Group group, User user, int op) {
        BasicAttribute member = new BasicAttribute("memberUid", user.getUid());
        ModificationItem[] modGroups = new ModificationItem[] {
              new ModificationItem(op, member) };

        Name groupName = buildGroupDN(realm, group.getGroupName());

        try {
            ldapTemplate.modifyAttributes(groupName, modGroups);
        } catch (NamingException e) {
            return false;
        }

        return true;
    }

    @Override
    public boolean removeUserFromGroup(String realm, Group group, User user) {
        return toggleUserInGroup(realm, group, user, DirContext.REMOVE_ATTRIBUTE);
    }

    @Override
    public boolean updateGroup(String realm, Group group) {
        DirContextAdapter context = (DirContextAdapter) ldapTemplate.lookupContext(buildGroupDN(realm,
              group.getGroupName()));
        mapGroupToContext(context, group);
        ldapTemplate.modifyAttributes(context);
        return true;
    }

    private Collection<User> filterByTenant(Collection<User> users, String tenant) {
        if (tenant == null || users == null) {
            return users;
        }

        List<User> filteredUsers = new ArrayList<User>();
        for (User user : users) {
            if (tenant.equals(user.getTenant())) {
                filteredUsers.add(user);
            }
        }
        return filteredUsers;
    }

    private Collection<User> filterByEdorgs(Collection<User> users, Collection<String> edorgs) {
        if (edorgs == null || edorgs.size() == 0 || users == null) {
            return users;
        }
        Collection<User> filteredUsers = new ArrayList<User>();
        for (User user : users) {
            if (edorgs.contains(user.getEdorg())) {
                filteredUsers.add(user);
            }
        }
        return filteredUsers;
    }

    private DirContextAdapter createUserContext(String realm, User user) {
        DirContextAdapter context = new DirContextAdapter(buildUserDN(realm, user));
        boolean isCreate = true;
        mapUserToContext(context, user, isCreate);
        context.setAttributeValue("cn", user.getCn());
        return context;
    }

    private DistinguishedName buildUserDN(String realm, User user) {
        return buildUserDN(realm, user.getCn());
    }

    private DistinguishedName buildUserDN(String realm, String cn) {
        return new DistinguishedName("cn=" + cn + ",ou=people,ou=" + realm);
    }

    private DistinguishedName buildGroupDN(String realm, String groupName) {
        return new DistinguishedName("cn=" + groupName + ",ou=groups,ou=" + realm);
    }

    /**
     * This method is used for both create and update operations.  For updates, we do not want certain attributes overridden which could bork the LDAP server.
     * @param context
     * @param user
     * @param isCreate boolean to identify if the context is for a create or update.
     */

    private void mapUserToContext(DirContextAdapter context, User user, final boolean isCreate) {

        LOG.debug("Before mapUserToContext: " + Boolean.toString(isCreate) + ToStringBuilder.reflectionToString(context, ToStringStyle.MULTI_LINE_STYLE));

        // TAF 2014-04-01 : Commented out for updates.  Why override the ObjectClasses that are returned from LDAP?  Should respect what hte LDAP server returns for updates.
        if (isCreate) {
            LOG.debug("mapUserToContext (create) -- set OBJECTCLASS array.");
            context.setAttributeValues(OBJECTCLASS, new String[]{"inetOrgPerson", "posixAccount", "top"});
        }

        context.setAttributeValue("givenName", user.getGivenName());

        String surName = user.getSn();

        context.setAttributeValue("sn", surName == null ? " " : surName);
        context.setAttributeValue("uid", user.getUid());
        context.setAttributeValue("uidNumber", USER_ID_NUMBER);
        context.setAttributeValue("gidNumber", GROUP_ID_NUMBER);
        context.setAttributeValue("loginShell", LOGIN_SHELL);
        context.setAttributeValue("mail", user.getEmail());
        context.setAttributeValue("homeDirectory", user.getHomeDir());

        if (user.getStatus() != null && user.getStatus().getStatusString() != null) {
            context.setAttributeValue("destinationindicator", user.getStatus().getStatusString());
        }
        String description = "";
        if (user.getTenant() != null) {
            description += "tenant=" + user.getTenant();
        }
        if (user.getEdorg() != null) {
            description += ",edOrg=" + user.getEdorg();
        }
        if (!"".equals(description)) {
            context.setAttributeValue("description", "tenant=" + user.getTenant() + "," + "edOrg=" + user.getEdorg());
        }
        if (user.getVendor() != null) {
            context.setAttributeValue("o", user.getVendor());
        }

        LOG.debug("After mapUserToContext:  " + ToStringBuilder.reflectionToString(context, ToStringStyle.MULTI_LINE_STYLE));

    }

    private void mapGroupToContext(DirContextAdapter context, Group group) {
        context.setAttributeValues("memberUid", group.getMemberUids().toArray());
    }

    private List<String> getGroupNames(Collection<Group> groups) {
        List<String> groupNames = new ArrayList<String>();
        if (groups != null) {
            for (Group group : groups) {
                groupNames.add(group.getGroupName());
            }
        }
        return groupNames;
    }

    @Override
    public void setLdapTemplate(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }
}
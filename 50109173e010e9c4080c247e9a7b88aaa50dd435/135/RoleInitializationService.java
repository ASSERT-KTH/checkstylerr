package com.ctrip.framework.apollo.portal.service;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import com.ctrip.framework.apollo.common.entity.App;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.portal.constant.PermissionType;
import com.ctrip.framework.apollo.portal.constant.RoleType;
import com.ctrip.framework.apollo.portal.entity.po.Permission;
import com.ctrip.framework.apollo.portal.entity.po.Role;
import com.ctrip.framework.apollo.portal.spi.UserInfoHolder;
import com.ctrip.framework.apollo.portal.util.RoleUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class RoleInitializationService {

  @Autowired
  private UserInfoHolder userInfoHolder;
  @Autowired
  private RolePermissionService rolePermissionService;

  @Transactional
  public void initAppRoles(App app) {
    String appId = app.getAppId();

    String appMasterRoleName = RoleUtils.buildAppMasterRoleName(appId);

    //has created before
    if (rolePermissionService.findRoleByRoleName(appMasterRoleName) != null) {
      return;
    }
    String operator = userInfoHolder.getUser().getUserId();
    //create app permissions
    createAppMasterRole(appId);

    //assign master role to user
    rolePermissionService
        .assignRoleToUsers(RoleUtils.buildAppMasterRoleName(appId), Sets.newHashSet(app.getOwnerName()),
            operator);

    initNamespaceRoles(appId, ConfigConsts.NAMESPACE_APPLICATION);

    //assign modify、release namespace role to user
    rolePermissionService.assignRoleToUsers(RoleUtils.buildNamespaceRoleName(appId, ConfigConsts.NAMESPACE_APPLICATION, RoleType.MODIFY_NAMESPACE),
                                            Sets.newHashSet(operator), operator);
    rolePermissionService.assignRoleToUsers(RoleUtils.buildNamespaceRoleName(appId, ConfigConsts.NAMESPACE_APPLICATION, RoleType.RELEASE_NAMESPACE),
                                            Sets.newHashSet(operator), operator);

  }

  @Transactional
  public void initNamespaceRoles(String appId, String namespaceName) {

    String modifyNamespaceRoleName = RoleUtils.buildModifyNamespaceRoleName(appId, namespaceName);
    if (rolePermissionService.findRoleByRoleName(modifyNamespaceRoleName) == null) {
      createDefaultNamespaceRole(appId, namespaceName, PermissionType.MODIFY_NAMESPACE,
          RoleUtils.buildModifyNamespaceRoleName(appId, namespaceName));
    }

    String releaseNamespaceRoleName = RoleUtils.buildReleaseNamespaceRoleName(appId, namespaceName);
    if (rolePermissionService.findRoleByRoleName(releaseNamespaceRoleName) == null) {
      createDefaultNamespaceRole(appId, namespaceName, PermissionType.RELEASE_NAMESPACE,
          RoleUtils.buildReleaseNamespaceRoleName(appId, namespaceName));
    }
  }

  private void createAppMasterRole(String appId) {
    Set<Permission> appPermissions =
        FluentIterable.from(Lists.newArrayList(
            PermissionType.CREATE_CLUSTER, PermissionType.CREATE_NAMESPACE, PermissionType.ASSIGN_ROLE))
            .transform(permissionType -> createPermisson(appId, permissionType)).toSet();
    Set<Permission> createdAppPermissions = rolePermissionService.createPermissions(appPermissions);
    Set<Long>
        appPermissionIds =
        FluentIterable.from(createdAppPermissions).transform(permission -> permission.getId()).toSet();

    //create app master role
    Role appMasterRole = createRole(RoleUtils.buildAppMasterRoleName(appId));

    rolePermissionService.createRoleWithPermissions(appMasterRole, appPermissionIds);
  }

  private Permission createPermisson(String targetId, String permisson) {
    Permission permission = new Permission();
    permission.setPermissionType(permisson);
    permission.setTargetId(targetId);
    String userId = userInfoHolder.getUser().getUserId();
    permission.setDataChangeCreatedBy(userId);
    permission.setDataChangeLastModifiedBy(userId);
    return permission;
  }

  private Role createRole(String roleName) {
    Role role = new Role();
    role.setRoleName(roleName);
    String operaterUserId = userInfoHolder.getUser().getUserId();
    role.setDataChangeCreatedBy(operaterUserId);
    role.setDataChangeLastModifiedBy(operaterUserId);
    return role;
  }

  private void createDefaultNamespaceRole(String appId, String namespaceName, String permissionType, String roleName) {

    Permission permisson =
        createPermisson(RoleUtils.buildNamespaceTargetId(appId, namespaceName), permissionType);
    Permission createdPermission = rolePermissionService.createPermission(permisson);

    Role role = createRole(roleName);
    rolePermissionService
        .createRoleWithPermissions(role, Sets.newHashSet(createdPermission.getId()));
  }
}

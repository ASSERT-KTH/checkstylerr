package com.ctrip.framework.apollo.portal.controller;

import com.google.common.collect.Sets;

import com.ctrip.framework.apollo.core.exception.BadRequestException;
import com.ctrip.framework.apollo.portal.auth.UserInfoHolder;
import com.ctrip.framework.apollo.portal.constant.RoleType;
import com.ctrip.framework.apollo.portal.entity.po.UserInfo;
import com.ctrip.framework.apollo.portal.entity.vo.AppRolesAssignedUsers;
import com.ctrip.framework.apollo.portal.entity.vo.NamespaceRolesAssignedUsers;
import com.ctrip.framework.apollo.portal.entity.vo.PermissionCondition;
import com.ctrip.framework.apollo.portal.service.RolePermissionService;
import com.ctrip.framework.apollo.portal.util.RoleUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

import static com.ctrip.framework.apollo.portal.util.RequestPrecondition.checkArgument;


@RestController
public class PermissionController {

  @Autowired
  private UserInfoHolder userInfoHolder;
  @Autowired
  private RolePermissionService rolePermissionService;

  @RequestMapping("/apps/{appId}/permissions/{permissionType}")
  public ResponseEntity<PermissionCondition> hasPermission(@PathVariable String appId, @PathVariable String permissionType) {
    PermissionCondition permissionCondition = new PermissionCondition();

    permissionCondition.setHasPermission(
        rolePermissionService.userHasPermission(userInfoHolder.getUser().getUserId(), permissionType, appId));

    return ResponseEntity.ok().body(permissionCondition);
  }

  @RequestMapping("/apps/{appId}/namespaces/{namespaceName}/permissions/{permissionType}")
  public ResponseEntity<PermissionCondition> hasPermission(@PathVariable String appId, @PathVariable String namespaceName,
                                                           @PathVariable String permissionType) {
    PermissionCondition permissionCondition = new PermissionCondition();

    permissionCondition.setHasPermission(
        rolePermissionService.userHasPermission(userInfoHolder.getUser().getUserId(), permissionType,
                                                RoleUtils.buildNamespaceTargetId(appId, namespaceName)));

    return ResponseEntity.ok().body(permissionCondition);
  }


  @RequestMapping("/apps/{appId}/namespaces/{namespaceName}/role_users")
  public NamespaceRolesAssignedUsers getNamespaceRoles(@PathVariable String appId, @PathVariable String namespaceName){

    NamespaceRolesAssignedUsers assignedUsers = new NamespaceRolesAssignedUsers();
    assignedUsers.setNamespaceName(namespaceName);
    assignedUsers.setAppId(appId);

    Set<UserInfo> releaseNamespaceUsers =
        rolePermissionService.queryUsersWithRole(RoleUtils.buildReleaseNamespaceRoleName(appId, namespaceName));
    assignedUsers.setReleaseRoleUsers(releaseNamespaceUsers);

    Set<UserInfo> modifyNamespaceUsers =
        rolePermissionService.queryUsersWithRole(RoleUtils.buildModifyNamespaceRoleName(appId, namespaceName));
    assignedUsers.setModifyRoleUsers(modifyNamespaceUsers);

    return assignedUsers;
  }

  @PreAuthorize(value = "@permissionValidator.hasAssignRolePermission(#appId)")
  @RequestMapping(value = "/apps/{appId}/namespaces/{namespaceName}/roles/{roleType}", method = RequestMethod.POST)
  public ResponseEntity<Void> assignNamespaceRoleToUser(@PathVariable String appId, @PathVariable String namespaceName,
                                                        @PathVariable String roleType, @RequestBody String user){

    checkArgument(user);

    if (!RoleType.isValidRoleType(roleType)){
      throw new BadRequestException("role type is illegal");
    }
    rolePermissionService.assignRoleToUsers(RoleUtils.buildNamespaceRoleName(appId, namespaceName, roleType),
                                            Sets.newHashSet(user), userInfoHolder.getUser().getUserId());

    return ResponseEntity.ok().build();
  }

  @PreAuthorize(value = "@permissionValidator.hasAssignRolePermission(#appId)")
  @RequestMapping(value = "/apps/{appId}/namespaces/{namespaceName}/roles/{roleType}", method = RequestMethod.DELETE)
  public ResponseEntity<Void> removeNamespaceRoleFromUser(@PathVariable String appId, @PathVariable String namespaceName,
                                                 @PathVariable String roleType, @RequestParam String user){
    checkArgument(user);

    if (!RoleType.isValidRoleType(roleType)){
      throw new BadRequestException("role type is illegal");
    }
    rolePermissionService.removeRoleFromUsers(RoleUtils.buildNamespaceRoleName(appId, namespaceName, roleType),
                                              Sets.newHashSet(user), userInfoHolder.getUser().getUserId());
    return ResponseEntity.ok().build();
  }

  @RequestMapping(value = "/apps/{appId}/role_users")
  public AppRolesAssignedUsers getAppRoles(@PathVariable String appId){
    AppRolesAssignedUsers users = new AppRolesAssignedUsers();
    users.setAppId(appId);

    Set<UserInfo> masterUsers = rolePermissionService.queryUsersWithRole(RoleUtils.buildAppMasterRoleName(appId));
    users.setMasterUsers(masterUsers);

    return users;
  }

  @PreAuthorize(value = "@permissionValidator.hasAssignRolePermission(#appId)")
  @RequestMapping(value = "/apps/{appId}/roles/{roleType}", method = RequestMethod.POST)
  public ResponseEntity<Void> assignAppRoleToUser(@PathVariable String appId, @PathVariable String roleType,
                                                  @RequestBody String user){

    checkArgument(user);

    if (!RoleType.isValidRoleType(roleType)){
      throw new BadRequestException("role type is illegal");
    }
    rolePermissionService.assignRoleToUsers(RoleUtils.buildAppRoleName(appId, roleType),
                                            Sets.newHashSet(user), userInfoHolder.getUser().getUserId());

    return ResponseEntity.ok().build();
  }

  @PreAuthorize(value = "@permissionValidator.hasAssignRolePermission(#appId)")
  @RequestMapping(value = "/apps/{appId}/roles/{roleType}", method = RequestMethod.DELETE)
  public ResponseEntity<Void> removeAppRoleFromUser(@PathVariable String appId, @PathVariable String roleType,
                                                 @RequestParam String user){
    checkArgument(user);

    if (!RoleType.isValidRoleType(roleType)){
      throw new BadRequestException("role type is illegal");
    }
    rolePermissionService.removeRoleFromUsers(RoleUtils.buildAppRoleName(appId, roleType),
                                              Sets.newHashSet(user), userInfoHolder.getUser().getUserId());
    return ResponseEntity.ok().build();
  }


}

package com.ctrip.framework.apollo.biz.service;

import com.ctrip.framework.apollo.biz.entity.Audit;
import com.ctrip.framework.apollo.biz.entity.Privilege;
import com.ctrip.framework.apollo.biz.repository.PrivilegeRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PrivilegeService {

  enum PrivilType {
    EDIT, REVIEW, RELEASE
  }

  @Autowired
  private PrivilegeRepository privilRepo;

  @Autowired
  private AuditService auditService;

  @Transactional
  public Privilege addPrivilege(long namespaceId, String name, PrivilType privilType) {
    Privilege privil =
        privilRepo.findByNamespaceIdAndNameAndPrivilType(namespaceId, name, privilType.name());
    if (privil == null) {
      privil = new Privilege();
      privil.setNamespaceId(namespaceId);
      privil.setPrivilType(privilType.name());
      privil.setName(name);
      privilRepo.save(privil);

      auditService.audit(Privilege.class.getSimpleName(), privil.getId(), Audit.OP.INSERT, name);
    }
    return privil;
  }

  public boolean hasPrivilege(long namespaceId, String name, PrivilType privilType) {
    Privilege privil =
        privilRepo.findByNamespaceIdAndNameAndPrivilType(namespaceId, name, privilType.name());
    return (privil != null) ? true : false;
  }

  public List<Privilege> listPrivileges(long namespaceId) {
    return privilRepo.findByNamespaceId(namespaceId);
  }

  @Transactional
  public void removePrivilege(long namespaceId, String name, PrivilType privilType) {
    Privilege privil =
        privilRepo.findByNamespaceIdAndNameAndPrivilType(namespaceId, name, privilType.name());
    if (privil != null) {
      privilRepo.delete(privil);

      auditService.audit(Privilege.class.getSimpleName(), privil.getId(), Audit.OP.DELETE, name);
    }
  }
}

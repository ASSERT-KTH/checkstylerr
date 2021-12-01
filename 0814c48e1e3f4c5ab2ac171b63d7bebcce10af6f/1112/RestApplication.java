package edu.internet2.middleware.grouper.ws.scim;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import edu.internet2.middleware.grouper.ws.scim.membership.MembershipRestResourceImpl;
import edu.psu.swe.scim.server.rest.ScimResourceHelper;

@ApplicationPath("v2")
public class RestApplication extends Application {
  
  @Override
  public Set<Class<?>> getClasses() {
    Set<Class<?>> clazzes = new HashSet<Class<?>>();
    
    clazzes.addAll(ScimResourceHelper.getScimClassesToLoad());
    clazzes.addAll(ScimResourceHelper.getSwaggerClassesToLoad());
    clazzes.add(MembershipRestResourceImpl.class);

    return clazzes;
  }

}

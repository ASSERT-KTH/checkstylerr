/**
 * 
 */
package edu.internet2.middleware.grouper.ws.scim;

import static javax.servlet.http.HttpServletResponse.SC_OK;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * @author vsachdeva
 *
 */
@WebFilter(filterName = "TierFilter", urlPatterns = {"/v2/*"})
public class TierFilter implements Filter {
  
  /**
   * thread local to store request start time in miilis
   */
  private static ThreadLocal<Long> threadLocalRequestStartMillis = new ThreadLocal<Long>();
  
  /**
   * thread local for request
   */
  private static ThreadLocal<HttpServletRequest> threadLocalRequest = new ThreadLocal<HttpServletRequest>();

  /**
   * if in request, get the start time
   * @return the start time
   */
  public static long retrieveRequestStartMillis() {
    Long requestStartMillis = threadLocalRequestStartMillis.get();
    return GrouperUtil.longValue(requestStartMillis, 0);
  }
  
  public static Subject retrieveSubjectFromRemoteUser() {
    GrouperSession rootSession = null;
    Subject subject = null;
    try {
      HttpServletRequest httpServletRequest = threadLocalRequest.get();
      
      
      Principal principal = httpServletRequest.getUserPrincipal();
      String principalName = null;
      if (principal == null) {
        principalName = httpServletRequest.getRemoteUser();
        if (StringUtils.isBlank(principalName)) {
          principalName = (String)httpServletRequest.getAttribute("REMOTE_USER");
        }
      } else {
        principalName = principal.getName();
      }
      
      if (principalName == null) {
        return null;
      }
      final String userId = principalName;
      rootSession = GrouperSession.startRootSession();
      subject = (Subject)GrouperSession.callbackGrouperSession(rootSession, new GrouperSessionHandler() {
        
        /**
         * we need a grouper session since subject searching also looks at groups
         * @param callbackGrouperSession
         */
        @Override
        public Object callback(GrouperSession callbackGrouperSession) throws GrouperSessionException {
          return SubjectFinder.findByIdOrIdentifier(userId, false);
        }
      });
      
      if (subject == null) {
       return null;
      }
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
    return subject;
  }
  
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
      throws IOException, ServletException {
    
    try {
      if (((HttpServletRequest) request).getMethod().equals("OPTIONS")) {
        ((HttpServletResponse) response).addHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS");
        ((HttpServletResponse) response).addHeader("Access-Control-Allow-Origin", "*");
        ((HttpServletResponse) response).addHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        ((HttpServletResponse) response).setStatus(SC_OK);
        return;
      }
      threadLocalRequestStartMillis.set(System.currentTimeMillis());
      threadLocalRequest.set((HttpServletRequest)request);
      
      if (retrieveSubjectFromRemoteUser() == null) {
        ((HttpServletResponse) response).setStatus(SC_UNAUTHORIZED);
        return;
      }
      
      chain.doFilter(request, response);
    } finally {
      threadLocalRequestStartMillis.remove();
      threadLocalRequest.remove();
    }
    
  }
  
  @Override
  public void destroy() {}
  
  @Override
  public void init(FilterConfig filterConfig) throws ServletException {}

}

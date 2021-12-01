/*
 * Copyright 2012-2013 inBloom, Inc. and its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.slc.sli.api.jersey.exceptionhandlers;

import org.slc.sli.api.representation.ErrorResponse;
import org.slc.sli.api.security.SLIPrincipal;
import org.slc.sli.api.security.SecurityEventBuilder;
import org.slc.sli.api.exceptions.APIAccessDeniedException;
import org.slc.sli.api.security.service.AuditLogger;
import org.slc.sli.common.constants.EntityNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * Handler for catching API access denied exceptions that log security events.
 *
 * @author bsuzuki
 */
@Provider
@Component
public class APIAccessDeniedExceptionHandler implements ExceptionMapper<APIAccessDeniedException> {

    private static final Logger LOG = LoggerFactory.getLogger(APIAccessDeniedExceptionHandler.class);

    @Autowired
    private SecurityEventBuilder securityEventBuilder;

    @Autowired
    private AuditLogger auditLogger;

    @Context
    UriInfo uriInfo;

    @Context
    private HttpHeaders headers;

    @Context
    private HttpServletResponse response;

    @Override
    public Response toResponse(APIAccessDeniedException e) {
        //There are a few jax-rs resources that generate HTML content, and we want the
        //default web-container error handler pages to get used in those cases.
        if (headers.getAcceptableMediaTypes().contains(MediaType.TEXT_HTML_TYPE)) {
            try {
                response.sendError(403, e.getMessage());
                logSecurityEvent(e);
                return null;    //the error page handles the response, so no need to return a response
            } catch (IOException ex) {
                LOG.error("Error displaying error page", ex);
            }
        }

        Response.Status errorStatus = Response.Status.FORBIDDEN;
        SLIPrincipal principal = null ;
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            principal = (SLIPrincipal)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            LOG.warn("Access has been denied to user: {}",principal );
        } else {
            LOG.warn("Access has been denied to user for being incorrectly associated");
        }
        LOG.warn("Cause: {}", e.getMessage());

        logSecurityEvent(e);

        MediaType errorType = MediaType.APPLICATION_JSON_TYPE;
        if(this.headers.getMediaType() == MediaType.APPLICATION_XML_TYPE) {
            errorType = MediaType.APPLICATION_XML_TYPE;
        }
        
        return Response.status(errorStatus).entity(new ErrorResponse(errorStatus.getStatusCode(), errorStatus.getReasonPhrase(), "Access DENIED: " + e.getMessage())).type(errorType).build();
    }

    private void logSecurityEvent(APIAccessDeniedException e) {

        if (e.getTargetEdOrgIds() != null) {
            // if we already have the target edOrgs - good to go
            auditLogger.audit(securityEventBuilder.createSecurityEvent(getThrowingClassName(e), uriInfo.getRequestUri(), "Access Denied:"
                    + e.getMessage(), e.getRealm(), EntityNames.EDUCATION_ORGANIZATION, e.getTargetEdOrgIds().toArray(new String[0])));

        } else if (e.getEntityType() != null) {

            if (e.getEntities() != null && !e.getEntities().isEmpty()) {
                auditLogger.audit(securityEventBuilder.createSecurityEvent(getThrowingClassName(e), uriInfo.getRequestUri(), "Access Denied:"
                        + e.getMessage(), e.getRealm(), e.getEntityType(), e.getEntities()));

            } else if (e.getEntityIds() != null && !e.getEntityIds().isEmpty()) {
                auditLogger.audit(securityEventBuilder.createSecurityEvent(getThrowingClassName(e), uriInfo.getRequestUri(), "Access Denied:"
                        + e.getMessage(), e.getRealm(), e.getEntityType(), e.getEntityIds().toArray(new String[0])));
            } else {
                auditLogger.audit(securityEventBuilder.createSecurityEvent(getThrowingClassName(e), uriInfo.getRequestUri(), "Access Denied:"
                        + e.getMessage(), e.getPrincipal(), e.getClientId(), e.getRealm(), null, e.isTargetIsUserEdOrg()));
            }
        } else {
            auditLogger.audit(securityEventBuilder.createSecurityEvent(getThrowingClassName(e), uriInfo.getRequestUri(), "Access Denied:"
                    + e.getMessage(), e.getPrincipal(), e.getClientId(), e.getRealm(), null, e.isTargetIsUserEdOrg()));
        }
    }

    private String getThrowingClassName(Exception e) {
        if (e != null && e.getStackTrace() != null) {
            StackTraceElement ste = e.getStackTrace()[0];
            if (ste != null) {
                return ste.getClassName();
            }
        }
        return null;
    }
}

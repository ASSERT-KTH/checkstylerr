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

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slc.sli.api.representation.CustomStatus;
import org.slc.sli.api.representation.ErrorResponse;
import org.slc.sli.api.security.context.ResponseTooLargeException;
import org.springframework.stereotype.Component;

/**
 * Hander for when the request is too large to manage
 */
@Provider
@Component
public class ResponseTooLargeExceptionHandler implements ExceptionMapper<ResponseTooLargeException> {
    
    @Override
    public Response toResponse(ResponseTooLargeException exception) {
        
        return Response
                .status(CustomStatus.ENTITY_TOO_LARGE)
                .entity(new ErrorResponse(CustomStatus.ENTITY_TOO_LARGE.getStatusCode(), CustomStatus.ENTITY_TOO_LARGE.getReasonPhrase(),
                        "The request is too large to resolve.")).build();
    }
}

/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.metron.rest.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

  @ApiOperation(value = "Retrieves the current user")
  @ApiResponse(message = "Current user", code = 200)
  @RequestMapping(method = RequestMethod.GET)
    public String user(Principal user) {
        return user.getName();
    }

  @Secured("IS_AUTHENTICATED_FULLY")
  @RequestMapping(path = "/whoami/roles", method = RequestMethod.GET)
  public List<String> user() {
    UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().
        getAuthentication().getPrincipal();
    return userDetails.getAuthorities().stream().map(ga -> ga.getAuthority()).collect(Collectors.toList());
  }
}

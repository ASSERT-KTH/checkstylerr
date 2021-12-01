/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicecomb.it.schema;

import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;

@RestSchema(schemaId = "defaultValueSpringmvc")
@RequestMapping(path = "/v1/defaultValueSpringmvc")
public class DefaultValueSpringmvcSchema {
  @GetMapping(path = "intQuery")
  public int intQuery(@RequestParam(name = "input", required = false, defaultValue = "13") int input) {
    return input;
  }

  @GetMapping(path = "intHeader")
  public int intHeader(@RequestHeader(name = "input", required = false, defaultValue = "13") int input) {
    return input;
  }

  @ApiImplicitParams({
      @ApiImplicitParam(name = "input", dataType = "integer", format = "int32", paramType = "form", value = "", defaultValue = "13", required = false)})
  @PostMapping(path = "intForm")
  public int intForm(int input) {
    return input;
  }

  // springmvc rule: required should be false because defaultValue have value
  @GetMapping(path = "intQueryRequire")
  public int intQueryRequire(@RequestParam(name = "input", required = true, defaultValue = "13") int input) {
    return input;
  }

  // springmvc rule: required should be false because defaultValue have value
  @GetMapping(path = "intHeaderRequire")
  public int intHeaderRequire(@RequestHeader(name = "input", required = true, defaultValue = "13") int input) {
    return input;
  }

  @ApiImplicitParams({
      @ApiImplicitParam(name = "input", dataType = "integer", format = "int32", paramType = "form", value = "a required form param", required = true, defaultValue = "13")})
  @PostMapping(path = "intFormRequire")
  public int intFormRequire(int input) {
    return input;
  }
}

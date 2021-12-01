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
package org.apache.metron.dataloads.extractor.stix.types;

import org.mitre.cybox.common_2.ObjectPropertiesType;

public enum ObjectTypeHandlers {
      ADDRESS(new AddressHandler())
    ,HOSTNAME(new HostnameHandler())
    ,DOMAINNAME(new DomainHandler())
    ,;
   ObjectTypeHandler _handler;
   ObjectTypeHandlers(ObjectTypeHandler handler) {
      _handler = handler;
   }
   ObjectTypeHandler getHandler() {
      return _handler;
   }
   public static ObjectTypeHandler getHandlerByInstance(ObjectPropertiesType inst) {
      for(ObjectTypeHandlers h : values()) {
         if(inst.getClass().equals(h.getHandler().getTypeClass())) {
            return h.getHandler();
         }
      }
      return null;
   }
}

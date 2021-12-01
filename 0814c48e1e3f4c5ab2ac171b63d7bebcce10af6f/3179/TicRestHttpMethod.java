/*******************************************************************************
 * Copyright 2016 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
/*
 * @author mchyzer $Id: AsasRestHttpMethodva,v 1.5 2008-03-29 10:50:43 mchyzer Exp $
 */
package edu.internet2.middleware.tierInstrumentationCollector.rest;

import java.util.List;
import java.util.Map;

import edu.internet2.middleware.tierInstrumentationCollector.corebeans.TicResponseBeanBase;
import edu.internet2.middleware.tierInstrumentationCollector.exceptions.TicRestInvalidRequest;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * types of http methods accepted by grouper rest
 */
public enum TicRestHttpMethod {

  /** POST */
  POST {

    /**
     * @see TicRestHttpMethod#service(List, Map)
     */
    @Override
    public TicResponseBeanBase service(List<String> urlStrings,
        Map<String, String> params, String body) {
      
      if (urlStrings.size() > 0) {
        
        String firstResource = GrouperClientUtils.popUrlString(urlStrings);
        
        //validate and get the first resource
        TierInstrumentationCollectorRestPut asasRestPut = TierInstrumentationCollectorRestPut.valueOfIgnoreCase(
            firstResource, true);
    
        return asasRestPut.service(urlStrings, params, body);

      }
      
      throw new TicRestInvalidRequest("Not expecting this request", "404", "ERROR_INVALID_PATH");
    }

  };

  /**
   * handle the incoming request based on HTTP method
   * @param clientVersion version of client, e.g. v1_3_000
   * @param urlStrings not including the app name or servlet.  for http://localhost/grouper-ws/servicesRest/groups/a:b
   * the urlStrings would be size two: {"group", "a:b"}
   * @param requestObject is the request body converted to object
   * @return the resultObject
   */
  public abstract TicResponseBeanBase service(
      List<String> urlStrings, Map<String, String> params, String body);

  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNotFound true to throw exception if method not found
   * @return the enum or null or exception if not found
   * @throws GrouperRestInvalidRequest if there is a problem
   */
  public static TicRestHttpMethod valueOfIgnoreCase(String string,
      boolean exceptionOnNotFound) throws TicRestInvalidRequest {
    return GrouperClientUtils.enumValueOfIgnoreCase(TicRestHttpMethod.class, string, exceptionOnNotFound);
  }
}

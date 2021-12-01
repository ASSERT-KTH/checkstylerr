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

package org.slc.sli.api.security.pdp;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for common mutation operations
 */
public class MutatorUtil {
    public static Map<String, String> getParameterMap(String queryParameters) {
        Map<String, String> parameters = new HashMap<String, String>();

        for (String query : queryParameters.split("&")) {
            String[] keyAndValue = query.split("=", 2);
            if (keyAndValue.length == 2) {
                parameters.put(keyAndValue[0], keyAndValue[1]);
            }
        }
        return parameters;
    }
}

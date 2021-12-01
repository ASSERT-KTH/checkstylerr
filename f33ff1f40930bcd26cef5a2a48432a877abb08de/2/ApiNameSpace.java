/*
* Copyright 2012 Shared Learning Collaborative, LLC
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
package org.slc.sli.bulk.extract.metadata;

import java.util.List;

/**
 * @author tke
 *
 */
public class ApiNameSpace {

    private String[] nameSpace;
    private List<ResourceEndPointTemplate> resources;

    public String[] getNameSpace() {
        String[] copy = new String[nameSpace.length];
        System.arraycopy(nameSpace, 0, copy, 0, nameSpace.length);

        return copy;
    }

    public void setNameSpace(String[] nameSpace) {
        String[] copy = new String[nameSpace.length];
        System.arraycopy(nameSpace, 0, copy, 0, nameSpace.length);

        this.nameSpace = copy;
    }

    public List<ResourceEndPointTemplate> getResources() {
        return resources;
    }

    public void setResources(List<ResourceEndPointTemplate> resources) {
        this.resources = resources;
    }
}

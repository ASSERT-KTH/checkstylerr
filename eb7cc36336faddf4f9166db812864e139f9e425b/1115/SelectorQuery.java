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

package org.slc.sli.api.selectors.doc;

import org.slc.sli.modeling.uml.Type;

import java.util.HashMap;

/**
 * Container class to hold information about a Selector query
 *
 * @author srupasinghe
 *
 */
public class SelectorQuery extends HashMap<Type, SelectorQueryPlan> {

    private static final long serialVersionUID = -1000986083487888863L;
}

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
package org.slc.sli.api.security.context.validator;

import java.util.Set;

import org.slc.sli.api.util.SecurityUtil;

/**
 * Abstract class to do common functionality
 *
 * @author nbrown
 *
 */
public abstract class BasicValidator extends AbstractContextValidator {
    private final boolean transitiveValidator;
    private final String type;
    private final String userType;

    public BasicValidator(boolean transitiveValidator, String userType, String type) {
        this.transitiveValidator = transitiveValidator;
        this.type = type;
        this.userType = userType;
    }

    @Override
    public boolean canValidate(String entityType, boolean isTransitive) {
        return userType.equals(SecurityUtil.getSLIPrincipal().getEntity().getType()) && type.equals(entityType)
                && isTransitive == transitiveValidator;
    }

    @Override
    public boolean validate(String entityType, Set<String> ids) throws IllegalStateException {
        if (!areParametersValid(type, entityType, ids)) {
            return false;
        }

        return doValidate(ids);
    }

    protected abstract boolean doValidate(Set<String> ids);

}
/*
 * Copyright (c) 2018 Ahome' Innovation Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ait.lienzo.client.core.shape.json.validators;

import com.google.gwt.json.client.JSONValue;

public class BooleanValidator extends AbstractAttributeTypeValidator
{
    public static final BooleanValidator INSTANCE = new BooleanValidator();

    public BooleanValidator()
    {
        super("Boolean");
    }
    @Override
    public void validate(final JSONValue jval, final ValidationContext ctx) throws ValidationException
    {
        if (null == jval)
        {
            ctx.addBadTypeError("Boolean");

            return;
        }
        if (null == jval.isBoolean())
        {
            ctx.addBadTypeError("Boolean");
        }
    }
}

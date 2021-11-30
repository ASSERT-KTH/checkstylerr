package com.piksel.sequoia.clientsdk.resource.json;

/*-
 * #%L
 * Sequoia Java Client SDK
 * %%
 * Copyright (C) 2018 Piksel
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import static java.security.AccessController.doPrivileged;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.security.PrivilegedAction;

import com.google.api.client.util.Key;
import com.google.gson.JsonParseException;
import com.piksel.sequoia.annotations.Internal;

@Internal
public abstract class JsonSerializationHelper {

    public static Class<?> getTypeAsClass(Type type) {
        if (!(type instanceof Class)) {
            throw new JsonParseException(
                    "Type <" + type + "> should be a class");
        }

        return (Class<?>) type;
    }

    public static void enableFieldAccess(Field field) {
        doPrivileged(new PrivilegedAction<Object>() {
            @Override
            public Object run() {
                field.setAccessible(true);
                return null;
            }
        });
    }

    public static String getName(Field field) {
        Key annotation = field.getAnnotation(Key.class);
        return annotation.value().equals("##default") ? field.getName()
                : annotation.value();
    }

    public static Object getValue(Field field, Object reference) {
        try {
            return field.get(reference);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new JsonParseException(e.getMessage(), e);
        }
    }

    public static void setValue(Field field, Object instance,
            Object fieldValue) {
        try {
            field.set(instance, fieldValue);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new JsonParseException(e.getMessage(), e);
        }
    }

}

/**
 *  Copyright 2011 Alexandru Craciun, Eyal Kaspi
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.stjs.testing.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Deprecated
/**
 * this annotation describes the scripts to be added to the page during the unit test before the script from the inclusion of any script generated from Java dependencies.
 * The scripts can be a file relative to the projects home, searched in the classpath (classpath:/script.js) or as an url.
 * This annotation is kept for backward compatibility. Please note that if you use this annotation, the scripts from bridges ARE NOT automatically added, because they may clash with scripts you may have added manually!
 *
 * @author acraciun
 * @see ScriptsBefore
 * @see ScriptsAfter
 *
 */
public @interface Scripts {
	public String[] value();
}

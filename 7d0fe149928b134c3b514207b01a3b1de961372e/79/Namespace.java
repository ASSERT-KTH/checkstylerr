/**
 * Copyright 2011 The Open Source Research Group,
 *                University of Erlangen-Nürnberg
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

package org.sweble.wikitext.engine.config;

import java.util.TreeSet;

public interface Namespace
{

	public abstract int getId();

	public abstract String getName();

	public abstract boolean isCanHaveSubpages();

	public abstract String getCanonical();

	public abstract TreeSet<String> getAliases();

	public abstract boolean isFileNs();

	public abstract boolean isMediaNs();

	public abstract boolean isTalkNamespace();

	public abstract boolean isSubjectNamespace();

	public abstract int getTalkspaceId();

	public abstract int getSubjectspaceId();

}

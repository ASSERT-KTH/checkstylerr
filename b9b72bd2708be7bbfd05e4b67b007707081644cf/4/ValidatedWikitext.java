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

package org.sweble.wikitext.parser.encval;

import org.sweble.wikitext.parser.WtEntityMap;

public class ValidatedWikitext
{
	private String wikitext;

	private WtEntityMap entityMap;

	private boolean containsIllegalCodePoints;

	public ValidatedWikitext(String wikitext, WtEntityMap entityMap, boolean containsIllegalCodePoints)
	{
		this.wikitext = wikitext;
		this.entityMap = entityMap;
		this.containsIllegalCodePoints = containsIllegalCodePoints;
	}

	public String getWikitext()
	{
		return wikitext;
	}

	public WtEntityMap getEntityMap()
	{
		return entityMap;
	}

	public boolean containsIllegalCodePoints()
	{
		return containsIllegalCodePoints;
	}
}

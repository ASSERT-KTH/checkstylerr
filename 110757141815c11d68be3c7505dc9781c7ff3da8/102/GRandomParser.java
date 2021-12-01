/*******************************************************************************
 * Copyright (c) 2018 Kiel University and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.elk.core.debug.grandom.parser.antlr;

import com.google.inject.Inject;
import org.eclipse.elk.core.debug.grandom.parser.antlr.internal.InternalGRandomParser;
import org.eclipse.elk.core.debug.grandom.services.GRandomGrammarAccess;
import org.eclipse.xtext.parser.antlr.AbstractAntlrParser;
import org.eclipse.xtext.parser.antlr.XtextTokenStream;

public class GRandomParser extends AbstractAntlrParser {

	@Inject
	private GRandomGrammarAccess grammarAccess;

	@Override
	protected void setInitialHiddenTokens(XtextTokenStream tokenStream) {
		tokenStream.setInitialHiddenTokens("RULE_WS", "RULE_ML_COMMENT", "RULE_SL_COMMENT");
	}
	

	@Override
	protected InternalGRandomParser createParser(XtextTokenStream stream) {
		return new InternalGRandomParser(stream, getGrammarAccess());
	}

	@Override 
	protected String getDefaultRuleName() {
		return "RandGraph";
	}

	public GRandomGrammarAccess getGrammarAccess() {
		return this.grammarAccess;
	}

	public void setGrammarAccess(GRandomGrammarAccess grammarAccess) {
		this.grammarAccess = grammarAccess;
	}
}

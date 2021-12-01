/*******************************************************************************
 * Copyright (c) 2020 Kiel University and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.elk.graph.json.text.parser.antlr;

import com.google.inject.Inject;
import org.eclipse.elk.graph.json.text.parser.antlr.internal.InternalElkGraphJsonParser;
import org.eclipse.elk.graph.json.text.services.ElkGraphJsonGrammarAccess;
import org.eclipse.xtext.parser.antlr.AbstractAntlrParser;
import org.eclipse.xtext.parser.antlr.XtextTokenStream;

public class ElkGraphJsonParser extends AbstractAntlrParser {

	@Inject
	private ElkGraphJsonGrammarAccess grammarAccess;

	@Override
	protected void setInitialHiddenTokens(XtextTokenStream tokenStream) {
		tokenStream.setInitialHiddenTokens("RULE_WS", "RULE_ML_COMMENT", "RULE_SL_COMMENT");
	}
	

	@Override
	protected InternalElkGraphJsonParser createParser(XtextTokenStream stream) {
		return new InternalElkGraphJsonParser(stream, getGrammarAccess());
	}

	@Override 
	protected String getDefaultRuleName() {
		return "ElkNode";
	}

	public ElkGraphJsonGrammarAccess getGrammarAccess() {
		return this.grammarAccess;
	}

	public void setGrammarAccess(ElkGraphJsonGrammarAccess grammarAccess) {
		this.grammarAccess = grammarAccess;
	}
}

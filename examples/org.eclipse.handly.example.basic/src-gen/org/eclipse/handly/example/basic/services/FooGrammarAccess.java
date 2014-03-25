/*
* generated by Xtext
*/
package org.eclipse.handly.example.basic.services;

import com.google.inject.Singleton;
import com.google.inject.Inject;

import java.util.List;

import org.eclipse.xtext.*;
import org.eclipse.xtext.service.GrammarProvider;
import org.eclipse.xtext.service.AbstractElementFinder.*;

import org.eclipse.xtext.common.services.TerminalsGrammarAccess;

@Singleton
public class FooGrammarAccess extends AbstractGrammarElementFinder {
	
	
	public class ModuleElements extends AbstractParserRuleElementFinder {
		private final ParserRule rule = (ParserRule) GrammarUtil.findRuleForName(getGrammar(), "Module");
		private final Group cGroup = (Group)rule.eContents().get(1);
		private final Assignment cVarsAssignment_0 = (Assignment)cGroup.eContents().get(0);
		private final RuleCall cVarsVarParserRuleCall_0_0 = (RuleCall)cVarsAssignment_0.eContents().get(0);
		private final Assignment cDefsAssignment_1 = (Assignment)cGroup.eContents().get(1);
		private final RuleCall cDefsDefParserRuleCall_1_0 = (RuleCall)cDefsAssignment_1.eContents().get(0);
		
		//Module:
		//	vars+=Var* defs+=Def*;
		public ParserRule getRule() { return rule; }

		//vars+=Var* defs+=Def*
		public Group getGroup() { return cGroup; }

		//vars+=Var*
		public Assignment getVarsAssignment_0() { return cVarsAssignment_0; }

		//Var
		public RuleCall getVarsVarParserRuleCall_0_0() { return cVarsVarParserRuleCall_0_0; }

		//defs+=Def*
		public Assignment getDefsAssignment_1() { return cDefsAssignment_1; }

		//Def
		public RuleCall getDefsDefParserRuleCall_1_0() { return cDefsDefParserRuleCall_1_0; }
	}

	public class VarElements extends AbstractParserRuleElementFinder {
		private final ParserRule rule = (ParserRule) GrammarUtil.findRuleForName(getGrammar(), "Var");
		private final Group cGroup = (Group)rule.eContents().get(1);
		private final Keyword cVarKeyword_0 = (Keyword)cGroup.eContents().get(0);
		private final Assignment cNameAssignment_1 = (Assignment)cGroup.eContents().get(1);
		private final RuleCall cNameIDTerminalRuleCall_1_0 = (RuleCall)cNameAssignment_1.eContents().get(0);
		private final Keyword cSemicolonKeyword_2 = (Keyword)cGroup.eContents().get(2);
		
		//Var:
		//	"var" name=ID ";";
		public ParserRule getRule() { return rule; }

		//"var" name=ID ";"
		public Group getGroup() { return cGroup; }

		//"var"
		public Keyword getVarKeyword_0() { return cVarKeyword_0; }

		//name=ID
		public Assignment getNameAssignment_1() { return cNameAssignment_1; }

		//ID
		public RuleCall getNameIDTerminalRuleCall_1_0() { return cNameIDTerminalRuleCall_1_0; }

		//";"
		public Keyword getSemicolonKeyword_2() { return cSemicolonKeyword_2; }
	}

	public class DefElements extends AbstractParserRuleElementFinder {
		private final ParserRule rule = (ParserRule) GrammarUtil.findRuleForName(getGrammar(), "Def");
		private final Group cGroup = (Group)rule.eContents().get(1);
		private final Keyword cDefKeyword_0 = (Keyword)cGroup.eContents().get(0);
		private final Assignment cNameAssignment_1 = (Assignment)cGroup.eContents().get(1);
		private final RuleCall cNameIDTerminalRuleCall_1_0 = (RuleCall)cNameAssignment_1.eContents().get(0);
		private final Keyword cLeftParenthesisKeyword_2 = (Keyword)cGroup.eContents().get(2);
		private final Assignment cParamsAssignment_3 = (Assignment)cGroup.eContents().get(3);
		private final RuleCall cParamsIDTerminalRuleCall_3_0 = (RuleCall)cParamsAssignment_3.eContents().get(0);
		private final Group cGroup_4 = (Group)cGroup.eContents().get(4);
		private final Keyword cCommaKeyword_4_0 = (Keyword)cGroup_4.eContents().get(0);
		private final Assignment cParamsAssignment_4_1 = (Assignment)cGroup_4.eContents().get(1);
		private final RuleCall cParamsIDTerminalRuleCall_4_1_0 = (RuleCall)cParamsAssignment_4_1.eContents().get(0);
		private final Keyword cRightParenthesisKeyword_5 = (Keyword)cGroup.eContents().get(5);
		private final Keyword cLeftCurlyBracketKeyword_6 = (Keyword)cGroup.eContents().get(6);
		private final Keyword cRightCurlyBracketKeyword_7 = (Keyword)cGroup.eContents().get(7);
		
		//Def:
		//	"def" name=ID "(" params+=ID? ("," params+=ID)* ")" "{" "}";
		public ParserRule getRule() { return rule; }

		//"def" name=ID "(" params+=ID? ("," params+=ID)* ")" "{" "}"
		public Group getGroup() { return cGroup; }

		//"def"
		public Keyword getDefKeyword_0() { return cDefKeyword_0; }

		//name=ID
		public Assignment getNameAssignment_1() { return cNameAssignment_1; }

		//ID
		public RuleCall getNameIDTerminalRuleCall_1_0() { return cNameIDTerminalRuleCall_1_0; }

		//"("
		public Keyword getLeftParenthesisKeyword_2() { return cLeftParenthesisKeyword_2; }

		//params+=ID?
		public Assignment getParamsAssignment_3() { return cParamsAssignment_3; }

		//ID
		public RuleCall getParamsIDTerminalRuleCall_3_0() { return cParamsIDTerminalRuleCall_3_0; }

		//("," params+=ID)*
		public Group getGroup_4() { return cGroup_4; }

		//","
		public Keyword getCommaKeyword_4_0() { return cCommaKeyword_4_0; }

		//params+=ID
		public Assignment getParamsAssignment_4_1() { return cParamsAssignment_4_1; }

		//ID
		public RuleCall getParamsIDTerminalRuleCall_4_1_0() { return cParamsIDTerminalRuleCall_4_1_0; }

		//")"
		public Keyword getRightParenthesisKeyword_5() { return cRightParenthesisKeyword_5; }

		//"{"
		public Keyword getLeftCurlyBracketKeyword_6() { return cLeftCurlyBracketKeyword_6; }

		//"}"
		public Keyword getRightCurlyBracketKeyword_7() { return cRightCurlyBracketKeyword_7; }
	}
	
	
	private ModuleElements pModule;
	private VarElements pVar;
	private DefElements pDef;
	
	private final Grammar grammar;

	private TerminalsGrammarAccess gaTerminals;

	@Inject
	public FooGrammarAccess(GrammarProvider grammarProvider,
		TerminalsGrammarAccess gaTerminals) {
		this.grammar = internalFindGrammar(grammarProvider);
		this.gaTerminals = gaTerminals;
	}
	
	protected Grammar internalFindGrammar(GrammarProvider grammarProvider) {
		Grammar grammar = grammarProvider.getGrammar(this);
		while (grammar != null) {
			if ("org.eclipse.handly.example.basic.Foo".equals(grammar.getName())) {
				return grammar;
			}
			List<Grammar> grammars = grammar.getUsedGrammars();
			if (!grammars.isEmpty()) {
				grammar = grammars.iterator().next();
			} else {
				return null;
			}
		}
		return grammar;
	}
	
	
	public Grammar getGrammar() {
		return grammar;
	}
	

	public TerminalsGrammarAccess getTerminalsGrammarAccess() {
		return gaTerminals;
	}

	
	//Module:
	//	vars+=Var* defs+=Def*;
	public ModuleElements getModuleAccess() {
		return (pModule != null) ? pModule : (pModule = new ModuleElements());
	}
	
	public ParserRule getModuleRule() {
		return getModuleAccess().getRule();
	}

	//Var:
	//	"var" name=ID ";";
	public VarElements getVarAccess() {
		return (pVar != null) ? pVar : (pVar = new VarElements());
	}
	
	public ParserRule getVarRule() {
		return getVarAccess().getRule();
	}

	//Def:
	//	"def" name=ID "(" params+=ID? ("," params+=ID)* ")" "{" "}";
	public DefElements getDefAccess() {
		return (pDef != null) ? pDef : (pDef = new DefElements());
	}
	
	public ParserRule getDefRule() {
		return getDefAccess().getRule();
	}

	//terminal ID:
	//	"^"? ("a".."z" | "A".."Z" | "_") ("a".."z" | "A".."Z" | "_" | "0".."9")*;
	public TerminalRule getIDRule() {
		return gaTerminals.getIDRule();
	} 

	//terminal INT returns ecore::EInt:
	//	"0".."9"+;
	public TerminalRule getINTRule() {
		return gaTerminals.getINTRule();
	} 

	//terminal STRING:
	//	"\"" ("\\" ("b" | "t" | "n" | "f" | "r" | "u" | "\"" | "\'" | "\\") | !("\\" | "\""))* "\"" | "\'" ("\\" ("b" | "t" |
	//	"n" | "f" | "r" | "u" | "\"" | "\'" | "\\") | !("\\" | "\'"))* "\'";
	public TerminalRule getSTRINGRule() {
		return gaTerminals.getSTRINGRule();
	} 

	//terminal ML_COMMENT:
	//	"/ *"->"* /";
	public TerminalRule getML_COMMENTRule() {
		return gaTerminals.getML_COMMENTRule();
	} 

	//terminal SL_COMMENT:
	//	"//" !("\n" | "\r")* ("\r"? "\n")?;
	public TerminalRule getSL_COMMENTRule() {
		return gaTerminals.getSL_COMMENTRule();
	} 

	//terminal WS:
	//	(" " | "\t" | "\r" | "\n")+;
	public TerminalRule getWSRule() {
		return gaTerminals.getWSRule();
	} 

	//terminal ANY_OTHER:
	//	.;
	public TerminalRule getANY_OTHERRule() {
		return gaTerminals.getANY_OTHERRule();
	} 
}

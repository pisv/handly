/*
* generated by Xtext
*/
grammar InternalFoo;

options {
	superClass=AbstractInternalAntlrParser;
	
}

@lexer::header {
package org.eclipse.handly.example.basic.parser.antlr.internal;

// Hack: Use our own Lexer superclass by means of import. 
// Currently there is no other way to specify the superclass for the lexer.
import org.eclipse.xtext.parser.antlr.Lexer;
}

@parser::header {
package org.eclipse.handly.example.basic.parser.antlr.internal; 

import org.eclipse.xtext.*;
import org.eclipse.xtext.parser.*;
import org.eclipse.xtext.parser.impl.*;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.parser.antlr.AbstractInternalAntlrParser;
import org.eclipse.xtext.parser.antlr.XtextTokenStream;
import org.eclipse.xtext.parser.antlr.XtextTokenStream.HiddenTokens;
import org.eclipse.xtext.parser.antlr.AntlrDatatypeRuleToken;
import org.eclipse.handly.example.basic.services.FooGrammarAccess;

}

@parser::members {

 	private FooGrammarAccess grammarAccess;
 	
    public InternalFooParser(TokenStream input, FooGrammarAccess grammarAccess) {
        this(input);
        this.grammarAccess = grammarAccess;
        registerRules(grammarAccess.getGrammar());
    }
    
    @Override
    protected String getFirstRuleName() {
    	return "Module";	
   	}
   	
   	@Override
   	protected FooGrammarAccess getGrammarAccess() {
   		return grammarAccess;
   	}
}

@rulecatch { 
    catch (RecognitionException re) { 
        recover(input,re); 
        appendSkippedTokens();
    } 
}




// Entry rule entryRuleModule
entryRuleModule returns [EObject current=null] 
	:
	{ newCompositeNode(grammarAccess.getModuleRule()); }
	 iv_ruleModule=ruleModule 
	 { $current=$iv_ruleModule.current; } 
	 EOF 
;

// Rule Module
ruleModule returns [EObject current=null] 
    @init { enterRule(); 
    }
    @after { leaveRule(); }:
((
(
		{ 
	        newCompositeNode(grammarAccess.getModuleAccess().getVarsVarParserRuleCall_0_0()); 
	    }
		lv_vars_0_0=ruleVar		{
	        if ($current==null) {
	            $current = createModelElementForParent(grammarAccess.getModuleRule());
	        }
       		add(
       			$current, 
       			"vars",
        		lv_vars_0_0, 
        		"Var");
	        afterParserOrEnumRuleCall();
	    }

)
)*(
(
		{ 
	        newCompositeNode(grammarAccess.getModuleAccess().getDefsDefParserRuleCall_1_0()); 
	    }
		lv_defs_1_0=ruleDef		{
	        if ($current==null) {
	            $current = createModelElementForParent(grammarAccess.getModuleRule());
	        }
       		add(
       			$current, 
       			"defs",
        		lv_defs_1_0, 
        		"Def");
	        afterParserOrEnumRuleCall();
	    }

)
)*)
;





// Entry rule entryRuleVar
entryRuleVar returns [EObject current=null] 
	:
	{ newCompositeNode(grammarAccess.getVarRule()); }
	 iv_ruleVar=ruleVar 
	 { $current=$iv_ruleVar.current; } 
	 EOF 
;

// Rule Var
ruleVar returns [EObject current=null] 
    @init { enterRule(); 
    }
    @after { leaveRule(); }:
(	otherlv_0='var' 
    {
    	newLeafNode(otherlv_0, grammarAccess.getVarAccess().getVarKeyword_0());
    }
(
(
		lv_name_1_0=RULE_ID
		{
			newLeafNode(lv_name_1_0, grammarAccess.getVarAccess().getNameIDTerminalRuleCall_1_0()); 
		}
		{
	        if ($current==null) {
	            $current = createModelElement(grammarAccess.getVarRule());
	        }
       		setWithLastConsumed(
       			$current, 
       			"name",
        		lv_name_1_0, 
        		"ID");
	    }

)
)	otherlv_2=';' 
    {
    	newLeafNode(otherlv_2, grammarAccess.getVarAccess().getSemicolonKeyword_2());
    }
)
;





// Entry rule entryRuleDef
entryRuleDef returns [EObject current=null] 
	:
	{ newCompositeNode(grammarAccess.getDefRule()); }
	 iv_ruleDef=ruleDef 
	 { $current=$iv_ruleDef.current; } 
	 EOF 
;

// Rule Def
ruleDef returns [EObject current=null] 
    @init { enterRule(); 
    }
    @after { leaveRule(); }:
(	otherlv_0='def' 
    {
    	newLeafNode(otherlv_0, grammarAccess.getDefAccess().getDefKeyword_0());
    }
(
(
		lv_name_1_0=RULE_ID
		{
			newLeafNode(lv_name_1_0, grammarAccess.getDefAccess().getNameIDTerminalRuleCall_1_0()); 
		}
		{
	        if ($current==null) {
	            $current = createModelElement(grammarAccess.getDefRule());
	        }
       		setWithLastConsumed(
       			$current, 
       			"name",
        		lv_name_1_0, 
        		"ID");
	    }

)
)	otherlv_2='(' 
    {
    	newLeafNode(otherlv_2, grammarAccess.getDefAccess().getLeftParenthesisKeyword_2());
    }
(
(
		lv_params_3_0=RULE_ID
		{
			newLeafNode(lv_params_3_0, grammarAccess.getDefAccess().getParamsIDTerminalRuleCall_3_0()); 
		}
		{
	        if ($current==null) {
	            $current = createModelElement(grammarAccess.getDefRule());
	        }
       		addWithLastConsumed(
       			$current, 
       			"params",
        		lv_params_3_0, 
        		"ID");
	    }

)
)?(	otherlv_4=',' 
    {
    	newLeafNode(otherlv_4, grammarAccess.getDefAccess().getCommaKeyword_4_0());
    }
(
(
		lv_params_5_0=RULE_ID
		{
			newLeafNode(lv_params_5_0, grammarAccess.getDefAccess().getParamsIDTerminalRuleCall_4_1_0()); 
		}
		{
	        if ($current==null) {
	            $current = createModelElement(grammarAccess.getDefRule());
	        }
       		addWithLastConsumed(
       			$current, 
       			"params",
        		lv_params_5_0, 
        		"ID");
	    }

)
))*	otherlv_6=')' 
    {
    	newLeafNode(otherlv_6, grammarAccess.getDefAccess().getRightParenthesisKeyword_5());
    }
	otherlv_7='{' 
    {
    	newLeafNode(otherlv_7, grammarAccess.getDefAccess().getLeftCurlyBracketKeyword_6());
    }
	otherlv_8='}' 
    {
    	newLeafNode(otherlv_8, grammarAccess.getDefAccess().getRightCurlyBracketKeyword_7());
    }
)
;





RULE_ID : '^'? ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'_'|'0'..'9')*;

RULE_INT : ('0'..'9')+;

RULE_STRING : ('"' ('\\' ('b'|'t'|'n'|'f'|'r'|'u'|'"'|'\''|'\\')|~(('\\'|'"')))* '"'|'\'' ('\\' ('b'|'t'|'n'|'f'|'r'|'u'|'"'|'\''|'\\')|~(('\\'|'\'')))* '\'');

RULE_ML_COMMENT : '/*' ( options {greedy=false;} : . )*'*/';

RULE_SL_COMMENT : '//' ~(('\n'|'\r'))* ('\r'? '\n')?;

RULE_WS : (' '|'\t'|'\r'|'\n')+;

RULE_ANY_OTHER : .;



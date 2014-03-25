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



import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("all")
public class InternalFooParser extends AbstractInternalAntlrParser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "RULE_ID", "RULE_INT", "RULE_STRING", "RULE_ML_COMMENT", "RULE_SL_COMMENT", "RULE_WS", "RULE_ANY_OTHER", "'var'", "';'", "'def'", "'('", "','", "')'", "'{'", "'}'"
    };
    public static final int RULE_ID=4;
    public static final int RULE_STRING=6;
    public static final int T__16=16;
    public static final int T__15=15;
    public static final int T__18=18;
    public static final int T__17=17;
    public static final int T__12=12;
    public static final int T__11=11;
    public static final int T__14=14;
    public static final int T__13=13;
    public static final int RULE_ANY_OTHER=10;
    public static final int RULE_INT=5;
    public static final int RULE_WS=9;
    public static final int RULE_SL_COMMENT=8;
    public static final int EOF=-1;
    public static final int RULE_ML_COMMENT=7;

    // delegates
    // delegators


        public InternalFooParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public InternalFooParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        

    public String[] getTokenNames() { return InternalFooParser.tokenNames; }
    public String getGrammarFileName() { return "../org.eclipse.handly.example.basic/src-gen/org/eclipse/handly/example/basic/parser/antlr/internal/InternalFoo.g"; }



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



    // $ANTLR start "entryRuleModule"
    // ../org.eclipse.handly.example.basic/src-gen/org/eclipse/handly/example/basic/parser/antlr/internal/InternalFoo.g:67:1: entryRuleModule returns [EObject current=null] : iv_ruleModule= ruleModule EOF ;
    public final EObject entryRuleModule() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleModule = null;


        try {
            // ../org.eclipse.handly.example.basic/src-gen/org/eclipse/handly/example/basic/parser/antlr/internal/InternalFoo.g:68:2: (iv_ruleModule= ruleModule EOF )
            // ../org.eclipse.handly.example.basic/src-gen/org/eclipse/handly/example/basic/parser/antlr/internal/InternalFoo.g:69:2: iv_ruleModule= ruleModule EOF
            {
             newCompositeNode(grammarAccess.getModuleRule()); 
            pushFollow(FOLLOW_ruleModule_in_entryRuleModule75);
            iv_ruleModule=ruleModule();

            state._fsp--;

             current =iv_ruleModule; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleModule85); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleModule"


    // $ANTLR start "ruleModule"
    // ../org.eclipse.handly.example.basic/src-gen/org/eclipse/handly/example/basic/parser/antlr/internal/InternalFoo.g:76:1: ruleModule returns [EObject current=null] : ( ( (lv_vars_0_0= ruleVar ) )* ( (lv_defs_1_0= ruleDef ) )* ) ;
    public final EObject ruleModule() throws RecognitionException {
        EObject current = null;

        EObject lv_vars_0_0 = null;

        EObject lv_defs_1_0 = null;


         enterRule(); 
            
        try {
            // ../org.eclipse.handly.example.basic/src-gen/org/eclipse/handly/example/basic/parser/antlr/internal/InternalFoo.g:79:28: ( ( ( (lv_vars_0_0= ruleVar ) )* ( (lv_defs_1_0= ruleDef ) )* ) )
            // ../org.eclipse.handly.example.basic/src-gen/org/eclipse/handly/example/basic/parser/antlr/internal/InternalFoo.g:80:1: ( ( (lv_vars_0_0= ruleVar ) )* ( (lv_defs_1_0= ruleDef ) )* )
            {
            // ../org.eclipse.handly.example.basic/src-gen/org/eclipse/handly/example/basic/parser/antlr/internal/InternalFoo.g:80:1: ( ( (lv_vars_0_0= ruleVar ) )* ( (lv_defs_1_0= ruleDef ) )* )
            // ../org.eclipse.handly.example.basic/src-gen/org/eclipse/handly/example/basic/parser/antlr/internal/InternalFoo.g:80:2: ( (lv_vars_0_0= ruleVar ) )* ( (lv_defs_1_0= ruleDef ) )*
            {
            // ../org.eclipse.handly.example.basic/src-gen/org/eclipse/handly/example/basic/parser/antlr/internal/InternalFoo.g:80:2: ( (lv_vars_0_0= ruleVar ) )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==11) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // ../org.eclipse.handly.example.basic/src-gen/org/eclipse/handly/example/basic/parser/antlr/internal/InternalFoo.g:81:1: (lv_vars_0_0= ruleVar )
            	    {
            	    // ../org.eclipse.handly.example.basic/src-gen/org/eclipse/handly/example/basic/parser/antlr/internal/InternalFoo.g:81:1: (lv_vars_0_0= ruleVar )
            	    // ../org.eclipse.handly.example.basic/src-gen/org/eclipse/handly/example/basic/parser/antlr/internal/InternalFoo.g:82:3: lv_vars_0_0= ruleVar
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getModuleAccess().getVarsVarParserRuleCall_0_0()); 
            	    	    
            	    pushFollow(FOLLOW_ruleVar_in_ruleModule131);
            	    lv_vars_0_0=ruleVar();

            	    state._fsp--;


            	    	        if (current==null) {
            	    	            current = createModelElementForParent(grammarAccess.getModuleRule());
            	    	        }
            	           		add(
            	           			current, 
            	           			"vars",
            	            		lv_vars_0_0, 
            	            		"Var");
            	    	        afterParserOrEnumRuleCall();
            	    	    

            	    }


            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);

            // ../org.eclipse.handly.example.basic/src-gen/org/eclipse/handly/example/basic/parser/antlr/internal/InternalFoo.g:98:3: ( (lv_defs_1_0= ruleDef ) )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( (LA2_0==13) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // ../org.eclipse.handly.example.basic/src-gen/org/eclipse/handly/example/basic/parser/antlr/internal/InternalFoo.g:99:1: (lv_defs_1_0= ruleDef )
            	    {
            	    // ../org.eclipse.handly.example.basic/src-gen/org/eclipse/handly/example/basic/parser/antlr/internal/InternalFoo.g:99:1: (lv_defs_1_0= ruleDef )
            	    // ../org.eclipse.handly.example.basic/src-gen/org/eclipse/handly/example/basic/parser/antlr/internal/InternalFoo.g:100:3: lv_defs_1_0= ruleDef
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getModuleAccess().getDefsDefParserRuleCall_1_0()); 
            	    	    
            	    pushFollow(FOLLOW_ruleDef_in_ruleModule153);
            	    lv_defs_1_0=ruleDef();

            	    state._fsp--;


            	    	        if (current==null) {
            	    	            current = createModelElementForParent(grammarAccess.getModuleRule());
            	    	        }
            	           		add(
            	           			current, 
            	           			"defs",
            	            		lv_defs_1_0, 
            	            		"Def");
            	    	        afterParserOrEnumRuleCall();
            	    	    

            	    }


            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);


            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleModule"


    // $ANTLR start "entryRuleVar"
    // ../org.eclipse.handly.example.basic/src-gen/org/eclipse/handly/example/basic/parser/antlr/internal/InternalFoo.g:124:1: entryRuleVar returns [EObject current=null] : iv_ruleVar= ruleVar EOF ;
    public final EObject entryRuleVar() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleVar = null;


        try {
            // ../org.eclipse.handly.example.basic/src-gen/org/eclipse/handly/example/basic/parser/antlr/internal/InternalFoo.g:125:2: (iv_ruleVar= ruleVar EOF )
            // ../org.eclipse.handly.example.basic/src-gen/org/eclipse/handly/example/basic/parser/antlr/internal/InternalFoo.g:126:2: iv_ruleVar= ruleVar EOF
            {
             newCompositeNode(grammarAccess.getVarRule()); 
            pushFollow(FOLLOW_ruleVar_in_entryRuleVar190);
            iv_ruleVar=ruleVar();

            state._fsp--;

             current =iv_ruleVar; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleVar200); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleVar"


    // $ANTLR start "ruleVar"
    // ../org.eclipse.handly.example.basic/src-gen/org/eclipse/handly/example/basic/parser/antlr/internal/InternalFoo.g:133:1: ruleVar returns [EObject current=null] : (otherlv_0= 'var' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= ';' ) ;
    public final EObject ruleVar() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token lv_name_1_0=null;
        Token otherlv_2=null;

         enterRule(); 
            
        try {
            // ../org.eclipse.handly.example.basic/src-gen/org/eclipse/handly/example/basic/parser/antlr/internal/InternalFoo.g:136:28: ( (otherlv_0= 'var' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= ';' ) )
            // ../org.eclipse.handly.example.basic/src-gen/org/eclipse/handly/example/basic/parser/antlr/internal/InternalFoo.g:137:1: (otherlv_0= 'var' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= ';' )
            {
            // ../org.eclipse.handly.example.basic/src-gen/org/eclipse/handly/example/basic/parser/antlr/internal/InternalFoo.g:137:1: (otherlv_0= 'var' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= ';' )
            // ../org.eclipse.handly.example.basic/src-gen/org/eclipse/handly/example/basic/parser/antlr/internal/InternalFoo.g:137:3: otherlv_0= 'var' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= ';'
            {
            otherlv_0=(Token)match(input,11,FOLLOW_11_in_ruleVar237); 

                	newLeafNode(otherlv_0, grammarAccess.getVarAccess().getVarKeyword_0());
                
            // ../org.eclipse.handly.example.basic/src-gen/org/eclipse/handly/example/basic/parser/antlr/internal/InternalFoo.g:141:1: ( (lv_name_1_0= RULE_ID ) )
            // ../org.eclipse.handly.example.basic/src-gen/org/eclipse/handly/example/basic/parser/antlr/internal/InternalFoo.g:142:1: (lv_name_1_0= RULE_ID )
            {
            // ../org.eclipse.handly.example.basic/src-gen/org/eclipse/handly/example/basic/parser/antlr/internal/InternalFoo.g:142:1: (lv_name_1_0= RULE_ID )
            // ../org.eclipse.handly.example.basic/src-gen/org/eclipse/handly/example/basic/parser/antlr/internal/InternalFoo.g:143:3: lv_name_1_0= RULE_ID
            {
            lv_name_1_0=(Token)match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleVar254); 

            			newLeafNode(lv_name_1_0, grammarAccess.getVarAccess().getNameIDTerminalRuleCall_1_0()); 
            		

            	        if (current==null) {
            	            current = createModelElement(grammarAccess.getVarRule());
            	        }
                   		setWithLastConsumed(
                   			current, 
                   			"name",
                    		lv_name_1_0, 
                    		"ID");
            	    

            }


            }

            otherlv_2=(Token)match(input,12,FOLLOW_12_in_ruleVar271); 

                	newLeafNode(otherlv_2, grammarAccess.getVarAccess().getSemicolonKeyword_2());
                

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleVar"


    // $ANTLR start "entryRuleDef"
    // ../org.eclipse.handly.example.basic/src-gen/org/eclipse/handly/example/basic/parser/antlr/internal/InternalFoo.g:171:1: entryRuleDef returns [EObject current=null] : iv_ruleDef= ruleDef EOF ;
    public final EObject entryRuleDef() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleDef = null;


        try {
            // ../org.eclipse.handly.example.basic/src-gen/org/eclipse/handly/example/basic/parser/antlr/internal/InternalFoo.g:172:2: (iv_ruleDef= ruleDef EOF )
            // ../org.eclipse.handly.example.basic/src-gen/org/eclipse/handly/example/basic/parser/antlr/internal/InternalFoo.g:173:2: iv_ruleDef= ruleDef EOF
            {
             newCompositeNode(grammarAccess.getDefRule()); 
            pushFollow(FOLLOW_ruleDef_in_entryRuleDef307);
            iv_ruleDef=ruleDef();

            state._fsp--;

             current =iv_ruleDef; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleDef317); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleDef"


    // $ANTLR start "ruleDef"
    // ../org.eclipse.handly.example.basic/src-gen/org/eclipse/handly/example/basic/parser/antlr/internal/InternalFoo.g:180:1: ruleDef returns [EObject current=null] : (otherlv_0= 'def' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= '(' ( (lv_params_3_0= RULE_ID ) )? (otherlv_4= ',' ( (lv_params_5_0= RULE_ID ) ) )* otherlv_6= ')' otherlv_7= '{' otherlv_8= '}' ) ;
    public final EObject ruleDef() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token lv_name_1_0=null;
        Token otherlv_2=null;
        Token lv_params_3_0=null;
        Token otherlv_4=null;
        Token lv_params_5_0=null;
        Token otherlv_6=null;
        Token otherlv_7=null;
        Token otherlv_8=null;

         enterRule(); 
            
        try {
            // ../org.eclipse.handly.example.basic/src-gen/org/eclipse/handly/example/basic/parser/antlr/internal/InternalFoo.g:183:28: ( (otherlv_0= 'def' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= '(' ( (lv_params_3_0= RULE_ID ) )? (otherlv_4= ',' ( (lv_params_5_0= RULE_ID ) ) )* otherlv_6= ')' otherlv_7= '{' otherlv_8= '}' ) )
            // ../org.eclipse.handly.example.basic/src-gen/org/eclipse/handly/example/basic/parser/antlr/internal/InternalFoo.g:184:1: (otherlv_0= 'def' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= '(' ( (lv_params_3_0= RULE_ID ) )? (otherlv_4= ',' ( (lv_params_5_0= RULE_ID ) ) )* otherlv_6= ')' otherlv_7= '{' otherlv_8= '}' )
            {
            // ../org.eclipse.handly.example.basic/src-gen/org/eclipse/handly/example/basic/parser/antlr/internal/InternalFoo.g:184:1: (otherlv_0= 'def' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= '(' ( (lv_params_3_0= RULE_ID ) )? (otherlv_4= ',' ( (lv_params_5_0= RULE_ID ) ) )* otherlv_6= ')' otherlv_7= '{' otherlv_8= '}' )
            // ../org.eclipse.handly.example.basic/src-gen/org/eclipse/handly/example/basic/parser/antlr/internal/InternalFoo.g:184:3: otherlv_0= 'def' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= '(' ( (lv_params_3_0= RULE_ID ) )? (otherlv_4= ',' ( (lv_params_5_0= RULE_ID ) ) )* otherlv_6= ')' otherlv_7= '{' otherlv_8= '}'
            {
            otherlv_0=(Token)match(input,13,FOLLOW_13_in_ruleDef354); 

                	newLeafNode(otherlv_0, grammarAccess.getDefAccess().getDefKeyword_0());
                
            // ../org.eclipse.handly.example.basic/src-gen/org/eclipse/handly/example/basic/parser/antlr/internal/InternalFoo.g:188:1: ( (lv_name_1_0= RULE_ID ) )
            // ../org.eclipse.handly.example.basic/src-gen/org/eclipse/handly/example/basic/parser/antlr/internal/InternalFoo.g:189:1: (lv_name_1_0= RULE_ID )
            {
            // ../org.eclipse.handly.example.basic/src-gen/org/eclipse/handly/example/basic/parser/antlr/internal/InternalFoo.g:189:1: (lv_name_1_0= RULE_ID )
            // ../org.eclipse.handly.example.basic/src-gen/org/eclipse/handly/example/basic/parser/antlr/internal/InternalFoo.g:190:3: lv_name_1_0= RULE_ID
            {
            lv_name_1_0=(Token)match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleDef371); 

            			newLeafNode(lv_name_1_0, grammarAccess.getDefAccess().getNameIDTerminalRuleCall_1_0()); 
            		

            	        if (current==null) {
            	            current = createModelElement(grammarAccess.getDefRule());
            	        }
                   		setWithLastConsumed(
                   			current, 
                   			"name",
                    		lv_name_1_0, 
                    		"ID");
            	    

            }


            }

            otherlv_2=(Token)match(input,14,FOLLOW_14_in_ruleDef388); 

                	newLeafNode(otherlv_2, grammarAccess.getDefAccess().getLeftParenthesisKeyword_2());
                
            // ../org.eclipse.handly.example.basic/src-gen/org/eclipse/handly/example/basic/parser/antlr/internal/InternalFoo.g:210:1: ( (lv_params_3_0= RULE_ID ) )?
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==RULE_ID) ) {
                alt3=1;
            }
            switch (alt3) {
                case 1 :
                    // ../org.eclipse.handly.example.basic/src-gen/org/eclipse/handly/example/basic/parser/antlr/internal/InternalFoo.g:211:1: (lv_params_3_0= RULE_ID )
                    {
                    // ../org.eclipse.handly.example.basic/src-gen/org/eclipse/handly/example/basic/parser/antlr/internal/InternalFoo.g:211:1: (lv_params_3_0= RULE_ID )
                    // ../org.eclipse.handly.example.basic/src-gen/org/eclipse/handly/example/basic/parser/antlr/internal/InternalFoo.g:212:3: lv_params_3_0= RULE_ID
                    {
                    lv_params_3_0=(Token)match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleDef405); 

                    			newLeafNode(lv_params_3_0, grammarAccess.getDefAccess().getParamsIDTerminalRuleCall_3_0()); 
                    		

                    	        if (current==null) {
                    	            current = createModelElement(grammarAccess.getDefRule());
                    	        }
                           		addWithLastConsumed(
                           			current, 
                           			"params",
                            		lv_params_3_0, 
                            		"ID");
                    	    

                    }


                    }
                    break;

            }

            // ../org.eclipse.handly.example.basic/src-gen/org/eclipse/handly/example/basic/parser/antlr/internal/InternalFoo.g:228:3: (otherlv_4= ',' ( (lv_params_5_0= RULE_ID ) ) )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( (LA4_0==15) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // ../org.eclipse.handly.example.basic/src-gen/org/eclipse/handly/example/basic/parser/antlr/internal/InternalFoo.g:228:5: otherlv_4= ',' ( (lv_params_5_0= RULE_ID ) )
            	    {
            	    otherlv_4=(Token)match(input,15,FOLLOW_15_in_ruleDef424); 

            	        	newLeafNode(otherlv_4, grammarAccess.getDefAccess().getCommaKeyword_4_0());
            	        
            	    // ../org.eclipse.handly.example.basic/src-gen/org/eclipse/handly/example/basic/parser/antlr/internal/InternalFoo.g:232:1: ( (lv_params_5_0= RULE_ID ) )
            	    // ../org.eclipse.handly.example.basic/src-gen/org/eclipse/handly/example/basic/parser/antlr/internal/InternalFoo.g:233:1: (lv_params_5_0= RULE_ID )
            	    {
            	    // ../org.eclipse.handly.example.basic/src-gen/org/eclipse/handly/example/basic/parser/antlr/internal/InternalFoo.g:233:1: (lv_params_5_0= RULE_ID )
            	    // ../org.eclipse.handly.example.basic/src-gen/org/eclipse/handly/example/basic/parser/antlr/internal/InternalFoo.g:234:3: lv_params_5_0= RULE_ID
            	    {
            	    lv_params_5_0=(Token)match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleDef441); 

            	    			newLeafNode(lv_params_5_0, grammarAccess.getDefAccess().getParamsIDTerminalRuleCall_4_1_0()); 
            	    		

            	    	        if (current==null) {
            	    	            current = createModelElement(grammarAccess.getDefRule());
            	    	        }
            	           		addWithLastConsumed(
            	           			current, 
            	           			"params",
            	            		lv_params_5_0, 
            	            		"ID");
            	    	    

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop4;
                }
            } while (true);

            otherlv_6=(Token)match(input,16,FOLLOW_16_in_ruleDef460); 

                	newLeafNode(otherlv_6, grammarAccess.getDefAccess().getRightParenthesisKeyword_5());
                
            otherlv_7=(Token)match(input,17,FOLLOW_17_in_ruleDef472); 

                	newLeafNode(otherlv_7, grammarAccess.getDefAccess().getLeftCurlyBracketKeyword_6());
                
            otherlv_8=(Token)match(input,18,FOLLOW_18_in_ruleDef484); 

                	newLeafNode(otherlv_8, grammarAccess.getDefAccess().getRightCurlyBracketKeyword_7());
                

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleDef"

    // Delegated rules


 

    public static final BitSet FOLLOW_ruleModule_in_entryRuleModule75 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleModule85 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleVar_in_ruleModule131 = new BitSet(new long[]{0x0000000000002802L});
    public static final BitSet FOLLOW_ruleDef_in_ruleModule153 = new BitSet(new long[]{0x0000000000002002L});
    public static final BitSet FOLLOW_ruleVar_in_entryRuleVar190 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleVar200 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_11_in_ruleVar237 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleVar254 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_12_in_ruleVar271 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleDef_in_entryRuleDef307 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleDef317 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_13_in_ruleDef354 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleDef371 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_14_in_ruleDef388 = new BitSet(new long[]{0x0000000000018010L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleDef405 = new BitSet(new long[]{0x0000000000018000L});
    public static final BitSet FOLLOW_15_in_ruleDef424 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleDef441 = new BitSet(new long[]{0x0000000000018000L});
    public static final BitSet FOLLOW_16_in_ruleDef460 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_17_in_ruleDef472 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_18_in_ruleDef484 = new BitSet(new long[]{0x0000000000000002L});

}
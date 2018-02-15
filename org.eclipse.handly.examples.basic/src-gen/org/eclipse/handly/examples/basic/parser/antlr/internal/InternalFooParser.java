package org.eclipse.handly.examples.basic.parser.antlr.internal;

import org.eclipse.xtext.*;
import org.eclipse.xtext.parser.*;
import org.eclipse.xtext.parser.impl.*;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.parser.antlr.AbstractInternalAntlrParser;
import org.eclipse.xtext.parser.antlr.XtextTokenStream;
import org.eclipse.xtext.parser.antlr.XtextTokenStream.HiddenTokens;
import org.eclipse.xtext.parser.antlr.AntlrDatatypeRuleToken;
import org.eclipse.handly.examples.basic.services.FooGrammarAccess;



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
    public static final int RULE_WS=9;
    public static final int RULE_STRING=6;
    public static final int RULE_ANY_OTHER=10;
    public static final int RULE_SL_COMMENT=8;
    public static final int T__15=15;
    public static final int T__16=16;
    public static final int T__17=17;
    public static final int RULE_INT=5;
    public static final int T__18=18;
    public static final int T__11=11;
    public static final int RULE_ML_COMMENT=7;
    public static final int T__12=12;
    public static final int T__13=13;
    public static final int T__14=14;
    public static final int EOF=-1;

    // delegates
    // delegators


        public InternalFooParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public InternalFooParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        

    public String[] getTokenNames() { return InternalFooParser.tokenNames; }
    public String getGrammarFileName() { return "InternalFoo.g"; }



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
    // InternalFoo.g:64:1: entryRuleModule returns [EObject current=null] : iv_ruleModule= ruleModule EOF ;
    public final EObject entryRuleModule() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleModule = null;


        try {
            // InternalFoo.g:64:47: (iv_ruleModule= ruleModule EOF )
            // InternalFoo.g:65:2: iv_ruleModule= ruleModule EOF
            {
             newCompositeNode(grammarAccess.getModuleRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleModule=ruleModule();

            state._fsp--;

             current =iv_ruleModule; 
            match(input,EOF,FOLLOW_2); 

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
    // InternalFoo.g:71:1: ruleModule returns [EObject current=null] : ( ( (lv_vars_0_0= ruleVar ) )* ( (lv_defs_1_0= ruleDef ) )* ) ;
    public final EObject ruleModule() throws RecognitionException {
        EObject current = null;

        EObject lv_vars_0_0 = null;

        EObject lv_defs_1_0 = null;



        	enterRule();

        try {
            // InternalFoo.g:77:2: ( ( ( (lv_vars_0_0= ruleVar ) )* ( (lv_defs_1_0= ruleDef ) )* ) )
            // InternalFoo.g:78:2: ( ( (lv_vars_0_0= ruleVar ) )* ( (lv_defs_1_0= ruleDef ) )* )
            {
            // InternalFoo.g:78:2: ( ( (lv_vars_0_0= ruleVar ) )* ( (lv_defs_1_0= ruleDef ) )* )
            // InternalFoo.g:79:3: ( (lv_vars_0_0= ruleVar ) )* ( (lv_defs_1_0= ruleDef ) )*
            {
            // InternalFoo.g:79:3: ( (lv_vars_0_0= ruleVar ) )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==11) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // InternalFoo.g:80:4: (lv_vars_0_0= ruleVar )
            	    {
            	    // InternalFoo.g:80:4: (lv_vars_0_0= ruleVar )
            	    // InternalFoo.g:81:5: lv_vars_0_0= ruleVar
            	    {

            	    					newCompositeNode(grammarAccess.getModuleAccess().getVarsVarParserRuleCall_0_0());
            	    				
            	    pushFollow(FOLLOW_3);
            	    lv_vars_0_0=ruleVar();

            	    state._fsp--;


            	    					if (current==null) {
            	    						current = createModelElementForParent(grammarAccess.getModuleRule());
            	    					}
            	    					add(
            	    						current,
            	    						"vars",
            	    						lv_vars_0_0,
            	    						"org.eclipse.handly.examples.basic.Foo.Var");
            	    					afterParserOrEnumRuleCall();
            	    				

            	    }


            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);

            // InternalFoo.g:98:3: ( (lv_defs_1_0= ruleDef ) )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( (LA2_0==13) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // InternalFoo.g:99:4: (lv_defs_1_0= ruleDef )
            	    {
            	    // InternalFoo.g:99:4: (lv_defs_1_0= ruleDef )
            	    // InternalFoo.g:100:5: lv_defs_1_0= ruleDef
            	    {

            	    					newCompositeNode(grammarAccess.getModuleAccess().getDefsDefParserRuleCall_1_0());
            	    				
            	    pushFollow(FOLLOW_4);
            	    lv_defs_1_0=ruleDef();

            	    state._fsp--;


            	    					if (current==null) {
            	    						current = createModelElementForParent(grammarAccess.getModuleRule());
            	    					}
            	    					add(
            	    						current,
            	    						"defs",
            	    						lv_defs_1_0,
            	    						"org.eclipse.handly.examples.basic.Foo.Def");
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
    // InternalFoo.g:121:1: entryRuleVar returns [EObject current=null] : iv_ruleVar= ruleVar EOF ;
    public final EObject entryRuleVar() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleVar = null;


        try {
            // InternalFoo.g:121:44: (iv_ruleVar= ruleVar EOF )
            // InternalFoo.g:122:2: iv_ruleVar= ruleVar EOF
            {
             newCompositeNode(grammarAccess.getVarRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleVar=ruleVar();

            state._fsp--;

             current =iv_ruleVar; 
            match(input,EOF,FOLLOW_2); 

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
    // InternalFoo.g:128:1: ruleVar returns [EObject current=null] : (otherlv_0= 'var' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= ';' ) ;
    public final EObject ruleVar() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token lv_name_1_0=null;
        Token otherlv_2=null;


        	enterRule();

        try {
            // InternalFoo.g:134:2: ( (otherlv_0= 'var' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= ';' ) )
            // InternalFoo.g:135:2: (otherlv_0= 'var' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= ';' )
            {
            // InternalFoo.g:135:2: (otherlv_0= 'var' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= ';' )
            // InternalFoo.g:136:3: otherlv_0= 'var' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= ';'
            {
            otherlv_0=(Token)match(input,11,FOLLOW_5); 

            			newLeafNode(otherlv_0, grammarAccess.getVarAccess().getVarKeyword_0());
            		
            // InternalFoo.g:140:3: ( (lv_name_1_0= RULE_ID ) )
            // InternalFoo.g:141:4: (lv_name_1_0= RULE_ID )
            {
            // InternalFoo.g:141:4: (lv_name_1_0= RULE_ID )
            // InternalFoo.g:142:5: lv_name_1_0= RULE_ID
            {
            lv_name_1_0=(Token)match(input,RULE_ID,FOLLOW_6); 

            					newLeafNode(lv_name_1_0, grammarAccess.getVarAccess().getNameIDTerminalRuleCall_1_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getVarRule());
            					}
            					setWithLastConsumed(
            						current,
            						"name",
            						lv_name_1_0,
            						"org.eclipse.xtext.common.Terminals.ID");
            				

            }


            }

            otherlv_2=(Token)match(input,12,FOLLOW_2); 

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
    // InternalFoo.g:166:1: entryRuleDef returns [EObject current=null] : iv_ruleDef= ruleDef EOF ;
    public final EObject entryRuleDef() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleDef = null;


        try {
            // InternalFoo.g:166:44: (iv_ruleDef= ruleDef EOF )
            // InternalFoo.g:167:2: iv_ruleDef= ruleDef EOF
            {
             newCompositeNode(grammarAccess.getDefRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleDef=ruleDef();

            state._fsp--;

             current =iv_ruleDef; 
            match(input,EOF,FOLLOW_2); 

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
    // InternalFoo.g:173:1: ruleDef returns [EObject current=null] : (otherlv_0= 'def' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= '(' ( (lv_params_3_0= RULE_ID ) )? (otherlv_4= ',' ( (lv_params_5_0= RULE_ID ) ) )* otherlv_6= ')' otherlv_7= '{' otherlv_8= '}' ) ;
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
            // InternalFoo.g:179:2: ( (otherlv_0= 'def' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= '(' ( (lv_params_3_0= RULE_ID ) )? (otherlv_4= ',' ( (lv_params_5_0= RULE_ID ) ) )* otherlv_6= ')' otherlv_7= '{' otherlv_8= '}' ) )
            // InternalFoo.g:180:2: (otherlv_0= 'def' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= '(' ( (lv_params_3_0= RULE_ID ) )? (otherlv_4= ',' ( (lv_params_5_0= RULE_ID ) ) )* otherlv_6= ')' otherlv_7= '{' otherlv_8= '}' )
            {
            // InternalFoo.g:180:2: (otherlv_0= 'def' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= '(' ( (lv_params_3_0= RULE_ID ) )? (otherlv_4= ',' ( (lv_params_5_0= RULE_ID ) ) )* otherlv_6= ')' otherlv_7= '{' otherlv_8= '}' )
            // InternalFoo.g:181:3: otherlv_0= 'def' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= '(' ( (lv_params_3_0= RULE_ID ) )? (otherlv_4= ',' ( (lv_params_5_0= RULE_ID ) ) )* otherlv_6= ')' otherlv_7= '{' otherlv_8= '}'
            {
            otherlv_0=(Token)match(input,13,FOLLOW_5); 

            			newLeafNode(otherlv_0, grammarAccess.getDefAccess().getDefKeyword_0());
            		
            // InternalFoo.g:185:3: ( (lv_name_1_0= RULE_ID ) )
            // InternalFoo.g:186:4: (lv_name_1_0= RULE_ID )
            {
            // InternalFoo.g:186:4: (lv_name_1_0= RULE_ID )
            // InternalFoo.g:187:5: lv_name_1_0= RULE_ID
            {
            lv_name_1_0=(Token)match(input,RULE_ID,FOLLOW_7); 

            					newLeafNode(lv_name_1_0, grammarAccess.getDefAccess().getNameIDTerminalRuleCall_1_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getDefRule());
            					}
            					setWithLastConsumed(
            						current,
            						"name",
            						lv_name_1_0,
            						"org.eclipse.xtext.common.Terminals.ID");
            				

            }


            }

            otherlv_2=(Token)match(input,14,FOLLOW_8); 

            			newLeafNode(otherlv_2, grammarAccess.getDefAccess().getLeftParenthesisKeyword_2());
            		
            // InternalFoo.g:207:3: ( (lv_params_3_0= RULE_ID ) )?
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==RULE_ID) ) {
                alt3=1;
            }
            switch (alt3) {
                case 1 :
                    // InternalFoo.g:208:4: (lv_params_3_0= RULE_ID )
                    {
                    // InternalFoo.g:208:4: (lv_params_3_0= RULE_ID )
                    // InternalFoo.g:209:5: lv_params_3_0= RULE_ID
                    {
                    lv_params_3_0=(Token)match(input,RULE_ID,FOLLOW_9); 

                    					newLeafNode(lv_params_3_0, grammarAccess.getDefAccess().getParamsIDTerminalRuleCall_3_0());
                    				

                    					if (current==null) {
                    						current = createModelElement(grammarAccess.getDefRule());
                    					}
                    					addWithLastConsumed(
                    						current,
                    						"params",
                    						lv_params_3_0,
                    						"org.eclipse.xtext.common.Terminals.ID");
                    				

                    }


                    }
                    break;

            }

            // InternalFoo.g:225:3: (otherlv_4= ',' ( (lv_params_5_0= RULE_ID ) ) )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( (LA4_0==15) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // InternalFoo.g:226:4: otherlv_4= ',' ( (lv_params_5_0= RULE_ID ) )
            	    {
            	    otherlv_4=(Token)match(input,15,FOLLOW_5); 

            	    				newLeafNode(otherlv_4, grammarAccess.getDefAccess().getCommaKeyword_4_0());
            	    			
            	    // InternalFoo.g:230:4: ( (lv_params_5_0= RULE_ID ) )
            	    // InternalFoo.g:231:5: (lv_params_5_0= RULE_ID )
            	    {
            	    // InternalFoo.g:231:5: (lv_params_5_0= RULE_ID )
            	    // InternalFoo.g:232:6: lv_params_5_0= RULE_ID
            	    {
            	    lv_params_5_0=(Token)match(input,RULE_ID,FOLLOW_9); 

            	    						newLeafNode(lv_params_5_0, grammarAccess.getDefAccess().getParamsIDTerminalRuleCall_4_1_0());
            	    					

            	    						if (current==null) {
            	    							current = createModelElement(grammarAccess.getDefRule());
            	    						}
            	    						addWithLastConsumed(
            	    							current,
            	    							"params",
            	    							lv_params_5_0,
            	    							"org.eclipse.xtext.common.Terminals.ID");
            	    					

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop4;
                }
            } while (true);

            otherlv_6=(Token)match(input,16,FOLLOW_10); 

            			newLeafNode(otherlv_6, grammarAccess.getDefAccess().getRightParenthesisKeyword_5());
            		
            otherlv_7=(Token)match(input,17,FOLLOW_11); 

            			newLeafNode(otherlv_7, grammarAccess.getDefAccess().getLeftCurlyBracketKeyword_6());
            		
            otherlv_8=(Token)match(input,18,FOLLOW_2); 

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


 

    public static final BitSet FOLLOW_1 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_2 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_3 = new BitSet(new long[]{0x0000000000002802L});
    public static final BitSet FOLLOW_4 = new BitSet(new long[]{0x0000000000002002L});
    public static final BitSet FOLLOW_5 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_6 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_7 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_8 = new BitSet(new long[]{0x0000000000018010L});
    public static final BitSet FOLLOW_9 = new BitSet(new long[]{0x0000000000018000L});
    public static final BitSet FOLLOW_10 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_11 = new BitSet(new long[]{0x0000000000040000L});

}
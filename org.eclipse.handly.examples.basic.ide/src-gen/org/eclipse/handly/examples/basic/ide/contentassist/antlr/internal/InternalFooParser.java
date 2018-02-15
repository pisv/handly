package org.eclipse.handly.examples.basic.ide.contentassist.antlr.internal;

import java.io.InputStream;
import org.eclipse.xtext.*;
import org.eclipse.xtext.parser.*;
import org.eclipse.xtext.parser.impl.*;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.parser.antlr.XtextTokenStream;
import org.eclipse.xtext.parser.antlr.XtextTokenStream.HiddenTokens;
import org.eclipse.xtext.ide.editor.contentassist.antlr.internal.AbstractInternalContentAssistParser;
import org.eclipse.xtext.ide.editor.contentassist.antlr.internal.DFA;
import org.eclipse.handly.examples.basic.services.FooGrammarAccess;



import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("all")
public class InternalFooParser extends AbstractInternalContentAssistParser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "RULE_ID", "RULE_INT", "RULE_STRING", "RULE_ML_COMMENT", "RULE_SL_COMMENT", "RULE_WS", "RULE_ANY_OTHER", "'var'", "';'", "'def'", "'('", "')'", "'{'", "'}'", "','"
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

    	public void setGrammarAccess(FooGrammarAccess grammarAccess) {
    		this.grammarAccess = grammarAccess;
    	}

    	@Override
    	protected Grammar getGrammar() {
    		return grammarAccess.getGrammar();
    	}

    	@Override
    	protected String getValueForTokenName(String tokenName) {
    		return tokenName;
    	}



    // $ANTLR start "entryRuleModule"
    // InternalFoo.g:53:1: entryRuleModule : ruleModule EOF ;
    public final void entryRuleModule() throws RecognitionException {
        try {
            // InternalFoo.g:54:1: ( ruleModule EOF )
            // InternalFoo.g:55:1: ruleModule EOF
            {
             before(grammarAccess.getModuleRule()); 
            pushFollow(FOLLOW_1);
            ruleModule();

            state._fsp--;

             after(grammarAccess.getModuleRule()); 
            match(input,EOF,FOLLOW_2); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "entryRuleModule"


    // $ANTLR start "ruleModule"
    // InternalFoo.g:62:1: ruleModule : ( ( rule__Module__Group__0 ) ) ;
    public final void ruleModule() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalFoo.g:66:2: ( ( ( rule__Module__Group__0 ) ) )
            // InternalFoo.g:67:2: ( ( rule__Module__Group__0 ) )
            {
            // InternalFoo.g:67:2: ( ( rule__Module__Group__0 ) )
            // InternalFoo.g:68:3: ( rule__Module__Group__0 )
            {
             before(grammarAccess.getModuleAccess().getGroup()); 
            // InternalFoo.g:69:3: ( rule__Module__Group__0 )
            // InternalFoo.g:69:4: rule__Module__Group__0
            {
            pushFollow(FOLLOW_2);
            rule__Module__Group__0();

            state._fsp--;


            }

             after(grammarAccess.getModuleAccess().getGroup()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "ruleModule"


    // $ANTLR start "entryRuleVar"
    // InternalFoo.g:78:1: entryRuleVar : ruleVar EOF ;
    public final void entryRuleVar() throws RecognitionException {
        try {
            // InternalFoo.g:79:1: ( ruleVar EOF )
            // InternalFoo.g:80:1: ruleVar EOF
            {
             before(grammarAccess.getVarRule()); 
            pushFollow(FOLLOW_1);
            ruleVar();

            state._fsp--;

             after(grammarAccess.getVarRule()); 
            match(input,EOF,FOLLOW_2); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "entryRuleVar"


    // $ANTLR start "ruleVar"
    // InternalFoo.g:87:1: ruleVar : ( ( rule__Var__Group__0 ) ) ;
    public final void ruleVar() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalFoo.g:91:2: ( ( ( rule__Var__Group__0 ) ) )
            // InternalFoo.g:92:2: ( ( rule__Var__Group__0 ) )
            {
            // InternalFoo.g:92:2: ( ( rule__Var__Group__0 ) )
            // InternalFoo.g:93:3: ( rule__Var__Group__0 )
            {
             before(grammarAccess.getVarAccess().getGroup()); 
            // InternalFoo.g:94:3: ( rule__Var__Group__0 )
            // InternalFoo.g:94:4: rule__Var__Group__0
            {
            pushFollow(FOLLOW_2);
            rule__Var__Group__0();

            state._fsp--;


            }

             after(grammarAccess.getVarAccess().getGroup()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "ruleVar"


    // $ANTLR start "entryRuleDef"
    // InternalFoo.g:103:1: entryRuleDef : ruleDef EOF ;
    public final void entryRuleDef() throws RecognitionException {
        try {
            // InternalFoo.g:104:1: ( ruleDef EOF )
            // InternalFoo.g:105:1: ruleDef EOF
            {
             before(grammarAccess.getDefRule()); 
            pushFollow(FOLLOW_1);
            ruleDef();

            state._fsp--;

             after(grammarAccess.getDefRule()); 
            match(input,EOF,FOLLOW_2); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "entryRuleDef"


    // $ANTLR start "ruleDef"
    // InternalFoo.g:112:1: ruleDef : ( ( rule__Def__Group__0 ) ) ;
    public final void ruleDef() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalFoo.g:116:2: ( ( ( rule__Def__Group__0 ) ) )
            // InternalFoo.g:117:2: ( ( rule__Def__Group__0 ) )
            {
            // InternalFoo.g:117:2: ( ( rule__Def__Group__0 ) )
            // InternalFoo.g:118:3: ( rule__Def__Group__0 )
            {
             before(grammarAccess.getDefAccess().getGroup()); 
            // InternalFoo.g:119:3: ( rule__Def__Group__0 )
            // InternalFoo.g:119:4: rule__Def__Group__0
            {
            pushFollow(FOLLOW_2);
            rule__Def__Group__0();

            state._fsp--;


            }

             after(grammarAccess.getDefAccess().getGroup()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "ruleDef"


    // $ANTLR start "rule__Module__Group__0"
    // InternalFoo.g:127:1: rule__Module__Group__0 : rule__Module__Group__0__Impl rule__Module__Group__1 ;
    public final void rule__Module__Group__0() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalFoo.g:131:1: ( rule__Module__Group__0__Impl rule__Module__Group__1 )
            // InternalFoo.g:132:2: rule__Module__Group__0__Impl rule__Module__Group__1
            {
            pushFollow(FOLLOW_3);
            rule__Module__Group__0__Impl();

            state._fsp--;

            pushFollow(FOLLOW_2);
            rule__Module__Group__1();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Module__Group__0"


    // $ANTLR start "rule__Module__Group__0__Impl"
    // InternalFoo.g:139:1: rule__Module__Group__0__Impl : ( ( rule__Module__VarsAssignment_0 )* ) ;
    public final void rule__Module__Group__0__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalFoo.g:143:1: ( ( ( rule__Module__VarsAssignment_0 )* ) )
            // InternalFoo.g:144:1: ( ( rule__Module__VarsAssignment_0 )* )
            {
            // InternalFoo.g:144:1: ( ( rule__Module__VarsAssignment_0 )* )
            // InternalFoo.g:145:2: ( rule__Module__VarsAssignment_0 )*
            {
             before(grammarAccess.getModuleAccess().getVarsAssignment_0()); 
            // InternalFoo.g:146:2: ( rule__Module__VarsAssignment_0 )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==11) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // InternalFoo.g:146:3: rule__Module__VarsAssignment_0
            	    {
            	    pushFollow(FOLLOW_4);
            	    rule__Module__VarsAssignment_0();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);

             after(grammarAccess.getModuleAccess().getVarsAssignment_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Module__Group__0__Impl"


    // $ANTLR start "rule__Module__Group__1"
    // InternalFoo.g:154:1: rule__Module__Group__1 : rule__Module__Group__1__Impl ;
    public final void rule__Module__Group__1() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalFoo.g:158:1: ( rule__Module__Group__1__Impl )
            // InternalFoo.g:159:2: rule__Module__Group__1__Impl
            {
            pushFollow(FOLLOW_2);
            rule__Module__Group__1__Impl();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Module__Group__1"


    // $ANTLR start "rule__Module__Group__1__Impl"
    // InternalFoo.g:165:1: rule__Module__Group__1__Impl : ( ( rule__Module__DefsAssignment_1 )* ) ;
    public final void rule__Module__Group__1__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalFoo.g:169:1: ( ( ( rule__Module__DefsAssignment_1 )* ) )
            // InternalFoo.g:170:1: ( ( rule__Module__DefsAssignment_1 )* )
            {
            // InternalFoo.g:170:1: ( ( rule__Module__DefsAssignment_1 )* )
            // InternalFoo.g:171:2: ( rule__Module__DefsAssignment_1 )*
            {
             before(grammarAccess.getModuleAccess().getDefsAssignment_1()); 
            // InternalFoo.g:172:2: ( rule__Module__DefsAssignment_1 )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( (LA2_0==13) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // InternalFoo.g:172:3: rule__Module__DefsAssignment_1
            	    {
            	    pushFollow(FOLLOW_5);
            	    rule__Module__DefsAssignment_1();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);

             after(grammarAccess.getModuleAccess().getDefsAssignment_1()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Module__Group__1__Impl"


    // $ANTLR start "rule__Var__Group__0"
    // InternalFoo.g:181:1: rule__Var__Group__0 : rule__Var__Group__0__Impl rule__Var__Group__1 ;
    public final void rule__Var__Group__0() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalFoo.g:185:1: ( rule__Var__Group__0__Impl rule__Var__Group__1 )
            // InternalFoo.g:186:2: rule__Var__Group__0__Impl rule__Var__Group__1
            {
            pushFollow(FOLLOW_6);
            rule__Var__Group__0__Impl();

            state._fsp--;

            pushFollow(FOLLOW_2);
            rule__Var__Group__1();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Var__Group__0"


    // $ANTLR start "rule__Var__Group__0__Impl"
    // InternalFoo.g:193:1: rule__Var__Group__0__Impl : ( 'var' ) ;
    public final void rule__Var__Group__0__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalFoo.g:197:1: ( ( 'var' ) )
            // InternalFoo.g:198:1: ( 'var' )
            {
            // InternalFoo.g:198:1: ( 'var' )
            // InternalFoo.g:199:2: 'var'
            {
             before(grammarAccess.getVarAccess().getVarKeyword_0()); 
            match(input,11,FOLLOW_2); 
             after(grammarAccess.getVarAccess().getVarKeyword_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Var__Group__0__Impl"


    // $ANTLR start "rule__Var__Group__1"
    // InternalFoo.g:208:1: rule__Var__Group__1 : rule__Var__Group__1__Impl rule__Var__Group__2 ;
    public final void rule__Var__Group__1() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalFoo.g:212:1: ( rule__Var__Group__1__Impl rule__Var__Group__2 )
            // InternalFoo.g:213:2: rule__Var__Group__1__Impl rule__Var__Group__2
            {
            pushFollow(FOLLOW_7);
            rule__Var__Group__1__Impl();

            state._fsp--;

            pushFollow(FOLLOW_2);
            rule__Var__Group__2();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Var__Group__1"


    // $ANTLR start "rule__Var__Group__1__Impl"
    // InternalFoo.g:220:1: rule__Var__Group__1__Impl : ( ( rule__Var__NameAssignment_1 ) ) ;
    public final void rule__Var__Group__1__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalFoo.g:224:1: ( ( ( rule__Var__NameAssignment_1 ) ) )
            // InternalFoo.g:225:1: ( ( rule__Var__NameAssignment_1 ) )
            {
            // InternalFoo.g:225:1: ( ( rule__Var__NameAssignment_1 ) )
            // InternalFoo.g:226:2: ( rule__Var__NameAssignment_1 )
            {
             before(grammarAccess.getVarAccess().getNameAssignment_1()); 
            // InternalFoo.g:227:2: ( rule__Var__NameAssignment_1 )
            // InternalFoo.g:227:3: rule__Var__NameAssignment_1
            {
            pushFollow(FOLLOW_2);
            rule__Var__NameAssignment_1();

            state._fsp--;


            }

             after(grammarAccess.getVarAccess().getNameAssignment_1()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Var__Group__1__Impl"


    // $ANTLR start "rule__Var__Group__2"
    // InternalFoo.g:235:1: rule__Var__Group__2 : rule__Var__Group__2__Impl ;
    public final void rule__Var__Group__2() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalFoo.g:239:1: ( rule__Var__Group__2__Impl )
            // InternalFoo.g:240:2: rule__Var__Group__2__Impl
            {
            pushFollow(FOLLOW_2);
            rule__Var__Group__2__Impl();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Var__Group__2"


    // $ANTLR start "rule__Var__Group__2__Impl"
    // InternalFoo.g:246:1: rule__Var__Group__2__Impl : ( ';' ) ;
    public final void rule__Var__Group__2__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalFoo.g:250:1: ( ( ';' ) )
            // InternalFoo.g:251:1: ( ';' )
            {
            // InternalFoo.g:251:1: ( ';' )
            // InternalFoo.g:252:2: ';'
            {
             before(grammarAccess.getVarAccess().getSemicolonKeyword_2()); 
            match(input,12,FOLLOW_2); 
             after(grammarAccess.getVarAccess().getSemicolonKeyword_2()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Var__Group__2__Impl"


    // $ANTLR start "rule__Def__Group__0"
    // InternalFoo.g:262:1: rule__Def__Group__0 : rule__Def__Group__0__Impl rule__Def__Group__1 ;
    public final void rule__Def__Group__0() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalFoo.g:266:1: ( rule__Def__Group__0__Impl rule__Def__Group__1 )
            // InternalFoo.g:267:2: rule__Def__Group__0__Impl rule__Def__Group__1
            {
            pushFollow(FOLLOW_6);
            rule__Def__Group__0__Impl();

            state._fsp--;

            pushFollow(FOLLOW_2);
            rule__Def__Group__1();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Def__Group__0"


    // $ANTLR start "rule__Def__Group__0__Impl"
    // InternalFoo.g:274:1: rule__Def__Group__0__Impl : ( 'def' ) ;
    public final void rule__Def__Group__0__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalFoo.g:278:1: ( ( 'def' ) )
            // InternalFoo.g:279:1: ( 'def' )
            {
            // InternalFoo.g:279:1: ( 'def' )
            // InternalFoo.g:280:2: 'def'
            {
             before(grammarAccess.getDefAccess().getDefKeyword_0()); 
            match(input,13,FOLLOW_2); 
             after(grammarAccess.getDefAccess().getDefKeyword_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Def__Group__0__Impl"


    // $ANTLR start "rule__Def__Group__1"
    // InternalFoo.g:289:1: rule__Def__Group__1 : rule__Def__Group__1__Impl rule__Def__Group__2 ;
    public final void rule__Def__Group__1() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalFoo.g:293:1: ( rule__Def__Group__1__Impl rule__Def__Group__2 )
            // InternalFoo.g:294:2: rule__Def__Group__1__Impl rule__Def__Group__2
            {
            pushFollow(FOLLOW_8);
            rule__Def__Group__1__Impl();

            state._fsp--;

            pushFollow(FOLLOW_2);
            rule__Def__Group__2();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Def__Group__1"


    // $ANTLR start "rule__Def__Group__1__Impl"
    // InternalFoo.g:301:1: rule__Def__Group__1__Impl : ( ( rule__Def__NameAssignment_1 ) ) ;
    public final void rule__Def__Group__1__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalFoo.g:305:1: ( ( ( rule__Def__NameAssignment_1 ) ) )
            // InternalFoo.g:306:1: ( ( rule__Def__NameAssignment_1 ) )
            {
            // InternalFoo.g:306:1: ( ( rule__Def__NameAssignment_1 ) )
            // InternalFoo.g:307:2: ( rule__Def__NameAssignment_1 )
            {
             before(grammarAccess.getDefAccess().getNameAssignment_1()); 
            // InternalFoo.g:308:2: ( rule__Def__NameAssignment_1 )
            // InternalFoo.g:308:3: rule__Def__NameAssignment_1
            {
            pushFollow(FOLLOW_2);
            rule__Def__NameAssignment_1();

            state._fsp--;


            }

             after(grammarAccess.getDefAccess().getNameAssignment_1()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Def__Group__1__Impl"


    // $ANTLR start "rule__Def__Group__2"
    // InternalFoo.g:316:1: rule__Def__Group__2 : rule__Def__Group__2__Impl rule__Def__Group__3 ;
    public final void rule__Def__Group__2() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalFoo.g:320:1: ( rule__Def__Group__2__Impl rule__Def__Group__3 )
            // InternalFoo.g:321:2: rule__Def__Group__2__Impl rule__Def__Group__3
            {
            pushFollow(FOLLOW_9);
            rule__Def__Group__2__Impl();

            state._fsp--;

            pushFollow(FOLLOW_2);
            rule__Def__Group__3();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Def__Group__2"


    // $ANTLR start "rule__Def__Group__2__Impl"
    // InternalFoo.g:328:1: rule__Def__Group__2__Impl : ( '(' ) ;
    public final void rule__Def__Group__2__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalFoo.g:332:1: ( ( '(' ) )
            // InternalFoo.g:333:1: ( '(' )
            {
            // InternalFoo.g:333:1: ( '(' )
            // InternalFoo.g:334:2: '('
            {
             before(grammarAccess.getDefAccess().getLeftParenthesisKeyword_2()); 
            match(input,14,FOLLOW_2); 
             after(grammarAccess.getDefAccess().getLeftParenthesisKeyword_2()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Def__Group__2__Impl"


    // $ANTLR start "rule__Def__Group__3"
    // InternalFoo.g:343:1: rule__Def__Group__3 : rule__Def__Group__3__Impl rule__Def__Group__4 ;
    public final void rule__Def__Group__3() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalFoo.g:347:1: ( rule__Def__Group__3__Impl rule__Def__Group__4 )
            // InternalFoo.g:348:2: rule__Def__Group__3__Impl rule__Def__Group__4
            {
            pushFollow(FOLLOW_9);
            rule__Def__Group__3__Impl();

            state._fsp--;

            pushFollow(FOLLOW_2);
            rule__Def__Group__4();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Def__Group__3"


    // $ANTLR start "rule__Def__Group__3__Impl"
    // InternalFoo.g:355:1: rule__Def__Group__3__Impl : ( ( rule__Def__ParamsAssignment_3 )? ) ;
    public final void rule__Def__Group__3__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalFoo.g:359:1: ( ( ( rule__Def__ParamsAssignment_3 )? ) )
            // InternalFoo.g:360:1: ( ( rule__Def__ParamsAssignment_3 )? )
            {
            // InternalFoo.g:360:1: ( ( rule__Def__ParamsAssignment_3 )? )
            // InternalFoo.g:361:2: ( rule__Def__ParamsAssignment_3 )?
            {
             before(grammarAccess.getDefAccess().getParamsAssignment_3()); 
            // InternalFoo.g:362:2: ( rule__Def__ParamsAssignment_3 )?
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==RULE_ID) ) {
                alt3=1;
            }
            switch (alt3) {
                case 1 :
                    // InternalFoo.g:362:3: rule__Def__ParamsAssignment_3
                    {
                    pushFollow(FOLLOW_2);
                    rule__Def__ParamsAssignment_3();

                    state._fsp--;


                    }
                    break;

            }

             after(grammarAccess.getDefAccess().getParamsAssignment_3()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Def__Group__3__Impl"


    // $ANTLR start "rule__Def__Group__4"
    // InternalFoo.g:370:1: rule__Def__Group__4 : rule__Def__Group__4__Impl rule__Def__Group__5 ;
    public final void rule__Def__Group__4() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalFoo.g:374:1: ( rule__Def__Group__4__Impl rule__Def__Group__5 )
            // InternalFoo.g:375:2: rule__Def__Group__4__Impl rule__Def__Group__5
            {
            pushFollow(FOLLOW_9);
            rule__Def__Group__4__Impl();

            state._fsp--;

            pushFollow(FOLLOW_2);
            rule__Def__Group__5();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Def__Group__4"


    // $ANTLR start "rule__Def__Group__4__Impl"
    // InternalFoo.g:382:1: rule__Def__Group__4__Impl : ( ( rule__Def__Group_4__0 )* ) ;
    public final void rule__Def__Group__4__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalFoo.g:386:1: ( ( ( rule__Def__Group_4__0 )* ) )
            // InternalFoo.g:387:1: ( ( rule__Def__Group_4__0 )* )
            {
            // InternalFoo.g:387:1: ( ( rule__Def__Group_4__0 )* )
            // InternalFoo.g:388:2: ( rule__Def__Group_4__0 )*
            {
             before(grammarAccess.getDefAccess().getGroup_4()); 
            // InternalFoo.g:389:2: ( rule__Def__Group_4__0 )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( (LA4_0==18) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // InternalFoo.g:389:3: rule__Def__Group_4__0
            	    {
            	    pushFollow(FOLLOW_10);
            	    rule__Def__Group_4__0();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop4;
                }
            } while (true);

             after(grammarAccess.getDefAccess().getGroup_4()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Def__Group__4__Impl"


    // $ANTLR start "rule__Def__Group__5"
    // InternalFoo.g:397:1: rule__Def__Group__5 : rule__Def__Group__5__Impl rule__Def__Group__6 ;
    public final void rule__Def__Group__5() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalFoo.g:401:1: ( rule__Def__Group__5__Impl rule__Def__Group__6 )
            // InternalFoo.g:402:2: rule__Def__Group__5__Impl rule__Def__Group__6
            {
            pushFollow(FOLLOW_11);
            rule__Def__Group__5__Impl();

            state._fsp--;

            pushFollow(FOLLOW_2);
            rule__Def__Group__6();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Def__Group__5"


    // $ANTLR start "rule__Def__Group__5__Impl"
    // InternalFoo.g:409:1: rule__Def__Group__5__Impl : ( ')' ) ;
    public final void rule__Def__Group__5__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalFoo.g:413:1: ( ( ')' ) )
            // InternalFoo.g:414:1: ( ')' )
            {
            // InternalFoo.g:414:1: ( ')' )
            // InternalFoo.g:415:2: ')'
            {
             before(grammarAccess.getDefAccess().getRightParenthesisKeyword_5()); 
            match(input,15,FOLLOW_2); 
             after(grammarAccess.getDefAccess().getRightParenthesisKeyword_5()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Def__Group__5__Impl"


    // $ANTLR start "rule__Def__Group__6"
    // InternalFoo.g:424:1: rule__Def__Group__6 : rule__Def__Group__6__Impl rule__Def__Group__7 ;
    public final void rule__Def__Group__6() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalFoo.g:428:1: ( rule__Def__Group__6__Impl rule__Def__Group__7 )
            // InternalFoo.g:429:2: rule__Def__Group__6__Impl rule__Def__Group__7
            {
            pushFollow(FOLLOW_12);
            rule__Def__Group__6__Impl();

            state._fsp--;

            pushFollow(FOLLOW_2);
            rule__Def__Group__7();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Def__Group__6"


    // $ANTLR start "rule__Def__Group__6__Impl"
    // InternalFoo.g:436:1: rule__Def__Group__6__Impl : ( '{' ) ;
    public final void rule__Def__Group__6__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalFoo.g:440:1: ( ( '{' ) )
            // InternalFoo.g:441:1: ( '{' )
            {
            // InternalFoo.g:441:1: ( '{' )
            // InternalFoo.g:442:2: '{'
            {
             before(grammarAccess.getDefAccess().getLeftCurlyBracketKeyword_6()); 
            match(input,16,FOLLOW_2); 
             after(grammarAccess.getDefAccess().getLeftCurlyBracketKeyword_6()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Def__Group__6__Impl"


    // $ANTLR start "rule__Def__Group__7"
    // InternalFoo.g:451:1: rule__Def__Group__7 : rule__Def__Group__7__Impl ;
    public final void rule__Def__Group__7() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalFoo.g:455:1: ( rule__Def__Group__7__Impl )
            // InternalFoo.g:456:2: rule__Def__Group__7__Impl
            {
            pushFollow(FOLLOW_2);
            rule__Def__Group__7__Impl();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Def__Group__7"


    // $ANTLR start "rule__Def__Group__7__Impl"
    // InternalFoo.g:462:1: rule__Def__Group__7__Impl : ( '}' ) ;
    public final void rule__Def__Group__7__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalFoo.g:466:1: ( ( '}' ) )
            // InternalFoo.g:467:1: ( '}' )
            {
            // InternalFoo.g:467:1: ( '}' )
            // InternalFoo.g:468:2: '}'
            {
             before(grammarAccess.getDefAccess().getRightCurlyBracketKeyword_7()); 
            match(input,17,FOLLOW_2); 
             after(grammarAccess.getDefAccess().getRightCurlyBracketKeyword_7()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Def__Group__7__Impl"


    // $ANTLR start "rule__Def__Group_4__0"
    // InternalFoo.g:478:1: rule__Def__Group_4__0 : rule__Def__Group_4__0__Impl rule__Def__Group_4__1 ;
    public final void rule__Def__Group_4__0() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalFoo.g:482:1: ( rule__Def__Group_4__0__Impl rule__Def__Group_4__1 )
            // InternalFoo.g:483:2: rule__Def__Group_4__0__Impl rule__Def__Group_4__1
            {
            pushFollow(FOLLOW_6);
            rule__Def__Group_4__0__Impl();

            state._fsp--;

            pushFollow(FOLLOW_2);
            rule__Def__Group_4__1();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Def__Group_4__0"


    // $ANTLR start "rule__Def__Group_4__0__Impl"
    // InternalFoo.g:490:1: rule__Def__Group_4__0__Impl : ( ',' ) ;
    public final void rule__Def__Group_4__0__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalFoo.g:494:1: ( ( ',' ) )
            // InternalFoo.g:495:1: ( ',' )
            {
            // InternalFoo.g:495:1: ( ',' )
            // InternalFoo.g:496:2: ','
            {
             before(grammarAccess.getDefAccess().getCommaKeyword_4_0()); 
            match(input,18,FOLLOW_2); 
             after(grammarAccess.getDefAccess().getCommaKeyword_4_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Def__Group_4__0__Impl"


    // $ANTLR start "rule__Def__Group_4__1"
    // InternalFoo.g:505:1: rule__Def__Group_4__1 : rule__Def__Group_4__1__Impl ;
    public final void rule__Def__Group_4__1() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalFoo.g:509:1: ( rule__Def__Group_4__1__Impl )
            // InternalFoo.g:510:2: rule__Def__Group_4__1__Impl
            {
            pushFollow(FOLLOW_2);
            rule__Def__Group_4__1__Impl();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Def__Group_4__1"


    // $ANTLR start "rule__Def__Group_4__1__Impl"
    // InternalFoo.g:516:1: rule__Def__Group_4__1__Impl : ( ( rule__Def__ParamsAssignment_4_1 ) ) ;
    public final void rule__Def__Group_4__1__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalFoo.g:520:1: ( ( ( rule__Def__ParamsAssignment_4_1 ) ) )
            // InternalFoo.g:521:1: ( ( rule__Def__ParamsAssignment_4_1 ) )
            {
            // InternalFoo.g:521:1: ( ( rule__Def__ParamsAssignment_4_1 ) )
            // InternalFoo.g:522:2: ( rule__Def__ParamsAssignment_4_1 )
            {
             before(grammarAccess.getDefAccess().getParamsAssignment_4_1()); 
            // InternalFoo.g:523:2: ( rule__Def__ParamsAssignment_4_1 )
            // InternalFoo.g:523:3: rule__Def__ParamsAssignment_4_1
            {
            pushFollow(FOLLOW_2);
            rule__Def__ParamsAssignment_4_1();

            state._fsp--;


            }

             after(grammarAccess.getDefAccess().getParamsAssignment_4_1()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Def__Group_4__1__Impl"


    // $ANTLR start "rule__Module__VarsAssignment_0"
    // InternalFoo.g:532:1: rule__Module__VarsAssignment_0 : ( ruleVar ) ;
    public final void rule__Module__VarsAssignment_0() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalFoo.g:536:1: ( ( ruleVar ) )
            // InternalFoo.g:537:2: ( ruleVar )
            {
            // InternalFoo.g:537:2: ( ruleVar )
            // InternalFoo.g:538:3: ruleVar
            {
             before(grammarAccess.getModuleAccess().getVarsVarParserRuleCall_0_0()); 
            pushFollow(FOLLOW_2);
            ruleVar();

            state._fsp--;

             after(grammarAccess.getModuleAccess().getVarsVarParserRuleCall_0_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Module__VarsAssignment_0"


    // $ANTLR start "rule__Module__DefsAssignment_1"
    // InternalFoo.g:547:1: rule__Module__DefsAssignment_1 : ( ruleDef ) ;
    public final void rule__Module__DefsAssignment_1() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalFoo.g:551:1: ( ( ruleDef ) )
            // InternalFoo.g:552:2: ( ruleDef )
            {
            // InternalFoo.g:552:2: ( ruleDef )
            // InternalFoo.g:553:3: ruleDef
            {
             before(grammarAccess.getModuleAccess().getDefsDefParserRuleCall_1_0()); 
            pushFollow(FOLLOW_2);
            ruleDef();

            state._fsp--;

             after(grammarAccess.getModuleAccess().getDefsDefParserRuleCall_1_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Module__DefsAssignment_1"


    // $ANTLR start "rule__Var__NameAssignment_1"
    // InternalFoo.g:562:1: rule__Var__NameAssignment_1 : ( RULE_ID ) ;
    public final void rule__Var__NameAssignment_1() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalFoo.g:566:1: ( ( RULE_ID ) )
            // InternalFoo.g:567:2: ( RULE_ID )
            {
            // InternalFoo.g:567:2: ( RULE_ID )
            // InternalFoo.g:568:3: RULE_ID
            {
             before(grammarAccess.getVarAccess().getNameIDTerminalRuleCall_1_0()); 
            match(input,RULE_ID,FOLLOW_2); 
             after(grammarAccess.getVarAccess().getNameIDTerminalRuleCall_1_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Var__NameAssignment_1"


    // $ANTLR start "rule__Def__NameAssignment_1"
    // InternalFoo.g:577:1: rule__Def__NameAssignment_1 : ( RULE_ID ) ;
    public final void rule__Def__NameAssignment_1() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalFoo.g:581:1: ( ( RULE_ID ) )
            // InternalFoo.g:582:2: ( RULE_ID )
            {
            // InternalFoo.g:582:2: ( RULE_ID )
            // InternalFoo.g:583:3: RULE_ID
            {
             before(grammarAccess.getDefAccess().getNameIDTerminalRuleCall_1_0()); 
            match(input,RULE_ID,FOLLOW_2); 
             after(grammarAccess.getDefAccess().getNameIDTerminalRuleCall_1_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Def__NameAssignment_1"


    // $ANTLR start "rule__Def__ParamsAssignment_3"
    // InternalFoo.g:592:1: rule__Def__ParamsAssignment_3 : ( RULE_ID ) ;
    public final void rule__Def__ParamsAssignment_3() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalFoo.g:596:1: ( ( RULE_ID ) )
            // InternalFoo.g:597:2: ( RULE_ID )
            {
            // InternalFoo.g:597:2: ( RULE_ID )
            // InternalFoo.g:598:3: RULE_ID
            {
             before(grammarAccess.getDefAccess().getParamsIDTerminalRuleCall_3_0()); 
            match(input,RULE_ID,FOLLOW_2); 
             after(grammarAccess.getDefAccess().getParamsIDTerminalRuleCall_3_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Def__ParamsAssignment_3"


    // $ANTLR start "rule__Def__ParamsAssignment_4_1"
    // InternalFoo.g:607:1: rule__Def__ParamsAssignment_4_1 : ( RULE_ID ) ;
    public final void rule__Def__ParamsAssignment_4_1() throws RecognitionException {

        		int stackSize = keepStackSize();
        	
        try {
            // InternalFoo.g:611:1: ( ( RULE_ID ) )
            // InternalFoo.g:612:2: ( RULE_ID )
            {
            // InternalFoo.g:612:2: ( RULE_ID )
            // InternalFoo.g:613:3: RULE_ID
            {
             before(grammarAccess.getDefAccess().getParamsIDTerminalRuleCall_4_1_0()); 
            match(input,RULE_ID,FOLLOW_2); 
             after(grammarAccess.getDefAccess().getParamsIDTerminalRuleCall_4_1_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Def__ParamsAssignment_4_1"

    // Delegated rules


 

    public static final BitSet FOLLOW_1 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_2 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_3 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_4 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_5 = new BitSet(new long[]{0x0000000000002002L});
    public static final BitSet FOLLOW_6 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_7 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_8 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_9 = new BitSet(new long[]{0x0000000000048010L});
    public static final BitSet FOLLOW_10 = new BitSet(new long[]{0x0000000000040002L});
    public static final BitSet FOLLOW_11 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_12 = new BitSet(new long[]{0x0000000000020000L});

}
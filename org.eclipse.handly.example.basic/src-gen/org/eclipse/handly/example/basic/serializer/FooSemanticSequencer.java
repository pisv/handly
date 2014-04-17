package org.eclipse.handly.example.basic.serializer;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.handly.example.basic.foo.Def;
import org.eclipse.handly.example.basic.foo.FooPackage;
import org.eclipse.handly.example.basic.foo.Module;
import org.eclipse.handly.example.basic.foo.Var;
import org.eclipse.handly.example.basic.services.FooGrammarAccess;
import org.eclipse.xtext.serializer.acceptor.ISemanticSequenceAcceptor;
import org.eclipse.xtext.serializer.acceptor.SequenceFeeder;
import org.eclipse.xtext.serializer.diagnostic.ISemanticSequencerDiagnosticProvider;
import org.eclipse.xtext.serializer.diagnostic.ISerializationDiagnostic.Acceptor;
import org.eclipse.xtext.serializer.sequencer.AbstractDelegatingSemanticSequencer;
import org.eclipse.xtext.serializer.sequencer.GenericSequencer;
import org.eclipse.xtext.serializer.sequencer.ISemanticNodeProvider.INodesForEObjectProvider;
import org.eclipse.xtext.serializer.sequencer.ISemanticSequencer;
import org.eclipse.xtext.serializer.sequencer.ITransientValueService;
import org.eclipse.xtext.serializer.sequencer.ITransientValueService.ValueTransient;

@SuppressWarnings("all")
public class FooSemanticSequencer extends AbstractDelegatingSemanticSequencer {

	@Inject
	private FooGrammarAccess grammarAccess;
	
	public void createSequence(EObject context, EObject semanticObject) {
		if(semanticObject.eClass().getEPackage() == FooPackage.eINSTANCE) switch(semanticObject.eClass().getClassifierID()) {
			case FooPackage.DEF:
				if(context == grammarAccess.getDefRule()) {
					sequence_Def(context, (Def) semanticObject); 
					return; 
				}
				else break;
			case FooPackage.MODULE:
				if(context == grammarAccess.getModuleRule()) {
					sequence_Module(context, (Module) semanticObject); 
					return; 
				}
				else break;
			case FooPackage.VAR:
				if(context == grammarAccess.getVarRule()) {
					sequence_Var(context, (Var) semanticObject); 
					return; 
				}
				else break;
			}
		if (errorAcceptor != null) errorAcceptor.accept(diagnosticProvider.createInvalidContextOrTypeDiagnostic(semanticObject, context));
	}
	
	/**
	 * Constraint:
	 *     (name=ID params+=ID? params+=ID*)
	 */
	protected void sequence_Def(EObject context, Def semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Constraint:
	 *     (vars+=Var* defs+=Def*)
	 */
	protected void sequence_Module(EObject context, Module semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Constraint:
	 *     name=ID
	 */
	protected void sequence_Var(EObject context, Var semanticObject) {
		if(errorAcceptor != null) {
			if(transientValues.isValueTransient(semanticObject, FooPackage.Literals.VAR__NAME) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, FooPackage.Literals.VAR__NAME));
		}
		INodesForEObjectProvider nodes = createNodeProvider(semanticObject);
		SequenceFeeder feeder = createSequencerFeeder(semanticObject, nodes);
		feeder.accept(grammarAccess.getVarAccess().getNameIDTerminalRuleCall_1_0(), semanticObject.getName());
		feeder.finish();
	}
}

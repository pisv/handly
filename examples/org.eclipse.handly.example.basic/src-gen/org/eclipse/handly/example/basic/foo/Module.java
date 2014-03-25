/**
 */
package org.eclipse.handly.example.basic.foo;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Module</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.handly.example.basic.foo.Module#getVars <em>Vars</em>}</li>
 *   <li>{@link org.eclipse.handly.example.basic.foo.Module#getDefs <em>Defs</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.handly.example.basic.foo.FooPackage#getModule()
 * @model
 * @generated
 */
public interface Module extends EObject
{
  /**
   * Returns the value of the '<em><b>Vars</b></em>' containment reference list.
   * The list contents are of type {@link org.eclipse.handly.example.basic.foo.Var}.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Vars</em>' containment reference list isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Vars</em>' containment reference list.
   * @see org.eclipse.handly.example.basic.foo.FooPackage#getModule_Vars()
   * @model containment="true"
   * @generated
   */
  EList<Var> getVars();

  /**
   * Returns the value of the '<em><b>Defs</b></em>' containment reference list.
   * The list contents are of type {@link org.eclipse.handly.example.basic.foo.Def}.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Defs</em>' containment reference list isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Defs</em>' containment reference list.
   * @see org.eclipse.handly.example.basic.foo.FooPackage#getModule_Defs()
   * @model containment="true"
   * @generated
   */
  EList<Def> getDefs();

} // Module

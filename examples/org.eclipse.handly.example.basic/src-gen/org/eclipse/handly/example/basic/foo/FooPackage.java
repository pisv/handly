/**
 */
package org.eclipse.handly.example.basic.foo;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see org.eclipse.handly.example.basic.foo.FooFactory
 * @model kind="package"
 * @generated
 */
public interface FooPackage extends EPackage
{
  /**
   * The package name.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  String eNAME = "foo";

  /**
   * The package namespace URI.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  String eNS_URI = "http://www.eclipse.org/handly/example/basic/Foo";

  /**
   * The package namespace name.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  String eNS_PREFIX = "foo";

  /**
   * The singleton instance of the package.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  FooPackage eINSTANCE = org.eclipse.handly.example.basic.foo.impl.FooPackageImpl.init();

  /**
   * The meta object id for the '{@link org.eclipse.handly.example.basic.foo.impl.ModuleImpl <em>Module</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.eclipse.handly.example.basic.foo.impl.ModuleImpl
   * @see org.eclipse.handly.example.basic.foo.impl.FooPackageImpl#getModule()
   * @generated
   */
  int MODULE = 0;

  /**
   * The feature id for the '<em><b>Vars</b></em>' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int MODULE__VARS = 0;

  /**
   * The feature id for the '<em><b>Defs</b></em>' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int MODULE__DEFS = 1;

  /**
   * The number of structural features of the '<em>Module</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int MODULE_FEATURE_COUNT = 2;

  /**
   * The meta object id for the '{@link org.eclipse.handly.example.basic.foo.impl.VarImpl <em>Var</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.eclipse.handly.example.basic.foo.impl.VarImpl
   * @see org.eclipse.handly.example.basic.foo.impl.FooPackageImpl#getVar()
   * @generated
   */
  int VAR = 1;

  /**
   * The feature id for the '<em><b>Name</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int VAR__NAME = 0;

  /**
   * The number of structural features of the '<em>Var</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int VAR_FEATURE_COUNT = 1;

  /**
   * The meta object id for the '{@link org.eclipse.handly.example.basic.foo.impl.DefImpl <em>Def</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.eclipse.handly.example.basic.foo.impl.DefImpl
   * @see org.eclipse.handly.example.basic.foo.impl.FooPackageImpl#getDef()
   * @generated
   */
  int DEF = 2;

  /**
   * The feature id for the '<em><b>Name</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int DEF__NAME = 0;

  /**
   * The feature id for the '<em><b>Params</b></em>' attribute list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int DEF__PARAMS = 1;

  /**
   * The number of structural features of the '<em>Def</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int DEF_FEATURE_COUNT = 2;


  /**
   * Returns the meta object for class '{@link org.eclipse.handly.example.basic.foo.Module <em>Module</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Module</em>'.
   * @see org.eclipse.handly.example.basic.foo.Module
   * @generated
   */
  EClass getModule();

  /**
   * Returns the meta object for the containment reference list '{@link org.eclipse.handly.example.basic.foo.Module#getVars <em>Vars</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference list '<em>Vars</em>'.
   * @see org.eclipse.handly.example.basic.foo.Module#getVars()
   * @see #getModule()
   * @generated
   */
  EReference getModule_Vars();

  /**
   * Returns the meta object for the containment reference list '{@link org.eclipse.handly.example.basic.foo.Module#getDefs <em>Defs</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference list '<em>Defs</em>'.
   * @see org.eclipse.handly.example.basic.foo.Module#getDefs()
   * @see #getModule()
   * @generated
   */
  EReference getModule_Defs();

  /**
   * Returns the meta object for class '{@link org.eclipse.handly.example.basic.foo.Var <em>Var</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Var</em>'.
   * @see org.eclipse.handly.example.basic.foo.Var
   * @generated
   */
  EClass getVar();

  /**
   * Returns the meta object for the attribute '{@link org.eclipse.handly.example.basic.foo.Var#getName <em>Name</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Name</em>'.
   * @see org.eclipse.handly.example.basic.foo.Var#getName()
   * @see #getVar()
   * @generated
   */
  EAttribute getVar_Name();

  /**
   * Returns the meta object for class '{@link org.eclipse.handly.example.basic.foo.Def <em>Def</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Def</em>'.
   * @see org.eclipse.handly.example.basic.foo.Def
   * @generated
   */
  EClass getDef();

  /**
   * Returns the meta object for the attribute '{@link org.eclipse.handly.example.basic.foo.Def#getName <em>Name</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Name</em>'.
   * @see org.eclipse.handly.example.basic.foo.Def#getName()
   * @see #getDef()
   * @generated
   */
  EAttribute getDef_Name();

  /**
   * Returns the meta object for the attribute list '{@link org.eclipse.handly.example.basic.foo.Def#getParams <em>Params</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute list '<em>Params</em>'.
   * @see org.eclipse.handly.example.basic.foo.Def#getParams()
   * @see #getDef()
   * @generated
   */
  EAttribute getDef_Params();

  /**
   * Returns the factory that creates the instances of the model.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the factory that creates the instances of the model.
   * @generated
   */
  FooFactory getFooFactory();

  /**
   * <!-- begin-user-doc -->
   * Defines literals for the meta objects that represent
   * <ul>
   *   <li>each class,</li>
   *   <li>each feature of each class,</li>
   *   <li>each enum,</li>
   *   <li>and each data type</li>
   * </ul>
   * <!-- end-user-doc -->
   * @generated
   */
  interface Literals
  {
    /**
     * The meta object literal for the '{@link org.eclipse.handly.example.basic.foo.impl.ModuleImpl <em>Module</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.eclipse.handly.example.basic.foo.impl.ModuleImpl
     * @see org.eclipse.handly.example.basic.foo.impl.FooPackageImpl#getModule()
     * @generated
     */
    EClass MODULE = eINSTANCE.getModule();

    /**
     * The meta object literal for the '<em><b>Vars</b></em>' containment reference list feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference MODULE__VARS = eINSTANCE.getModule_Vars();

    /**
     * The meta object literal for the '<em><b>Defs</b></em>' containment reference list feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference MODULE__DEFS = eINSTANCE.getModule_Defs();

    /**
     * The meta object literal for the '{@link org.eclipse.handly.example.basic.foo.impl.VarImpl <em>Var</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.eclipse.handly.example.basic.foo.impl.VarImpl
     * @see org.eclipse.handly.example.basic.foo.impl.FooPackageImpl#getVar()
     * @generated
     */
    EClass VAR = eINSTANCE.getVar();

    /**
     * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute VAR__NAME = eINSTANCE.getVar_Name();

    /**
     * The meta object literal for the '{@link org.eclipse.handly.example.basic.foo.impl.DefImpl <em>Def</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.eclipse.handly.example.basic.foo.impl.DefImpl
     * @see org.eclipse.handly.example.basic.foo.impl.FooPackageImpl#getDef()
     * @generated
     */
    EClass DEF = eINSTANCE.getDef();

    /**
     * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute DEF__NAME = eINSTANCE.getDef_Name();

    /**
     * The meta object literal for the '<em><b>Params</b></em>' attribute list feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute DEF__PARAMS = eINSTANCE.getDef_Params();

  }

} //FooPackage

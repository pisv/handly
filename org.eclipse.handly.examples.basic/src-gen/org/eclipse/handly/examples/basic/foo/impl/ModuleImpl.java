/**
 */
package org.eclipse.handly.examples.basic.foo.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.eclipse.handly.examples.basic.foo.Def;
import org.eclipse.handly.examples.basic.foo.FooPackage;
import org.eclipse.handly.examples.basic.foo.Module;
import org.eclipse.handly.examples.basic.foo.Var;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Module</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.handly.examples.basic.foo.impl.ModuleImpl#getVars <em>Vars</em>}</li>
 *   <li>{@link org.eclipse.handly.examples.basic.foo.impl.ModuleImpl#getDefs <em>Defs</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ModuleImpl extends MinimalEObjectImpl.Container implements Module
{
  /**
   * The cached value of the '{@link #getVars() <em>Vars</em>}' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getVars()
   * @generated
   * @ordered
   */
  protected EList<Var> vars;

  /**
   * The cached value of the '{@link #getDefs() <em>Defs</em>}' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getDefs()
   * @generated
   * @ordered
   */
  protected EList<Def> defs;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected ModuleImpl()
  {
    super();
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  protected EClass eStaticClass()
  {
    return FooPackage.Literals.MODULE;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EList<Var> getVars()
  {
    if (vars == null)
    {
      vars = new EObjectContainmentEList<Var>(Var.class, this, FooPackage.MODULE__VARS);
    }
    return vars;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EList<Def> getDefs()
  {
    if (defs == null)
    {
      defs = new EObjectContainmentEList<Def>(Def.class, this, FooPackage.MODULE__DEFS);
    }
    return defs;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs)
  {
    switch (featureID)
    {
      case FooPackage.MODULE__VARS:
        return ((InternalEList<?>)getVars()).basicRemove(otherEnd, msgs);
      case FooPackage.MODULE__DEFS:
        return ((InternalEList<?>)getDefs()).basicRemove(otherEnd, msgs);
    }
    return super.eInverseRemove(otherEnd, featureID, msgs);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public Object eGet(int featureID, boolean resolve, boolean coreType)
  {
    switch (featureID)
    {
      case FooPackage.MODULE__VARS:
        return getVars();
      case FooPackage.MODULE__DEFS:
        return getDefs();
    }
    return super.eGet(featureID, resolve, coreType);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @SuppressWarnings("unchecked")
  @Override
  public void eSet(int featureID, Object newValue)
  {
    switch (featureID)
    {
      case FooPackage.MODULE__VARS:
        getVars().clear();
        getVars().addAll((Collection<? extends Var>)newValue);
        return;
      case FooPackage.MODULE__DEFS:
        getDefs().clear();
        getDefs().addAll((Collection<? extends Def>)newValue);
        return;
    }
    super.eSet(featureID, newValue);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public void eUnset(int featureID)
  {
    switch (featureID)
    {
      case FooPackage.MODULE__VARS:
        getVars().clear();
        return;
      case FooPackage.MODULE__DEFS:
        getDefs().clear();
        return;
    }
    super.eUnset(featureID);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public boolean eIsSet(int featureID)
  {
    switch (featureID)
    {
      case FooPackage.MODULE__VARS:
        return vars != null && !vars.isEmpty();
      case FooPackage.MODULE__DEFS:
        return defs != null && !defs.isEmpty();
    }
    return super.eIsSet(featureID);
  }

} //ModuleImpl

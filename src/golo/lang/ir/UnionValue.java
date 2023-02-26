package golo.lang.ir;

import golo.compiler.PackageAndClass;

public final class UnionValue extends TypeWithMembers<UnionValue> {

  public UnionValue(String name) {
    super(name);
  }

  protected UnionValue self() {
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PackageAndClass getPackageAndClass() {
    Union u = ancestorOfType(Union.class);
    if (u == null) {
      return PackageAndClass.of(getName());
    }
    return u.getPackageAndClass().createInnerClass(getName());
  }

  public Union getUnion() {
    return ancestorOfType(Union.class);
  }

  protected String getFactoryDelegateName() {
    return getUnion().getPackageAndClass().toString() + "." + getName();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitUnionValue(this);
  }
}

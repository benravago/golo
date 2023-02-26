package golo.lang.ir;

import golo.compiler.PackageAndClass;

public abstract class GoloType<T extends GoloType<T>> extends GoloElement<T> implements NamedElement {

  private final String name;

  GoloType(String name) {
    super();
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  String getFullName() {
    return getPackageAndClass().toString();
  }

  public PackageAndClass getPackageAndClass() {
    GoloModule m = enclosingModule();
    if (m == null) {
      return PackageAndClass.of(getName());
    }
    return m.getTypesPackage().createSubPackage(getName());
  }

}

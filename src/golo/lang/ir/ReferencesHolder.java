package golo.lang.ir;

/**
 * Interface for elements that contains local references.
 */
public interface ReferencesHolder {

  /**
   * Returns the references contained in this element.
   */
  LocalReference[] getReferences();

  /**
   * Returns the number of references contained in this element.
   */
  default int getReferencesCount() {
    return getReferences().length;
  }

  /**
   * Returns only the declaring references.
   */
  default LocalReference[] getDeclaringReferences() {
    return getReferences();
  }
}

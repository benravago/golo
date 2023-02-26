package golo.doc;

public interface DocumentationElement extends Comparable<DocumentationElement> {

  /**
   * The simple name of the element.
   */
  String name();

  /**
   * The documentation comment for the element.
   */
  String documentation();

  /**
   * Chech if this element has a documentation.
   * <p>
   * An element has a documentation if its {@link #documentation()} method returns a non null non empty (when trimmed)
   * string.
   */
  default boolean hasDocumentation() {
    String doc = documentation();
    if (doc == null) {
      return false;
    }
    doc = doc.trim();
    return !doc.isEmpty();
  }

  /**
   * The line where the element is defined.
   */
  int line();

  /**
   * The parent element.
   * <p>
   * For toplevel element, this is the module. For functions, it can be the augmentation. For union values, it is the
   * union.
   */
  DocumentationElement parent();

  /**
   * A readable name for the kind of element.
   */
  String type();

  /**
   * The fully qualified named of the element.
   * <p>
   * Typically, the parent full name and the element name.
   */
  default String fullName() {
    return parent().fullName() + "." + name();
  }

  /**
   * A unique identifier for the element.
   * <p>
   * Can be used in html ID for instance.
   */
  default String id() {
    if (parent() != null && !parent().id().isEmpty()) {
      return parent().id() + '.' + name();
    }
    return name();
  }

  /**
   * A readable representation of the element.
   * <p>
   * Typically the name, but can also contains argument names for functions for instance.
   */
  default String label() {
    return name();
  }

  default int compareTo(DocumentationElement other) {
    if (this == other) {
      return 0;
    }
    if (other == null) {
      return 1;
    }
    int c = label().compareToIgnoreCase(other.label());
    if (c == 0) {
      c = type().compareTo(other.type());
    }
    if (c == 0 && parent() != this) {
      c = parent().compareTo(other.parent());
    }
    return c;
  }
}

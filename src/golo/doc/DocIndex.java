package golo.doc;

import java.util.*;

import golo.lang.FunctionReference;

/**
 * Store the (public) elements defined in order to generate a global index.
 */
class DocIndex implements Iterable<DocumentationElement> {

  private final SortedSet<DocumentationElement> elements = new TreeSet<>();

  public void update(ModuleDocumentation moduleDoc) {
    for (DocumentationElement e : moduleDoc.functions()) {
      elements.add(e);
    }
    for (DocumentationElement e : moduleDoc.macros()) {
      elements.add(e);
    }
    for (StructDocumentation e : moduleDoc.structs()) {
      elements.add(e);
      for (MemberDocumentation m : e.members()) {
        elements.add(m);
      }
    }
    for (UnionDocumentation e : moduleDoc.unions()) {
      elements.add(e);
      for (UnionDocumentation.UnionValueDocumentation v : e.values()) {
        elements.add(v);
        for (MemberDocumentation m : v.members()) {
          elements.add(m);
        }
      }
    }
    for (NamedAugmentationDocumentation e : moduleDoc.namedAugmentations()) {
      elements.add(e);
      for (DocumentationElement f : e.functions()) {
        elements.add(f);
      }
    }
    for (AugmentationDocumentation e : moduleDoc.augmentations()) {
      elements.add(e);
      for (DocumentationElement f : e.functions()) {
        elements.add(f);
      }
    }
  }

  public Map<String, Set<DocumentationElement>> groupBy(FunctionReference f) throws Throwable {
    TreeMap<String, Set<DocumentationElement>> map = new TreeMap<>();
    for (DocumentationElement e : elements) {
      String k = f.invoke(e).toString();
      if (!map.containsKey(k)) {
        map.put(k, new TreeSet<>());
      }
      map.get(k).add(e);
    }
    return map;
  }

  @Override
  public Iterator<DocumentationElement> iterator() {
    return elements.iterator();
  }

}

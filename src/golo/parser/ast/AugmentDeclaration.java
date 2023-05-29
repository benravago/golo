package golo.parser.ast;

import java.util.List;

public class AugmentDeclaration extends golo.parser.Node {
  public String name;
  public String target;
  public List<String> augmentationNames;
  public String documentation;
}
// boolean isNamedAugmentation() {
//   return (augmentationNames != null && !augmentationNames.isEmpty());

package golo.doc;

import java.util.List;
import java.util.Collection;

interface MemberHolder {
  MemberDocumentation addMember(String name);

  List<MemberDocumentation> members();

  MemberHolder members(Collection<MemberDocumentation> m);

  default boolean hasMembers() {
    return !members().isEmpty();
  }
}

package golo.compiler.macro;

import java.lang.invoke.MethodHandle;

import golo.lang.ir.AbstractInvocation;
import golo.lang.ir.GoloElement;

final class MacroFinderResult {
  private final boolean special;
  private final boolean contextual;
  private final MethodHandle target;

  MacroFinderResult(MethodHandle target, boolean special, boolean contextual) {
    this.special = special;
    this.contextual = contextual;
    this.target = target;
  }

  MethodHandle binded(MacroExpansionIrVisitor visitor, AbstractInvocation<?> invocation) {
    MethodHandle handle = this.target;
    if (this.contextual) {
      handle = handle.bindTo(invocation);
    }
    if (this.special) {
      handle = handle.bindTo(visitor);
    }
    if (this.target.isVarargsCollector()) {
      handle = handle.asVarargsCollector(GoloElement[].class);
    }
    return handle;
  }

  @Override
  public String toString() {
    return String.format("MethodFinderResult{special=%s,contextual=%s,target=%s}", special, contextual, target);
  }
}

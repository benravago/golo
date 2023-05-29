package org.golo.jjtree;

import golo.parser.Token;

import java.io.IOException;
import java.io.PrintStream;

/** Token Manager. */
class TokenManager implements ParserConstants {

  interface TokenCompleter {
    void completeToken(Token t);
  }

  TokenCompleter tokenCompleter = null;
  Token lastToken = null;

  void commonTokenAction(Token t) {
    t.previousToken = lastToken;
    if (tokenCompleter != null) {
      tokenCompleter.completeToken(t);
    }
    lastToken = t;
  }

  /** Debug output. */
  PrintStream debugStream = System.out;

  /** Set debug output. */
  void setDebugStream(PrintStream ds) {
    debugStream = ds;
  }

  int stopStringLiteralDfa_0(int pos, long active0, long active1) {
    switch (pos) {
      case 0 -> {
        if ((active0 & 0x800000000000L) != 0L) { matchedKind = 66; return 73; }
        if ((active0 & 0x21100L) != 0L) { matchedKind = 66; return 95; }
        if ((active1 & 0x100000L) != 0L) { matchedKind = 35; return 91; }
        if ((active0 & 0x100000000L) != 0L) { matchedKind = 66; return 64; }
        if ((active0 & 0x80000000000L) != 0L || (active1 & 0x4000L) != 0L) { return 16; }
        if ((active0 & 0x400001000080L) != 0L) { matchedKind = 66; return 67; }
        if ((active0 & 0x4010000000L) != 0L) { matchedKind = 66; return 105; }
        if ((active0 & 0x1000000000400L) != 0L) { matchedKind = 66; return 62; }
        if ((active0 & 0x4000000020c0000L) != 0L) { matchedKind = 66; return 78; }
        if ((active0 & 0xa000202e0f1ea00L) != 0L) { matchedKind = 66; return 168; }
        if ((active0 & 0x8000000000000000L) != 0L) { matchedKind = 73; return 169; }
        if ((active0 & 0x18004000000L) != 0L) { matchedKind = 66; return 14; }
        return -1;
      }
      case 1 -> {
        if ((active0 & 0x10000000L) != 0L) { if (matchedPos != 1) { matchedKind = 66; matchedPos = 1; } return 104; }
        if ((active0 & 0x400001000000L) != 0L) { if (matchedPos != 1) { matchedKind = 66; matchedPos = 1; } return 66; }
        if ((active0 & 0xe018243e6fdef80L) != 0L) { if (matchedPos != 1) { matchedKind = 66; matchedPos = 1; } return 168; }
        if ((active0 & 0x8000000000000000L) != 0L) { if (matchedPos != 1) { matchedKind = 55; matchedPos = 1; } return -1; }
        if ((active0 & 0x18000021000L) != 0L) { return 168; }
        return -1;
      }
      case 2 -> {
        if ((active0 & 0x10000000L) != 0L) { if (matchedPos != 2) { matchedKind = 66; matchedPos = 2; } return 103; }
        if ((active0 & 0xe004103e7f46f80L) != 0L) { if (matchedPos != 2) { matchedKind = 66; matchedPos = 2; } return 168; }
        if ((active0 & 0x8000000000000000L) != 0L) { if (matchedPos < 1) { matchedKind = 55; matchedPos = 1; } return -1; }
        if ((active0 & 0x1824000098000L) != 0L) { return 168; }
        return -1;
      }
      case 3 -> {
        if ((active0 & 0x10000000L) != 0L) { matchedKind = 66; matchedPos = 3; return 102; }
        if ((active0 & 0x8004103c5354f80L) != 0L) { matchedKind = 66; matchedPos = 3; return 168; }
        if ((active0 & 0x600000022c02000L) != 0L) { return 168; }
        return -1;
      }
      case 4 -> {
        if ((active0 & 0x10000000L) != 0L) { matchedKind = 66; matchedPos = 4; return 101; }
        if ((active0 & 0x10184210b80L) != 0L) { matchedKind = 66; matchedPos = 4; return 168; }
        if ((active0 & 0x800400241144400L) != 0L) { return 168; }
        return -1;
      }
      case 5 -> {
        if ((active0 & 0x10084210200L) != 0L) { matchedKind = 66; matchedPos = 5; return 168; }
        if ((active0 & 0x10000000L) != 0L) { matchedKind = 66; matchedPos = 5; return 100; }
        if ((active0 & 0x100000980L) != 0L) { return 168; }
        return -1;
      }
      case 6 -> {
        if ((active0 & 0x10000000L) != 0L) { matchedKind = 27; matchedPos = 6; return 168; }
        if ((active0 & 0x10084000200L) != 0L) { matchedKind = 66; matchedPos = 6; return 168; }
        if ((active0 & 0x210000L) != 0L) { return 168; }
        return -1;
      }
      case 7 -> {
        if ((active0 & 0x14000000L) != 0L) { matchedKind = 66; matchedPos = 7; return 168; }
        if ((active0 & 0x10080000200L) != 0L) { return 168; }
        return -1;
      }
      case 8 -> {
        if ((active0 & 0x10000000L) != 0L) { matchedKind = 66; matchedPos = 8; return 168; }
        if ((active0 & 0x4000000L) != 0L) { return 168; }
        return -1;
      }
      case 9 -> {
        if ((active0 & 0x10000000L) != 0L) { matchedKind = 66; matchedPos = 9; return 168; }
        return -1;
      }
      case 10 -> {
        if ((active0 & 0x10000000L) != 0L) { matchedKind = 66; matchedPos = 10; return 168; }
        return -1;
      }
      default -> { return -1; }
    }
  }

  int startNfa_0(int pos, long active0, long active1) {
    return moveNfa_0(stopStringLiteralDfa_0(pos, active0, active1), pos + 1);
  }

  int stopAtPos(int pos, int kind) {
    matchedKind = kind;
    matchedPos = pos;
    return pos + 1;
  }

  int moveStringLiteralDfa0_0() {
    return switch (curChar) {
      case 10 -> stopAtPos(0, 5);
      case 33 -> startNfaWithStates_0(0, 43, 16);
      case 34 -> moveStringLiteralDfa1_0(0x8000000000000000L, 0x0L);
      case 38 -> stopAtPos(0, 45);
      case 40 -> stopAtPos(0, 80);
      case 41 -> stopAtPos(0, 81);
      case 44 -> stopAtPos(0, 75);
      case 45 -> moveStringLiteralDfa1_0(0x0L, 0x100000L);
      case 46 -> { matchedKind = 74; yield moveStringLiteralDfa1_0(0x0L, 0x241000L); }
      case 61 -> startNfaWithStates_0(0, 78, 16);
      case 64 -> stopAtPos(0, 44);
      case 93 -> stopAtPos(0, 86);
      case 96 -> stopAtPos(0, 6);
      case 97 -> moveStringLiteralDfa1_0(0x4010000000L, 0x0L);
      case 98 -> moveStringLiteralDfa1_0(0x40000000L, 0x0L);
      case 99 -> moveStringLiteralDfa1_0(0x80500000L, 0x0L);
      case 101 -> moveStringLiteralDfa1_0(0x2000L, 0x0L);
      case 102 -> moveStringLiteralDfa1_0(0x800000000218200L, 0x0L);
      case 105 -> moveStringLiteralDfa1_0(0x21100L, 0x0L);
      case 108 -> moveStringLiteralDfa1_0(0x1000000000400L, 0x0L);
      case 109 -> moveStringLiteralDfa1_0(0x400001000080L, 0x0L);
      case 110 -> moveStringLiteralDfa1_0(0x200020000000000L, 0x0L);
      case 111 -> moveStringLiteralDfa1_0(0x18004000000L, 0x0L);
      case 114 -> moveStringLiteralDfa1_0(0x800L, 0x0L);
      case 115 -> moveStringLiteralDfa1_0(0x100000000L, 0x0L);
      case 116 -> moveStringLiteralDfa1_0(0x4000000020c0000L, 0x0L);
      case 117 -> moveStringLiteralDfa1_0(0x200000000L, 0x0L);
      case 118 -> moveStringLiteralDfa1_0(0x800000000000L, 0x0L);
      case 119 -> moveStringLiteralDfa1_0(0x20804000L, 0x0L);
      case 123 -> stopAtPos(0, 79);
      case 124 -> stopAtPos(0, 83);
      case 125 -> stopAtPos(0, 77);
      default -> moveNfa_0(3, 0);
    };
  }

  int moveStringLiteralDfa1_0(long active0, long active1) {
    try {
      curChar = charSource.readChar();
    } catch (IOException e) {
      stopStringLiteralDfa_0(0, active0, active1);
      return 1;
    }
    switch (curChar) {
      case 34 -> { return moveStringLiteralDfa2_0(active0, 0x8000000000000000L, active1, 0L); }
      case 46 -> { if ((active1 & 0x200000L) != 0L) { matchedKind = 85; matchedPos = 1; } return moveStringLiteralDfa2_0(active0, 0L, active1, 0x40000L); }
      case 62 -> { if ((active1 & 0x100000L) != 0L) return stopAtPos(1, 84); }
      case 97 -> { return moveStringLiteralDfa2_0(active0, 0x800c00001500000L, active1, 0L); }
      case 101 -> { return moveStringLiteralDfa2_0(active0, 0x1000000000800L, active1, 0L); }
      case 102 -> { if ((active0 & 0x1000L) != 0L) return startNfaWithStates_0(1, 12, 168); }
      case 104 -> { return moveStringLiteralDfa2_0(active0, 0x2844000L, active1, 0L); }
      case 105 -> { return moveStringLiteralDfa2_0(active0, 0x20200000L, active1, 0L); }
      case 108 -> { return moveStringLiteralDfa2_0(active0, 0x2000L, active1, 0L); }
      case 109 -> { return moveStringLiteralDfa2_0(active0, 0x100L, active1, 0L); }
      case 110 -> { return (active0 & 0x20000L) != 0L ? startNfaWithStates_0(1, 17, 168) : moveStringLiteralDfa2_0(active0, 0x4200000000L, active1, 0L); }
      case 111 -> { return moveStringLiteralDfa2_0(active0, 0x20080018480L, active1, 0L); }
      case 114 -> { if ((active0 & 0x8000000000L) != 0L) { matchedKind = 39; matchedPos = 1; } return moveStringLiteralDfa2_0(active0, 0x400010040080000L, active1, 0L); }
      case 116 -> { return moveStringLiteralDfa2_0(active0, 0x104000000L, active1, 0L); }
      case 117 -> { return moveStringLiteralDfa2_0(active0, 0x200000010000200L, active1, 0L); }
      case 123 -> { if ((active1 & 0x1000L) != 0L) return stopAtPos(1, 76); }
      // default -> {}
    }
    return startNfa_0(0, active0, active1);
  }

  int moveStringLiteralDfa2_0(long old0, long active0, long old1, long active1) {
    if (((active0 &= old0) | (active1 &= old1)) == 0L) {
      return startNfa_0(0, old0, old1);
    }
    try {
      curChar = charSource.readChar();
    } catch (IOException e) {
      stopStringLiteralDfa_0(1, active0, active1);
      return 2;
    }
    switch (curChar) {
      case 34 -> { if ((active0 & 0x8000000000000000L) != 0L) return stopAtPos(2, 63); }
      case 46 -> { if ((active1 & 0x40000L) != 0L) return stopAtPos(2, 82); }
      case 73 -> { return moveStringLiteralDfa3_0(active0, 0x10000000000L, active1, 0L); }
      case 99 -> { return moveStringLiteralDfa3_0(active0, 0x400000000400L, active1, 0L); }
      case 100 -> { return (active0 & 0x4000000000L) != 0L ? startNfaWithStates_0(2, 38, 168) : moveStringLiteralDfa3_0(active0, 0x80L, active1, 0L); }
      case 101 -> { return moveStringLiteralDfa3_0(active0, 0x42800000L, active1, 0L); }
      case 103 -> { return moveStringLiteralDfa3_0(active0, 0x10000000L, active1, 0L); }
      case 104 -> { return moveStringLiteralDfa3_0(active0, 0x4000000L, active1, 0L); }
      case 105 -> { return moveStringLiteralDfa3_0(active0, 0x200004000L, active1, 0L); }
      case 108 -> { return moveStringLiteralDfa3_0(active0, 0xa00000000000000L, active1, 0L); }
      case 110 -> { return moveStringLiteralDfa3_0(active0, 0x80200200L, active1, 0L); }
      case 112 -> { return moveStringLiteralDfa3_0(active0, 0x100L, active1, 0L); }
      case 114 -> { if ((active0 & 0x8000L) != 0L) { matchedKind = 15; matchedPos = 2; } else if ((active0 & 0x800000000000L) != 0L) { return startNfaWithStates_0(2, 47, 168); } return moveStringLiteralDfa3_0(active0, 0x100050000L, active1, 0L); }
      case 115 -> { return moveStringLiteralDfa3_0(active0, 0x402000L, active1, 0L); }
      case 116 -> { return (active0 & 0x20000000000L) != 0L ? startNfaWithStates_0(2, 41, 168) : (active0 & 0x1000000000000L) != 0L ? startNfaWithStates_0(2, 48, 168) : moveStringLiteralDfa3_0(active0, 0x21100800L, active1, 0L); }
      case 117 -> { return moveStringLiteralDfa3_0(active0, 0x400000000000000L, active1, 0L); }
      case 121 -> { if ((active0 & 0x80000L) != 0L) return startNfaWithStates_0(2, 19, 168); }
      // default -> {}
    }
    return startNfa_0(1, active0, active1);
  }

  int moveStringLiteralDfa3_0(long old0, long active0, long old1, long active1) {
    if (((active0 &= old0) | (active1 &= old1)) == 0L) {
      return startNfa_0(1, old0, old1);
    }
    try {
      curChar = charSource.readChar();
    } catch (IOException e) {
      stopStringLiteralDfa_0(2, active0, 0L);
      return 3;
    }
    switch (curChar) {
      case 97 -> { return moveStringLiteralDfa4_0(active0, 0x40200400L); }
      case 99 -> { return moveStringLiteralDfa4_0(active0, 0x1100200L); }
      case 101 -> { return (active0 & 0x2000L) != 0L ? startNfaWithStates_0(3, 13, 168) : (active0 & 0x400000L) != 0L ? startNfaWithStates_0(3, 22, 168) : (active0 & 0x400000000000000L) != 0L ? startNfaWithStates_0(3, 58, 168) : moveStringLiteralDfa4_0(active0, 0x4010000L); }
      case 102 -> { return moveStringLiteralDfa4_0(active0, 0x10000000000L); }
      case 104 -> { if ((active0 & 0x20000000L) != 0L) return startNfaWithStates_0(3, 29, 168); }
      case 108 -> { return (active0 & 0x200000000000000L) != 0L ? startNfaWithStates_0(3, 57, 168) : moveStringLiteralDfa4_0(active0, 0x4000L); }
      case 109 -> { return moveStringLiteralDfa4_0(active0, 0x10000000L); }
      case 110 -> { if ((active0 & 0x800000L) != 0L) return startNfaWithStates_0(3, 23, 168); else if ((active0 & 0x2000000L) != 0L) return startNfaWithStates_0(3, 25, 168); }
      case 111 -> { return moveStringLiteralDfa4_0(active0, 0x200040100L); }
      case 114 -> { return moveStringLiteralDfa4_0(active0, 0x400000000000L); }
      case 115 -> { return moveStringLiteralDfa4_0(active0, 0x800000000000000L); }
      case 116 -> { return moveStringLiteralDfa4_0(active0, 0x80000000L); }
      case 117 -> { return moveStringLiteralDfa4_0(active0, 0x100000880L); }
      // default -> {}
    }
    return startNfa_0(2, active0, 0L);
  }

  int moveStringLiteralDfa4_0(long old0, long active0) {
    if (((active0 &= old0)) == 0L) {
      return startNfa_0(2, old0, 0L);
    }
    try {
      curChar = charSource.readChar();
    } catch (IOException e) {
      stopStringLiteralDfa_0(3, active0, 0L);
      return 4;
    }
    switch (curChar) {
      case 78 -> { return moveStringLiteralDfa5_0(active0, 0x10000000000L); }
      case 97 -> { return moveStringLiteralDfa5_0(active0, 0x10000L); }
      case 99 -> { return moveStringLiteralDfa5_0(active0, 0x100000000L); }
      case 101 -> { return (active0 & 0x4000L) != 0L ? startNfaWithStates_0(4, 14, 168) : (active0 & 0x800000000000000L) != 0L ? startNfaWithStates_0(4, 59, 168) : moveStringLiteralDfa5_0(active0, 0x10000000L); }
      case 104 -> { if ((active0 & 0x100000L) != 0L) return startNfaWithStates_0(4, 20, 168); else if ((active0 & 0x1000000L) != 0L) return startNfaWithStates_0(4, 24, 168); }
      case 105 -> { return moveStringLiteralDfa5_0(active0, 0x80000000L); }
      case 107 -> { if ((active0 & 0x40000000L) != 0L) return startNfaWithStates_0(4, 30, 168); }
      case 108 -> { return (active0 & 0x400L) != 0L ? startNfaWithStates_0(4, 10, 168) : moveStringLiteralDfa5_0(active0, 0x200080L); }
      case 110 -> { if ((active0 & 0x200000000L) != 0L) return startNfaWithStates_0(4, 33, 168); }
      case 111 -> { if ((active0 & 0x400000000000L) != 0L) return startNfaWithStates_0(4, 46, 168); }
      case 114 -> { return moveStringLiteralDfa5_0(active0, 0x4000900L); }
      case 116 -> { return moveStringLiteralDfa5_0(active0, 0x200L); }
      case 119 -> { if ((active0 & 0x40000L) != 0L) return startNfaWithStates_0(4, 18, 168); }
      // default -> {}
    }
    return startNfa_0(3, active0, 0L);
  }

  int moveStringLiteralDfa5_0(long old0, long active0) {
    if (((active0 &= old0)) == 0L) {
      return startNfa_0(3, old0, 0L);
    }
    try {
      curChar = charSource.readChar();
    } catch (IOException e) {
      stopStringLiteralDfa_0(4, active0, 0L);
      return 5;
    }
    switch (curChar) {
      case 99 -> { return moveStringLiteralDfa6_0(active0, 0x10000L); }
      case 101 -> { if ((active0 & 0x80L) != 0L) return startNfaWithStates_0(5, 7, 168); }
      case 105 -> { return moveStringLiteralDfa6_0(active0, 0x200L); }
      case 108 -> { return moveStringLiteralDfa6_0(active0, 0x200000L); }
      case 110 -> { return (active0 & 0x800L) != 0L ? startNfaWithStates_0(5, 11, 168) : moveStringLiteralDfa6_0(active0, 0x90000000L); }
      case 116 -> { if ((active0 & 0x100L) != 0L) return startNfaWithStates_0(5, 8, 168); else if ((active0 & 0x100000000L) != 0L) return startNfaWithStates_0(5, 32, 168); }
      case 117 -> { return moveStringLiteralDfa6_0(active0, 0x10000000000L); }
      case 119 -> { return moveStringLiteralDfa6_0(active0, 0x4000000L); }
      // default -> {}
    }
    return startNfa_0(4, active0, 0L);
  }

  int moveStringLiteralDfa6_0(long old0, long active0) {
    if (((active0 &= old0)) == 0L) {
      return startNfa_0(4, old0, 0L);
    }
    try {
      curChar = charSource.readChar();
    } catch (IOException e) {
      stopStringLiteralDfa_0(5, active0, 0L);
      return 6;
    }
    switch (curChar) {
      case 104 -> { if ((active0 & 0x10000L) != 0L) return startNfaWithStates_0(6, 16, 168); }
      case 105 -> { return moveStringLiteralDfa7_0(active0, 0x4000000L); }
      case 108 -> { return moveStringLiteralDfa7_0(active0, 0x10000000000L); }
      case 111 -> { return moveStringLiteralDfa7_0(active0, 0x200L); }
      case 116 -> { return moveStringLiteralDfa7_0(active0, 0x10000000L); }
      case 117 -> { return moveStringLiteralDfa7_0(active0, 0x80000000L); }
      case 121 -> { if ((active0 & 0x200000L) != 0L) return startNfaWithStates_0(6, 21, 168); }
      // default -> {}
    }
    return startNfa_0(5, active0, 0L);
  }

  int moveStringLiteralDfa7_0(long old0, long active0) {
    if (((active0 &= old0)) == 0L) {
      return startNfa_0(5, old0, 0L);
    }
    try {
      curChar = charSource.readChar();
    } catch (IOException e) {
      stopStringLiteralDfa_0(6, active0, 0L);
      return 7;
    }
    switch (curChar) {
      case 97 -> { return moveStringLiteralDfa8_0(active0, 0x10000000L); }
      case 101 -> { if ((active0 & 0x80000000L) != 0L) return startNfaWithStates_0(7, 31, 168); }
      case 108 -> { if ((active0 & 0x10000000000L) != 0L) return startNfaWithStates_0(7, 40, 168); }
      case 110 -> { if ((active0 & 0x200L) != 0L) return startNfaWithStates_0(7, 9, 168); }
      case 115 -> { return moveStringLiteralDfa8_0(active0, 0x4000000L); }
      // default -> {}
    }
    return startNfa_0(6, active0, 0L);
  }

  int moveStringLiteralDfa8_0(long old0, long active0) {
    if (((active0 &= old0)) == 0L) {
      return startNfa_0(6, old0, 0L);
    }
    try {
      curChar = charSource.readChar();
    } catch (IOException e) {
      stopStringLiteralDfa_0(7, active0, 0L);
      return 8;
    }
    switch (curChar) {
      case 101 -> { if ((active0 & 0x4000000L) != 0L) return startNfaWithStates_0(8, 26, 168); }
      case 116 -> { return moveStringLiteralDfa9_0(active0, 0x10000000L); }
      // default -> {}
    }
    return startNfa_0(7, active0, 0L);
  }

  int moveStringLiteralDfa9_0(long old0, long active0) {
    if (((active0 &= old0)) == 0L) {
      return startNfa_0(7, old0, 0L);
    }
    try {
      curChar = charSource.readChar();
    } catch (IOException e) {
      stopStringLiteralDfa_0(8, active0, 0L);
      return 9;
    }
    switch (curChar) {
      case 105 -> { return moveStringLiteralDfa10_0(active0, 0x10000000L); }
      // default -> {}
    }
    return startNfa_0(8, active0, 0L);
  }

  int moveStringLiteralDfa10_0(long old0, long active0) {
    if (((active0 &= old0)) == 0L) {
      return startNfa_0(8, old0, 0L);
    }
    try {
      curChar = charSource.readChar();
    } catch (IOException e) {
      stopStringLiteralDfa_0(9, active0, 0L);
      return 10;
    }
    switch (curChar) {
      case 111 -> { return moveStringLiteralDfa11_0(active0, 0x10000000L); }
      // default -> {}
    }
    return startNfa_0(9, active0, 0L);
  }

  int moveStringLiteralDfa11_0(long old0, long active0) {
    if (((active0 &= old0)) == 0L) {
      return startNfa_0(9, old0, 0L);
    }
    try {
      curChar = charSource.readChar();
    } catch (IOException e) {
      stopStringLiteralDfa_0(10, active0, 0L);
      return 11;
    }
    switch (curChar) {
      case 110 -> { if ((active0 & 0x10000000L) != 0L) return startNfaWithStates_0(11, 28, 168); }
      // default -> {}
    }
    return startNfa_0(10, active0, 0L);
  }

  int startNfaWithStates_0(int pos, int kind, int state) {
    matchedKind = kind;
    matchedPos = pos;
    try {
      curChar = charSource.readChar();
    } catch (IOException e) {
      return pos + 1;
    }
    return moveNfa_0(state, pos + 1);
  }

  static final long[]
    bitVec0 = { 0xfffffffffffffffeL, 0xffffffffffffffffL, 0xffffffffffffffffL, 0xffffffffffffffffL },
    bitVec2 = { 0x0L, 0x0L, 0xffffffffffffffffL, 0xffffffffffffffffL },
    bitVec3 = { 0x0L, 0x0L, 0x423567c00000000L, 0x7fffffffffffffffL };

  int moveNfa_0(int startState, int curPos) {
    var startsAt = 0;
    newStateCount = 168;
    var i = 1;
    stateSet[0] = startState;
    var kind = 0x7fffffff;
    for (;;) {
      if (++round == 0x7fffffff) {
        resetRounds();
      }
      if (curChar < 64) {
        var l = 1L << curChar;
        do {
          switch (stateSet[--i]) {
            case 3 -> {
              if ((0xdbff8cfeffffcdffL & l) != 0L) { if (kind > 73) kind = 73; }
                else if (curChar == 45) { checkNAddStates(0, 5); }
                else if (curChar == 58) { if (kind > 42) kind = 42; }
                else if (curChar == 61) { checkNAdd(16); }
              if ((0x3ff000000000000L & l) != 0L) { if (kind > 49) kind = 49; checkNAddStates(6, 27); }
                else if ((0x842000000000L & l) != 0L) { if (kind > 34) kind = 34; }
                else if ((0x5000000000000000L & l) != 0L) { if (kind > 36) kind = 36; }
                else if ((0x280000000000L & l) != 0L) { if (kind > 35) kind = 35; }
                else if (curChar == 36) { if (kind > 66) kind = 66; checkNAddStates(28, 31); }
                else if (curChar == 35) { if (kind > 69) kind = 69; checkNAddStates(32, 34); }
                else if (curChar == 39) { addStates(35, 37); }
                else if (curChar == 34) { checkNAddStates(38, 41); }
                else if (curChar == 33) { checkNAdd(16); }
                else if (curChar == 63) { stateSet[newStateCount++] = 20; }
              if (curChar == 45) { stateSet[newStateCount++] = 91; }
                else if (curChar == 62) { checkNAdd(7); }
                else if (curChar == 60) { checkNAdd(7); }
            }
            case 73 -> {
              if ((0x3ff001000000000L & l) != 0L) { if (kind > 66) kind = 66; checkNAdd(167); }
                else if (curChar == 46) { addStates(42, 43); }
              if ((0x3ff001000000000L & l) != 0L) { checkNAddStates(44, 46); }
                else if (curChar == 46) { stateSet[newStateCount++] = 153; }
            }
            case 105 -> {
              if ((0x3ff001000000000L & l) != 0L) { if (kind > 66) kind = 66; checkNAdd(167); }
                else if (curChar == 46) { addStates(42, 43); }
              if ((0x3ff001000000000L & l) != 0L) { checkNAddStates(44, 46); }
                else if (curChar == 46) { stateSet[newStateCount++] = 153; }
            }
            case 14 -> {
              if ((0x3ff001000000000L & l) != 0L) { if (kind > 66) kind = 66; checkNAdd(167); }
                else if (curChar == 46) { addStates(42, 43); }
              if ((0x3ff001000000000L & l) != 0L) { checkNAddStates(44, 46); }
                else if (curChar == 46) { stateSet[newStateCount++] = 153; }
            }
            case 169 -> {
              if ((0xfffffffbffffdbffL & l) != 0L) { checkNAddStates(38, 41); }
                else if (curChar == 34) { if (kind > 55) kind = 55; }
            }
            case 168 -> {
              if ((0x3ff001000000000L & l) != 0L) { if (kind > 66) kind = 66; checkNAdd(167); }
                else if (curChar == 46) { addStates(42, 43); }
              if ((0x3ff001000000000L & l) != 0L) { checkNAddStates(44, 46); }
                else if (curChar == 46) { stateSet[newStateCount++] = 153; }
            }
            case 78 -> {
              if ((0x3ff001000000000L & l) != 0L) { if (kind > 66) kind = 66; checkNAdd(167); }
                else if (curChar == 46) { addStates(42, 43); }
              if ((0x3ff001000000000L & l) != 0L) { checkNAddStates(44, 46); }
                else if (curChar == 46) { stateSet[newStateCount++] = 153; }
            }
            case 91 -> {
              if ((0x3ff000000000000L & l) != 0L) { checkNAddStates(47, 51); }
                else if (curChar == 45) { stateSet[newStateCount++] = 90; }
              if ((0x3ff000000000000L & l) != 0L) { checkNAddStates(52, 56); }
              if ((0x3ff000000000000L & l) != 0L) { if (kind > 52) kind = 52; checkNAddStates(57, 60); }
              if ((0x3ff000000000000L & l) != 0L) { checkNAddStates(61, 63); }
              if ((0x3ff000000000000L & l) != 0L) { checkNAddStates(64, 66); }
              if ((0x3ff000000000000L & l) != 0L) { if (kind > 49) kind = 49; checkNAddTwoStates(112, 111); }
            }
            case 66 -> {
              if ((0x3ff001000000000L & l) != 0L) { if (kind > 66) kind = 66; checkNAdd(167); }
                else if (curChar == 46) { addStates(42, 43); }
              if ((0x3ff001000000000L & l) != 0L) { checkNAddStates(44, 46); }
                else if (curChar == 46) { stateSet[newStateCount++] = 153; }
            }
            case 100 -> {
              if ((0x3ff001000000000L & l) != 0L) { if (kind > 66) kind = 66; checkNAdd(167); }
                else if (curChar == 46) { addStates(42, 43); }
              if ((0x3ff001000000000L & l) != 0L) { checkNAddStates(44, 46); }
                else if (curChar == 46) { stateSet[newStateCount++] = 153; }
            }
            case 67 -> {
              if ((0x3ff001000000000L & l) != 0L) { if (kind > 66) kind = 66; checkNAdd(167); }
                else if (curChar == 46) { addStates(42, 43); }
              if ((0x3ff001000000000L & l) != 0L) { checkNAddStates(44, 46); }
                else if (curChar == 46) { stateSet[newStateCount++] = 153; }
            }
            case 101 -> {
              if ((0x3ff001000000000L & l) != 0L) { if (kind > 66) kind = 66; checkNAdd(167); }
                else if (curChar == 46) { addStates(42, 43); }
              if ((0x3ff001000000000L & l) != 0L) { checkNAddStates(44, 46); }
                else if (curChar == 46) { stateSet[newStateCount++] = 153; }
            }
            case 102 -> {
              if ((0x3ff001000000000L & l) != 0L) { if (kind > 66) kind = 66; checkNAdd(167); }
                else if (curChar == 46) { addStates(42, 43); }
              if ((0x3ff001000000000L & l) != 0L) { checkNAddStates(44, 46); }
                else if (curChar == 46) { stateSet[newStateCount++] = 153; }
            }
            case 103 -> {
              if ((0x3ff001000000000L & l) != 0L) { if (kind > 66) kind = 66; checkNAdd(167); }
                else if (curChar == 46) { addStates(42, 43); }
              if ((0x3ff001000000000L & l) != 0L) { checkNAddStates(44, 46); }
                else if (curChar == 46) { stateSet[newStateCount++] = 153; }
            }
            case 104 -> {
              if ((0x3ff001000000000L & l) != 0L) { if (kind > 66) kind = 66; checkNAdd(167); }
                else if (curChar == 46) { addStates(42, 43); }
              if ((0x3ff001000000000L & l) != 0L) { checkNAddStates(44, 46); }
                else if (curChar == 46) { stateSet[newStateCount++] = 153; }
            }
            case 62 -> {
              if ((0x3ff001000000000L & l) != 0L) { if (kind > 66) kind = 66; checkNAdd(167); }
                else if (curChar == 46) { addStates(42, 43); }
              if ((0x3ff001000000000L & l) != 0L) { checkNAddStates(44, 46); }
                else if (curChar == 46) { stateSet[newStateCount++] = 153; }
            }
            case 95 -> {
              if ((0x3ff001000000000L & l) != 0L) { if (kind > 66) kind = 66; checkNAdd(167); }
                else if (curChar == 46) { addStates(42, 43); }
              if ((0x3ff001000000000L & l) != 0L) { checkNAddStates(44, 46); }
                else if (curChar == 46) { stateSet[newStateCount++] = 153; }
            }
            case 64 -> {
              if ((0x3ff001000000000L & l) != 0L) { if (kind > 66) kind = 66; checkNAdd(167); }
                else if (curChar == 46) { addStates(42, 43); }
              if ((0x3ff001000000000L & l) != 0L) { checkNAddStates(44, 46); }
                else if (curChar == 46) { stateSet[newStateCount++] = 153; }
            }
            case 4 -> { if ((0x842000000000L & l) != 0L && kind > 34) kind = 34; }
            case 5 -> { if ((0x280000000000L & l) != 0L && kind > 35) kind = 35; }
            case 6 -> { if ((0x5000000000000000L & l) != 0L && kind > 36) kind = 36; }
            case 7 -> { if (curChar == 61 && kind > 36) kind = 36; }
            case 8 -> { if (curChar == 60) checkNAdd(7); }
            case 9 -> { if (curChar == 62) checkNAdd(7); }
            case 16 -> { if (curChar == 61 && kind > 37) kind = 37; }
            case 17 -> { if (curChar == 61) checkNAdd(16); }
            case 18 -> { if (curChar == 33) checkNAdd(16); }
            case 19, 20 -> { if (curChar == 58 && kind > 42) kind = 42; }
            case 21 -> { if (curChar == 63) stateSet[newStateCount++] = 20; }
            case 22 -> { if (curChar == 34) checkNAddStates(38, 41); }
            case 23 -> { if ((0xfffffffbffffdbffL & l) != 0L) checkNAddStates(38, 41); }
            case 25 -> { if ((0x3ff000000000000L & l) != 0L) stateSet[newStateCount++] = 26; }
            case 26 -> { if ((0x3ff000000000000L & l) != 0L) stateSet[newStateCount++] = 27; }
            case 27 -> { if ((0x3ff000000000000L & l) != 0L) stateSet[newStateCount++] = 28; }
            case 28 -> { if ((0x3ff000000000000L & l) != 0L) checkNAddStates(38, 41); }
            case 30 -> { if ((0x8400000000L & l) != 0L) checkNAddStates(38, 41); }
            case 31 -> { if (curChar == 34 && kind > 55) kind = 55; }
            case 32 -> { if (curChar == 39) addStates(35, 37); }
            case 33 -> { if ((0xffffff7fffffdbffL & l) != 0L) checkNAdd(34); }
            case 34 -> { if (curChar == 39 && kind > 56) kind = 56; }
            case 36 -> { if ((0x3ff000000000000L & l) != 0L) stateSet[newStateCount++] = 37; }
            case 37 -> { if ((0x3ff000000000000L & l) != 0L) stateSet[newStateCount++] = 38; }
            case 38 -> { if ((0x3ff000000000000L & l) != 0L) stateSet[newStateCount++] = 39; }
            case 39 -> { if ((0x3ff000000000000L & l) != 0L) checkNAdd(34); }
            case 41 -> { if ((0x8400000000L & l) != 0L) checkNAdd(34); }
            case 43 -> { if (curChar != 36) break; if (kind > 61) kind = 61; checkNAddStates(67, 71); }
            case 44 -> { if ((0x3ff001000000000L & l) != 0L) checkNAddStates(72, 74); }
            case 45 -> { if (curChar == 46) stateSet[newStateCount++] = 46; }
            case 46 -> { if (curChar == 36) checkNAddStates(75, 77); }
            case 47 -> { if ((0x3ff001000000000L & l) != 0L) checkNAddStates(75, 77); }
            case 48 -> { if (curChar == 58) stateSet[newStateCount++] = 49; }
            case 49 -> { if (curChar != 36) break; if (kind > 61) kind = 61; checkNAddTwoStates(50, 51); }
            case 50 -> { if ((0x3ff001000000000L & l) == 0L) break; if (kind > 61) kind = 61; checkNAddTwoStates(50, 51); }
            case 52 -> { if (curChar == 45) checkNAdd(53); }
            case 53 -> { if ((0x3ff000000000000L & l) == 0L) break; if (kind > 61) kind = 61; checkNAddStates(78, 80); }
            case 55 -> { if (curChar == 46 && kind > 61) kind = 61; }
            case 56 -> { if (curChar == 46) stateSet[newStateCount++] = 55; }
            case 57 -> { if (curChar == 46) stateSet[newStateCount++] = 56; }
            case 58 -> { if (curChar == 58) stateSet[newStateCount++] = 48; }
            case 80 -> { if (curChar != 35) break; if (kind > 69) kind = 69; checkNAddStates(32, 34); }
            case 81 -> { if ((0xffffffffffffdbffL & l) == 0L) break; if (kind > 69) kind = 69; checkNAddStates(32, 34); }
            case 82 -> { if ((0x2400L & l) != 0L && kind > 69) kind = 69; }
            case 83 -> { if (curChar == 10 && kind > 69) kind = 69; }
            case 84 -> { if (curChar == 13) stateSet[newStateCount++] = 83; }
            case 85 -> { if (curChar == 45) checkNAddStates(81, 83); }
            case 86 -> { if ((0x100000200L & l) != 0L) checkNAddStates(81, 83); }
            case 87 -> { if ((0x2400L & l) != 0L && kind > 70) kind = 70; }
            case 88 -> { if (curChar == 10 && kind > 70) kind = 70; }
            case 89 -> { if (curChar == 13) stateSet[newStateCount++] = 88; }
            case 90 -> { if (curChar == 45) stateSet[newStateCount++] = 85; }
            case 92 -> { if (curChar == 45) stateSet[newStateCount++] = 91; }
            case 93 -> { if ((0xdbff8cfeffffcdffL & l) != 0L && kind > 73) kind = 73; }
            case 110 -> { if (curChar == 45) checkNAddStates(0, 5); }
            case 111 -> { if ((0x3ff000000000000L & l) == 0L) break; if (kind > 49) kind = 49; checkNAddTwoStates(112, 111); }
            case 113 -> { if ((0x3ff000000000000L & l) != 0L) checkNAddStates(64, 66); }
            case 117 -> { if ((0x3ff000000000000L & l) != 0L) checkNAddStates(61, 63); }
            case 121 -> { if ((0x3ff000000000000L & l) == 0L) break; if (kind > 52) kind = 52; checkNAddStates(57, 60); }
            case 123 -> { if (curChar == 46) checkNAdd(124); }
            case 124 -> { if ((0x3ff000000000000L & l) == 0L) break; if (kind > 52) kind = 52; checkNAddStates(84, 86); }
            case 127 -> { if (curChar == 45) checkNAdd(128); }
            case 128 -> { if ((0x3ff000000000000L & l) == 0L) break; if (kind > 52) kind = 52; checkNAdd(128); }
            case 129 -> { if ((0x3ff000000000000L & l) != 0L) checkNAddStates(52, 56); }
            case 131 -> { if (curChar == 46) checkNAdd(132); }
            case 132 -> { if ((0x3ff000000000000L & l) != 0L) checkNAddStates(87, 90); }
            case 135 -> { if (curChar == 45) checkNAdd(136); }
            case 136 -> { if ((0x3ff000000000000L & l) != 0L) checkNAddTwoStates(136, 138); }
            case 139 -> { if ((0x3ff000000000000L & l) != 0L) checkNAddStates(47, 51); }
            case 141 -> { if (curChar == 46) checkNAdd(142); }
            case 142 -> { if ((0x3ff000000000000L & l) != 0L) checkNAddStates(91, 94); }
            case 145 -> { if (curChar == 45) checkNAdd(146); }
            case 146 -> { if ((0x3ff000000000000L & l) != 0L) checkNAddTwoStates(146, 148); }
            case 149 -> { if ((0x3ff000000000000L & l) == 0L) break; if (kind > 49) kind = 49; checkNAddStates(6, 27); }
            case 150 -> { if (curChar != 36) break; if (kind > 66) kind = 66; checkNAddStates(28, 31); }
            case 151 -> { if ((0x3ff001000000000L & l) != 0L) checkNAddStates(44, 46); }
            case 152 -> { if (curChar == 46) stateSet[newStateCount++] = 153; }
            case 153 -> { if (curChar == 36) checkNAddStates(95, 97); }
            case 154 -> { if ((0x3ff001000000000L & l) != 0L) checkNAddStates(95, 97); }
            case 155 -> { if (curChar == 46) addStates(42, 43); }
            case 167 -> { if ((0x3ff001000000000L & l) == 0L) break; if (kind > 66) kind = 66; checkNAdd(167); }
            // default -> {}
          }
        } while (i != startsAt);
      } else if (curChar < 128) {
        var l = 1L << (curChar & 077);
        do {
          switch (stateSet[--i]) {
            case 3 -> {
              if ((0xc7fffffedfffffffL & l) != 0L) { if (kind > 73) kind = 73; }
              if ((0x7fffffe87fffffeL & l) != 0L) { if (kind > 66) kind = 66; checkNAddStates(28, 31); }
                else if (curChar == 91) { if (kind > 62) kind = 62; }
                else if (curChar == 94) { stateSet[newStateCount++] = 43; }
              if (curChar == 97) { addStates(98, 99); }
                else if (curChar == 105) { addStates(100, 101); }
                else if (curChar == 116) { stateSet[newStateCount++] = 78; }
                else if (curChar == 118) { stateSet[newStateCount++] = 73; }
                else if (curChar == 109) { stateSet[newStateCount++] = 67; }
                else if (curChar == 115) { stateSet[newStateCount++] = 64; }
                else if (curChar == 108) { stateSet[newStateCount++] = 62; }
                else if (curChar == 111) { stateSet[newStateCount++] = 14; }
                else if (curChar == 112) { stateSet[newStateCount++] = 2; }
            }
            case 73 -> {
              if ((0x7fffffe87fffffeL & l) != 0L) { if (kind > 66) kind = 66; checkNAdd(167); }
              if ((0x7fffffe87fffffeL & l) != 0L) { checkNAddStates(44, 46); }
              if (curChar == 101) { stateSet[newStateCount++] = 72; }
            }
            case 105 -> {
              if ((0x7fffffe87fffffeL & l) != 0L) { if (kind > 66) kind = 66; checkNAdd(167); }
              if ((0x7fffffe87fffffeL & l) != 0L) { checkNAddStates(44, 46); }
              if (curChar == 114) { stateSet[newStateCount++] = 108; }
                else if (curChar == 117) { stateSet[newStateCount++] = 104; }
            }
            case 14 -> {
              if ((0x7fffffe87fffffeL & l) != 0L) { if (kind > 66) kind = 66; checkNAdd(167); }
              if ((0x7fffffe87fffffeL & l) != 0L) { checkNAddStates(44, 46); }
              if (curChar == 102) { stateSet[newStateCount++] = 13; }
            }
            case 169 -> {
              if ((0xffffffffefffffffL & l) != 0L) { checkNAddStates(38, 41); }
                else if (curChar == 92) { stateSet[newStateCount++] = 30; }
              if (curChar == 117) { stateSet[newStateCount++] = 25; }
            }
            case 168 -> {
              if ((0x7fffffe87fffffeL & l) != 0L) { if (kind > 66) kind = 66; checkNAdd(167); }
              if ((0x7fffffe87fffffeL & l) != 0L) { checkNAddStates(44, 46); }
            }
            case 78 -> {
              if ((0x7fffffe87fffffeL & l) != 0L) { if (kind > 66) kind = 66; checkNAdd(167); }
              if ((0x7fffffe87fffffeL & l) != 0L) { checkNAddStates(44, 46); }
              if (curChar == 117) { stateSet[newStateCount++] = 77; }
            }
            case 66 -> {
              if ((0x7fffffe87fffffeL & l) != 0L) { if (kind > 66) kind = 66; checkNAdd(167); }
              if ((0x7fffffe87fffffeL & l) != 0L) { checkNAddStates(44, 46); }
              if (curChar == 112) { checkNAdd(60); }
            }
            case 100 -> {
              if ((0x7fffffe87fffffeL & l) != 0L) { if (kind > 66) kind = 66; checkNAdd(167); }
              if ((0x7fffffe87fffffeL & l) != 0L) { checkNAddStates(44, 46); }
              if (curChar == 116) { if (kind > 27) kind = 27; }
            }
            case 67 -> {
              if ((0x7fffffe87fffffeL & l) != 0L) { if (kind > 66) kind = 66; checkNAdd(167); }
              if ((0x7fffffe87fffffeL & l) != 0L) { checkNAddStates(44, 46); }
              if (curChar == 97) { stateSet[newStateCount++] = 66; }
            }
            case 101 -> {
              if ((0x7fffffe87fffffeL & l) != 0L) { if (kind > 66) kind = 66; checkNAdd(167); }
              if ((0x7fffffe87fffffeL & l) != 0L) { checkNAddStates(44, 46); }
              if (curChar == 110) { stateSet[newStateCount++] = 100; }
            }
            case 102 -> {
              if ((0x7fffffe87fffffeL & l) != 0L) { if (kind > 66) kind = 66; checkNAdd(167); }
              if ((0x7fffffe87fffffeL & l) != 0L) { checkNAddStates(44, 46); }
              if (curChar == 101) { stateSet[newStateCount++] = 101; }
            }
            case 103 -> {
              if ((0x7fffffe87fffffeL & l) != 0L) { if (kind > 66) kind = 66; checkNAdd(167); }
              if ((0x7fffffe87fffffeL & l) != 0L) { checkNAddStates(44, 46); }
              if (curChar == 109) { stateSet[newStateCount++] = 102; }
            }
            case 104 -> {
              if ((0x7fffffe87fffffeL & l) != 0L) { if (kind > 66) kind = 66; checkNAdd(167); }
              if ((0x7fffffe87fffffeL & l) != 0L) { checkNAddStates(44, 46); }
              if (curChar == 103) { stateSet[newStateCount++] = 103; }
            }
            case 62 -> { if ((0x7fffffe87fffffeL & l) != 0L) { if (kind > 66) kind = 66; checkNAdd(167); }
              if ((0x7fffffe87fffffeL & l) != 0L) { checkNAddStates(44, 46); }
              if (curChar == 105) { stateSet[newStateCount++] = 61; }
            }
            case 95 -> {
              if ((0x7fffffe87fffffeL & l) != 0L) { if (kind > 66) kind = 66; checkNAdd(167); }
              if ((0x7fffffe87fffffeL & l) != 0L) { checkNAddStates(44, 46); }
              if (curChar == 115) { stateSet[newStateCount++] = 97; }
              if (curChar == 115) { if (kind > 37) kind = 37; }
            }
            case 64 -> {
              if ((0x7fffffe87fffffeL & l) != 0L) { if (kind > 66) kind = 66; checkNAdd(167); }
              if ((0x7fffffe87fffffeL & l) != 0L) { checkNAddStates(44, 46); }
              if (curChar == 101) { checkNAdd(59); }
            }
            case 0 -> { if (curChar == 112 && kind > 27) kind = 27; }
            case 1 -> { if (curChar == 109) stateSet[newStateCount++] = 0; }
            case 2 -> { if (curChar == 105) stateSet[newStateCount++] = 1; }
            case 10 -> { if (curChar == 101 && kind > 36) kind = 36; }
            case 11 -> { if (curChar == 112) stateSet[newStateCount++] = 10; }
            case 12 -> { if (curChar == 121) stateSet[newStateCount++] = 11; }
            case 13 -> { if (curChar == 116) stateSet[newStateCount++] = 12; }
            case 15 -> { if (curChar == 111) stateSet[newStateCount++] = 14; }
            case 23 -> { if ((0xffffffffefffffffL & l) != 0L) checkNAddStates(38, 41); }
            case 24 -> { if (curChar == 117) stateSet[newStateCount++] = 25; }
            case 25 -> { if ((0x7e00000000L & l) != 0L) stateSet[newStateCount++] = 26; }
            case 26 -> { if ((0x7e00000000L & l) != 0L) stateSet[newStateCount++] = 27; }
            case 27 -> { if ((0x7e00000000L & l) != 0L) stateSet[newStateCount++] = 28; }
            case 28 -> { if ((0x7e00000000L & l) != 0L) checkNAddStates(38, 41); }
            case 29 -> { if (curChar == 92) stateSet[newStateCount++] = 30; }
            case 30 -> { if ((0x14404410000000L & l) != 0L) checkNAddStates(38, 41); }
            case 33 -> { if ((0xffffffffefffffffL & l) != 0L) checkNAdd(34); }
            case 35 -> { if (curChar == 117) stateSet[newStateCount++] = 36; }
            case 36 -> { if ((0x7e00000000L & l) != 0L) stateSet[newStateCount++] = 37; }
            case 37 -> { if ((0x7e00000000L & l) != 0L) stateSet[newStateCount++] = 38; }
            case 38 -> { if ((0x7e00000000L & l) != 0L) stateSet[newStateCount++] = 39; }
            case 39 -> { if ((0x7e00000000L & l) != 0L) checkNAdd(34); }
            case 40 -> { if (curChar == 92) stateSet[newStateCount++] = 41; }
            case 41 -> { if ((0x14404410000000L & l) != 0L) checkNAdd(34); }
            case 42 -> { if (curChar == 94) stateSet[newStateCount++] = 43; }
            case 43 -> { if ((0x7fffffe87fffffeL & l) == 0L) break; if (kind > 61) kind = 61; checkNAddStates(67, 71); }
            case 44 -> { if ((0x7fffffe87fffffeL & l) != 0L) checkNAddStates(72, 74); }
            case 46, 47 -> { if ((0x7fffffe87fffffeL & l) != 0L) checkNAddStates(75, 77); }
            case 49, 50 -> { if ((0x7fffffe87fffffeL & l) == 0L) break; if (kind > 61) kind = 61; checkNAddTwoStates(50, 51); }
            case 51 -> { if (curChar == 92) checkNAddTwoStates(52, 53); }
            case 54 -> { if (curChar == 95) checkNAdd(53); }
            case 59 -> { if (curChar == 116) checkNAdd(60); }
            case 60 -> { if (curChar == 91 && kind > 62) kind = 62; }
            case 61 -> { if (curChar == 115) checkNAdd(59); }
            case 63 -> { if (curChar == 108) stateSet[newStateCount++] = 62; }
            case 65 -> { if (curChar == 115) stateSet[newStateCount++] = 64; }
            case 68 -> { if (curChar == 109) stateSet[newStateCount++] = 67; }
            case 69 -> { if (curChar == 114) checkNAdd(60); }
            case 70 -> { if (curChar == 111) stateSet[newStateCount++] = 69; }
            case 71 -> { if (curChar == 116) stateSet[newStateCount++] = 70; }
            case 72 -> { if (curChar == 99) stateSet[newStateCount++] = 71; }
            case 74 -> { if (curChar == 118) stateSet[newStateCount++] = 73; }
            case 75 -> { if (curChar == 101) checkNAdd(60); }
            case 76 -> { if (curChar == 108) stateSet[newStateCount++] = 75; }
            case 77 -> { if (curChar == 112) stateSet[newStateCount++] = 76; }
            case 79 -> { if (curChar == 116) stateSet[newStateCount++] = 78; }
            case 81 -> { if (kind > 69) kind = 69; addStates(32, 34); }
            case 93 -> { if ((0xc7fffffedfffffffL & l) != 0L && kind > 73) kind = 73; }
            case 94 -> { if (curChar == 105) addStates(100, 101); }
            case 96 -> { if (curChar == 116 && kind > 37) kind = 37; }
            case 97 -> { if (curChar == 110) stateSet[newStateCount++] = 96; }
            case 98 -> { if (curChar == 115) stateSet[newStateCount++] = 97; }
            case 99 -> { if (curChar == 97) addStates(98, 99); }
            case 106 -> { if (curChar == 121) checkNAdd(60); }
            case 107 -> { if (curChar == 97) stateSet[newStateCount++] = 106; }
            case 108 -> { if (curChar == 114) stateSet[newStateCount++] = 107; }
            case 109 -> { if (curChar == 114) stateSet[newStateCount++] = 108; }
            case 112 -> { if (curChar == 95) stateSet[newStateCount++] = 111; }
            case 114 -> { if (curChar == 95) stateSet[newStateCount++] = 113; }
            case 115 -> { if (curChar == 76 && kind > 50) kind = 50; }
            case 116 -> { if (curChar == 95) stateSet[newStateCount++] = 115; }
            case 118 -> { if (curChar == 95) stateSet[newStateCount++] = 117; }
            case 119 -> { if (curChar == 66 && kind > 51) kind = 51; }
            case 120 -> { if (curChar == 95) stateSet[newStateCount++] = 119; }
            case 122 -> { if (curChar == 95) stateSet[newStateCount++] = 121; }
            case 125 -> { if (curChar == 95) stateSet[newStateCount++] = 124; }
            case 126 -> { if (curChar == 101) addStates(102, 103); }
            case 130 -> { if (curChar == 95) stateSet[newStateCount++] = 129; }
            case 133 -> { if (curChar == 95) stateSet[newStateCount++] = 132; }
            case 134 -> { if (curChar == 101) addStates(104, 105); }
            case 137 -> { if (curChar == 70 && kind > 53) kind = 53; }
            case 138 -> { if (curChar == 95) stateSet[newStateCount++] = 137; }
            case 140 -> { if (curChar == 95) stateSet[newStateCount++] = 139; }
            case 143 -> { if (curChar == 95) stateSet[newStateCount++] = 142; }
            case 144 -> { if (curChar == 101) addStates(106, 107); }
            case 147 -> { if (curChar == 66 && kind > 54) kind = 54; }
            case 148 -> { if (curChar == 95) stateSet[newStateCount++] = 147; }
            case 150 -> { if ((0x7fffffe87fffffeL & l) == 0L) break; if (kind > 66) kind = 66; checkNAddStates(28, 31); }
            case 151 -> { if ((0x7fffffe87fffffeL & l) != 0L) checkNAddStates(44, 46); }
            case 153, 154 -> { if ((0x7fffffe87fffffeL & l) != 0L) checkNAddStates(95, 97); }
            case 156 -> { if (curChar == 115 && kind > 60) kind = 60; }
            case 157 -> { if (curChar == 115) stateSet[newStateCount++] = 156; }
            case 158 -> { if (curChar == 97) stateSet[newStateCount++] = 157; }
            case 159 -> { if (curChar == 108) stateSet[newStateCount++] = 158; }
            case 160 -> { if (curChar == 99) stateSet[newStateCount++] = 159; }
            case 161 -> { if (curChar == 101 && kind > 60) kind = 60; }
            case 162 -> { if (curChar == 108) stateSet[newStateCount++] = 161; }
            case 163 -> { if (curChar == 117) stateSet[newStateCount++] = 162; }
            case 164 -> { if (curChar == 100) stateSet[newStateCount++] = 163; }
            case 165 -> { if (curChar == 111) stateSet[newStateCount++] = 164; }
            case 166 -> { if (curChar == 109) stateSet[newStateCount++] = 165; }
            case 167 -> { if ((0x7fffffe87fffffeL & l) == 0L) break; if (kind > 66) kind = 66; checkNAdd(167); }
            // default -> {}
          }
        } while (i != startsAt);
      } else {
        var hiByte = (curChar >> 8);
        var i1 = hiByte >> 6;
        var l1 = 1L << (hiByte & 077);
        var i2 = (curChar & 0xff) >> 6;
        var l2 = 1L << (curChar & 077);
        do {
          switch (stateSet[--i]) {
            case 3 -> {
              if (canMove_0(hiByte, i1, i2, l1, l2)) { if (kind > 73) kind = 73; }
              if (canMove_1(hiByte, i1, i2, l1, l2)) { if (kind > 66) kind = 66; checkNAddStates(28, 31); }
            }
            case 73 -> {
              if (canMove_1(hiByte, i1, i2, l1, l2)) { checkNAddStates(44, 46); }
              if (canMove_1(hiByte, i1, i2, l1, l2)) { if (kind > 66) kind = 66; checkNAdd(167); }
            }
            case 105 -> {
              if (canMove_1(hiByte, i1, i2, l1, l2)) { checkNAddStates(44, 46); }
              if (canMove_1(hiByte, i1, i2, l1, l2)) { if (kind > 66) kind = 66; checkNAdd(167); }
            }
            case 14 -> {
              if (canMove_1(hiByte, i1, i2, l1, l2)) { checkNAddStates(44, 46); }
              if (canMove_1(hiByte, i1, i2, l1, l2)) { if (kind > 66) kind = 66; checkNAdd(167); }
            }
            case 169, 23 -> {
              if (canMove_0(hiByte, i1, i2, l1, l2)) { checkNAddStates(38, 41); }
            }
            case 168 -> {
              if (canMove_1(hiByte, i1, i2, l1, l2)) { checkNAddStates(44, 46); }
              if (canMove_1(hiByte, i1, i2, l1, l2)) { if (kind > 66) kind = 66; checkNAdd(167); }
            }
            case 78 -> {
              if (canMove_1(hiByte, i1, i2, l1, l2)) { checkNAddStates(44, 46); }
              if (canMove_1(hiByte, i1, i2, l1, l2)) { if (kind > 66) kind = 66; checkNAdd(167); }
            }
            case 66 -> {
              if (canMove_1(hiByte, i1, i2, l1, l2)) { checkNAddStates(44, 46); }
              if (canMove_1(hiByte, i1, i2, l1, l2)) { if (kind > 66) kind = 66; checkNAdd(167); }
            }
            case 100 -> {
              if (canMove_1(hiByte, i1, i2, l1, l2)) { checkNAddStates(44, 46); }
              if (canMove_1(hiByte, i1, i2, l1, l2)) { if (kind > 66) kind = 66; checkNAdd(167); }
            }
            case 67 -> {
              if (canMove_1(hiByte, i1, i2, l1, l2)) { checkNAddStates(44, 46); }
              if (canMove_1(hiByte, i1, i2, l1, l2)) { if (kind > 66) kind = 66; checkNAdd(167); }
            }
            case 101 -> {
              if (canMove_1(hiByte, i1, i2, l1, l2)) { checkNAddStates(44, 46); }
              if (canMove_1(hiByte, i1, i2, l1, l2)) { if (kind > 66)  kind = 66; checkNAdd(167); }
            }
            case 102 -> {
              if (canMove_1(hiByte, i1, i2, l1, l2)) { checkNAddStates(44, 46); }
              if (canMove_1(hiByte, i1, i2, l1, l2)) { if (kind > 66) kind = 66; checkNAdd(167); }
            }
            case 103 -> {
              if (canMove_1(hiByte, i1, i2, l1, l2)) { checkNAddStates(44, 46); }
              if (canMove_1(hiByte, i1, i2, l1, l2)) { if (kind > 66) kind = 66; checkNAdd(167); }
            }
            case 104 -> {
              if (canMove_1(hiByte, i1, i2, l1, l2)) { checkNAddStates(44, 46); }
              if (canMove_1(hiByte, i1, i2, l1, l2)) { if (kind > 66) kind = 66; checkNAdd(167); }
            }
            case 62 -> {
              if (canMove_1(hiByte, i1, i2, l1, l2)) { checkNAddStates(44, 46); }
              if (canMove_1(hiByte, i1, i2, l1, l2)) { if (kind > 66) kind = 66; checkNAdd(167); }
            }
            case 95 -> {
              if (canMove_1(hiByte, i1, i2, l1, l2)) { checkNAddStates(44, 46); }
              if (canMove_1(hiByte, i1, i2, l1, l2)) { if (kind > 66) kind = 66; checkNAdd(167); }
            }
            case 64 -> {
              if (canMove_1(hiByte, i1, i2, l1, l2)) { checkNAddStates(44, 46); }
              if (canMove_1(hiByte, i1, i2, l1, l2)) { if (kind > 66) kind = 66; checkNAdd(167);}
            }
            case 33 -> { if (canMove_0(hiByte, i1, i2, l1, l2)) stateSet[newStateCount++] = 34; }
            case 43 -> { if (!canMove_1(hiByte, i1, i2, l1, l2)) break; if (kind > 61) kind = 61; checkNAddStates(67, 71); }
            case 44 -> { if (canMove_1(hiByte, i1, i2, l1, l2)) checkNAddStates(72, 74); }
            case 46, 47 -> { if (canMove_1(hiByte, i1, i2, l1, l2)) checkNAddStates(75, 77); }
            case 49, 50 -> { if (!canMove_1(hiByte, i1, i2, l1, l2)) break; if (kind > 61)  kind = 61; checkNAddTwoStates(50, 51); }
            case 81 -> { if (!canMove_0(hiByte, i1, i2, l1, l2)) break; if (kind > 69) kind = 69; addStates(32, 34); }
            case 93 -> { if (canMove_0(hiByte, i1, i2, l1, l2) && kind > 73) kind = 73; }
            case 150 -> { if (!canMove_1(hiByte, i1, i2, l1, l2)) break; if (kind > 66)  kind = 66; checkNAddStates(28, 31); }
            case 151 -> { if (canMove_1(hiByte, i1, i2, l1, l2)) checkNAddStates(44, 46); }
            case 153, 154 -> { if (canMove_1(hiByte, i1, i2, l1, l2)) checkNAddStates(95, 97); }
            case 167 -> { if (!canMove_1(hiByte, i1, i2, l1, l2)) break; if (kind > 66) kind = 66; checkNAdd(167); }
            // default -> { if (i1 == 0 || l1 == 0 || i2 == 0 || l2 == 0) {} else {} }
          }
        } while (i != startsAt);
      }
      if (kind != 0x7fffffff) {
        matchedKind = kind;
        matchedPos = curPos;
        kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = newStateCount) == (startsAt = 168 - (newStateCount = startsAt))) {
        return curPos;
      }
      try {
        curChar = charSource.readChar();
      } catch (IOException e) {
        return curPos;
      }
    }
  }

  int moveStringLiteralDfa0_3() {
    return moveNfa_3(7, 0);
  }

  int moveNfa_3(int startState, int curPos) {
    var startsAt = 0;
    newStateCount = 9;
    var i = 1;
    stateSet[0] = startState;
    var kind = 0x7fffffff;
    for (;;) {
      if (++round == 0x7fffffff) {
        resetRounds();
      }
      if (curChar < 64) {
        var l = 1L << curChar;
        do {
          switch (stateSet[--i]) {
            case 7 -> {
              if ((0xdbff8cfeffffcdffL & l) != 0L) { if (kind > 73) kind = 73; }
                else if (curChar == 45) { stateSet[newStateCount++] = 6; }
            }
            case 0 -> { if (curChar == 45) checkNAddStates(108, 110); }
            case 1 -> { if ((0x100000200L & l) != 0L) checkNAddStates(108, 110); }
            case 2 -> { if ((0x2400L & l) != 0L && kind > 72) kind = 72; }
            case 3 -> { if (curChar == 10 && kind > 72) kind = 72; }
            case 4 -> { if (curChar == 13) stateSet[newStateCount++] = 3; }
            case 5 -> { if (curChar == 45) stateSet[newStateCount++] = 0; }
            case 6 -> { if (curChar == 45) stateSet[newStateCount++] = 5; }
            case 8 -> { if ((0xdbff8cfeffffcdffL & l) != 0L && kind > 73) kind = 73; }
            // default -> {}
          }
        } while (i != startsAt);
      } else if (curChar < 128) {
        var l = 1L << (curChar & 077);
        do {
          switch (stateSet[--i]) {
            case 7 -> { if ((0xc7fffffedfffffffL & l) != 0L) kind = 73; }
            // default -> {}
          }
        } while (i != startsAt);
      } else {
        var hiByte = (curChar >> 8);
        var i1 = hiByte >> 6;
        var l1 = 1L << (hiByte & 077);
        var i2 = (curChar & 0xff) >> 6;
        var l2 = 1L << (curChar & 077);
        do {
          switch (stateSet[--i]) {
            case 7 -> { if (canMove_0(hiByte, i1, i2, l1, l2) && kind > 73) kind = 73; }
            // default -> { if (i1 == 0 || l1 == 0 || i2 == 0 || l2 == 0) {} else {} }
          }
        } while (i != startsAt);
      }
      if (kind != 0x7fffffff) {
        matchedKind = kind;
        matchedPos = curPos;
        kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = newStateCount) == (startsAt = 9 - (newStateCount = startsAt))) {
        return curPos;
      }
      try {
        curChar = charSource.readChar();
      } catch (IOException e) {
        return curPos;
      }
    }
  }

  int moveStringLiteralDfa0_2() {
    return moveNfa_2(0, 0);
  }

  int moveNfa_2(int startState, int curPos) {
    var startsAt = 0;
    newStateCount = 3;
    var i = 1;
    stateSet[0] = startState;
    var kind = 0x7fffffff;
    for (;;) {
      if (++round == 0x7fffffff) {
        resetRounds();
      }
      if (curChar < 64) {
        var l = 1L << curChar;
        do {
          switch (stateSet[--i]) {
            case 0 -> {
              if ((0xdbff8cfeffffcdffL & l) != 0L) { if (kind > 73) kind = 73; }
              if (curChar == 36) { if (kind > 66) kind = 66; checkNAdd(1); }
            }
            case 1 -> { if ((0x3ff001000000000L & l) == 0L) break; if (kind > 66) kind = 66; checkNAdd(1); }
            case 2 -> { if ((0xdbff8cfeffffcdffL & l) != 0L && kind > 73) kind = 73; }
            // default -> {}
          }
        } while (i != startsAt);
      } else if (curChar < 128) {
        var l = 1L << (curChar & 077);
        do {
          switch (stateSet[--i]) {
            case 0 -> {
              if ((0xc7fffffedfffffffL & l) != 0L) { if (kind > 73) kind = 73; }
              if ((0x7fffffe87fffffeL & l) != 0L) { if (kind > 66) kind = 66; checkNAdd(1); }
            }
            case 1 -> { if ((0x7fffffe87fffffeL & l) == 0L) break; if (kind > 66) kind = 66; checkNAdd(1); }
            case 2 -> { if ((0xc7fffffedfffffffL & l) != 0L && kind > 73) kind = 73; }
            // default -> {}
          }
        } while (i != startsAt);
      } else {
        var hiByte = (curChar >> 8);
        var i1 = hiByte >> 6;
        var l1 = 1L << (hiByte & 077);
        var i2 = (curChar & 0xff) >> 6;
        var l2 = 1L << (curChar & 077);
        do {
          switch (stateSet[--i]) {
            case 0 -> {
              if (canMove_1(hiByte, i1, i2, l1, l2)) { if (kind > 66) kind = 66; checkNAdd(1); }
              if (canMove_0(hiByte, i1, i2, l1, l2)) { if (kind > 73) kind = 73; }
            }
            case 1 -> { if (!canMove_1(hiByte, i1, i2, l1, l2)) break; if (kind > 66) kind = 66; checkNAdd(1); }
            case 2 -> { if (canMove_0(hiByte, i1, i2, l1, l2) && kind > 73) kind = 73; }
            // default -> { if (i1 == 0 || l1 == 0 || i2 == 0 || l2 == 0) {} else {} }
          }
        } while (i != startsAt);
      }
      if (kind != 0x7fffffff) {
        matchedKind = kind;
        matchedPos = curPos;
        kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = newStateCount) == (startsAt = 3 - (newStateCount = startsAt))) {
        return curPos;
      }
      try {
        curChar = charSource.readChar();
      } catch (IOException e) {
        return curPos;
      }
    }
  }

  int stopStringLiteralDfa_1(int pos, long active0, long active1) {
    switch (pos) {
      case 0 -> {
        if ((active1 & 0x2L) != 0L) {
          matchedKind = 64;
          return -1;
        }
        return -1;
      }
      case 1 -> {
        if ((active1 & 0x2L) != 0L) {
          if (matchedPos == 0) {
            matchedKind = 64;
            matchedPos = 0;
          }
          return -1;
        }
        return -1;
      }
      default -> {
        return -1;
      }
    }
  }

  int startNfa_1(int pos, long active0, long active1) {
    return moveNfa_1(stopStringLiteralDfa_1(pos, active0, active1), pos + 1);
  }

  int moveStringLiteralDfa0_1() {
    return switch (curChar) {
      case 34 -> moveStringLiteralDfa1_1(0x2L);
      default -> moveNfa_1(0, 0);
    };
  }

  int moveStringLiteralDfa1_1(long active1) {
    try {
      curChar = charSource.readChar();
    } catch (IOException e) {
      stopStringLiteralDfa_1(0, 0L, active1);
      return 1;
    }
    switch (curChar) {
      case 34 -> { return moveStringLiteralDfa2_1(active1, 0x2L); }
      // default -> {}
    }
    return startNfa_1(0, 0L, active1);
  }

  int moveStringLiteralDfa2_1(long old1, long active1) {
    if (((active1 &= old1)) == 0L) {
      return startNfa_1(0, 0L, old1);
    }
    try {
      curChar = charSource.readChar();
    } catch (IOException e) {
      stopStringLiteralDfa_1(1, 0L, active1);
      return 2;
    }
    switch (curChar) {
      case 34 -> { if ((active1 & 0x2L) != 0L) return stopAtPos(2, 65); }
      // default -> {}
    }
    return startNfa_1(1, 0L, active1);
  }

  int moveNfa_1(int startState, int curPos) {
    var startsAt = 0;
    newStateCount = 4;
    var i = 1;
    stateSet[0] = startState;
    var kind = 0x7fffffff;
    for (;;) {
      if (++round == 0x7fffffff) {
        resetRounds();
      }
      if (curChar < 64) {
        var l = 1L << curChar;
        do {
          switch (stateSet[--i]) {
            case 0 -> { if (kind > 64) kind = 64; if ((0xdbff8cfeffffcdffL & l) != 0L) if (kind > 73) kind = 73; }
            case 2 -> { if (curChar == 34 && kind > 64) kind = 64; }
            case 3 -> { if ((0xdbff8cfeffffcdffL & l) != 0L && kind > 73) kind = 73; }
            // default -> {}
          }
        } while (i != startsAt);
      } else if (curChar < 128) {
        var l = 1L << (curChar & 077);
        do {
          switch (stateSet[--i]) {
            case 0 -> { if (kind > 64) kind = 64; if ((0xc7fffffedfffffffL & l) != 0L) { if (kind > 73) kind = 73; } if (curChar == 92) stateSet[newStateCount++] = 2; }
            case 1 -> { if (curChar == 92) stateSet[newStateCount++] = 2; }
            case 3 -> { if ((0xc7fffffedfffffffL & l) != 0L && kind > 73) kind = 73; }
            // default -> {}
          }
        } while (i != startsAt);
      } else {
        var hiByte = (curChar >> 8);
        var i1 = hiByte >> 6;
        var l1 = 1L << (hiByte & 077);
        var i2 = (curChar & 0xff) >> 6;
        var l2 = 1L << (curChar & 077);
        do {
          switch (stateSet[--i]) {
            case 0 -> {
              if (canMove_0(hiByte, i1, i2, l1, l2)) { if (kind > 64) kind = 64; }
              if (canMove_0(hiByte, i1, i2, l1, l2)) { if (kind > 73) kind = 73; }
            }
            case 3 -> { if (canMove_0(hiByte, i1, i2, l1, l2) && kind > 73) kind = 73; }
            // default -> { if (i1 == 0 || l1 == 0 || i2 == 0 || l2 == 0) {} else {} }
          }
        } while (i != startsAt);
      }
      if (kind != 0x7fffffff) {
        matchedKind = kind;
        matchedPos = curPos;
        kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = newStateCount) == (startsAt = 4 - (newStateCount = startsAt))) {
        return curPos;
      }
      try {
        curChar = charSource.readChar();
      } catch (IOException e) {
        return curPos;
      }
    }
  }

  /** Token literal values. */
  static String[] strLiteralImages = {
    "", null, null, null, null, "\12", null, "\155\157\144\165\154\145",
    "\151\155\160\157\162\164", "\146\165\156\143\164\151\157\156", "\154\157\143\141\154",
    "\162\145\164\165\162\156", "\151\146", "\145\154\163\145", "\167\150\151\154\145", "\146\157\162",
    "\146\157\162\145\141\143\150", "\151\156", "\164\150\162\157\167", "\164\162\171", "\143\141\164\143\150",
    "\146\151\156\141\154\154\171", "\143\141\163\145", "\167\150\145\156", "\155\141\164\143\150",
    "\164\150\145\156", "\157\164\150\145\162\167\151\163\145", null,
    "\141\165\147\155\145\156\164\141\164\151\157\156", "\167\151\164\150", "\142\162\145\141\153",
    "\143\157\156\164\151\156\165\145", "\163\164\162\165\143\164", "\165\156\151\157\156", null, null, null, null,
    "\141\156\144", "\157\162", "\157\162\111\146\116\165\154\154", "\156\157\164", null, "\41",
    "\100", "\46", "\155\141\143\162\157", "\166\141\162", "\154\145\164", null, null,
    null, null, null, null, null, null, "\156\165\154\154", "\164\162\165\145",
    "\146\141\154\163\145", null, null, null, null, null, null, null, null, null, null, null, null, null,
    null, "\56", "\54", "\56\173", "\175", "\75", "\173", "\50", "\51", "\56\56\56",
    "\174", "\55\76", "\56\56", "\135",
  };

  Token fillToken() {
    var im = strLiteralImages[matchedKind];
    var curTokenImage = (im == null) ? charSource.GetImage() : im;
    var t = Token.newToken(matchedKind, curTokenImage);
    t.beginLine = charSource.getBeginLine();
    t.endLine = charSource.getEndLine();
    t.beginColumn = charSource.getBeginColumn();
    t.endColumn = charSource.getEndColumn();
    return t;
  }

  static int[] nextStates = {
    111, 113, 117, 121, 129, 139, 112, 111, 114, 113, 116, 118, 117, 120, 122, 121,
    123, 126, 130, 129, 131, 134, 138, 140, 139, 141, 144, 148, 151, 152, 167, 155,
    81, 82, 84, 33, 35, 40, 23, 24, 29, 31, 160, 166, 151, 152, 155, 140,
    139, 141, 144, 148, 130, 129, 131, 134, 138, 122, 121, 123, 126, 118, 117, 120,
    114, 113, 116, 44, 45, 58, 50, 51, 44, 45, 58, 45, 47, 58, 54, 53,
    57, 86, 87, 89, 125, 124, 126, 133, 132, 134, 138, 143, 142, 144, 148, 152,
    154, 155, 105, 109, 95, 98, 127, 128, 135, 136, 145, 146, 1, 2, 4,
  };

  static boolean canMove_0(int hiByte, int i1, int i2, long l1, long l2) {
    return switch (hiByte) {
      case 0 -> (bitVec2[i2] & l2) != 0L;
      default -> (bitVec0[i1] & l1) != 0L;
    };
  }

  static boolean canMove_1(int hiByte, int i1, int i2, long l1, long l2) {
    return switch (hiByte) {
      case 0 -> (bitVec3[i2] & l2) != 0L;
      default -> false;
    };
  }

  int curLexState = 0;
  int defaultLexState = 0;
  int newStateCount;
  int round;
  int matchedPos;
  int matchedKind;

  /** Get the next Token. */
  Token getNextToken() {
    Token matchedToken;
    var curPos = 0;
    loop: for (;;) {
      try {
        curChar = charSource.BeginToken();
      } catch (Exception e) {
        matchedKind = 0;
        matchedPos = -1;
        matchedToken = fillToken();
        commonTokenAction(matchedToken);
        return matchedToken;
      }
      for (;;) {
        switch (curLexState) {
          case 0 -> {
            try {
              charSource.backup(0);
              while (curChar <= 32 && (0x100003200L & (1L << curChar)) != 0L) {
                curChar = charSource.BeginToken();
              }
            } catch (IOException e1) {
              continue loop;
            }
            matchedKind = 0x7fffffff;
            matchedPos = 0;
            curPos = moveStringLiteralDfa0_0();
          }
          case 1 -> {
            matchedKind = 0x7fffffff;
            matchedPos = 0;
            curPos = moveStringLiteralDfa0_1();
          }
          case 2 -> {
            matchedKind = 0x7fffffff;
            matchedPos = 0;
            curPos = moveStringLiteralDfa0_2();
          }
          case 3 -> {
            matchedKind = 0x7fffffff;
            matchedPos = 0;
            curPos = moveStringLiteralDfa0_3();
            if (matchedPos == 0 && matchedKind > 71) {
              matchedKind = 71;
            }
          }
        }
        if (matchedKind != 0x7fffffff) {
          if (matchedPos + 1 < curPos) {
            charSource.backup(curPos - matchedPos - 1);
          }
          if ((toToken[matchedKind >> 6] & (1L << (matchedKind & 077))) != 0L) {
            matchedToken = fillToken();
            if (newLexState[matchedKind] != -1) {
              curLexState = newLexState[matchedKind];
            }
            commonTokenAction(matchedToken);
            return matchedToken;
          } else if ((toSkip[matchedKind >> 6] & (1L << (matchedKind & 077))) != 0L) {
            if (newLexState[matchedKind] != -1) {
              curLexState = newLexState[matchedKind];
            }
            continue loop;
          }
          if (newLexState[matchedKind] != -1) {
            curLexState = newLexState[matchedKind];
          }
          curPos = 0;
          matchedKind = 0x7fffffff;
          try {
            curChar = charSource.readChar();
            continue;
          } catch (IOException e1) {}
        }
        var errorLine = charSource.getEndLine();
        var errorColumn = charSource.getEndColumn();
        String errorAfter = null;
        var eof = false;
        try {
          charSource.readChar();
          charSource.backup(1);
        } catch (IOException e1) {
          eof = true;
          errorAfter = curPos <= 1 ? "" : charSource.GetImage();
          if (curChar == '\n' || curChar == '\r') {
            errorLine++;
            errorColumn = 0;
          } else {
            errorColumn++;
          }
        }
        if (!eof) {
          charSource.backup(1);
          errorAfter = curPos <= 1 ? "" : charSource.GetImage();
        }
        throw new TokenManagerError(eof, curLexState, errorLine, errorColumn, errorAfter, curChar, TokenManagerError.LEXICAL_ERROR);
      }
    }
  }

  void checkNAdd(int state) {
    if (jjrounds[state] != round) {
      stateSet[newStateCount++] = state;
      jjrounds[state] = round;
    }
  }

  void addStates(int start, int end) {
    do {
      stateSet[newStateCount++] = nextStates[start];
    } while (start++ != end);
  }

  void checkNAddTwoStates(int state1, int state2) {
    checkNAdd(state1);
    checkNAdd(state2);
  }

  void checkNAddStates(int start, int end) {
    do {
      checkNAdd(nextStates[start]);
    } while (start++ != end);
  }

  /** Constructor. */
  TokenManager(CharSource stream) {

    if (CharSource.staticFlag) {
      throw new Error("ERROR: Cannot use a static CharStream class with a non-static lexical analyzer.");
    }

    charSource = stream;
  }

  /** Constructor. */
  TokenManager(CharSource stream, int lexState) {
    reset(stream);
    switchTo(lexState);
  }

  /** Reinitialise parser. */
  void reset(CharSource stream) {

    matchedPos
            = newStateCount
            = 0;
    curLexState = defaultLexState;
    charSource = stream;
    resetRounds();
  }

  void resetRounds() {
    int i;
    round = 0x80000001;
    for (i = 168; i-- > 0;) {
      jjrounds[i] = 0x80000000;
    }
  }

  /** Reinitialise parser. */
  void reset(CharSource stream, int lexState) {
    reset(stream);
    switchTo(lexState);
  }

  /** Switch to specified lex state. */
  void switchTo(int lexState) {
    if (lexState >= 4 || lexState < 0) {
      throw new TokenManagerError("Error: Ignoring invalid lexical state : " + lexState + ". State unchanged.", TokenManagerError.INVALID_LEXICAL_STATE);
    } else {
      curLexState = lexState;
    }
  }

  /** Lexer state names. */
  static final String[] lexStateNames = {
    "DEFAULT",
    "WithinMultiString",
    "ESCAPED",
    "WithinDocumentation",
  };

  /** Lex State array. */
  static final int[] newLexState = {
    -1, -1, -1, -1, -1, -1,  2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,  1, -1,  0,  0, -1, -1, -1,  3, -1,  0,  0, -1,
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
  };

  static final long[]
    toToken = { 0x7fffffffffffffa1L, 0x7fff26L,},
    toSkip = { 0x5eL, 0x0L,},
    toSpecial = { 0x0L, 0x0L,},
    toMore = { 0x8000000000000000L, 0xc1L,};

  CharSource charSource;

  final int[] jjrounds = new int[168];
  final int[] stateSet = new int[2 * 168];

  final StringBuilder imageBuilder = new StringBuilder();
  StringBuilder image = imageBuilder;
  int imageLen;

  int lengthOfMatch;
  int curChar;

}
/*
  void skipLexicalActions(Token matchedToken) {
    // switch (matchedKind) { default -> {} }
  }
  void moreLexicalActions() {
    imageLen += (lengthOfMatch = matchedPos + 1);
    // switch (matchedKind) { default -> {} }
  }
  void tokenLexicalActions(Token matchedToken) {
    // switch (matchedKind) { default -> {} }
  }
*/

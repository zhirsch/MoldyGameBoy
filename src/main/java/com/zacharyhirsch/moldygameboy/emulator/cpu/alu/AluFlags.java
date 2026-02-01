package com.zacharyhirsch.moldygameboy.emulator.cpu.alu;

public record AluFlags(Boolean z, Boolean n, Boolean h, Boolean c) {

  public static final int Z = 0b1000_0000;
  public static final int N = 0b0100_0000;
  public static final int H = 0b0010_0000;
  public static final int C = 0b0001_0000;

  public static boolean isZero(byte flags) {
    return (flags & Z) != 0;
  }

  public static boolean isCarry(byte flags) {
    return (flags & C) != 0;
  }

  public static byte apply(byte flags, Boolean z, Boolean n, Boolean h, Boolean c) {
    int mask = ~(z == null ? 0 : Z) | (n == null ? 0 : N) | (h == null ? 0 : H) | (c == null ? 0 : C);
    byte v = (byte) (flags & mask);
    if (z != null && z) {
      v = (byte) (v | Z);
    }
    if (n != null && n) {
      v = (byte) (v | N);
    }
    if (h != null && h) {
      v = (byte) (v | H);
    }
    if (c != null && c) {
      v = (byte) (v | C);
    }
    return v;
  }
}

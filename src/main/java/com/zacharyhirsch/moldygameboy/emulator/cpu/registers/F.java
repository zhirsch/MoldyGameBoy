package com.zacharyhirsch.moldygameboy.emulator.cpu.registers;

import java.util.function.Predicate;

public final class F extends R {

  private static final int Z = 0b1000_0000;
  private static final int N = 0b0100_0000;
  private static final int H = 0b0010_0000;
  private static final int C = 0b0001_0000;

  private final Flag z;
  private final Flag n;
  private final Flag h;
  private final Flag c;

  F() {
    this.z = new Flag(this, Z);
    this.n = new Flag(this, N);
    this.h = new Flag(this, H);
    this.c = new Flag(this, C);
  }

  @Override
  public byte get() {
    return (byte) (super.get() & 0xf0);
  }

  @Override
  public void set(byte value) {
    super.set((byte) (value & 0xf0));
  }

  public Flag z() {
    return z;
  }

  public Flag n() {
    return n;
  }

  public Flag h() {
    return h;
  }

  public Flag c() {
    return c;
  }

  public static final class Flag {

    private final R register;
    private final int mask;

    Flag(R register, int mask) {
      this.register = register;
      this.mask = mask;
    }

    public boolean get() {
      return (register.get() & mask) != 0;
    }

    public void set(boolean value) {
      register.set(value ? (byte) (register.get() | mask) : (byte) (register.get() & ~mask));
    }
  }
}

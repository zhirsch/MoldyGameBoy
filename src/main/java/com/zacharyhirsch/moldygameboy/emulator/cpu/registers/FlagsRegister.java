package com.zacharyhirsch.moldygameboy.emulator.cpu.registers;

import com.zacharyhirsch.moldygameboy.emulator.arch.Register8;
import com.zacharyhirsch.moldygameboy.emulator.arch.UInt8;

public final class FlagsRegister implements UInt8 {

  private static final int Z = 0b1000_0000;
  private static final int N = 0b0100_0000;
  private static final int H = 0b0010_0000;
  private static final int C = 0b0001_0000;

  private final Register8 register;

  public FlagsRegister() {
    this.register = new Register8();
  }

  public boolean getZ() {
    return (register.get() & Z) != 0;
  }

  public boolean getN() {
    return (register.get() & N) != 0;
  }

  public boolean getH() {
    return (register.get() & H) != 0;
  }

  public boolean getC() {
    return (register.get() & C) != 0;
  }

  public void setZ(boolean z) {
    if (z) {
      register.set((byte) (register.get() | Z));
    } else {
      register.set((byte) (register.get() & ~Z));
    }
  }

  public void setN(boolean n) {
    if (n) {
      register.set((byte) (register.get() | N));
    } else {
      register.set((byte) (register.get() & ~N));
    }
  }

  public void setH(boolean h) {
    if (h) {
      register.set((byte) (register.get() | H));
    } else {
      register.set((byte) (register.get() & ~H));
    }
  }

  public void setC(boolean c) {
    if (c) {
      register.set((byte) (register.get() | C));
    } else {
      register.set((byte) (register.get() & ~C));
    }
  }

  @Override
  public byte get() {
    return register.get();
  }

  @Override
  public void set(byte value) {
    register.set((byte) (value & 0b1111_0000));
  }
}

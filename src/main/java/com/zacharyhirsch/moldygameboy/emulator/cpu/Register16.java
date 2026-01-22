package com.zacharyhirsch.moldygameboy.emulator.cpu;

import com.zacharyhirsch.moldygameboy.emulator.arch.UInt16Input;
import com.zacharyhirsch.moldygameboy.emulator.arch.UInt16Output;

final class Register16 implements UInt16Input, UInt16Output {

  private final Register8 hi;
  private final Register8 lo;

  Register16() {
    this.hi = new Register8();
    this.lo = new Register8();
  }

  public Register8 hi() {
    return hi;
  }

  public Register8 lo() {
    return lo;
  }

  @Override
  public short get() {
    return (short) ((Byte.toUnsignedInt(hi.get()) << 8) | Byte.toUnsignedInt(lo.get()));
  }

  public void set(short value) {
    hi.set((byte) ((value & 0xff00) >>> 8));
    lo.set((byte) ((value & 0x00ff) >>> 0));
  }
}

package com.zacharyhirsch.moldygameboy.emulator.memory.registers;

import com.zacharyhirsch.moldygameboy.emulator.arch.IORegister;

public final class Div implements IORegister {

  private static final int MASK = 0b1111_1111;

  private byte value = 0;

  public Div() {}

  @Override
  public byte read() {
    return (byte) ((value & MASK) | ~MASK);
  }

  @Override
  public void write(byte value) {
    this.value = (byte) ((this.value & ~MASK) | (value & MASK));
  }

  public byte get() {
    return value;
  }

  public void set(byte value) {
    this.value = value;
  }
}

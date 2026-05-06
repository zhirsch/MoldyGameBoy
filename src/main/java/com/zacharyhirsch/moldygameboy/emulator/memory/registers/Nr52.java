package com.zacharyhirsch.moldygameboy.emulator.memory.registers;

import com.zacharyhirsch.moldygameboy.emulator.arch.IORegister;

public final class Nr52 implements IORegister {

  private static final int READ_MASK = 0b1000_1111;
  private static final int WRITE_MASK = 0b1000_0000;

  private byte value = 0;

  public Nr52() {}

  @Override
  public byte read() {
    return (byte) ((value & READ_MASK) | ~READ_MASK);
  }

  @Override
  public void write(byte value) {
    this.value = (byte) ((this.value & ~WRITE_MASK) | (value & WRITE_MASK));
  }
}

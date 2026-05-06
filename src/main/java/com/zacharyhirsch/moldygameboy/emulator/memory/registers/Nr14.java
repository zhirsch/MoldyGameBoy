package com.zacharyhirsch.moldygameboy.emulator.memory.registers;

import com.zacharyhirsch.moldygameboy.emulator.arch.IORegister;

public final class Nr14 implements IORegister {

  private static final int READ_MASK = 0b0100_0000;
  private static final int WRITE_MASK = 0b1100_0111;

  private byte value = 0;

  public Nr14() {}

  @Override
  public byte read() {
    return (byte) ((value & READ_MASK) | ~READ_MASK);
  }

  @Override
  public void write(byte value) {
    this.value = (byte) ((this.value & ~WRITE_MASK) | (value & WRITE_MASK));
  }
}

package com.zacharyhirsch.moldygameboy.emulator.memory.registers;

import com.zacharyhirsch.moldygameboy.emulator.arch.IORegister;

public final class Nr51 implements IORegister {

  private static final int READ_MASK = 0b1111_1111;
  private static final int WRITE_MASK = 0b1111_1111;

  private byte value = 0;

  public Nr51() {}

  @Override
  public byte read() {
    return (byte) ((value & READ_MASK) | ~READ_MASK);
  }

  @Override
  public void write(byte value) {
    this.value = (byte) ((this.value & ~WRITE_MASK) | (value & WRITE_MASK));
  }
}

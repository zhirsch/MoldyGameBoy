package com.zacharyhirsch.moldygameboy.emulator.memory.registers;

import com.zacharyhirsch.moldygameboy.emulator.arch.Register8;

public final class Nr13 implements Register8 {

  private static final int READ_MASK = 0b0000_0000;
  private static final int WRITE_MASK = 0b1111_1111;

  private byte value = 0;

  public Nr13() {}

  @Override
  public byte read() {
    return (byte) ((value & READ_MASK) | ~READ_MASK);
  }

  @Override
  public void write(byte value) {
    this.value = (byte) ((this.value & ~WRITE_MASK) | (value & WRITE_MASK));
  }
}

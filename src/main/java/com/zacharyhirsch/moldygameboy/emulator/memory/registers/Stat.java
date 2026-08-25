package com.zacharyhirsch.moldygameboy.emulator.memory.registers;

import com.zacharyhirsch.moldygameboy.emulator.arch.IORegister;

public final class Stat implements IORegister {

  private static final int READ_MASK = 0b0111_1111;
  private static final int WRITE_MASK = 0b0111_1000;

  private byte value = 0;

  public Stat() {}

  @Override
  public byte read() {
    return get();
  }

  @Override
  public void write(byte value) {
    this.value = (byte) ((this.value & ~WRITE_MASK) | (value & WRITE_MASK));
  }

  public byte get() {
    return (byte) ((value & READ_MASK) | ~READ_MASK);
  }

  public void setLyEqualsLyc(boolean value) {
    this.value = (byte) ((this.value & 0b1111_1011) | (value ? 0b0000_0100 : 0));
  }

  public void setMode(byte value) {
    this.value = (byte) ((this.value & 0b1111_1100) | (value & 0b0000_0011));
    switch (value) {
      case 0:
        this.value = (byte) ((this.value & 0b1100_0111) | 0b0000_1000);
        break;
      case 1:
        this.value = (byte) ((this.value & 0b1100_0111) | 0b001_0000);
        break;
      case 2:
        this.value = (byte) ((this.value & 0b1100_0111) | 0b0010_0000);
        break;
      case 3:
        this.value = (byte) ((this.value & 0b1100_0111) | 0b0000_0000);
        break;
      default:
        throw new IllegalArgumentException(String.valueOf(value));
    }
  }
}

package com.zacharyhirsch.moldygameboy.emulator.memory.registers;

import com.zacharyhirsch.moldygameboy.emulator.arch.Register8;

public final class Svbk implements Register8 {

  private static final int MASK = 0b0000_0111;

  private byte value = 0;

  public Svbk() {}

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
}

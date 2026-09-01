package com.zacharyhirsch.moldygameboy.emulator.memory.registers;

import com.zacharyhirsch.moldygameboy.emulator.arch.Register8;

public final class Sb implements Register8 {

  public Sb() {}

  @Override
  public byte read() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void write(byte value) {
    System.out.printf("SB: %02x (%s)\n", value, (char) value);
  }

  public byte get() {
    throw new UnsupportedOperationException();
  }

  public void set(byte value) {
    throw new UnsupportedOperationException();
  }
}

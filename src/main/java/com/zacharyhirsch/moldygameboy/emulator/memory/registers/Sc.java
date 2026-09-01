package com.zacharyhirsch.moldygameboy.emulator.memory.registers;

import com.zacharyhirsch.moldygameboy.emulator.arch.Register8;

public final class Sc implements Register8 {

  public Sc() {}

  @Override
  public byte read() {
    return (byte) 0xff;
  }

  @Override
  public void write(byte value) {
    System.out.printf("SC: %02x (%s)\n", value, (char) value);
  }

  public byte get() {
    throw new UnsupportedOperationException();
  }

  public void set(byte value) {
    throw new UnsupportedOperationException();
  }
}

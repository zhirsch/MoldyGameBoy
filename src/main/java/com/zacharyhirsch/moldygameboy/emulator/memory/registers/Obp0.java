package com.zacharyhirsch.moldygameboy.emulator.memory.registers;

import com.zacharyhirsch.moldygameboy.emulator.arch.Register8;

public final class Obp0 implements Register8 {

  private byte value = 0;

  public Obp0() {}

  @Override
  public byte read() {
    return value;
  }

  @Override
  public void write(byte value) {
    this.value = value;
  }

  public byte get() {
    return value;
  }
}

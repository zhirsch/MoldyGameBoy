package com.zacharyhirsch.moldygameboy.emulator.memory.registers;

import com.zacharyhirsch.moldygameboy.emulator.arch.IORegister;

public final class Wy implements IORegister {

  private byte value = 0;

  public Wy() {}

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

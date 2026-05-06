package com.zacharyhirsch.moldygameboy.emulator.memory.registers;

import com.zacharyhirsch.moldygameboy.emulator.arch.IORegister;

public final class Scy implements IORegister {

  private byte value = 0;

  public Scy() {}

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

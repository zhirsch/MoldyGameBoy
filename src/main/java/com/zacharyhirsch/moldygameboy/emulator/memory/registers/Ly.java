package com.zacharyhirsch.moldygameboy.emulator.memory.registers;

import com.zacharyhirsch.moldygameboy.emulator.arch.IORegister;

public final class Ly implements IORegister {

  private byte value = 0;

  public Ly() {}

  @Override
  public byte read() {
    return value;
  }

  @Override
  public void write(byte value) {}

  public byte get() {
    return value;
  }

  public void set(byte value) {
    this.value = value;
  }
}

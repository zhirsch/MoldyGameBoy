package com.zacharyhirsch.moldygameboy.emulator.memory.registers;

import com.zacharyhirsch.moldygameboy.emulator.arch.IORegister;

public final class Bank implements IORegister {

  private byte value = 0;

  public Bank() {}

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

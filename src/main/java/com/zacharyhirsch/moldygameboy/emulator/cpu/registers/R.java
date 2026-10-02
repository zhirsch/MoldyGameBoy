package com.zacharyhirsch.moldygameboy.emulator.cpu.registers;

public abstract class R {

  private byte value = 0;

  public byte get() {
    return value;
  }

  public void set(byte value) {
    this.value = value;
  }
}

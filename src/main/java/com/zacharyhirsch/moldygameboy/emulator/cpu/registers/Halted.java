package com.zacharyhirsch.moldygameboy.emulator.cpu.registers;

public final class Halted {

  private boolean value = false;

  public boolean get() {
    return value;
  }

  public void set(boolean value) {
    this.value = value;
  }
}

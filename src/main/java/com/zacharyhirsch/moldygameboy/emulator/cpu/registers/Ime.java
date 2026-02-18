package com.zacharyhirsch.moldygameboy.emulator.cpu.registers;

public final class Ime {

  private boolean value = false;

  public boolean get() {
    return value;
  }

  public void set() {
    this.value = true;
  }

  public void reset() {
    this.value = false;
  }
}

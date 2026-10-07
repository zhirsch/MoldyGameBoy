package com.zacharyhirsch.moldygameboy.emulator.cpu.registers;

public final class Ime {

  private boolean value = false;
  private boolean pending = false;
  private int delay = 0;

  public boolean get() {
    return value;
  }

  public void set(boolean value, int delay) {
    this.pending = value;
    this.delay = delay;
  }

  public void tick() {
    if (delay == 0) {
      this.value = pending;
      return;
    }
    delay--;
  }
}

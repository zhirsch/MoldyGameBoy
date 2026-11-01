package com.zacharyhirsch.moldygameboy.emulator.cpu.registers;

public final class Ime {

  private boolean value = false;
  private boolean pending = false;
  private int delay = 0;

  public boolean get() {
    return value;
  }

  public void set(boolean value, int delay) {
    if (value && pending) {
      // An EI while an enable is already pending doesn't delay it further, so in a sequence of
      // EIs, IME is still set after the second one (see mooneye ei_sequence).
      this.delay = Math.min(this.delay, delay);
      return;
    }
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

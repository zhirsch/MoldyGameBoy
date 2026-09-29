package com.zacharyhirsch.moldygameboy.emulator.arch;

public final class Signal {

  private boolean value;

  public boolean get() {
    return value;
  }

  public void set(boolean signaled) {
    this.value = signaled;
  }
}

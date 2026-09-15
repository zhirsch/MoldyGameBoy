package com.zacharyhirsch.moldygameboy.emulator.arch;

public final class InterruptRequestLine {

  private boolean oldValue = false;
  private boolean asserted = false;

  public InterruptRequestLine() {}

  public boolean get() {
    boolean asserted = this.asserted;
    this.asserted = false;
    return asserted;
  }

  public void set(boolean newValue) {
    if (!oldValue && newValue) {
      asserted = true;
    }
    oldValue = newValue;
  }
}

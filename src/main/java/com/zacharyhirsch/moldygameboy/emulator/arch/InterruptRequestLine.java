package com.zacharyhirsch.moldygameboy.emulator.arch;

public final class InterruptRequestLine {

  private boolean value = false;
  private boolean asserted = false;

  public InterruptRequestLine() {}

  public boolean get() {
    boolean asserted = this.asserted;
    this.asserted = false;
    return asserted;
  }

  public void set(boolean value) {
    if (!this.value && value) {
      asserted = true;
    }
    this.value = value;
  }
}

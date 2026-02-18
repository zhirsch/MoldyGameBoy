package com.zacharyhirsch.moldygameboy.emulator.arch;

public final class Register1 {

  private boolean value;

  public Register1() {
    this.value = false;
  }

  public boolean get() {
    return value;
  }

  public void set(boolean value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return "Register1{%b}".formatted(value);
  }
}

package com.zacharyhirsch.moldygameboy.emulator.arch;

public final class Register8 {

  private byte value;

  public Register8() {
    this.value = 0;
  }

  public byte get() {
    return value;
  }

  public void set(byte value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return "Register8{%02x}".formatted(value);
  }
}

package com.zacharyhirsch.moldygameboy.emulator.arch;

public final class AddressBus {

  private short address;

  public short get() {
    return address;
  }

  public void set(short address) {
    this.address = address;
  }
}

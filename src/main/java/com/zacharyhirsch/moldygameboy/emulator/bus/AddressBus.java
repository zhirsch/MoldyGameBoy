package com.zacharyhirsch.moldygameboy.emulator.bus;

import com.zacharyhirsch.moldygameboy.emulator.arch.UInt16Input;
import com.zacharyhirsch.moldygameboy.emulator.arch.UInt16Output;

public final class AddressBus implements UInt16Input, UInt16Output {

  private short address;

  public AddressBus() {
    this.address = 0;
  }

  @Override
  public short get() {
    return address;
  }

  @Override
  public void set(short address) {
    this.address = address;
  }
}

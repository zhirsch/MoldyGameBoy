package com.zacharyhirsch.moldygameboy.emulator.bus;

import com.zacharyhirsch.moldygameboy.emulator.arch.UInt8Input;
import com.zacharyhirsch.moldygameboy.emulator.arch.UInt8Output;

public final class DataBus implements UInt8Input, UInt8Output {

  private byte value;

  public DataBus() {
    this.value = 0;
  }

  @Override
  public byte get() {
    return value;
  }

  @Override
  public void set(byte value) {
    this.value = value;
  }
}

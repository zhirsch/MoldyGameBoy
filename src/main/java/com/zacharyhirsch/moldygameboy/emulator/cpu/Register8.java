package com.zacharyhirsch.moldygameboy.emulator.cpu;

import com.zacharyhirsch.moldygameboy.emulator.arch.UInt8Input;
import com.zacharyhirsch.moldygameboy.emulator.arch.UInt8Output;

final class Register8 implements UInt8Input, UInt8Output {

  private byte value;

  Register8() {
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

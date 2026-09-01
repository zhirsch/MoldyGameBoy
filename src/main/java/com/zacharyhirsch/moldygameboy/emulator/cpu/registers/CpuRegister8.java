package com.zacharyhirsch.moldygameboy.emulator.cpu.registers;

import com.zacharyhirsch.moldygameboy.emulator.arch.Register8;

public final class CpuRegister8 implements Register8 {

  private byte value = 0;

  @Override
  public byte read() {
    return value;
  }

  @Override
  public void write(byte value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return "Register8{%02x}".formatted(value);
  }
}

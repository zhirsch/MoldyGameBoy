package com.zacharyhirsch.moldygameboy.emulator.cpu.registers;

import com.zacharyhirsch.moldygameboy.emulator.arch.Register8;

public final class Flag {

  private final Register8 register;
  private final int mask;

  Flag(Register8 register, int mask) {
    this.register = register;
    this.mask = mask;
  }

  public boolean get() {
    return (register.read() & mask) != 0;
  }

  public void set(boolean value) {
    register.write(value ? (byte) (register.read() | mask) : (byte) (register.read() & ~mask));
  }
}

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
    return (register.get() & mask) != 0;
  }

  public void set(boolean value) {
    register.set(value ? (byte) (register.get() | mask) : (byte) (register.get() & ~mask));
  }
}

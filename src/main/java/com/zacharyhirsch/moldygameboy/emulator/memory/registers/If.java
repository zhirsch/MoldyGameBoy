package com.zacharyhirsch.moldygameboy.emulator.memory.registers;

import com.zacharyhirsch.moldygameboy.emulator.arch.IORegister;
import com.zacharyhirsch.moldygameboy.emulator.arch.InterruptType;

public final class If implements IORegister {

  private static final int MASK = 0b0001_1111;

  private byte value = 0;

  public If() {}

  @Override
  public byte read() {
    return get();
  }

  @Override
  public void write(byte value) {
    this.value = (byte) ((this.value & ~MASK) | (value & MASK));
  }

  public byte get() {
    return (byte) ((value & MASK) | ~MASK);
  }

  public void request(InterruptType interruptType) {
    this.value = (byte) (this.value | interruptType.mask());
  }

  public void clear(InterruptType interruptType) {
    this.value = (byte) (this.value & ~interruptType.mask());
  }
}

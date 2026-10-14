package com.zacharyhirsch.moldygameboy.emulator.memory;

import com.zacharyhirsch.moldygameboy.emulator.arch.IoRegister;

public final class DivIoRegister implements IoRegister {

  private short counter = 0;

  @Override
  public int get() {
    return counter >>> 8;
  }

  @Override
  public void set(int value) {
    counter = 0;
  }

  @Override
  public boolean bit(int index) {
    return (counter & (1 << index)) != 0;
  }

  public void increment() {
    counter++;
  }

  @Override
  public String toString() {
    return "div @ ff04 = %02x [counter=%04x]".formatted(get(), counter);
  }
}

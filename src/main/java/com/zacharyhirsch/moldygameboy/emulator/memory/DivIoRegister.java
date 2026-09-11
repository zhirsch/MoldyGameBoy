package com.zacharyhirsch.moldygameboy.emulator.memory;

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

  public void increment() {
    counter++;
  }

  @Override
  public String toString() {
    return "div @ ff04 = %02x [counter=%04x]".formatted(get(), counter);
  }
}

package com.zacharyhirsch.moldygameboy.emulator.timer;

import com.zacharyhirsch.moldygameboy.emulator.memory.IORegisters;

public final class Divider {

  private final IORegisters ioRegisters;

  private int count = 0;

  public Divider(IORegisters ioRegisters) {
    this.ioRegisters = ioRegisters;
  }

  public void tick() {
    count = (count + 1) % 64;
    if (count == 0) {
      ioRegisters.div().set((byte) (ioRegisters.div().get() + 1));
    }
  }
}

package com.zacharyhirsch.moldygameboy.emulator.timer;

import com.zacharyhirsch.moldygameboy.emulator.memory.IORegisters;

public final class Timer {

  private final IORegisters ioRegisters;

  private int count = 0;

  public Timer(IORegisters ioRegisters) {
    this.ioRegisters = ioRegisters;
  }

  public void tick() {
    if ((ioRegisters.tac().get() & 0b0000_0100) == 0) {
      return;
    }
    int period =
        switch (ioRegisters.tac().get() & 0b0000_0011) {
          case 0 -> 256;
          case 1 -> 4;
          case 2 -> 16;
          case 3 -> 64;
          default -> throw new IllegalStateException();
        };
    count = (count + 1) % period;
    if (count != 0) {
      return;
    }
    if (ioRegisters.tima().get() == (byte) 0xff) {
      ioRegisters.if_().set((byte) (ioRegisters.if_().get() | 0b0000_0100));
      ioRegisters.tima().set(ioRegisters.tma().get());
      return;
    }
    ioRegisters.tima().set((byte) (ioRegisters.tima().get() + 1));
  }
}

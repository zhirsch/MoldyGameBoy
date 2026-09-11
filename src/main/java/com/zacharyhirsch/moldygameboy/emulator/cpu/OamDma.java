package com.zacharyhirsch.moldygameboy.emulator.cpu;

import com.zacharyhirsch.moldygameboy.emulator.arch.Register8;
import com.zacharyhirsch.moldygameboy.emulator.cpu.memory.CpuBus;

public final class OamDma {

  private int delay = 0;
  private int offset = 0;
  private short base = 0;

  private final CpuBus bus;

  public OamDma(CpuBus bus) {
    this.bus = bus;
  }

  public byte read() {
    // TODO: return the byte currently being transferred
    return (byte) 0xff;
  }

  public void write(byte baseHi) {
    delay = 1;
    offset = 0;
    base = (short) (baseHi << 8);
    bus.setOamDmaActive(true);
  }

  public void tick() {
    if (!bus.isOamDmaActive()) {
      return;
    }

    if (delay > 0) {
      delay--;
      return;
    }

    short src = (short) (base + offset);
    byte value = bus.read(src);

    bus.write((short) (0xfe00 + offset), value);

    offset++;

    if (offset == 160) {
      bus.setOamDmaActive(false);
    }
  }
}

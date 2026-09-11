package com.zacharyhirsch.moldygameboy.emulator.memory;

public final class EchoRam implements MemoryRegion {

  private static final int BASE = 0xe000;
  private static final int SIZE = 0x1e00;

  private final Wram wram;

  public EchoRam(Wram wram) {
    this.wram = wram;
  }

  @Override
  public byte read(short address) {
    return wram.read((short) (address & 0xdfff));
  }

  @Override
  public void write(short address, byte value) {
    wram.write((short) (address & 0xdfff), value);
  }
}

package com.zacharyhirsch.moldygameboy.emulator.memory;

public class OpenBus implements MemoryRegion {

  @Override
  public byte read(short address) {
    return (byte) 0xff;
  }

  @Override
  public void write(short address, byte value) {}
}

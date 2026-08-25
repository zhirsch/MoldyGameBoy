package com.zacharyhirsch.moldygameboy.emulator.memory.registers;

import com.zacharyhirsch.moldygameboy.emulator.arch.IORegister;

public final class Lcdc implements IORegister {

  private byte value = 0;

  public Lcdc() {}

  @Override
  public byte read() {
    return value;
  }

  @Override
  public void write(byte value) {
    this.value = value;
  }

  public short getBackgroundTileIdBase() {
    return (short) ((value & 0b0000_1000) != 0 ? 0x9c00 : 0x9800);
  }

  public short getWindowTileIdBase() {
    return (short) ((value & 0b0100_0000) != 0 ? 0x9c00 : 0x9800);
  }

  public short getTileDataBase() {
    return (short) (isTileDataBase8000() ? 0x8000 : 0x8800);
  }

  public boolean isBackgroundEnabled() {
    return (value & 0b0000_0001) != 0;
  }

  public boolean isTileDataBase8000() {
    return (value & 0b0001_0000) != 0;
  }

  public boolean isWindowEnabled() {
    return (value & 0b0010_0000) != 0;
  }
}

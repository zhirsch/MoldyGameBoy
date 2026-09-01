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

  public boolean isBackgroundEnabled() {
    return bit(0);
  }

  public boolean isBackgroundTileMapBase9c00() {
    return bit(3);
  }

  public boolean isTileDataBase8000() {
    return bit(4);
  }

  public boolean isWindowEnabled() {
    return bit(5);
  }

  public boolean isWindowTileMapBase9c00() {
    return bit(6);
  }

  private boolean bit(int i) {
    return (value & (1 << i)) != 0;
  }
}

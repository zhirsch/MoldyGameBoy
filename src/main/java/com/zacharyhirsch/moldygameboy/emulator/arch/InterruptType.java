package com.zacharyhirsch.moldygameboy.emulator.arch;

public enum InterruptType {
  VBLANK((byte) 0b0000_0001, (short) 0x0040),
  LCD((byte) 0b0000_0010, (short) 0x0048),
  TIMER((byte) 0b0000_0100, (short) 0x0050),
  SERIAL((byte) 0b0000_1000, (short) 0x0058),
  JOYPAD((byte) 0b0001_0000, (short) 0x0060),
  ;

  private final byte mask;
  private final short vector;

  InterruptType(byte mask, short vector) {
    this.mask = mask;
    this.vector = vector;
  }

  public byte mask() {
    return mask;
  }

  public short vector() {
    return vector;
  }
}

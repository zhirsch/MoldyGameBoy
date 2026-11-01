package com.zacharyhirsch.moldygameboy.emulator.io;

public record Color(byte r, byte g, byte b) {

  public int rgb() {
    return (Byte.toUnsignedInt(r()) << 16)
        | (Byte.toUnsignedInt(g()) << 8)
        | Byte.toUnsignedInt(b());
  }
}

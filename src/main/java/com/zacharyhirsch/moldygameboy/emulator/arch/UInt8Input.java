package com.zacharyhirsch.moldygameboy.emulator.arch;

public interface UInt8Input {

  byte get();

  static UInt8Input of(byte value) {
    return () -> value;
  }

  static UInt8Input of(int value) {
    return of((byte) value);
  }
}

package com.zacharyhirsch.moldygameboy.emulator.arch;

public interface UInt16Input {

  short get();

  static UInt16Input of(short value) {
    return () -> value;
  }

  static UInt16Input of(int value) {
    return of((short) value);
  }

  static UInt16Input of(byte hi, byte lo) {
    return of((hi << 8) | lo);
  }
}

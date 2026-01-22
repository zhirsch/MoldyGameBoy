package com.zacharyhirsch.moldygameboy.emulator.arch;

public interface UInt8Output {

  void set(byte value);

  default void set(int value) {
    set((byte) value);
  }
}

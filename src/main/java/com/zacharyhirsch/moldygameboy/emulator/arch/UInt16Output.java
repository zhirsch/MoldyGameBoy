package com.zacharyhirsch.moldygameboy.emulator.arch;

public interface UInt16Output {

  void set(short value);

  default void set(byte hi, byte lo) {
    set((short) ((hi << 8) | lo));
  }
}

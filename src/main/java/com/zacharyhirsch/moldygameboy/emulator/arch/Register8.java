package com.zacharyhirsch.moldygameboy.emulator.arch;

public interface Register8 {

  byte read();

  void write(byte value);
}

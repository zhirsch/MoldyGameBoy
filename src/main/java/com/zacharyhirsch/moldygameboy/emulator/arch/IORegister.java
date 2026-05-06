package com.zacharyhirsch.moldygameboy.emulator.arch;

public interface IORegister {

  byte read();

  void write(byte value);
}

package com.zacharyhirsch.moldygameboy.emulator.cpu.registers;

public final class Pc extends RR {

  Pc() {
    super(new Pch(), new Pcl());
  }

  private static final class Pch extends R {}

  private static final class Pcl extends R {}
}

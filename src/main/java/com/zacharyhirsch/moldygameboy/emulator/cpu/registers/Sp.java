package com.zacharyhirsch.moldygameboy.emulator.cpu.registers;

public final class Sp extends RR {

  Sp() {
    super(new Sph(), new Spl());
  }

  private static final class Sph extends R {}

  private static final class Spl extends R {}
}

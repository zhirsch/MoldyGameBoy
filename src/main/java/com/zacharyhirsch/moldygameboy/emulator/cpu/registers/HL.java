package com.zacharyhirsch.moldygameboy.emulator.cpu.registers;

public final class HL extends RR {

  HL() {
    super(new H(), new L());
  }

  @Override
  public H hi() {
    return (H) super.hi();
  }

  @Override
  public L lo() {
    return (L) super.lo();
  }
}

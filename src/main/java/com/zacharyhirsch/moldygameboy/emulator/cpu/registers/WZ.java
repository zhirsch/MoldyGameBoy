package com.zacharyhirsch.moldygameboy.emulator.cpu.registers;

public final class WZ extends RR {

  WZ() {
    super(new W(), new Z());
  }

  @Override
  public W hi() {
    return (W) super.hi();
  }

  @Override
  public Z lo() {
    return (Z) super.lo();
  }
}

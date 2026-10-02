package com.zacharyhirsch.moldygameboy.emulator.cpu.registers;

public final class BC extends RR {

  BC() {
    super(new B(), new C());
  }

  @Override
  public B hi() {
    return (B) super.hi();
  }

  @Override
  public C lo() {
    return (C) super.lo();
  }
}

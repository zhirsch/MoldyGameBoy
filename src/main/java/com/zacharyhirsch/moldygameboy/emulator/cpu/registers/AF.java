package com.zacharyhirsch.moldygameboy.emulator.cpu.registers;

public final class AF extends RR {

  AF() {
    super(new A(), new F());
  }

  @Override
  public A hi() {
    return (A) super.hi();
  }

  @Override
  public F lo() {
    return (F) super.lo();
  }
}

package com.zacharyhirsch.moldygameboy.emulator.cpu.registers;

public final class DE extends RR {

  DE() {
    super(new D(), new E());
  }

  @Override
  public D hi() {
    return (D) super.hi();
  }

  @Override
  public E lo() {
    return (E) super.lo();
  }
}

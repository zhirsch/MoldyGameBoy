package com.zacharyhirsch.moldygameboy.emulator.cpu.registers;

import com.zacharyhirsch.moldygameboy.emulator.arch.Register8;

public final class FlagsRegister implements Register8 {

  private static final int Z = 0b1000_0000;
  private static final int N = 0b0100_0000;
  private static final int H = 0b0010_0000;
  private static final int C = 0b0001_0000;

  private final Register8 register;
  private final Flag z;
  private final Flag n;
  private final Flag h;
  private final Flag c;

  FlagsRegister() {
    this.register = new CpuRegister8();
    this.z = new Flag(register, Z);
    this.n = new Flag(register, N);
    this.h = new Flag(register, H);
    this.c = new Flag(register, C);
  }

  public byte read() {
    return (byte) (register.read() & 0xf0);
  }

  public void write(byte value) {
    register.write(value);
  }

  public Flag z() {
    return z;
  }

  public Flag n() {
    return n;
  }

  public Flag h() {
    return h;
  }

  public Flag c() {
    return c;
  }
}

package com.zacharyhirsch.moldygameboy.emulator.cpu.registers;

import java.util.Objects;

public abstract class RR {

  private final R hi;
  private final R lo;

  protected RR(R hi, R lo) {
    this.hi = hi;
    this.lo = lo;
  }

  public R hi() {
    return hi;
  }

  public R lo() {
    return lo;
  }

  public short get() {
    return (short) ((Byte.toUnsignedInt(hi.get()) << 8) | (Byte.toUnsignedInt(lo.get()) << 0));
  }

  public void set(short value) {
    hi.set((byte) ((Short.toUnsignedInt(value) >>> 8) & 0xff));
    lo.set((byte) ((Short.toUnsignedInt(value) >>> 0) & 0xff));
  }

  public void increment() {
    set((short) (get() + 1));
  }

  public void decrement() {
    set((short) (get() - 1));
  }

  @Override
  public String toString() {
    return "Register16{%04x}".formatted(get());
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (obj == null || obj.getClass() != this.getClass()) return false;
    var that = (RR) obj;
    return Objects.equals(this.hi, that.hi) && Objects.equals(this.lo, that.lo);
  }

  @Override
  public int hashCode() {
    return Objects.hash(hi, lo);
  }
}

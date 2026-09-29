package com.zacharyhirsch.moldygameboy.emulator.cpu.registers;

public record RR(R hi, R lo) {

  public RR() {
    this(new R(), new R());
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

  public short getAndIncrement() {
    short value = get();
    increment();
    return value;
  }

  public short getAndDecrement() {
    short value = get();
    decrement();
    return value;
  }

  @Override
  public String toString() {
    return "Register16{%04x}".formatted(get());
  }
}

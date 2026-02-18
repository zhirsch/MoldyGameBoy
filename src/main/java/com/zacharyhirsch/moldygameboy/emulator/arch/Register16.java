package com.zacharyhirsch.moldygameboy.emulator.arch;

public record Register16<Hi extends UInt8, Lo extends UInt8>(Hi hi, Lo lo) implements UInt16 {

  public short getAndIncrement() {
    short value = get();
    set((short) (value + 1));
    return value;
  }

  public short getAndDecrement() {
    short value = get();
    set((short) (value - 1));
    return value;
  }

  @Override
  public short get() {
    return (short) ((Byte.toUnsignedInt(hi.get()) << 8) | Byte.toUnsignedInt(lo.get()));
  }

  public void set(short value) {
    hi.set((byte) ((value & 0xff00) >>> 8));
    lo.set((byte) ((value & 0x00ff) >>> 0));
  }

  public void set(byte hi, byte lo) {
    this.hi.set(hi);
    this.lo.set(lo);
  }

  @Override
  public String toString() {
    return "Register16{%04x}".formatted(get());
  }
}

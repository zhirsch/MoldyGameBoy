package com.zacharyhirsch.moldygameboy.emulator.arch;

public record Register16<Hi extends Register8, Lo extends Register8>(Hi hi, Lo lo) {

  public short getAndIncrement() {
    short value = read();
    write((short) (value + 1));
    return value;
  }

  public short getAndDecrement() {
    short value = read();
    write((short) (value - 1));
    return value;
  }

  public short read() {
    return (short) ((Byte.toUnsignedInt(hi.read()) << 8) | Byte.toUnsignedInt(lo.read()));
  }

  public void write(short value) {
    hi.write((byte) ((value & 0xff00) >>> 8));
    lo.write((byte) ((value & 0x00ff) >>> 0));
  }

  public void write(byte hi, byte lo) {
    this.hi.write(hi);
    this.lo.write(lo);
  }

  @Override
  public String toString() {
    return "Register16{%04x}".formatted(read());
  }
}

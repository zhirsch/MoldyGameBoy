package com.zacharyhirsch.moldygameboy.emulator.arch;

public interface Memory {

  enum Register {
    DIV(0x04),
    TIMA(0x05),
    TMA(0x06),
    TAC(0x07),
    IF(0x0f),
    BANK(0x50),
    IE(0xff);

    private final int index;

    Register(int index) {
      this.index = index;
    }

    public int index() {
      return index;
    }
  }

  byte read(short address);

  default byte read(Register register) {
    return read((short) (0xff00 | register.index()));
  }

  void write(short address, byte data);

  default void write(Register register, byte data) {
    write((short) (0xff00 | register.index()), data);
  }

  void none(short address);
}

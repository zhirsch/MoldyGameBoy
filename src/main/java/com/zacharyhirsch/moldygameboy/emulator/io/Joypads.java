package com.zacharyhirsch.moldygameboy.emulator.io;

public interface Joypads extends AutoCloseable {

  enum Button {
    RIGHT((byte) 0b1000_0000),
    LEFT((byte) 0b0100_0000),
    DOWN((byte) 0b0010_0000),
    UP((byte) 0b0001_0000),
    START((byte) 0b0000_1000),
    SELECT((byte) 0b0000_0100),
    BUTTON_B((byte) 0b0000_0010),
    BUTTON_A((byte) 0b0000_0001),
    ;

    private final byte value;

    Button(byte value) {
      this.value = value;
    }

    public byte getValue() {
      return value;
    }
  }

  byte readJoypad(int index);

  void writeJoypads(byte data);
}

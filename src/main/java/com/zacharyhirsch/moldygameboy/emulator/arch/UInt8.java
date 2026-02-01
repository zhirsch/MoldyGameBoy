package com.zacharyhirsch.moldygameboy.emulator.arch;

public interface UInt8 {

  UInt8 ZERO = UInt8.constant(0);

  static UInt8 constant(int value) {
    return constant((byte) value);
  }

  static UInt8 constant(byte value) {
    return new UInt8() {
      @Override
      public byte get() {
        return value;
      }

      @Override
      public void set(byte value) {
        throw new UnsupportedOperationException();
      }
    };
  }

  byte get();

  void set(byte value);
}

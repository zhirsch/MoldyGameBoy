package com.zacharyhirsch.moldygameboy.emulator.arch;

public interface UInt16 {

  UInt16 ZERO = UInt16.constant(0);

  static UInt16 constant(int value) {
    return constant((short) value);
  }

  static UInt16 constant(short value) {
    return new UInt16() {
      @Override
      public short get() {
        return value;
      }

      @Override
      public void set(short value) {
        throw new UnsupportedOperationException();
      }
    };
  }

  static UInt16 from(UInt8 hi, UInt8 lo) {
    return new UInt16() {
      @Override
      public short get() {
        return (short) ((hi.get() << 8) | lo.get());
      }

      @Override
      public void set(short value) {
        throw new UnsupportedOperationException();
      }
    };
  }

  short get();

  void set(short value);
}

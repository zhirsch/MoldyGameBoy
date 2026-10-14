package com.zacharyhirsch.moldygameboy.emulator.arch;

public interface IoRegister {

  int get();

  void set(int value);

  default boolean bit(int index) {
    return (get() & (1 << index)) != 0;
  }

  static IoRegister create(String name, short address) {
    return new IoRegister() {

      private byte value = 0;

      @Override
      public int get() {
        return Byte.toUnsignedInt(value);
      }

      @Override
      public void set(int value) {
        this.value = (byte) value;
      }

      @Override
      public String toString() {
        return "%s @ %04x = %02x".formatted(name, address, value);
      }
    };
  }
}

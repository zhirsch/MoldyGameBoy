package com.zacharyhirsch.moldygameboy.emulator.arch;

import java.util.function.Consumer;
import java.util.function.Supplier;

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

  static IoRegister wrap(String name, short address, Supplier<Byte> getter, Consumer<Byte> setter) {
    return new IoRegister() {
      @Override
      public int get() {
        return Byte.toUnsignedInt(getter.get());
      }

      @Override
      public void set(int value) {
        setter.accept((byte) value);
      }

      @Override
      public String toString() {
        return "%s @ %04x = %02x".formatted(name, address, getter.get());
      }
    };
  }
}

package com.zacharyhirsch.moldygameboy.emulator.cpu.alu;

import com.google.auto.value.AutoBuilder;

public final class Alu {

  private Alu() {}

  public record Result<T extends Number>(T result, boolean z, boolean n, boolean h, boolean c) {

    static Builder<Byte> builder(byte result) {
      return new AutoBuilder_Alu_Result_Builder<Byte>().result(result).z(result == 0);
    }

    static Builder<Short> builder(short result) {
      return new AutoBuilder_Alu_Result_Builder<Short>().result(result).z(result == 0);
    }

    @AutoBuilder
    public interface Builder<T extends Number> {

      Builder<T> result(T result);

      Builder<T> z(boolean z);

      Builder<T> n(boolean n);

      Builder<T> h(boolean h);

      Builder<T> c(boolean c);

      Result<T> build();
    }
  }

  public static Result<Byte> adc(byte lhs, byte rhs, boolean carry) {
    int c = carry ? 1 : 0;
    return Result.builder((byte) (Byte.toUnsignedInt(lhs) + Byte.toUnsignedInt(rhs) + c))
        .n(false)
        .h((Byte.toUnsignedInt(lhs) & 0x0f) + (Byte.toUnsignedInt(rhs) & 0x0f) + c > 0x0f)
        .c((Byte.toUnsignedInt(lhs) & 0xff) + (Byte.toUnsignedInt(rhs) & 0xff) + c > 0xff)
        .build();
  }

  public static Result<Byte> add(byte lhs, byte rhs) {
    return adc(lhs, rhs, false);
  }

  public static Result<Short> add16(short lhs, short rhs) {
    return Result.builder((short) (Short.toUnsignedInt(lhs) + Short.toUnsignedInt(rhs)))
        .n(false)
        .h((Short.toUnsignedInt(lhs) & 0x0fff) + (Short.toUnsignedInt(rhs) & 0x0fff) > 0x0fff)
        .c((Short.toUnsignedInt(lhs) & 0xffff) + (Short.toUnsignedInt(rhs) & 0xffff) > 0xffff)
        .build();
  }

  public static Result<Short> add16(short lhs, byte rhs) {
    return Result.builder((short) (Short.toUnsignedInt(lhs) + (int) rhs)) // sign extend rhs
        .z(false)
        .n(false)
        .h((Short.toUnsignedInt(lhs) & 0x000f) + (Byte.toUnsignedInt(rhs) & 0x0f) > 0x000f)
        .c((Short.toUnsignedInt(lhs) & 0x00ff) + (Byte.toUnsignedInt(rhs) & 0xff) > 0x00ff)
        .build();
  }

  public static Result<Byte> inc(byte value) {
    return adc(value, (byte) 1, false);
  }

  public static Result<Byte> and(byte lhs, byte rhs) {
    byte result = (byte) (lhs & rhs);
    return Result.builder(result).n(false).h(true).c(false).build();
  }

  public static Result<Byte> bit(byte value, int index) {
    byte result = (byte) (value & (1 << index));
    return Result.builder(result).n(false).h(true).c(false).build();
  }

  public static Result<Byte> ccf(boolean carry) {
    byte result = (byte) 0;
    return Result.builder(result).n(false).h(false).c(!carry).build();
  }

  public static Result<Byte> cpl(byte value) {
    byte result = (byte) ~value;
    return Result.builder(result).n(true).h(true).c(false).build();
  }

  public static Result<Byte> or(byte lhs, byte rhs) {
    byte result = (byte) (lhs | rhs);
    return Result.builder(result).n(false).h(false).c(false).build();
  }

  public static Result<Byte> res(byte value, int index) {
    byte result = (byte) (value & ~(1 << index));
    return Result.builder(result).n(false).h(false).c(false).build();
  }

  public static Result<Byte> rl(byte value, boolean carry) {
    byte c = (byte) (carry ? 0x01 : 0x00);
    byte result = (byte) ((Byte.toUnsignedInt(value) << 1) | c);
    return Result.builder(result).n(false).h(false).c((value & 0x80) != 0).build();
  }

  public static Result<Byte> rr(byte value, boolean carry) {
    byte c = (byte) (carry ? 0x80 : 0x00);
    byte result = (byte) ((Byte.toUnsignedInt(value) >>> 1) | c);
    return Result.builder(result).n(false).h(false).c((value & 0x01) != 0).build();
  }

  public static Result<Byte> set(byte value, int index) {
    byte result = (byte) (value | (1 << index));
    return Result.builder(result).n(false).h(false).c(false).build();
  }

  public static Result<Byte> sub(byte lhs, byte rhs, boolean carry) {
    byte c = (byte) (carry ? 1 : 0);
    byte result = (byte) (lhs - rhs - c);
    return Result.builder(result)
        .n(true)
        .h((lhs & 0x0f) - (rhs & 0x0f) - c < 0)
        .c(Byte.toUnsignedInt(lhs) - Byte.toUnsignedInt(rhs) - c < 0)
        .build();
  }

  public static Result<Byte> swap(byte value) {
    byte l = (byte) (value & 0x0f);
    byte h = (byte) (value & 0xf0);
    byte result = (byte) ((Byte.toUnsignedInt(l) << 4) | (Byte.toUnsignedInt(h) >> 4));
    return Result.builder(result).n(false).h(false).c(false).build();
  }

  public static Result<Byte> xor(byte lhs, byte rhs) {
    byte result = (byte) (lhs ^ rhs);
    return Result.builder(result).n(false).h(false).c(false).build();
  }
}

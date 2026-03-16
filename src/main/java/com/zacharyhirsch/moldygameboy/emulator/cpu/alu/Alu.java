package com.zacharyhirsch.moldygameboy.emulator.cpu.alu;

import com.google.auto.value.AutoBuilder;

public final class Alu {

  private Alu() {}

  public record Result(byte result, boolean z, boolean n, boolean h, boolean c) {

    static Builder builder(byte result) {
      return new AutoBuilder_Alu_Result_Builder().result(result).z(result == 0);
    }

    @AutoBuilder
    public interface Builder {

      Builder result(byte result);

      Builder z(boolean z);

      Builder n(boolean n);

      Builder h(boolean h);

      Builder c(boolean c);

      Result build();
    }
  }

  public static Result add(byte lhs, byte rhs, boolean carry) {
    byte c = (byte) (carry ? 1 : 0);
    byte result = (byte) (lhs + rhs + c);
    return Result.builder(result)
        .n(false)
        .h((lhs & 0x0f) + (rhs & 0x0f) + c > 0x0f)
        .c(Byte.toUnsignedInt(lhs) + Byte.toUnsignedInt(rhs) + c > 0xff)
        .build();
  }

  public static Result and(byte lhs, byte rhs) {
    byte result = (byte) (lhs & rhs);
    return Result.builder(result).n(false).h(true).c(false).build();
  }

  public static Result bit(byte value, int index) {
    byte result = (byte) (value & (1 << index));
    return Result.builder(result).n(false).h(true).c(false).build();
  }

  public static Result ccf(boolean carry) {
    byte result = (byte) 0;
    return Result.builder(result).n(false).h(false).c(!carry).build();
  }

  public static Result cpl(byte value) {
    byte result = (byte) ~value;
    return Result.builder(result).n(true).h(true).c(false).build();
  }

  public static Result or(byte lhs, byte rhs) {
    byte result = (byte) (lhs | rhs);
    return Result.builder(result).n(false).h(false).c(false).build();
  }

  public static Result res(byte value, int index) {
    byte result = (byte) (value & ~(1 << index));
    return Result.builder(result).n(false).h(false).c(false).build();
  }

  public static Result rl(byte value, boolean carry) {
    byte c = (byte) (carry ? 0x01 : 0x00);
    byte result = (byte) ((Byte.toUnsignedInt(value) << 1) | c);
    return Result.builder(result).n(false).h(false).c((value & 0x80) != 0).build();
  }

  public static Result rr(byte value, boolean carry) {
    byte c = (byte) (carry ? 0x80 : 0x00);
    byte result = (byte) ((Byte.toUnsignedInt(value) >>> 1) | c);
    return Result.builder(result).n(false).h(false).c((value & 0x01) != 0).build();
  }

  public static Result set(byte value, int index) {
    byte result = (byte) (value | (1 << index));
    return Result.builder(result).n(false).h(false).c(false).build();
  }

  public static Result sub(byte lhs, byte rhs, boolean carry) {
    byte c = (byte) (carry ? 1 : 0);
    byte result = (byte) (lhs - rhs - c);
    return Result.builder(result)
        .n(true)
        .h((lhs & 0x0f) - (rhs & 0x0f) - c < 0)
        .c(Byte.toUnsignedInt(lhs) - Byte.toUnsignedInt(rhs) - c < 0)
        .build();
  }

  public static Result swap(byte value) {
    byte l = (byte) (value & 0x0f);
    byte h = (byte) (value & 0xf0);
    byte result = (byte) ((Byte.toUnsignedInt(l) << 4) | (Byte.toUnsignedInt(h) >> 4));
    return Result.builder(result).n(false).h(false).c(false).build();
  }

  public static Result xor(byte lhs, byte rhs) {
    byte result = (byte) (lhs ^ rhs);
    return Result.builder(result).n(false).h(false).c(false).build();
  }
}

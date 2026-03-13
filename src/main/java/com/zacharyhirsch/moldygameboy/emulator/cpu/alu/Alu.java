package com.zacharyhirsch.moldygameboy.emulator.cpu.alu;

import com.google.auto.value.AutoBuilder;

public final class Alu {

  private Alu() {}

  public record Result(byte result, boolean z, boolean n, boolean h, boolean c) {

    public static Builder builder() {
      return new AutoBuilder_Alu_Result_Builder();
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

  public static Result add(byte lhs, byte rhs) {
    return adc(lhs, rhs, false);
  }

  public static Result adc(byte lhs, byte rhs, boolean carry) {
    byte c = (byte) (carry ? 1 : 0);
    byte result = (byte) (lhs + rhs + c);
    return Result.builder()
        .result(result)
        .z(result == 0)
        .n(false)
        .h((lhs & 0x0f) + (rhs & 0x0f) + c > 0x0f)
        .c(Byte.toUnsignedInt(lhs) + Byte.toUnsignedInt(rhs) + c > 0xff)
        .build();
  }

  public static Result and(byte lhs, byte rhs) {
    byte result = (byte) (lhs & rhs);
    return new Result(result, result == 0, false, true, false);
  }

  public static Result bit(byte value, int index) {
    byte result = (byte) (value & (1 << index));
    return new Result(result, result == 0, false, true, false);
  }

  public static Result ccf(boolean carry) {
    byte result = (byte) (1 - (carry ? 1 : 0));
    return new Result(result, result == 0, false, false, !carry);
  }

  public static Result cpl(byte value) {
    return sbc((byte) 0, value, true);
  }

  public static Result or(byte lhs, byte rhs) {
    byte result = (byte) (lhs | rhs);
    return new Result(result, result == 0, false, false, false);
  }

  public static Result rla(byte value, boolean carry) {
    boolean b7 = (value & 0x80) != 0;
    return Result.builder()
        .result((byte) ((Byte.toUnsignedInt(value) << 1) | (carry ? 0x01 : 0)))
        .z(false)
        .n(false)
        .h(false)
        .c(b7)
        .build();
  }

  public static Result rlc(byte value) {
    boolean b7 = (value & 0x80) != 0;
    return Result.builder()
        .result((byte) ((Byte.toUnsignedInt(value) << 1) | (b7 ? 0x01 : 0)))
        .z(false)
        .n(false)
        .h(false)
        .c(b7)
        .build();
  }

  public static Result rrc(byte value) {
    boolean b0 = (value & 0x01) != 0;
    return Result.builder()
        .result((byte) ((Byte.toUnsignedInt(value) >>> 1) | (b0 ? 0x80 : 0)))
        .z(false)
        .n(false)
        .h(false)
        .c(b0)
        .build();
  }

  public static Result sub(byte lhs, byte rhs) {
    return sbc(lhs, rhs, false);
  }

  public static Result sbc(byte lhs, byte rhs, boolean carry) {
    byte c = (byte) (carry ? 1 : 0);
    byte result = (byte) (lhs - rhs - c);
    return Result.builder()
        .result(result)
        .z(result == 0)
        .n(true)
        .h((lhs & 0x0f) - (rhs & 0x0f) - c < 0)
        .c(Byte.toUnsignedInt(lhs) - Byte.toUnsignedInt(rhs) - c < 0)
        .build();
  }
}

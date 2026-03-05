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

  public static Result add(byte lhs, byte rhs, boolean carry) {
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
    return sub((byte) 0, value, (byte) 1);
  }

  public static Result or(byte lhs, byte rhs) {
    byte result = (byte) (lhs | rhs);
    return new Result(result, result == 0, false, false, false);
  }

  public static Result sub(byte lhs, byte rhs, byte carry) {
    byte result = (byte) (lhs - rhs - carry);
    boolean h = (lhs & 0x0f) - (rhs & 0x0f) - carry < 0;
    boolean c = Byte.toUnsignedInt(lhs) - Byte.toUnsignedInt(rhs) - carry < 0;
    return new Result(result, result == 0, true, h, c);
  }
}

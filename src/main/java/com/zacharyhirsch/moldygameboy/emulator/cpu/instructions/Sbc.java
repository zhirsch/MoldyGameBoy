package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.arch.Register8;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;


public final class Sbc {

  public static final class Register extends AbstractInstruction {

    private final Registers registers;
    private final Register8 register;

    public Register(Registers registers, Register8 register) {
      this.registers = registers;
      this.register = register;
    }

    @Override
    protected Mem execute0(byte data) {
      byte result = sub(registers.a().get(), register.get(), registers.f().getC() ? 1 : 0);
      registers.a().set(result);
      registers.f().setZ(result == 0);
      return Mem.read(registers.pc().getAndIncrement());
    }

    @Override
    protected Mem execute1(byte data) {
      registers.ir().set(data);
      return null;
    }

    private byte sub(byte lhs, byte rhs, int carry) {
      registers.f().setN(true);
      registers.f().setH((lhs & 0x0f) - (rhs & 0x0f) - carry < 0);
      registers.f().setC(Byte.toUnsignedInt(lhs) - Byte.toUnsignedInt(rhs) - carry < 0);
      return (byte) (lhs - rhs - carry);
    }
  }

  public static final class Indirect extends AbstractInstruction {

    private final Registers registers;

    public Indirect(Registers registers) {
      this.registers = registers;
    }

    @Override
    protected Mem execute0(byte data) {
      return Mem.read(registers.hl().get());
    }

    @Override
    protected Mem execute1(byte data) {
      byte result = sub(registers.a().get(), data, registers.f().getC() ? 1 : 0);
      registers.a().set(result);
      registers.f().setZ(result == 0);
      return Mem.read(registers.pc().getAndIncrement());
    }

    @Override
    protected Mem execute2(byte data) {
      registers.ir().set(data);
      return null;
    }

    private byte sub(byte lhs, byte rhs, int carry) {
      registers.f().setN(true);
      registers.f().setH((lhs & 0x0f) - (rhs & 0x0f) - carry < 0);
      registers.f().setC(Byte.toUnsignedInt(lhs) - Byte.toUnsignedInt(rhs) - carry < 0);
      return (byte) (lhs - rhs - carry);
    }
  }

  public static final class Immediate extends AbstractInstruction {

    private final Registers registers;

    public Immediate(Registers registers) {
      this.registers = registers;
    }

    @Override
    protected Mem execute0(byte data) {
      return Mem.read(registers.pc().getAndIncrement());
    }

    @Override
    protected Mem execute1(byte data) {
      byte result = sub(registers.a().get(), data, registers.f().getC() ? 1 : 0);
      registers.a().set(result);
      registers.f().setZ(result == 0);
      return Mem.read(registers.pc().getAndIncrement());
    }

    @Override
    protected Mem execute2(byte data) {
      registers.ir().set(data);
      return null;
    }

    private byte sub(byte lhs, byte rhs, int carry) {
      registers.f().setN(true);
      registers.f().setH((lhs & 0x0f) - (rhs & 0x0f) - carry < 0);
      registers.f().setC(Byte.toUnsignedInt(lhs) - Byte.toUnsignedInt(rhs) - carry < 0);
      return (byte) (lhs - rhs - carry);
    }
  }
}

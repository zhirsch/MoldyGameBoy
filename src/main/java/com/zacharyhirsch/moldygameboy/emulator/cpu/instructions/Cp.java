package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.arch.Register8;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import com.zacharyhirsch.moldygameboy.emulator.memory.MemOperation;
import com.zacharyhirsch.moldygameboy.emulator.memory.MemRead;

public final class Cp {

  private Cp() {}

  public static final class Indirect extends AbstractInstruction {

    private final Registers registers;

    public Indirect(Registers registers) {
      this.registers = registers;
    }

    @Override
    protected MemOperation execute0(byte data) {
      return new MemRead(registers.hl().get());
    }

    @Override
    protected MemOperation execute1(byte data) {
      sub(registers.a().get(), data, 0);
      return new MemRead(registers.pc().getAndIncrement());
    }

    @Override
    protected MemOperation execute2(byte data) {
      registers.ir().set(data);
      return null;
    }

    private byte sub(byte lhs, byte rhs, int carry) {
      registers.f().setZ(lhs - rhs - carry == 0);
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
    protected MemOperation execute0(byte data) {
      return new MemRead(registers.pc().getAndIncrement());
    }

    @Override
    protected MemOperation execute1(byte data) {
      sub(registers.a().get(), data, 0);
      return new MemRead(registers.pc().getAndIncrement());
    }

    @Override
    protected MemOperation execute2(byte data) {
      registers.ir().set(data);
      return null;
    }

    private byte sub(byte lhs, byte rhs, int carry) {
      registers.f().setZ(lhs - rhs - carry == 0);
      registers.f().setN(true);
      registers.f().setH((lhs & 0x0f) - (rhs & 0x0f) - carry < 0);
      registers.f().setC(Byte.toUnsignedInt(lhs) - Byte.toUnsignedInt(rhs) - carry < 0);
      return (byte) (lhs - rhs - carry);
    }
  }

  public static final class Register extends AbstractInstruction {

    private final Registers registers;
    private final Register8 rhs;

    public Register(Registers registers, Register8 rhs) {
      this.registers = registers;
      this.rhs = rhs;
    }

    @Override
    protected MemOperation execute0(byte data) {
      sub(registers.a().get(), rhs.get(), 0);
      return new MemRead(registers.pc().getAndIncrement());
    }

    @Override
    protected MemOperation execute1(byte data) {
      registers.ir().set(data);
      return null;
    }

    private byte sub(byte lhs, byte rhs, int carry) {
      registers.f().setZ(lhs - rhs - carry == 0);
      registers.f().setN(true);
      registers.f().setH((lhs & 0x0f) - (rhs & 0x0f) - carry < 0);
      registers.f().setC(Byte.toUnsignedInt(lhs) - Byte.toUnsignedInt(rhs) - carry < 0);
      return (byte) (lhs - rhs - carry);
    }
  }
}

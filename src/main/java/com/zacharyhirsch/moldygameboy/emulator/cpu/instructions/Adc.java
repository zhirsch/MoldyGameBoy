package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.arch.Register8;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import com.zacharyhirsch.moldygameboy.emulator.memory.MemOperation;
import com.zacharyhirsch.moldygameboy.emulator.memory.MemRead;

public final class Adc {

  public static final class Register extends AbstractInstruction {

    private final Registers registers;
    private final Register8 register;

    public Register(Registers registers, Register8 register) {
      this.registers = registers;
      this.register = register;
    }

    @Override
    protected MemRead execute0(byte data) {
      byte result = add(registers.a().get(), register.get(), registers.f().getC() ? 1 : 0);
      registers.a().set(result);
      registers.f().setZ(result == 0);
      return new MemRead(registers.pc().getAndIncrement());
    }

    @Override
    protected MemOperation execute1(byte data) {
      registers.ir().set(data);
      return null;
    }

    private byte add(byte lhs, byte rhs, int carry) {
      registers.f().setN(false);
      registers.f().setH((lhs & 0x0f) + (rhs & 0x0f) + carry > 0x0f);
      registers.f().setC(Byte.toUnsignedInt(lhs) + Byte.toUnsignedInt(rhs) + carry > 0xff);
      return (byte) (lhs + rhs + carry);
    }
  }

  public static final class Indirect extends AbstractInstruction {

    private final Registers registers;

    public Indirect(Registers registers) {
      this.registers = registers;
    }

    @Override
    protected MemRead execute0(byte data) {
      return new MemRead(registers.hl().get());
    }

    @Override
    protected MemOperation execute1(byte data) {
      byte result = add(registers.a().get(), data, registers.f().getC() ? 1 : 0);
      registers.a().set(result);
      registers.f().setZ(result == 0);
      return new MemRead(registers.pc().getAndIncrement());
    }

    @Override
    protected MemOperation execute2(byte data) {
      registers.ir().set(data);
      return null;
    }

    private byte add(byte lhs, byte rhs, int carry) {
      registers.f().setN(false);
      registers.f().setH((lhs & 0x0f) + (rhs & 0x0f) + carry > 0x0f);
      registers.f().setC(Byte.toUnsignedInt(lhs) + Byte.toUnsignedInt(rhs) + carry > 0xff);
      return (byte) (lhs + rhs + carry);
    }
  }

  public static final class Immediate extends AbstractInstruction {

    private final Registers registers;

    public Immediate(Registers registers) {
      this.registers = registers;
    }

    @Override
    protected MemRead execute0(byte data) {
      return new MemRead(registers.pc().getAndIncrement());
    }

    @Override
    protected MemRead execute1(byte data) {
      byte result = add(registers.a().get(), data, registers.f().getC() ? 1 : 0);
      registers.a().set(result);
      registers.f().setZ(result == 0);
      return new MemRead(registers.pc().getAndIncrement());
    }

    @Override
    protected MemOperation execute2(byte data) {
      registers.ir().set(data);
      return null;
    }

    private byte add(byte lhs, byte rhs, int carry) {
      registers.f().setN(false);
      registers.f().setH((lhs & 0x0f) + (rhs & 0x0f) + carry > 0x0f);
      registers.f().setC(Byte.toUnsignedInt(lhs) + Byte.toUnsignedInt(rhs) + carry > 0xff);
      return (byte) (lhs + rhs + carry);
    }
  }
}

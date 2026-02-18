package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.arch.Register8;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import com.zacharyhirsch.moldygameboy.emulator.memory.MemOperation;
import com.zacharyhirsch.moldygameboy.emulator.memory.MemRead;
import com.zacharyhirsch.moldygameboy.emulator.memory.MemWrite;

public final class Rr {

  public static final class Register extends AbstractInstruction {

    private final Registers registers;
    private final Register8 register;

    public Register(Registers registers, Register8 register) {
      this.registers = registers;
      this.register = register;
    }

    @Override
    protected MemRead execute0(byte data) {
      boolean b0 = (register.get() & 0x01) != 0;
      byte carry = (byte) (registers.f().getC() ? 0x80 : 0x00);
      byte result = (byte) ((Byte.toUnsignedInt(register.get()) >>> 1) | carry);
      register.set(result);
      registers.f().setZ(result == 0);
      registers.f().setN(false);
      registers.f().setH(false);
      registers.f().setC(b0);
      return new MemRead(registers.pc().getAndIncrement());
    }

    @Override
    protected MemOperation execute1(byte data) {
      registers.ir().set(data);
      return null;
    }
  }

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
      boolean b0 = (data & 0x01) != 0;
      byte carry = (byte) (registers.f().getC() ? 0x80 : 0x00);
      byte result = (byte) ((Byte.toUnsignedInt(data) >>> 1) | carry);
      registers.f().setZ(result == 0);
      registers.f().setN(false);
      registers.f().setH(false);
      registers.f().setC(b0);
      return new MemWrite(registers.hl().get(), result);
    }

    @Override
    protected MemOperation execute2(byte data) {
      return new MemRead(registers.pc().getAndIncrement());
    }

    @Override
    protected MemOperation execute3(byte data) {
      registers.ir().set(data);
      return null;
    }
  }
}

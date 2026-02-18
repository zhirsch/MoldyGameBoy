package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.arch.Register8;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import com.zacharyhirsch.moldygameboy.emulator.memory.MemOperation;
import com.zacharyhirsch.moldygameboy.emulator.memory.MemRead;
import com.zacharyhirsch.moldygameboy.emulator.memory.MemWrite;

public final class Swap {

  public static final class Register extends AbstractInstruction {

    private final Registers registers;
    private final Register8 register;

    public Register(Registers registers, Register8 register) {
      this.registers = registers;
      this.register = register;
    }

    @Override
    protected MemRead execute0(byte data) {
      byte l = (byte) (register.get() & 0x0f);
      byte h = (byte) (register.get() & 0xf0);
      byte result = (byte) ((Byte.toUnsignedInt(l) << 4) | (Byte.toUnsignedInt(h) >> 4));
      register.set(result);
      registers.f().setZ(result == 0);
      registers.f().setN(false);
      registers.f().setH(false);
      registers.f().setC(false);
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
      byte l = (byte) (data & 0x0f);
      byte h = (byte) (data & 0xf0);
      byte result = (byte) ((Byte.toUnsignedInt(l) << 4) | (Byte.toUnsignedInt(h) >> 4));
      registers.f().setZ(result == 0);
      registers.f().setN(false);
      registers.f().setH(false);
      registers.f().setC(false);
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

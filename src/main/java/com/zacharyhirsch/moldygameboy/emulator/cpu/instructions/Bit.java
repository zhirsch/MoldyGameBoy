package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.arch.Register8;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import com.zacharyhirsch.moldygameboy.emulator.memory.MemOperation;
import com.zacharyhirsch.moldygameboy.emulator.memory.MemRead;

public final class Bit {

  public static final class Register extends AbstractInstruction {

    private final Registers registers;
    private final Register8 register;
    private final int index;

    public Register(Registers registers, Register8 register, int index) {
      this.registers = registers;
      this.register = register;
      this.index = index;
    }

    @Override
    protected MemRead execute0(byte data) {
      registers.f().setZ((register.get() & (1 << index)) == 0);
      registers.f().setN(false);
      registers.f().setH(true);
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
    private final int index;

    public Indirect(Registers registers, int index) {
      this.registers = registers;
      this.index = index;
    }

    @Override
    protected MemOperation execute0(byte data) {
      return new MemRead(registers.hl().get());
    }

    @Override
    protected MemOperation execute1(byte data) {
      registers.f().setZ((data & (1 << index)) == 0);
      registers.f().setN(false);
      registers.f().setH(true);
      return new MemRead(registers.pc().getAndIncrement());
    }

    @Override
    protected MemOperation execute2(byte data) {
      registers.ir().set(data);
      return null;
    }
  }
}

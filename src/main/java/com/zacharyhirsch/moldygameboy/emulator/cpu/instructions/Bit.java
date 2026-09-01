package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.arch.Register8;
import com.zacharyhirsch.moldygameboy.emulator.cpu.alu.Alu;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;

public final class Bit {

  public static final class Register extends AbstractInstruction1 {

    private final Registers registers;
    private final Register8 register;
    private final int index;

    public Register(Registers registers, Register8 register, int index) {
      this.registers = registers;
      this.register = register;
      this.index = index;
    }

    @Override
    protected Mem execute0() {
      Alu.Result result = Alu.bit(register.read(), index);
      registers.f().z().set(result.z());
      registers.f().n().set(result.n());
      registers.f().h().set(result.h());
      return Mem.read(registers.pc().getAndIncrement(), registers.ir()::write);
    }
  }

  public static final class Indirect extends AbstractInstruction2 {

    private final Registers registers;
    private final int index;

    private byte z;

    public Indirect(Registers registers, int index) {
      this.registers = registers;
      this.index = index;
    }

    @Override
    protected Mem execute0() {
      return Mem.read(registers.hl().read(), data -> z = data);
    }

    @Override
    protected Mem execute1() {
      Alu.Result result = Alu.bit(z, index);
      registers.f().z().set(result.z());
      registers.f().n().set(result.n());
      registers.f().h().set(result.h());
      return Mem.read(registers.pc().getAndIncrement(), registers.ir()::write);
    }
  }
}

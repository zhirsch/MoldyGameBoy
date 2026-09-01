package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.arch.Register8;
import com.zacharyhirsch.moldygameboy.emulator.cpu.alu.Alu;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;

public final class Rlc {

  public static final class Register extends AbstractInstruction1 {

    private final Registers registers;
    private final Register8 register;

    public Register(Registers registers, Register8 register) {
      this.registers = registers;
      this.register = register;
    }

    @Override
    protected Mem execute0() {
      Alu.Result result = Alu.rl(register.read(), (register.read() & 0x80) != 0);
      register.write(result.result());
      registers.f().z().set(result.z());
      registers.f().n().set(result.n());
      registers.f().h().set(result.h());
      registers.f().c().set(result.c());
      return Mem.read(registers.pc().getAndIncrement(), registers.ir()::write);
    }
  }

  public static final class Indirect extends AbstractInstruction3 {

    private final Registers registers;

    private byte z;

    public Indirect(Registers registers) {
      this.registers = registers;
    }

    @Override
    protected Mem execute0() {
      return Mem.read(registers.hl().read(), data -> z = data);
    }

    @Override
    protected Mem execute1() {
      Alu.Result result = Alu.rl(z, (z & 0x80) != 0);
      registers.f().z().set(result.z());
      registers.f().n().set(result.n());
      registers.f().h().set(result.h());
      registers.f().c().set(result.c());
      return Mem.write(registers.hl().read(), result::result);
    }

    @Override
    protected Mem execute2() {
      return Mem.read(registers.pc().getAndIncrement(), registers.ir()::write);
    }
  }
}

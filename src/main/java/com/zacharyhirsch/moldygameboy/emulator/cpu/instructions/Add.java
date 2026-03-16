package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.arch.Register16;
import com.zacharyhirsch.moldygameboy.emulator.arch.Register8;
import com.zacharyhirsch.moldygameboy.emulator.cpu.alu.Alu;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;

public final class Add {

  public static final class Register extends AbstractInstruction1 {

    private final Registers registers;
    private final Register8 register;

    public Register(Registers registers, Register8 register) {
      this.registers = registers;
      this.register = register;
    }

    @Override
    protected Mem execute0() {
      Alu.Result result = Alu.add(registers.a().get(), register.get(), false);
      registers.a().set(result.result());
      registers.f().z().set(result.z());
      registers.f().n().set(result.n());
      registers.f().h().set(result.h());
      registers.f().c().set(result.c());
      return Mem.read(registers.pc().getAndIncrement(), registers.ir()::set);
    }
  }

  public static final class Indirect extends AbstractInstruction2 {

    private final Registers registers;

    private byte z;

    public Indirect(Registers registers) {
      this.registers = registers;
    }

    @Override
    protected Mem execute0() {
      return Mem.read(registers.hl().get(), data -> z = data);
    }

    @Override
    protected Mem execute1() {
      Alu.Result result = Alu.add(registers.a().get(), z, false);
      registers.a().set(result.result());
      registers.f().z().set(result.z());
      registers.f().n().set(result.n());
      registers.f().h().set(result.h());
      registers.f().c().set(result.c());
      return Mem.read(registers.pc().getAndIncrement(), registers.ir()::set);
    }
  }

  public static final class Immediate8 extends AbstractInstruction2 {

    private final Registers registers;

    private byte z;

    public Immediate8(Registers registers) {
      this.registers = registers;
    }

    @Override
    protected Mem execute0() {
      return Mem.read(registers.pc().getAndIncrement(), data -> z = data);
    }

    @Override
    protected Mem execute1() {
      Alu.Result result = Alu.add(registers.a().get(), z, false);
      registers.a().set(result.result());
      registers.f().z().set(result.z());
      registers.f().n().set(result.n());
      registers.f().h().set(result.h());
      registers.f().c().set(result.c());
      return Mem.read(registers.pc().getAndIncrement(), registers.ir()::set);
    }
  }

  public static final class Immediate16 extends AbstractInstruction2 {

    private final Registers registers;
    private final Register16 rhs;

    public Immediate16(Registers registers, Register16 rhs) {
      this.registers = registers;
      this.rhs = rhs;
    }

    @Override
    protected Mem execute0() {
      Alu.Result result = Alu.add(registers.hl().lo().get(), rhs.lo().get(), false);
      registers.hl().lo().set(result.result());
      registers.f().n().set(result.n());
      registers.f().h().set(result.h());
      registers.f().c().set(result.c());
      return Mem.none((short) 0x0000);
    }

    @Override
    protected Mem execute1() {
      var result = Alu.add(registers.hl().hi().get(), rhs.hi().get(), registers.f().c().get());
      registers.hl().hi().set(result.result());
      registers.f().n().set(result.n());
      registers.f().h().set(result.h());
      registers.f().c().set(result.c());
      return Mem.read(registers.pc().getAndIncrement(), registers.ir()::set);
    }
  }

  public static class StackOffset extends AbstractInstruction4 {

    private final Registers registers;

    private byte z;

    public StackOffset(Registers registers) {
      this.registers = registers;
    }

    @Override
    protected Mem execute0() {
      return Mem.read(registers.pc().getAndIncrement(), data -> z = data);
    }

    @Override
    protected Mem execute1() {
      Alu.Result result = Alu.add(registers.sp().lo().get(), z, false);
      registers.sp().lo().set(result.result());
      registers.f().z().set(false);
      registers.f().n().set(false);
      registers.f().h().set(result.h());
      registers.f().c().set(result.c());
      return Mem.none((short) 0);
    }

    @Override
    protected Mem execute2() {
      var result = Alu.add(registers.sp().hi().get(), (byte) (z >>> 7), registers.f().c().get());
      registers.sp().hi().set(result.result());
      return Mem.none((short) 0);
    }

    @Override
    protected Mem execute3() {
      return Mem.read(registers.pc().getAndIncrement(), registers.ir()::set);
    }
  }
}

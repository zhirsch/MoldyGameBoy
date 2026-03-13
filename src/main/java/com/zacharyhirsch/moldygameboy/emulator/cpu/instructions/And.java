package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.arch.Register8;
import com.zacharyhirsch.moldygameboy.emulator.cpu.alu.Alu;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;

public final class And {

//  public static final class Register extends AbstractInstruction {
//
//    private final Registers registers;
//    private final Register8 register;
//
//    public Register(Registers registers, Register8 register) {
//      this.registers = registers;
//      this.register = register;
//    }
//
//    @Override
//    protected Mem execute0(byte data) {
//      Alu.Result result = Alu.and(registers.a().get(), register.get());
//      registers.a().set(result.result());
//      registers.f().z().set(result.z());
//      registers.f().n().set(result.n());
//      registers.f().h().set(result.h());
//      registers.f().c().set(result.c());
//      return Mem.read(registers.pc().getAndIncrement());
//    }
//
//    @Override
//    protected Mem execute1(byte data) {
//      registers.ir().set(data);
//      return null;
//    }
//  }
//
//  public static final class Indirect extends AbstractInstruction {
//
//    private final Registers registers;
//
//    public Indirect(Registers registers) {
//      this.registers = registers;
//    }
//
//    @Override
//    protected Mem execute0(byte data) {
//      return Mem.read(registers.hl().get());
//    }
//
//    @Override
//    protected Mem execute1(byte data) {
//      Alu.Result result = Alu.and(registers.a().get(), data);
//      registers.a().set(result.result());
//      registers.f().z().set(result.z());
//      registers.f().n().set(result.n());
//      registers.f().h().set(result.h());
//      registers.f().c().set(result.c());
//      return Mem.read(registers.pc().getAndIncrement());
//    }
//
//    @Override
//    protected Mem execute2(byte data) {
//      registers.ir().set(data);
//      return null;
//    }
//  }
//
//  public static final class Immediate extends AbstractInstruction {
//
//    private final Registers registers;
//
//    public Immediate(Registers registers) {
//      this.registers = registers;
//    }
//
//    @Override
//    protected Mem execute0(byte data) {
//      return Mem.read(registers.pc().getAndIncrement());
//    }
//
//    @Override
//    protected Mem execute1(byte data) {
//      Alu.Result result = Alu.and(registers.a().get(), data);
//      registers.a().set(result.result());
//      registers.f().z().set(result.z());
//      registers.f().n().set(result.n());
//      registers.f().h().set(result.h());
//      registers.f().c().set(result.c());
//      return Mem.read(registers.pc().getAndIncrement());
//    }
//
//    @Override
//    protected Mem execute2(byte data) {
//      registers.ir().set(data);
//      return null;
//    }
//  }
}

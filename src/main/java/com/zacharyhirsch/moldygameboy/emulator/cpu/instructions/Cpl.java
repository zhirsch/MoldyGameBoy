//package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;
//
//import com.zacharyhirsch.moldygameboy.emulator.cpu.alu.Alu;
//import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
//
//public final class Cpl extends AbstractInstruction {
//
//  private final Registers registers;
//
//  public Cpl(Registers registers) {
//    this.registers = registers;
//  }
//
//  @Override
//  protected Mem execute0(byte data) {
//    Alu.Result result = Alu.cpl(registers.a().get());
//    registers.a().set(result.result());
//    registers.f().n().set(result.n());
//    registers.f().h().set(result.h());
//    return Mem.read(registers.pc().getAndIncrement());
//  }
//
//  @Override
//  protected Mem execute1(byte data) {
//    registers.ir().set(data);
//    return null;
//  }
//}

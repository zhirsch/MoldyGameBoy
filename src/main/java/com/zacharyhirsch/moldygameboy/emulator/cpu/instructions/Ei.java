//package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;
//
//import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
//
//
//public final class Ei extends AbstractInstruction {
//
//  private final Registers registers;
//
//  public Ei(Registers registers) {
//    this.registers = registers;
//  }
//
//  @Override
//  protected Mem execute0(byte data) {
//    registers.ime().set((byte) 1);
//    return Mem.read(registers.pc().getAndIncrement());
//  }
//
//  @Override
//  protected Mem execute1(byte data) {
//    registers.ir().set(data);
//    return null;
//  }
//}

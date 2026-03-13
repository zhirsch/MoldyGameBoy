//package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;
//
//import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
//
//
//public final class Reti extends AbstractInstruction {
//
//  private final Registers registers;
//
//  private byte w = 0;
//  private byte z = 0;
//
//  public Reti(Registers registers) {
//    this.registers = registers;
//  }
//
//  @Override
//  protected Mem execute0(byte data) {
//    return Mem.read(registers.sp().getAndIncrement());
//  }
//
//  @Override
//  protected Mem execute1(byte data) {
//    z = data;
//    return Mem.read(registers.sp().getAndIncrement());
//  }
//
//  @Override
//  protected Mem execute2(byte data) {
//    w = data;
//    return Mem.read((short) 0);
//  }
//
//  @Override
//  protected Mem execute3(byte data) {
//    registers.pc().set(w, z);
//    registers.ime().set((byte) 1);
//    return Mem.read(registers.pc().getAndIncrement());
//  }
//
//  @Override
//  protected Mem execute4(byte data) {
//    registers.ir().set(data);
//    return null;
//  }
//}

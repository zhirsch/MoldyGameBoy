//package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;
//
//import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.FlagsRegister;
//import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
//
//
//public final class Rra extends AbstractInstruction {
//
//  private final Registers registers;
//
//  public Rra(Registers registers) {
//    this.registers = registers;
//  }
//
//  @Override
//  protected Mem execute0(byte data) {
//    boolean b0 = (registers.a().get() & 0x01) != 0;
//    byte carry = (byte) (registers.f().c().get() ? 0x80 : 0x00);
//    byte result = (byte) ((Byte.toUnsignedInt(registers.a().get()) >>> 1) | carry);
//    registers.a().set(result);
//    registers.f().z().set(false);
//    registers.f().n().set(false);
//    registers.f().h().set(false);
//    registers.f().c().set(b0);
//    return Mem.read(registers.pc().getAndIncrement());
//  }
//
//  @Override
//  protected Mem execute1(byte data) {
//    registers.ir().set(data);
//    return null;
//  }
//}

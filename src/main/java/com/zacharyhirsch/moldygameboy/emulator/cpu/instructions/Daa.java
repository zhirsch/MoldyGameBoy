//package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;
//
//import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
//
//public final class Daa extends AbstractInstruction {
//
//  private final Registers registers;
//
//  public Daa(Registers registers) {
//    this.registers = registers;
//  }
//
//  @Override
//  protected Mem execute0(byte data) {
//    int a = Byte.toUnsignedInt(registers.a().get());
//    if (registers.f().n().get()) {
//      // after subtraction
//      if (registers.f().c().get()) {
//        a -= 0x60;
//      }
//      if (registers.f().h().get()) {
//        a -= 0x06;
//      }
//    } else {
//      // after addition
//      if (registers.f().c().get() || a > 0x99) {
//        a += 0x60;
//        registers.f().c().set(true);
//      }
//      if (registers.f().h().get() || (a & 0x0f) > 0x09) {
//        a += 0x06;
//      }
//    }
//    registers.a().set((byte) a);
//    registers.f().z().set(((byte) a) == 0);
//    registers.f().h().set(false);
//    return Mem.read(registers.pc().getAndIncrement());
//  }
//
//  @Override
//  protected Mem execute1(byte data) {
//    registers.ir().set(data);
//    return null;
//  }
//}

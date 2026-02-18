package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import com.zacharyhirsch.moldygameboy.emulator.memory.MemOperation;
import com.zacharyhirsch.moldygameboy.emulator.memory.MemRead;

public final class Daa extends AbstractInstruction {

  private final Registers registers;

  public Daa(Registers registers) {
    this.registers = registers;
  }

  @Override
  protected MemOperation execute0(byte data) {
    int a = Byte.toUnsignedInt(registers.a().get());
    if (registers.f().getN()) {
      // after subtraction
      if (registers.f().getC()) {
        a -= 0x60;
      }
      if (registers.f().getH()) {
        a -= 0x06;
      }
    } else {
      // after addition
      if (registers.f().getC() || a > 0x99) {
        a += 0x60;
        registers.f().setC(true);
      }
      if (registers.f().getH() || (a & 0x0f) > 0x09) {
        a += 0x06;
      }
    }
    registers.a().set((byte) a);
    registers.f().setZ(((byte) a) == 0);
    registers.f().setH(false);
    return new MemRead(registers.pc().getAndIncrement());
  }

  @Override
  protected MemOperation execute1(byte data) {
    registers.ir().set(data);
    return null;
  }
}

package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;


public final class Rrca extends AbstractInstruction {

  private final Registers registers;

  public Rrca(Registers registers) {
    this.registers = registers;
  }

  @Override
  protected Mem execute0(byte data) {
    boolean b0 = (registers.a().get() & 0x01) != 0;
    registers.a().set((byte) ((Byte.toUnsignedInt(registers.a().get()) >>> 1) | (b0 ? 0x80 : 0)));
    registers.f().setZ(false);
    registers.f().setN(false);
    registers.f().setH(false);
    registers.f().setC(b0);
    return Mem.read(registers.pc().getAndIncrement());
  }

  @Override
  protected Mem execute1(byte data) {
    registers.ir().set(data);
    return null;
  }
}

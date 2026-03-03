package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.FlagsRegister;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;


public final class Rlca extends AbstractInstruction {

  private final Registers registers;

  public Rlca(Registers registers) {
    this.registers = registers;
  }

  @Override
  protected Mem execute0(byte data) {
    boolean b7 = (registers.a().get() & 0x80) != 0;
    registers.a().set((byte) ((registers.a().get() << 1) | (b7 ? 1 : 0)));
    registers.f().z().set(false);
    registers.f().n().set(false);
    registers.f().h().set(false);
    registers.f().c().set(b7);
    return Mem.read(registers.pc().getAndIncrement());
  }

  @Override
  protected Mem execute1(byte data) {
    registers.ir().set(data);
    return null;
  }
}

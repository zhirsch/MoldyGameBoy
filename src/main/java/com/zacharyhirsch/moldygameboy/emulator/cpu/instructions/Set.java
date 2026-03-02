package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.arch.Register8;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;


public final class Set {

  public static final class Register extends AbstractInstruction {

    private final Registers registers;
    private final Register8 register;
    private final int index;

    public Register(Registers registers, Register8 register, int index) {
      this.registers = registers;
      this.register = register;
      this.index = index;
    }

    @Override
    protected Mem execute0(byte data) {
      register.set((byte) (register.get() | (1 << index)));
      return Mem.read(registers.pc().getAndIncrement());
    }

    @Override
    protected Mem execute1(byte data) {
      registers.ir().set(data);
      return null;
    }
  }

  public static final class Indirect extends AbstractInstruction {

    private final Registers registers;
    private final int index;

    public Indirect(Registers registers, int index) {
      this.registers = registers;
      this.index = index;
    }

    @Override
    protected Mem execute0(byte data) {
      return Mem.read(registers.hl().get());
    }

    @Override
    protected Mem execute1(byte data) {
      byte result = (byte) (data | (1 << index));
      return Mem.write(registers.hl().get(), result);
    }

    @Override
    protected Mem execute2(byte data) {
      return Mem.read(registers.pc().getAndIncrement());
    }

    @Override
    protected Mem execute3(byte data) {
      registers.ir().set(data);
      return null;
    }
  }
}

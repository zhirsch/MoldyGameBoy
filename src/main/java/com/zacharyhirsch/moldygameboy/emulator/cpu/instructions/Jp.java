package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;

public class Jp {

  public static final class Immediate extends AbstractInstruction4 {

    private final Registers registers;

    private byte z;
    private byte w;

    public Immediate(Registers registers) {
      this.registers = registers;
    }

    @Override
    protected Mem execute0() {
      return Mem.read(registers.pc().getAndIncrement(), data -> z = data);
    }

    @Override
    protected Mem execute1() {
      return Mem.read(registers.pc().getAndIncrement(), data -> w = data);
    }

    @Override
    protected Mem execute2() {
      registers.pc().set(w, z);
      return Mem.none((short) 0x0000);
    }

    @Override
    protected Mem execute3() {
      return Mem.read(registers.pc().getAndIncrement(), registers.ir()::set);
    }
  }

  public static final class Register extends AbstractInstruction1 {

    private final Registers registers;

    public Register(Registers registers) {
      this.registers = registers;
    }

    @Override
    protected Mem execute0() {
      registers.pc().set(registers.hl().get());
      return Mem.read(registers.pc().getAndIncrement(), registers.ir()::set);
    }
  }
}

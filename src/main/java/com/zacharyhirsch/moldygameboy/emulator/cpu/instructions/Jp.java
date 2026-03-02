package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;


public class Jp {

  public static final class Immediate extends AbstractInstruction {

    private final Registers registers;

    private byte z;
    private byte w;

    public Immediate(Registers registers) {
      this.registers = registers;
    }

    @Override
    protected Mem execute0(byte data) {
      return Mem.read(registers.pc().getAndIncrement());
    }

    @Override
    protected Mem execute1(byte data) {
      z = data;
      return Mem.read(registers.pc().getAndIncrement());
    }

    @Override
    protected Mem execute2(byte data) {
      w = data;
      registers.pc().set(w, z);
      return Mem.read((short) 0);
    }

    @Override
    protected Mem execute3(byte data) {
      return Mem.read(registers.pc().getAndIncrement());
    }

    @Override
    protected Mem execute4(byte data) {
      registers.ir().set(data);
      return null;
    }
  }

  public static final class Register extends AbstractInstruction {

    private final Registers registers;

    public Register(Registers registers) {
      this.registers = registers;
    }

    @Override
    protected Mem execute0(byte data) {
      registers.pc().set(registers.hl().get());
      return Mem.read(registers.pc().getAndIncrement());
    }

    @Override
    protected Mem execute1(byte data) {
      registers.ir().set(data);
      return null;
    }
  }
}

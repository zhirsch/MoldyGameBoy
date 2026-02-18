package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import com.zacharyhirsch.moldygameboy.emulator.memory.MemOperation;
import com.zacharyhirsch.moldygameboy.emulator.memory.MemRead;

public class Jp {

  public static final class Immediate extends AbstractInstruction {

    private final Registers registers;

    private byte z;
    private byte w;

    public Immediate(Registers registers) {
      this.registers = registers;
    }

    @Override
    protected MemOperation execute0(byte data) {
      return new MemRead(registers.pc().getAndIncrement());
    }

    @Override
    protected MemOperation execute1(byte data) {
      z = data;
      return new MemRead(registers.pc().getAndIncrement());
    }

    @Override
    protected MemOperation execute2(byte data) {
      w = data;
      registers.pc().set(w, z);
      return new MemRead((short) 0);
    }

    @Override
    protected MemOperation execute3(byte data) {
      return new MemRead(registers.pc().getAndIncrement());
    }

    @Override
    protected MemOperation execute4(byte data) {
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
    protected MemOperation execute0(byte data) {
      registers.pc().set(registers.hl().get());
      return new MemRead(registers.pc().getAndIncrement());
    }

    @Override
    protected MemOperation execute1(byte data) {
      registers.ir().set(data);
      return null;
    }
  }
}

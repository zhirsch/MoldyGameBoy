package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;

public final class Cond {

  private Cond() {}

  public static Instruction c0(Registers registers, Instruction instruction) {
    return registers.f().c().get() ? instruction : new NoBranch0(registers);
  }

  public static Instruction nc0(Registers registers, Instruction instruction) {
    return !registers.f().c().get() ? instruction : new NoBranch0(registers);
  }

  public static Instruction z0(Registers registers, Instruction instruction) {
    return registers.f().z().get() ? instruction : new NoBranch0(registers);
  }

  public static Instruction nz0(Registers registers, Instruction instruction) {
    return !registers.f().z().get() ? instruction : new NoBranch0(registers);
  }

  public static Instruction c8(Registers registers, Instruction instruction) {
    return registers.f().c().get() ? instruction : new NoBranch8(registers);
  }

  public static Instruction nc8(Registers registers, Instruction instruction) {
    return !registers.f().c().get() ? instruction : new NoBranch8(registers);
  }

  public static Instruction z8(Registers registers, Instruction instruction) {
    return registers.f().z().get() ? instruction : new NoBranch8(registers);
  }

  public static Instruction nz8(Registers registers, Instruction instruction) {
    return !registers.f().z().get() ? instruction : new NoBranch8(registers);
  }

  public static Instruction c16(Registers registers, Instruction instruction) {
    return registers.f().c().get() ? instruction : new NoBranch16(registers);
  }

  public static Instruction nc16(Registers registers, Instruction instruction) {
    return !registers.f().c().get() ? instruction : new NoBranch16(registers);
  }

  public static Instruction z16(Registers registers, Instruction instruction) {
    return registers.f().z().get() ? instruction : new NoBranch16(registers);
  }

  public static Instruction nz16(Registers registers, Instruction instruction) {
    return !registers.f().z().get() ? instruction : new NoBranch16(registers);
  }

  private static class NoBranch0 extends AbstractInstruction {

    private final Registers registers;

    public NoBranch0(Registers registers) {
      this.registers = registers;
    }

    @Override
    protected Mem execute0(byte data) {
      return Mem.read(registers.pc().getAndIncrement());
    }

    @Override
    protected Mem execute1(byte data) {
      registers.ir().set(data);
      return null;
    }
  }

  private static class NoBranch8 extends AbstractInstruction {

    private final Registers registers;

    public NoBranch8(Registers registers) {
      this.registers = registers;
    }

    @Override
    protected Mem execute0(byte data) {
      return Mem.read(registers.pc().getAndIncrement());
    }

    @Override
    protected Mem execute1(byte data) {
      return Mem.read(registers.pc().getAndIncrement());
    }

    @Override
    protected Mem execute2(byte data) {
      registers.ir().set(data);
      return null;
    }
  }

  private static class NoBranch16 extends AbstractInstruction {

    private final Registers registers;

    public NoBranch16(Registers registers) {
      this.registers = registers;
    }

    @Override
    protected Mem execute0(byte data) {
      return Mem.read(registers.pc().getAndIncrement());
    }

    @Override
    protected Mem execute1(byte data) {
      return Mem.read(registers.pc().getAndIncrement());
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

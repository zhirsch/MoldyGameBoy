package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.cpu.alu.Alu;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;

public final class Inc {

  public static final class Indirect extends AbstractInstruction {

    private final Registers registers;

    public Indirect(Registers registers) {
      this.registers = registers;
    }

    @Override
    protected Mem execute0(byte data) {
      return Mem.read(registers.hl().get());
    }

    @Override
    protected Mem execute1(byte data) {
      Alu.Result result = Alu.add(data, (byte) 1, false);
      registers.f().z().set(result.z());
      registers.f().n().set(result.n());
      registers.f().h().set(result.h());
      return Mem.write(registers.hl().get(), result.result());
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

  public static final class Register8 extends AbstractInstruction {

    private final Registers registers;
    private final com.zacharyhirsch.moldygameboy.emulator.arch.Register8 register;

    public Register8(
        Registers registers, com.zacharyhirsch.moldygameboy.emulator.arch.Register8 register) {
      this.registers = registers;
      this.register = register;
    }

    @Override
    protected Mem execute0(byte data) {
      Alu.Result result = Alu.add(register.get(), (byte) 1, false);
      register.set(result.result());
      registers.f().z().set(result.z());
      registers.f().n().set(result.n());
      registers.f().h().set(result.h());
      return Mem.read(registers.pc().getAndIncrement());
    }

    @Override
    protected Mem execute1(byte data) {
      registers.ir().set(data);
      return null;
    }

  }

  public static final class Register16 extends AbstractInstruction {

    private final Registers registers;
    private final com.zacharyhirsch.moldygameboy.emulator.arch.Register16 register;

    public Register16(
        Registers registers, com.zacharyhirsch.moldygameboy.emulator.arch.Register16 register) {
      this.registers = registers;
      this.register = register;
    }

    @Override
    protected Mem execute0(byte data) {
      return Mem.read(register.getAndIncrement());
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
}

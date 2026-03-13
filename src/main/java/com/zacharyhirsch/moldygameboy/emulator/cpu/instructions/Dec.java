package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.arch.Register16;
import com.zacharyhirsch.moldygameboy.emulator.arch.Register8;
import com.zacharyhirsch.moldygameboy.emulator.cpu.alu.Alu;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;

public final class Dec {

  //  public static final class Indirect extends AbstractInstruction {
  //
  //    private final Registers registers;
  //
  //    public Indirect(Registers registers) {
  //      this.registers = registers;
  //    }
  //
  //    @Override
  //    protected Mem execute0(byte data) {
  //      return Mem.read(registers.hl().get());
  //    }
  //
  //    @Override
  //    protected Mem execute1(byte data) {
  //      Alu.Result result = Alu.sub(data, (byte) 1, (byte) 0);
  //      registers.f().z().set(result.z());
  //      registers.f().n().set(result.n());
  //      registers.f().h().set(result.h());
  //      return Mem.write(registers.hl().get(), result.result());
  //    }
  //
  //    @Override
  //    protected Mem execute2(byte data) {
  //      return Mem.read(registers.pc().getAndIncrement());
  //    }
  //
  //    @Override
  //    protected Mem execute3(byte data) {
  //      registers.ir().set(data);
  //      return null;
  //    }
  //  }

  public static final class R8 extends AbstractInstruction1 {

    private final Registers registers;
    private final Register8 register;

    public R8(Registers registers, Register8 register) {
      this.registers = registers;
      this.register = register;
    }

    @Override
    protected Mem execute0() {
      Alu.Result result = Alu.sub(register.get(), (byte) 1);
      register.set(result.result());
      registers.f().z().set(result.z());
      registers.f().n().set(result.n());
      registers.f().h().set(result.h());
      return Mem.read(registers.pc().getAndIncrement(), registers.ir()::set);
    }
  }

  public static final class R16 extends AbstractInstruction2 {

    private final Registers registers;
    private final Register16 register;

    public R16(Registers registers, Register16 register) {
      this.registers = registers;
      this.register = register;
    }

    @Override
    protected Mem execute0() {
      return Mem.none(register.getAndDecrement());
    }

    @Override
    protected Mem execute1() {
      return Mem.read(registers.pc().getAndIncrement(), registers.ir()::set);
    }
  }
}

package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.arch.Register16;
import com.zacharyhirsch.moldygameboy.emulator.arch.Register8;
import com.zacharyhirsch.moldygameboy.emulator.cpu.alu.Alu;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;

public final class Inc {

  public static final class Indirect extends AbstractInstruction3 {

    private final Registers registers;

    private byte z;

    public Indirect(Registers registers) {
      this.registers = registers;
    }

    @Override
    protected Mem execute0() {
      return Mem.read(registers.hl().read(), data -> z = data);
    }

    @Override
    protected Mem execute1() {
      Alu.Result result = Alu.add(z, (byte) 1, false);
      registers.f().z().set(result.z());
      registers.f().n().set(result.n());
      registers.f().h().set(result.h());
      return Mem.write(registers.hl().read(), result::result);
    }

    @Override
    protected Mem execute2() {
      return Mem.read(registers.pc().getAndIncrement(), registers.ir()::write);
    }
  }

  public static final class R8 extends AbstractInstruction1 {

    private final Registers registers;
    private final Register8 register;

    public R8(Registers registers, Register8 register) {
      this.registers = registers;
      this.register = register;
    }

    @Override
    protected Mem execute0() {
      Alu.Result result = Alu.add(register.read(), (byte) 1, false);
      register.write(result.result());
      registers.f().z().set(result.z());
      registers.f().n().set(result.n());
      registers.f().h().set(result.h());
      return Mem.read(registers.pc().getAndIncrement(), registers.ir()::write);
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
      return Mem.none(register.getAndIncrement());
    }

    @Override
    protected Mem execute1() {
      return Mem.read(registers.pc().getAndIncrement(), registers.ir()::write);
    }
  }
}

package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

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
      byte newValue = add(data, (byte) 1, 0);
      return Mem.write(registers.hl().get(), newValue);
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

    private byte add(byte lhs, byte rhs, int carry) {
      registers.f().setZ(lhs + rhs + carry == 0);
      registers.f().setN(false);
      registers.f().setH((lhs & 0x0f) + (rhs & 0x0f) + carry > 0x0f);
      return (byte) (lhs + rhs + carry);
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
      byte newValue = add(register.get(), (byte) 1, 0);
      register.set(newValue);
      return Mem.read(registers.pc().getAndIncrement());
    }

    @Override
    protected Mem execute1(byte data) {
      registers.ir().set(data);
      return null;
    }

    private byte add(byte lhs, byte rhs, int carry) {
      registers.f().setZ(lhs + rhs + carry == 0);
      registers.f().setN(false);
      registers.f().setH((lhs & 0x0f) + (rhs & 0x0f) + carry > 0x0f);
      return (byte) (lhs + rhs + carry);
    }
  }

  public static final class Register16 extends AbstractInstruction {

    private final Registers registers;
    private final com.zacharyhirsch.moldygameboy.emulator.arch.Register16<
            com.zacharyhirsch.moldygameboy.emulator.arch.Register8,
            com.zacharyhirsch.moldygameboy.emulator.arch.Register8>
        register;

    public Register16(
        Registers registers,
        com.zacharyhirsch.moldygameboy.emulator.arch.Register16<
                com.zacharyhirsch.moldygameboy.emulator.arch.Register8,
                com.zacharyhirsch.moldygameboy.emulator.arch.Register8>
            register) {
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

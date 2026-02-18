package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import com.zacharyhirsch.moldygameboy.emulator.memory.MemOperation;
import com.zacharyhirsch.moldygameboy.emulator.memory.MemRead;
import com.zacharyhirsch.moldygameboy.emulator.memory.MemWrite;

public final class Inc {

  public static final class Indirect extends AbstractInstruction {

    private final Registers registers;

    public Indirect(Registers registers) {
      this.registers = registers;
    }

    @Override
    protected MemOperation execute0(byte data) {
      return new MemRead(registers.hl().get());
    }

    @Override
    protected MemOperation execute1(byte data) {
      byte newValue = add(data, (byte) 1, 0);
      return new MemWrite(registers.hl().get(), newValue);
    }

    @Override
    protected MemOperation execute2(byte data) {
      return new MemRead(registers.pc().getAndIncrement());
    }

    @Override
    protected MemOperation execute3(byte data) {
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
    protected MemOperation execute0(byte data) {
      byte newValue = add(register.get(), (byte) 1, 0);
      register.set(newValue);
      return new MemRead(registers.pc().getAndIncrement());
    }

    @Override
    protected MemOperation execute1(byte data) {
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
    protected MemOperation execute0(byte data) {
      return new MemRead(register.getAndIncrement());
    }

    @Override
    protected MemOperation execute1(byte data) {
      return new MemRead(registers.pc().getAndIncrement());
    }

    @Override
    protected MemOperation execute2(byte data) {
      registers.ir().set(data);
      return null;
    }
  }
}

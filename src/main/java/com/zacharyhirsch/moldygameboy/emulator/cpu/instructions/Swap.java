package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.arch.Register8;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.FlagsRegister;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;


public final class Swap {

  public static final class Register extends AbstractInstruction {

    private final Registers registers;
    private final Register8 register;

    public Register(Registers registers, Register8 register) {
      this.registers = registers;
      this.register = register;
    }

    @Override
    protected Mem execute0(byte data) {
      byte l = (byte) (register.get() & 0x0f);
      byte h = (byte) (register.get() & 0xf0);
      byte result = (byte) ((Byte.toUnsignedInt(l) << 4) | (Byte.toUnsignedInt(h) >> 4));
      register.set(result);
      registers.f().z().set(result == 0);
      registers.f().n().set(false);
      registers.f().h().set(false);
      registers.f().c().set(false);
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

    public Indirect(Registers registers) {
      this.registers = registers;
    }

    @Override
    protected Mem execute0(byte data) {
      return Mem.read(registers.hl().get());
    }

    @Override
    protected Mem execute1(byte data) {
      byte l = (byte) (data & 0x0f);
      byte h = (byte) (data & 0xf0);
      byte result = (byte) ((Byte.toUnsignedInt(l) << 4) | (Byte.toUnsignedInt(h) >> 4));
      registers.f().z().set(result == 0);
      registers.f().n().set(false);
      registers.f().h().set(false);
      registers.f().c().set(false);
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

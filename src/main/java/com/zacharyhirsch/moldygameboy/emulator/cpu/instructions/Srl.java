package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.arch.Register8;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;


public final class Srl {

  public static final class Register extends AbstractInstruction {

    private final Registers registers;
    private final Register8 register;

    public Register(Registers registers, Register8 register) {
      this.registers = registers;
      this.register = register;
    }

    @Override
    protected Mem execute0(byte data) {
      boolean b0 = (register.get() & 0x01) != 0;
      byte result = (byte) (Byte.toUnsignedInt(register.get()) >>> 1);
      register.set(result);
      registers.f().setZ(result == 0);
      registers.f().setN(false);
      registers.f().setH(false);
      registers.f().setC(b0);
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
      boolean b0 = (data & 0x01) != 0;
      byte result = (byte) (Byte.toUnsignedInt(data) >>> 1);
      registers.f().setZ(result == 0);
      registers.f().setN(false);
      registers.f().setH(false);
      registers.f().setC(b0);
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

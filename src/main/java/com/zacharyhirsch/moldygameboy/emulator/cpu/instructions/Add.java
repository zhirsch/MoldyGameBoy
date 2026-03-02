package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.arch.Register16;
import com.zacharyhirsch.moldygameboy.emulator.arch.Register8;
import com.zacharyhirsch.moldygameboy.emulator.arch.UInt8;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;


public final class Add {

  public static final class Register extends AbstractInstruction {

    private final Registers registers;
    private final Register8 register;

    public Register(Registers registers, Register8 register) {
      this.registers = registers;
      this.register = register;
    }

    @Override
    protected Mem execute0(byte data) {
      byte result = add(registers.a().get(), register.get(), 0);
      registers.a().set(result);
      registers.f().setZ(result == 0);
      return Mem.read(registers.pc().getAndIncrement());
    }

    @Override
    protected Mem execute1(byte data) {
      registers.ir().set(data);
      return null;
    }

    private byte add(byte lhs, byte rhs, int carry) {
      registers.f().setN(false);
      registers.f().setH((lhs & 0x0f) + (rhs & 0x0f) + carry > 0x0f);
      registers.f().setC(Byte.toUnsignedInt(lhs) + Byte.toUnsignedInt(rhs) + carry > 0xff);
      return (byte) (lhs + rhs + carry);
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
      byte result = add(registers.a().get(), data, 0);
      registers.a().set(result);
      registers.f().setZ(result == 0);
      return Mem.read(registers.pc().getAndIncrement());
    }

    @Override
    protected Mem execute2(byte data) {
      registers.ir().set(data);
      return null;
    }

    private byte add(byte lhs, byte rhs, int carry) {
      registers.f().setN(false);
      registers.f().setH((lhs & 0x0f) + (rhs & 0x0f) + carry > 0x0f);
      registers.f().setC(Byte.toUnsignedInt(lhs) + Byte.toUnsignedInt(rhs) + carry > 0xff);
      return (byte) (lhs + rhs + carry);
    }
  }

  public static final class Immediate8 extends AbstractInstruction {

    private final Registers registers;

    public Immediate8(Registers registers) {
      this.registers = registers;
    }

    @Override
    protected Mem execute0(byte data) {
      return Mem.read(registers.pc().getAndIncrement());
    }

    @Override
    protected Mem execute1(byte data) {
      byte result = add(registers.a().get(), data, 0);
      registers.a().set(result);
      registers.f().setZ(result == 0);
      return Mem.read(registers.pc().getAndIncrement());
    }

    @Override
    protected Mem execute2(byte data) {
      registers.ir().set(data);
      return null;
    }

    private byte add(byte lhs, byte rhs, int carry) {
      registers.f().setN(false);
      registers.f().setH((lhs & 0x0f) + (rhs & 0x0f) + carry > 0x0f);
      registers.f().setC(Byte.toUnsignedInt(lhs) + Byte.toUnsignedInt(rhs) + carry > 0xff);
      return (byte) (lhs + rhs + carry);
    }
  }

  public static final class Immediate16 extends AbstractInstruction {

    private final Registers registers;
    private final Register16<Register8, Register8> lhs;
    private final Register16<? extends UInt8, ? extends UInt8> rhs;

    public Immediate16(Registers registers, Register16<? extends UInt8, ? extends UInt8> rhs) {
      this.registers = registers;
      this.lhs = registers.hl();
      this.rhs = rhs;
    }

    @Override
    protected Mem execute0(byte data) {
      lhs.lo().set(add(lhs.lo().get(), rhs.lo().get(), 0));
      return Mem.read(registers.pc().getAndIncrement());
    }

    @Override
    protected Mem execute1(byte data) {
      lhs.hi().set(add(lhs.hi().get(), rhs.hi().get(), registers.f().getC() ? 1 : 0));
      registers.ir().set(data);
      return null;
    }

    private byte add(byte lhs, byte rhs, int carry) {
      registers.f().setN(false);
      registers.f().setH((lhs & 0x0f) + (rhs & 0x0f) + carry > 0x0f);
      registers.f().setC(Byte.toUnsignedInt(lhs) + Byte.toUnsignedInt(rhs) + carry > 0xff);
      return (byte) (lhs + rhs + carry);
    }
  }

  public static class StackOffset extends AbstractInstruction {

    private final Registers registers;

    private byte z;

    public StackOffset(Registers registers) {
      this.registers = registers;
    }

    @Override
    protected Mem execute0(byte data) {
      return Mem.read(registers.pc().getAndIncrement());
    }

    @Override
    protected Mem execute1(byte data) {
      z = data;
      byte lhs = registers.sp().lo().get();
      byte rhs = z;
      registers.sp().lo().set((byte) (Byte.toUnsignedInt(lhs) + rhs));
      registers.f().setZ(false);
      registers.f().setN(false);
      registers.f().setH((lhs & 0x0f) + (rhs & 0x0f) > 0x0f);
      registers.f().setC(Byte.toUnsignedInt(lhs) + Byte.toUnsignedInt(rhs) > 0xff);
      return Mem.read((short) 0);
    }

    @Override
    protected Mem execute2(byte data) {
      byte lhs = registers.sp().hi().get();
      byte rhs = (byte) ((z & 0x80) != 0 ? 0xff : 0);
      byte carry = (byte) (registers.f().getC() ? 1 : 0);
      registers.sp().hi().set((byte) (Byte.toUnsignedInt(lhs) + rhs + carry));
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
}

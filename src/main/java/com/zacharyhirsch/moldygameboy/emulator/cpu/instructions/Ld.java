package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.arch.Register16;
import com.zacharyhirsch.moldygameboy.emulator.arch.Register8;
import com.zacharyhirsch.moldygameboy.emulator.cpu.alu.Alu;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;

public final class Ld {

  private Ld() {}

  public static final class RegisterRegister extends AbstractInstruction1 {

    private final Registers registers;
    private final Register8 src;
    private final Register8 dst;

    public RegisterRegister(Registers registers, Register8 src, Register8 dst) {
      this.registers = registers;
      this.src = src;
      this.dst = dst;
    }

    @Override
    protected Mem execute0() {
      dst.set(src.get());
      return Mem.read(registers.pc().getAndIncrement(), registers.ir()::set);
    }
  }

  public static final class RegisterRegister16 extends AbstractInstruction2 {

    private final Registers registers;
    private final Register16 src;
    private final Register16 dst;

    public RegisterRegister16(Registers registers, Register16 src, Register16 dst) {
      this.registers = registers;
      this.src = src;
      this.dst = dst;
    }

    @Override
    protected Mem execute0() {
      dst.set(src.get());
      return Mem.none(src.get());
    }

    @Override
    protected Mem execute1() {
      return Mem.read(registers.pc().getAndIncrement(), registers.ir()::set);
    }
  }

  public static final class RegisterIndirectIncrement extends AbstractInstruction2 {

    private final Registers registers;
    private final Register8 dst;

    public RegisterIndirectIncrement(Registers registers, Register8 dst) {
      this.registers = registers;
      this.dst = dst;
    }

    @Override
    protected Mem execute0() {
      return Mem.read(registers.hl().getAndIncrement(), dst::set);
    }

    @Override
    protected Mem execute1() {
      return Mem.read(registers.pc().getAndIncrement(), registers.ir()::set);
    }
  }

  public static final class RegisterIndirect extends AbstractInstruction2 {

    private final Registers registers;
    private final Register8 dst;

    public RegisterIndirect(Registers registers, Register8 dst) {
      this.registers = registers;
      this.dst = dst;
    }

    @Override
    protected Mem execute0() {
      return Mem.read(registers.hl().get(), dst::set);
    }

    @Override
    protected Mem execute1() {
      return Mem.read(registers.pc().getAndIncrement(), registers.ir()::set);
    }
  }

  public static final class AccumulatorIndirect extends AbstractInstruction2 {

    private final Registers registers;
    private final Register16 register;

    public AccumulatorIndirect(Registers registers, Register16 register) {
      this.registers = registers;
      this.register = register;
    }

    @Override
    protected Mem execute0() {
      return Mem.read(register.get(), registers.a()::set);
    }

    @Override
    protected Mem execute1() {
      return Mem.read(registers.pc().getAndIncrement(), registers.ir()::set);
    }
  }

  public static final class AccumulatorIndirectDecrement extends AbstractInstruction2 {

    private final Registers registers;
    private final Register16 register;

    public AccumulatorIndirectDecrement(Registers registers, Register16 register) {
      this.registers = registers;
      this.register = register;
    }

    @Override
    protected Mem execute0() {
      return Mem.read(register.getAndDecrement(), registers.a()::set);
    }

    @Override
    protected Mem execute1() {
      return Mem.read(registers.pc().getAndIncrement(), registers.ir()::set);
    }
  }

  public static final class AccumulatorIndirectHi extends AbstractInstruction2 {

    private final Registers registers;
    private final Register8 register;

    public AccumulatorIndirectHi(Registers registers, Register8 register) {
      this.registers = registers;
      this.register = register;
    }

    @Override
    protected Mem execute0() {
      short address = (short) (0xff00 | register.get());
      return Mem.read(address, registers.a()::set);
    }

    @Override
    protected Mem execute1() {
      return Mem.read(registers.pc().getAndIncrement(), registers.ir()::set);
    }
  }

  public static final class ImmediateIndirect extends AbstractInstruction3 {

    private final Registers registers;

    private byte z;

    public ImmediateIndirect(Registers registers) {
      this.registers = registers;
    }

    @Override
    protected Mem execute0() {
      return Mem.read(registers.pc().getAndIncrement(), data -> z = data);
    }

    @Override
    protected Mem execute1() {
      return Mem.write(registers.hl().get(), () -> z);
    }

    @Override
    protected Mem execute2() {
      return Mem.read(registers.pc().getAndIncrement(), registers.ir()::set);
    }
  }

  public static final class IndirectRegister extends AbstractInstruction2 {

    private final Registers registers;
    private final Register8 src;
    private final Register16 dst;

    public IndirectRegister(Registers registers, Register8 src, Register16 dst) {
      this.registers = registers;
      this.src = src;
      this.dst = dst;
    }

    @Override
    protected Mem execute0() {
      return Mem.write(dst.get(), src::get);
    }

    @Override
    protected Mem execute1() {
      return Mem.read(registers.pc().getAndIncrement(), registers.ir()::set);
    }
  }

  public static final class IndirectIncrementAccumulator extends AbstractInstruction2 {

    private final Registers registers;

    public IndirectIncrementAccumulator(Registers registers) {
      this.registers = registers;
    }

    @Override
    protected Mem execute0() {
      return Mem.write(registers.hl().getAndIncrement(), registers.a()::get);
    }

    @Override
    protected Mem execute1() {
      return Mem.read(registers.pc().getAndIncrement(), registers.ir()::set);
    }
  }

  public static final class IndirectDecrementAccumulator extends AbstractInstruction2 {

    private final Registers registers;

    public IndirectDecrementAccumulator(Registers registers) {
      this.registers = registers;
    }

    @Override
    protected Mem execute0() {
      return Mem.write(registers.hl().getAndDecrement(), registers.a()::get);
    }

    @Override
    protected Mem execute1() {
      return Mem.read(registers.pc().getAndIncrement(), registers.ir()::set);
    }
  }

  public static final class RegisterImmediate8 extends AbstractInstruction2 {

    private final Registers registers;
    private final Register8 register;

    public RegisterImmediate8(Registers registers, Register8 register) {
      this.registers = registers;
      this.register = register;
    }

    @Override
    protected Mem execute0() {
      return Mem.read(registers.pc().getAndIncrement(), register::set);
    }

    @Override
    protected Mem execute1() {
      return Mem.read(registers.pc().getAndIncrement(), registers.ir()::set);
    }
  }

  public static final class RegisterImmediate16 extends AbstractInstruction3 {

    private final Registers registers;
    private final Register16 register;

    private byte z;

    public RegisterImmediate16(Registers registers, Register16 register) {
      this.registers = registers;
      this.register = register;
    }

    @Override
    protected Mem execute0() {
      return Mem.read(registers.pc().getAndIncrement(), data -> z = data);
    }

    @Override
    protected Mem execute1() {
      return Mem.read(registers.pc().getAndIncrement(), data -> register.set(data, z));
    }

    @Override
    protected Mem execute2() {
      return Mem.read(registers.pc().getAndIncrement(), registers.ir()::set);
    }
  }

  public static final class DirectAccumulator extends AbstractInstruction4 {

    private final Registers registers;

    private byte z;
    private byte w;

    public DirectAccumulator(Registers registers) {
      this.registers = registers;
    }

    @Override
    protected Mem execute0() {
      return Mem.read(registers.pc().getAndIncrement(), data -> z = data);
    }

    @Override
    protected Mem execute1() {
      return Mem.read(registers.pc().getAndIncrement(), data -> w = data);
    }

    @Override
    protected Mem execute2() {
      short address = (short) ((Byte.toUnsignedInt(w) << 8) | Byte.toUnsignedInt(z));
      return Mem.write(address, registers.a()::get);
    }

    @Override
    protected Mem execute3() {
      return Mem.read(registers.pc().getAndIncrement(), registers.ir()::set);
    }
  }

  public static final class DirectStackPointer extends AbstractInstruction5 {

    private final Registers registers;
    private final Register16 wz;

    public DirectStackPointer(Registers registers) {
      this.registers = registers;
      this.wz = new Register16();
    }

    @Override
    protected Mem execute0() {
      return Mem.read(registers.pc().getAndIncrement(), wz.lo()::set);
    }

    @Override
    protected Mem execute1() {
      return Mem.read(registers.pc().getAndIncrement(), wz.hi()::set);
    }

    @Override
    protected Mem execute2() {
      return Mem.write(wz.getAndIncrement(), registers.sp().lo()::get);
    }

    @Override
    protected Mem execute3() {
      return Mem.write(wz.getAndIncrement(), registers.sp().hi()::get);
    }

    @Override
    protected Mem execute4() {
      return Mem.read(registers.pc().getAndIncrement(), registers.ir()::set);
    }
  }

  public static final class DirectHiAccumulator extends AbstractInstruction3 {

    private final Registers registers;

    private byte z;

    public DirectHiAccumulator(Registers registers) {
      this.registers = registers;
    }

    @Override
    protected Mem execute0() {
      return Mem.read(registers.pc().getAndIncrement(), data -> z = data);
    }

    @Override
    protected Mem execute1() {
      short address = (short) (0xff00 | Byte.toUnsignedInt(z));
      return Mem.write(address, registers.a()::get);
    }

    @Override
    protected Mem execute2() {
      return Mem.read(registers.pc().getAndIncrement(), registers.ir()::set);
    }
  }

  public static final class IndirectHiAccumulator extends AbstractInstruction2 {

    private final Registers registers;
    private final Register8 register;

    public IndirectHiAccumulator(Registers registers, Register8 register) {
      this.registers = registers;
      this.register = register;
    }

    @Override
    protected Mem execute0() {
      short address = (short) (0xff00 | Byte.toUnsignedInt(register.get()));
      return Mem.write(address, registers.a()::get);
    }

    @Override
    protected Mem execute1() {
      return Mem.read(registers.pc().getAndIncrement(), registers.ir()::set);
    }
  }

  public static final class AccumulatorDirect extends AbstractInstruction4 {

    private final Registers registers;
    private final Register16 wz;

    public AccumulatorDirect(Registers registers) {
      this.registers = registers;
      this.wz = new Register16();
    }

    @Override
    protected Mem execute0() {
      return Mem.read(registers.pc().getAndIncrement(), wz.lo()::set);
    }

    @Override
    protected Mem execute1() {
      return Mem.read(registers.pc().getAndIncrement(), wz.hi()::set);
    }

    @Override
    protected Mem execute2() {
      return Mem.read(wz.getAndIncrement(), registers.a()::set);
    }

    @Override
    protected Mem execute3() {
      return Mem.read(registers.pc().getAndIncrement(), registers.ir()::set);
    }
  }

  public static final class AccumulatorDirectHi extends AbstractInstruction3 {

    private final Registers registers;

    private byte z;

    public AccumulatorDirectHi(Registers registers) {
      this.registers = registers;
    }

    @Override
    protected Mem execute0() {
      return Mem.read(registers.pc().getAndIncrement(), data -> z = data);
    }

    @Override
    protected Mem execute1() {
      short address = (short) (0xff00 | Byte.toUnsignedInt(z));
      return Mem.read(address, registers.a()::set);
    }

    @Override
    protected Mem execute2() {
      return Mem.read(registers.pc().getAndIncrement(), registers.ir()::set);
    }
  }

  public static final class StackOffset extends AbstractInstruction3 {

    private final Registers registers;

    private byte z;

    public StackOffset(Registers registers) {
      this.registers = registers;
    }

    @Override
    protected Mem execute0() {
      return Mem.read(registers.pc().getAndIncrement(), data -> z = data);
    }

    @Override
    protected Mem execute1() {
      Alu.Result result = Alu.add(registers.sp().lo().get(), z, false);
      registers.hl().lo().set(result.result());
      registers.f().z().set(false);
      registers.f().n().set(false);
      registers.f().h().set(result.h());
      registers.f().c().set(result.c());
      return Mem.none((short) 0);
    }

    @Override
    protected Mem execute2() {
      var result = Alu.add(registers.sp().hi().get(), (byte) (z >>> 7), registers.f().c().get());
      registers.hl().hi().set(result.result());
      return Mem.read(registers.pc().getAndIncrement(), registers.ir()::set);
    }
  }
}

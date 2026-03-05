package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.arch.Register16;
import com.zacharyhirsch.moldygameboy.emulator.arch.Register8;
import com.zacharyhirsch.moldygameboy.emulator.cpu.alu.Alu;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;

public final class Ld {

  private Ld() {}

  public static final class RegisterRegister extends AbstractInstruction {

    private final Registers registers;
    private final Register8 src;
    private final Register8 dst;

    public RegisterRegister(Registers registers, Register8 src, Register8 dst) {
      this.registers = registers;
      this.src = src;
      this.dst = dst;
    }

    @Override
    protected Mem execute0(byte data) {
      dst.set(src.get());
      return Mem.read(registers.pc().getAndIncrement());
    }

    @Override
    protected Mem execute1(byte data) {
      registers.ir().set(data);
      return null;
    }
  }

  public static final class RegisterRegister16 extends AbstractInstruction {

    private final Registers registers;
    private final Register16 src;
    private final Register16 dst;

    public RegisterRegister16(Registers registers, Register16 src, Register16 dst) {
      this.registers = registers;
      this.src = src;
      this.dst = dst;
    }

    @Override
    protected Mem execute0(byte data) {
      dst.set(src.get());
      return Mem.read(registers.pc().getAndIncrement());
    }

    @Override
    protected Mem execute1(byte data) {
      registers.ir().set(data);
      return null;
    }
  }

  public static final class RegisterIndirectIncrement extends AbstractInstruction {

    private final Registers registers;
    private final Register8 dst;

    public RegisterIndirectIncrement(Registers registers, Register8 dst) {
      this.registers = registers;
      this.dst = dst;
    }

    @Override
    protected Mem execute0(byte data) {
      return Mem.read(registers.hl().getAndIncrement());
    }

    @Override
    protected Mem execute1(byte data) {
      dst.set(data);
      return Mem.read(registers.pc().getAndIncrement());
    }

    @Override
    protected Mem execute2(byte data) {
      registers.ir().set(data);
      return null;
    }
  }

  public static final class RegisterIndirect extends AbstractInstruction {

    private final Registers registers;
    private final Register8 dst;

    public RegisterIndirect(Registers registers, Register8 dst) {
      this.registers = registers;
      this.dst = dst;
    }

    @Override
    protected Mem execute0(byte data) {
      return Mem.read(registers.hl().get());
    }

    @Override
    protected Mem execute1(byte data) {
      dst.set(data);
      return Mem.read(registers.pc().getAndIncrement());
    }

    @Override
    protected Mem execute2(byte data) {
      registers.ir().set(data);
      return null;
    }
  }

  public static final class AccumulatorIndirect extends AbstractInstruction {

    private final Registers registers;
    private final Register16 register;

    public AccumulatorIndirect(Registers registers, Register16 register) {
      this.registers = registers;
      this.register = register;
    }

    @Override
    protected Mem execute0(byte data) {
      return Mem.read(register.get());
    }

    @Override
    protected Mem execute1(byte data) {
      registers.a().set(data);
      return Mem.read(registers.pc().getAndIncrement());
    }

    @Override
    protected Mem execute2(byte data) {
      registers.ir().set(data);
      return null;
    }
  }
  public static final class AccumulatorIndirectDecrement extends AbstractInstruction {

    private final Registers registers;
    private final Register16 register;

    public AccumulatorIndirectDecrement(Registers registers, Register16 register) {
      this.registers = registers;
      this.register = register;
    }

    @Override
    protected Mem execute0(byte data) {
      return Mem.read(register.getAndDecrement());
    }

    @Override
    protected Mem execute1(byte data) {
      registers.a().set(data);
      return Mem.read(registers.pc().getAndIncrement());
    }

    @Override
    protected Mem execute2(byte data) {
      registers.ir().set(data);
      return null;
    }
  }

  public static final class AccumulatorIndirectHi extends AbstractInstruction {

    private final Registers registers;
    private final Register8 register;

    public AccumulatorIndirectHi(Registers registers, Register8 register) {
      this.registers = registers;
      this.register = register;
    }

    @Override
    protected Mem execute0(byte data) {
      short address = (short) (0xff00 | register.get());
      return Mem.read(address);
    }

    @Override
    protected Mem execute1(byte data) {
      registers.a().set(data);
      return Mem.read(registers.pc().getAndIncrement());
    }

    @Override
    protected Mem execute2(byte data) {
      registers.ir().set(data);
      return null;
    }
  }

  public static final class ImmediateIndirect extends AbstractInstruction {

    private final Registers registers;

    public ImmediateIndirect(Registers registers) {
      this.registers = registers;
    }

    @Override
    protected Mem execute0(byte data) {
      return Mem.read(registers.pc().getAndIncrement());
    }

    @Override
    protected Mem execute1(byte data) {
      return Mem.write(registers.hl().get(), data);
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

  public static final class IndirectRegister extends AbstractInstruction {

    private final Registers registers;
    private final Register8 src;
    private final Register16 dst;

    public IndirectRegister(Registers registers, Register8 src, Register16 dst) {
      this.registers = registers;
      this.src = src;
      this.dst = dst;
    }

    @Override
    protected Mem execute0(byte data) {
      return Mem.write(dst.get(), src.get());
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

  public static final class IndirectIncrementAccumulator extends AbstractInstruction {

    private final Registers registers;

    public IndirectIncrementAccumulator(Registers registers) {
      this.registers = registers;
    }

    @Override
    protected Mem execute0(byte data) {
      return Mem.write(registers.hl().getAndIncrement(), registers.a().get());
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

  public static final class IndirectDecrementAccumulator extends AbstractInstruction {

    private final Registers registers;

    public IndirectDecrementAccumulator(Registers registers) {
      this.registers = registers;
    }

    @Override
    protected Mem execute0(byte data) {
      return Mem.write(registers.hl().getAndDecrement(), registers.a().get());
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

  public static final class RegisterImmediate8 extends AbstractInstruction {

    private final Registers registers;
    private final Register8 register;

    public RegisterImmediate8(Registers registers, Register8 register) {
      this.registers = registers;
      this.register = register;
    }

    @Override
    protected Mem execute0(byte data) {
      return Mem.read(registers.pc().getAndIncrement());
    }

    @Override
    protected Mem execute1(byte data) {
      register.set(data);
      return Mem.read(registers.pc().getAndIncrement());
    }

    @Override
    protected Mem execute2(byte data) {
      registers.ir().set(data);
      return null;
    }
  }

  public static final class RegisterImmediate16 extends AbstractInstruction {

    private final Registers registers;
    private final Register16 register;

    private byte z;
    private byte w;

    public RegisterImmediate16(Registers registers, Register16 register) {
      this.registers = registers;
      this.register = register;
    }

    @Override
    protected Mem execute0(byte data) {
      return Mem.read(registers.pc().getAndIncrement());
    }

    @Override
    protected Mem execute1(byte data) {
      z = data;
      return Mem.read(registers.pc().getAndIncrement());
    }

    @Override
    protected Mem execute2(byte data) {
      w = data;
      register.set(w, z);
      return Mem.read(registers.pc().getAndIncrement());
    }

    @Override
    protected Mem execute3(byte data) {
      registers.ir().set(data);
      return null;
    }
  }

  public static final class DirectAccumulator extends AbstractInstruction {

    private final Registers registers;

    private byte z;
    private byte w;

    public DirectAccumulator(Registers registers) {
      this.registers = registers;
    }

    @Override
    protected Mem execute0(byte data) {
      return Mem.read(registers.pc().getAndIncrement());
    }

    @Override
    protected Mem execute1(byte data) {
      z = data;
      return Mem.read(registers.pc().getAndIncrement());
    }

    @Override
    protected Mem execute2(byte data) {
      w = data;
      short address = (short) ((Byte.toUnsignedInt(w) << 8) | Byte.toUnsignedInt(z));
      return Mem.write(address, registers.a().get());
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

  public static final class DirectStackPointer extends AbstractInstruction {

    private final Registers registers;

    private byte z;
    private byte w;

    public DirectStackPointer(Registers registers) {
      this.registers = registers;
    }

    @Override
    protected Mem execute0(byte data) {
      return Mem.read(registers.pc().getAndIncrement());
    }

    @Override
    protected Mem execute1(byte data) {
      z = data;
      return Mem.read(registers.pc().getAndIncrement());
    }

    @Override
    protected Mem execute2(byte data) {
      w = data;
      short address = (short) ((Byte.toUnsignedInt(w) << 8) | Byte.toUnsignedInt(z));
      return Mem.write(address, registers.sp().lo().get());
    }

    @Override
    protected Mem execute3(byte data) {
      short address = (short) (((Byte.toUnsignedInt(w) << 8) | Byte.toUnsignedInt(z)) + 1);
      return Mem.write(address, registers.sp().hi().get());
    }

    @Override
    protected Mem execute4(byte data) {
      return Mem.read(registers.pc().getAndIncrement());
    }

    @Override
    protected Mem execute5(byte data) {
      registers.ir().set(data);
      return null;
    }
  }

  public static final class DirectHiAccumulator extends AbstractInstruction {

    private final Registers registers;

    public DirectHiAccumulator(Registers registers) {
      this.registers = registers;
    }

    @Override
    protected Mem execute0(byte data) {
      return Mem.read(registers.pc().getAndIncrement());
    }

    @Override
    protected Mem execute1(byte data) {
      short address = (short) (0xff00 | Byte.toUnsignedInt(data));
      return Mem.write(address, registers.a().get());
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

  public static final class IndirectHiAccumulator extends AbstractInstruction {

    private final Registers registers;
    private final Register8 register;

    public IndirectHiAccumulator(Registers registers, Register8 register) {
      this.registers = registers;
      this.register = register;
    }

    @Override
    protected Mem execute0(byte data) {
      short address = (short) (0xff00 | Byte.toUnsignedInt(register.get()));
      return Mem.write(address, registers.a().get());
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

  public static final class AccumulatorDirect extends AbstractInstruction {

    private final Registers registers;

    private byte z;
    private byte w;

    public AccumulatorDirect(Registers registers) {
      this.registers = registers;
    }

    @Override
    protected Mem execute0(byte data) {
      return Mem.read(registers.pc().getAndIncrement());
    }

    @Override
    protected Mem execute1(byte data) {
      z = data;
      return Mem.read(registers.pc().getAndIncrement());
    }

    @Override
    protected Mem execute2(byte data) {
      w = data;
      short address = (short) ((Byte.toUnsignedInt(w) << 8) | Byte.toUnsignedInt(z));
      return Mem.read(address);
    }

    @Override
    protected Mem execute3(byte data) {
      registers.a().set(data);
      return Mem.read(registers.pc().getAndIncrement());
    }

    @Override
    protected Mem execute4(byte data) {
      registers.ir().set(data);
      return null;
    }
  }

  public static final class AccumulatorDirectHi extends AbstractInstruction {

    private final Registers registers;

    public AccumulatorDirectHi(Registers registers) {
      this.registers = registers;
    }

    @Override
    protected Mem execute0(byte data) {
      return Mem.read(registers.pc().getAndIncrement());
    }

    @Override
    protected Mem execute1(byte data) {
      short address = (short) (0xff00 | Byte.toUnsignedInt(data));
      return Mem.read(address);
    }

    @Override
    protected Mem execute2(byte data) {
      registers.a().set(data);
      return Mem.read(registers.pc().getAndIncrement());
    }

    @Override
    protected Mem execute3(byte data) {
      registers.ir().set(data);
      return null;
    }
  }

  public static final class StackOffset extends AbstractInstruction {

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
      Alu.Result result = Alu.add(registers.sp().lo().get(), z, false);
      registers.hl().lo().set(result.result());
      registers.f().z().set(result.z());
      registers.f().n().set(result.n());
      registers.f().h().set(result.h());
      registers.f().c().set(result.c());
      return Mem.read((short) 0);
    }

    @Override
    protected Mem execute2(byte data) {
      var result = Alu.add(registers.sp().hi().get(), (byte) (z >> 7), registers.f().c().get());
      registers.hl().hi().set(result.result());
      return Mem.read(registers.pc().getAndIncrement());
    }

    @Override
    protected Mem execute3(byte data) {
      registers.ir().set(data);
      return null;
    }
  }
}

package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.FlagsRegister;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import com.zacharyhirsch.moldygameboy.emulator.memory.MemOperation;
import com.zacharyhirsch.moldygameboy.emulator.memory.MemRead;
import java.util.function.Predicate;

public final class Cond {

  private Cond() {}

  public static Instruction c0(Registers registers, Instruction instruction) {
    return check0(FlagsRegister::getC, instruction, registers);
  }

  public static Instruction nc0(Registers registers, Instruction instruction) {
    return check0(Predicate.not(FlagsRegister::getC), instruction, registers);
  }

  public static Instruction z0(Registers registers, Instruction instruction) {
    return check0(FlagsRegister::getZ, instruction, registers);
  }

  public static Instruction nz0(Registers registers, Instruction instruction) {
    return check0(Predicate.not(FlagsRegister::getZ), instruction, registers);
  }

  public static Instruction c8(Registers registers, Instruction instruction) {
    return check8(FlagsRegister::getC, instruction, registers);
  }

  public static Instruction nc8(Registers registers, Instruction instruction) {
    return check8(Predicate.not(FlagsRegister::getC), instruction, registers);
  }

  public static Instruction z8(Registers registers, Instruction instruction) {
    return check8(FlagsRegister::getZ, instruction, registers);
  }

  public static Instruction nz8(Registers registers, Instruction instruction) {
    return check8(Predicate.not(FlagsRegister::getZ), instruction, registers);
  }

  public static Instruction c16(Registers registers, Instruction instruction) {
    return check16(FlagsRegister::getC, instruction, registers);
  }

  public static Instruction nc16(Registers registers, Instruction instruction) {
    return check16(Predicate.not(FlagsRegister::getC), instruction, registers);
  }

  public static Instruction z16(Registers registers, Instruction instruction) {
    return check16(FlagsRegister::getZ, instruction, registers);
  }

  public static Instruction nz16(Registers registers, Instruction instruction) {
    return check16(Predicate.not(FlagsRegister::getZ), instruction, registers);
  }

  private static Instruction check0(
      Predicate<FlagsRegister> predicate, Instruction instruction, Registers registers) {
    if (predicate.test(registers.f())) {
      return instruction;
    }
    return new AbstractInstruction() {
      @Override
      protected MemRead execute0(byte data) {
        return new MemRead(registers.pc().getAndIncrement());
      }

      @Override
      protected MemOperation execute1(byte data) {
        registers.ir().set(data);
        return null;
      }
    };
  }

  private static Instruction check8(
      Predicate<FlagsRegister> predicate, Instruction instruction, Registers registers) {
    if (predicate.test(registers.f())) {
      return instruction;
    }
    return new AbstractInstruction() {
      @Override
      protected MemRead execute0(byte data) {
        return new MemRead(registers.pc().getAndIncrement());
      }

      @Override
      protected MemRead execute1(byte data) {
        return new MemRead(registers.pc().getAndIncrement());
      }

      @Override
      protected MemOperation execute2(byte data) {
        registers.ir().set(data);
        return null;
      }
    };
  }

  private static Instruction check16(
      Predicate<FlagsRegister> predicate, Instruction instruction, Registers registers) {
    if (predicate.test(registers.f())) {
      return instruction;
    }
    return new AbstractInstruction() {
      @Override
      protected MemRead execute0(byte data) {
        return new MemRead(registers.pc().getAndIncrement());
      }

      @Override
      protected MemRead execute1(byte data) {
        return new MemRead(registers.pc().getAndIncrement());
      }

      @Override
      protected MemRead execute2(byte data) {
        return new MemRead(registers.pc().getAndIncrement());
      }

      @Override
      protected MemOperation execute3(byte data) {
        registers.ir().set(data);
        return null;
      }
    };
  }
}

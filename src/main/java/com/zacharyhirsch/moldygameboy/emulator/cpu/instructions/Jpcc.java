package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.FlagsRegister;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import java.util.function.Predicate;

public class Jpcc {

  public static final class Immediate implements Instruction {

    private final Registers registers;
    private final Predicate<FlagsRegister> predicate;

    private int i = 0;
    private byte z;
    private byte w;

    public Immediate(Registers registers, Predicate<FlagsRegister> predicate) {
      this.registers = registers;
      this.predicate = predicate;
    }

    @Override
    public Mem tick() {
      return switch (i++) {
        case 0 -> execute0();
        case 1 -> execute1();
        case 2 -> execute2();
        case 3 -> execute3();
        case 4 -> null;
        default -> throw new IllegalStateException();
      };
    }

    private Mem execute0() {
      return Mem.read(registers.pc().getAndIncrement(), data -> z = data);
    }

    private Mem execute1() {
      return Mem.read(registers.pc().getAndIncrement(), data -> w = data);
    }

    private Mem execute2() {
      if (!predicate.test(registers.f())) {
        i = 4;
        return Mem.read(registers.pc().getAndIncrement(), registers.ir()::set);
      }
      registers.pc().set(w, z);
      return Mem.none((short) 0x0000);
    }

    private Mem execute3() {
      return Mem.read(registers.pc().getAndIncrement(), registers.ir()::set);
    }
  }

  //  public static final class Register extends AbstractInstruction {
  //
  //    private final Registers registers;
  //
  //    public Register(Registers registers) {
  //      this.registers = registers;
  //    }
  //
  //    @Override
  //    protected Mem execute0(byte data) {
  //      registers.pc().set(registers.hl().get());
  //      return Mem.read(registers.pc().getAndIncrement());
  //    }
  //
  //    @Override
  //    protected Mem execute1(byte data) {
  //      registers.ir().set(data);
  //      return null;
  //    }
  //  }
}

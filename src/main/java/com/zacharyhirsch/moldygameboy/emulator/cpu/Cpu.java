package com.zacharyhirsch.moldygameboy.emulator.cpu;

import com.zacharyhirsch.moldygameboy.emulator.arch.UInt16Input;
import com.zacharyhirsch.moldygameboy.emulator.bus.AddressBus;
import com.zacharyhirsch.moldygameboy.emulator.bus.DataBus;
import com.zacharyhirsch.moldygameboy.emulator.cpu.alu.AluComplement;
import com.zacharyhirsch.moldygameboy.emulator.cpu.alu.AluDecrement;
import com.zacharyhirsch.moldygameboy.emulator.cpu.alu.AluFlags;
import com.zacharyhirsch.moldygameboy.emulator.cpu.alu.AluOperation;
import com.zacharyhirsch.moldygameboy.emulator.cpu.alu.AluXor;
import com.zacharyhirsch.moldygameboy.emulator.cpu.idu.IduDecrement;
import com.zacharyhirsch.moldygameboy.emulator.cpu.idu.IduIncrement;
import com.zacharyhirsch.moldygameboy.emulator.cpu.idu.IduOperation;
import com.zacharyhirsch.moldygameboy.emulator.cpu.mem.MemOperation;
import com.zacharyhirsch.moldygameboy.emulator.cpu.mem.MemRead;
import com.zacharyhirsch.moldygameboy.emulator.cpu.mem.MemWrite;
import com.zacharyhirsch.moldygameboy.emulator.memory.CpuMemory;
import java.util.function.Predicate;

public final class Cpu {

  private final Registers registers;
  private final AddressBus addressBus;
  private final DataBus dataBus;
  private final CpuMemory memory;

  private Cycle cycle;
  private MemOperation memOperation;
  private IduOperation iduOperation;
  private AluOperation aluOperation;

  public Cpu(AddressBus addressBus, DataBus dataBus, CpuMemory memory) {
    this.registers = new Registers();
    this.addressBus = addressBus;
    this.dataBus = dataBus;
    this.memory = memory;
    this.cycle = this::fetch;
    this.memOperation = null;
    this.iduOperation = null;
    this.aluOperation = null;
  }

  public void tick() {
    memOperation = null;
    iduOperation = null;
    aluOperation = null;

    cycle = cycle.execute();

    memOperation.execute(memory, addressBus, dataBus);
    if (iduOperation != null) {
      iduOperation.execute(addressBus);
    }
    if (aluOperation != null) {
      aluOperation.execute(registers.f());
    }
  }

  private Cycle fetch() {
    memOperation = new MemRead(registers.pc());
    iduOperation = new IduIncrement(registers.pc());
    return () -> {
      registers.ir().set(dataBus.get());
      return decode();
    };
  }

  private Cycle decode() {
    byte opcode = registers.ir().get();
    int octH = (opcode & 0b1100_0000) >>> 6;
    int octM = (opcode & 0b0011_1000) >>> 3;
    int octL = (opcode & 0b0000_0111) >>> 0;
    return switch (octH) {
      case 00 -> decode0(0, octM, octL);
      case 01 -> decode1(1, octM, octL);
      case 02 -> decode2(2, octM, octL);
      case 03 -> decode3(3, octM, octL);
      default -> throw new IllegalStateException();
    };
  }

  private Cycle decode0(int octH, int octM, int octL) {
    return switch (octL) {
      case 00 ->
          switch (octM) {
            case 00 -> nop();
            case 01 -> ld_indirect_imm16_sp();
            case 02 -> stop();
            case 03 -> jr_imm8();
            case 04 -> jr_cond_imm8(Predicate.not(AluFlags::isZero));
            case 05 -> jr_cond_imm8(AluFlags::isZero);
            case 06 -> jr_cond_imm8(Predicate.not(AluFlags::isCarry));
            case 07 -> jr_cond_imm8(AluFlags::isCarry);
            default -> throw new IllegalStateException();
          };
      case 01 ->
          switch (octM) {
            case 00 -> ld_r16_imm16(registers.bc());
            case 01 -> add_hl_r16(registers.bc());
            case 02 -> ld_r16_imm16(registers.de());
            case 03 -> add_hl_r16(registers.de());
            case 04 -> ld_r16_imm16(registers.hl());
            case 05 -> add_hl_r16(registers.hl());
            case 06 -> ld_r16_imm16(registers.sp());
            case 07 -> add_hl_r16(registers.sp());
            default -> throw new IllegalStateException();
          };
      case 02 ->
          switch (octM) {
            case 00 -> ld_indirect_r16mem_a(registers.bc());
            case 01 -> ld_a_indirect_r16mem(registers.bc());
            case 02 -> ld_indirect_r16mem_a(registers.de());
            case 03 -> ld_a_indirect_r16mem(registers.de());
            case 04 -> throw new UnsupportedOperationException("[hl+]"); // ld_indirect_r16mem_a
            case 05 -> throw new UnsupportedOperationException("[hl+]"); // ld_a_indirect_r16mem
            case 06 -> throw new UnsupportedOperationException("[hl-]"); // ld_indirect_r16mem_a
            case 07 -> throw new UnsupportedOperationException("[hl-]"); // ld_a_indirect_r16mem
            default -> throw new IllegalStateException();
          };
      case 03 ->
          switch (octM) {
            case 00 -> inc_r16(registers.bc());
            case 01 -> dec_r16(registers.bc());
            case 02 -> inc_r16(registers.de());
            case 03 -> dec_r16(registers.de());
            case 04 -> inc_r16(registers.hl());
            case 05 -> dec_r16(registers.hl());
            case 06 -> inc_r16(registers.sp());
            case 07 -> dec_r16(registers.sp());
            default -> throw new IllegalStateException();
          };
      case 04 ->
          switch (octM) {
            case 0 -> inc_r8(registers.b());
            case 1 -> inc_r8(registers.c());
            case 2 -> inc_r8(registers.d());
            case 3 -> inc_r8(registers.e());
            case 4 -> inc_r8(registers.h());
            case 5 -> inc_r8(registers.l());
            case 6 -> throw new UnsupportedOperationException("[hl]"); // inc_r8
            case 7 -> inc_r8(registers.a());
            default -> throw new IllegalStateException();
          };
      case 05 ->
          switch (octM) {
            case 0 -> dec_r8(registers.b());
            case 1 -> dec_r8(registers.c());
            case 2 -> dec_r8(registers.d());
            case 3 -> dec_r8(registers.e());
            case 4 -> dec_r8(registers.h());
            case 5 -> dec_r8(registers.l());
            case 6 -> throw new UnsupportedOperationException("[hl]"); // dec_r8
            case 7 -> dec_r8(registers.a());
            default -> throw new IllegalStateException();
          };
      case 06 ->
          switch (octM) {
            case 0 -> ld_r8_imm8(registers.b());
            case 1 -> ld_r8_imm8(registers.c());
            case 2 -> ld_r8_imm8(registers.d());
            case 3 -> ld_r8_imm8(registers.e());
            case 4 -> ld_r8_imm8(registers.h());
            case 5 -> ld_r8_imm8(registers.l());
            case 6 -> throw new UnsupportedOperationException("[hl]"); // ld_r8_imm8
            case 7 -> ld_r8_imm8(registers.a());
            default -> throw new IllegalStateException();
          };
      case 07 ->
          switch (octM) {
            case 00 -> rlca();
            case 01 -> rrca();
            case 02 -> rla();
            case 03 -> rra();
            case 04 -> daa();
            case 05 -> cpl();
            case 06 -> scf();
            case 07 -> ccf();
            default -> throw new IllegalStateException();
          };
      default -> throw new IllegalStateException();
    };
  }

  private Cycle decode1(int octH, int octM, int octL) {
    return null;
  }

  private Cycle decode2(int octH, int octM, int octL) {
    return null;
  }

  private Cycle decode3(int octH, int octM, int octL) {
    return null;
  }

  private Cycle ld_r8_imm8(Register8 register) {
    return null;
  }

  private Cycle dec_r8(Register8 register) {
    return null;
  }

  private Cycle inc_r8(Register8 register) {
    return null;
  }

  private Cycle dec_r16(Register16 register) {
    return null;
  }

  private Cycle inc_r16(Register16 register) {
    return null;
  }

  private Cycle ld_a_indirect_r16mem(Register16 register) {
    return null;
  }

  private Cycle ld_indirect_r16mem_a(Register16 register) {
    return null;
  }

  private Cycle add_hl_r16(Register16 register) {
    return null;
  }

  private Cycle ld_r16_imm16(Register16 register) {
    return null;
  }

  private Cycle jr_cond_imm8(Predicate<Byte> cond) {
    return null;
  }

  private Cycle jr_imm8() {
    return null;
  }

  private Cycle stop() {
    return null;
  }

  private Cycle ld_indirect_imm16_sp() {
    return null;
  }

  private Cycle decode_old() {
    byte opcode = registers.ir().get();
    return switch (Byte.toUnsignedInt(opcode)) {
      case 0x00 -> nop();
      case 0x01 -> loadFromImmediate(registers.bc());
      case 0x02 -> loadIndirectFromAccumulator(registers.bc());
      case 0x06 -> loadFromImmediate(registers.b());
      case 0x0d -> decrementRegister(registers.c());
      case 0x0e -> loadFromImmediate(registers.c());
      case 0x11 -> loadFromImmediate(registers.de());
      case 0x12 -> loadIndirectFromAccumulator(registers.de());
      case 0x16 -> loadFromImmediate(registers.d());
      case 0x1e -> loadFromImmediate(registers.e());
      case 0x20 -> jumpRelativeIfNonZero();
      case 0x21 -> loadFromImmediate(registers.hl());
      case 0x22 -> loadIndirectHlIncrementFromAccumulator();
      case 0x26 -> loadFromImmediate(registers.h());
      case 0x2e -> loadFromImmediate(registers.l());
      case 0x2f -> complement();
      case 0x31 -> loadFromImmediate(registers.sp());
      case 0x32 -> loadIndirectHlDecrementFromAccumulator();
      case 0x36 -> loadIndirectFromImmediate(registers.hl());
      case 0x3e -> loadFromImmediate(registers.a());
      case 0xaf -> xorRegister(registers.a());
      case 0xc3 -> jumpAbsoluteImmediate();
      case 0xcd -> callFunction();
      case 0xe0 -> loadHiFromAccumulator();
      default -> throw new InvalidOpcodeError(opcode);
    };
  }

  private Cycle nop() {
    return fetch();
  }

  private Cycle loadFromImmediate(Register8 register) {
    memOperation = new MemRead(registers.pc());
    iduOperation = new IduIncrement(registers.pc());
    return () -> {
      register.set(dataBus.get());
      return fetch();
    };
  }

  private Cycle loadFromImmediate(Register16 register) {
    memOperation = new MemRead(registers.pc());
    iduOperation = new IduIncrement(registers.pc());
    return () -> {
      byte lo = dataBus.get();
      memOperation = new MemRead(registers.pc());
      iduOperation = new IduIncrement(registers.pc());
      return () -> {
        register.set(dataBus.get(), lo);
        return fetch();
      };
    };
  }

  private Cycle loadHiFromAccumulator() {
    memOperation = new MemRead(registers.pc());
    iduOperation = new IduIncrement(registers.pc());
    return () -> {
      memOperation = new MemWrite((byte) 0xff, dataBus.get(), registers.a());
      return this::fetch;
    };
  }

  private Cycle loadIndirectFromImmediate(Register16 register) {
    memOperation = new MemRead(registers.pc());
    iduOperation = new IduIncrement(registers.pc());
    return () -> {
      memOperation = new MemWrite(register, dataBus);
      return this::fetch;
    };
  }

  private Cycle loadIndirectFromAccumulator(Register16 register) {
    memOperation = new MemWrite(register, registers.a());
    return this::fetch;
  }

  private Cycle loadIndirectHlIncrementFromAccumulator() {
    memOperation = new MemWrite(registers.hl(), registers.a());
    iduOperation = new IduIncrement(registers.hl());
    return this::fetch;
  }

  private Cycle loadIndirectHlDecrementFromAccumulator() {
    memOperation = new MemWrite(registers.hl(), registers.a());
    iduOperation = new IduDecrement(registers.hl());
    return this::fetch;
  }

  private Cycle decrementRegister(Register8 register) {
    aluOperation = new AluDecrement(register, register);
    return fetch();
  }

  private Cycle complement() {
    aluOperation = new AluComplement(registers.a(), registers.a());
    return fetch();
  }

  private Cycle xorRegister(Register8 register) {
    aluOperation = new AluXor(registers.a(), register, registers.a());
    return fetch();
  }

  private Cycle jumpAbsoluteImmediate() {
    memOperation = new MemRead(registers.pc());
    iduOperation = new IduIncrement(registers.pc());
    return () -> {
      byte lo = dataBus.get();
      memOperation = new MemRead(registers.pc());
      iduOperation = new IduIncrement(registers.pc());
      return () -> {
        registers.pc().set(dataBus.get(), lo);
        memOperation = new MemRead(UInt16Input.of(0));
        return this::fetch;
      };
    };
  }

  private Cycle jumpRelativeIfNonZero() {
    //    memOperation = new MemRead(registers.pc());
    //    iduOperation = IduOperation.PC_INCREMENT;
    //    return () -> {
    //      UInt8 rel = dataBus.get();
    //      if (flags.isZ()) {
    //        return fetch();
    //      }
    //      addressBus.set(new UInt16(0, registers.pc().hi().get()));
    //      return () -> {
    //        registers.pc().set(new UInt16(dataBus.get(), lo));
    //        read(UInt16.ZERO);
    //        return this::fetch;
    //      };
    //    };
    throw new UnsupportedOperationException();
  }

  private Cycle callFunction() {
    memOperation = new MemRead(registers.pc());
    iduOperation = new IduIncrement(registers.pc());
    return () -> {
      byte lo = dataBus.get();
      memOperation = new MemRead(registers.pc());
      iduOperation = new IduIncrement(registers.pc());
      return () -> {
        byte hi = dataBus.get();
        memOperation = new MemRead(registers.sp());
        iduOperation = new IduDecrement(registers.sp());
        return () -> {
          memOperation = new MemWrite(registers.sp(), registers.pc().hi());
          iduOperation = new IduDecrement(registers.sp());
          return () -> {
            memOperation = new MemWrite(registers.sp(), registers.pc().lo());
            registers.pc().set(hi, lo);
            return this::fetch;
          };
        };
      };
    };
  }
}

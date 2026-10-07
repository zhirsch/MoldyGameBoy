package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.arch.Bus;
import com.zacharyhirsch.moldygameboy.emulator.cpu.alu.Alu;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.A;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.F;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.HL;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.R;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.RR;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Sp;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.WZ;

public final class CpuOpcodeDecoder {

  private CpuOpcodeDecoder() {}

  public static Instruction decode(Registers registers) {
    return switch (Byte.toUnsignedInt(registers.ir().get())) {
      case 0x00 -> new Nop();
      case 0x01 -> new Ld.RegisterImmediate16(registers.bc());
      case 0x02 -> new Ld.IndirectRegister(registers.a(), registers.bc());
      case 0x03 -> new Inc.R16(registers.bc());
      case 0x04 -> create(Alu::inc, registers.b());
      case 0x05 -> create(Alu::dec, registers.b());
      case 0x06 -> new Ld.RegisterImmediate8(registers.b());
      case 0x07 -> create(Alu::rlca);
      case 0x08 -> new Ld.DirectStackPointer();
      case 0x09 -> create(Alu::add16, registers.bc());
      case 0x0a -> new Ld.AccumulatorIndirect(registers.bc());
      case 0x0b -> new Dec.R16(registers.bc());
      case 0x0c -> create(Alu::inc, registers.c());
      case 0x0d -> create(Alu::dec, registers.c());
      case 0x0e -> new Ld.RegisterImmediate8(registers.c());
      case 0x0f -> create(Alu::rrca);
      //      case 0x10 -> new Stop(registers);
      case 0x11 -> new Ld.RegisterImmediate16(registers.de());
      case 0x12 -> new Ld.IndirectRegister(registers.a(), registers.de());
      case 0x13 -> new Inc.R16(registers.de());
      case 0x14 -> create(Alu::inc, registers.d());
      case 0x15 -> create(Alu::dec, registers.d());
      case 0x16 -> new Ld.RegisterImmediate8(registers.d());
      case 0x17 -> create(Alu::rla);
      case 0x18 -> new Jr();
      case 0x19 -> create(Alu::add16, registers.de());
      case 0x1a -> new Ld.AccumulatorIndirect(registers.de());
      case 0x1b -> new Dec.R16(registers.de());
      case 0x1c -> create(Alu::inc, registers.e());
      case 0x1d -> create(Alu::dec, registers.e());
      case 0x1e -> new Ld.RegisterImmediate8(registers.e());
      case 0x1f -> create(Alu::rra);
      case 0x20 -> new Jrcc(flags -> !flags.z().get());
      case 0x21 -> new Ld.RegisterImmediate16(registers.hl());
      case 0x22 -> new Ld.IndirectIncrementAccumulator();
      case 0x23 -> new Inc.R16(registers.hl());
      case 0x24 -> create(Alu::inc, registers.h());
      case 0x25 -> create(Alu::dec, registers.h());
      case 0x26 -> new Ld.RegisterImmediate8(registers.h());
      case 0x27 -> new Daa();
      case 0x28 -> new Jrcc(flags -> flags.z().get());
      case 0x29 -> create(Alu::add16, registers.hl());
      case 0x2a -> new Ld.AccumulatorIndirectIncrement();
      case 0x2b -> new Dec.R16(registers.hl());
      case 0x2c -> create(Alu::inc, registers.l());
      case 0x2d -> create(Alu::dec, registers.l());
      case 0x2e -> new Ld.RegisterImmediate8(registers.l());
      case 0x2f -> create(Alu::cpl);
      case 0x30 -> new Jrcc(flags -> !flags.c().get());
      case 0x31 -> new Ld.RegisterImmediate16(registers.sp());
      case 0x32 -> new Ld.IndirectDecrementAccumulator();
      case 0x33 -> new Inc.R16(registers.sp());
      case 0x34 -> create(Alu::inc, new ReadModifyWriteIndirect());
      case 0x35 -> create(Alu::dec, new ReadModifyWriteIndirect());
      case 0x36 -> new Ld.ImmediateIndirect();
      case 0x37 -> create(Alu::scf);
      case 0x38 -> new Jrcc(flags -> flags.c().get());
      case 0x39 -> create(Alu::add16, registers.sp());
      case 0x3a -> new Ld.AccumulatorIndirectDecrement();
      case 0x3b -> new Dec.R16(registers.sp());
      case 0x3c -> create(Alu::inc, registers.a());
      case 0x3d -> create(Alu::dec, registers.a());
      case 0x3e -> new Ld.RegisterImmediate8(registers.a());
      case 0x3f -> create(Alu::ccf);
      case 0x40 -> new Ld.RegisterRegister(registers.b(), registers.b());
      case 0x41 -> new Ld.RegisterRegister(registers.c(), registers.b());
      case 0x42 -> new Ld.RegisterRegister(registers.d(), registers.b());
      case 0x43 -> new Ld.RegisterRegister(registers.e(), registers.b());
      case 0x44 -> new Ld.RegisterRegister(registers.h(), registers.b());
      case 0x45 -> new Ld.RegisterRegister(registers.l(), registers.b());
      case 0x46 -> new Ld.RegisterIndirect(registers.b());
      case 0x47 -> new Ld.RegisterRegister(registers.a(), registers.b());
      case 0x48 -> new Ld.RegisterRegister(registers.b(), registers.c());
      case 0x49 -> new Ld.RegisterRegister(registers.c(), registers.c());
      case 0x4a -> new Ld.RegisterRegister(registers.d(), registers.c());
      case 0x4b -> new Ld.RegisterRegister(registers.e(), registers.c());
      case 0x4c -> new Ld.RegisterRegister(registers.h(), registers.c());
      case 0x4d -> new Ld.RegisterRegister(registers.l(), registers.c());
      case 0x4e -> new Ld.RegisterIndirect(registers.c());
      case 0x4f -> new Ld.RegisterRegister(registers.a(), registers.c());
      case 0x50 -> new Ld.RegisterRegister(registers.b(), registers.d());
      case 0x51 -> new Ld.RegisterRegister(registers.c(), registers.d());
      case 0x52 -> new Ld.RegisterRegister(registers.d(), registers.d());
      case 0x53 -> new Ld.RegisterRegister(registers.e(), registers.d());
      case 0x54 -> new Ld.RegisterRegister(registers.h(), registers.d());
      case 0x55 -> new Ld.RegisterRegister(registers.l(), registers.d());
      case 0x56 -> new Ld.RegisterIndirect(registers.d());
      case 0x57 -> new Ld.RegisterRegister(registers.a(), registers.d());
      case 0x58 -> new Ld.RegisterRegister(registers.b(), registers.e());
      case 0x59 -> new Ld.RegisterRegister(registers.c(), registers.e());
      case 0x5a -> new Ld.RegisterRegister(registers.d(), registers.e());
      case 0x5b -> new Ld.RegisterRegister(registers.e(), registers.e());
      case 0x5c -> new Ld.RegisterRegister(registers.h(), registers.e());
      case 0x5d -> new Ld.RegisterRegister(registers.l(), registers.e());
      case 0x5e -> new Ld.RegisterIndirect(registers.e());
      case 0x5f -> new Ld.RegisterRegister(registers.a(), registers.e());
      case 0x60 -> new Ld.RegisterRegister(registers.b(), registers.h());
      case 0x61 -> new Ld.RegisterRegister(registers.c(), registers.h());
      case 0x62 -> new Ld.RegisterRegister(registers.d(), registers.h());
      case 0x63 -> new Ld.RegisterRegister(registers.e(), registers.h());
      case 0x64 -> new Ld.RegisterRegister(registers.h(), registers.h());
      case 0x65 -> new Ld.RegisterRegister(registers.l(), registers.h());
      case 0x66 -> new Ld.RegisterIndirect(registers.h());
      case 0x67 -> new Ld.RegisterRegister(registers.a(), registers.h());
      case 0x68 -> new Ld.RegisterRegister(registers.b(), registers.l());
      case 0x69 -> new Ld.RegisterRegister(registers.c(), registers.l());
      case 0x6a -> new Ld.RegisterRegister(registers.d(), registers.l());
      case 0x6b -> new Ld.RegisterRegister(registers.e(), registers.l());
      case 0x6c -> new Ld.RegisterRegister(registers.h(), registers.l());
      case 0x6d -> new Ld.RegisterRegister(registers.l(), registers.l());
      case 0x6e -> new Ld.RegisterIndirect(registers.l());
      case 0x6f -> new Ld.RegisterRegister(registers.a(), registers.l());
      case 0x70 -> new Ld.IndirectRegister(registers.b(), registers.hl());
      case 0x71 -> new Ld.IndirectRegister(registers.c(), registers.hl());
      case 0x72 -> new Ld.IndirectRegister(registers.d(), registers.hl());
      case 0x73 -> new Ld.IndirectRegister(registers.e(), registers.hl());
      case 0x74 -> new Ld.IndirectRegister(registers.h(), registers.hl());
      case 0x75 -> new Ld.IndirectRegister(registers.l(), registers.hl());
      //      case 0x76 -> new Halt(registers);
      case 0x77 -> new Ld.IndirectRegister(registers.a(), registers.hl());
      case 0x78 -> new Ld.RegisterRegister(registers.b(), registers.a());
      case 0x79 -> new Ld.RegisterRegister(registers.c(), registers.a());
      case 0x7a -> new Ld.RegisterRegister(registers.d(), registers.a());
      case 0x7b -> new Ld.RegisterRegister(registers.e(), registers.a());
      case 0x7c -> new Ld.RegisterRegister(registers.h(), registers.a());
      case 0x7d -> new Ld.RegisterRegister(registers.l(), registers.a());
      case 0x7e -> new Ld.RegisterIndirect(registers.a());
      case 0x7f -> new Ld.RegisterRegister(registers.a(), registers.a());
      case 0x80 -> create(Alu::add, registers.b());
      case 0x81 -> create(Alu::add, registers.c());
      case 0x82 -> create(Alu::add, registers.d());
      case 0x83 -> create(Alu::add, registers.e());
      case 0x84 -> create(Alu::add, registers.h());
      case 0x85 -> create(Alu::add, registers.l());
      case 0x86 -> create(Alu::add, new ReadIndirect());
      case 0x87 -> create(Alu::add, registers.a());
      case 0x88 -> create(Alu::adc, registers.b());
      case 0x89 -> create(Alu::adc, registers.c());
      case 0x8a -> create(Alu::adc, registers.d());
      case 0x8b -> create(Alu::adc, registers.e());
      case 0x8c -> create(Alu::adc, registers.h());
      case 0x8d -> create(Alu::adc, registers.l());
      case 0x8e -> create(Alu::adc, new ReadIndirect());
      case 0x8f -> create(Alu::adc, registers.a());
      case 0x90 -> create(Alu::sub, registers.b());
      case 0x91 -> create(Alu::sub, registers.c());
      case 0x92 -> create(Alu::sub, registers.d());
      case 0x93 -> create(Alu::sub, registers.e());
      case 0x94 -> create(Alu::sub, registers.h());
      case 0x95 -> create(Alu::sub, registers.l());
      case 0x96 -> create(Alu::sub, new ReadIndirect());
      case 0x97 -> create(Alu::sub, registers.a());
      case 0x98 -> create(Alu::sbc, registers.b());
      case 0x99 -> create(Alu::sbc, registers.c());
      case 0x9a -> create(Alu::sbc, registers.d());
      case 0x9b -> create(Alu::sbc, registers.e());
      case 0x9c -> create(Alu::sbc, registers.h());
      case 0x9d -> create(Alu::sbc, registers.l());
      case 0x9e -> create(Alu::sbc, new ReadIndirect());
      case 0x9f -> create(Alu::sbc, registers.a());
      case 0xa0 -> create(Alu::and, registers.b());
      case 0xa1 -> create(Alu::and, registers.c());
      case 0xa2 -> create(Alu::and, registers.d());
      case 0xa3 -> create(Alu::and, registers.e());
      case 0xa4 -> create(Alu::and, registers.h());
      case 0xa5 -> create(Alu::and, registers.l());
      case 0xa6 -> create(Alu::and, new ReadIndirect());
      case 0xa7 -> create(Alu::and, registers.a());
      case 0xa8 -> create(Alu::xor, registers.b());
      case 0xa9 -> create(Alu::xor, registers.c());
      case 0xaa -> create(Alu::xor, registers.d());
      case 0xab -> create(Alu::xor, registers.e());
      case 0xac -> create(Alu::xor, registers.h());
      case 0xad -> create(Alu::xor, registers.l());
      case 0xae -> create(Alu::xor, new ReadIndirect());
      case 0xaf -> create(Alu::xor, registers.a());
      case 0xb0 -> create(Alu::or, registers.b());
      case 0xb1 -> create(Alu::or, registers.c());
      case 0xb2 -> create(Alu::or, registers.d());
      case 0xb3 -> create(Alu::or, registers.e());
      case 0xb4 -> create(Alu::or, registers.h());
      case 0xb5 -> create(Alu::or, registers.l());
      case 0xb6 -> create(Alu::or, new ReadIndirect());
      case 0xb7 -> create(Alu::or, registers.a());
      case 0xb8 -> create(Alu::cp, registers.b());
      case 0xb9 -> create(Alu::cp, registers.c());
      case 0xba -> create(Alu::cp, registers.d());
      case 0xbb -> create(Alu::cp, registers.e());
      case 0xbc -> create(Alu::cp, registers.h());
      case 0xbd -> create(Alu::cp, registers.l());
      case 0xbe -> create(Alu::cp, new ReadIndirect());
      case 0xbf -> create(Alu::cp, registers.a());
      case 0xc0 -> new Retcc(flags -> !flags.z().get());
      case 0xc1 -> new Pop(registers.bc());
      case 0xc2 -> new Jpcc(flags -> !flags.z().get());
      case 0xc3 -> new Jp.Immediate();
      case 0xc4 -> new Callcc(flags -> !flags.z().get());
      case 0xc5 -> new Push(registers.bc());
      case 0xc6 -> create(Alu::add, new N8());
      case 0xc7 -> new Rst((short) 0x00);
      case 0xc8 -> new Retcc(flags -> flags.z().get());
      case 0xc9 -> new Ret();
      case 0xca -> new Jpcc(flags -> flags.z().get());
      case 0xcc -> new Callcc(flags -> flags.z().get());
      case 0xcd -> new Call();
      case 0xce -> create(Alu::adc, new N8());
      case 0xcf -> new Rst((short) 0x08);
      case 0xd0 -> new Retcc(flags -> !flags.c().get());
      case 0xd1 -> new Pop(registers.de());
      case 0xd2 -> new Jpcc(flags -> !flags.c().get());
      case 0xd4 -> new Callcc(flags -> !flags.c().get());
      case 0xd5 -> new Push(registers.de());
      case 0xd6 -> create(Alu::sub, new N8());
      case 0xd7 -> new Rst((short) 0x10);
      case 0xd8 -> new Retcc(flags -> flags.c().get());
      case 0xd9 -> new Reti();
      case 0xda -> new Jpcc(flags -> flags.c().get());
      case 0xdc -> new Callcc(flags -> flags.c().get());
      case 0xde -> create(Alu::sbc, new N8());
      case 0xdf -> new Rst((short) 0x18);
      case 0xe0 -> new Ld.DirectHiAccumulator();
      case 0xe1 -> new Pop(registers.hl());
      case 0xe2 -> new Ld.IndirectHiAccumulator();
      case 0xe5 -> new Push(registers.hl());
      case 0xe6 -> create(Alu::and, new N8());
      case 0xe7 -> new Rst((short) 0x20);
      case 0xe8 -> create(Alu::add16, new E8());
      case 0xe9 -> new Jp.Register();
      case 0xea -> new Ld.DirectAccumulator();
      case 0xee -> create(Alu::xor, new N8());
      case 0xef -> new Rst((short) 0x28);
      case 0xf0 -> new Ld.AccumulatorDirectHi();
      case 0xf1 -> new Pop(registers.af());
      case 0xf2 -> new Ld.AccumulatorIndirectHi();
      case 0xf3 -> new Di();
      case 0xf5 -> new Push(registers.af());
      case 0xf6 -> create(Alu::or, new N8());
      case 0xf7 -> new Rst((short) 0x30);
      case 0xf8 -> new Ld.StackOffset();
      case 0xf9 -> new Ld.RegisterRegister16(registers.hl(), registers.sp());
      case 0xfa -> new Ld.AccumulatorDirect();
      case 0xfb -> new Ei();
      case 0xfe -> create(Alu::cp, new N8());
      case 0xff -> new Rst((short) 0x38);
      default -> throw new InvalidOpcodeError(registers.ir().get());
    };
  }

  public static Instruction decodePrefixed(Registers registers) {
    return switch (Byte.toUnsignedInt(registers.ir().get())) {
      case 0x00 -> create(Alu::rlc, registers.b());
      case 0x01 -> create(Alu::rlc, registers.c());
      case 0x02 -> create(Alu::rlc, registers.d());
      case 0x03 -> create(Alu::rlc, registers.e());
      case 0x04 -> create(Alu::rlc, registers.h());
      case 0x05 -> create(Alu::rlc, registers.l());
      case 0x06 -> create(Alu::rlc, new ReadModifyWriteIndirect());
      case 0x07 -> create(Alu::rlc, registers.a());
      case 0x08 -> create(Alu::rrc, registers.b());
      case 0x09 -> create(Alu::rrc, registers.c());
      case 0x0a -> create(Alu::rrc, registers.d());
      case 0x0b -> create(Alu::rrc, registers.e());
      case 0x0c -> create(Alu::rrc, registers.h());
      case 0x0d -> create(Alu::rrc, registers.l());
      case 0x0e -> create(Alu::rrc, new ReadModifyWriteIndirect());
      case 0x0f -> create(Alu::rrc, registers.a());
      case 0x10 -> create(Alu::rl, registers.b());
      case 0x11 -> create(Alu::rl, registers.c());
      case 0x12 -> create(Alu::rl, registers.d());
      case 0x13 -> create(Alu::rl, registers.e());
      case 0x14 -> create(Alu::rl, registers.h());
      case 0x15 -> create(Alu::rl, registers.l());
      case 0x16 -> create(Alu::rl, new ReadModifyWriteIndirect());
      case 0x17 -> create(Alu::rl, registers.a());
      case 0x18 -> create(Alu::rr, registers.b());
      case 0x19 -> create(Alu::rr, registers.c());
      case 0x1a -> create(Alu::rr, registers.d());
      case 0x1b -> create(Alu::rr, registers.e());
      case 0x1c -> create(Alu::rr, registers.h());
      case 0x1d -> create(Alu::rr, registers.l());
      case 0x1e -> create(Alu::rr, new ReadModifyWriteIndirect());
      case 0x1f -> create(Alu::rr, registers.a());
      case 0x20 -> create(Alu::sla, registers.b());
      case 0x21 -> create(Alu::sla, registers.c());
      case 0x22 -> create(Alu::sla, registers.d());
      case 0x23 -> create(Alu::sla, registers.e());
      case 0x24 -> create(Alu::sla, registers.h());
      case 0x25 -> create(Alu::sla, registers.l());
      case 0x26 -> create(Alu::sla, new ReadModifyWriteIndirect());
      case 0x27 -> create(Alu::sla, registers.a());
      case 0x28 -> create(Alu::sra, registers.b());
      case 0x29 -> create(Alu::sra, registers.c());
      case 0x2a -> create(Alu::sra, registers.d());
      case 0x2b -> create(Alu::sra, registers.e());
      case 0x2c -> create(Alu::sra, registers.h());
      case 0x2d -> create(Alu::sra, registers.l());
      case 0x2e -> create(Alu::sra, new ReadModifyWriteIndirect());
      case 0x2f -> create(Alu::sra, registers.a());
      case 0x30 -> create(Alu::swap, registers.b());
      case 0x31 -> create(Alu::swap, registers.c());
      case 0x32 -> create(Alu::swap, registers.d());
      case 0x33 -> create(Alu::swap, registers.e());
      case 0x34 -> create(Alu::swap, registers.h());
      case 0x35 -> create(Alu::swap, registers.l());
      case 0x36 -> create(Alu::swap, new ReadModifyWriteIndirect());
      case 0x37 -> create(Alu::swap, registers.a());
      case 0x38 -> create(Alu::srl, registers.b());
      case 0x39 -> create(Alu::srl, registers.c());
      case 0x3a -> create(Alu::srl, registers.d());
      case 0x3b -> create(Alu::srl, registers.e());
      case 0x3c -> create(Alu::srl, registers.h());
      case 0x3d -> create(Alu::srl, registers.l());
      case 0x3e -> create(Alu::srl, new ReadModifyWriteIndirect());
      case 0x3f -> create(Alu::srl, registers.a());
      case 0x40 -> create(partial(Alu::bit, 0), registers.b());
      case 0x41 -> create(partial(Alu::bit, 0), registers.c());
      case 0x42 -> create(partial(Alu::bit, 0), registers.d());
      case 0x43 -> create(partial(Alu::bit, 0), registers.e());
      case 0x44 -> create(partial(Alu::bit, 0), registers.h());
      case 0x45 -> create(partial(Alu::bit, 0), registers.l());
      case 0x46 -> create(partial(Alu::bit, 0), new ReadIndirect());
      case 0x47 -> create(partial(Alu::bit, 0), registers.a());
      case 0x48 -> create(partial(Alu::bit, 1), registers.b());
      case 0x49 -> create(partial(Alu::bit, 1), registers.c());
      case 0x4a -> create(partial(Alu::bit, 1), registers.d());
      case 0x4b -> create(partial(Alu::bit, 1), registers.e());
      case 0x4c -> create(partial(Alu::bit, 1), registers.h());
      case 0x4d -> create(partial(Alu::bit, 1), registers.l());
      case 0x4e -> create(partial(Alu::bit, 1), new ReadIndirect());
      case 0x4f -> create(partial(Alu::bit, 1), registers.a());
      case 0x50 -> create(partial(Alu::bit, 2), registers.b());
      case 0x51 -> create(partial(Alu::bit, 2), registers.c());
      case 0x52 -> create(partial(Alu::bit, 2), registers.d());
      case 0x53 -> create(partial(Alu::bit, 2), registers.e());
      case 0x54 -> create(partial(Alu::bit, 2), registers.h());
      case 0x55 -> create(partial(Alu::bit, 2), registers.l());
      case 0x56 -> create(partial(Alu::bit, 2), new ReadIndirect());
      case 0x57 -> create(partial(Alu::bit, 2), registers.a());
      case 0x58 -> create(partial(Alu::bit, 3), registers.b());
      case 0x59 -> create(partial(Alu::bit, 3), registers.c());
      case 0x5a -> create(partial(Alu::bit, 3), registers.d());
      case 0x5b -> create(partial(Alu::bit, 3), registers.e());
      case 0x5c -> create(partial(Alu::bit, 3), registers.h());
      case 0x5d -> create(partial(Alu::bit, 3), registers.l());
      case 0x5e -> create(partial(Alu::bit, 3), new ReadIndirect());
      case 0x5f -> create(partial(Alu::bit, 3), registers.a());
      case 0x60 -> create(partial(Alu::bit, 4), registers.b());
      case 0x61 -> create(partial(Alu::bit, 4), registers.c());
      case 0x62 -> create(partial(Alu::bit, 4), registers.d());
      case 0x63 -> create(partial(Alu::bit, 4), registers.e());
      case 0x64 -> create(partial(Alu::bit, 4), registers.h());
      case 0x65 -> create(partial(Alu::bit, 4), registers.l());
      case 0x66 -> create(partial(Alu::bit, 4), new ReadIndirect());
      case 0x67 -> create(partial(Alu::bit, 4), registers.a());
      case 0x68 -> create(partial(Alu::bit, 5), registers.b());
      case 0x69 -> create(partial(Alu::bit, 5), registers.c());
      case 0x6a -> create(partial(Alu::bit, 5), registers.d());
      case 0x6b -> create(partial(Alu::bit, 5), registers.e());
      case 0x6c -> create(partial(Alu::bit, 5), registers.h());
      case 0x6d -> create(partial(Alu::bit, 5), registers.l());
      case 0x6e -> create(partial(Alu::bit, 5), new ReadIndirect());
      case 0x6f -> create(partial(Alu::bit, 5), registers.a());
      case 0x70 -> create(partial(Alu::bit, 6), registers.b());
      case 0x71 -> create(partial(Alu::bit, 6), registers.c());
      case 0x72 -> create(partial(Alu::bit, 6), registers.d());
      case 0x73 -> create(partial(Alu::bit, 6), registers.e());
      case 0x74 -> create(partial(Alu::bit, 6), registers.h());
      case 0x75 -> create(partial(Alu::bit, 6), registers.l());
      case 0x76 -> create(partial(Alu::bit, 6), new ReadIndirect());
      case 0x77 -> create(partial(Alu::bit, 6), registers.a());
      case 0x78 -> create(partial(Alu::bit, 7), registers.b());
      case 0x79 -> create(partial(Alu::bit, 7), registers.c());
      case 0x7a -> create(partial(Alu::bit, 7), registers.d());
      case 0x7b -> create(partial(Alu::bit, 7), registers.e());
      case 0x7c -> create(partial(Alu::bit, 7), registers.h());
      case 0x7d -> create(partial(Alu::bit, 7), registers.l());
      case 0x7e -> create(partial(Alu::bit, 7), new ReadIndirect());
      case 0x7f -> create(partial(Alu::bit, 7), registers.a());
      case 0x80 -> create(partial(Alu::res, 0), registers.b());
      case 0x81 -> create(partial(Alu::res, 0), registers.c());
      case 0x82 -> create(partial(Alu::res, 0), registers.d());
      case 0x83 -> create(partial(Alu::res, 0), registers.e());
      case 0x84 -> create(partial(Alu::res, 0), registers.h());
      case 0x85 -> create(partial(Alu::res, 0), registers.l());
      case 0x86 -> create(partial(Alu::res, 0), new ReadModifyWriteIndirect());
      case 0x87 -> create(partial(Alu::res, 0), registers.a());
      case 0x88 -> create(partial(Alu::res, 1), registers.b());
      case 0x89 -> create(partial(Alu::res, 1), registers.c());
      case 0x8a -> create(partial(Alu::res, 1), registers.d());
      case 0x8b -> create(partial(Alu::res, 1), registers.e());
      case 0x8c -> create(partial(Alu::res, 1), registers.h());
      case 0x8d -> create(partial(Alu::res, 1), registers.l());
      case 0x8e -> create(partial(Alu::res, 1), new ReadModifyWriteIndirect());
      case 0x8f -> create(partial(Alu::res, 1), registers.a());
      case 0x90 -> create(partial(Alu::res, 2), registers.b());
      case 0x91 -> create(partial(Alu::res, 2), registers.c());
      case 0x92 -> create(partial(Alu::res, 2), registers.d());
      case 0x93 -> create(partial(Alu::res, 2), registers.e());
      case 0x94 -> create(partial(Alu::res, 2), registers.h());
      case 0x95 -> create(partial(Alu::res, 2), registers.l());
      case 0x96 -> create(partial(Alu::res, 2), new ReadModifyWriteIndirect());
      case 0x97 -> create(partial(Alu::res, 2), registers.a());
      case 0x98 -> create(partial(Alu::res, 3), registers.b());
      case 0x99 -> create(partial(Alu::res, 3), registers.c());
      case 0x9a -> create(partial(Alu::res, 3), registers.d());
      case 0x9b -> create(partial(Alu::res, 3), registers.e());
      case 0x9c -> create(partial(Alu::res, 3), registers.h());
      case 0x9d -> create(partial(Alu::res, 3), registers.l());
      case 0x9e -> create(partial(Alu::res, 3), new ReadModifyWriteIndirect());
      case 0x9f -> create(partial(Alu::res, 3), registers.a());
      case 0xa0 -> create(partial(Alu::res, 4), registers.b());
      case 0xa1 -> create(partial(Alu::res, 4), registers.c());
      case 0xa2 -> create(partial(Alu::res, 4), registers.d());
      case 0xa3 -> create(partial(Alu::res, 4), registers.e());
      case 0xa4 -> create(partial(Alu::res, 4), registers.h());
      case 0xa5 -> create(partial(Alu::res, 4), registers.l());
      case 0xa6 -> create(partial(Alu::res, 4), new ReadModifyWriteIndirect());
      case 0xa7 -> create(partial(Alu::res, 4), registers.a());
      case 0xa8 -> create(partial(Alu::res, 5), registers.b());
      case 0xa9 -> create(partial(Alu::res, 5), registers.c());
      case 0xaa -> create(partial(Alu::res, 5), registers.d());
      case 0xab -> create(partial(Alu::res, 5), registers.e());
      case 0xac -> create(partial(Alu::res, 5), registers.h());
      case 0xad -> create(partial(Alu::res, 5), registers.l());
      case 0xae -> create(partial(Alu::res, 5), new ReadModifyWriteIndirect());
      case 0xaf -> create(partial(Alu::res, 5), registers.a());
      case 0xb0 -> create(partial(Alu::res, 6), registers.b());
      case 0xb1 -> create(partial(Alu::res, 6), registers.c());
      case 0xb2 -> create(partial(Alu::res, 6), registers.d());
      case 0xb3 -> create(partial(Alu::res, 6), registers.e());
      case 0xb4 -> create(partial(Alu::res, 6), registers.h());
      case 0xb5 -> create(partial(Alu::res, 6), registers.l());
      case 0xb6 -> create(partial(Alu::res, 6), new ReadModifyWriteIndirect());
      case 0xb7 -> create(partial(Alu::res, 6), registers.a());
      case 0xb8 -> create(partial(Alu::res, 7), registers.b());
      case 0xb9 -> create(partial(Alu::res, 7), registers.c());
      case 0xba -> create(partial(Alu::res, 7), registers.d());
      case 0xbb -> create(partial(Alu::res, 7), registers.e());
      case 0xbc -> create(partial(Alu::res, 7), registers.h());
      case 0xbd -> create(partial(Alu::res, 7), registers.l());
      case 0xbe -> create(partial(Alu::res, 7), new ReadModifyWriteIndirect());
      case 0xbf -> create(partial(Alu::res, 7), registers.a());
      case 0xc0 -> create(partial(Alu::set, 0), registers.b());
      case 0xc1 -> create(partial(Alu::set, 0), registers.c());
      case 0xc2 -> create(partial(Alu::set, 0), registers.d());
      case 0xc3 -> create(partial(Alu::set, 0), registers.e());
      case 0xc4 -> create(partial(Alu::set, 0), registers.h());
      case 0xc5 -> create(partial(Alu::set, 0), registers.l());
      case 0xc6 -> create(partial(Alu::set, 0), new ReadModifyWriteIndirect());
      case 0xc7 -> create(partial(Alu::set, 0), registers.a());
      case 0xc8 -> create(partial(Alu::set, 1), registers.b());
      case 0xc9 -> create(partial(Alu::set, 1), registers.c());
      case 0xca -> create(partial(Alu::set, 1), registers.d());
      case 0xcb -> create(partial(Alu::set, 1), registers.e());
      case 0xcc -> create(partial(Alu::set, 1), registers.h());
      case 0xcd -> create(partial(Alu::set, 1), registers.l());
      case 0xce -> create(partial(Alu::set, 1), new ReadModifyWriteIndirect());
      case 0xcf -> create(partial(Alu::set, 1), registers.a());
      case 0xd0 -> create(partial(Alu::set, 2), registers.b());
      case 0xd1 -> create(partial(Alu::set, 2), registers.c());
      case 0xd2 -> create(partial(Alu::set, 2), registers.d());
      case 0xd3 -> create(partial(Alu::set, 2), registers.e());
      case 0xd4 -> create(partial(Alu::set, 2), registers.h());
      case 0xd5 -> create(partial(Alu::set, 2), registers.l());
      case 0xd6 -> create(partial(Alu::set, 2), new ReadModifyWriteIndirect());
      case 0xd7 -> create(partial(Alu::set, 2), registers.a());
      case 0xd8 -> create(partial(Alu::set, 3), registers.b());
      case 0xd9 -> create(partial(Alu::set, 3), registers.c());
      case 0xda -> create(partial(Alu::set, 3), registers.d());
      case 0xdb -> create(partial(Alu::set, 3), registers.e());
      case 0xdc -> create(partial(Alu::set, 3), registers.h());
      case 0xdd -> create(partial(Alu::set, 3), registers.l());
      case 0xde -> create(partial(Alu::set, 3), new ReadModifyWriteIndirect());
      case 0xdf -> create(partial(Alu::set, 3), registers.a());
      case 0xe0 -> create(partial(Alu::set, 4), registers.b());
      case 0xe1 -> create(partial(Alu::set, 4), registers.c());
      case 0xe2 -> create(partial(Alu::set, 4), registers.d());
      case 0xe3 -> create(partial(Alu::set, 4), registers.e());
      case 0xe4 -> create(partial(Alu::set, 4), registers.h());
      case 0xe5 -> create(partial(Alu::set, 4), registers.l());
      case 0xe6 -> create(partial(Alu::set, 4), new ReadModifyWriteIndirect());
      case 0xe7 -> create(partial(Alu::set, 4), registers.a());
      case 0xe8 -> create(partial(Alu::set, 5), registers.b());
      case 0xe9 -> create(partial(Alu::set, 5), registers.c());
      case 0xea -> create(partial(Alu::set, 5), registers.d());
      case 0xeb -> create(partial(Alu::set, 5), registers.e());
      case 0xec -> create(partial(Alu::set, 5), registers.h());
      case 0xed -> create(partial(Alu::set, 5), registers.l());
      case 0xee -> create(partial(Alu::set, 5), new ReadModifyWriteIndirect());
      case 0xef -> create(partial(Alu::set, 5), registers.a());
      case 0xf0 -> create(partial(Alu::set, 6), registers.b());
      case 0xf1 -> create(partial(Alu::set, 6), registers.c());
      case 0xf2 -> create(partial(Alu::set, 6), registers.d());
      case 0xf3 -> create(partial(Alu::set, 6), registers.e());
      case 0xf4 -> create(partial(Alu::set, 6), registers.h());
      case 0xf5 -> create(partial(Alu::set, 6), registers.l());
      case 0xf6 -> create(partial(Alu::set, 6), new ReadModifyWriteIndirect());
      case 0xf7 -> create(partial(Alu::set, 6), registers.a());
      case 0xf8 -> create(partial(Alu::set, 7), registers.b());
      case 0xf9 -> create(partial(Alu::set, 7), registers.c());
      case 0xfa -> create(partial(Alu::set, 7), registers.d());
      case 0xfb -> create(partial(Alu::set, 7), registers.e());
      case 0xfc -> create(partial(Alu::set, 7), registers.h());
      case 0xfd -> create(partial(Alu::set, 7), registers.l());
      case 0xfe -> create(partial(Alu::set, 7), new ReadModifyWriteIndirect());
      case 0xff -> create(partial(Alu::set, 7), registers.a());
      default -> throw new InvalidOpcodeError((byte) 0xcb, registers.ir().get());
    };
  }

  private static <T1, T2> AluFn1<T1> partial(AluFn2<T1, T2> op, T2 given) {
    return (t1, flags) -> op.execute(t1, given, flags);
  }

  private static Instruction create(AluFn0 op) {
    return (registers, _) -> {
      op.execute(registers.f());
      return null;
    };
  }

  private static Instruction create(AluFn1<A> op) {
    return (registers, _) -> {
      op.execute(registers.a(), registers.f());
      return null;
    };
  }

  private static Instruction create(AluFn1<R> op, R r) {
    return (registers, _) -> {
      op.execute(r, registers.f());
      return null;
    };
  }

  private static Instruction create(AluFn1<R> op, ReadIndirect indirect) {
    assert indirect != null;
    return new Instruction() {
      @Override
      public MCycle start(Registers registers, Bus bus) {
        return new M2();
      }

      private final class M2 implements MCycle {

        @Override
        public void t1(Registers registers, Bus bus) {
          bus.addressBus().set(registers.hl().get());
        }

        @Override
        public void t2(Registers registers, Bus bus) {
          bus.rd().set(true);
        }

        @Override
        public void t3(Registers registers, Bus bus) {
          registers.z().set(bus.dataBus().get());
        }

        @Override
        public MCycle t4(Registers registers, Bus bus) {
          bus.rd().set(false);
          op.execute(registers.z(), registers.f());
          return null;
        }
      }
    };
  }

  private static Instruction create(AluFn1<R> op, ReadModifyWriteIndirect indirect) {
    assert indirect != null;
    return new Instruction() {
      @Override
      public MCycle start(Registers registers, Bus bus) {
        return new M2();
      }

      private final class M2 implements MCycle {

        @Override
        public void t1(Registers registers, Bus bus) {
          bus.addressBus().set(registers.hl().get());
        }

        @Override
        public void t2(Registers registers, Bus bus) {
          bus.rd().set(true);
        }

        @Override
        public void t3(Registers registers, Bus bus) {
          registers.z().set(bus.dataBus().get());
        }

        @Override
        public MCycle t4(Registers registers, Bus bus) {
          bus.rd().set(false);
          op.execute(registers.z(), registers.f());
          return new M3();
        }
      }

      private static final class M3 implements MCycle {

        @Override
        public void t1(Registers registers, Bus bus) {
          bus.addressBus().set(registers.hl().get());
        }

        @Override
        public void t2(Registers registers, Bus bus) {
          bus.dataBus().set(registers.z().get());
          bus.wr().set(true);
        }

        @Override
        public void t3(Registers registers, Bus bus) {}

        @Override
        public MCycle t4(Registers registers, Bus bus) {
          bus.wr().set(false);
          return null;
        }
      }
    };
  }

  private static Instruction create(AluFn2<A, R> op, R r) {
    return (registers, _) -> {
      op.execute(registers.a(), r, registers.f());
      return null;
    };
  }

  private static Instruction create(AluFn2<HL, RR> op, RR rr) {
    return new Instruction() {
      @Override
      public MCycle start(Registers registers, Bus bus) {
        return new M2();
      }

      private final class M2 implements MCycle {

        @Override
        public void t1(Registers registers, Bus bus) {}

        @Override
        public void t2(Registers registers, Bus bus) {}

        @Override
        public void t3(Registers registers, Bus bus) {}

        @Override
        public MCycle t4(Registers registers, Bus bus) {
          op.execute(registers.hl(), rr, registers.f());
          return null;
        }
      }
    };
  }

  private static Instruction create(AluFn2<A, R> op, ReadIndirect indirect) {
    assert indirect != null;
    return new Instruction() {
      @Override
      public MCycle start(Registers registers, Bus bus) {
        return new M2();
      }

      private final class M2 implements MCycle {

        @Override
        public void t1(Registers registers, Bus bus) {
          bus.addressBus().set(registers.hl().get());
        }

        @Override
        public void t2(Registers registers, Bus bus) {
          bus.rd().set(true);
        }

        @Override
        public void t3(Registers registers, Bus bus) {
          registers.z().set(bus.dataBus().get());
        }

        @Override
        public MCycle t4(Registers registers, Bus bus) {
          bus.rd().set(false);
          op.execute(registers.a(), registers.z(), registers.f());
          return null;
        }
      }
    };
  }

  private static Instruction create(AluFn2<A, R> op, N8 n8) {
    assert n8 != null;
    return new Instruction() {
      @Override
      public MCycle start(Registers registers, Bus bus) {
        return new M2();
      }

      private final class M2 implements MCycle {

        @Override
        public void t1(Registers registers, Bus bus) {
          bus.addressBus().set(registers.pc().get());
        }

        @Override
        public void t2(Registers registers, Bus bus) {
          bus.rd().set(true);
          registers.pc().increment();
        }

        @Override
        public void t3(Registers registers, Bus bus) {
          registers.z().set(bus.dataBus().get());
        }

        @Override
        public MCycle t4(Registers registers, Bus bus) {
          bus.rd().set(false);
          op.execute(registers.a(), registers.z(), registers.f());
          return null;
        }
      }
    };
  }

  private static Instruction create(AluFn2<Sp, WZ> op, E8 e8) {
    assert e8 != null;
    return new Instruction() {
      @Override
      public MCycle start(Registers registers, Bus bus) {
        return new M2();
      }

      private final class M2 implements MCycle {

        @Override
        public void t1(Registers registers, Bus bus) {
          bus.addressBus().set(registers.pc().get());
        }

        @Override
        public void t2(Registers registers, Bus bus) {
          bus.rd().set(true);
          registers.pc().increment();
        }

        @Override
        public void t3(Registers registers, Bus bus) {
          registers.z().set(bus.dataBus().get());
        }

        @Override
        public MCycle t4(Registers registers, Bus bus) {
          bus.rd().set(false);
          return new M3();
        }
      }

      private final class M3 implements MCycle {

        @Override
        public void t1(Registers registers, Bus bus) {}

        @Override
        public void t2(Registers registers, Bus bus) {}

        @Override
        public void t3(Registers registers, Bus bus) {}

        @Override
        public MCycle t4(Registers registers, Bus bus) {
          op.execute(registers.sp(), registers.wz(), registers.f());
          return new M4();
        }
      }

      private static final class M4 implements MCycle {

        @Override
        public void t1(Registers registers, Bus bus) {}

        @Override
        public void t2(Registers registers, Bus bus) {
          registers.sp().set(registers.wz().get());
        }

        @Override
        public void t3(Registers registers, Bus bus) {}

        @Override
        public MCycle t4(Registers registers, Bus bus) {
          return null;
        }
      }
    };
  }

  private interface AluFn0 {

    void execute(F flags);
  }

  private interface AluFn1<T> {

    void execute(T t1, F flags);
  }

  private interface AluFn2<T1, T2> {

    void execute(T1 t1, T2 t2, F flags);
  }

  private static final class ReadIndirect {}

  private static final class ReadModifyWriteIndirect {}

  private static final class N8 {}

  private static final class E8 {}
}

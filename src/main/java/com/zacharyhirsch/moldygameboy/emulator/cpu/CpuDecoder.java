package com.zacharyhirsch.moldygameboy.emulator.cpu;

import com.zacharyhirsch.moldygameboy.emulator.arch.UInt8;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.FlagsRegister;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Register16;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Register8;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import com.zacharyhirsch.moldygameboy.emulator.memory.MemOperation;
import com.zacharyhirsch.moldygameboy.emulator.memory.MemRead;
import com.zacharyhirsch.moldygameboy.emulator.memory.MemWrite;
import java.util.concurrent.Exchanger;
import java.util.function.Predicate;

final class CpuDecoder {

  private final Exchanger<MemOperation> memSema;
  private final Exchanger<Byte> cpuSema;
  private final Registers registers;

  private boolean ime = false;

  CpuDecoder(Exchanger<MemOperation> memSema, Exchanger<Byte> cpuSema, Registers registers) {
    this.memSema = memSema;
    this.cpuSema = cpuSema;
    this.registers = registers;
  }

  private byte read(short address) {
    try {
      memSema.exchange(new MemRead(address));
      return cpuSema.exchange(null);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private void write(short address, byte data) {
    try {
      memSema.exchange(new MemWrite(address, data));
      cpuSema.exchange(null);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private void adc_r8_indirect_r8(R8 arg0, R8 arg1) {
    throw new InvalidOpcodeError(registers.ir().get());
  }

  private void adc() {
    byte z = read(registers.pc().getAndIncrement());
    byte oldValue = registers.a().get();
    int c = registers.f().getC() ? 1 : 0;
    byte newValue = (byte) (oldValue + z + c);
    registers.a().set(newValue);
    registers.f().setZ(newValue == 0);
    registers.f().setN(false);
    registers.f().setH((oldValue & 0x0f) + (z & 0x0f) + c > 0x0f);
    registers.f().setC(Byte.toUnsignedInt(oldValue) + Byte.toUnsignedInt(z) + c > 0xff);
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private void adc_r8_r8(R8 arg0, R8 arg1) {
    throw new InvalidOpcodeError(registers.ir().get());
  }

  private void add_r16_e8(R16 arg0) {
    throw new InvalidOpcodeError(registers.ir().get());
  }

  private void add(Register16<? extends UInt8, ? extends UInt8> register) {
    {
      byte lhs = registers.hl().lo().get();
      byte rhs = register.lo().get();
      registers.hl().lo().set((byte) (lhs + rhs));
      registers.f().setN(false);
      registers.f().setH((lhs & 0x0f) + (rhs & 0x0f) > 0x0f);
      registers.f().setC(Byte.toUnsignedInt(lhs) + Byte.toUnsignedInt(rhs) > 0xff);
    }
    {
      byte lhs = registers.hl().hi().get();
      byte rhs = register.hi().get();
      int c = registers.f().getC() ? 1 : 0;
      registers.hl().hi().set((byte) (lhs + rhs + c));
      registers.f().setN(false);
      registers.f().setH((lhs & 0x0f) + (rhs & 0x0f) + c > 0x0f);
      registers.f().setC(Byte.toUnsignedInt(lhs) + Byte.toUnsignedInt(rhs) + c > 0xff);
    }
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private void add_r8_indirect_r8(R8 arg0, R8 arg1) {
    throw new InvalidOpcodeError(registers.ir().get());
  }

  private void add_r8_n8(R8 arg0) {
    byte z = read(registers.pc().getAndIncrement());
    byte oldValue = registers.a().get();
    byte newValue = (byte) (oldValue + z);
    registers.a().set(newValue);
    registers.f().setZ(newValue == 0);
    registers.f().setN(false);
    registers.f().setH((oldValue & 0x0f) + (z & 0x0f) > 0x0f);
    registers.f().setC(Byte.toUnsignedInt(oldValue) + Byte.toUnsignedInt(z) > 0xff);
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private void add_r8_r8(R8 arg0, R8 arg1) {
    throw new InvalidOpcodeError(registers.ir().get());
  }

  private void and_r8_indirect_r8(R8 arg0, R8 arg1) {
    throw new InvalidOpcodeError(registers.ir().get());
  }

  private void and_r8_n8() {
    byte z = read(registers.pc().getAndIncrement());
    byte result = (byte) (registers.a().get() & z);
    registers.a().set(result);
    registers.f().setZ(result == 0);
    registers.f().setN(false);
    registers.f().setH(true);
    registers.f().setC(false);
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private void and_r8_r8(R8 arg0, R8 arg1) {
    throw new InvalidOpcodeError(registers.ir().get());
  }

  private void call() {
    byte z = read(registers.pc().getAndIncrement());
    byte w = read(registers.pc().getAndIncrement());

    read(registers.sp().getAndDecrement());
    write(registers.sp().getAndDecrement(), registers.pc().hi().get());
    write(registers.sp().get(), registers.pc().lo().get());

    registers.pc().set(w, z);
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private void call(Predicate<FlagsRegister> predicate) {
    byte z = read(registers.pc().getAndIncrement());
    byte w = read(registers.pc().getAndIncrement());
    if (!predicate.test(registers.f())) {
      registers.ir().set(read(registers.pc().getAndIncrement()));
      return;
    }

    read(registers.sp().getAndDecrement());
    write(registers.sp().getAndDecrement(), registers.pc().hi().get());
    write(registers.sp().get(), registers.pc().lo().get());

    registers.pc().set(w, z);
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private void ccf() {
    throw new InvalidOpcodeError(registers.ir().get());
  }

  private void cp_indirect() {
    byte z = read(registers.hl().get());
    byte result = (byte) (registers.a().get() - z);
    registers.f().setZ(result == 0);
    registers.f().setN(true);
    registers.f().setH((registers.a().get() & 0x0f) - (z & 0x0f) < 0);
    registers.f().setC((registers.a().get() & 0xff) - (z & 0xff) < 0);
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private void cp() {
    byte z = read(registers.pc().getAndIncrement());
    byte result = (byte) (registers.a().get() - z);
    registers.f().setZ(result == 0);
    registers.f().setN(true);
    registers.f().setH((registers.a().get() & 0x0f) - (z & 0x0f) < 0);
    registers.f().setC((registers.a().get() & 0xff) - (z & 0xff) < 0);
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private void cp(Register8 register) {
    byte result = (byte) (registers.a().get() - register.get());
    registers.f().setZ(result == 0);
    registers.f().setN(true);
    registers.f().setH((registers.a().get() & 0x0f) - (register.get() & 0x0f) < 0);
    registers.f().setC((registers.a().get() & 0xff) - (register.get() & 0xff) < 0);
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private void cpl() {
    registers.a().set((byte) ~registers.a().get());
    registers.f().setN(true);
    registers.f().setH(true);
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private void daa() {
    int a = Byte.toUnsignedInt(registers.a().get());
    if (registers.f().getN()) {
      // after subtraction
      if (registers.f().getC()) {
        a -= 0x60;
      }
      if (registers.f().getH()) {
        a -= 0x06;
      }
    } else {
      // after addition
      if (registers.f().getC() || a > 0x99) {
        a += 0x60;
        registers.f().setC(true);
      }
      if (registers.f().getH() || (a & 0x0f) > 0x09) {
        a += 0x06;
      }
    }
    registers.a().set((byte) a);
    registers.f().setZ(((byte)a) == 0);
    registers.f().setH(false);
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private void dec() {
    byte oldValue = read(registers.hl().get());
    byte newValue = (byte) (oldValue - 1);
    write(registers.hl().get(), newValue);
    registers.f().setZ(newValue == 0);
    registers.f().setN(true);
    registers.f().setH((oldValue & 0x0f) == 0);
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private void dec_r16(R16 arg0) {
    throw new InvalidOpcodeError(registers.ir().get());
  }

  private void dec(Register8 register) {
    byte oldValue = register.get();
    byte newValue = (byte) (oldValue - 1);
    register.set(newValue);
    registers.f().setZ(newValue == 0);
    registers.f().setN(true);
    registers.f().setH((oldValue & 0x0f) == 0);
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private void di() {
    ime = false;
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private void ei() {
    //    ime = true;
    //    return fetch();
    throw new InvalidOpcodeError(registers.ir().get());
  }

  private void halt() {
    throw new InvalidOpcodeError(registers.ir().get());
  }

  private void illegal_d3() {
    throw new InvalidOpcodeError(registers.ir().get());
  }

  private void illegal_db() {
    throw new InvalidOpcodeError(registers.ir().get());
  }

  private void illegal_dd() {
    throw new InvalidOpcodeError(registers.ir().get());
  }

  private void illegal_e3() {
    throw new InvalidOpcodeError(registers.ir().get());
  }

  private void illegal_e4() {
    throw new InvalidOpcodeError(registers.ir().get());
  }

  private void illegal_eb() {
    throw new InvalidOpcodeError(registers.ir().get());
  }

  private void illegal_ec() {
    throw new InvalidOpcodeError(registers.ir().get());
  }

  private void illegal_ed() {
    throw new InvalidOpcodeError(registers.ir().get());
  }

  private void illegal_f4() {
    throw new InvalidOpcodeError(registers.ir().get());
  }

  private void illegal_fc() {
    throw new InvalidOpcodeError(registers.ir().get());
  }

  private void illegal_fd() {
    throw new InvalidOpcodeError(registers.ir().get());
  }

  private void inc_indirect_r8(R8 arg0) {
    throw new InvalidOpcodeError(registers.ir().get());
  }

  private void inc(Register16<? extends UInt8, ? extends UInt8> register) {
    register.set((short) (register.get() + 1));
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private void inc(Register8 register) {
    byte oldValue = register.get();
    byte newValue = (byte) (oldValue + 1);
    register.set(newValue);
    registers.f().setZ(newValue == 0);
    registers.f().setN(false);
    registers.f().setH((oldValue & 0x0f) == 0x0f);
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private void jp() {
    byte z = read(registers.pc().getAndIncrement());
    byte w = read(registers.pc().getAndIncrement());
    read((short) 0x0000);
    registers.pc().set(w, z);
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private void jp(Predicate<FlagsRegister> predicate) {
    byte z = read(registers.pc().getAndIncrement());
    byte w = read(registers.pc().getAndIncrement());
    if (!predicate.test(registers.f())) {
      registers.ir().set(read(registers.pc().getAndIncrement()));
      return;
    }
    read((short) 0x0000);
    registers.pc().set(w, z);
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private void jp_indirect() {
    registers.pc().set(registers.hl().get());
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private void jr(Predicate<FlagsRegister> predicate) {
    byte z = read(registers.pc().getAndIncrement());
    if (!predicate.test(registers.f())) {
      registers.ir().set(read(registers.pc().getAndIncrement()));
      return;
    }
    short wz = (short) (Short.toUnsignedInt(registers.pc().get()) + z);
    read(registers.pc().getAndIncrement());
    registers.pc().set(wz);
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private void jr() {
    byte z = read(registers.pc().getAndIncrement());
    short wz = (short) (Short.toUnsignedInt(registers.pc().get()) + z);
    read(registers.pc().getAndIncrement());
    registers.pc().set(wz);
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private void ld_indirect_a16_r16(R16 arg0) {
    throw new InvalidOpcodeError(registers.ir().get());
  }

  private void ld_indirect_a16_r8(R8 arg0) {
    byte z = read(registers.pc().getAndIncrement());
    byte w = read(registers.pc().getAndIncrement());
    write((short) ((Byte.toUnsignedInt(w) << 8) | Byte.toUnsignedInt(z)), registers.a().get());
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private void ld_indirect_decrement_hl_a() {
    write(registers.hl().getAndDecrement(), registers.a().get());
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private void ld_indirect_increment_hl_a() {
    write(registers.hl().getAndIncrement(), registers.a().get());
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private void ld_indirect_r16_a(Register16<? extends UInt8, ? extends UInt8> register) {
    write(register.get(), registers.a().get());
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private void ld_indirect_hl_imm8() {
    byte z = read(registers.pc().getAndIncrement());
    write(registers.hl().get(), z);
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private void ld_indirect_r8_r8(Register8 register) {
    write(registers.hl().get(), register.get());
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private void ld_r16_increment_r16_e8(R16 arg0, R16 arg1) {
    throw new InvalidOpcodeError(registers.ir().get());
  }

  private void ld_imm16(Register16<? extends UInt8, ? extends UInt8> register) {
    byte z = read(registers.pc().getAndIncrement());
    byte w = read(registers.pc().getAndIncrement());
    register.set(w, z);
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private void ld_r16_r16(R16 arg0, R16 arg1) {
    throw new InvalidOpcodeError(registers.ir().get());
  }

  private void ld_r8_indirect_a16() {
    byte z = read(registers.pc().getAndIncrement());
    byte w = read(registers.pc().getAndIncrement());
    registers.a().set(read((short) ((Byte.toUnsignedInt(w) << 8) | Byte.toUnsignedInt(z))));
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private void ld_a_indirect_decrement_hl() {
    registers.a().set(read(registers.hl().getAndDecrement()));
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private void ld_a_indirect_increment_hl() {
    registers.a().set(read(registers.hl().getAndIncrement()));
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private void ld_a_indirect(Register16<? extends UInt8, ? extends UInt8> register) {
    registers.a().set(read(register.get()));
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private void ld_r8_indirect_hl(Register8 register) {
    register.set(read(registers.hl().get()));
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private void ld_imm8(Register8 register) {
    register.set(read(registers.pc().getAndIncrement()));
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private void ldh_indirect_imm8() {
    byte z = read(registers.pc().getAndIncrement());
    write((short) (0xff00 | Byte.toUnsignedInt(z)), registers.a().get());
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private void ldh_indirect() {
    write((short) (0xff00 | Byte.toUnsignedInt(registers.c().get())), registers.a().get());
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private void ldh_a_indirect_imm8() {
    byte z = read(registers.pc().getAndIncrement());
    Register8 dst = registers.a();
    dst.set(read((short) (0xff00 | Byte.toUnsignedInt(z))));
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private void ldh_a_indirect_c() {
    Register8 dst = registers.a();
    dst.set(read((short) (0xff00 | Byte.toUnsignedInt(registers.c().get()))));
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private void ld_r8(Register8 dst, Register8 src) {
    dst.set(src.get());
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private void nop() {
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private void or_a_indirect_hl() {
    byte z = read(registers.hl().get());
    byte result = (byte) (registers.a().get() | z);
    registers.a().set(result);
    registers.f().setZ(result == 0);
    registers.f().setN(false);
    registers.f().setH(false);
    registers.f().setC(false);
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private void or_r8_n8(R8 arg0) {
    throw new InvalidOpcodeError(registers.ir().get());
  }

  private void or(Register8 register) {
    byte result = (byte) (registers.a().get() | register.get());
    registers.a().set(result);
    registers.f().setZ(result == 0);
    registers.f().setN(false);
    registers.f().setH(false);
    registers.f().setC(false);
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private void pop(Register16<? extends UInt8, ? extends UInt8> register) {
    byte z = read(registers.sp().getAndIncrement());
    byte w = read(registers.sp().getAndIncrement());
    register.set(w, z);
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private void prefix() {
    registers.ir().set(read(registers.pc().getAndIncrement()));
    decode2();
  }

  private void push(Register16<? extends UInt8, ? extends UInt8> register) {
    read(registers.sp().getAndDecrement());
    write(registers.sp().getAndDecrement(), register.hi().get());
    write(registers.sp().get(), register.lo().get());
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private void ret() {
    byte z = read(registers.sp().getAndIncrement());
    byte w = read(registers.sp().getAndIncrement());
    read((short) 0x0000);
    registers.pc().set(w, z);
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private void ret(Predicate<FlagsRegister> predicate) {
    if (!predicate.test(registers.f())) {
      registers.ir().set(read(registers.pc().getAndIncrement()));
      return;
    }

    byte z = read(registers.sp().getAndIncrement());
    byte w = read(registers.sp().getAndIncrement());
    read((short) 0x0000);
    registers.pc().set(w, z);
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private void reti() {
    throw new InvalidOpcodeError(registers.ir().get());
  }

  private void rla() {
    byte oldValue = registers.a().get();
    byte newValue = (byte) ((oldValue << 1) | (registers.f().getC() ? 1 : 0));
    registers.a().set(newValue);
    registers.f().setZ(false);
    registers.f().setN(false);
    registers.f().setH(false);
    registers.f().setC(bit8(oldValue, 7) == 1);
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private void rlca() {
    throw new InvalidOpcodeError(registers.ir().get());
  }

  private void rra() {
    byte oldValue = registers.a().get();
    byte c = registers.f().getC() ? (byte) 0b1000_0000 : 0;
    byte newValue = (byte) ((Byte.toUnsignedInt(oldValue) >>> 1) | c);
    registers.a().set(newValue);
    registers.f().setZ(false);
    registers.f().setN(false);
    registers.f().setH(false);
    registers.f().setC(bit8(oldValue, 0) == 1);
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private void rrca() {
    throw new InvalidOpcodeError(registers.ir().get());
  }

  private void rst_tgt3(int arg0) {
    throw new InvalidOpcodeError(registers.ir().get());
  }

  private void sbc_r8_indirect_r8(R8 arg0, R8 arg1) {
    throw new InvalidOpcodeError(registers.ir().get());
  }

  private void sbc_r8_n8(R8 arg0) {
    throw new InvalidOpcodeError(registers.ir().get());
  }

  private void sbc_r8_r8(R8 arg0, R8 arg1) {
    throw new InvalidOpcodeError(registers.ir().get());
  }

  private void scf() {
    throw new InvalidOpcodeError(registers.ir().get());
  }

  private void stop_n8() {
    throw new InvalidOpcodeError(registers.ir().get());
  }

  private void sub_r8_indirect_r8(R8 arg0, R8 arg1) {
    throw new InvalidOpcodeError(registers.ir().get());
  }

  private void sub_r8_n8() {
    byte z = read(registers.pc().getAndIncrement());
    byte oldValue = registers.a().get();
    byte newValue = (byte) (oldValue - z);
    registers.a().set(newValue);
    registers.f().setZ(newValue == 0);
    registers.f().setN(true);
    registers.f().setH((oldValue & 0x0f) - (z & 0x0f) < 0);
    registers.f().setC(Byte.toUnsignedInt(oldValue) - Byte.toUnsignedInt(z) < 0);
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private void sub_r8_r8(R8 arg0, R8 arg1) {
    throw new InvalidOpcodeError(registers.ir().get());
  }

  private void xor_r8_indirect_r8() {
    byte z = read(registers.hl().get());
    byte result = (byte) (registers.a().get() ^ z);
    registers.a().set(result);
    registers.f().setZ(result == 0);
    registers.f().setN(false);
    registers.f().setH(false);
    registers.f().setC(false);
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private void xor() {
    byte z = read(registers.pc().getAndIncrement());
    byte result = (byte) (registers.a().get() ^ z);
    registers.a().set(result);
    registers.f().setZ(result == 0);
    registers.f().setN(false);
    registers.f().setH(false);
    registers.f().setC(false);
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private void xor(Register8 register) {
    byte result = (byte) (registers.a().get() ^ register.get());
    registers.a().set(result);
    registers.f().setZ(result == 0);
    registers.f().setN(false);
    registers.f().setH(false);
    registers.f().setC(false);
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private void bit_b3_indirect_r8(int arg0, R8 arg1) {
    throw new InvalidOpcodeError(registers.ir().get());
  }

  private void bit(Register8 register, int bit) {
    registers.f().setZ(bit8(register.get(), bit) == 0);
    registers.f().setN(false);
    registers.f().setH(true);
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private void res_b3_indirect_r8(int arg0, R8 arg1) {
    //    assert arg1 == R8.INDIRECT_HL;
    //    return new Cycle(
    //        new MemRead(registers.hl()),
    //        null,
    //        Z ->
    //            new Cycle(
    //                new MemWrite(registers.hl(), (byte) (Z & ~(1 << arg0))), null, _ -> fetch()));
    throw new InvalidOpcodeError(registers.ir().get());
  }

  private void res_b3_r8(int arg0, R8 arg1) {
    throw new InvalidOpcodeError(registers.ir().get());
  }

  private void rl_indirect_r8(R8 arg0) {
    throw new InvalidOpcodeError(registers.ir().get());
  }

  private void rl(Register8 register) {
    byte oldValue = register.get();
    int c = registers.f().getC() ? 1 : 0;
    byte newValue = (byte) ((oldValue << 1) | c);
    register.set(newValue);
    registers.f().setZ(newValue == 0);
    registers.f().setN(false);
    registers.f().setH(false);
    registers.f().setC(bit8(oldValue, 7) == 1);
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private void rlc_indirect_r8(R8 arg0) {
    throw new InvalidOpcodeError(registers.ir().get());
  }

  private void rlc_r8(R8 arg0) {
    throw new InvalidOpcodeError(registers.ir().get());
  }

  private void rr_indirect_r8(R8 arg0) {
    throw new InvalidOpcodeError(registers.ir().get());
  }

  private void rr(Register8 register) {
    byte oldValue = register.get();
    byte c = (byte) (registers.f().getC() ? 0b1000_0000 : 0);
    byte newValue = (byte) ((Byte.toUnsignedInt(oldValue) >>> 1) | c);
    register.set(newValue);
    registers.f().setZ(newValue == 0);
    registers.f().setN(false);
    registers.f().setH(false);
    registers.f().setC(bit8(oldValue, 0) == 1);
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private void rrc_indirect_r8(R8 arg0) {
    throw new InvalidOpcodeError(registers.ir().get());
  }

  private void rrc_r8(R8 arg0) {
    throw new InvalidOpcodeError(registers.ir().get());
  }

  private void set_b3_indirect_r8(int arg0, R8 arg1) {
    throw new InvalidOpcodeError(registers.ir().get());
  }

  private void set_b3_r8(int arg0, R8 arg1) {
    throw new InvalidOpcodeError(registers.ir().get());
  }

  private void sla_indirect_r8(R8 arg0) {
    throw new InvalidOpcodeError(registers.ir().get());
  }

  private void sla_r8(R8 arg0) {
    throw new InvalidOpcodeError(registers.ir().get());
  }

  private void sra_indirect_r8(R8 arg0) {
    throw new InvalidOpcodeError(registers.ir().get());
  }

  private void sra_r8(R8 arg0) {
    throw new InvalidOpcodeError(registers.ir().get());
  }

  private void srl_indirect_r8(R8 arg0) {
    throw new InvalidOpcodeError(registers.ir().get());
  }

  private void srl(Register8 register) {
    byte oldValue = register.get();
    byte newValue = (byte) (Byte.toUnsignedInt(oldValue) >>> 1);
    register.set(newValue);
    registers.f().setZ(newValue == 0);
    registers.f().setN(false);
    registers.f().setH(false);
    registers.f().setC(bit8(oldValue, 0) == 1);
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private void swap_indirect_r8(R8 arg0) {
    throw new InvalidOpcodeError(registers.ir().get());
  }

  private void swap(Register8 register) {
    byte result = (byte) (((register.get() & 0x0f) << 4) | ((register.get() & 0xf0) >>> 4));
    register.set(result);
    registers.f().setZ(result == 0);
    registers.f().setN(false);
    registers.f().setH(false);
    registers.f().setC(false);
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private Register8 getRegister(R8 arg) {
    return switch (arg) {
      case B -> registers.b();
      case C -> registers.c();
      case D -> registers.d();
      case E -> registers.e();
      case H -> registers.h();
      case L -> registers.l();
      case A -> registers.a();
      default -> throw new InvalidOperandError(registers.ir().get(), arg);
    };
  }

  private Register16 getRegister(R16 arg0) {
    return switch (arg0) {
      case BC -> registers.bc();
      case DE -> registers.de();
      case HL -> registers.hl();
      case SP -> registers.sp();
      default -> throw new InvalidOperandError(registers.ir().get(), arg0);
    };
  }

  private Predicate<FlagsRegister> getPredicate(Cond arg0) {
    return switch (arg0) {
      case Z -> FlagsRegister::getZ;
      case NZ -> Predicate.not(FlagsRegister::getZ);
      case C -> FlagsRegister::getC;
      case NC -> Predicate.not(FlagsRegister::getC);
    };
  }

  private static int bit8(byte value, int bit) {
    return (value >>> bit) & 1;
  }

  void decode() {
    switch (Byte.toUnsignedInt(registers.ir().get())) {
      case 0x00 -> nop();
      case 0x01 -> ld_imm16(registers.bc());
      case 0x02 -> ld_indirect_r16_a(getRegister(R16.BC));
      case 0x03 -> inc(registers.bc());
      case 0x04 -> inc(registers.b());
      case 0x05 -> dec(registers.b());
      case 0x06 -> ld_imm8(registers.b());
      case 0x07 -> rlca();
      case 0x08 -> ld_indirect_a16_r16(R16.SP);
      case 0x09 -> add(registers.bc());
      case 0x0a -> ld_a_indirect(registers.bc());
      case 0x0b -> dec_r16(R16.BC);
      case 0x0c -> inc(registers.c());
      case 0x0d -> dec(registers.c());
      case 0x0e -> ld_imm8(registers.c());
      case 0x0f -> rrca();
      case 0x10 -> stop_n8();
      case 0x11 -> ld_imm16(registers.de());
      case 0x12 -> ld_indirect_r16_a(getRegister(R16.DE));
      case 0x13 -> inc(registers.de());
      case 0x14 -> inc(registers.d());
      case 0x15 -> dec(registers.d());
      case 0x16 -> ld_imm8(registers.d());
      case 0x17 -> rla();
      case 0x18 -> jr();
      case 0x19 -> add(registers.de());
      case 0x1a -> ld_a_indirect(registers.de());
      case 0x1b -> dec_r16(R16.DE);
      case 0x1c -> inc(registers.e());
      case 0x1d -> dec(registers.e());
      case 0x1e -> ld_imm8(registers.e());
      case 0x1f -> rra();
      case 0x20 -> jr(Predicate.not(FlagsRegister::getZ));
      case 0x21 -> ld_imm16(registers.hl());
      case 0x22 -> ld_indirect_increment_hl_a();
      case 0x23 -> inc(registers.hl());
      case 0x24 -> inc(registers.h());
      case 0x25 -> dec(registers.h());
      case 0x26 -> ld_imm8(registers.h());
      case 0x27 -> daa();
      case 0x28 -> jr(FlagsRegister::getZ);
      case 0x29 -> add(registers.hl());
      case 0x2a -> ld_a_indirect_increment_hl();
      case 0x2b -> dec_r16(R16.HL);
      case 0x2c -> inc(registers.l());
      case 0x2d -> dec(registers.l());
      case 0x2e -> ld_imm8(registers.l());
      case 0x2f -> cpl();
      case 0x30 -> jr(Predicate.not(FlagsRegister::getC));
      case 0x31 -> ld_imm16(registers.sp());
      case 0x32 -> ld_indirect_decrement_hl_a();
      case 0x33 -> inc(registers.sp());
      case 0x34 -> inc_indirect_r8(R8.INDIRECT_HL);
      case 0x35 -> dec();
      case 0x36 -> ld_indirect_hl_imm8();
      case 0x37 -> scf();
      case 0x38 -> jr(FlagsRegister::getC);
      case 0x39 -> add(registers.sp());
      case 0x3a -> ld_a_indirect_decrement_hl();
      case 0x3b -> dec_r16(R16.SP);
      case 0x3c -> inc(registers.a());
      case 0x3d -> dec(registers.a());
      case 0x3e -> ld_imm8(registers.a());
      case 0x3f -> ccf();
      case 0x40 -> ld_r8(registers.b(), registers.b());
      case 0x41 -> ld_r8(registers.b(), registers.c());
      case 0x42 -> ld_r8(registers.b(), registers.d());
      case 0x43 -> ld_r8(registers.b(), registers.e());
      case 0x44 -> ld_r8(registers.b(), registers.h());
      case 0x45 -> ld_r8(registers.b(), registers.l());
      case 0x46 -> ld_r8_indirect_hl(registers.b());
      case 0x47 -> ld_r8(registers.b(), registers.a());
      case 0x48 -> ld_r8(registers.c(), registers.b());
      case 0x49 -> ld_r8(registers.c(), registers.c());
      case 0x4a -> ld_r8(registers.c(), registers.d());
      case 0x4b -> ld_r8(registers.c(), registers.e());
      case 0x4c -> ld_r8(registers.c(), registers.h());
      case 0x4d -> ld_r8(registers.c(), registers.l());
      case 0x4e -> ld_r8_indirect_hl(registers.c());
      case 0x4f -> ld_r8(registers.c(), registers.a());
      case 0x50 -> ld_r8(registers.d(), registers.b());
      case 0x51 -> ld_r8(registers.d(), registers.c());
      case 0x52 -> ld_r8(registers.d(), registers.d());
      case 0x53 -> ld_r8(registers.d(), registers.e());
      case 0x54 -> ld_r8(registers.d(), registers.h());
      case 0x55 -> ld_r8(registers.d(), registers.l());
      case 0x56 -> ld_r8_indirect_hl(registers.d());
      case 0x57 -> ld_r8(registers.d(), registers.a());
      case 0x58 -> ld_r8(registers.e(), registers.b());
      case 0x59 -> ld_r8(registers.e(), registers.c());
      case 0x5a -> ld_r8(registers.e(), registers.d());
      case 0x5b -> ld_r8(registers.e(), registers.e());
      case 0x5c -> ld_r8(registers.e(), registers.h());
      case 0x5d -> ld_r8(registers.e(), registers.l());
      case 0x5e -> ld_r8_indirect_hl(registers.e());
      case 0x5f -> ld_r8(registers.e(), registers.a());
      case 0x60 -> ld_r8(registers.h(), registers.b());
      case 0x61 -> ld_r8(registers.h(), registers.c());
      case 0x62 -> ld_r8(registers.h(), registers.d());
      case 0x63 -> ld_r8(registers.h(), registers.e());
      case 0x64 -> ld_r8(registers.h(), registers.h());
      case 0x65 -> ld_r8(registers.h(), registers.l());
      case 0x66 -> ld_r8_indirect_hl(registers.h());
      case 0x67 -> ld_r8(registers.h(), registers.a());
      case 0x68 -> ld_r8(registers.l(), registers.b());
      case 0x69 -> ld_r8(registers.l(), registers.c());
      case 0x6a -> ld_r8(registers.l(), registers.d());
      case 0x6b -> ld_r8(registers.l(), registers.e());
      case 0x6c -> ld_r8(registers.l(), registers.h());
      case 0x6d -> ld_r8(registers.l(), registers.l());
      case 0x6e -> ld_r8_indirect_hl(registers.l());
      case 0x6f -> ld_r8(registers.l(), registers.a());
      case 0x70 -> ld_indirect_r8_r8(registers.b());
      case 0x71 -> ld_indirect_r8_r8(registers.c());
      case 0x72 -> ld_indirect_r8_r8(registers.d());
      case 0x73 -> ld_indirect_r8_r8(registers.e());
      case 0x74 -> ld_indirect_r8_r8(registers.h());
      case 0x75 -> ld_indirect_r8_r8(registers.l());
      case 0x76 -> halt();
      case 0x77 -> ld_indirect_r8_r8(registers.a());
      case 0x78 -> ld_r8(registers.a(), registers.b());
      case 0x79 -> ld_r8(registers.a(), registers.c());
      case 0x7a -> ld_r8(registers.a(), registers.d());
      case 0x7b -> ld_r8(registers.a(), registers.e());
      case 0x7c -> ld_r8(registers.a(), registers.h());
      case 0x7d -> ld_r8(registers.a(), registers.l());
      case 0x7e -> ld_r8_indirect_hl(registers.a());
      case 0x7f -> ld_r8(registers.a(), registers.a());
      case 0x80 -> add_r8_r8(R8.A, R8.B);
      case 0x81 -> add_r8_r8(R8.A, R8.C);
      case 0x82 -> add_r8_r8(R8.A, R8.D);
      case 0x83 -> add_r8_r8(R8.A, R8.E);
      case 0x84 -> add_r8_r8(R8.A, R8.H);
      case 0x85 -> add_r8_r8(R8.A, R8.L);
      case 0x86 -> add_r8_indirect_r8(R8.A, R8.INDIRECT_HL);
      case 0x87 -> add_r8_r8(R8.A, R8.A);
      case 0x88 -> adc_r8_r8(R8.A, R8.B);
      case 0x89 -> adc_r8_r8(R8.A, R8.C);
      case 0x8a -> adc_r8_r8(R8.A, R8.D);
      case 0x8b -> adc_r8_r8(R8.A, R8.E);
      case 0x8c -> adc_r8_r8(R8.A, R8.H);
      case 0x8d -> adc_r8_r8(R8.A, R8.L);
      case 0x8e -> adc_r8_indirect_r8(R8.A, R8.INDIRECT_HL);
      case 0x8f -> adc_r8_r8(R8.A, R8.A);
      case 0x90 -> sub_r8_r8(R8.A, R8.B);
      case 0x91 -> sub_r8_r8(R8.A, R8.C);
      case 0x92 -> sub_r8_r8(R8.A, R8.D);
      case 0x93 -> sub_r8_r8(R8.A, R8.E);
      case 0x94 -> sub_r8_r8(R8.A, R8.H);
      case 0x95 -> sub_r8_r8(R8.A, R8.L);
      case 0x96 -> sub_r8_indirect_r8(R8.A, R8.INDIRECT_HL);
      case 0x97 -> sub_r8_r8(R8.A, R8.A);
      case 0x98 -> sbc_r8_r8(R8.A, R8.B);
      case 0x99 -> sbc_r8_r8(R8.A, R8.C);
      case 0x9a -> sbc_r8_r8(R8.A, R8.D);
      case 0x9b -> sbc_r8_r8(R8.A, R8.E);
      case 0x9c -> sbc_r8_r8(R8.A, R8.H);
      case 0x9d -> sbc_r8_r8(R8.A, R8.L);
      case 0x9e -> sbc_r8_indirect_r8(R8.A, R8.INDIRECT_HL);
      case 0x9f -> sbc_r8_r8(R8.A, R8.A);
      case 0xa0 -> and_r8_r8(R8.A, R8.B);
      case 0xa1 -> and_r8_r8(R8.A, R8.C);
      case 0xa2 -> and_r8_r8(R8.A, R8.D);
      case 0xa3 -> and_r8_r8(R8.A, R8.E);
      case 0xa4 -> and_r8_r8(R8.A, R8.H);
      case 0xa5 -> and_r8_r8(R8.A, R8.L);
      case 0xa6 -> and_r8_indirect_r8(R8.A, R8.INDIRECT_HL);
      case 0xa7 -> and_r8_r8(R8.A, R8.A);
      case 0xa8 -> xor(registers.b());
      case 0xa9 -> xor(registers.c());
      case 0xaa -> xor(registers.d());
      case 0xab -> xor(registers.e());
      case 0xac -> xor(registers.h());
      case 0xad -> xor(registers.l());
      case 0xae -> xor_r8_indirect_r8();
      case 0xaf -> xor(registers.a());
      case 0xb0 -> or(registers.b());
      case 0xb1 -> or(registers.c());
      case 0xb2 -> or(registers.d());
      case 0xb3 -> or(registers.e());
      case 0xb4 -> or(registers.h());
      case 0xb5 -> or(registers.l());
      case 0xb6 -> or_a_indirect_hl();
      case 0xb7 -> or(registers.a());
      case 0xb8 -> cp(registers.b());
      case 0xb9 -> cp(registers.c());
      case 0xba -> cp(registers.d());
      case 0xbb -> cp(registers.e());
      case 0xbc -> cp(registers.h());
      case 0xbd -> cp(registers.l());
      case 0xbe -> cp_indirect();
      case 0xbf -> cp(registers.a());
      case 0xc0 -> ret(Predicate.not(FlagsRegister::getZ));
      case 0xc1 -> pop(registers.bc());
      case 0xc2 -> jp(Predicate.not(FlagsRegister::getZ));
      case 0xc3 -> jp();
      case 0xc4 -> call(Predicate.not(FlagsRegister::getZ));
      case 0xc5 -> push(registers.bc());
      case 0xc6 -> add_r8_n8(R8.A);
      case 0xc7 -> rst_tgt3(0x00);
      case 0xc8 -> ret(FlagsRegister::getZ);
      case 0xc9 -> ret();
      case 0xca -> jp(FlagsRegister::getZ);
      case 0xcb -> prefix();
      case 0xcc -> call(FlagsRegister::getZ);
      case 0xcd -> call();
      case 0xce -> adc();
      case 0xcf -> rst_tgt3(0x08);
      case 0xd0 -> ret(Predicate.not(FlagsRegister::getC));
      case 0xd1 -> pop(registers.de());
      case 0xd2 -> jp(Predicate.not(FlagsRegister::getC));
      case 0xd3 -> illegal_d3();
      case 0xd4 -> call(Predicate.not(FlagsRegister::getC));
      case 0xd5 -> push(registers.de());
      case 0xd6 -> sub_r8_n8();
      case 0xd7 -> rst_tgt3(0x10);
      case 0xd8 -> ret(FlagsRegister::getC);
      case 0xd9 -> reti();
      case 0xda -> jp(FlagsRegister::getC);
      case 0xdb -> illegal_db();
      case 0xdc -> call(FlagsRegister::getC);
      case 0xdd -> illegal_dd();
      case 0xde -> sbc_r8_n8(R8.A);
      case 0xdf -> rst_tgt3(0x18);
      case 0xe0 -> ldh_indirect_imm8();
      case 0xe1 -> pop(registers.hl());
      case 0xe2 -> ldh_indirect();
      case 0xe3 -> illegal_e3();
      case 0xe4 -> illegal_e4();
      case 0xe5 -> push(registers.hl());
      case 0xe6 -> and_r8_n8();
      case 0xe7 -> rst_tgt3(0x20);
      case 0xe8 -> add_r16_e8(R16.SP);
      case 0xe9 -> jp_indirect();
      case 0xea -> ld_indirect_a16_r8(R8.A);
      case 0xeb -> illegal_eb();
      case 0xec -> illegal_ec();
      case 0xed -> illegal_ed();
      case 0xee -> xor();
      case 0xef -> rst_tgt3(0x28);
      case 0xf0 -> ldh_a_indirect_imm8();
      case 0xf1 -> pop(registers.af());
      case 0xf2 -> ldh_a_indirect_c();
      case 0xf3 -> di();
      case 0xf4 -> illegal_f4();
      case 0xf5 -> push(registers.af());
      case 0xf6 -> or_r8_n8(R8.A);
      case 0xf7 -> rst_tgt3(0x30);
      case 0xf8 -> ld_r16_increment_r16_e8(R16.HL, R16.SP);
      case 0xf9 -> ld_r16_r16(R16.SP, R16.HL);
      case 0xfa -> ld_r8_indirect_a16();
      case 0xfb -> ei();
      case 0xfc -> illegal_fc();
      case 0xfd -> illegal_fd();
      case 0xfe -> cp();
      case 0xff -> rst_tgt3(0x38);
      default -> throw new IllegalArgumentException();
    }
  }

  private void decode2() {
    switch (Byte.toUnsignedInt(registers.ir().get())) {
      case 0x00 -> rlc_r8(R8.B);
      case 0x01 -> rlc_r8(R8.C);
      case 0x02 -> rlc_r8(R8.D);
      case 0x03 -> rlc_r8(R8.E);
      case 0x04 -> rlc_r8(R8.H);
      case 0x05 -> rlc_r8(R8.L);
      case 0x06 -> rlc_indirect_r8(R8.INDIRECT_HL);
      case 0x07 -> rlc_r8(R8.A);
      case 0x08 -> rrc_r8(R8.B);
      case 0x09 -> rrc_r8(R8.C);
      case 0x0a -> rrc_r8(R8.D);
      case 0x0b -> rrc_r8(R8.E);
      case 0x0c -> rrc_r8(R8.H);
      case 0x0d -> rrc_r8(R8.L);
      case 0x0e -> rrc_indirect_r8(R8.INDIRECT_HL);
      case 0x0f -> rrc_r8(R8.A);
      case 0x10 -> rl(registers.b());
      case 0x11 -> rl(registers.c());
      case 0x12 -> rl(registers.d());
      case 0x13 -> rl(registers.e());
      case 0x14 -> rl(registers.h());
      case 0x15 -> rl(registers.l());
      case 0x16 -> rl_indirect_r8(R8.INDIRECT_HL);
      case 0x17 -> rl(registers.a());
      case 0x18 -> rr(registers.b());
      case 0x19 -> rr(registers.c());
      case 0x1a -> rr(registers.d());
      case 0x1b -> rr(registers.e());
      case 0x1c -> rr(registers.h());
      case 0x1d -> rr(registers.l());
      case 0x1e -> rr_indirect_r8(R8.INDIRECT_HL);
      case 0x1f -> rr(registers.a());
      case 0x20 -> sla_r8(R8.B);
      case 0x21 -> sla_r8(R8.C);
      case 0x22 -> sla_r8(R8.D);
      case 0x23 -> sla_r8(R8.E);
      case 0x24 -> sla_r8(R8.H);
      case 0x25 -> sla_r8(R8.L);
      case 0x26 -> sla_indirect_r8(R8.INDIRECT_HL);
      case 0x27 -> sla_r8(R8.A);
      case 0x28 -> sra_r8(R8.B);
      case 0x29 -> sra_r8(R8.C);
      case 0x2a -> sra_r8(R8.D);
      case 0x2b -> sra_r8(R8.E);
      case 0x2c -> sra_r8(R8.H);
      case 0x2d -> sra_r8(R8.L);
      case 0x2e -> sra_indirect_r8(R8.INDIRECT_HL);
      case 0x2f -> sra_r8(R8.A);
      case 0x30 -> swap(registers.b());
      case 0x31 -> swap(registers.c());
      case 0x32 -> swap(registers.d());
      case 0x33 -> swap(registers.e());
      case 0x34 -> swap(registers.h());
      case 0x35 -> swap(registers.l());
      case 0x36 -> swap_indirect_r8(R8.INDIRECT_HL);
      case 0x37 -> swap(registers.a());
      case 0x38 -> srl(registers.b());
      case 0x39 -> srl(registers.c());
      case 0x3a -> srl(registers.d());
      case 0x3b -> srl(registers.e());
      case 0x3c -> srl(registers.h());
      case 0x3d -> srl(registers.l());
      case 0x3e -> srl_indirect_r8(R8.INDIRECT_HL);
      case 0x3f -> srl(registers.a());
      case 0x40 -> bit(registers.b(), 0);
      case 0x41 -> bit(registers.c(), 0);
      case 0x42 -> bit(registers.d(), 0);
      case 0x43 -> bit(registers.e(), 0);
      case 0x44 -> bit(registers.h(), 0);
      case 0x45 -> bit(registers.l(), 0);
      case 0x46 -> bit_b3_indirect_r8(0, R8.INDIRECT_HL);
      case 0x47 -> bit(registers.a(), 0);
      case 0x48 -> bit(registers.b(), 1);
      case 0x49 -> bit(registers.c(), 1);
      case 0x4a -> bit(registers.d(), 1);
      case 0x4b -> bit(registers.e(), 1);
      case 0x4c -> bit(registers.h(), 1);
      case 0x4d -> bit(registers.l(), 1);
      case 0x4e -> bit_b3_indirect_r8(1, R8.INDIRECT_HL);
      case 0x4f -> bit(registers.a(), 1);
      case 0x50 -> bit(registers.b(), 2);
      case 0x51 -> bit(registers.c(), 2);
      case 0x52 -> bit(registers.d(), 2);
      case 0x53 -> bit(registers.e(), 2);
      case 0x54 -> bit(registers.h(), 2);
      case 0x55 -> bit(registers.l(), 2);
      case 0x56 -> bit_b3_indirect_r8(2, R8.INDIRECT_HL);
      case 0x57 -> bit(registers.a(), 2);
      case 0x58 -> bit(registers.b(), 3);
      case 0x59 -> bit(registers.c(), 3);
      case 0x5a -> bit(registers.d(), 3);
      case 0x5b -> bit(registers.e(), 3);
      case 0x5c -> bit(registers.h(), 3);
      case 0x5d -> bit(registers.l(), 3);
      case 0x5e -> bit_b3_indirect_r8(3, R8.INDIRECT_HL);
      case 0x5f -> bit(registers.a(), 3);
      case 0x60 -> bit(registers.b(), 4);
      case 0x61 -> bit(registers.c(), 4);
      case 0x62 -> bit(registers.d(), 4);
      case 0x63 -> bit(registers.e(), 4);
      case 0x64 -> bit(registers.h(), 4);
      case 0x65 -> bit(registers.l(), 4);
      case 0x66 -> bit_b3_indirect_r8(4, R8.INDIRECT_HL);
      case 0x67 -> bit(registers.a(), 4);
      case 0x68 -> bit(registers.b(), 5);
      case 0x69 -> bit(registers.c(), 5);
      case 0x6a -> bit(registers.d(), 5);
      case 0x6b -> bit(registers.e(), 5);
      case 0x6c -> bit(registers.h(), 5);
      case 0x6d -> bit(registers.l(), 5);
      case 0x6e -> bit_b3_indirect_r8(5, R8.INDIRECT_HL);
      case 0x6f -> bit(registers.a(), 5);
      case 0x70 -> bit(registers.b(), 6);
      case 0x71 -> bit(registers.c(), 6);
      case 0x72 -> bit(registers.d(), 6);
      case 0x73 -> bit(registers.e(), 6);
      case 0x74 -> bit(registers.h(), 6);
      case 0x75 -> bit(registers.l(), 6);
      case 0x76 -> bit_b3_indirect_r8(6, R8.INDIRECT_HL);
      case 0x77 -> bit(registers.a(), 6);
      case 0x78 -> bit(registers.b(), 7);
      case 0x79 -> bit(registers.c(), 7);
      case 0x7a -> bit(registers.d(), 7);
      case 0x7b -> bit(registers.e(), 7);
      case 0x7c -> bit(registers.h(), 7);
      case 0x7d -> bit(registers.l(), 7);
      case 0x7e -> bit_b3_indirect_r8(7, R8.INDIRECT_HL);
      case 0x7f -> bit(registers.a(), 7);
      case 0x80 -> res_b3_r8(0, R8.B);
      case 0x81 -> res_b3_r8(0, R8.C);
      case 0x82 -> res_b3_r8(0, R8.D);
      case 0x83 -> res_b3_r8(0, R8.E);
      case 0x84 -> res_b3_r8(0, R8.H);
      case 0x85 -> res_b3_r8(0, R8.L);
      case 0x86 -> res_b3_indirect_r8(0, R8.INDIRECT_HL);
      case 0x87 -> res_b3_r8(0, R8.A);
      case 0x88 -> res_b3_r8(1, R8.B);
      case 0x89 -> res_b3_r8(1, R8.C);
      case 0x8a -> res_b3_r8(1, R8.D);
      case 0x8b -> res_b3_r8(1, R8.E);
      case 0x8c -> res_b3_r8(1, R8.H);
      case 0x8d -> res_b3_r8(1, R8.L);
      case 0x8e -> res_b3_indirect_r8(1, R8.INDIRECT_HL);
      case 0x8f -> res_b3_r8(1, R8.A);
      case 0x90 -> res_b3_r8(2, R8.B);
      case 0x91 -> res_b3_r8(2, R8.C);
      case 0x92 -> res_b3_r8(2, R8.D);
      case 0x93 -> res_b3_r8(2, R8.E);
      case 0x94 -> res_b3_r8(2, R8.H);
      case 0x95 -> res_b3_r8(2, R8.L);
      case 0x96 -> res_b3_indirect_r8(2, R8.INDIRECT_HL);
      case 0x97 -> res_b3_r8(2, R8.A);
      case 0x98 -> res_b3_r8(3, R8.B);
      case 0x99 -> res_b3_r8(3, R8.C);
      case 0x9a -> res_b3_r8(3, R8.D);
      case 0x9b -> res_b3_r8(3, R8.E);
      case 0x9c -> res_b3_r8(3, R8.H);
      case 0x9d -> res_b3_r8(3, R8.L);
      case 0x9e -> res_b3_indirect_r8(3, R8.INDIRECT_HL);
      case 0x9f -> res_b3_r8(3, R8.A);
      case 0xa0 -> res_b3_r8(4, R8.B);
      case 0xa1 -> res_b3_r8(4, R8.C);
      case 0xa2 -> res_b3_r8(4, R8.D);
      case 0xa3 -> res_b3_r8(4, R8.E);
      case 0xa4 -> res_b3_r8(4, R8.H);
      case 0xa5 -> res_b3_r8(4, R8.L);
      case 0xa6 -> res_b3_indirect_r8(4, R8.INDIRECT_HL);
      case 0xa7 -> res_b3_r8(4, R8.A);
      case 0xa8 -> res_b3_r8(5, R8.B);
      case 0xa9 -> res_b3_r8(5, R8.C);
      case 0xaa -> res_b3_r8(5, R8.D);
      case 0xab -> res_b3_r8(5, R8.E);
      case 0xac -> res_b3_r8(5, R8.H);
      case 0xad -> res_b3_r8(5, R8.L);
      case 0xae -> res_b3_indirect_r8(5, R8.INDIRECT_HL);
      case 0xaf -> res_b3_r8(5, R8.A);
      case 0xb0 -> res_b3_r8(6, R8.B);
      case 0xb1 -> res_b3_r8(6, R8.C);
      case 0xb2 -> res_b3_r8(6, R8.D);
      case 0xb3 -> res_b3_r8(6, R8.E);
      case 0xb4 -> res_b3_r8(6, R8.H);
      case 0xb5 -> res_b3_r8(6, R8.L);
      case 0xb6 -> res_b3_indirect_r8(6, R8.INDIRECT_HL);
      case 0xb7 -> res_b3_r8(6, R8.A);
      case 0xb8 -> res_b3_r8(7, R8.B);
      case 0xb9 -> res_b3_r8(7, R8.C);
      case 0xba -> res_b3_r8(7, R8.D);
      case 0xbb -> res_b3_r8(7, R8.E);
      case 0xbc -> res_b3_r8(7, R8.H);
      case 0xbd -> res_b3_r8(7, R8.L);
      case 0xbe -> res_b3_indirect_r8(7, R8.INDIRECT_HL);
      case 0xbf -> res_b3_r8(7, R8.A);
      case 0xc0 -> set_b3_r8(0, R8.B);
      case 0xc1 -> set_b3_r8(0, R8.C);
      case 0xc2 -> set_b3_r8(0, R8.D);
      case 0xc3 -> set_b3_r8(0, R8.E);
      case 0xc4 -> set_b3_r8(0, R8.H);
      case 0xc5 -> set_b3_r8(0, R8.L);
      case 0xc6 -> set_b3_indirect_r8(0, R8.INDIRECT_HL);
      case 0xc7 -> set_b3_r8(0, R8.A);
      case 0xc8 -> set_b3_r8(1, R8.B);
      case 0xc9 -> set_b3_r8(1, R8.C);
      case 0xca -> set_b3_r8(1, R8.D);
      case 0xcb -> set_b3_r8(1, R8.E);
      case 0xcc -> set_b3_r8(1, R8.H);
      case 0xcd -> set_b3_r8(1, R8.L);
      case 0xce -> set_b3_indirect_r8(1, R8.INDIRECT_HL);
      case 0xcf -> set_b3_r8(1, R8.A);
      case 0xd0 -> set_b3_r8(2, R8.B);
      case 0xd1 -> set_b3_r8(2, R8.C);
      case 0xd2 -> set_b3_r8(2, R8.D);
      case 0xd3 -> set_b3_r8(2, R8.E);
      case 0xd4 -> set_b3_r8(2, R8.H);
      case 0xd5 -> set_b3_r8(2, R8.L);
      case 0xd6 -> set_b3_indirect_r8(2, R8.INDIRECT_HL);
      case 0xd7 -> set_b3_r8(2, R8.A);
      case 0xd8 -> set_b3_r8(3, R8.B);
      case 0xd9 -> set_b3_r8(3, R8.C);
      case 0xda -> set_b3_r8(3, R8.D);
      case 0xdb -> set_b3_r8(3, R8.E);
      case 0xdc -> set_b3_r8(3, R8.H);
      case 0xdd -> set_b3_r8(3, R8.L);
      case 0xde -> set_b3_indirect_r8(3, R8.INDIRECT_HL);
      case 0xdf -> set_b3_r8(3, R8.A);
      case 0xe0 -> set_b3_r8(4, R8.B);
      case 0xe1 -> set_b3_r8(4, R8.C);
      case 0xe2 -> set_b3_r8(4, R8.D);
      case 0xe3 -> set_b3_r8(4, R8.E);
      case 0xe4 -> set_b3_r8(4, R8.H);
      case 0xe5 -> set_b3_r8(4, R8.L);
      case 0xe6 -> set_b3_indirect_r8(4, R8.INDIRECT_HL);
      case 0xe7 -> set_b3_r8(4, R8.A);
      case 0xe8 -> set_b3_r8(5, R8.B);
      case 0xe9 -> set_b3_r8(5, R8.C);
      case 0xea -> set_b3_r8(5, R8.D);
      case 0xeb -> set_b3_r8(5, R8.E);
      case 0xec -> set_b3_r8(5, R8.H);
      case 0xed -> set_b3_r8(5, R8.L);
      case 0xee -> set_b3_indirect_r8(5, R8.INDIRECT_HL);
      case 0xef -> set_b3_r8(5, R8.A);
      case 0xf0 -> set_b3_r8(6, R8.B);
      case 0xf1 -> set_b3_r8(6, R8.C);
      case 0xf2 -> set_b3_r8(6, R8.D);
      case 0xf3 -> set_b3_r8(6, R8.E);
      case 0xf4 -> set_b3_r8(6, R8.H);
      case 0xf5 -> set_b3_r8(6, R8.L);
      case 0xf6 -> set_b3_indirect_r8(6, R8.INDIRECT_HL);
      case 0xf7 -> set_b3_r8(6, R8.A);
      case 0xf8 -> set_b3_r8(7, R8.B);
      case 0xf9 -> set_b3_r8(7, R8.C);
      case 0xfa -> set_b3_r8(7, R8.D);
      case 0xfb -> set_b3_r8(7, R8.E);
      case 0xfc -> set_b3_r8(7, R8.H);
      case 0xfd -> set_b3_r8(7, R8.L);
      case 0xfe -> set_b3_indirect_r8(7, R8.INDIRECT_HL);
      case 0xff -> set_b3_r8(7, R8.A);
      default -> throw new IllegalArgumentException();
    }
  }

  public enum R8 {
    B,
    C,
    D,
    E,
    H,
    L,
    INDIRECT_HL,
    A
  }

  public enum R16 {
    AF,
    BC,
    DE,
    HL,
    SP,
    HLI,
    HLD
  }

  public enum Cond {
    Z,
    NZ,
    C,
    NC
  }
}

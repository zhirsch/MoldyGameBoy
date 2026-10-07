package com.zacharyhirsch.moldygameboy.emulator.cpu.alu;

import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.A;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.F;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.HL;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.R;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.RR;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Sp;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.WZ;

public final class Alu {

  private Alu() {}

  public static void adc(A a, R r, F flags) {
    int lhs = Byte.toUnsignedInt(a.get());
    int rhs = Byte.toUnsignedInt(r.get());
    int carry = flags.c().get() ? 1 : 0;
    byte result = (byte) (lhs + rhs + carry);
    a.set(result);
    flags.z().set(result == 0);
    flags.n().set(false);
    flags.h().set((lhs & 0x0f) + (rhs & 0x0f) + carry > 0x0f);
    flags.c().set((lhs & 0xff) + (rhs & 0xff) + carry > 0xff);
  }

  public static void add(A a, R r, F flags) {
    int lhs = Byte.toUnsignedInt(a.get());
    int rhs = Byte.toUnsignedInt(r.get());
    byte result = (byte) (lhs + rhs);
    a.set(result);
    flags.z().set(result == 0);
    flags.n().set(false);
    flags.h().set((lhs & 0x0f) + (rhs & 0x0f) > 0x0f);
    flags.c().set((lhs & 0xff) + (rhs & 0xff) > 0xff);
  }

  public static void add16(HL hl, RR rr, F flags) {
    int lhs = Short.toUnsignedInt(hl.get());
    int rhs = Short.toUnsignedInt(rr.get());
    hl.set((short) (lhs + rhs));
    flags.n().set(false);
    flags.h().set((lhs & 0x0fff) + (rhs & 0x0fff) > 0x0fff);
    flags.c().set((lhs & 0xffff) + (rhs & 0xffff) > 0xffff);
  }

  public static void add16(Sp sp, WZ wz, F flags) {
    int lhs = Short.toUnsignedInt(sp.get());
    int rhs = Byte.toUnsignedInt(wz.lo().get());
    wz.set((short) (lhs + (int) (byte) rhs)); // sign extend rhs
    flags.z().set(false);
    flags.n().set(false);
    flags.h().set((lhs & 0x000f) + (rhs & 0x0f) > 0x000f);
    flags.c().set((lhs & 0x00ff) + (rhs & 0xff) > 0x00ff);
  }

  public static void sbc(A a, R r, F flags) {
    int lhs = Byte.toUnsignedInt(a.get());
    int rhs = Byte.toUnsignedInt(r.get());
    int carry = flags.c().get() ? 1 : 0;
    byte result = (byte) (lhs - rhs - carry);
    a.set(result);
    flags.z().set(result == 0);
    flags.n().set(true);
    flags.h().set((lhs & 0x0f) - (rhs & 0x0f) - carry < 0);
    flags.c().set((lhs & 0xff) - (rhs & 0xff) - carry < 0);
  }

  public static void sub(A a, R r, F flags) {
    int lhs = Byte.toUnsignedInt(a.get());
    int rhs = Byte.toUnsignedInt(r.get());
    byte result = (byte) (lhs - rhs);
    a.set(result);
    flags.z().set(result == 0);
    flags.n().set(true);
    flags.h().set((lhs & 0x0f) - (rhs & 0x0f) < 0);
    flags.c().set((lhs & 0xff) - (rhs & 0xff) < 0);
  }

  public static void cp(A a, R r, F flags) {
    int lhs = Byte.toUnsignedInt(a.get());
    int rhs = Byte.toUnsignedInt(r.get());
    byte result = (byte) (lhs - rhs);
    flags.z().set(result == 0);
    flags.n().set(true);
    flags.h().set((lhs & 0x0f) - (rhs & 0x0f) < 0);
    flags.c().set((lhs & 0xff) - (rhs & 0xff) < 0);
  }

  public static void inc(R r, F flags) {
    int value = Byte.toUnsignedInt(r.get());
    byte result = (byte) (value + 1);
    r.set(result);
    flags.z().set(result == 0);
    flags.n().set(false);
    flags.h().set((value & 0x0f) + 1 > 0x0f);
  }

  public static void dec(R r, F flags) {
    int value = Byte.toUnsignedInt(r.get());
    byte result = (byte) (value - 1);
    r.set(result);
    flags.z().set(result == 0);
    flags.n().set(true);
    flags.h().set((value & 0x0f) - 1 < 0);
  }

  public static void and(A a, R r, F flags) {
    byte result = (byte) (a.get() & r.get());
    a.set(result);
    flags.z().set(result == 0);
    flags.n().set(false);
    flags.h().set(true);
    flags.c().set(false);
  }

  public static void bit(R r, int index, F flags) {
    assert 0 <= index && index < 8;
    flags.z().set((byte) (r.get() & (1 << index)) == 0);
    flags.n().set(false);
    flags.h().set(true);
  }

  public static void ccf(F flags) {
    flags.n().set(false);
    flags.h().set(false);
    flags.c().set(!flags.c().get());
  }

  public static void scf(F flags) {
    flags.n().set(false);
    flags.h().set(false);
    flags.c().set(true);
  }

  public static void cpl(A a, F flags) {
    a.set((byte) ~a.get());
    flags.n().set(true);
    flags.h().set(true);
  }

  public static void or(A a, R r, F flags) {
    byte result = (byte) (a.get() | r.get());
    a.set(result);
    flags.z().set(result == 0);
    flags.n().set(false);
    flags.h().set(false);
    flags.c().set(false);
  }

  public static void res(R r, int index, F flags) {
    assert 0 <= index && index < 8;
    r.set((byte) (r.get() & ~(1 << index)));
  }

  public static void rl(R r, F flags) {
    int value = Byte.toUnsignedInt(r.get());
    int carry = flags.c().get() ? 1 : 0;
    byte result = (byte) ((value << 1) | carry);
    r.set(result);
    flags.z().set(result == 0);
    flags.n().set(false);
    flags.h().set(false);
    flags.c().set((value & 0x80) != 0);
  }

  public static void rla(A a, F flags) {
    rl(a, flags);
    flags.z().set(false);
  }

  public static void rlc(R r, F flags) {
    int value = Byte.toUnsignedInt(r.get());
    int carry = (value & 0x80) != 0 ? 1 : 0;
    byte result = (byte) ((value << 1) | carry);
    r.set(result);
    flags.z().set(result == 0);
    flags.n().set(false);
    flags.h().set(false);
    flags.c().set((value & 0x80) != 0);
  }

  public static void rlca(A a, F flags) {
    rlc(a, flags);
    flags.z().set(false);
  }

  public static void sla(R r, F flags) {
    int value = Byte.toUnsignedInt(r.get());
    byte result = (byte) ((value << 1));
    r.set(result);
    flags.z().set(result == 0);
    flags.n().set(false);
    flags.h().set(false);
    flags.c().set((value & 0x80) != 0);
  }

  public static void rr(R r, F flags) {
    int value = Byte.toUnsignedInt(r.get());
    int carry = flags.c().get() ? 0x80 : 0x00;
    byte result = (byte) ((value >>> 1) | carry);
    r.set(result);
    flags.z().set(result == 0);
    flags.n().set(false);
    flags.h().set(false);
    flags.c().set((value & 0x01) != 0);
  }

  public static void rra(A a, F flags) {
    rr(a, flags);
    flags.z().set(false);
  }

  public static void rrc(R r, F flags) {
    int value = Byte.toUnsignedInt(r.get());
    int carry = (r.get() & 0x01) != 0 ? 0x80 : 0x00;
    byte result = (byte) ((value >>> 1) | carry);
    r.set(result);
    flags.z().set(result == 0);
    flags.n().set(false);
    flags.h().set(false);
    flags.c().set((value & 0x01) != 0);
  }

  public static void rrca(A a, F flags) {
    rrc(a, flags);
    flags.z().set(false);
  }

  public static void sra(R r, F flags) {
    int value = Byte.toUnsignedInt(r.get());
    int carry = (r.get() & 0x80) != 0 ? 0x80 : 0x00;
    byte result = (byte) ((value >>> 1) | carry);
    r.set(result);
    flags.z().set(result == 0);
    flags.n().set(false);
    flags.h().set(false);
    flags.c().set((value & 0x01) != 0);
  }

  public static void srl(R r, F flags) {
    int value = Byte.toUnsignedInt(r.get());
    byte result = (byte) (value >>> 1);
    r.set(result);
    flags.z().set(result == 0);
    flags.n().set(false);
    flags.h().set(false);
    flags.c().set((value & 0x01) != 0);
  }

  public static void set(R r, int index, F flags) {
    assert 0 <= index && index < 8;
    r.set((byte) (r.get() | (1 << index)));
  }

  public static void swap(R r, F flags) {
    int lo = Byte.toUnsignedInt((byte) (r.get() & 0x0f));
    int hi = Byte.toUnsignedInt((byte) (r.get() & 0xf0));
    byte result = (byte) ((lo << 4) | (hi >> 4));
    r.set(result);
    flags.z().set(result == 0);
    flags.n().set(false);
    flags.h().set(false);
    flags.c().set(false);
  }

  public static void xor(A a, R r, F flags) {
    byte result = (byte) (a.get() ^ r.get());
    a.set(result);
    flags.z().set(result == 0);
    flags.n().set(false);
    flags.h().set(false);
    flags.c().set(false);
  }
}

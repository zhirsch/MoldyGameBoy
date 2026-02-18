package com.zacharyhirsch.moldygameboy.emulator.cpu.registers;

import com.zacharyhirsch.moldygameboy.emulator.arch.Register16;
import com.zacharyhirsch.moldygameboy.emulator.arch.Register8;

public record Registers(
    Register8 ir,
    Register8 ie,
    Register16<Register8, FlagsRegister> af,
    Register16<Register8, Register8> bc,
    Register16<Register8, Register8> de,
    Register16<Register8, Register8> hl,
    Register16<Register8, Register8> pc,
    Register16<Register8, Register8> sp,
    Ime ime) {

  public Registers() {
    this(
        new Register8(),
        new Register8(),
        new Register16<>(new Register8(), new FlagsRegister()),
        new Register16<>(new Register8(), new Register8()),
        new Register16<>(new Register8(), new Register8()),
        new Register16<>(new Register8(), new Register8()),
        new Register16<>(new Register8(), new Register8()),
        new Register16<>(new Register8(), new Register8()),
        new Ime());
  }

  public Register8 a() {
    return af().hi();
  }

  public FlagsRegister f() {
    return af().lo();
  }

  public Register8 b() {
    return bc().hi();
  }

  public Register8 c() {
    return bc().lo();
  }

  public Register8 d() {
    return de().hi();
  }

  public Register8 e() {
    return de().lo();
  }

  public Register8 h() {
    return hl().hi();
  }

  public Register8 l() {
    return hl().lo();
  }

}

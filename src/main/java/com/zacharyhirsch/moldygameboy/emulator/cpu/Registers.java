package com.zacharyhirsch.moldygameboy.emulator.cpu;

record Registers(
    Register8 ir,
    Register8 ie,
    Register16 af,
    Register16 bc,
    Register16 de,
    Register16 hl,
    Register16 pc,
    Register16 sp) {

  Registers() {
    this(
        new Register8(),
        new Register8(),
        new Register16(),
        new Register16(),
        new Register16(),
        new Register16(),
        new Register16(),
        new Register16());
  }

  Register8 a() {
    return af().hi();
  }

  Register8 f() {
    return af().lo();
  }

  Register8 b() {
    return bc().hi();
  }

  Register8 c() {
    return bc().lo();
  }

  Register8 d() {
    return de().hi();
  }

  Register8 e() {
    return de().lo();
  }

  Register8 h() {
    return hl().hi();
  }

  Register8 l() {
    return hl().lo();
  }

}

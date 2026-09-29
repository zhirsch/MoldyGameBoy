package com.zacharyhirsch.moldygameboy.emulator.cpu.registers;

public record Registers(
    R ir,
    R ie,
    RR af,
    RR bc,
    RR de,
    RR hl,
    RR wz,
    RR pc,
    RR sp,
    R ime) {

  public Registers() {
    this(
        new R(),
        new R(),
        new RR(),
        new RR(),
        new RR(),
        new RR(),
        new RR(),
        new RR(),
        new RR(),
        new R());
  }

  public R a() {
    return af().hi();
  }

  public FlagsRegister f() {
    return new FlagsRegister(af().lo());
  }

  public R b() {
    return bc().hi();
  }

  public R c() {
    return bc().lo();
  }

  public R d() {
    return de().hi();
  }

  public R e() {
    return de().lo();
  }

  public R h() {
    return hl().hi();
  }

  public R l() {
    return hl().lo();
  }

  public R w() {
    return wz().hi();
  }

  public R z() {
    return wz().lo();
  }
}

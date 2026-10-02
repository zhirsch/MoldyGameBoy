package com.zacharyhirsch.moldygameboy.emulator.cpu.registers;

public record Registers(Ir ir, Ie ie, AF af, BC bc, DE de, HL hl, WZ wz, Pc pc, Sp sp, Ime ime) {

  public Registers() {
    this(
        new Ir(), new Ie(), new AF(), new BC(), new DE(), new HL(), new WZ(), new Pc(), new Sp(),
        new Ime());
  }

  public A a() {
    return af().hi();
  }

  public F f() {
    return af().lo();
  }

  public B b() {
    return bc().hi();
  }

  public C c() {
    return bc().lo();
  }

  public D d() {
    return de().hi();
  }

  public E e() {
    return de().lo();
  }

  public H h() {
    return hl().hi();
  }

  public L l() {
    return hl().lo();
  }

  public W w() {
    return wz().hi();
  }

  public Z z() {
    return wz().lo();
  }

  @Override
  public String toString() {
    return "PC=%04x IR=%02x AF=%04x BC=%04x DE=%04x HL=%04x SP=%04x"
        .formatted(
            pc().get(), ir().get(), af().get(), bc().get(), de().get(), hl().get(), sp().get());
  }
}

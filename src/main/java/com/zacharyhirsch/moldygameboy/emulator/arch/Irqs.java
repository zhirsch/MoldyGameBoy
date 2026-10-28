package com.zacharyhirsch.moldygameboy.emulator.arch;

public final class Irqs {

  private final IoRegister if_;
  private final Irq vblank;
  private final Irq lcd;
  private final Irq timer;
  private final Irq serial;
  private final Irq joypad;

  public Irqs() {
    this.if_ = IoRegister.create("if", (short) 0xff0f);
    this.vblank = new Irq(InterruptType.VBLANK, if_);
    this.lcd = new Irq(InterruptType.LCD, if_);
    this.timer = new Irq(InterruptType.TIMER, if_);
    this.serial = new Irq(InterruptType.SERIAL, if_);
    this.joypad = new Irq(InterruptType.JOYPAD, if_);
  }

  public IoRegister if_() {
    return if_;
  }

  public Irq vblank() {
    return vblank;
  }

  public Irq lcd() {
    return lcd;
  }

  public Irq timer() {
    return timer;
  }

  public Irq serial() {
    return serial;
  }

  public Irq joypad() {
    return joypad;
  }
}

package com.zacharyhirsch.moldygameboy.emulator.arch;

public record Irqs(Line vblank, Line lcd, Line timer, Line serial, Line joypad) {

  public Irqs(IoRegister if_) {
    this(
        new Line(InterruptType.VBLANK, if_),
        new Line(InterruptType.LCD, if_),
        new Line(InterruptType.TIMER, if_),
        new Line(InterruptType.SERIAL, if_),
        new Line(InterruptType.JOYPAD, if_));
  }

  public static final class Line {

    private final InterruptType interrupt;
    private final IoRegister if_;

    private boolean oldValue = false;

    public Line(InterruptType interrupt, IoRegister if_) {
      this.interrupt = interrupt;
      this.if_ = if_;
    }

    public void set(boolean newValue) {
      if (!oldValue && newValue) {
        if_.set(if_.get() | interrupt.mask());
      }
      oldValue = newValue;
    }
  }
}

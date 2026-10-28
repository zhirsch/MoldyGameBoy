package com.zacharyhirsch.moldygameboy.emulator.arch;

public final class Irq {

  private final InterruptType interrupt;
  private final IoRegister if_;

  private boolean oldValue = false;

  public Irq(InterruptType interrupt, IoRegister if_) {
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

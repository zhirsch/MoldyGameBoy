package com.zacharyhirsch.moldygameboy.emulator.arch;

public record Irqs(Line vblank, Line lcd, Line timer, Line serial, Line joypad) {

  public Irqs() {
    this(new Line(), new Line(), new Line(), new Line(), new Line());
  }

  public static final class Line {

    private boolean oldValue = false;
    private boolean asserted = false;

    public Line() {}

    public boolean get() {
      boolean asserted = this.asserted;
      this.asserted = false;
      return asserted;
    }

    public void set(boolean newValue) {
      if (!oldValue && newValue) {
        asserted = true;
      }
      oldValue = newValue;
    }
  }
}

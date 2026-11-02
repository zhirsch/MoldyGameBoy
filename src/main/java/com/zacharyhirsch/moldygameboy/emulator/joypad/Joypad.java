package com.zacharyhirsch.moldygameboy.emulator.joypad;

import com.zacharyhirsch.moldygameboy.emulator.arch.IoRegister;
import com.zacharyhirsch.moldygameboy.emulator.arch.Irq;
import com.zacharyhirsch.moldygameboy.emulator.io.Joypads;
import com.zacharyhirsch.moldygameboy.emulator.io.Joypads.Button;

/**
 * The joypad, read through JOYP ($FF00). Writing 0 to bit 4 selects the direction buttons and 0 to
 * bit 5 selects the action buttons; the low 4 bits then read 0 for each selected button that's
 * pressed. The joypad interrupt is requested whenever one of those 4 lines goes from high to low.
 */
public final class Joypad {

  private static final Button[] DIRECTIONS = {Button.RIGHT, Button.LEFT, Button.UP, Button.DOWN};
  private static final Button[] ACTIONS = {
    Button.BUTTON_A, Button.BUTTON_B, Button.SELECT, Button.START
  };

  private final Joypads joypads;
  private final Irq irq;

  private int select = 0x00;
  private int lines = 0x0f;

  public Joypad(Joypads joypads, Irq irq) {
    this.joypads = joypads;
    this.irq = irq;
  }

  public IoRegister joyp() {
    return new IoRegister() {
      @Override
      public int get() {
        return select | lines();
      }

      @Override
      public void set(int value) {
        select = value & 0x30;
      }

      @Override
      public String toString() {
        return "joyp @ ff00 = %02x".formatted(get());
      }
    };
  }

  public void tick() {
    int newLines = lines();
    irq.set((lines & ~newLines) != 0);
    lines = newLines;
  }

  private int lines() {
    int pressed = 0;
    if ((select & 0x10) == 0) {
      pressed |= pressed(DIRECTIONS);
    }
    if ((select & 0x20) == 0) {
      pressed |= pressed(ACTIONS);
    }
    return ~pressed & 0x0f;
  }

  private int pressed(Button[] buttons) {
    int pressed = 0;
    for (int i = 0; i < buttons.length; i++) {
      if (joypads.isPressed(buttons[i])) {
        pressed |= 1 << i;
      }
    }
    return pressed;
  }
}

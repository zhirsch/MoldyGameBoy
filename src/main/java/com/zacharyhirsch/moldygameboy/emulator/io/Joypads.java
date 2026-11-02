package com.zacharyhirsch.moldygameboy.emulator.io;

/** The state of the Game Boy's buttons. */
public interface Joypads extends AutoCloseable {

  enum Button {
    RIGHT,
    LEFT,
    UP,
    DOWN,
    BUTTON_A,
    BUTTON_B,
    SELECT,
    START,
  }

  boolean isPressed(Button button);
}

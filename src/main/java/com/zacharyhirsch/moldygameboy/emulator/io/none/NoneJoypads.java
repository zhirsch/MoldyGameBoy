package com.zacharyhirsch.moldygameboy.emulator.io.none;

import com.zacharyhirsch.moldygameboy.emulator.io.Joypads;

final class NoneJoypads implements Joypads {

  @Override
  public boolean isPressed(Button button) {
    return false;
  }

  @Override
  public void close() {}
}

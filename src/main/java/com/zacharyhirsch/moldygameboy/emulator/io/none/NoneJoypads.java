package com.zacharyhirsch.moldygameboy.emulator.io.none;

import com.zacharyhirsch.moldygameboy.emulator.io.Joypads;

final class NoneJoypads implements Joypads {

  @Override
  public byte readJoypad(int index) {
    return 0;
  }

  @Override
  public void writeJoypads(byte data) {}

  @Override
  public void close() {}
}

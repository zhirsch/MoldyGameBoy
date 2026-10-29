package com.zacharyhirsch.moldygameboy.emulator.io;

public interface Ticker {

  void tick();

  boolean isDone();
}

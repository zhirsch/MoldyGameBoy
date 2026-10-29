package com.zacharyhirsch.moldygameboy.emulator.io.none;

import com.zacharyhirsch.moldygameboy.emulator.io.EventLoop;
import com.zacharyhirsch.moldygameboy.emulator.io.Ticker;

final class NoneEventLoop implements EventLoop {

  @Override
  public void run(Ticker ticker) {
    while (!ticker.isDone()) {
      ticker.tick();
    }
  }
}

package com.zacharyhirsch.moldygameboy.emulator.io.none;

import com.zacharyhirsch.moldygameboy.emulator.io.EventLoop;

final class NoneEventLoop implements EventLoop {

  @Override
  public void run(Runnable tick) {}
}

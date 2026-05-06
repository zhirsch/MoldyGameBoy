package com.zacharyhirsch.moldygameboy.emulator.io;

public interface EventLoop {

  void run(Runnable tick);
}

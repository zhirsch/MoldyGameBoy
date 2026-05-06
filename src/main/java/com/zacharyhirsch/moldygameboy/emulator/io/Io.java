package com.zacharyhirsch.moldygameboy.emulator.io;

public interface Io extends AutoCloseable {

  Audio audio();

  Video video();

  Joypads joypads();

  EventLoop eventLoop();
}

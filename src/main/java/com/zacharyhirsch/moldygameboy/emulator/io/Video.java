package com.zacharyhirsch.moldygameboy.emulator.io;

public interface Video extends AutoCloseable {

  void writeVideoPixel(int x, int y, Color color);

  void setError(Throwable error);

  void present();
}

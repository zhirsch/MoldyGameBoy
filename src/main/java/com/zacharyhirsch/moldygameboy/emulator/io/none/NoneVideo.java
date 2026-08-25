package com.zacharyhirsch.moldygameboy.emulator.io.none;

import com.zacharyhirsch.moldygameboy.emulator.io.Color;
import com.zacharyhirsch.moldygameboy.emulator.io.Video;

final class NoneVideo implements Video {

  @Override
  public void writeVideoPixel(int x, int y, Color color) {}

  @Override
  public void setError(Throwable error) {}

  @Override
  public void present() {}

  @Override
  public void close() {}
}

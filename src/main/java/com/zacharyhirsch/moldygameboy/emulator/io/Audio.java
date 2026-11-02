package com.zacharyhirsch.moldygameboy.emulator.io;

public interface Audio extends AutoCloseable {

  /** Writes one stereo sample, each side in [-1, 1]. */
  void writeAudioSample(float left, float right);

  void present();
}

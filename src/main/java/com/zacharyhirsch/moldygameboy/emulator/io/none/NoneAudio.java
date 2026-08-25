package com.zacharyhirsch.moldygameboy.emulator.io.none;

import com.zacharyhirsch.moldygameboy.emulator.io.Audio;

final class NoneAudio implements Audio {

  @Override
  public void writeAudioSample(float sample) {}

  @Override
  public void present() {}

  @Override
  public void close() {}
}

package com.zacharyhirsch.moldygameboy.emulator.io;

public interface Audio extends AutoCloseable {

  void writeAudioSample(float sample);

  void present();
}

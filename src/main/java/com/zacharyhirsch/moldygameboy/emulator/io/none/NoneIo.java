package com.zacharyhirsch.moldygameboy.emulator.io.none;

import com.zacharyhirsch.moldygameboy.emulator.io.Audio;
import com.zacharyhirsch.moldygameboy.emulator.io.EventLoop;
import com.zacharyhirsch.moldygameboy.emulator.io.Io;
import com.zacharyhirsch.moldygameboy.emulator.io.Joypads;
import com.zacharyhirsch.moldygameboy.emulator.io.Video;

public class NoneIo implements Io {

  @Override
  public Audio audio() {
    return new NoneAudio();
  }

  @Override
  public Video video() {
    return new NoneVideo();
  }

  @Override
  public Joypads joypads() {
    return new NoneJoypads();
  }

  @Override
  public EventLoop eventLoop() {
    return new NoneEventLoop();
  }

  @Override
  public void close() {}
}

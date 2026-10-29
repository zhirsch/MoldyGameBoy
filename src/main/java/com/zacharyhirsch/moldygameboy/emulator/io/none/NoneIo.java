package com.zacharyhirsch.moldygameboy.emulator.io.none;

import com.zacharyhirsch.moldygameboy.emulator.io.Audio;
import com.zacharyhirsch.moldygameboy.emulator.io.EventLoop;
import com.zacharyhirsch.moldygameboy.emulator.io.Io;
import com.zacharyhirsch.moldygameboy.emulator.io.Joypads;
import com.zacharyhirsch.moldygameboy.emulator.io.Ticker;
import com.zacharyhirsch.moldygameboy.emulator.io.Video;

public final class NoneIo implements Io {

  private final NoneAudio audio;
  private final NoneVideo video;
  private final NoneJoypads joypads;
  private final NoneEventLoop eventLoop;

  public NoneIo() {
    this.audio = new NoneAudio();
    this.video = new NoneVideo();
    this.joypads = new NoneJoypads();
    this.eventLoop = new NoneEventLoop();
  }

  @Override
  public Audio audio() {
    return audio;
  }

  @Override
  public Video video() {
    return video;
  }

  @Override
  public Joypads joypads() {
    return joypads;
  }

  @Override
  public EventLoop eventLoop() {
    return eventLoop;
  }

  @Override
  public void close() {}
}

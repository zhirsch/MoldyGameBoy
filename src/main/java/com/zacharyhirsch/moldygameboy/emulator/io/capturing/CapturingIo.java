package com.zacharyhirsch.moldygameboy.emulator.io.capturing;

import com.zacharyhirsch.moldygameboy.emulator.io.Audio;
import com.zacharyhirsch.moldygameboy.emulator.io.EventLoop;
import com.zacharyhirsch.moldygameboy.emulator.io.Io;
import com.zacharyhirsch.moldygameboy.emulator.io.Joypads;

/** Delegates to another {@link Io}, but records every pixel written to the screen. */
public record CapturingIo(Io delegate, CapturingVideo video) implements Io {

  public CapturingIo(Io delegate) {
    this(delegate, new CapturingVideo(delegate.video()));
  }

  @Override
  public Audio audio() {
    return delegate.audio();
  }

  @Override
  public Joypads joypads() {
    return delegate.joypads();
  }

  @Override
  public EventLoop eventLoop() {
    return delegate.eventLoop();
  }

  @Override
  public void close() throws Exception {
    delegate.close();
    video.close();
  }
}

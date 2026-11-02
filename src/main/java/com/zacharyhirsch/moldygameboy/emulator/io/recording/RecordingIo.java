package com.zacharyhirsch.moldygameboy.emulator.io.recording;

import com.zacharyhirsch.moldygameboy.emulator.io.Audio;
import com.zacharyhirsch.moldygameboy.emulator.io.Color;
import com.zacharyhirsch.moldygameboy.emulator.io.EventLoop;
import com.zacharyhirsch.moldygameboy.emulator.io.Io;
import com.zacharyhirsch.moldygameboy.emulator.io.Joypads;
import com.zacharyhirsch.moldygameboy.emulator.io.Video;
import java.nio.file.Path;

/** Delegates to another {@link Io}, and also records the video and audio to an MP4 file. */
public final class RecordingIo implements Io {

  private final Io delegate;
  private final Mp4Recorder recorder;
  private final Video video;
  private final Audio audio;

  public RecordingIo(Io delegate, Path output) {
    this.delegate = delegate;
    this.recorder = new Mp4Recorder(output);
    this.video = new RecordingVideo();
    this.audio = new RecordingAudio();
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
    return delegate.joypads();
  }

  @Override
  public EventLoop eventLoop() {
    return delegate.eventLoop();
  }

  @Override
  public void close() throws Exception {
    try {
      delegate.close();
    } finally {
      recorder.close();
    }
  }

  private final class RecordingVideo implements Video {

    @Override
    public void writeVideoPixel(int x, int y, Color color) {
      delegate.video().writeVideoPixel(x, y, color);
      recorder.writeVideoPixel(x, y, color);
    }

    @Override
    public void setError(Throwable error) {
      delegate.video().setError(error);
    }

    @Override
    public void present() {
      delegate.video().present();
      recorder.presentVideo();
    }

    @Override
    public void close() throws Exception {
      delegate.video().close();
    }
  }

  private final class RecordingAudio implements Audio {

    @Override
    public void writeAudioSample(float left, float right) {
      delegate.audio().writeAudioSample(left, right);
      recorder.writeAudioSample(left, right);
    }

    @Override
    public void present() {
      delegate.audio().present();
      recorder.presentAudio();
    }

    @Override
    public void close() throws Exception {
      delegate.audio().close();
    }
  }
}

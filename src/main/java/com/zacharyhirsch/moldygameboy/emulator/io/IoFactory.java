package com.zacharyhirsch.moldygameboy.emulator.io;

import com.zacharyhirsch.moldygameboy.emulator.io.none.NoneIo;
import com.zacharyhirsch.moldygameboy.emulator.io.recording.RecordingIo;
import com.zacharyhirsch.moldygameboy.emulator.io.sdl.SdlIo;
import java.lang.foreign.Arena;
import java.nio.file.Path;

public final class IoFactory {

  public static Io none() {
    return new NoneIo();
  }

  public static Io sdl() {
    return new SdlIo(Arena.global());
  }

  /** Wraps {@code delegate}, also recording its video and audio to an MP4 file. */
  public static Io recording(Io delegate, Path output) {
    return new RecordingIo(delegate, output);
  }
}

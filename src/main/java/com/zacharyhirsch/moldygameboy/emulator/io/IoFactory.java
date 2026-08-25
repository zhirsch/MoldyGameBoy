package com.zacharyhirsch.moldygameboy.emulator.io;

import com.zacharyhirsch.moldygameboy.emulator.io.none.NoneIo;
import com.zacharyhirsch.moldygameboy.emulator.io.sdl.SdlIo;
import java.lang.foreign.Arena;

public final class IoFactory {

  public static Io none() {
    return new NoneIo();
  }

  public static Io sdl() {
    return new SdlIo(Arena.global());
  }
}

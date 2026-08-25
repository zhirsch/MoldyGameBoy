package com.zacharyhirsch.moldygameboy.emulator.io.sdl;

import static com.zacharyhirsch.jna.sdl3.SDL.*;

final class SdlException extends RuntimeException {

  SdlException(String message) {
    super("%s: %s".formatted(message, SDL_GetError().getString(0)));
  }
}

package com.zacharyhirsch.moldygameboy.emulator.io.sdl;

import static com.zacharyhirsch.jna.sdl3.SDL.*;

import com.zacharyhirsch.jna.sdl3.SDL_Event;
import com.zacharyhirsch.moldygameboy.emulator.io.EventLoop;
import com.zacharyhirsch.moldygameboy.emulator.io.Ticker;
import com.zacharyhirsch.moldygameboy.emulator.io.Video;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.time.Duration;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class SdlEventLoop implements EventLoop {

  private static final Logger log = LoggerFactory.getLogger(SdlEventLoop.class);

  // A frame's worth of dots.
  private static final int TICKS_PER_PUMP = 70224;
  private static final Duration ERROR_PUMP_INTERVAL = Duration.ofMillis(16);

  private final MemorySegment event;
  private final Video video;
  private final Consumer<MemorySegment> onGamepadAdded;
  private final Consumer<MemorySegment> onGamepadRemoved;
  private final Consumer<MemorySegment> onGamepadAxisMotion;
  private final Consumer<MemorySegment> onGamepadButtonDown;
  private final Consumer<MemorySegment> onGamepadButtonUp;
  private final Consumer<MemorySegment> onKeyDown;
  private final Consumer<MemorySegment> onKeyUp;

  private boolean quit = false;

  SdlEventLoop(
      Arena arena,
      Video video,
      Consumer<MemorySegment> onGamepadAdded,
      Consumer<MemorySegment> onGamepadRemoved,
      Consumer<MemorySegment> onGamepadAxisMotion,
      Consumer<MemorySegment> onGamepadButtonDown,
      Consumer<MemorySegment> onGamepadButtonUp,
      Consumer<MemorySegment> onKeyDown,
      Consumer<MemorySegment> onKeyUp) {
    this.event = SDL_Event.allocate(arena);
    this.video = video;
    this.onGamepadAdded = onGamepadAdded;
    this.onGamepadRemoved = onGamepadRemoved;
    this.onGamepadAxisMotion = onGamepadAxisMotion;
    this.onGamepadButtonDown = onGamepadButtonDown;
    this.onGamepadButtonUp = onGamepadButtonUp;
    this.onKeyDown = onKeyDown;
    this.onKeyUp = onKeyUp;
  }

  /**
   * Ticks until the ticker is done or the window is closed, handling events about once per frame.
   * If ticking fails, the error is shown until the window is closed, then rethrown.
   */
  @Override
  public void run(Ticker ticker) {
    try {
      while (!quit && !ticker.isDone()) {
        for (int i = 0; i < TICKS_PER_PUMP; i++) {
          ticker.tick();
        }
        pump();
      }
    } catch (Exception e) {
      log.error("Emulation failed", e);
      video.setError(e);
      while (!quit) {
        pump();
        SDL_DelayNS(ERROR_PUMP_INTERVAL.toNanos());
      }
      throw e;
    }
  }

  public void pump() {
    while (SDL_PollEvent(event)) {
      int type = SDL_Event.type(event);
      if (type == SDL_EVENT_QUIT()) {
        quit = true;
        continue;
      }
      if (type == SDL_EVENT_GAMEPAD_ADDED()) {
        onGamepadAdded.accept(SDL_Event.gdevice(event));
        continue;
      }
      if (type == SDL_EVENT_GAMEPAD_REMOVED()) {
        onGamepadRemoved.accept(SDL_Event.gdevice(event));
        continue;
      }
      if (type == SDL_EVENT_GAMEPAD_AXIS_MOTION()) {
        onGamepadAxisMotion.accept(SDL_Event.gaxis(event));
        continue;
      }
      if (type == SDL_EVENT_GAMEPAD_BUTTON_DOWN()) {
        onGamepadButtonDown.accept(SDL_Event.gbutton(event));
        continue;
      }
      if (type == SDL_EVENT_GAMEPAD_BUTTON_UP()) {
        onGamepadButtonUp.accept(SDL_Event.gbutton(event));
        continue;
      }
      if (type == SDL_EVENT_KEY_DOWN()) {
        onKeyDown.accept(SDL_Event.key(event));
        continue;
      }
      if (type == SDL_EVENT_KEY_UP()) {
        onKeyUp.accept(SDL_Event.key(event));
        continue;
      }
    }
  }
}

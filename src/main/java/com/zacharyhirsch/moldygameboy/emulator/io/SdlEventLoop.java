package com.zacharyhirsch.moldygameboy.emulator.io;

import static com.zacharyhirsch.jna.sdl3.SDL.*;

import com.zacharyhirsch.jna.sdl3.SDL_Event;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.function.Consumer;

final class SdlEventLoop implements EventLoop {

  private final MemorySegment event;
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
      Consumer<MemorySegment> onGamepadAdded,
      Consumer<MemorySegment> onGamepadRemoved,
      Consumer<MemorySegment> onGamepadAxisMotion,
      Consumer<MemorySegment> onGamepadButtonDown,
      Consumer<MemorySegment> onGamepadButtonUp,
      Consumer<MemorySegment> onKeyDown,
      Consumer<MemorySegment> onKeyUp) {
    this.event = SDL_Event.allocate(arena);
    this.onGamepadAdded = onGamepadAdded;
    this.onGamepadRemoved = onGamepadRemoved;
    this.onGamepadAxisMotion = onGamepadAxisMotion;
    this.onGamepadButtonDown = onGamepadButtonDown;
    this.onGamepadButtonUp = onGamepadButtonUp;
    this.onKeyDown = onKeyDown;
    this.onKeyUp = onKeyUp;
  }

  @Override
  public void run(Runnable tick) {
    int i = 0;
    while (!quit) {
      tick.run();
      if ((i++ % 100) == 0) {
        pump();
      }
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

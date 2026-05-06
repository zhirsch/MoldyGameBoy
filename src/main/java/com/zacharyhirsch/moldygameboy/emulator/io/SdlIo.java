package com.zacharyhirsch.moldygameboy.emulator.io;

import static com.zacharyhirsch.jna.sdl3.SDL.*;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public final class SdlIo implements Io {

  private static final double FPS = 60.0988;
  private static final int CPU_CYCLES_PER_SECOND = (int) (341 * 262 * FPS / 3);

  private final SdlEventLoop eventLoop;
  private final SdlAudio audio;
  private final SdlVideo video;
  private final SdlJoypads joypads;

  public SdlIo(Arena arena) {
    this.eventLoop =
        new SdlEventLoop(
            arena,
            this::onGamepadAdded,
            this::onGamepadRemoved,
            this::onGamepadAxisMotion,
            this::onGamepadButtonDown,
            this::onGamepadButtonUp,
            this::onKeyDown,
            this::onKeyUp);
    this.audio = new SdlAudio(arena, CPU_CYCLES_PER_SECOND);
    this.video = new SdlVideo(arena);
    this.joypads = new SdlJoypads();
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
  public void close() {
    audio.close();
    video.close();
    SDL_Quit();
  }

  private void onGamepadAdded(MemorySegment gamepadDeviceEvent) {
    joypads.onGamepadAdded(gamepadDeviceEvent);
  }

  private void onGamepadRemoved(MemorySegment gamepadDeviceEvent) {
    joypads.onGamepadRemoved(gamepadDeviceEvent);
  }

  private void onGamepadAxisMotion(MemorySegment gamepadAxisEvent) {
    joypads.onGamepadAxisMotion(gamepadAxisEvent);
  }

  private void onGamepadButtonDown(MemorySegment gamepadButtonEvent) {
    joypads.onGamepadButtonDown(gamepadButtonEvent);
  }

  private void onGamepadButtonUp(MemorySegment gamepadButtonEvent) {
    joypads.onGamepadButtonUp(gamepadButtonEvent);
  }

  private void onKeyDown(MemorySegment keyboardEvent) {
    joypads.onKeyDown(keyboardEvent);
  }

  private void onKeyUp(MemorySegment keyboardEvent) {
    joypads.onKeyUp(keyboardEvent);
  }
}

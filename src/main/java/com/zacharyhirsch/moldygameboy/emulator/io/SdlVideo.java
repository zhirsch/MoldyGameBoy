package com.zacharyhirsch.moldygameboy.emulator.io;

import static com.zacharyhirsch.jna.sdl3.SDL.*;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.time.Duration;
import java.time.Instant;

final class SdlVideo implements Video {

  private static final int W = 160;
  private static final int H = 144;
  private static final int SCALE = 3;
  private static final double FPS = (1 << 22) / 70224.0;
  private static final Duration FRAME_DURATION = Duration.ofNanos((long) (1_000_000_000 / FPS));

  private final MemorySegment window;
  private final MemorySegment renderer;
  private final MemorySegment texture;
  private final MemorySegment pixelsPtr;
  private final MemorySegment pitch;
  private final byte[] frame;

  private Instant lastFrameTime = null;

  SdlVideo(Arena arena) {
    if (!SDL_InitSubSystem(SDL_INIT_VIDEO())) {
      throw new SdlException("Unable to initialize SDL video library");
    }

    MemorySegment title = arena.allocateFrom("MoldyGameBoy");
    MemorySegment windowPtr = arena.allocate(C_POINTER);
    MemorySegment rendererPtr = arena.allocate(C_POINTER);
    if (!SDL_CreateWindowAndRenderer(title, W * SCALE, H * SCALE, 0, windowPtr, rendererPtr)) {
      throw new SdlException("Unable to create SDL window and renderer");
    }
    window = windowPtr.get(C_POINTER, 0);
    renderer = rendererPtr.get(C_POINTER, 0);
    if (!SDL_SetRenderLogicalPresentation(renderer, W, H, SDL_LOGICAL_PRESENTATION_DISABLED())) {
      throw new SdlException("Unable to set logical size");
    }
    if (!SDL_SetRenderDrawColor(renderer, (byte) 0, (byte) 255, (byte) 0, (byte) 255)) {
      throw new SdlException("Unable to set draw color");
    }
    texture =
        SDL_CreateTexture(renderer, SDL_PIXELFORMAT_RGB24(), SDL_TEXTUREACCESS_STREAMING(), W, H);
    if (texture == null) {
      throw new SdlException("Unable to create SDL texture");
    }

    this.pixelsPtr = arena.allocate(C_POINTER);
    this.pitch = arena.allocate(C_POINTER);
    this.frame = new byte[W * H * 3];
  }

  @Override
  public void writeVideoPixel(int x, int y, Color color) {
    assert 0 <= x && x < W;
    assert 0 <= y && y < H;
    int i = 3 * (y * W + x);
    if (i == 69120) {
      throw new IllegalStateException();
    }
    frame[i + 0] = color.r();
    frame[i + 1] = color.g();
    frame[i + 2] = color.b();
  }

  @Override
  public void setError(Throwable error) {
    if (!SDL_SetRenderDrawColor(renderer, (byte) 255, (byte) 0, (byte) 0, (byte) 255)) {
      throw new SdlException("Unable to set draw color");
    }
    if (!SDL_RenderRect(renderer, MemorySegment.NULL)) {
      throw new SdlException("Unable to draw rectangle");
    }
    if (!SDL_RenderPresent(renderer)) {
      throw new SdlException("Unable to present renderer");
    }
  }

  @Override
  public void present() {
    delay();
    if (!SDL_LockTexture(texture, MemorySegment.NULL, pixelsPtr, pitch)) {
      throw new SdlException("Unable to lock texture");
    }
    MemorySegment pixels = pixelsPtr.get(C_POINTER, 0);
    MemorySegment.copy(frame, 0, pixels, ValueLayout.JAVA_BYTE, 0, frame.length);
    SDL_UnlockTexture(texture);
    if (!SDL_RenderTexture(renderer, texture, MemorySegment.NULL, MemorySegment.NULL)) {
      throw new SdlException("Unable to render texture");
    }
    if (!SDL_RenderPresent(renderer)) {
      throw new SdlException("Unable to present renderer");
    }
  }

  @Override
  public void close() {
    SDL_DestroyRenderer(renderer);
    SDL_DestroyWindow(window);
  }

  private void delay() {
    if (lastFrameTime != null) {
      Duration elapsed = Duration.between(lastFrameTime, Instant.now());
      Duration delay = FRAME_DURATION.minus(elapsed);
      if (delay.isPositive()) {
        SDL_DelayNS(delay.toNanos());
      }
    }
    lastFrameTime = Instant.now();
  }
}

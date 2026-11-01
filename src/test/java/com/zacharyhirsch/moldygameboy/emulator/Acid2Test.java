package com.zacharyhirsch.moldygameboy.emulator;

import static com.google.common.truth.Truth.assertWithMessage;

import com.google.common.io.Resources;
import com.zacharyhirsch.moldygameboy.emulator.io.Audio;
import com.zacharyhirsch.moldygameboy.emulator.io.Color;
import com.zacharyhirsch.moldygameboy.emulator.io.EventLoop;
import com.zacharyhirsch.moldygameboy.emulator.io.Io;
import com.zacharyhirsch.moldygameboy.emulator.io.IoFactory;
import com.zacharyhirsch.moldygameboy.emulator.io.Joypads;
import com.zacharyhirsch.moldygameboy.emulator.io.Ticker;
import com.zacharyhirsch.moldygameboy.emulator.io.Video;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

final class Acid2Test {

  private static final int WIDTH = 160;
  private static final int HEIGHT = 144;

  @Test
  void testDmgAcid2() throws Exception {
    BufferedImage reference =
        ImageIO.read(Resources.getResource("roms/dmg-acid2-reference.png"));
    try (CapturingIo io = new CapturingIo(IoFactory.none())) {
      MoldyGameBoy gb = new MoldyGameBoy(readRom("dmg.bin"), readRom("roms/dmg-acid2.gb"), io);
      io.eventLoop().run(new Acid2Ticker(gb, io));

      List<String> mismatches = new ArrayList<>();
      for (int y = 0; y < HEIGHT; y++) {
        for (int x = 0; x < WIDTH; x++) {
          int expected = shade(reference.getRGB(x, y) & 0xff);
          int actual = shade(io.frame.getRGB(x, y) & 0xff);
          if (expected != actual) {
            mismatches.add("(%d,%d): expected shade %d, got %d".formatted(x, y, expected, actual));
          }
        }
      }
      if (!mismatches.isEmpty()) {
        writeUndeclaredOutput(io.frame, "dmg-acid2-actual.png");
      }
      assertWithMessage("%s pixels differ from the reference", mismatches.size())
          .that(mismatches.subList(0, Math.min(10, mismatches.size())))
          .isEmpty();
    }
  }

  /** Maps a gray level (0x00, 0x55, 0xaa, 0xff) to a DMG shade index (3 = black ... 0 = white). */
  private static int shade(int gray) {
    return 3 - (gray >> 6);
  }

  /**
   * Runs until dmg-acid2 executes its LD B,B breakpoint (it has finished setting up), then two
   * more frames, so the last presented frame was drawn entirely after setup.
   */
  private static final class Acid2Ticker implements Ticker {

    private final MoldyGameBoy gb;
    private final CapturingIo io;
    private int breakpointFrame = -1;

    Acid2Ticker(MoldyGameBoy gb, CapturingIo io) {
      this.gb = gb;
      this.io = io;
    }

    @Override
    public void tick() {
      gb.tick();
      if (breakpointFrame < 0 && gb.cpu().registers().ir().get() == (byte) 0x40) {
        breakpointFrame = io.framesPresented;
      }
    }

    @Override
    public boolean isDone() {
      return breakpointFrame >= 0 && io.framesPresented >= breakpointFrame + 2;
    }
  }

  /** Delegates to another {@link Io}, but records every pixel written to the screen. */
  private static final class CapturingIo implements Io {

    private final Io delegate;
    private final BufferedImage frame =
        new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
    private int framesPresented = 0;
    private final Video video =
        new Video() {
          @Override
          public void writeVideoPixel(int x, int y, Color color) {
            frame.setRGB(
                x,
                y,
                (Byte.toUnsignedInt(color.r()) << 16)
                    | (Byte.toUnsignedInt(color.g()) << 8)
                    | Byte.toUnsignedInt(color.b()));
          }

          @Override
          public void setError(Throwable error) {
            throw new AssertionError(error);
          }

          @Override
          public void present() {
            framesPresented++;
          }

          @Override
          public void close() {}
        };

    CapturingIo(Io delegate) {
      this.delegate = delegate;
    }

    @Override
    public Audio audio() {
      return delegate.audio();
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
      delegate.close();
    }
  }

  private static void writeUndeclaredOutput(BufferedImage image, String name) throws IOException {
    String dir = System.getenv("TEST_UNDECLARED_OUTPUTS_DIR");
    if (dir != null) {
      ImageIO.write(image, "png", Path.of(dir, name).toFile());
    }
  }

  private static ByteBuffer readRom(String path) throws IOException {
    return ByteBuffer.wrap(Resources.toByteArray(Resources.getResource(path)));
  }
}

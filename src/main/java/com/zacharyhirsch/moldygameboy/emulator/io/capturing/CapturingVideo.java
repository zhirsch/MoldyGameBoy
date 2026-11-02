package com.zacharyhirsch.moldygameboy.emulator.io.capturing;

import com.zacharyhirsch.moldygameboy.emulator.io.Color;
import com.zacharyhirsch.moldygameboy.emulator.io.Video;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class CapturingVideo implements Video {

  private static final int WIDTH = 160;
  private static final int HEIGHT = 144;

  private final Video delegate;
  private final BufferedImage frame;

  private int count = 0;

  CapturingVideo(Video delegate) {
    this.delegate = delegate;
    this.frame = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
  }

  @Override
  public void writeVideoPixel(int x, int y, Color color) {
    delegate.writeVideoPixel(x, y, color);
    frame.setRGB(x, y, color.rgb());
  }

  @Override
  public void setError(Throwable error) {
    delegate.setError(error);
  }

  @Override
  public void present() {
    delegate.present();
    count++;
  }

  @Override
  public void close() throws Exception {
    delegate.close();
  }

  public int count() {
    return count;
  }

  public BufferedImage frame() {
    return frame;
  }

  public List<String> diff(BufferedImage ref) throws IOException {
    List<String> mismatches = new ArrayList<>();
    for (int y = 0; y < HEIGHT; y++) {
      for (int x = 0; x < WIDTH; x++) {
        int expected = shade(ref.getRGB(x, y) & 0xff);
        int actual = shade(frame.getRGB(x, y) & 0xff);
        if (expected != actual) {
          mismatches.add("(%d,%d): expected shade %d, got %d".formatted(x, y, expected, actual));
        }
      }
    }
    return mismatches;
  }

  /** Compares the frame's exact colors with a reference image. */
  public List<String> diffColors(BufferedImage ref) {
    List<String> mismatches = new ArrayList<>();
    for (int y = 0; y < HEIGHT; y++) {
      for (int x = 0; x < WIDTH; x++) {
        int expected = ref.getRGB(x, y) & 0xffffff;
        int actual = frame.getRGB(x, y) & 0xffffff;
        if (expected != actual) {
          mismatches.add("(%d,%d): expected %06x, got %06x".formatted(x, y, expected, actual));
        }
      }
    }
    return mismatches;
  }

  /** Maps a gray level (0x00, 0x55, 0xaa, 0xff) to a DMG shade index (3 = black ... 0 = white). */
  private static int shade(int gray) {
    return 3 - (gray >> 6);
  }
}

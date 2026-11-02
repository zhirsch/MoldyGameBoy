package com.zacharyhirsch.moldygameboy.emulator;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.io.Resources;
import com.zacharyhirsch.moldygameboy.emulator.io.Io;
import com.zacharyhirsch.moldygameboy.emulator.io.IoFactory;
import com.zacharyhirsch.moldygameboy.emulator.io.Ticker;
import com.zacharyhirsch.moldygameboy.emulator.io.capturing.CapturingIo;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

final class Acid2Test {

  @Test
  void testDmgAcid2() throws Exception {
    BufferedImage ref = ImageIO.read(Resources.getResource("roms/dmg-acid2-reference.png"));
    try (CapturingIo io = new CapturingIo(IoFactory.none())) {
      MoldyGameBoy gb = new MoldyGameBoy(readRom("dmg.bin"), readRom("roms/dmg-acid2.gb"), io);
      io.eventLoop().run(new Acid2Ticker(gb, io));
      List<String> mismatches = io.video().diff(ref);
      if (!mismatches.isEmpty()) {
        writeUndeclaredOutput(io.video().frame(), "dmg-acid2-actual.png");
      }
      assertThat(mismatches).isEmpty();
    }
  }

  @Test
  void testCgbAcid2() throws Exception {
    BufferedImage ref = ImageIO.read(Resources.getResource("roms/cgb-acid2-reference.png"));
    try (CapturingIo io = new CapturingIo(IoFactory.none())) {
      MoldyGameBoy gb = new MoldyGameBoy(readRom("cgb.bin"), readRom("roms/cgb-acid2.gbc"), io);
      io.eventLoop().run(new Acid2Ticker(gb, io));
      List<String> mismatches = io.video().diffColors(ref);
      if (!mismatches.isEmpty()) {
        writeUndeclaredOutput(io.video().frame(), "cgb-acid2-actual.png");
      }
      assertThat(mismatches).isEmpty();
    }
  }

  /**
   * Runs until the test executes its LD B,B breakpoint (it has finished setting up), then two more
   * frames, so the last presented frame was drawn entirely after setup. LD B,B in the boot ROM
   * (the CGB's has one) doesn't count, so breakpoints only count once execution reaches $0100.
   */
  private static final class Acid2Ticker implements Ticker {

    private final MoldyGameBoy gb;
    private final CapturingIo io;

    private boolean booted = false;
    private int breakpointFrame = -1;

    Acid2Ticker(MoldyGameBoy gb, CapturingIo io) {
      this.gb = gb;
      this.io = io;
    }

    @Override
    public void tick() {
      gb.tick();
      booted |= gb.cpu().registers().pc().get() == 0x0100;
      if (booted && breakpointFrame < 0 && gb.cpu().registers().ir().get() == (byte) 0x40) {
        breakpointFrame = io.video().count();
      }
    }

    @Override
    public boolean isDone() {
      return breakpointFrame >= 0 && io.video().count() >= breakpointFrame + 2;
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

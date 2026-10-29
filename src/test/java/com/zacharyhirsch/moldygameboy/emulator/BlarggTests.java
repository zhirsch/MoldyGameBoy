package com.zacharyhirsch.moldygameboy.emulator;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.io.Resources;
import com.zacharyhirsch.moldygameboy.emulator.io.Io;
import com.zacharyhirsch.moldygameboy.emulator.io.IoFactory;
import com.zacharyhirsch.moldygameboy.emulator.io.Ticker;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

final class BlarggTests {

  @Test
  void testCpuInstrs() throws Exception {
    doTest("blargg-test-suite/cpu_instrs/cpu_instrs.gb");
  }

  @Test
  void testInstrTiming() throws Exception {
    doTest("blargg-test-suite/instr_timing/instr_timing.gb");
  }

  @Test
  void testMemTiming() throws Exception {
    doTest("blargg-test-suite/mem_timing/mem_timing.gb");
  }

  @Test
  @Disabled("Doesn't pass on DMG")
  void testInterruptTime() throws Exception {
    doTest("blargg-test-suite/interrupt_time/interrupt_time.gb");
  }

  private static void doTest(String path) throws Exception {
    try (Io io = Boolean.getBoolean("moldy.gui") ? IoFactory.sdl() : IoFactory.none()) {
      MoldyGameBoy gb = new MoldyGameBoy(readRom("dmg.bin"), readRom(path), io);
      BlarggTicker ticker = new BlarggTicker(gb);
      io.eventLoop().run(ticker);

      String output = ticker.serial().toString();
      assertThat(output).contains("Passed");
      assertThat(output).doesNotContain("Failed");
    }
  }

  private record BlarggTicker(MoldyGameBoy gb, StringBuilder serial) implements Ticker {

    BlarggTicker(MoldyGameBoy gb) {
      this(gb, new StringBuilder());
      gb.setSerialOutput(serial::append);
    }

    @Override
    public void tick() {
      gb().tick();
    }

    @Override
    public boolean isDone() {
      String output = serial().toString();
      int verdict = Math.max(output.lastIndexOf("Passed"), output.lastIndexOf("Failed"));
      return verdict >= 0 && output.indexOf('\n', verdict) >= 0;
    }
  }

  private static ByteBuffer readRom(String path) throws IOException {
    return ByteBuffer.wrap(Resources.toByteArray(Resources.getResource(path)));
  }
}

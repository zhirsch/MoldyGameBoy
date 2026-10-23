package com.zacharyhirsch.moldygameboy.emulator;

import com.google.common.io.Resources;
import com.zacharyhirsch.moldygameboy.emulator.io.Io;
import com.zacharyhirsch.moldygameboy.emulator.io.IoFactory;
import java.io.IOException;
import java.nio.ByteBuffer;
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
    doTest("blargg-test-suite/mem_timing-2/mem_timing.gb");
  }

  @Test
  void testInterruptTime() throws Exception {
    doTest("blargg-test-suite/interrupt_time/interrupt_time.gb");
  }

  private static void doTest(String path) throws Exception {
    try (Io io = IoFactory.sdl()) {
      MoldyGameBoy gb = new MoldyGameBoy(readRom("dmg.bin"), readRom(path), io);
      try {
        io.eventLoop().run(gb::tick);
      } catch (Exception e) {
        io.video().setError(e);
        io.eventLoop().run(() -> {});
        throw e;
      }
    }
  }

  private static ByteBuffer readRom(String path) throws IOException {
    return ByteBuffer.wrap(Resources.toByteArray(Resources.getResource(path)));
  }
}

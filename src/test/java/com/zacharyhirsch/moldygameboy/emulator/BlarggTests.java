package com.zacharyhirsch.moldygameboy.emulator;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.io.Resources;
import com.zacharyhirsch.moldygameboy.emulator.io.Io;
import com.zacharyhirsch.moldygameboy.emulator.io.IoFactory;
import com.zacharyhirsch.moldygameboy.emulator.io.Ticker;
import com.zacharyhirsch.moldygameboy.emulator.serial.SerialDevice;
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

      String output = ticker.serial();
      assertThat(output).contains("Passed");
      assertThat(output).doesNotContain("Failed");
    }
  }

  private record BlarggTicker(MoldyGameBoy gb, SerialReceiver receiver) implements Ticker {

    BlarggTicker(MoldyGameBoy gb) {
      this(gb, new SerialReceiver());
      gb.connectSerial(receiver);
    }

    String serial() {
      return receiver.output().toString();
    }

    @Override
    public void tick() {
      gb.tick();
    }

    @Override
    public boolean isDone() {
      String output = serial();
      int verdict = Math.max(output.lastIndexOf("Passed"), output.lastIndexOf("Failed"));
      return verdict >= 0 && output.indexOf('\n', verdict) >= 0;
    }
  }

  /**
   * A link partner that collects the bytes Blargg's tests print by sending them over the serial
   * port, and sends back $FF like an unconnected port.
   */
  private static final class SerialReceiver implements SerialDevice {

    private final StringBuilder output = new StringBuilder();

    private int bits = 0;
    private int value = 0;

    StringBuilder output() {
      return output;
    }

    @Override
    public int exchange(int bit) {
      value = (value << 1) | bit;
      if (++bits == 8) {
        output.append((char) value);
        bits = 0;
        value = 0;
      }
      return 1;
    }
  }

  private static ByteBuffer readRom(String path) throws IOException {
    return ByteBuffer.wrap(Resources.toByteArray(Resources.getResource(path)));
  }
}

package com.zacharyhirsch.moldygameboy.emulator;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

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

  @Test
  void testDmgSound() throws Exception {
    doMemoryTest("dmg.bin", "blargg-test-suite/dmg_sound/dmg_sound.gb");
  }

  @Test
  void testCgbSound() throws Exception {
    doMemoryTest("cgb.bin", "blargg-test-suite/cgb_sound/cgb_sound.gb");
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

  /** Runs a test that reports through cartridge RAM rather than the serial port. */
  private static void doMemoryTest(String boot, String path) throws Exception {
    try (Io io = Boolean.getBoolean("moldy.gui") ? IoFactory.sdl() : IoFactory.none()) {
      MoldyGameBoy gb = new MoldyGameBoy(readRom(boot), readRom(path), io);
      MemoryTicker ticker = new MemoryTicker(gb);
      io.eventLoop().run(ticker);

      assertWithMessage(ticker.output()).that(ticker.status()).isEqualTo(0);
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
   * Waits for a test's result in cartridge RAM. Once $A001-$A003 hold the signature $DE $B0 $61,
   * $A000 is the status ($80 while running, then the result code, 0 for success) and the text
   * output is a zero-terminated string from $A004.
   */
  private static final class MemoryTicker implements Ticker {

    private static final int CHECK_INTERVAL = 1 << 16;

    private final MoldyGameBoy gb;

    private int ticks = 0;
    private boolean done = false;

    MemoryTicker(MoldyGameBoy gb) {
      this.gb = gb;
    }

    int status() {
      return read(0xa000);
    }

    String output() {
      StringBuilder output = new StringBuilder();
      for (int address = 0xa004; address < 0xc000 && read(address) != 0; address++) {
        output.append((char) read(address));
      }
      return output.toString();
    }

    @Override
    public void tick() {
      gb.tick();
      if (++ticks % CHECK_INTERVAL == 0) {
        boolean signed = read(0xa001) == 0xde && read(0xa002) == 0xb0 && read(0xa003) == 0x61;
        done = signed && status() != 0x80;
      }
    }

    @Override
    public boolean isDone() {
      return done;
    }

    private int read(int address) {
      return Byte.toUnsignedInt(gb.read((short) address));
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

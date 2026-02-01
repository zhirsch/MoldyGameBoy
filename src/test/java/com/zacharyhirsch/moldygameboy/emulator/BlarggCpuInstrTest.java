package com.zacharyhirsch.moldygameboy.emulator;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.io.Resources;
import com.zacharyhirsch.moldygameboy.emulator.bus.AddressBus;
import com.zacharyhirsch.moldygameboy.emulator.bus.DataBus;
import com.zacharyhirsch.moldygameboy.emulator.cpu.Cpu;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import com.zacharyhirsch.moldygameboy.emulator.memory.MemOperation;
import com.zacharyhirsch.moldygameboy.emulator.memory.Memory;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

final class BlarggCpuInstrTest {

  enum BlarggTest {
    SPECIAL("cpu_instrs/individual/01-special.gb", 1_256_633),
    ;

    private final String path;
    private final int cycles;

    BlarggTest(String path, int cycles) {
      this.path = path;
      this.cycles = cycles;
    }

    public String getPath() {
      return path;
    }

    public int getCycles() {
      return cycles;
    }
  }

  @ParameterizedTest
  @EnumSource(BlarggTest.class)
  void testBlarggTest(BlarggTest test) throws Exception {
    URL romResource = Resources.getResource(test.getPath());
    Exchanger<MemOperation> memSema = new Exchanger<>();
    Exchanger<Byte> cpuSema = new Exchanger<>();

    ByteBuffer rom = ByteBuffer.wrap(Resources.toByteArray(romResource));

    AddressBus addressBus = new AddressBus();
    DataBus dataBus = new DataBus();
    Memory memory = new Memory(addressBus, dataBus, rom, rom);
    Registers registers = new Registers();
    registers.ir().set(rom.get(0x0100));
    registers.pc().set((short) 0x0101);
    registers.sp().set((short) 0xfffe);
    registers.a().set((byte) 0x01);
    registers.f().set((byte) 0xb0);
    registers.b().set((byte) 0x00);
    registers.c().set((byte) 0x13);
    registers.d().set((byte) 0x00);
    registers.e().set((byte) 0xd8);
    registers.h().set((byte) 0x01);
    registers.l().set((byte) 0x4d);
    Cpu cpu = new Cpu(memSema, cpuSema, registers, memory);

    Thread cpuThread = Thread.startVirtualThread(() -> cpu.run(test.getCycles() + 10));
    MyUncaughtExceptionHandler ueh = new MyUncaughtExceptionHandler();
    cpuThread.setUncaughtExceptionHandler(ueh);
    while (cpuThread.isAlive()) {
      MemOperation mem;
      try {
        mem = memSema.exchange(null, 10, TimeUnit.MILLISECONDS);
      } catch (TimeoutException e) {
        mem = null;
      }
      if (mem != null) {
        mem.execute(memory, addressBus, dataBus);
        cpuSema.exchange(dataBus.get());
      }
    }

    assertThat(ueh.getException()).isNull();
  }

  private static class MyUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    private Throwable exception = null;

    @Override
    public void uncaughtException(Thread t, Throwable e) {
      this.exception = e;
    }

    public Throwable getException() {
      return exception;
    }
  }
}

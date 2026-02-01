package com.zacharyhirsch.moldygameboy.emulator;

import com.google.common.io.Resources;
import com.zacharyhirsch.moldygameboy.emulator.bus.AddressBus;
import com.zacharyhirsch.moldygameboy.emulator.bus.DataBus;
import com.zacharyhirsch.moldygameboy.emulator.cpu.Cpu;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import com.zacharyhirsch.moldygameboy.emulator.memory.MemOperation;
import com.zacharyhirsch.moldygameboy.emulator.memory.Memory;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Exchanger;

final class MoldyGameBoy {

  private static final String BOOT_ROM_PATH = "cgb.bin";

  static void main(String[] args) throws Exception {
    Exchanger<MemOperation> memSema = new Exchanger<>();
    Exchanger<Byte> cpuSema = new Exchanger<>();

    ByteBuffer boot = ByteBuffer.wrap(Resources.toByteArray(Resources.getResource(BOOT_ROM_PATH)));
    ByteBuffer rom = readRom(args[0]);

    AddressBus addressBus = new AddressBus();
    DataBus dataBus = new DataBus();
    Memory memory = new Memory(addressBus, dataBus, boot, rom);
    Cpu cpu = new Cpu(memSema, cpuSema, new Registers(), memory);

    Thread cpuThread =
        Thread.startVirtualThread(
            () -> {
              while (true) {
                cpu.run(1);
              }
            });

    while (cpuThread.isAlive()) {
      MemOperation mem = memSema.exchange(null);
      mem.execute(memory, addressBus, dataBus);
      cpuSema.exchange(dataBus.get());
    }

    cpuThread.join();
  }

  private static ByteBuffer readRom(String path) {
    try (FileInputStream input = new FileInputStream(path)) {
      return ByteBuffer.wrap(input.readAllBytes());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}

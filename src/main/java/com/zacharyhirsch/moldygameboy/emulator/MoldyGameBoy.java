package com.zacharyhirsch.moldygameboy.emulator;

import com.zacharyhirsch.moldygameboy.emulator.cpu.Cpu;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import com.zacharyhirsch.moldygameboy.emulator.memory.IORegisters;
import com.zacharyhirsch.moldygameboy.emulator.memory.MemoryMap;
import com.zacharyhirsch.moldygameboy.emulator.timer.Divider;
import com.zacharyhirsch.moldygameboy.emulator.timer.Timer;
import java.io.Writer;
import java.nio.ByteBuffer;

final class MoldyGameBoy {

  public static void run(
      long cycles,
      ByteBuffer boot,
      ByteBuffer rom,
      Registers registers,
      IORegisters ioRegisters,
      Writer writer) {
    MemoryMap memory = new MemoryMap(boot, rom, ioRegisters);
    Divider divider = new Divider(ioRegisters);
    Timer timer = new Timer(ioRegisters);
    Cpu cpu = new Cpu(registers, ioRegisters, memory, writer);

    for (long i = 0; i < cycles; i++) {
      divider.tick();
      timer.tick();
      cpu.tick();
    }
  }
}

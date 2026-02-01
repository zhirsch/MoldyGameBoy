package com.zacharyhirsch.moldygameboy.emulator.cpu;

import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import com.zacharyhirsch.moldygameboy.emulator.memory.MemOperation;
import com.zacharyhirsch.moldygameboy.emulator.memory.Memory;
import java.util.concurrent.Exchanger;

public final class Cpu {

  private final Registers registers;
  private final Memory memory;
  private final CpuDecoder decoder;

  public Cpu(
      Exchanger<MemOperation> memSema,
      Exchanger<Byte> cpuSema,
      Registers registers,
      Memory memory) {
    this.registers = registers;
    this.memory = memory;
    this.decoder = new CpuDecoder(memSema, cpuSema, registers);
  }

  public void run(int cycles) {
    for (int i = 0; i < cycles; i++) {
      int pc = Short.toUnsignedInt(registers.pc().get());
      System.out.printf(
          "A:%02x F:%02x B:%02x C:%02x D:%02x E:%02x H:%02x L:%02x SP:%04x PC:%04x PCMEM:%02x,%02x,%02x,%02x%n",
          registers.a().get(),
          registers.f().get(),
          registers.b().get(),
          registers.c().get(),
          registers.d().get(),
          registers.e().get(),
          registers.h().get(),
          registers.l().get(),
          registers.sp().get(),
          pc - 1,
          memoryAt((short) (pc - 1)),
          memoryAt((short) pc),
          memoryAt((short) (pc + 1)),
          memoryAt((short) (pc + 2)));
      decoder.decode();
    }
  }

  private byte memoryAt(short address) {
    return memory.read(Short.toUnsignedInt(address));
  }
}

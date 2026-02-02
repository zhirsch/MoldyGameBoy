package com.zacharyhirsch.moldygameboy.emulator.cpu;

import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import com.zacharyhirsch.moldygameboy.emulator.memory.MemOperation;
import com.zacharyhirsch.moldygameboy.emulator.memory.MemRead;
import com.zacharyhirsch.moldygameboy.emulator.memory.MemWrite;
import com.zacharyhirsch.moldygameboy.emulator.memory.Memory;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.function.Function;

public final class Cpu {

  private static final byte INTERRUPT_VBLANK = 0b0000_0001;
  private static final byte INTERRUPT_LCD = 0b0000_0010;
  private static final byte INTERRUPT_TIMER = 0b0000_0100;
  private static final byte INTERRUPT_SERIAL = 0b0000_1000;
  private static final byte INTERRUPT_JOYPAD = 0b0001_0000;
  private static final Map<Byte, Short> INTERRUPT_VECTORS =
      Map.ofEntries(
          Map.entry(INTERRUPT_VBLANK, (short) 0x0040),
          Map.entry(INTERRUPT_LCD, (short) 0x0048),
          Map.entry(INTERRUPT_TIMER, (short) 0x0050),
          Map.entry(INTERRUPT_SERIAL, (short) 0x0058),
          Map.entry(INTERRUPT_JOYPAD, (short) 0x0060));

  private final Registers registers;
  private final Memory memory;
  private final Function<MemOperation, Byte> memOperator;
  private final CpuDecoder decoder;
  private final Writer writer;

  public Cpu(
      Registers registers, Memory memory, Function<MemOperation, Byte> memOperator, Writer writer) {
    this.registers = registers;
    this.memory = memory;
    this.memOperator = memOperator;
    this.decoder = new CpuDecoder(registers, memOperator);
    this.writer = writer;
  }

  public void tick() {
    logState();
    if (isInterruptRequested(INTERRUPT_VBLANK)) {
      doInterrupt(INTERRUPT_VBLANK);
    } else if (isInterruptRequested(INTERRUPT_LCD)) {
      doInterrupt(INTERRUPT_LCD);
    } else if (isInterruptRequested(INTERRUPT_TIMER)) {
      doInterrupt(INTERRUPT_TIMER);
    } else if (isInterruptRequested(INTERRUPT_SERIAL)) {
      doInterrupt(INTERRUPT_SERIAL);
    } else if (isInterruptRequested(INTERRUPT_JOYPAD)) {
      doInterrupt(INTERRUPT_JOYPAD);
    }
    decoder.decode();
  }

  private boolean isInterruptRequested(int interrupt) {
    return decoder.getIme()
        && (memory.getIe() & interrupt) != 0
        && (memory.getIf() & interrupt) != 0;
  }

  private void doInterrupt(byte interrupt) {
    var _ = read(registers.pc().getAndDecrement());
    var _ = read(registers.sp().getAndDecrement());
    write(registers.sp().getAndDecrement(), registers.pc().hi().get());
    write(registers.sp().get(), registers.pc().lo().get());
    decoder.setIme(false);
    memory.setIf((byte) (memory.getIf() & ~interrupt));
    registers.pc().set(INTERRUPT_VECTORS.get(interrupt));
    registers.ir().set(read(registers.pc().getAndIncrement()));
  }

  private byte read(short address) {
    return memOperator.apply(new MemRead(address));
  }

  private void write(short address, byte data) {
    var _ = memOperator.apply(new MemWrite(address, data));
  }

  private void logState() {
    if (writer == null) {
      return;
    }
    try {
      writer.write(getStateString() + "\n");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String getStateString() {
    int pc = Short.toUnsignedInt(registers.pc().get()) - INTERRUPT_VBLANK;
    return "A:%02x F:%02x B:%02x C:%02x D:%02x E:%02x H:%02x L:%02x SP:%04x PC:%04x PCMEM:%02x,%02x,%02x,%02x"
        .formatted(
            registers.a().get(),
            registers.f().get(),
            registers.b().get(),
            registers.c().get(),
            registers.d().get(),
            registers.e().get(),
            registers.h().get(),
            registers.l().get(),
            registers.sp().get(),
            pc,
            memory.read((short) (pc + 0)),
            memory.read((short) (pc + INTERRUPT_VBLANK)),
            memory.read((short) (pc + INTERRUPT_LCD)),
            memory.read((short) (pc + 3)));
  }
}

package com.zacharyhirsch.moldygameboy.emulator.memory;

public record MemRead(short address) implements MemOperation {

  @Override
  public byte execute(Memory memory) {
    return memory.read(address);
  }
}

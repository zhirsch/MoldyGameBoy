package com.zacharyhirsch.moldygameboy.emulator.memory;

public record MemWrite(short address, byte data) implements MemOperation {

  @Override
  public byte execute(Memory memory) {
    memory.write(address, data);
    return data;
  }
}

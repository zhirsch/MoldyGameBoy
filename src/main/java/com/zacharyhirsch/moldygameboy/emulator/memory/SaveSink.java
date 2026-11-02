package com.zacharyhirsch.moldygameboy.emulator.memory;

import java.io.IOException;

/** Where a cartridge's battery-backed state is written when it's saved. */
@FunctionalInterface
public interface SaveSink {

  void write(byte[] save) throws IOException;
}

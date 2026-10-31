package com.zacharyhirsch.moldygameboy.emulator.ppu;

import com.zacharyhirsch.moldygameboy.emulator.memory.IoRegisters;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

final class OamScanner {

  private final IoRegisters ioregs;
  private final ByteBuffer oam;
  private final List<Sprite> selected;

  private int oamIndex = 0;
  private boolean secondDot = false;

  OamScanner(IoRegisters ioregs, ByteBuffer oam) {
    this.ioregs = ioregs;
    this.oam = oam;
    this.selected = new ArrayList<>();
  }

  List<Sprite> selected() {
    return selected;
  }

  void reset() {
    selected.clear();
    oamIndex = 0;
    secondDot = false;
  }

  void tick() {
    if (!secondDot) {
      secondDot = true;
      return;
    }
    secondDot = false;
    int ly = ioregs.ly.get();
    int height = ioregs.lcdc.bit(2) ? 16 : 8;
    int y = Byte.toUnsignedInt(oam.get(oamIndex * 4));
    int x = Byte.toUnsignedInt(oam.get(oamIndex * 4 + 1));
    if (y <= ly + 16 && ly + 16 < y + height && selected.size() < 10) {
      selected.add(new Sprite(oamIndex, y, x));
    }
    oamIndex++;
  }
}

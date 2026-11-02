package com.zacharyhirsch.moldygameboy.emulator.ppu;

/**
 * An object pixel. The palette is 0-1 (OBP0/OBP1) in DMG mode and 0-7 in CGB mode; the OAM index
 * decides which object wins where they overlap in CGB mode.
 */
record ObjPixel(int color, int palette, boolean behindBg, int oamIndex) {}

package com.zacharyhirsch.moldygameboy.emulator.ppu;

/**
 * A background or window pixel. The palette (0-7) and priority (BG-to-OBJ priority, attribute bit
 * 7) only apply in CGB mode.
 */
record BgPixel(int color, int palette, boolean priority) {}

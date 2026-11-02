package com.zacharyhirsch.moldygameboy.emulator.cpu;

import com.zacharyhirsch.moldygameboy.emulator.arch.IoRegister;

/**
 * The CGB's double speed mode, controlled through KEY1: writing bit 0 arms a switch, which the next
 * STOP carries out, and bit 7 reads whether double speed is on. In double speed the CPU, timer,
 * serial port and OAM DMA run twice as fast; the PPU, APU and HDMA don't.
 */
public final class SpeedSwitch {

  private boolean armed = false;
  private boolean doubleSpeed = false;

  public boolean isDoubleSpeed() {
    return doubleSpeed;
  }

  boolean isArmed() {
    return armed;
  }

  void toggle() {
    doubleSpeed = !doubleSpeed;
    armed = false;
  }

  public IoRegister key1() {
    return new IoRegister() {
      @Override
      public int get() {
        return (doubleSpeed ? 0x80 : 0x00) | (armed ? 0x01 : 0x00);
      }

      @Override
      public void set(int value) {
        armed = (value & 0x01) != 0;
      }

      @Override
      public String toString() {
        return "key1 @ ff4d = %02x".formatted(get());
      }
    };
  }
}

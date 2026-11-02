package com.zacharyhirsch.moldygameboy.emulator.memory;

import static com.google.common.truth.Truth.assertThat;

import java.nio.ByteBuffer;
import org.junit.jupiter.api.Test;

final class Mbc3MapperTest {

  private static final int DOTS_PER_SECOND = 1 << 22;

  private final Mbc3Mapper mapper =
      new Mbc3Mapper(rom(), ByteBuffer.allocate(4 * 0x2000), new RealTimeClock());

  @Test
  void romBankIsSevenBitsWithZeroMeaningOne() {
    write(0x2000, 0x05);
    assertThat(readRom(0x4000)).isEqualTo(0x05);
    write(0x2000, 0x00);
    assertThat(readRom(0x4000)).isEqualTo(0x01);
    write(0x2000, 0x85);
    assertThat(readRom(0x4000)).isEqualTo(0x05);
  }

  @Test
  void ramIsBankedAndOnlyAccessibleWhenEnabled() {
    write(0x4000, 0x02);
    writeRam(0xa123, 0x22);
    assertThat(readRam(0xa123)).isEqualTo(0xff);

    write(0x0000, 0x0a);
    writeRam(0xa123, 0x22);
    write(0x4000, 0x03);
    writeRam(0xa123, 0x33);

    assertThat(readRam(0xa123)).isEqualTo(0x33);
    write(0x4000, 0x02);
    assertThat(readRam(0xa123)).isEqualTo(0x22);
  }

  @Test
  void clockReadsTheLatchedTime() {
    write(0x0000, 0x0a);
    tick(3 * DOTS_PER_SECOND);

    assertThat(readClock(RealTimeClock.SECONDS)).isEqualTo(0);
    latch();
    assertThat(readClock(RealTimeClock.SECONDS)).isEqualTo(3);
    tick(DOTS_PER_SECOND);
    assertThat(readClock(RealTimeClock.SECONDS)).isEqualTo(3);
  }

  @Test
  void latchingNeedsZeroThenOne() {
    write(0x0000, 0x0a);
    tick(DOTS_PER_SECOND);

    write(0x6000, 0x01);
    assertThat(readClock(RealTimeClock.SECONDS)).isEqualTo(0);
    latch();
    assertThat(readClock(RealTimeClock.SECONDS)).isEqualTo(1);
  }

  @Test
  void haltStopsTheClock() {
    write(0x0000, 0x0a);
    writeClock(RealTimeClock.DAYS_HIGH, 0x40);
    tick(5 * DOTS_PER_SECOND);

    latch();
    assertThat(readClock(RealTimeClock.SECONDS)).isEqualTo(0);
  }

  @Test
  void daysOverflowIntoTheCarryFlag() {
    write(0x0000, 0x0a);
    writeClock(RealTimeClock.SECONDS, 59);
    writeClock(RealTimeClock.MINUTES, 59);
    writeClock(RealTimeClock.HOURS, 23);
    writeClock(RealTimeClock.DAYS_LOW, 0xff);
    writeClock(RealTimeClock.DAYS_HIGH, 0x01);
    tick(DOTS_PER_SECOND);

    latch();
    assertThat(readClock(RealTimeClock.SECONDS)).isEqualTo(0);
    assertThat(readClock(RealTimeClock.MINUTES)).isEqualTo(0);
    assertThat(readClock(RealTimeClock.HOURS)).isEqualTo(0);
    assertThat(readClock(RealTimeClock.DAYS_LOW)).isEqualTo(0);
    assertThat(readClock(RealTimeClock.DAYS_HIGH)).isEqualTo(0x80);
  }

  @Test
  void counterWrittenPastItsLimitWrapsWithoutCarrying() {
    write(0x0000, 0x0a);
    writeClock(RealTimeClock.SECONDS, 62);
    tick(2 * DOTS_PER_SECOND);

    latch();
    assertThat(readClock(RealTimeClock.SECONDS)).isEqualTo(0);
    assertThat(readClock(RealTimeClock.MINUTES)).isEqualTo(0);
  }

  private void tick(int dots) {
    for (int i = 0; i < dots; i++) {
      mapper.tick();
    }
  }

  private void latch() {
    write(0x6000, 0x00);
    write(0x6000, 0x01);
  }

  private void write(int address, int value) {
    mapper.mapRom().write((short) address, (byte) value);
  }

  private int readRom(int address) {
    return Byte.toUnsignedInt(mapper.mapRom().read((short) address));
  }

  private void writeRam(int address, int value) {
    mapper.mapRam().write((short) address, (byte) value);
  }

  private int readRam(int address) {
    return Byte.toUnsignedInt(mapper.mapRam().read((short) address));
  }

  private void writeClock(int register, int value) {
    write(0x4000, register);
    writeRam(0xa000, value);
  }

  private int readClock(int register) {
    write(0x4000, register);
    return readRam(0xa000);
  }

  /** 128 ROM banks, each starting with its bank number. */
  private static ByteBuffer rom() {
    ByteBuffer rom = ByteBuffer.allocate(128 * 0x4000);
    for (int bank = 0; bank < 128; bank++) {
      rom.put(bank * 0x4000, (byte) bank);
    }
    return rom;
  }
}

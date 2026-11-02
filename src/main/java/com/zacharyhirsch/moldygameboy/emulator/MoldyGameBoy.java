package com.zacharyhirsch.moldygameboy.emulator;

import com.zacharyhirsch.moldygameboy.emulator.apu.Apu;
import com.zacharyhirsch.moldygameboy.emulator.arch.Irqs;
import com.zacharyhirsch.moldygameboy.emulator.arch.Model;
import com.zacharyhirsch.moldygameboy.emulator.cpu.Cpu;
import com.zacharyhirsch.moldygameboy.emulator.cpu.SpeedSwitch;
import com.zacharyhirsch.moldygameboy.emulator.io.Io;
import com.zacharyhirsch.moldygameboy.emulator.joypad.Joypad;
import com.zacharyhirsch.moldygameboy.emulator.memory.Hdma;
import com.zacharyhirsch.moldygameboy.emulator.memory.IoRegisters;
import com.zacharyhirsch.moldygameboy.emulator.memory.Mapper;
import com.zacharyhirsch.moldygameboy.emulator.memory.Memory;
import com.zacharyhirsch.moldygameboy.emulator.memory.VideoMemoryLock;
import com.zacharyhirsch.moldygameboy.emulator.ppu.PaletteRam;
import com.zacharyhirsch.moldygameboy.emulator.ppu.Ppu;
import com.zacharyhirsch.moldygameboy.emulator.serial.Serial;
import com.zacharyhirsch.moldygameboy.emulator.serial.SerialDevice;
import com.zacharyhirsch.moldygameboy.emulator.timer.Timer;
import java.nio.ByteBuffer;

final class MoldyGameBoy {


  private enum TCycle {
    T1,
    T2,
    T3,
    T4,
    ;

    TCycle next() {
      return switch (this) {
        case T1 -> T2;
        case T2 -> T3;
        case T3 -> T4;
        case T4 -> T1;
      };
    }
  }

  private final Memory memory;
  private final Hdma hdma;
  private final SpeedSwitch speedSwitch;
  private final Timer timer;
  private final Serial serial;
  private final Joypad joypad;
  private final Apu apu;
  private final IoRegisters ioregs;
  private final Cpu cpu;
  private final Ppu ppu;

  private TCycle tcycle = TCycle.T1;
  private boolean cpuStalled = false;
  private int dot = 0;

  /** The model is chosen by the boot ROM: the CGB's is 2304 bytes, the DMG's 256. */
  public MoldyGameBoy(ByteBuffer boot, ByteBuffer rom, Io io) {
    Model model = boot.limit() == 0x900 ? Model.CGB : Model.DMG;
    ByteBuffer vram = ByteBuffer.allocate(Memory.VRAM_SIZE);
    ByteBuffer oam = ByteBuffer.allocate(0x00a0);
    this.memory = new Memory(Mapper.create(rom), vram, oam);
    this.hdma = new Hdma(memory, this::isLcdOn);
    VideoMemoryLock videoMemoryLock = new VideoMemoryLock();
    Irqs irqs = new Irqs();
    this.timer = new Timer(irqs.timer());
    this.serial = new Serial(timer::counter, irqs.serial());
    this.joypad = new Joypad(io.joypads(), irqs.joypad());
    this.speedSwitch = new SpeedSwitch();
    this.apu = new Apu(timer::counter, speedSwitch::isDoubleSpeed, io.audio());
    PaletteRam bgPalettes = new PaletteRam("bc", 0xff68);
    PaletteRam objPalettes = new PaletteRam("oc", 0xff6a);
    this.ioregs =
        new IoRegisters(
            model,
            joypad.joyp(),
            serial.sb(),
            serial.sc(),
            timer.div(),
            timer.tima(),
            timer.tma(),
            timer.tac(),
            irqs.if_(),
            apu.nr10(),
            apu.nr11(),
            apu.nr12(),
            apu.nr13(),
            apu.nr14(),
            apu.nr21(),
            apu.nr22(),
            apu.nr23(),
            apu.nr24(),
            apu.nr30(),
            apu.nr31(),
            apu.nr32(),
            apu.nr33(),
            apu.nr34(),
            apu.nr41(),
            apu.nr42(),
            apu.nr43(),
            apu.nr44(),
            apu.nr50(),
            apu.nr51(),
            apu.nr52(),
            apu.wave0(),
            apu.wave1(),
            apu.wave2(),
            apu.wave3(),
            apu.wave4(),
            apu.wave5(),
            apu.wave6(),
            apu.wave7(),
            apu.wave8(),
            apu.wave9(),
            apu.wavea(),
            apu.waveb(),
            apu.wavec(),
            apu.waved(),
            apu.wavee(),
            apu.wavef(),
            speedSwitch.key1(),
            memory.vbk(),
            hdma.hdma1(),
            hdma.hdma2(),
            hdma.hdma3(),
            hdma.hdma4(),
            hdma.hdma5(),
            bgPalettes.spec(),
            bgPalettes.data(),
            objPalettes.spec(),
            objPalettes.data(),
            memory.svbk(),
            apu.pcm12(),
            apu.pcm34(),
            memory);
    this.cpu = new Cpu(boot, memory, ioregs, videoMemoryLock, speedSwitch);
    this.ppu =
        new Ppu(
            ioregs,
            vram,
            oam,
            videoMemoryLock,
            io.video(),
            irqs.vblank(),
            irqs.lcd(),
            hdma::onHBlank,
            bgPalettes,
            objPalettes);
  }

  /**
   * Advances by one dot (4194304 Hz), the rate the PPU, APU and HDMA always run at. The CPU side
   * (CPU, OAM DMA, timer, serial port and joypad) runs one T-cycle per dot, or two in double speed.
   */
  public void tick() {
    ppu.tick();
    tickCpuSide();
    if (speedSwitch.isDoubleSpeed()) {
      tickCpuSide();
    }
    apu.tick();
    if (++dot == 4) {
      dot = 0;
      hdma.tick();
    }
  }

  private void tickCpuSide() {
    switch (tcycle) {
      case T1 -> {
        // HDMA stops the CPU for whole M-cycles.
        cpuStalled = hdma.isTransferring();
        ioregs.dma.tickT1();
        if (!cpuStalled) {
          cpu.tickT1();
        }
        timer.tickT1();
        serial.tick();
      }
      case T2 -> {
        ioregs.dma.tickT2();
        if (!cpuStalled) {
          cpu.tickT2();
        }
        timer.tickT2();
        serial.tick();
      }
      case T3 -> {
        ioregs.dma.tickT3();
        if (!cpuStalled) {
          cpu.tickT3();
        }
        timer.tickT3();
        serial.tick();
      }
      case T4 -> {
        ioregs.dma.tickT4();
        if (!cpuStalled) {
          cpu.tickT4();
        }
        timer.tickT4();
        serial.tick();
        joypad.tick();
      }
    }
    tcycle = tcycle.next();
  }

  private boolean isLcdOn() {
    return ioregs.lcdc.bit(7);
  }

  public Cpu cpu() {
    return cpu;
  }

  /** Reads memory as the cartridge and video buses see it, bypassing the CPU. */
  byte read(short address) {
    return memory.read(address);
  }

  /** Plugs a device into the link port. */
  void connectSerial(SerialDevice device) {
    serial.connect(device);
  }
}

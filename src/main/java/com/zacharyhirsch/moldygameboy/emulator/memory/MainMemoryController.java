package com.zacharyhirsch.moldygameboy.emulator.memory;

public final class MainMemoryController {

  private final Rom rom;
  private final Vram vram;
  private final Ram ram;
  private final Wram wram;
  private final Oam oam;
  private final OamDmaRegister dma;

  public MainMemoryController(Rom rom, Vram vram, OamDmaRegister dma) {
    this.rom = rom;
    this.vram = vram;
    this.ram = new Ram();
    this.wram = new Wram();
    this.oam = new Oam();
    this.dma = dma;
  }

  public byte read(short address) {
    return getMemoryRegion(address).read(address);
  }

  public void write(short address, byte data) {
    getMemoryRegion(address).write(address, data);
  }

  public MemoryRegion getMemoryRegion(short address) {
    int addr = Short.toUnsignedInt(address);
    if (dma.isActive()) {
      return new OpenBus();
    }
    if (0x0000 <= addr && addr <= 0x7fff) {
      return rom;
    }
    if (0x8000 <= addr && addr <= 0x9fff) {
      return vram;
    }
    if (0xa000 <= addr && addr <= 0xbfff) {
      return ram;
    }
    if (0xc000 <= addr && addr <= 0xdfff) {
      return wram;
    }
    if (0xe000 <= addr && addr <= 0xfdff) {
      return new EchoRam(wram);
    }
    if (0xfe00 <= addr && addr <= 0xfe9f) {
      return oam;
    }
    if (0xfea0 <= addr && addr <= 0xffff) {
      return new OpenBus();
    }
    throw new IllegalArgumentException("%04x".formatted(address));
  }

  private static class OpenBus implements MemoryRegion {

    @Override
    public byte read(short address) {
      return (byte) 0xff;
    }

    @Override
    public void write(short address, byte value) {}
  }
}

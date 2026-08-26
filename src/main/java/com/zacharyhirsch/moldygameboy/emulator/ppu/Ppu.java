package com.zacharyhirsch.moldygameboy.emulator.ppu;

import com.zacharyhirsch.moldygameboy.emulator.arch.InterruptRequestLine;
import com.zacharyhirsch.moldygameboy.emulator.io.Color;
import com.zacharyhirsch.moldygameboy.emulator.io.Video;
import com.zacharyhirsch.moldygameboy.emulator.memory.Memory;
import java.util.ArrayDeque;
import java.util.Queue;

public final class Ppu {

  private static final Color[] PALETTE = {
    new Color((byte) 0xff, (byte) 0xff, (byte) 0xff), // white
    new Color((byte) 0xaa, (byte) 0xaa, (byte) 0xaa), // light gray
    new Color((byte) 0x55, (byte) 0x55, (byte) 0x55), // dark gray
    new Color((byte) 0x00, (byte) 0x00, (byte) 0x00), // black
  };

  private enum Mode {
    MODE_0_HBLANK((byte) 0),
    MODE_1_VBLANK((byte) 1),
    MODE_2_OAM_SCAN((byte) 2),
    MODE_3_DRAWING((byte) 3),
    ;

    private final byte value;

    Mode(byte value) {
      this.value = value;
    }

    public byte getValue() {
      return value;
    }
  }

  enum State {
    GET_TILE_ID,
    GET_TILE_DATA_LO,
    GET_TILE_DATA_HI,
    PUSH_TO_FIFO,
  }

  private final Memory memory;
  private final Video video;
  private final InterruptRequestLine vblank;
  private final InterruptRequestLine lcd;
  private final Queue<Integer> fifo;

  private int dot = 0;
  private int drawnDots = 0;
  private int pushedPixels = 0;
  private Mode mode = Mode.MODE_2_OAM_SCAN;
  private State state = State.GET_TILE_ID;
  private boolean yCondition = false;
  private boolean isStalledForSprite = false;
  private boolean windowWasDrawnThisLine = false;

  private int clock = 0;
  private int fetcherX = 0;
  private int windowFetcherX = 0;
  private boolean inWindow = false;
  private int windowLy = 0;
  private byte tileId;
  private byte tileDataLo;
  private byte tileDataHi;

  public Ppu(Memory memory, Video video, InterruptRequestLine vblank, InterruptRequestLine lcd) {
    this.memory = memory;
    this.video = video;
    this.vblank = vblank;
    this.lcd = lcd;
    this.fifo = new ArrayDeque<>();
  }

  public void tick() {
    dot++;
    yCondition =
        yCondition
            || Byte.toUnsignedInt(memory.registers().ly().get())
                == Byte.toUnsignedInt(memory.registers().wy().get());
    switch (mode) {
      case MODE_2_OAM_SCAN -> tickOamScan();
      case MODE_3_DRAWING -> tickDrawing();
      case MODE_0_HBLANK -> tickHBlank();
      case MODE_1_VBLANK -> tickVBlank();
    }
    boolean lyEqualsLyc = memory.registers().ly().get() == memory.registers().lyc().get();
    memory.registers().stat().setLyEqualsLyc(lyEqualsLyc);
    memory.registers().stat().setMode(mode.getValue());
    if (dot == 456) {
      dot = 0;
    }
    vblank.set(mode == Mode.MODE_1_VBLANK);
    lcd.set((memory.registers().stat().get() & 0b0111_1100) != 0);
  }

  private void tickOamScan() {
    if (dot == 80) {
      mode = Mode.MODE_3_DRAWING;
    }
  }

  private void tickDrawing() {
    if (!fifo.isEmpty() && !isStalledForSprite) {
      render(fifo.remove());
      pushedPixels++;
    }
    boolean isWindowEnabled = memory.registers().lcdc().isWindowEnabled();
    if (isWindowEnabled
        && yCondition
        && !isInWindow()
        && (memory.registers().wx().get() & 0xff) - 7 == drawnDots) {
      startWindow();
      fifo.clear();
      windowWasDrawnThisLine = true;
    }
    switch (state) {
      case GET_TILE_ID -> getTileId();
      case GET_TILE_DATA_LO -> getTileDataLo();
      case GET_TILE_DATA_HI -> getTileDataHi();
      case PUSH_TO_FIFO -> pushToFifo();
    }
    if (pushedPixels == 160) {
      pushedPixels = 0;
      mode = Mode.MODE_0_HBLANK;
      nextLine();
    }
  }

  private void tickHBlank() {
    if (dot == 456) {
      int nextLy = Byte.toUnsignedInt(memory.registers().ly().get()) + 1;
      if (nextLy == 144) {
        mode = Mode.MODE_1_VBLANK;
      } else {
        mode = Mode.MODE_2_OAM_SCAN;
      }
      memory.registers().ly().set((byte) nextLy);
      drawnDots = 0;
      windowWasDrawnThisLine = false;
      yCondition = nextLy >= Byte.toUnsignedInt(memory.registers().wy().get());
    }
  }

  private void tickVBlank() {
    if (dot == 456) {
      int nextLy = Byte.toUnsignedInt(memory.registers().ly().get()) + 1;
      if (nextLy == 154) {
        video.present();
        mode = Mode.MODE_2_OAM_SCAN;
        nextLy = 0;
        yCondition = false;
        nextFrame();
      }
      memory.registers().ly().set((byte) nextLy);
    }
  }

  private void render(int pixel) {
    int colorIdx =
        switch (pixel) {
          case 0 -> (memory.registers().bgp().get() & 0b0000_0011) >>> 0;
          case 1 -> (memory.registers().bgp().get() & 0b0000_1100) >>> 2;
          case 2 -> (memory.registers().bgp().get() & 0b0011_0000) >>> 4;
          case 3 -> (memory.registers().bgp().get() & 0b1100_0000) >>> 6;
          default -> throw new IllegalStateException();
        };
    int y = Byte.toUnsignedInt(memory.registers().ly().get());
    video.writeVideoPixel(drawnDots++, y, PALETTE[colorIdx]);
  }

  private void getTileId() {
    if (clock == 0) {
      clock++;
      return;
    }
    if (memory.registers().lcdc().isWindowEnabled() && inWindow) {
      int base = memory.registers().lcdc().getWindowTileIdBase() & 0xffff;
      int row = (windowLy / 8) & 0x1f;
      int tileX = windowFetcherX & 0x1f;
      int address = base + (row * 32) + tileX;
      tileId = memory.read((short) address);
      state = State.GET_TILE_DATA_LO;
    } else if (memory.registers().lcdc().isBackgroundEnabled()) {
      int base = memory.registers().lcdc().getBackgroundTileIdBase() & 0xffff;
      int ly = memory.registers().ly().get() & 0xff;
      int scx = memory.registers().scx().get() & 0xff;
      int scy = memory.registers().scy().get() & 0xff;
      int tileY = (ly + scy) & 0xff;
      int tileX = (fetcherX + (scx / 8)) & 0x1f;
      int row = (tileY / 8) & 0x1f;
      int address = base + (row * 32) + tileX;
      tileId = memory.read((short) address);
      state = State.GET_TILE_DATA_LO;
    }
    clock = 0;
  }

  private void getTileDataLo() {
    if (clock == 0) {
      clock++;
      return;
    }

    tileDataLo = memory.read((short) computeTileAddress());
    state = State.GET_TILE_DATA_HI;
    clock = 0;
  }

  private void getTileDataHi() {
    if (clock == 0) {
      clock++;
      return;
    }

    tileDataHi = memory.read((short) (computeTileAddress() + 1));
    state = State.PUSH_TO_FIFO;
    clock = 0;
  }

  private void pushToFifo() {
    if (clock == 0) {
      clock++;
      return;
    }
    if (fifo.size() > 8) {
      return;
    }
    for (int bit = 7; bit >= 0; bit--) {
      int loBit = (tileDataLo >> bit) & 0x01;
      int hiBit = (tileDataHi >> bit) & 0x01;
      fifo.add((hiBit << 1) | loBit);
    }
    if (inWindow) {
      windowFetcherX++;
    } else {
      fetcherX++;
    }
    state = State.GET_TILE_ID;
    clock = 0;
  }

  private void nextLine() {
    fetcherX = 0;
    windowFetcherX = 0;
    if (windowWasDrawnThisLine) {
      windowLy++;
    }
    inWindow = false;
  }

  private void nextFrame() {
    windowLy = 0;
    inWindow = false;
  }

  private void startWindow() {
    inWindow = true;
    windowFetcherX = 0;
    state = State.GET_TILE_ID;
    clock = 0;
  }

  private boolean isInWindow() {
    return inWindow;
  }

  private int computeTileAddress() {
    boolean isUnsigned = memory.registers().lcdc().isTileDataBase8000();

    int base;
    int tileOffset;
    if (isUnsigned) {
      base = 0x8000;
      tileOffset = Byte.toUnsignedInt(tileId) * 16;
    } else {
      base = 0x9000;
      tileOffset = tileId * 16;
    }

    int tileLine;
    if (inWindow) {
      tileLine = windowLy % 8;
    } else {
      byte ly = memory.registers().ly().get();
      byte scy = memory.registers().scy().get();
      tileLine = ((ly & 0xff) + (scy & 0xff)) % 8;
    }

    return base + tileOffset + (tileLine * 2);
  }
}

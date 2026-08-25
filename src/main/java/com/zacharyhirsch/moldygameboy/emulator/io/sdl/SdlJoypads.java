package com.zacharyhirsch.moldygameboy.emulator.io.sdl;

import static com.zacharyhirsch.jna.sdl3.SDL.*;

import com.google.common.collect.ImmutableMap;
import com.zacharyhirsch.jna.sdl3.SDL_GamepadAxisEvent;
import com.zacharyhirsch.jna.sdl3.SDL_GamepadButtonEvent;
import com.zacharyhirsch.jna.sdl3.SDL_GamepadDeviceEvent;
import com.zacharyhirsch.jna.sdl3.SDL_KeyboardEvent;
import com.zacharyhirsch.moldygameboy.emulator.io.Joypads;
import java.lang.foreign.MemorySegment;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class SdlJoypads implements Joypads {

  private static final Logger log = LoggerFactory.getLogger(SdlJoypads.class);

  private static final Map<Integer, Joypads.Button> JOYPAD1_KEYS =
      ImmutableMap.<Integer, Joypads.Button>builder()
          .put(SDLK_S(), Joypads.Button.DOWN)
          .put(SDLK_W(), Joypads.Button.UP)
          .put(SDLK_D(), Joypads.Button.RIGHT)
          .put(SDLK_A(), Joypads.Button.LEFT)
          .put(SDLK_Z(), Joypads.Button.SELECT)
          .put(SDLK_X(), Joypads.Button.START)
          .put(SDLK_M(), Joypads.Button.BUTTON_A)
          .put((int) ',', Joypads.Button.BUTTON_B)
          .build();

  private static final Map<Integer, Joypads.Button> JOYPAD2_KEYS =
      ImmutableMap.<Integer, Joypads.Button>builder()
          .put(SDLK_DOWN(), Joypads.Button.DOWN)
          .put(SDLK_UP(), Joypads.Button.UP)
          .put(SDLK_RIGHT(), Joypads.Button.RIGHT)
          .put(SDLK_LEFT(), Joypads.Button.LEFT)
          .put(SDLK_SPACE(), Joypads.Button.SELECT)
          .put(SDLK_RETURN(), Joypads.Button.START)
          .put(SDLK_PERIOD(), Joypads.Button.BUTTON_A)
          .put(SDLK_SLASH(), Joypads.Button.BUTTON_B)
          .build();

  private final boolean[] strobe;
  private final int[] index;
  private final byte[] status;

  private MemorySegment gamepad;

  SdlJoypads() {
    this.strobe = new boolean[] {false, false};
    this.index = new int[] {0, 0};
    this.status = new byte[] {0, 0};
    this.gamepad = null;

    if (!SDL_Init(SDL_INIT_GAMEPAD())) {
      throw new SdlException("Unable to initialize SDL gamepad library");
    }
  }

  @Override
  public byte readJoypad(int index) {
    if (this.index[index] > 7) {
      return 1;
    }
    byte value = (byte) bit(status[index], this.index[index]);
    if (!strobe[index]) {
      this.index[index]++;
    }
    return value;
  }

  @Override
  public void writeJoypads(byte value) {
    writeJoypad(0, value);
    writeJoypad(1, value);
  }

  private void writeJoypad(int index, byte value) {
    strobe[index] = bit(value, 0) == 1;
    if (strobe[index]) {
      this.index[index] = 0;
    }
  }

  @Override
  public void close() {
    closeGamepad();
  }

  private void closeGamepad() {
    if (gamepad != null) {
      SDL_CloseGamepad(gamepad);
      gamepad = null;
      log.info("Closed gamepad {}", 0);
    }
  }

  public void onGamepadAdded(MemorySegment gamepadEvent) {
    int joystickId = SDL_GamepadDeviceEvent.which(gamepadEvent);
    closeGamepad();
    openGamepad(joystickId);
  }

  private void openGamepad(int joystickId) {
    gamepad = SDL_OpenGamepad(joystickId);
    if (gamepad == MemorySegment.NULL) {
      throw new SdlException("Unable to open gamepad %s".formatted(joystickId));
    }
    log.info("Opened gamepad \"{}\"", SDL_GetGamepadName(gamepad).getString(0));
  }

  public void onGamepadRemoved(MemorySegment gamepadEvent) {
    // int joystickId = SDL_GamepadDeviceEvent.which(gamepadEvent);
    closeGamepad();
  }

  public void onGamepadAxisMotion(MemorySegment gamepadAxisEvent) {
    // int joystickId = SDL_GamepadDeviceEvent.which(gamepadEvent);
    byte axis = SDL_GamepadAxisEvent.axis(gamepadAxisEvent);
    short value = SDL_GamepadAxisEvent.value(gamepadAxisEvent);
    if (axis == SDL_GAMEPAD_AXIS_LEFTX()) {
      if (value <= SDL_JOYSTICK_AXIS_MIN() / 2) {
        setButtonDown(0, Button.LEFT);
      } else if (value >= SDL_JOYSTICK_AXIS_MAX() / 2) {
        setButtonDown(0, Button.RIGHT);
      } else {
        setButtonUp(0, Button.LEFT);
        setButtonUp(0, Button.RIGHT);
      }
    } else if (axis == SDL_GAMEPAD_AXIS_LEFTY()) {
      if (value <= SDL_JOYSTICK_AXIS_MIN() / 2) {
        setButtonDown(0, Button.UP);
      } else if (value >= SDL_JOYSTICK_AXIS_MAX() / 2) {
        setButtonDown(0, Button.DOWN);
      } else {
        setButtonUp(0, Button.UP);
        setButtonUp(0, Button.DOWN);
      }
    }
  }

  public void onGamepadButtonDown(MemorySegment gamepadButtonEvent) {
    // int joystickId = SDL_GamepadDeviceEvent.which(gamepadEvent);
    Button button = mapGamepadButton(SDL_GamepadButtonEvent.button(gamepadButtonEvent));
    if (button != null) {
      setButtonDown(0, button);
    }
  }

  public void onGamepadButtonUp(MemorySegment gamepadButtonEvent) {
    // int joystickId = SDL_GamepadDeviceEvent.which(gamepadEvent);
    Button button = mapGamepadButton(SDL_GamepadButtonEvent.button(gamepadButtonEvent));
    if (button != null) {
      setButtonUp(0, button);
    }
  }

  private static Button mapGamepadButton(byte buttonCode) {
    if (buttonCode == SDL_GAMEPAD_BUTTON_DPAD_LEFT()) {
      return Button.LEFT;
    } else if (buttonCode == SDL_GAMEPAD_BUTTON_DPAD_RIGHT()) {
      return Button.RIGHT;
    } else if (buttonCode == SDL_GAMEPAD_BUTTON_DPAD_UP()) {
      return Button.UP;
    } else if (buttonCode == SDL_GAMEPAD_BUTTON_DPAD_DOWN()) {
      return Button.DOWN;
    } else if (buttonCode == SDL_GAMEPAD_BUTTON_LABEL_UNKNOWN()) {
      return Button.BUTTON_A;
    } else if (buttonCode == SDL_GAMEPAD_BUTTON_LABEL_B()) {
      return Button.BUTTON_B;
    } else if (buttonCode == SDL_GAMEPAD_BUTTON_START()) {
      return Button.START;
    } else if (buttonCode == SDL_GAMEPAD_BUTTON_BACK()) {
      return Button.SELECT;
    } else {
      log.warn("Unknown gamepad button {}", buttonCode);
      return null;
    }
  }

  public void onKeyDown(MemorySegment keyboardEvent) {
    int keyCode = SDL_KeyboardEvent.key(keyboardEvent);
    onKeyDown(0, keyCode);
    onKeyDown(1, keyCode);
  }

  private void onKeyDown(int index, int keyCode) {
    Map<Integer, Button> mapping = index == 0 ? JOYPAD1_KEYS : JOYPAD2_KEYS;
    Button button = mapping.get(keyCode);
    if (button != null) {
      setButtonDown(index, button);
    }
  }

  public void onKeyUp(MemorySegment keyboardEvent) {
    int keyCode = SDL_KeyboardEvent.key(keyboardEvent);
    onKeyUp(0, keyCode);
    onKeyUp(1, keyCode);
  }

  private void onKeyUp(int index, int keyCode) {
    Map<Integer, Button> mapping = index == 0 ? JOYPAD1_KEYS : JOYPAD2_KEYS;
    Button button = mapping.get(keyCode);
    setButtonUp(index, button);
  }

  private void setButtonDown(int index, Button button) {
    status[index] |= button.getValue();
  }

  private void setButtonUp(int index, Button button) {
    if (button != null) {
      status[index] &= (byte) ~button.getValue();
    }
  }

  private static int bit(byte value, int i) {
    return (Byte.toUnsignedInt(value) >>> i) & 1;
  }
}

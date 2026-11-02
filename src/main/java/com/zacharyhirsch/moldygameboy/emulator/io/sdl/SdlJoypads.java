package com.zacharyhirsch.moldygameboy.emulator.io.sdl;

import static com.zacharyhirsch.jna.sdl3.SDL.*;

import com.google.common.collect.ImmutableMap;
import com.zacharyhirsch.jna.sdl3.SDL_GamepadAxisEvent;
import com.zacharyhirsch.jna.sdl3.SDL_GamepadButtonEvent;
import com.zacharyhirsch.jna.sdl3.SDL_GamepadDeviceEvent;
import com.zacharyhirsch.jna.sdl3.SDL_KeyboardEvent;
import com.zacharyhirsch.moldygameboy.emulator.io.Joypads;
import java.lang.foreign.MemorySegment;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class SdlJoypads implements Joypads {

  private static final Logger log = LoggerFactory.getLogger(SdlJoypads.class);

  private static final Map<Integer, Button> KEYS =
      ImmutableMap.<Integer, Button>builder()
          .put(SDLK_W(), Button.UP)
          .put(SDLK_A(), Button.LEFT)
          .put(SDLK_S(), Button.DOWN)
          .put(SDLK_D(), Button.RIGHT)
          .put(SDLK_Z(), Button.START)
          .put(SDLK_X(), Button.SELECT)
          .put(SDLK_PERIOD(), Button.BUTTON_B)
          .put(SDLK_SLASH(), Button.BUTTON_A)
          .build();

  private final Set<Button> pressed = EnumSet.noneOf(Button.class);

  private MemorySegment gamepad;

  SdlJoypads() {
    this.gamepad = null;

    if (!SDL_Init(SDL_INIT_GAMEPAD())) {
      throw new SdlException("Unable to initialize SDL gamepad library");
    }
  }

  @Override
  public boolean isPressed(Button button) {
    return pressed.contains(button);
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
        setButtonDown(Button.LEFT);
      } else if (value >= SDL_JOYSTICK_AXIS_MAX() / 2) {
        setButtonDown(Button.RIGHT);
      } else {
        setButtonUp(Button.LEFT);
        setButtonUp(Button.RIGHT);
      }
    } else if (axis == SDL_GAMEPAD_AXIS_LEFTY()) {
      if (value <= SDL_JOYSTICK_AXIS_MIN() / 2) {
        setButtonDown(Button.UP);
      } else if (value >= SDL_JOYSTICK_AXIS_MAX() / 2) {
        setButtonDown(Button.DOWN);
      } else {
        setButtonUp(Button.UP);
        setButtonUp(Button.DOWN);
      }
    }
  }

  public void onGamepadButtonDown(MemorySegment gamepadButtonEvent) {
    // int joystickId = SDL_GamepadDeviceEvent.which(gamepadEvent);
    Button button = mapGamepadButton(SDL_GamepadButtonEvent.button(gamepadButtonEvent));
    if (button != null) {
      setButtonDown(button);
    }
  }

  public void onGamepadButtonUp(MemorySegment gamepadButtonEvent) {
    // int joystickId = SDL_GamepadDeviceEvent.which(gamepadEvent);
    Button button = mapGamepadButton(SDL_GamepadButtonEvent.button(gamepadButtonEvent));
    if (button != null) {
      setButtonUp(button);
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
    } else if (buttonCode == SDL_GAMEPAD_BUTTON_EAST()) {
      return Button.BUTTON_A;
    } else if (buttonCode == SDL_GAMEPAD_BUTTON_SOUTH()) {
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
    Button button = KEYS.get(SDL_KeyboardEvent.key(keyboardEvent));
    if (button != null) {
      setButtonDown(button);
    }
  }

  public void onKeyUp(MemorySegment keyboardEvent) {
    Button button = KEYS.get(SDL_KeyboardEvent.key(keyboardEvent));
    if (button != null) {
      setButtonUp(button);
    }
  }

  private void setButtonDown(Button button) {
    pressed.add(button);
  }

  private void setButtonUp(Button button) {
    pressed.remove(button);
  }
}

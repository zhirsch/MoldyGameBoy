package com.zacharyhirsch.moldygameboy.emulator.io;

import static com.zacharyhirsch.jna.sdl3.SDL.*;

import com.zacharyhirsch.jna.sdl3.SDL_AudioSpec;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

final class SdlAudio implements Audio {

  private final MemorySegment audioBuffer;
  private int audioBufferLen;
  private final int audioDeviceId;
  private final MemorySegment audioStream;

  public SdlAudio(Arena arena, int sampleRate) {
    if (!SDL_InitSubSystem(SDL_INIT_AUDIO())) {
      throw new SdlException("Unable to initialize SDL audio library");
    }

    audioBuffer = arena.allocate(1 << 20);
    audioBufferLen = 0;

    MemorySegment output = SDL_AudioSpec.allocate(arena);
    SDL_AudioSpec.freq(output, 44100);
    SDL_AudioSpec.format(output, SDL_AUDIO_F32());
    SDL_AudioSpec.channels(output, 1);
    audioDeviceId = SDL_OpenAudioDevice(SDL_AUDIO_DEVICE_DEFAULT_PLAYBACK(), output);
    if (audioDeviceId == 0) {
      throw new SdlException("Unable to open audio device");
    }

    MemorySegment input = SDL_AudioSpec.allocate(arena);
    SDL_AudioSpec.freq(input, sampleRate);
    SDL_AudioSpec.format(input, SDL_AUDIO_F32());
    SDL_AudioSpec.channels(input, 1);
    audioStream = SDL_CreateAudioStream(input, output);
    if (audioStream == null) {
      throw new SdlException("Unable to open audio stream");
    }
    if (!SDL_BindAudioStream(audioDeviceId, audioStream)) {
      throw new SdlException("Unable to bind audio stream");
    }
    if (!SDL_ResumeAudioDevice(audioDeviceId)) {
      throw new SdlException("Unable to resume audio device");
    }
  }

  @Override
  public void writeAudioSample(float sample) {
    audioBuffer.set(ValueLayout.JAVA_FLOAT, audioBufferLen, sample);
    audioBufferLen += Float.BYTES;
  }

  @Override
  public void present() {
    if (audioBufferLen == 0) {
      return;
    }
    if (!SDL_PutAudioStreamData(audioStream, audioBuffer, audioBufferLen)) {
      throw new SdlException("Unable to add sample to audio stream");
    }
    audioBufferLen = 0;
  }

  @Override
  public void close() {
    SDL_CloseAudioDevice(audioDeviceId);
    SDL_DestroyAudioStream(audioStream);
  }
}

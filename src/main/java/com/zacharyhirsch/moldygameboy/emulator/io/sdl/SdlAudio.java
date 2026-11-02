package com.zacharyhirsch.moldygameboy.emulator.io.sdl;

import static com.zacharyhirsch.jna.sdl3.SDL.*;

import com.zacharyhirsch.jna.sdl3.SDL_AudioSpec;
import com.zacharyhirsch.moldygameboy.emulator.io.Audio;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

final class SdlAudio implements Audio {

  // How much audio to queue before playing, as a cushion against frame timing jitter.
  private static final double LATENCY_SECONDS = 0.05;
  // Emulation waits while more than this many cushions' worth of audio is queued. Video pacing
  // stops while the LCD is off, so this keeps emulation in step with playback then too.
  private static final int MAX_QUEUED_LATENCIES = 3;

  private final long latencyBytes;
  private final MemorySegment audioBuffer;
  private int audioBufferLen;
  private final int audioDeviceId;
  private final MemorySegment audioStream;

  private boolean playing = false;

  public SdlAudio(Arena arena, int sampleRate) {
    if (!SDL_InitSubSystem(SDL_INIT_AUDIO())) {
      throw new SdlException("Unable to initialize SDL audio library");
    }

    latencyBytes = (long) (sampleRate * LATENCY_SECONDS) * Float.BYTES;
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
    // Stay paused until enough audio is queued (see present).
    if (!SDL_PauseAudioDevice(audioDeviceId)) {
      throw new SdlException("Unable to pause audio device");
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
    // If playback ran out of audio, pause until the cushion has built up again.
    if (playing && SDL_GetAudioStreamQueued(audioStream) == 0) {
      if (!SDL_PauseAudioDevice(audioDeviceId)) {
        throw new SdlException("Unable to pause audio device");
      }
      playing = false;
    }
    if (!SDL_PutAudioStreamData(audioStream, audioBuffer, audioBufferLen)) {
      throw new SdlException("Unable to add sample to audio stream");
    }
    audioBufferLen = 0;
    if (!playing && SDL_GetAudioStreamQueued(audioStream) >= latencyBytes) {
      if (!SDL_ResumeAudioDevice(audioDeviceId)) {
        throw new SdlException("Unable to resume audio device");
      }
      playing = true;
    }
    while (playing && SDL_GetAudioStreamQueued(audioStream) > MAX_QUEUED_LATENCIES * latencyBytes) {
      SDL_DelayNS(1_000_000);
    }
  }

  @Override
  public void close() {
    SDL_CloseAudioDevice(audioDeviceId);
    SDL_DestroyAudioStream(audioStream);
  }
}

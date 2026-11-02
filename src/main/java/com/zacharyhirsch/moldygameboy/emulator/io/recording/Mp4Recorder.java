package com.zacharyhirsch.moldygameboy.emulator.io.recording;

import static org.bytedeco.ffmpeg.global.avcodec.AV_CODEC_ID_AAC;
import static org.bytedeco.ffmpeg.global.avcodec.AV_CODEC_ID_H264;
import static org.bytedeco.ffmpeg.global.avutil.AV_PIX_FMT_RGB24;
import static org.bytedeco.ffmpeg.global.avutil.AV_PIX_FMT_YUV420P;

import com.zacharyhirsch.moldygameboy.emulator.io.Color;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;

/**
 * Encodes the emulator's video and audio into an MP4 file (H.264 and AAC), on a background thread
 * so that encoding doesn't slow emulation down.
 *
 * <p>The APU writes one audio sample per M-cycle whether or not the LCD is on, but frames are only
 * presented while it's on, so the audio is the clock: each presented frame is repeated (or
 * dropped) so that the video keeps up with the audio.
 */
final class Mp4Recorder implements AutoCloseable {

  private static final int WIDTH = 160;
  private static final int HEIGHT = 144;
  // Pixels are scaled up so that chroma subsampling doesn't blur them.
  private static final int SCALE = 4;

  private static final double INPUT_SAMPLE_RATE = (1 << 22) / 4.0;
  private static final int OUTPUT_SAMPLE_RATE = 48_000;
  private static final double FRAME_RATE = (1 << 22) / 70224.0;
  private static final double SAMPLES_PER_FRAME = INPUT_SAMPLE_RATE / FRAME_RATE;
  private static final double SAMPLES_PER_OUTPUT_SAMPLE = INPUT_SAMPLE_RATE / OUTPUT_SAMPLE_RATE;

  private final FFmpegFrameRecorder recorder;
  private final ExecutorService encoder = Executors.newSingleThreadExecutor();
  private final byte[] frame = new byte[WIDTH * HEIGHT * 3];

  // Downsampling: each output sample is the average of the input samples it covers. The output is
  // stereo, with left and right samples interleaved.
  private final float[] output = new float[2 * 4096];
  private int outputLength = 0;
  private double leftSum = 0;
  private double rightSum = 0;
  private int summed = 0;
  private double nextOutputSample = SAMPLES_PER_OUTPUT_SAMPLE;

  private long inputSamples = 0;
  private long framesWritten = 0;

  private volatile Exception error = null;

  Mp4Recorder(Path path) {
    recorder = new FFmpegFrameRecorder(path.toFile(), WIDTH * SCALE, HEIGHT * SCALE, 2);
    recorder.setFormat("mp4");
    recorder.setVideoCodec(AV_CODEC_ID_H264);
    recorder.setPixelFormat(AV_PIX_FMT_YUV420P);
    recorder.setFrameRate(FRAME_RATE);
    recorder.setVideoBitrate(4_000_000);
    recorder.setAudioCodec(AV_CODEC_ID_AAC);
    recorder.setSampleRate(OUTPUT_SAMPLE_RATE);
    recorder.setAudioBitrate(192_000);
    try {
      recorder.start();
    } catch (FFmpegFrameRecorder.Exception e) {
      throw new IllegalStateException("Unable to start recording to " + path, e);
    }
  }

  void writeVideoPixel(int x, int y, Color color) {
    int i = (y * WIDTH + x) * 3;
    frame[i] = color.r();
    frame[i + 1] = color.g();
    frame[i + 2] = color.b();
  }

  void presentVideo() {
    long frames = Math.round(inputSamples / SAMPLES_PER_FRAME) - framesWritten;
    if (frames > 0) {
      byte[] scaled = scale(frame);
      submit(() -> recordFrame(scaled, frames));
      framesWritten += frames;
    }
  }

  void writeAudioSample(float left, float right) {
    inputSamples++;
    leftSum += left;
    rightSum += right;
    summed++;
    if (inputSamples >= nextOutputSample) {
      output[outputLength++] = (float) (leftSum / summed);
      output[outputLength++] = (float) (rightSum / summed);
      leftSum = 0;
      rightSum = 0;
      summed = 0;
      nextOutputSample += SAMPLES_PER_OUTPUT_SAMPLE;
      if (outputLength == output.length) {
        presentAudio();
      }
    }
  }

  void presentAudio() {
    if (outputLength == 0) {
      return;
    }
    float[] samples = Arrays.copyOf(output, outputLength);
    outputLength = 0;
    submit(() -> recorder.recordSamples(OUTPUT_SAMPLE_RATE, 2, FloatBuffer.wrap(samples)));
  }

  @Override
  public void close() throws Exception {
    presentAudio();
    presentVideo();
    encoder.shutdown();
    encoder.awaitTermination(1, TimeUnit.MINUTES);
    recorder.stop();
    recorder.release();
    if (error != null) {
      throw error;
    }
  }

  private void submit(EncoderTask task) {
    encoder.execute(
        () -> {
          if (error != null) {
            return;
          }
          try {
            task.run();
          } catch (Exception e) {
            error = e;
          }
        });
  }

  private void recordFrame(byte[] rgb, long count) throws FFmpegFrameRecorder.Exception {
    Frame f = new Frame(WIDTH * SCALE, HEIGHT * SCALE, Frame.DEPTH_UBYTE, 3);
    ((ByteBuffer) f.image[0]).put(rgb).flip();
    for (long i = 0; i < count; i++) {
      recorder.record(f, AV_PIX_FMT_RGB24);
    }
  }

  private static byte[] scale(byte[] frame) {
    int width = WIDTH * SCALE;
    byte[] scaled = new byte[width * HEIGHT * SCALE * 3];
    for (int y = 0; y < HEIGHT * SCALE; y++) {
      for (int x = 0; x < width; x++) {
        int from = ((y / SCALE) * WIDTH + (x / SCALE)) * 3;
        int to = (y * width + x) * 3;
        scaled[to] = frame[from];
        scaled[to + 1] = frame[from + 1];
        scaled[to + 2] = frame[from + 2];
      }
    }
    return scaled;
  }

  @FunctionalInterface
  private interface EncoderTask {
    void run() throws Exception;
  }
}

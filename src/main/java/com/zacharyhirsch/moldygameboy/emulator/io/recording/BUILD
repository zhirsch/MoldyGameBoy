load("@rules_java//java:java_library.bzl", "java_library")
load("@rules_jvm_external//:defs.bzl", "artifact")

java_library(
    name = "recording",
    srcs = glob(["*.java"]),
    visibility = [
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/io:__pkg__",
        "//src/test/java/com/zacharyhirsch/moldygameboy/emulator:__pkg__",
    ],
    runtime_deps = [
        "@maven//:org_bytedeco_ffmpeg_macosx_arm64",
        "@maven//:org_bytedeco_javacpp_macosx_arm64",
    ],
    deps = [
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/io",
        artifact("org.bytedeco:ffmpeg"),
        artifact("org.bytedeco:javacpp"),
        artifact("org.bytedeco:javacv"),
    ],
)

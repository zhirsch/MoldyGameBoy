load("@rules_java//java:java_library.bzl", "java_library")
load("@rules_jvm_external//:defs.bzl", "artifact")

java_library(
    name = "io",
    srcs = glob(["*.java"]),
    visibility = [
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator:__pkg__",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/ppu:__pkg__",
        "//src/test/java/com/zacharyhirsch/moldygameboy/emulator:__pkg__",
    ],
    deps = [
        "//third_party/sdl3",
        artifact("com.google.guava:guava"),
        artifact("net.java.dev.jna:jna"),
        artifact("org.slf4j.slf4j:api"),
    ],
)

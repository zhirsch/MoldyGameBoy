load("@rules_java//java:java_library.bzl", "java_library")
load("@rules_jvm_external//:defs.bzl", "artifact")

java_library(
    name = "io",
    srcs = glob(
        ["*.java"],
        exclude = ["IoFactory.java"],
    ),
    visibility = [
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator:__pkg__",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/io/none:__pkg__",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/io/sdl:__pkg__",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/ppu:__pkg__",
        "//src/test/java/com/zacharyhirsch/moldygameboy/emulator:__pkg__",
    ],
    deps = [
        artifact("com.google.guava:guava"),
        artifact("net.java.dev.jna:jna"),
        artifact("org.slf4j.slf4j:api"),
    ],
)

java_library(
    name = "factory",
    srcs = ["IoFactory.java"],
    visibility = [
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator:__pkg__",
        "//src/test/java/com/zacharyhirsch/moldygameboy/emulator:__pkg__",
    ],
    deps = [
        ":io",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/io/none",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/io/sdl",
    ],
)

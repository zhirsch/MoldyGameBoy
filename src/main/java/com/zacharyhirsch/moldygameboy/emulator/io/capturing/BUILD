load("@rules_java//java:java_library.bzl", "java_library")
load("@rules_jvm_external//:defs.bzl", "artifact")

java_library(
    name = "capturing",
    srcs = glob(["*.java"]),
    visibility = [
        "//src/test/java/com/zacharyhirsch/moldygameboy/emulator:__pkg__",
    ],
    deps = [
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/io",
    ],
)

load("@rules_java//java:java_library.bzl", "java_library")
load("@rules_jvm_external//:defs.bzl", "artifact")

java_library(
    name = "none",
    srcs = glob(["*.java"]),
    visibility = ["//src/main/java/com/zacharyhirsch/moldygameboy/emulator/io:__pkg__"],
    deps = [
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/io",
        artifact("com.google.guava:guava"),
        artifact("net.java.dev.jna:jna"),
        artifact("org.slf4j.slf4j:api"),
    ],
)

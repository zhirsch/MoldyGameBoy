load("@rules_java//java:java_library.bzl", "java_library")
load("@rules_jvm_external//:defs.bzl", "artifact")

java_library(
    name = "arch",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        artifact("com.google.guava:guava"),
    ],
)

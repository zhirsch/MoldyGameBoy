load("@contrib_rules_jvm//java:defs.bzl", "JUNIT5_DEPS", "java_junit5_test")
load("@rules_jvm_external//:defs.bzl", "artifact")

java_junit5_test(
    name = "MemoryTest",
    srcs = ["MemoryTest.java"],
    deps = [
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/memory",
        artifact("com.google.truth:truth"),
        artifact("org.junit.jupiter:junit-jupiter-api"),
    ] + JUNIT5_DEPS,
)

java_junit5_test(
    name = "HdmaTest",
    srcs = ["HdmaTest.java"],
    deps = [
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/memory",
        artifact("com.google.truth:truth"),
        artifact("org.junit.jupiter:junit-jupiter-api"),
    ] + JUNIT5_DEPS,
)

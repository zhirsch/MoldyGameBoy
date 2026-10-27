load("@contrib_rules_jvm//java:defs.bzl", "JUNIT5_DEPS", "JUNIT5_RUNTIME_DEPS", "java_junit5_test")
load("@rules_java//java:java_library.bzl", "java_library")
load("@rules_jvm_external//:defs.bzl", "artifact")

java_library(
    name = "emulator",
    testonly = True,
    srcs = ["RegistersSubject.java"],
    deps = [
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/cpu/registers",
        artifact("com.google.truth:truth"),
    ],
)

java_junit5_test(
    name = "BlarggTests",
    srcs = ["BlarggTests.java"],
    jvm_flags =
        select({
            "//:macos": ["-XstartOnFirstThread"],
            "//conditions:default": [],
        }) + [
            "--enable-native-access=ALL-UNNAMED",
        ],
    resources = [
        "//:blargg-test-suite",
        "//src/main/resources:dmg.bin",
        "//src/main/resources:logback.xml",
    ],
    deps = [
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/arch",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/cpu",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/cpu/registers",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/io",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/io:factory",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/memory",
        artifact("com.google.guava:guava"),
        artifact("com.google.truth:truth"),
        artifact("org.junit.jupiter:junit-jupiter-api"),
        artifact("org.junit.jupiter:junit-jupiter-params"),
    ] + JUNIT5_DEPS,
)

java_junit5_test(
    name = "MooneyeTest",
    srcs = ["MooneyeTest.java"],
    jvm_flags =
        select({
            "//:macos": ["-XstartOnFirstThread"],
            "//conditions:default": [],
        }) + [
            "--enable-native-access=ALL-UNNAMED",
        ],
    resources = [
        "//:mooneye-test-suite",
        "//src/main/resources:dmg.bin",
        "//src/main/resources:logback.xml",
    ],
    runtime_deps = [artifact("ch.qos.logback:logback-classic")],
    deps = [
        ":emulator",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/arch",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/cpu/registers",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/io",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/io:factory",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/memory",
        artifact("com.google.guava:guava"),
        artifact("com.google.truth:truth"),
        artifact("org.junit.jupiter:junit-jupiter-api"),
        artifact("org.junit.jupiter:junit-jupiter-params"),
    ] + JUNIT5_DEPS,
)

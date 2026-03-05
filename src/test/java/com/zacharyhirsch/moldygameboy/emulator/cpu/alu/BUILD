load("@contrib_rules_jvm//java:defs.bzl", "java_test_suite")
load("@rules_jvm_external//:defs.bzl", "artifact")

java_test_suite(
    name = "AllTests",
    srcs = glob(["*.java"]),
    jvm_flags = ["-Djava.security.manager=disallow"],
    runner = "junit5",
    runtime_deps = [
        artifact("org.junit.jupiter:junit-jupiter-engine"),
        artifact("org.junit.platform:junit-platform-launcher"),
        artifact("org.junit.platform:junit-platform-reporting"),
    ],
    deps = [
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/cpu/alu",
        artifact("com.google.truth:truth"),
        artifact("org.junit.jupiter:junit-jupiter-api"),
        artifact("org.junit.jupiter:junit-jupiter-params"),
    ],
)

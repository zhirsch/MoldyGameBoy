config_setting(
    name = "macos",
    constraint_values = ["@platforms//os:osx"],
    visibility = ["//visibility:public"],
)

config_setting(
    name = "windows",
    constraint_values = ["@platforms//os:windows"],
    visibility = ["//visibility:public"],
)

filegroup(
    name = "sm83-v1-files",
    srcs = glob(["sm83/v1/*.json"]),
    visibility = ["//src/test:__subpackages__"],
)

filegroup(
    name = "mooneye-test-suite",
    srcs = glob(["mooneye-test-suite/build/**/*.gb"]),
    visibility = ["//src/test:__subpackages__"],
)

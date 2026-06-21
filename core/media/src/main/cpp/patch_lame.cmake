# This script is executed by FetchContent's PATCH_COMMAND.
# The working directory is automatically set to the fetched LAME source directory.

# 1. Patch the fetched CMakeLists.txt to comply with CMake 4.x
file(READ "CMakeLists.txt" LAME_CMAKE)
string(REGEX REPLACE "cmake_minimum_required\\(VERSION [A-Za-z0-9\\.]+\\)" "cmake_minimum_required(VERSION 3.5)" NEW_LAME_CMAKE "${LAME_CMAKE}")
file(WRITE "CMakeLists.txt" "${NEW_LAME_CMAKE}")

# 2. Patch util.h for Android NDK math conflict
file(READ "libmp3lame/util.h" UTIL_H)
string(REPLACE "extern ieee754_float32_t fast_log2(ieee754_float32_t x);" "extern float fast_log2(float x);" NEW_UTIL_H "${UTIL_H}")
file(WRITE "libmp3lame/util.h" "${NEW_UTIL_H}")

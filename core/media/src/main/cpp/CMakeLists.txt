cmake_minimum_required(VERSION 3.22.1)
project("mp3lame-jni")

set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -Wl,-z,max-page-size=16384")

include(FetchContent)

FetchContent_Declare(
    lame
    GIT_REPOSITORY https://github.com/Iunusov/libmp3lame-CMAKE.git
    GIT_TAG        master
)

FetchContent_GetProperties(lame)
if(NOT lame_POPULATED)
    FetchContent_Populate(lame)
    
    file(READ "${lame_SOURCE_DIR}/libmp3lame/util.h" UTIL_H)
    string(REPLACE "extern ieee754_float32_t fast_log2(ieee754_float32_t x);" "extern float fast_log2(float x);" NEW_UTIL_H "${UTIL_H}")
    file(WRITE "${lame_SOURCE_DIR}/libmp3lame/util.h" "${NEW_UTIL_H}")
    
    add_subdirectory(${lame_SOURCE_DIR} ${lame_BINARY_DIR})
endif()

add_library(lame-jni SHARED LameEncoder.cpp)
target_link_libraries(lame-jni mp3lame log)

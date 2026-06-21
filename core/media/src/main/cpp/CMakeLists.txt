cmake_minimum_required(VERSION 3.24)
project("mp3lame-jni")

set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -Wl,-z,max-page-size=16384")

include(FetchContent)

FetchContent_Declare(
    lame
    GIT_REPOSITORY https://github.com/Iunusov/libmp3lame-CMAKE.git
    GIT_TAG        master
    PATCH_COMMAND  ${CMAKE_COMMAND} -P ${CMAKE_CURRENT_LIST_DIR}/patch_lame.cmake
)

FetchContent_MakeAvailable(lame)

add_library(lame-jni SHARED LameEncoder.cpp)
target_link_libraries(lame-jni mp3lame log)

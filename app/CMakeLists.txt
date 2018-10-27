cmake_minimum_required(VERSION 3.6)

include_directories(${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/include/)

add_library( # Sets the name of the library.

             native-lib



             # Sets the library as a shared library.

             SHARED



             # Provides a relative path to your source file(s).

             src/main/cpp/native-lib.cpp )
include_directories(openssl-armeabi-v7a/include/)

find_library(
                log-lib
                log
                )
target_link_libraries(
                native-lib
                ${CMAKE_CURRENT_SOURCE_DIR}/openssl-armeabi-v7a/lib/libcrypto.a
                ${CMAKE_CURRENT_SOURCE_DIR}/openssl-armeabi-v7a/lib/libssl.a
                ${log-lib}



)
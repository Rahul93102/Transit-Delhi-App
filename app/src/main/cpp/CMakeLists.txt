cmake_minimum_required(VERSION 3.22.1)
project(metro_path_finder)

# Set C++ standard
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Add Android STL type
set(CMAKE_ANDROID_STL_TYPE c++_shared)

# Add compiler flags
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++17 -fexceptions -frtti")

# Add the native library
add_library(metro_path_finder SHARED
            metro_graph.cpp
            metro_path_finder.cpp
            metro_data_parser.cpp
            jni_bridge.cpp)

# Include directories
target_include_directories(metro_path_finder PRIVATE 
                          ${CMAKE_CURRENT_SOURCE_DIR}
                          ${CMAKE_CURRENT_SOURCE_DIR}/include)

# Link libraries
target_link_libraries(metro_path_finder
                      android
                      log) 
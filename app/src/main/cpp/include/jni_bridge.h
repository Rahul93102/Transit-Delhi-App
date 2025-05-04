#ifndef JNI_BRIDGE_H
#define JNI_BRIDGE_H

#include <jni.h>
#include <string>
#include <vector>
#include <memory>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <android/log.h>

// Include metro-related headers
#include "metro_graph.h"
#include "metro_path_finder.h"
#include "metro_data_parser.h"

// Global pointers to access from different JNI functions
extern std::unique_ptr<MetroGraph> gMetroGraph;
extern std::unique_ptr<MetroPathFinder> gPathFinder;

// JNI function declarations
extern "C" {

// Initialize the metro graph with data from GTFS files
JNIEXPORT jboolean JNICALL
Java_com_example_opendelhitransit_data_native_MetroNativeLib_initMetroGraphNative(JNIEnv* env, jobject thiz, jobject assetManager);

// Find shortest path between two stations by station IDs
JNIEXPORT jobject JNICALL
Java_com_example_opendelhitransit_data_native_MetroNativeLib_findShortestPathNative(JNIEnv* env, jobject thiz, jint sourceId, jint targetId);

// Find fastest path between two stations by station IDs
JNIEXPORT jobject JNICALL
Java_com_example_opendelhitransit_data_native_MetroNativeLib_findFastestPathNative(JNIEnv* env, jobject thiz, jint sourceId, jint targetId);

// Find shortest path between two stations by names
JNIEXPORT jobject JNICALL
Java_com_example_opendelhitransit_data_native_MetroNativeLib_findShortestPathByNamesNative(JNIEnv* env, jobject thiz, jstring sourceName, jstring targetName);

// Find fastest path between two stations by names
JNIEXPORT jobject JNICALL
Java_com_example_opendelhitransit_data_native_MetroNativeLib_findFastestPathByNamesNative(JNIEnv* env, jobject thiz, jstring sourceName, jstring targetName);

// Get all station names
JNIEXPORT jobjectArray JNICALL
Java_com_example_opendelhitransit_data_native_MetroNativeLib_getAllStationNamesNative(JNIEnv* env, jobject thiz);

// Helper function to convert a MetroPath to a Java MetroPath object
jobject createJavaMetroPath(JNIEnv* env, const MetroPath& path);

} // extern "C"

#endif // JNI_BRIDGE_H 
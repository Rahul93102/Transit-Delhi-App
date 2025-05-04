#include "jni_bridge.h"
#include <android/log.h>

#define LOG_TAG "MetroNative"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Global references to the graph and path finder
std::unique_ptr<MetroGraph> gMetroGraph;
std::unique_ptr<MetroPathFinder> gPathFinder;

// Implementation in metro_native_lib.cpp to ensure proper library linking 

extern "C" {

JNIEXPORT jboolean JNICALL
Java_com_example_opendelhitransit_data_native_MetroNativeLib_initMetroGraphNative(JNIEnv* env, jobject thiz, jobject assetManager) {
    // Get the AAssetManager
    AAssetManager* nativeAssetManager = AAssetManager_fromJava(env, assetManager);
    if (!nativeAssetManager) {
        LOGE("Failed to get native asset manager");
        return JNI_FALSE;
    }
    
    // Create Metro Graph if not already created
    if (!gMetroGraph) {
        gMetroGraph = std::make_unique<MetroGraph>();
    }
    
    // Create Metro Data Parser
    MetroDataParser parser(*gMetroGraph, nativeAssetManager);
    
    // Parse GTFS data
    bool success = parser.parseGTFSData();
    
    if (success) {
        // Create path finder
        gPathFinder = std::make_unique<MetroPathFinder>(*gMetroGraph);
        
        // Log station count
        auto stationIds = gMetroGraph->getAllStationIds();
        LOGI("Metro graph initialized with %d stations", static_cast<int>(stationIds.size()));
        
        // Check connections
        int connectedStations = 0;
        for (auto id : stationIds) {
            if (!gMetroGraph->getNeighbors(id).empty()) {
                connectedStations++;
            }
        }
        
        LOGI("Found %d/%d stations with connections", connectedStations, static_cast<int>(stationIds.size()));
        
        if (connectedStations == 0) {
            LOGE("Warning: No station connections found in the metro graph!");
        }
        
    } else {
        LOGE("Failed to initialize metro graph");
    }
    
    return success ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jobject JNICALL
Java_com_example_opendelhitransit_data_native_MetroNativeLib_findShortestPathNative(JNIEnv* env, jobject thiz, jint sourceId, jint targetId) {
    if (!gPathFinder) {
        LOGE("Path finder not initialized");
        return nullptr;
    }
    
    LOGI("Finding shortest path from station ID %d to %d", sourceId, targetId);
    
    // Find shortest path
    MetroPath path = gPathFinder->findShortestPath(sourceId, targetId);
    
    if (path.stationIds.empty()) {
        LOGE("No path found between station IDs %d and %d", sourceId, targetId);
        return nullptr;
    }
    
    LOGI("Path found with %d stations, %d lines, %.2f km, %.2f min", 
         static_cast<int>(path.stationIds.size()), static_cast<int>(path.lineIds.size()), 
         path.totalDistance, path.totalTime);
    
    // Convert to Java object
    return createJavaMetroPath(env, path);
}

JNIEXPORT jobject JNICALL
Java_com_example_opendelhitransit_data_native_MetroNativeLib_findFastestPathNative(JNIEnv* env, jobject thiz, jint sourceId, jint targetId) {
    if (!gPathFinder) {
        LOGE("Path finder not initialized");
        return nullptr;
    }
    
    LOGI("Finding fastest path from station ID %d to %d", sourceId, targetId);
    
    // Find fastest path
    MetroPath path = gPathFinder->findFastestPath(sourceId, targetId);
    
    if (path.stationIds.empty()) {
        LOGE("No path found between station IDs %d and %d", sourceId, targetId);
        return nullptr;
    }
    
    LOGI("Path found with %d stations, %d lines, %.2f km, %.2f min", 
         static_cast<int>(path.stationIds.size()), static_cast<int>(path.lineIds.size()), 
         path.totalDistance, path.totalTime);
    
    // Convert to Java object
    return createJavaMetroPath(env, path);
}

JNIEXPORT jobject JNICALL
Java_com_example_opendelhitransit_data_native_MetroNativeLib_findShortestPathByNamesNative(JNIEnv* env, jobject thiz, jstring sourceName, jstring targetName) {
    if (!gPathFinder) {
        LOGE("Path finder not initialized");
        return nullptr;
    }
    
    // Convert Java strings to C++ strings
    const char* sourceNameChars = env->GetStringUTFChars(sourceName, nullptr);
    const char* targetNameChars = env->GetStringUTFChars(targetName, nullptr);
    
    std::string sourceNameStr(sourceNameChars);
    std::string targetNameStr(targetNameChars);
    
    LOGI("Finding path from '%s' to '%s'", sourceNameStr.c_str(), targetNameStr.c_str());
    
    // Release Java strings
    env->ReleaseStringUTFChars(sourceName, sourceNameChars);
    env->ReleaseStringUTFChars(targetName, targetNameChars);
    
    // Find shortest path
    MetroPath path = gPathFinder->findShortestPath(sourceNameStr, targetNameStr);
    
    // Log results
    if (path.stationIds.empty()) {
        LOGE("No path found between '%s' and '%s'", sourceNameStr.c_str(), targetNameStr.c_str());
        
        // Log some diagnostic information
        auto sourceStations = gMetroGraph->getStationsByName(sourceNameStr);
        auto targetStations = gMetroGraph->getStationsByName(targetNameStr);
        
        LOGI("Found %d stations matching source name and %d stations matching target name", 
             static_cast<int>(sourceStations.size()), static_cast<int>(targetStations.size()));
        
        if (!sourceStations.empty() && !targetStations.empty()) {
            int sourceId = sourceStations[0]->id;
            int targetId = targetStations[0]->id;
            
            auto sourceNeighbors = gMetroGraph->getNeighbors(sourceId);
            auto targetNeighbors = gMetroGraph->getNeighbors(targetId);
            
            LOGI("Source station '%s' (ID: %d) has %d connections", 
                 sourceStations[0]->name.c_str(), sourceId, static_cast<int>(sourceNeighbors.size()));
            LOGI("Target station '%s' (ID: %d) has %d connections", 
                 targetStations[0]->name.c_str(), targetId, static_cast<int>(targetNeighbors.size()));
            
            if (sourceNeighbors.empty() || targetNeighbors.empty()) {
                LOGE("One or both stations have no connections, so no path can be found");
            }
        }
        
        return nullptr;
    }
    
    LOGI("Path found with %d stations, %d lines, %.2f km, %.2f min", 
        static_cast<int>(path.stationIds.size()), static_cast<int>(path.lineIds.size()), 
        path.totalDistance, path.totalTime);
    
    // Log the first few stations in the path
    for (size_t i = 0; i < std::min(path.stationIds.size(), size_t(5)); i++) {
        const MetroStation* station = gMetroGraph->getStation(path.stationIds[i]);
        if (station) {
            LOGI("Station %d: %s", static_cast<int>(i), station->name.c_str());
        }
    }
    
    // Convert to Java object
    return createJavaMetroPath(env, path);
}

JNIEXPORT jobject JNICALL
Java_com_example_opendelhitransit_data_native_MetroNativeLib_findFastestPathByNamesNative(JNIEnv* env, jobject thiz, jstring sourceName, jstring targetName) {
    if (!gPathFinder) {
        LOGE("Path finder not initialized");
        return nullptr;
    }
    
    // Convert Java strings to C++ strings
    const char* sourceNameChars = env->GetStringUTFChars(sourceName, nullptr);
    const char* targetNameChars = env->GetStringUTFChars(targetName, nullptr);
    
    std::string sourceNameStr(sourceNameChars);
    std::string targetNameStr(targetNameChars);
    
    LOGI("Finding fastest path from '%s' to '%s'", sourceNameStr.c_str(), targetNameStr.c_str());
    
    // Release Java strings
    env->ReleaseStringUTFChars(sourceName, sourceNameChars);
    env->ReleaseStringUTFChars(targetName, targetNameChars);
    
    // Find fastest path
    MetroPath path = gPathFinder->findFastestPath(sourceNameStr, targetNameStr);
    
    // Log results
    if (path.stationIds.empty()) {
        LOGE("No fastest path found between '%s' and '%s'", sourceNameStr.c_str(), targetNameStr.c_str());
        return nullptr;
    }
    
    LOGI("Fastest path found with %d stations, %d lines, %.2f km, %.2f min", 
        static_cast<int>(path.stationIds.size()), static_cast<int>(path.lineIds.size()), 
        path.totalDistance, path.totalTime);
    
    // Convert to Java object
    return createJavaMetroPath(env, path);
}

JNIEXPORT jboolean JNICALL
Java_com_example_opendelhitransit_data_native_MetroNativeLib_releaseResources(JNIEnv* env, jobject thiz) {
    LOGI("Releasing native resources");
    
    try {
        gMetroGraph.reset();
        gPathFinder.reset();
        
        LOGI("Resources released successfully");
        return JNI_TRUE;
    } catch (const std::exception& e) {
        LOGE("Exception in releaseResources: %s", e.what());
        return JNI_FALSE;
    }
}

} // extern "C" 
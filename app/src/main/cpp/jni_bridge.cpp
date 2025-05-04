#include "jni_bridge.h"
#include <android/log.h>
#include <algorithm>
#include <cctype>
#include <string>

#define LOG_TAG "MetroNative"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Helper function to get the line color name (part before the space)
static std::string getLineColorName(const std::string& lineName);

// Helper function to check if two lines represent a real interchange
bool isRealInterchange(std::unique_ptr<MetroGraph>& graph, int prevLineId, int currentLineId);

// Global instances
std::unique_ptr<MetroGraph> gMetroGraph;
std::unique_ptr<MetroPathFinder> gPathFinder;

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
        LOGI("Metro graph initialized successfully");
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
    
    // Find shortest path
    MetroPath path = gPathFinder->findShortestPath(sourceId, targetId);
    
    // Convert to Java object
    return createJavaMetroPath(env, path);
}

JNIEXPORT jobject JNICALL
Java_com_example_opendelhitransit_data_native_MetroNativeLib_findFastestPathNative(JNIEnv* env, jobject thiz, jint sourceId, jint targetId) {
    if (!gPathFinder) {
        LOGE("Path finder not initialized");
        return nullptr;
    }
    
    // Find fastest path
    MetroPath path = gPathFinder->findFastestPath(sourceId, targetId);
    
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
    
    LOGI("Finding shortest path from '%s' to '%s'", sourceNameStr.c_str(), targetNameStr.c_str());
    
    // Release Java strings
    env->ReleaseStringUTFChars(sourceName, sourceNameChars);
    env->ReleaseStringUTFChars(targetName, targetNameChars);
    
    // Find shortest path
    MetroPath path = gPathFinder->findShortestPath(sourceNameStr, targetNameStr);
    
    if (path.stationIds.empty()) {
        LOGE("No path found between '%s' and '%s'", sourceNameStr.c_str(), targetNameStr.c_str());
        return nullptr;
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
    
    if (path.stationIds.empty()) {
        LOGE("No path found between '%s' and '%s'", sourceNameStr.c_str(), targetNameStr.c_str());
        return nullptr;
    }
    
    // Convert to Java object
    return createJavaMetroPath(env, path);
}

JNIEXPORT jobjectArray JNICALL
Java_com_example_opendelhitransit_data_native_MetroNativeLib_getAllStationNamesNative(JNIEnv* env, jobject thiz) {
    if (!gMetroGraph) {
        LOGE("Metro graph not initialized");
        return nullptr;
    }
    
    // Get all station IDs
    std::vector<int> stationIds = gMetroGraph->getAllStationIds();
    
    // Create Java string array
    jobjectArray result = env->NewObjectArray(stationIds.size(), 
                                             env->FindClass("java/lang/String"), 
                                             env->NewStringUTF(""));
    
    // Fill array with station names
    for (size_t i = 0; i < stationIds.size(); i++) {
        const MetroStation* station = gMetroGraph->getStation(stationIds[i]);
        if (station) {
            jstring stationName = env->NewStringUTF(station->name.c_str());
            env->SetObjectArrayElement(result, i, stationName);
            env->DeleteLocalRef(stationName);
        }
    }
    
    return result;
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

// Helper function to convert MetroPath to Java object
jobject createJavaMetroPath(JNIEnv* env, const MetroPath& path) {
    // Find MetroPath class
    jclass metroPathClass = env->FindClass("com/example/opendelhitransit/data/model/MetroPath");
    if (!metroPathClass) {
        LOGE("Failed to find MetroPath class");
        return nullptr;
    }
    
    // Find constructor
    jmethodID constructor = env->GetMethodID(metroPathClass, "<init>", "(Ljava/util/List;Ljava/util/List;Ljava/util/List;DDI)V");
    if (!constructor) {
        LOGE("Failed to find MetroPath constructor");
        return nullptr;
    }
    
    // Create ArrayLists for stations, lines, and interchanges
    jclass arrayListClass = env->FindClass("java/util/ArrayList");
    jmethodID arrayListConstructor = env->GetMethodID(arrayListClass, "<init>", "()V");
    jmethodID arrayListAdd = env->GetMethodID(arrayListClass, "add", "(Ljava/lang/Object;)Z");
    
    // Create stations ArrayList
    jobject stationsList = env->NewObject(arrayListClass, arrayListConstructor);
    for (int stationId : path.stationIds) {
        // Get station name
        const MetroStation* station = gMetroGraph->getStation(stationId);
        std::string stationName;
        
        if (station) {
            stationName = station->name;
            LOGI("Added station: %s", station->name.c_str());
        } else {
            stationName = std::to_string(stationId);
            LOGI("Added unknown station ID: %d", stationId);
        }
        
        jstring jStationName = env->NewStringUTF(stationName.c_str());
        env->CallBooleanMethod(stationsList, arrayListAdd, jStationName);
        env->DeleteLocalRef(jStationName);
    }
    
    // Create lines ArrayList
    jobject linesList = env->NewObject(arrayListClass, arrayListConstructor);
    for (int lineId : path.lineIds) {
        // Get line name
        const MetroLine* line = gMetroGraph->getLine(lineId);
        std::string lineName;
        
        if (line) {
            lineName = line->name;
            LOGI("Added line: %s", line->name.c_str());
        } else {
            lineName = std::to_string(lineId);
            LOGI("Added unknown line ID: %d", lineId);
        }
        
        jstring jLineName = env->NewStringUTF(lineName.c_str());
        env->CallBooleanMethod(linesList, arrayListAdd, jLineName);
        env->DeleteLocalRef(jLineName);
    }
    
    // Create interchanges ArrayList
    jobject interchangesList = env->NewObject(arrayListClass, arrayListConstructor);
    if (path.stationIds.size() > 1 && path.lineIds.size() > 0) {
        int prevLineId = path.lineIds[0];
        
        // Start from index 1 since we're comparing with previous
        for (size_t i = 1; i < path.lineIds.size(); i++) {
            int currentLineId = path.lineIds[i];
            
            // If line changes, add the corresponding station as an interchange
            if (isRealInterchange(gMetroGraph, prevLineId, currentLineId)) {
                int stationId = path.stationIds[i];
                
                // Get station name for the interchange
                const MetroStation* station = gMetroGraph->getStation(stationId);
                std::string stationName;
                
                if (station) {
                    stationName = station->name;
                    LOGI("Added interchange at station: %s", station->name.c_str());
                } else {
                    stationName = std::to_string(stationId);
                    LOGI("Added interchange at unknown station ID: %d", stationId);
                }
                
                jstring jStationName = env->NewStringUTF(stationName.c_str());
                env->CallBooleanMethod(interchangesList, arrayListAdd, jStationName);
                env->DeleteLocalRef(jStationName);
            }
            
            prevLineId = currentLineId;
        }
    }
    
    // Create the MetroPath object with the lists and data
    jobject result = env->NewObject(metroPathClass, constructor, 
                                   stationsList, linesList, interchangesList, 
                                   path.totalDistance, path.totalTime, 
                                   path.interchangeCount);
    
    // Clean up local references
    env->DeleteLocalRef(stationsList);
    env->DeleteLocalRef(linesList);
    env->DeleteLocalRef(interchangesList);
    
    return result;
}

} // extern "C" 

// Helper function to get the line color name (part before the space)
static std::string getLineColorName(const std::string& lineName) {
    // Find the first space
    size_t spacePos = lineName.find(' ');
    if (spacePos != std::string::npos) {
        // Return part before space, converted to lowercase
        std::string colorName = lineName.substr(0, spacePos);
        std::transform(colorName.begin(), colorName.end(), colorName.begin(),
                      [](unsigned char c){ return std::tolower(c); });
        return colorName;
    }
    return lineName; // Return the whole name if no space found
}

// Helper function to check if two lines represent a real interchange
bool isRealInterchange(std::unique_ptr<MetroGraph>& graph, int prevLineId, int currentLineId) {
    // If they have the same ID, they're definitely the same line
    if (prevLineId == currentLineId) {
        return false;
    }
    
    // Get the line objects
    const MetroLine* prevLine = graph->getLine(prevLineId);
    const MetroLine* currentLine = graph->getLine(currentLineId);
    
    // If either line doesn't exist, consider it an interchange
    if (!prevLine || !currentLine) {
        return true;
    }
    
    // Get color parts of the names (e.g., "magenta" from "Magenta Line")
    std::string prevColor = getLineColorName(prevLine->name);
    std::string currentColor = getLineColorName(currentLine->name);
    
    // It's a real interchange only if the color parts are different
    return prevColor != currentColor;
} 
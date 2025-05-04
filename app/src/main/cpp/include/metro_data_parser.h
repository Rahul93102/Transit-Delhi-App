#ifndef METRO_DATA_PARSER_H
#define METRO_DATA_PARSER_H

#include "metro_graph.h"
#include <string>
#include <fstream>
#include <sstream>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>

// Class responsible for parsing GTFS data and populating the Metro Graph
class MetroDataParser {
private:
    MetroGraph& graph;
    AAssetManager* assetManager;
    
    // Internal parsing functions
    void parseStops();
    void parseRoutes();
    void parseTripData();
    
    // Helper function to read a file from assets
    std::string readFileFromAssets(const std::string& filename);
    
    // Calculate distance between two geographic points (Haversine formula)
    double calculateDistance(double lat1, double lon1, double lat2, double lon2);
    
    // Calculate time difference between two GTFS time strings (format HH:MM:SS)
    double calculateTimeDifference(const std::string& startTime, const std::string& endTime);

public:
    // Constructor
    MetroDataParser(MetroGraph& metroGraph, AAssetManager* manager)
        : graph(metroGraph), assetManager(manager) {}
    
    // Parse all GTFS data and build the metro graph
    bool parseGTFSData();
};

#endif // METRO_DATA_PARSER_H 
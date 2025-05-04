#ifndef METRO_GRAPH_H
#define METRO_GRAPH_H

#include <string>
#include <vector>
#include <unordered_map>
#include <memory>

// Metro station struct to store all station details
struct MetroStation {
    int id;
    std::string code;
    std::string name;
    double latitude;
    double longitude;
    
    // Default constructor required for std::unordered_map
    MetroStation() : id(0), latitude(0), longitude(0) {}
    
    // Constructor
    MetroStation(int id, std::string code, std::string name, double lat, double lon) 
        : id(id), code(std::move(code)), name(std::move(name)), latitude(lat), longitude(lon) {}
};

// Metro line struct to represent a line in the network
struct MetroLine {
    int id;
    std::string name;
    std::string color;
    
    // Default constructor required for std::unordered_map
    MetroLine() : id(0) {}
    
    // Constructor
    MetroLine(int id, std::string name, std::string color) 
        : id(id), name(std::move(name)), color(std::move(color)) {}
};

// Edge struct to represent connection between stations
struct MetroEdge {
    int sourceId;
    int targetId;
    int lineId;
    double distance;     // Distance in km
    double time;         // Time in minutes
    
    // Constructor
    MetroEdge(int src, int tgt, int line, double dist, double t) 
        : sourceId(src), targetId(tgt), lineId(line), distance(dist), time(t) {}
};

// Path struct to represent a path in the network
struct MetroPath {
    std::vector<int> stationIds;
    std::vector<int> lineIds;
    double totalDistance;
    double totalTime;
    int interchangeCount;
    
    // Default constructor
    MetroPath() : totalDistance(0), totalTime(0), interchangeCount(0) {}
};

// Metro Graph class representing the entire metro network
class MetroGraph {
private:
    std::unordered_map<int, MetroStation> stations;
    std::unordered_map<int, MetroLine> lines;
    std::unordered_map<int, std::vector<MetroEdge>> adjacencyList;

public:
    // Add a station to the graph
    void addStation(const MetroStation& station);
    
    // Add a line to the graph
    void addLine(const MetroLine& line);
    
    // Add an edge between stations
    void addEdge(const MetroEdge& edge);
    
    // Get station by ID
    const MetroStation* getStation(int id) const;
    
    // Get station by name (might return multiple matches)
    std::vector<const MetroStation*> getStationsByName(const std::string& name) const;
    
    // Get line by ID
    const MetroLine* getLine(int id) const;
    
    // Get all neighbors of a station
    const std::vector<MetroEdge>& getNeighbors(int stationId) const;
    
    // Get all station IDs
    std::vector<int> getAllStationIds() const;
    
    // Clear all data
    void clear();
};

#endif // METRO_GRAPH_H 
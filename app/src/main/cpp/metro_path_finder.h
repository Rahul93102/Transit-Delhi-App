#ifndef METRO_PATH_FINDER_H
#define METRO_PATH_FINDER_H

#include "metro_graph.h"
#include <queue>
#include <limits>
#include <unordered_set>

// Path finder class to find shortest and fastest paths in the metro network
class MetroPathFinder {
private:
    const MetroGraph& graph;
    
    // Helper struct for Dijkstra's algorithm
    struct DijkstraNode {
        int stationId;
        double cost;
        int prevStationId;
        int lineId;
        
        // Constructor
        DijkstraNode(int id, double c, int prev, int line) 
            : stationId(id), cost(c), prevStationId(prev), lineId(line) {}
        
        // Comparison operator for priority queue
        bool operator>(const DijkstraNode& other) const {
            return cost > other.cost;
        }
    };
    
    // Internal function to find path using Dijkstra's algorithm
    MetroPath findPathDijkstra(int sourceId, int targetId, bool useDistance);
    
    // Helper function to reconstruct path from Dijkstra results
    MetroPath reconstructPath(
        int sourceId,
        int targetId,
        const std::unordered_map<int, int>& prevStation,
        const std::unordered_map<int, int>& prevLine,
        double totalCost,
        bool useDistance);

public:
    // Constructor
    explicit MetroPathFinder(const MetroGraph& metroGraph) : graph(metroGraph) {}
    
    // Find shortest path by distance
    MetroPath findShortestPath(int sourceId, int targetId);
    
    // Find fastest path by time
    MetroPath findFastestPath(int sourceId, int targetId);
    
    // Find shortest path by station names
    MetroPath findShortestPath(const std::string& sourceName, const std::string& targetName);
    
    // Find fastest path by station names
    MetroPath findFastestPath(const std::string& sourceName, const std::string& targetName);
};

#endif // METRO_PATH_FINDER_H 
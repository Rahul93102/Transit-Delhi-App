#include "metro_path_finder.h"
#include <algorithm>
#include <string>
#include <cctype>

// Helper function to get the line color name (part before the space)
std::string getLineColorName(const std::string& lineName) {
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

// Helper function to check if two lines are truly different (comparing names rather than just IDs)
bool isRealInterchange(const MetroGraph& graph, int prevLineId, int currentLineId) {
    // If they have the same ID, they're definitely the same line
    if (prevLineId == currentLineId) {
        return false;
    }
    
    // Get the line objects
    const MetroLine* prevLine = graph.getLine(prevLineId);
    const MetroLine* currentLine = graph.getLine(currentLineId);
    
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

MetroPath MetroPathFinder::findPathDijkstra(int sourceId, int targetId, bool useDistance) {
    // Use priority queue for Dijkstra algorithm
    std::priority_queue<DijkstraNode, std::vector<DijkstraNode>, std::greater<>> pq;
    
    // Maps to track the shortest path
    std::unordered_map<int, double> dist;
    std::unordered_map<int, int> prevStation;
    std::unordered_map<int, int> prevLine;
    std::unordered_set<int> visited;
    
    // Initialize all distances as infinite
    for (int id : graph.getAllStationIds()) {
        dist[id] = std::numeric_limits<double>::infinity();
    }
    
    // Distance from source to itself is 0
    dist[sourceId] = 0;
    
    // Push source node to priority queue
    pq.push(DijkstraNode(sourceId, 0, -1, -1));
    
    // Process nodes in priority queue
    while (!pq.empty()) {
        DijkstraNode current = pq.top();
        pq.pop();
        
        int currentId = current.stationId;
        
        // If we've reached the target, we can stop
        if (currentId == targetId) {
            break;
        }
        
        // Skip if already processed
        if (visited.count(currentId) > 0) {
            continue;
        }
        
        visited.insert(currentId);
        
        // Process all neighbors
        for (const auto& edge : graph.getNeighbors(currentId)) {
            int neighborId = edge.targetId;
            double cost = useDistance ? edge.distance : edge.time;
            
            // Add interchange penalty (8 minutes) if switching lines and not optimizing for distance
            // Only add penalty for REAL interchanges (different line colors)
            if (!useDistance && current.prevStationId != -1 && 
                isRealInterchange(graph, current.lineId, edge.lineId)) {
                cost += 8.0; // 8 minute interchange penalty
            }
            
            // If we found a shorter path
            if (dist[neighborId] > dist[currentId] + cost) {
                dist[neighborId] = dist[currentId] + cost;
                prevStation[neighborId] = currentId;
                prevLine[neighborId] = edge.lineId;
                
                // Add to priority queue
                pq.push(DijkstraNode(neighborId, dist[neighborId], currentId, edge.lineId));
            }
        }
    }
    
    // If we couldn't reach the target
    if (dist[targetId] == std::numeric_limits<double>::infinity()) {
        return MetroPath(); // Return empty path
    }
    
    // Reconstruct the path
    return reconstructPath(sourceId, targetId, prevStation, prevLine, dist[targetId], useDistance);
}

MetroPath MetroPathFinder::reconstructPath(
    int sourceId,
    int targetId,
    const std::unordered_map<int, int>& prevStation,
    const std::unordered_map<int, int>& prevLine,
    double totalCost,
    bool useDistance) {
    
    MetroPath path;
    
    // Start from target and work backwards
    int currentId = targetId;
    std::vector<int> stations;
    std::vector<int> lines;
    
    // Build the path in reverse
    while (currentId != sourceId) {
        stations.push_back(currentId);
        
        // Get the line used to reach this station
        int lineId = prevLine.at(currentId);
        lines.push_back(lineId);
        
        // Move to previous station
        currentId = prevStation.at(currentId);
    }
    
    // Add the source station
    stations.push_back(sourceId);
    
    // Reverse the vectors to get correct order
    std::reverse(stations.begin(), stations.end());
    std::reverse(lines.begin(), lines.end());
    
    // Set path data
    path.stationIds = stations;
    path.lineIds = lines;
    
    // Calculate total distance and time
    path.totalDistance = 0;
    path.totalTime = 0;
    path.interchangeCount = 0;
    
    // Count REAL interchanges (line changes between different colored lines)
    int prevLineId = -1;
    for (size_t i = 0; i < lines.size(); i++) {
        int lineId = lines[i];
        if (prevLineId != -1 && isRealInterchange(graph, prevLineId, lineId)) {
            path.interchangeCount++;
        }
        prevLineId = lineId;
    }
    
    // Set the total cost based on what we optimized for
    if (useDistance) {
        path.totalDistance = totalCost;
        
        // Calculate time
        for (size_t i = 0; i < stations.size() - 1; i++) {
            for (const auto& edge : graph.getNeighbors(stations[i])) {
                if (edge.targetId == stations[i + 1] && edge.lineId == lines[i]) {
                    path.totalTime += edge.time;
                    break;
                }
            }
        }
        
        // Add 8 minutes for each REAL interchange
        path.totalTime += path.interchangeCount * 8.0;
    } else {
        // When optimizing for time, totalCost already includes the interchange penalties
        path.totalTime = totalCost;
        
        // Calculate distance
        for (size_t i = 0; i < stations.size() - 1; i++) {
            for (const auto& edge : graph.getNeighbors(stations[i])) {
                if (edge.targetId == stations[i + 1] && edge.lineId == lines[i]) {
                    path.totalDistance += edge.distance;
                    break;
                }
            }
        }
    }
    
    return path;
}

MetroPath MetroPathFinder::findShortestPath(int sourceId, int targetId) {
    return findPathDijkstra(sourceId, targetId, true);
}

MetroPath MetroPathFinder::findFastestPath(int sourceId, int targetId) {
    return findPathDijkstra(sourceId, targetId, false);
}

MetroPath MetroPathFinder::findShortestPath(const std::string& sourceName, const std::string& targetName) {
    auto sourceStations = graph.getStationsByName(sourceName);
    auto targetStations = graph.getStationsByName(targetName);
    
    if (sourceStations.empty() || targetStations.empty()) {
        return MetroPath(); // Return empty path if stations not found
    }
    
    // Use first matching station for simplicity
    // In a real app, you might want to ask user to choose if multiple matches
    return findShortestPath(sourceStations[0]->id, targetStations[0]->id);
}

MetroPath MetroPathFinder::findFastestPath(const std::string& sourceName, const std::string& targetName) {
    auto sourceStations = graph.getStationsByName(sourceName);
    auto targetStations = graph.getStationsByName(targetName);
    
    if (sourceStations.empty() || targetStations.empty()) {
        return MetroPath(); // Return empty path if stations not found
    }
    
    // Use first matching station for simplicity
    return findFastestPath(sourceStations[0]->id, targetStations[0]->id);
} 
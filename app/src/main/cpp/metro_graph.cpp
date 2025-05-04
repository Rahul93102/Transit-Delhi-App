#include "metro_graph.h"
#include <algorithm>
#include <vector>

void MetroGraph::addStation(const MetroStation& station) {
    stations[station.id] = station;
}

void MetroGraph::addLine(const MetroLine& line) {
    lines[line.id] = line;
}

void MetroGraph::addEdge(const MetroEdge& edge) {
    adjacencyList[edge.sourceId].push_back(edge);
}

const MetroStation* MetroGraph::getStation(int id) const {
    auto it = stations.find(id);
    if (it != stations.end()) {
        return &(it->second);
    }
    return nullptr;
}

std::vector<const MetroStation*> MetroGraph::getStationsByName(const std::string& name) const {
    std::vector<const MetroStation*> result;
    
    // Convert search name to lowercase for case-insensitive comparison
    std::string searchName = name;
    std::transform(searchName.begin(), searchName.end(), searchName.begin(), 
                  [](unsigned char c){ return std::tolower(c); });
    
    for (const auto& pair : stations) {
        std::string stationName = pair.second.name;
        std::transform(stationName.begin(), stationName.end(), stationName.begin(), 
                      [](unsigned char c){ return std::tolower(c); });
        
        if (stationName.find(searchName) != std::string::npos) {
            result.push_back(&pair.second);
        }
    }
    
    return result;
}

const MetroLine* MetroGraph::getLine(int id) const {
    auto it = lines.find(id);
    if (it != lines.end()) {
        return &(it->second);
    }
    return nullptr;
}

const std::vector<MetroEdge>& MetroGraph::getNeighbors(int stationId) const {
    static const std::vector<MetroEdge> emptyVector;
    auto it = adjacencyList.find(stationId);
    if (it != adjacencyList.end()) {
        return it->second;
    }
    return emptyVector;
}

std::vector<int> MetroGraph::getAllStationIds() const {
    std::vector<int> ids;
    ids.reserve(stations.size());
    
    for (const auto& pair : stations) {
        ids.push_back(pair.first);
    }
    
    return ids;
}

void MetroGraph::clear() {
    stations.clear();
    lines.clear();
    adjacencyList.clear();
} 
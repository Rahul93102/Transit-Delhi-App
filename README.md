# Open Delhi Transit App

A comprehensive Delhi transit application that integrates metro information, journey planning, and real-time transit tracking, and maps into a unified experience. This app helps users navigate Delhi's public transportation system efficiently.

## Features

- **Metro Information**: Search for Delhi metro stations, view line information, and find the best routes between stations.
- **Journey Planner**: Plan your trip across Delhi using various transportation options including metro, and bus.
- **User-Friendly Interface**: Modern UI built with Jetpack Compose for a seamless user experience with bottom navigation.

## Features and Implementation Approaches

### Accessibility Features

- **High Contrast UI**: The app uses Material Design 3 with carefully selected color schemes to ensure good contrast ratios, making content readable for users with visual impairments.
- **Text Scaling**: All text elements use scalable typography (sp units) that respect user device settings for font size.

### Sensing Capabilities

- **Location-Based Services**: Utilizes device GPS to provide nearby fuel station recommendations and real-time location tracking.
- **Network Connectivity Awareness**: Detects network changes to adjust functionality between online and offline modes.

### Unique UI Elements

- **Interactive Metro Line Visualization**: Custom implementation of metro lines with proper color coding and interchange indicators that go beyond standard Android UI components.
- **Station Path Visualization**: Distinctive visual representation of metro routes with:

  - Color-coded line indicators
  - Interchange highlighting with special icons
  - Sequential numbering of stations
  - Visual connectors between stations

- **Dynamic Search Interface**: Custom search implementation with real-time filtering and intuitive selection mechanism for source and destination stations.
- **Journey Details Card**: Specialized card layout that provides comprehensive route information including:
  - Total station count
  - Number of interchanges
  - Complete station-by-station path with line changes

### Native API Integration

- **Jetpack Compose**: Built entirely with Android's modern declarative UI toolkit for a more maintainable and performant interface.
- **Hilt Dependency Injection**: Leverages Hilt for dependency management throughout the application.
- **Kotlin Coroutines and Flow**: Utilizes Kotlin's native concurrency solutions for asynchronous operations and reactive data handling.
- **ViewModel Architecture**: Implements the MVVM pattern using Android Architecture Components.
- **Material Design 3**: Adopts the latest Material Design guidelines with dynamic theming capabilities.
- **StateFlow for UI State Management**: Uses Kotlin's StateFlow for reactive UI updates and state management.
- **Path Finding Algorithm**: Uses Native C++ code and to find shotest path for metros.

## Implementation Highlights

The app demonstrates advanced implementation techniques particularly in the route display functionality:

- The `RouteDetails` composable in `MetroScreen.kt` provides a comprehensive journey visualization with:

  - Source and destination station details
  - Summary statistics (total stations and interchanges)
  - Two different representations of the complete route:
    1. A simplified list with numbering and interchange indicators
    2. A detailed view with line colors and comprehensive interchange information

- Each station in the path is displayed with:
  - Station name
  - Line information with appropriate color coding
  - Sequential numbering
  - Visual indicators for line changes

This implementation provides users with a rich, intuitive understanding of their metro journey beyond what standard components offer.

## Architecture

The app is built using modern Android development patterns:

- **MVVM Architecture**: Clean separation of UI, business logic, and data layers.
- **Jetpack Compose**: Modern UI toolkit for building native Android UI.
- **Hilt**: Dependency injection for better code organization and testability.
- **Navigation Component**: For handling navigation between different screens.

## Integration

This app successfully integrates three previously separate projects:

1. **Open Delhi Transit App**: Provides Delhi metro and public transport information.
2. **MyApplication**: Provides route search and find station functionality.

All features are now accessible from a single app with a unified navigation system and consistent UI design.

## Project Structure

- **app/**: Main application module
  - **src/main/java/com/example/opendelhitransit/**
    - **data/**: Data models and repository classes
    - **di/**: Dependency injection modules
    - **features/**: Feature-specific components
      - **home/**: Home screen with links to all features
      - **metro/**: Metro information screens with route planning
      - **transitapp/**: Journey planning screens for multiple transport modes
    - **util/**: Utility classes for JSON and CSV parsing
    - **viewmodel/**: ViewModels for managing UI state

## Setup Instructions

1. Clone the repository:

   ```
   git clone https://github.com/Rahul93102/Transit-Delhi-App.git
   ```

2. Open the project in Android Studio (Arctic Fox or later)

3. Make sure you have the following installed:

   - JDK 17
   - Android SDK 34
   - Gradle 8.2 or higher

4. Build and run the app:
   - Connect your Android device or use an emulator
   - Click the Run button in Android Studio

## Usage

1. **Home Screen**: The app opens to the Home screen with cards for each main feature.

   - Click on any feature card to navigate directly to that feature.
   - Use the bottom navigation bar to switch between main sections.

2. **Metro Screen**: Access Delhi Metro information

   - Search for stations by name
   - Plan metro routes between stations
   - View estimated travel time and interchanges
   - Interactive metro map with color-coded lines and stations

3. **Real-time Transit**: Track public transport in real-time

   - View live locations of transit vehicles
   - See vehicle route IDs, speeds, and timestamps
   - Toggle auto-refresh for continuous updates
   - Open detailed map view to visualize all transit vehicles

4. **Fuel Screen**: Find fuel stations near you

   - Location-based fuel station recommendations
   - Get directions to your selected fuel station from google maps

5. **Settings**: Customize your app experience

   - Toggle between light and dark themes
   - Adjust map and navigation preferences

## Dependencies

- Jetpack Compose
- Hilt (Dependency Injection)
- Gson (JSON parsing)
- OpenCSV (CSV parsing)
- ConstraintLayout
- Material Design Components
- Google Maps for mapping functionality
- Kotlin Coroutines for asynchronous operations

## Implementation Notes

### Metro Data

Metro line and station data is stored in the app's assets folder:

- JSON files for each metro line (yellow.json, blue.json, etc.)
- DELHI_METRO_DATA.csv for comprehensive station information

### Transit Planning

The transit planning feature combines multiple data sources:

- Metro routes and stations
- Walking distance estimation
- Fare calculation

## Permissions

The app requires the following permissions:

- `INTERNET`: For fetching any online transit data

## Troubleshooting

- **Build Issues**: Make sure you're using JDK 17 and have the latest Android Gradle Plugin
- **Metro Data Not Loading**: Check that the assets folder is properly included in the build

### Future Improvements

- Improve route finding algorithm for more accurate journey planning
- Add real-time metro schedule information
- Enhance UI with animations and transitions
- Add user accounts for saving favorite routes and stations

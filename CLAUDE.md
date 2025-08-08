# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Android application demonstrating Google Places SDK Autocomplete integration with Jetpack Compose. The app provides location search functionality with India-specific location bias.

## Development Commands

### Build & Run
```bash
# Build the project
./gradlew build

# Clean build
./gradlew clean build

# Install debug APK on connected device/emulator
./gradlew installDebug

# Run the app on connected device/emulator
./gradlew installDebug && adb shell am start -n com.example.places.autocomplete/.MainActivity
```

### Testing
```bash
# Run unit tests
./gradlew test

# Run instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest

# Run all tests
./gradlew test connectedAndroidTest

# Run tests for specific module
./gradlew :app:test
./gradlew :core-network:test
```

### Code Quality
```bash
# Run lint checks
./gradlew lint

# Run lint and generate HTML report
./gradlew lintDebug

# Check for deprecated APIs
./gradlew build -Xlint:deprecation
```

## Architecture

### Module Structure
- **app**: Main application module containing UI components and Places SDK integration
  - Uses Jetpack Compose for UI with Material3 design system
  - Implements Hilt for dependency injection (@HiltAndroidApp, @AndroidEntryPoint)
  - Places SDK initialization happens in MainActivity with API key from BuildConfig
  
- **core-network**: Network layer module (currently scaffolded for future API integrations)
  - Contains Retrofit/OkHttp dependencies for potential geocoding or custom API calls
  - NetworkModule prepared for Dagger/Hilt providers

### Key Dependencies & Versions
- **Kotlin**: 2.2.0 with Compose compiler
- **Android Gradle Plugin**: 8.12.0
- **Compose BOM**: 2025.07.00
- **Google Places SDK**: 4.4.1 with places-compose 0.1.3
- **Hilt**: 2.57 for dependency injection
- **Min SDK**: 28 / Target SDK: 36

### Layered Architecture
```
┌─────────────────────────────────────┐
│         Presentation Layer          │
│    (Compose UI + ViewModels)        │
├─────────────────────────────────────┤
│          Domain Layer               │
│    (Use Cases + Domain Models)      │
├─────────────────────────────────────┤
│           Data Layer                │
│  (Repositories + Data Sources)      │
└─────────────────────────────────────┘
```

### Unidirectional Data Flow (UDF)
```kotlin
// ViewModel emits state
data class SearchUiState(
    val query: String = "",
    val results: List<Place> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

// UI observes state
val uiState by viewModel.uiState.collectAsStateWithLifecycle()

// User actions trigger events
viewModel.onSearchQueryChanged(query)

// ViewModel processes events and updates state
fun onSearchQueryChanged(query: String) {
    _uiState.update { it.copy(query = query) }
    searchPlaces(query)
}
```

### API Key Configuration
The project uses the Secrets Gradle Plugin for API key management:
1. API keys are stored in `secrets.properties` (not in version control)
2. Default values in `local.defaults.properties` 
3. Keys are injected as BuildConfig fields at compile time
4. Access via `BuildConfig.MAPS_API_KEY`

### Location Bias Implementation
The app implements India-specific location bias using RectangularBounds:
- Southwest: 8.4°N, 68.7°E (Southern India)
- Northeast: 35.5°N, 97.25°E (Northern Kashmir to Eastern border)
- Filters for administrative areas (levels 1-5) to capture cities, districts, villages

### State Management Pattern
- Uses Kotlin Flow with debouncing (500ms) for search queries
- `collectAsStateWithLifecycle()` for Compose state management
- MutableStateFlow for reactive search text updates

## Git Commit Guidelines

For detailed commit message conventions, refer to the team's shared guidelines:
`C:\Users\bhave\TrucksG\trucksG-android\.github\git-commit-instructions.md`

Key points:
- Follow Conventional Commits format: `<type>(<scope>): <subject>`
- Use imperative mood in subject line
- Include body for complex changes with bullet points
- Reference flavors when applicable
- Use backticks for file names
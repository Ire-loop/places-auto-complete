# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Android application demonstrating Google Places SDK Autocomplete integration with Jetpack Compose. The app provides location search functionality with India-specific location bias and custom geocoding capabilities.

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
./gradlew :core-domain:test

# Run network-specific tests
./gradlew :core-network:test --tests "*NetworkTest"
./gradlew :core-domain:test --tests "*RepositoryTest"
```

### Code Quality
```bash
# Run lint checks
./gradlew lint

# Run lint and generate HTML report
./gradlew lintDebug

# Check for deprecated APIs
./gradlew build -Xlint:deprecation

# Check network module specifically
./gradlew :core-network:lint
```

## Architecture

### Module Structure
- **app**: Main application module containing UI components and Places SDK integration
  - Uses Jetpack Compose for UI with Material3 design system
  - Implements Hilt for dependency injection (@HiltAndroidApp, @AndroidEntryPoint)
  - Places SDK initialization happens in MainActivity with API key from BuildConfig

- **core-network**: Network layer module for API communications
  - Retrofit 2.9.0 for REST API calls
  - OkHttp 4.12.0 as HTTP client with interceptors
  - Custom UserAgentInterceptor for headers
  - Scalars converter for HTML response parsing
  - Configured timeouts (15 seconds for connect/read/write)

- **core-domain**: Business logic and repository implementations
  - Repository pattern for data access abstraction
  - GoogleMapsHtmlParser for extracting coordinates from HTML
  - Result sealed class for error handling
  - Coroutines Flow for reactive data streams

### Key Dependencies & Versions
- **Kotlin**: 2.2.0 with Compose compiler
- **Android Gradle Plugin**: 8.12.0
- **Compose BOM**: 2025.07.00
- **Google Places SDK**: 4.4.1 with places-compose 0.1.3
- **Hilt**: 2.57 for dependency injection
- **Retrofit**: 2.9.0 with Scalars converter
- **OkHttp**: 4.12.0 with logging interceptor
- **Coroutines**: 1.7.3 for async operations
- **Min SDK**: 28 / Target SDK: 36

### Layered Architecture
```
┌─────────────────────────────────────┐
│         Presentation Layer          │
│    (Compose UI + ViewModels)        │
├─────────────────────────────────────┤
│          Domain Layer               │
│  (Use Cases + Repositories + Models)│
├─────────────────────────────────────┤
│           Data Layer                │
│  (Network APIs + Data Sources)      │
└─────────────────────────────────────┘
```

## Networking

### Network Architecture Overview
The application uses a multi-layered networking approach with Retrofit and OkHttp for external API calls, specifically designed for geocoding addresses through Google Maps HTML parsing.

### Retrofit Configuration
```kotlin
// core-network/NetworkModule.kt
@Provides
@Singleton
fun provideGeocodingRetrofit(okHttpClient: OkHttpClient): Retrofit {
    return Retrofit.Builder()
        .baseUrl("https://www.google.com/")
        .client(okHttpClient)
        .addConverterFactory(ScalarsConverterFactory.create()) // For HTML responses
        .build()
}
```

### OkHttp Client Setup
```kotlin
// Configured with:
// - 15-second timeouts for all operations
// - Custom User-Agent for Google Maps compatibility
// - HTTP logging in debug builds
// - Automatic redirect following
@Provides
@Singleton
fun provideOkHttpClient(
    loggingInterceptor: HttpLoggingInterceptor,
    userAgentInterceptor: UserAgentInterceptor
): OkHttpClient {
    return OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(userAgentInterceptor)
        .addInterceptor(loggingInterceptor)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()
}
```

### Repository Pattern Implementation
```kotlin
// Domain layer repository interface
interface GeocodingRepository {
    suspend fun geocodeAddress(address: String): Result<GeocodedLocation>
    fun geocodeAddressFlow(address: String): Flow<Result<GeocodedLocation>>
}

// Implementation with error handling and fallback strategies
class GeocodingRepositoryImpl @Inject constructor(
    private val geocodingApi: GeocodingApi,
    private val htmlParser: GoogleMapsHtmlParser
) : GeocodingRepository {
    // Primary method tries /maps/place/{address}
    // Fallback method tries /maps/search/{address}
    // Returns Result.Success or Result.Error
}
```

### Geocoding API Endpoints
```kotlin
interface GeocodingApi {
    @GET("maps/place/{encodedPlace}")
    suspend fun getPlaceDetails(@Path("encodedPlace") encodedPlace: String): Response<String>
    
    @GET("maps/search/{encodedPlace}")
    suspend fun searchPlace(@Path("encodedPlace") encodedPlace: String): Response<String>
    
    @GET
    suspend fun getPlaceFromUrl(@Url url: String): Response<String>
}
```

### HTML Parser for Coordinate Extraction
The `GoogleMapsHtmlParser` uses multiple extraction strategies:
1. **JSON Pattern**: `[null,null,[lat,lng]]` format
2. **Validated Coordinates**: Decimal format with range validation
3. **Viewport Bounds**: Calculate center from bounds
4. **URL Patterns**: Extract from `@lat,lng` in URLs
5. **Static Map**: Parse from staticmap query parameters

### Error Handling Strategy
```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

// In ViewModel
viewModel.geocodingState.collect { state ->
    when (state) {
        is GeocodingUiState.Loading -> showProgressBar()
        is GeocodingUiState.Success -> updateMap(state.location)
        is GeocodingUiState.Error -> showError(state.message)
    }
}
```

### Network Request Flow
```
User Input → ViewModel → Repository → API → HTML Parser → Result
     ↑                                                         ↓
     ←────────────────── UI State Update ←────────────────────
```

### Testing Network Code
```kotlin
// Unit test example for repository
@Test
fun `geocodeAddress returns valid coordinates for known address`() = runTest {
    // Given
    val address = "Thrissur, Kerala, India"
    val expectedHtml = loadTestHtml("thrissur_response.html")
    coEvery { api.getPlaceDetails(any()) } returns Response.success(expectedHtml)
    
    // When
    val result = repository.geocodeAddress(address)
    
    // Then
    assertTrue(result is Result.Success)
    assertEquals(10.5276, result.data.latitude, 0.001)
    assertEquals(76.2144, result.data.longitude, 0.001)
}

// Integration test with MockWebServer
@Test
fun `real network call handles timeout gracefully`() = runTest {
    // Setup MockWebServer with delay
    server.enqueue(MockResponse().setBodyDelay(20, TimeUnit.SECONDS))
    
    // Execute and verify timeout error
    val result = repository.geocodeAddress("test")
    assertTrue(result is Result.Error)
}
```

### Common Network Debugging Commands
```bash
# Monitor network traffic with Charles Proxy
adb shell settings put global http_proxy <host_ip>:8888

# Clear proxy settings
adb shell settings delete global http_proxy

# Check OkHttp cache
adb shell run-as com.example.places.autocomplete ls cache/

# View network logs
adb logcat | grep "OkHttp"

# Test API endpoint with curl
curl -H "User-Agent: Mozilla/5.0" "https://www.google.com/maps/place/Thrissur"
```

### Network Security Configuration
```xml
<!-- res/xml/network_security_config.xml -->
<network-security-config>
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="true">google.com</domain>
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </domain-config>
    
    <!-- Debug-only config for Charles Proxy -->
    <debug-overrides>
        <trust-anchors>
            <certificates src="user" />
        </trust-anchors>
    </debug-overrides>
</network-security-config>
```

### ProGuard/R8 Rules for Networking
```pro
# Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# OkHttp
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# Keep models
-keep class com.yourapp.core.domain.model.** { *; }
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

## Troubleshooting

### Network Issues
- **Timeout errors**: Check internet connectivity and increase timeout values in OkHttpClient
- **SSL errors**: Verify network_security_config.xml and certificate pinning
- **Parsing errors**: Enable OkHttp logging to inspect raw HTML responses
- **Rate limiting**: Implement exponential backoff and retry logic

### Geocoding Issues
- **Invalid coordinates**: Verify coordinate validation logic (lat: -90 to 90, lng: -180 to 180)
- **Missing results**: Try fallback endpoints and check HTML parser patterns
- **Encoding issues**: Ensure proper URL encoding with `URLEncoder.encode(address, "UTF-8")`
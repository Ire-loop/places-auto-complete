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
./gradlew :core-data:test

# Run network-specific tests
./gradlew :core-network:test --tests "*NetworkTest"
./gradlew :core-domain:test --tests "*RepositoryTest"

# Run data layer tests
./gradlew :core-data:test --tests "*DaoTest"
./gradlew :core-data:connectedAndroidTest
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

# Check data module
./gradlew :core-data:lint
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

- **core-data**: Data persistence and caching layer
  - Room database for local storage of places and search history
  - DataStore for user preferences and settings
  - Repository implementations bridging network and local data sources
  - Data mappers for entity transformations
  - Caching strategies with TTL (Time To Live) management
  - Android Keystore integration for sensitive data encryption

### Key Dependencies & Versions
- **Kotlin**: 2.2.0 with Compose compiler
- **Android Gradle Plugin**: 8.12.0
- **Compose BOM**: 2025.07.00
- **Google Places SDK**: 4.4.1 with places-compose 0.1.3
- **Hilt**: 2.57 for dependency injection
- **Retrofit**: 2.9.0 with Scalars converter
- **OkHttp**: 4.12.0 with logging interceptor
- **Coroutines**: 1.7.3 for async operations
- **Room**: 2.6.1 for database
- **DataStore**: 1.0.0 for preferences
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
│  ┌─────────────────────────────────┐│
│  │     core-data module            ││
│  │  - Room Database                ││
│  │  - DataStore Preferences        ││
│  │  - Repository Implementations   ││
│  │  - Data Mappers                 ││
│  │  - Caching Logic                ││
│  └─────────────────────────────────┘│
└─────────────────────────────────────┘
```

## Core-Data Module

### Module Overview
The `core-data` module is responsible for all data persistence, caching, and concrete repository implementations. It acts as the single source of truth for the application's data, coordinating between network and local storage.

### Module Structure
```
core-data/
├── src/main/java/com/places/autocomplete/core/data/
│   ├── database/
│   │   ├── PlacesDatabase.kt          # Room database definition
│   │   ├── dao/
│   │   │   ├── PlacesDao.kt           # DAO for places operations
│   │   │   └── SearchHistoryDao.kt    # DAO for search history
│   │   ├── entities/
│   │   │   ├── PlaceEntity.kt         # Database entity for places
│   │   │   ├── SearchHistoryEntity.kt # Database entity for search history
│   │   │   └── CachedLocationEntity.kt # Cached geocoding results
│   │   └── converters/
│   │       └── Converters.kt          # Type converters for Room
│   ├── datastore/
│   │   ├── UserPreferencesSerializer.kt # Proto DataStore serializer
│   │   ├── PreferencesRepository.kt     # DataStore repository
│   │   └── proto/                       # Protocol buffer definitions
│   ├── repository/
│   │   ├── PlacesRepositoryImpl.kt     # Concrete repository implementation
│   │   ├── GeocodingRepositoryImpl.kt  # Geocoding with caching
│   │   └── SearchHistoryRepositoryImpl.kt # Search history management
│   ├── mappers/
│   │   ├── PlaceMapper.kt              # Entity <-> Domain model mapping
│   │   └── LocationMapper.kt           # Location data transformations
│   ├── di/
│   │   └── DataModule.kt               # Hilt dependency injection
│   └── util/
│       ├── CacheManager.kt             # Cache invalidation logic
│       └── EncryptionHelper.kt         # Android Keystore encryption
└── build.gradle.kts
```

### Room Database Configuration
```kotlin
// core-data/database/PlacesDatabase.kt
@Database(
    entities = [
        PlaceEntity::class,
        SearchHistoryEntity::class,
        CachedLocationEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class PlacesDatabase : RoomDatabase() {
    abstract fun placesDao(): PlacesDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    
    companion object {
        const val DATABASE_NAME = "places_database"
    }
}

// Dependency injection setup
@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PlacesDatabase {
        return Room.databaseBuilder(
            context,
            PlacesDatabase::class.java,
            PlacesDatabase.DATABASE_NAME
        )
        .fallbackToDestructiveMigration()
        .build()
    }
}
```

### DataStore Configuration
```kotlin
// core-data/datastore/PreferencesRepository.kt
@Singleton
class PreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val Context.dataStore by preferencesDataStore(
        name = "user_preferences",
        serializer = UserPreferencesSerializer
    )
    
    val userPreferences: Flow<UserPreferences> = context.dataStore.data
    
    suspend fun updateLocationBias(enabled: Boolean) {
        context.dataStore.updateData { preferences ->
            preferences.toBuilder()
                .setLocationBiasEnabled(enabled)
                .build()
        }
    }
    
    suspend fun setApiKey(encryptedKey: String) {
        // Store encrypted API key using Android Keystore
        context.dataStore.updateData { preferences ->
            preferences.toBuilder()
                .setEncryptedApiKey(encryptedKey)
                .build()
        }
    }
}
```

### Repository Implementation Pattern
```kotlin
// core-data/repository/PlacesRepositoryImpl.kt
@Singleton
class PlacesRepositoryImpl @Inject constructor(
    private val placesDao: PlacesDao,
    private val placesApi: PlacesApi,
    private val placeMapper: PlaceMapper,
    private val cacheManager: CacheManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : PlacesRepository {
    
    override fun getPlaces(query: String): Flow<Result<List<Place>>> = flow {
        emit(Result.Loading)
        
        // Check cache first
        val cachedPlaces = placesDao.searchPlaces(query)
        if (cachedPlaces.isNotEmpty() && !cacheManager.isExpired(query)) {
            emit(Result.Success(cachedPlaces.map { placeMapper.toDomain(it) }))
            return@flow
        }
        
        // Fetch from network
        try {
            val networkPlaces = placesApi.searchPlaces(query)
            val entities = networkPlaces.map { placeMapper.toEntity(it) }
            
            // Update cache
            placesDao.insertPlaces(entities)
            cacheManager.updateTimestamp(query)
            
            emit(Result.Success(networkPlaces))
        } catch (e: Exception) {
            // Fallback to cache even if expired
            if (cachedPlaces.isNotEmpty()) {
                emit(Result.Success(
                    cachedPlaces.map { placeMapper.toDomain(it) },
                    isFromCache = true
                ))
            } else {
                emit(Result.Error(e))
            }
        }
    }.flowOn(ioDispatcher)
}
```

### Caching Strategy
```kotlin
// core-data/util/CacheManager.kt
@Singleton
class CacheManager @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) {
    companion object {
        const val DEFAULT_CACHE_DURATION_HOURS = 24
        const val SEARCH_CACHE_DURATION_MINUTES = 30
        const val LOCATION_CACHE_DURATION_DAYS = 7
    }
    
    fun isExpired(key: String, type: CacheType = CacheType.SEARCH): Boolean {
        val timestamp = getCacheTimestamp(key)
        val currentTime = System.currentTimeMillis()
        
        return when (type) {
            CacheType.SEARCH -> {
                currentTime - timestamp > TimeUnit.MINUTES.toMillis(SEARCH_CACHE_DURATION_MINUTES)
            }
            CacheType.LOCATION -> {
                currentTime - timestamp > TimeUnit.DAYS.toMillis(LOCATION_CACHE_DURATION_DAYS)
            }
            CacheType.PLACES -> {
                currentTime - timestamp > TimeUnit.HOURS.toMillis(DEFAULT_CACHE_DURATION_HOURS)
            }
        }
    }
    
    suspend fun clearExpiredCache() {
        // Periodic cleanup of expired cache entries
    }
}
```

### Android Keystore Integration
```kotlin
// core-data/util/EncryptionHelper.kt
@Singleton
class EncryptionHelper @Inject constructor() {
    private val keyAlias = "PlacesAppSecretKey"
    
    init {
        generateKey()
    }
    
    private fun generateKey() {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        )
        
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            keyAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()
        
        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    }
    
    fun encrypt(plainText: String): ByteArray {
        // Encrypt sensitive data using Android Keystore
    }
    
    fun decrypt(encryptedData: ByteArray): String {
        // Decrypt data using Android Keystore
    }
}
```

### Data Mappers
```kotlin
// core-data/mappers/PlaceMapper.kt
@Singleton
class PlaceMapper @Inject constructor() {
    
    fun toEntity(domain: Place): PlaceEntity {
        return PlaceEntity(
            placeId = domain.id,
            name = domain.name,
            address = domain.address,
            latitude = domain.location.latitude,
            longitude = domain.location.longitude,
            types = domain.types,
            timestamp = System.currentTimeMillis()
        )
    }
    
    fun toDomain(entity: PlaceEntity): Place {
        return Place(
            id = entity.placeId,
            name = entity.name,
            address = entity.address,
            location = Location(
                latitude = entity.latitude,
                longitude = entity.longitude
            ),
            types = entity.types
        )
    }
    
    fun toEntityList(domains: List<Place>): List<PlaceEntity> {
        return domains.map { toEntity(it) }
    }
    
    fun toDomainList(entities: List<PlaceEntity>): List<Place> {
        return entities.map { toDomain(it) }
    }
}
```

### Testing Core-Data Module
```kotlin
// Unit tests for DAOs
@RunWith(AndroidJUnit4::class)
class PlacesDaoTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private lateinit var database: PlacesDatabase
    private lateinit var placesDao: PlacesDao
    
    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            PlacesDatabase::class.java
        ).allowMainThreadQueries().build()
        placesDao = database.placesDao()
    }
    
    @Test
    fun insertAndRetrievePlaces() = runTest {
        // Given
        val places = listOf(
            createTestPlaceEntity("1", "Thrissur"),
            createTestPlaceEntity("2", "Kochi")
        )
        
        // When
        placesDao.insertPlaces(places)
        val retrieved = placesDao.getAllPlaces()
        
        // Then
        assertEquals(2, retrieved.size)
        assertEquals("Thrissur", retrieved[0].name)
    }
    
    @After
    fun tearDown() {
        database.close()
    }
}

// Repository tests with mocked dependencies
@ExperimentalCoroutinesApi
class PlacesRepositoryImplTest {
    @get:Rule
    val coroutineRule = MainCoroutineRule()
    
    @MockK
    private lateinit var placesDao: PlacesDao
    
    @MockK
    private lateinit var placesApi: PlacesApi
    
    @MockK
    private lateinit var cacheManager: CacheManager
    
    private lateinit var repository: PlacesRepositoryImpl
    
    @Before
    fun setup() {
        MockKAnnotations.init(this)
        repository = PlacesRepositoryImpl(
            placesDao, 
            placesApi, 
            PlaceMapper(), 
            cacheManager,
            coroutineRule.testDispatcher
        )
    }
    
    @Test
    fun `returns cached data when available and not expired`() = runTest {
        // Test implementation
    }
}
```

### ProGuard Rules for Core-Data
```pro
# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers class * {
    @androidx.room.* <fields>;
}

# DataStore
-keep class androidx.datastore.*.** {*;}

# Keep your entities and DAOs
-keep class com.places.autocomplete.core.data.database.entities.** { *; }
-keep interface com.places.autocomplete.core.data.database.dao.** { *; }
```

### Build Configuration (build.gradle.kts)
```kotlin
// core-data/build.gradle.kts
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.protobuf)
}

android {
    namespace = "com.places.autocomplete.core.data"
    compileSdk = 36
    
    defaultConfig {
        minSdk = 28
        
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Export Room schemas for migration testing
        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "$projectDir/schemas"
            }
        }
    }
}

dependencies {
    implementation(project(":core-domain"))
    implementation(project(":core-network"))
    
    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    
    // DataStore
    implementation(libs.androidx.datastore)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.protobuf.kotlin.lite)
    
    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    
    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.androidx.test.runner)
}
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

### Data Layer Issues
- **Room migration errors**: Check schema exports in `/schemas` directory and implement proper migration strategies
- **DataStore corruption**: Clear app data or implement fallback to default preferences
- **Cache invalidation**: Verify CacheManager TTL settings and cleanup jobs
- **Encryption failures**: Check Android Keystore availability and fallback to unencrypted storage for non-sensitive data
- **Memory leaks**: Use proper lifecycle-aware collectors (`collectAsStateWithLifecycle()`) and clean up observers
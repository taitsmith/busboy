# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

Busboy is an Android app (published on Google Play as `com.taitsmith.busboy`) that shows bus arrival
predictions, nearby stops, live vehicle locations, service alerts, and walking directions. It supports
**two transit agencies** — AC Transit (Oakland/East Bay) and Chicago's CTA — and the user switches
between them from the Settings screen (persisted in DataStore). It talks to the AC Transit realtime API,
the CTA BusTracker API, and the Google Maps/Directions API.

Both agencies run on the same Clever Devices "BusTime" platform, so their prediction payloads share the
`bustime-response → prd[]` shape and the core prediction transform is provider-agnostic. See
`add-cta-feature.md` in the repo root for the full multi-agency design rationale.

## Git workflow

- **Do not `git commit` or `git push`.** Committing and pushing are the user's responsibility. Stage
  finished work with `git add` and leave it for the user to commit manually.
- **Staging (`git add`) and read-only git commands** (`git status`, `git diff`, `git log`, `git show`, etc.)
  are fine.
- **Keep all work on the current branch** — do not create branches, switch branches, or use worktrees.
- **If the current branch is `dev` or `master`, stop and prompt the user to switch to a new branch** before
  making changes; do not proceed on those branches.

## Build & Test

Single-module Gradle project (`:app`). Use the wrapper.

```bash
./gradlew assembleDebug            # build debug APK
./gradlew test                     # run JVM unit tests (src/test)
./gradlew testDebugUnitTest        # same, debug variant
./gradlew connectedAndroidTest     # instrumented tests (src/androidTest) — needs device/emulator
./gradlew lint                     # Android lint
./gradlew installDebug             # build + install on connected device
```

Run a single unit test class or method:

```bash
./gradlew test --tests "com.taitsmith.busboy.viewmodels.ByIdViewModelTest"
./gradlew test --tests "com.taitsmith.busboy.viewmodels.ByIdViewModelTest.someTestMethod"
```

Toolchain: Gradle wrapper 8.11.1, AGP 8.9.2, Kotlin 2.1.0, Java 21. `compileSdk`/`targetSdk` 36,
`minSdk` 26. Compose uses the Kotlin 2 compiler plugin (`org.jetbrains.kotlin.plugin.compose`), so there
is no `kotlinCompilerExtensionVersion`; Compose BOM is `2025.03.01`.

There is no version catalog — dependency versions are hardcoded strings in `app/build.gradle`. There is
no CI (`.github/` does not exist) and no ktlint/spotless/detekt; `./gradlew lint` is the only static
analysis.

### API keys required to build/run

The `secrets-gradle-plugin` is the single source of truth for keys. It reads `secrets.properties` at the
repo root (gitignored, never committed) and falls back to the committed `secrets.defaults.properties`,
which holds `PLACEHOLDER` values so a fresh clone or CI can configure. A build with placeholders compiles
but network calls fail at runtime. Fill in real values:

```properties
ac_transit_key=...          # BuildConfig.ac_transit_key — all AC Transit endpoints in api/ApiInterface.kt
cta_key=...                 # BuildConfig.cta_key — CTA BusTracker endpoints in api/CtaApiInterface.kt
google_directions_key=...   # BuildConfig.google_directions_key — walking-directions call
google_maps_key=...         # BuildConfig field; currently read by no source file
MAPS_API_KEY=...            # AndroidManifest placeholder ${MAPS_API_KEY} → com.google.android.geo.API_KEY
```

`google_maps_key` and `MAPS_API_KEY` must both hold the **same Google Maps key**, but only `MAPS_API_KEY`
feeds the manifest — if it holds anything else, the map silently fails to authenticate. Values may be
written with or without surrounding quotes; the plugin strips and re-adds them when generating the
`BuildConfig` String literal.

Do not add `buildConfigField` or `manifestPlaceholders` entries for these keys. The plugin generates both,
and declaring them manually produces duplicate `BuildConfig` fields. Do not use `local.properties` for keys;
it only holds `sdk.dir`.

## Architecture

MVVM with Hilt DI, Kotlin Coroutines/Flow, LiveData/StateFlow, Retrofit, and Room. The layering is strict —
UI never touches Retrofit or Room directly.

**Layer flow:** `Fragment` → `ViewModel` → `ApiRepository`/`DatabaseRepository`/`LocationRepository` →
`RemoteDataSource` → `ApiInterface` (Retrofit).

- **`ui/`** — one Activity (`MainActivity`, `@AndroidEntryPoint`) hosting fragments via Jetpack Navigation
  (`res/navigation/nav_graph.xml`, bottom nav). `MainActivity` uses DataBinding; fragments use ViewBinding.
  Exactly one screen is Compose: `ServiceAlertFragment`, which hosts a `ComposeView` inside an XML layout.
  Everything else is View-XML.
- **`viewmodels/`** — `@HiltViewModel` classes. Most expose `sealed class` state holders via `StateFlow`
  (`ByIdViewModel.PredictionState` / `BusState`, `NearbyViewModel.NearbyStopsState`,
  `MainActivityViewModel.LoadingState`), plus `LiveData` for simpler fields. Not universal —
  `FavoritesViewModel` just exposes `Flow<List<Stop>>`. Work runs in `viewModelScope`.
- **`di/`** — this package holds BOTH the Hilt modules AND the repository interfaces/implementations
  (it is the whole data/repository layer, not just wiring):
  - `ApiRepository`/`ApiRepositoryImpl` — maps `RemoteDataSource` responses to domain `data/` models, and
    carries real domain logic: `stopPredictions` drops predictions whose `dyn` is non-zero (a null `dyn`
    is treated as stopping and kept) and rewrites `prdctdn` into `"Arriving"` (for `"1"`/`"Due"`/`"DUE"`,
    matched case-insensitively so CTA's uppercase sentinel works) or `"in X minutes"`. This transform is
    **provider-agnostic** — it serves both agencies unchanged. `ApiRepositoryTransformTest` pins it down.
  - `RemoteDataSource` — **one interface, three implementations** (this is the multi-agency spine):
    - `AcTransitRemoteDataSource` and `CtaRemoteDataSource` — per-agency; each wraps its Retrofit
      interface(s) and maps DTOs into the shared domain types (`Prediction`/`Bus`/`ServiceAlertResponse`/
      `LatLng`). Both convert EitherNet `ApiResult` failures into thrown `Exception`s with the **same
      string-code messages**. `predictions(...)` and `vehicleLocation(...)` are infinite `flow`s that
      re-emit every 60s (`refreshIntervalMillis`); every other call emits once.
    - `DelegatingRemoteDataSource` — the facade that everything injects. It holds a
      `Map<Agency, Provider<RemoteDataSource>>` (Hilt `@IntoMap` keyed by `@AgencyKey`) plus
      `SettingsRepository`, and delegates each call to the active agency's source. **It reads the selected
      agency inside each `flow{}` builder / at call entry — at *collection* time, not construction** — which
      is what makes a mid-session city switch take effect on the next call. Bound as the *unqualified*
      `RemoteDataSource` in `DataSourceModule`; the two concretes are the qualified `@IntoMap` entries.
    - CTA specifics: `CtaRemoteDataSource` serves alerts from `getdetours` (CTA has no bulletin endpoint),
      route polylines from `getpatterns` (longest pattern, sorted by `seq`), and Nearby from the bundled
      GTFS catalog (`CtaStopCatalog`, below) rather than a live geo query.
  - `SettingsRepository`/`SettingsRepositoryImpl` — the selected `Agency`, backed by DataStore Preferences.
    Exposes `selectedAgency: Flow<Agency>` and `selectedAgencyState: StateFlow<Agency>` (the synchronous
    handle the delegate and `DatabaseRepository` read) plus `suspend setAgency()`. `@Singleton`, wired in
    `SettingsModule` (which also provides the `DataStore<Preferences>` and an `@AppCoroutineScope`).
  - `CtaStopCatalog` (`@Singleton`) — on-device CTA nearby-stop search over a bundled GTFS catalog
    (separate read-only `CtaCatalogDatabase`, seeded from `assets/cta_stops.tsv`, mutex-guarded and
    count-gated). Bounding-box SQL then Haversine filter/sort in Kotlin. Pure helpers
    (`parseLine`/`filterAndSort`/`haversineMeters`) are unit-tested in `CtaStopCatalogTest`.
  - `DatabaseRepository` — Room access (favorites). Injects `SettingsRepository`: writes **stamp** the
    active agency onto each `Stop` and reads **filter** by it, so the same numeric stop id doesn't collide
    across agencies. `LocationRepository`/`LocationRepositoryImpl` — wraps `FusedLocationProviderClient`,
    exposes location as `StateFlow`, polls at `PRIORITY_LOW_POWER`.
  - `StatusRepository` (implements `StatusInterface`) — a `@Singleton` `MutableStateFlow<LoadingState>` used
    as an app-wide loading/status bus. ViewModels call `isLoading(...)`/`updateStatus("SOME_CODE")`.
    `MainActivityViewModel.init` collects `statusRepository.state` into its own `uiState`, and `MainActivity`
    collects `uiState` (the Activity does not observe the repository directly).
  - Hilt scoping: API interfaces and data sources are `ViewModelComponent`-scoped; the Room databases
    (`DatabaseModule`), the location client (`AppModule`, in `BusboyApplication.kt`), and
    `SettingsRepository`/`CtaStopCatalog` are `SingletonComponent`-scoped (an ancestor of
    `ViewModelComponent`, so they inject cleanly into the data sources). The two AC Transit-shaped Retrofit
    `ApiInterface` instances are distinguished by the `@AcTransitApiInterface` and `@MapsApiInterface`
    qualifiers; the CTA `CtaApiInterface` is a **distinct type**, so Hilt resolves it by return type with no
    qualifier.
- **`api/`** — `ApiInterface` (AC Transit + Maps Retrofit endpoints; tokens default to `BuildConfig` keys),
  `CtaApiInterface` (CTA BusTracker endpoints — `getPredictions`/`getVehicles`/`getPatterns`/`getDetours`;
  the `key` and `format=json` params are injected by an OkHttp interceptor, not method args), and Gson
  response DTOs including the CTA shapes (`CtaVehicle`+`toBus()`, `CtaDetour`+`toServiceAlert()`,
  `CtaPatternResponse`, `CtaVehicleResponse`, `CtaDetourResponse`). **EitherNet is not universal:**
  `getStopPredictionList`, `getNearbyStops`, `getStopDestinations`, `getVehicleInfo`,
  `getServiceAlertsForStop`, and the CTA endpoints return `ApiResult<T, Unit>`, but `getBusRouteWaypoints`
  (`List<WaypointResponse>`), `getDetailedVehicleInfo` (`List<Bus>`), and the Maps `getNavigationToStop`
  (`DirectionResponse`) return raw types. The Maps Retrofit instance does not register the EitherNet
  converter/call-adapter at all.
- **`data/`** — Room entities (`Stop`, `StopDestinationResponse.RouteDestination`, and the CTA catalog
  entity `CtaStop`), DAOs, the two databases (`BusboyDatabase`, `CtaCatalogDatabase`), the `Agency` enum,
  and domain models (`Bus`, `Prediction`, `ServiceAlert` — plain `Serializable`, not entities). `Bus` and
  `Stop` are the shared domain/UI types; CTA maps its terser payloads into them (CTA vehicles leave
  make/wifi/AC/etc. null).
- **`utils/`** — RecyclerView adapters (`PredictionAdapter`, `NearbyAdapter`), `StatusInterface`, `Constants`.

### Status codes

Errors travel as `Exception` messages carrying a string code, thrown from **two** layers:

- `AcTransitRemoteDataSource` / `CtaRemoteDataSource` — `"404"` (for both `ApiFailure` *and* `HttpFailure`),
  `"CALL_FAILURE"` (`NetworkFailure`), `"UNKNOWN"` (`UnknownFailure`), and `"empty_response"` (in
  `getBusRouteWaypoints`). Both agencies emit the identical vocabulary.
- `ApiRepositoryImpl` — `"NO_SERVICE_SCHEDULED"`, `"UNKNOWN"`, `"NULL_BUS_COORDS"`.

ViewModels catch these and forward them to `StatusRepository.updateStatus(code)`. `MainActivity.updateStatus`
then maps the code to UI via a hand-written `when (s)` block (~18 arms) resolving to `R.string.snackbar_*`
ids. Codes are **not** string-resource names and are not looked up dynamically — the vocabulary is an
informal enum spread across ViewModels and the Activity, with no single source of truth. Adding a new code
means adding a `when` arm.

### Room databases

There are **two** Room databases:

- **`BusboyDatabase`** (`busboy_database`) — favorites, at **version 4** with `exportSchema = true`
  (schemas in `app/schemas/`). Migrations: `AutoMigration`s 1→2 and 2→3 (`Migration_2_3`, an
  `AutoMigrationSpec` that `@DeleteColumn`s `Stop.id`), then a **handwritten `MIGRATION_3_4`** (in the
  `BusboyDatabase` companion, registered via `.addMigrations(...)` in `DatabaseModule`). v4 gave `Stop` a
  composite primary key `(stopId, agency)` — a PK change is not AutoMigration-able, so `MIGRATION_3_4`
  rebuilds the table by hand (create → `INSERT…SELECT` stamping existing rows `AC_TRANSIT` → drop → rename).
  `MigrationTest` (androidTest) covers it; `app/schemas` is wired into androidTest assets so
  `MigrationTestHelper` finds the exported JSON. When changing an entity, bump the version and add a
  migration — do not rely on destructive fallback.
- **`CtaCatalogDatabase`** (`cta_catalog_database`) — the read-only bundled CTA GTFS stop catalog
  (`CtaStop`, ~10,670 rows), `exportSchema = false`, version 1. Deliberately **separate** from favorites so
  the 10k-row seed doesn't entangle the favorites schema/migrations; a future GTFS refresh is a drop-in
  asset swap. Seeded lazily by `CtaStopCatalog` from `assets/cta_stops.tsv` (regenerate via the offline
  `tools/build_cta_catalog.py` pipeline).

## Known warts

Deliberate or long-standing oddities. Each looks like a bug worth "fixing" until you know otherwise:

- `LocationRepositoryImpl` is annotated `@Singleton` but is `@Binds`-bound in `DataSourceModule`, a
  `ViewModelComponent` module, with no scope on the binding method. The `@Singleton` is therefore inert and
  the repository is effectively ViewModel-scoped.
- `ApiRepositoryImpl` is annotated `@Module @InstallIn(ViewModelComponent::class)` despite containing no
  `@Provides`. The annotation is superfluous; the real binding is in `DataSourceModule`. (The data-source
  classes no longer carry this — the old `RemoteDataSourceImpl` dropped it when it was split/renamed into
  `AcTransitRemoteDataSource`.)
- `NearbyViewModel`'s `companion object` holds `lateinit var currentLocation`, `lateinit var loc`, and
  `locationPermGranted` — app-wide mutable static state, written from `MainActivity.onRequestPermissionsResult`,
  living outside the DI/repository layering.
- `BusDetailFragment` is a `DialogFragment` with a constructor argument (`class BusDetailFragment(private val
  bus: Bus)`), yet `nav_graph.xml` also declares it with a `selectedBus` Safe Args argument. A
  constructor-injected instance will not survive recreation.
- Hilt runtime is `2.55` while the Hilt Gradle plugin is `2.46.1`.
- `navigation-fragment-ktx` and `navigation-ui-ktx` are each declared twice, at `2.7.7` and `2.8.9`; the
  SafeArgs plugin is pinned to `2.7.7`.
- kapt does not support Kotlin 2.x language versions and silently falls back to `1.9` (a build warning).
- `gradlew` and `gradlew.bat` are **not tracked in git**, though `gradle/wrapper/gradle-wrapper.jar` and
  `.properties` are. A fresh clone cannot run `./gradlew` until the scripts are restored. `.gitignore` —
  which is what keeps `secrets.properties` out of git — is likewise untracked.
- `utils/Constants.kt` is an empty class.

The following two are **genuine unresolved latent bugs** (not deliberate) — noted here so they aren't
rediscovered from scratch:

- `MapsFragment.onDestroy()` calls `googleMap.clear()` / `setOnMarkerDragListener(null)` /
  `setOnMarkerClickListener(null)` on the `lateinit var googleMap`, which is only assigned inside the async
  `OnMapReadyCallback`. If the fragment is destroyed before the map finishes loading — navigate in and
  straight back out, no map, or `childFragmentManager.findFragmentById(R.id.map)` returns null so
  `getMapAsync` is never called — the callback never fires and `onDestroy` throws
  `UninitializedPropertyAccessException`. Guard with `if (::googleMap.isInitialized)`.
- `NearbyViewModel._enableSearchButton` is a one-way latch: `NearbyFragment` only ever does
  `if (enabled) binding.nearbySearchButton.isEnabled = true` and nothing sets it back to `false`. Once the
  search button is enabled it can never be re-disabled, so it does not reflect actual state (e.g. after a
  location choice is invalidated).

## Testing conventions

- Unit tests (`src/test`) test ViewModels, `ApiRepository`, and `RemoteDataSource` against **hand-written
  fakes** or Mockito mocks — never the real network. The fakes are split across packages:
  `FakeApiRepository` and `FakeRemoteDataSource` live in test `api/`; `FakeLocationRepository` lives in
  test `viewmodels/`.
- **There is no Hilt test infrastructure** — no `@HiltAndroidTest`, no custom runner. Tests construct
  ViewModels and repositories directly and pass fakes/mocks into their constructors. This is the first thing
  to know before writing a test here.
- The fakes branch on **magic sentinel inputs**: `predictions("good")`, `vehicleLocation("1234")`, and a stop
  named `"good"`. Tests assert on the branches those trigger, so the sentinels cannot be renamed casually.
  `FakeApiRepository` hard-codes `prdctdn` values, which is why `ApiRepositoryTransformTest` bypasses it and
  mocks `RemoteDataSource` instead.
- `FakeLocationRepository.stopUpdates()` is `TODO("Not yet implemented")` and throws if a test calls it.
- `MainDispatchRule` (in `MainDispatchRule.kt`) swaps `Dispatchers.Main` for a test dispatcher;
  `InstantTaskExecutorRule` runs LiveData synchronously. `getOrAwaitValue` (in `LiveDataTestUtil.kt`) reads
  LiveData in tests, and also works on a `StateFlow` bridged with `.asLiveData()`.
- StateFlow pattern: assert the initial `Loading` state, `launch(UnconfinedTestDispatcher(testScheduler)) {
  vm.state.collect {} }` to keep the flow hot, trigger the action, re-read `.value`, then `collectJob.cancel()`.
- Assertions are mixed. `ApiRepositoryTest` is pure JUnit4; `ApiRepositoryTransformTest`,
  `AcTransitRemoteDataSourceTest`, and `CtaRemoteDataSourceTest` are pure Kotest (`shouldBe`,
  `shouldBeTypeOf`); the ViewModel tests mix both; `RoomTest`/`MigrationTest` (androidTest) use Hamcrest.
  Kotest is assertions-only (`kotest-assertions-core`), not a runner. There is no Turbine and no Robolectric.
- `AcTransitRemoteDataSourceTest` and `CtaRemoteDataSourceTest` mock the real `ApiInterface`/`CtaApiInterface`
  to pin the EitherNet `ApiResult.Failure` → status-code contract that the fakes do not cover.
  `DelegatingRemoteDataSourceTest` verifies agency routing with a private `FakeSettingsRepository`;
  `CtaStopCatalogTest` covers the pure parse/geo helpers.
- Instrumented tests (`src/androidTest`): `RoomTest.kt` exercises the real favorites DB via
  `inMemoryDatabaseBuilder`; `MigrationTest.kt` drives `MIGRATION_3_4` with `MigrationTestHelper` against the
  exported schemas.
- `unitTests.returnDefaultValues = true` is set, so Android framework calls return defaults in JVM tests.

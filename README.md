<div align="center">

# nowinandroid-agent-kit

**One command to scaffold a production-ready Android project with AI agent harness.**

Based on [NowInAndroid](https://github.com/android/nowinandroid) by Google.

[![Kotlin](https://img.shields.io/badge/Kotlin-2.1-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Hilt](https://img.shields.io/badge/Hilt-DI-34A853?logo=google&logoColor=white)](https://dagger.dev/hilt/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

**Works with:** Claude Code · Gemini CLI · Cursor · Copilot

</div>

---

## Generate a Project

```bash
git clone https://github.com/warrenth/nowinandroid-agent-kit.git
cd nowinandroid-agent-kit
./generate.sh
```

```
Project name: MyApp
Package name: com.example.myapp
Output directory: ~/Projects/MyApp

==> Copying project scaffold...          ✓
==> Replacing placeholders...            ✓
==> Organizing source files...           ✓
==> Setting up AI agent harness...       ✓ (8 rules, 8 skills, 4 agents)
==> Initializing git repository...       ✓

✓ Project generated successfully!

  14 modules ready:
  ├── app                     Entry point + Bottom Navigation
  ├── core/model              Domain data classes
  ├── core/domain             UseCases
  ├── core/data               Repository (offline-first)
  ├── core/database           Room DAOs + Entities
  ├── core/network            Retrofit + OkHttp
  ├── core/datastore          DataStore preferences
  ├── core/designsystem       Material 3 Theme + Components
  ├── core/ui                 Shared Composables
  ├── core/navigation         Routes
  ├── core/common             Dispatcher DI
  ├── core/testing            Test utilities + Fakes
  ├── feature/home            ★ Sample feature (copy this!)
  ├── feature/settings        Settings placeholder
  └── sync/work               WorkManager sync
```

> **Open in Android Studio → Replace `BASE_URL` → Add your features.**

---

## Or Just Copy What You Need

Don't need the full project? Grab individual files:

```bash
# One skill
cp -r skills/compose-animation/ your-project/.claude/skills/

# All rules
cp -r rules/ your-project/.claude/rules/

# Everything
cp -r rules/ skills/ agents/ your-project/.claude/
```

---

## What's Inside

### Rules — always loaded, keep your AI on track

| Rule | Enforces |
|------|----------|
| [architecture](rules/architecture.md) | Clean Architecture layers, ViewModel / UseCase / Repository |
| [module-boundary](rules/module-boundary.md) | Feature API/Impl split, dependency direction |
| [compose-rules](rules/compose-rules.md) | State hoisting, recomposition, side effects |
| [coroutines](rules/coroutines.md) | Scope, Flow collection, CancellationException |
| [kotlin-style](rules/kotlin-style.md) | Formatting, naming, null safety |
| [naming-convention](rules/naming-convention.md) | NIA class/function/resource naming |
| [testing](rules/testing.md) | Fakes over Mocks, Turbine, AAA pattern |
| [performance](rules/performance.md) | Recomposition, image loading, startup |

### Skills — on-demand deep knowledge

| Skill | Teaches |
|-------|---------|
| [compose-animation](skills/compose-animation/) | 7 animation patterns (visibility, rotation, keyframes, stagger, state machine, canvas, gesture) |
| [compose-performance](skills/compose-performance/) | Stability, graphicsLayer, derivedStateOf |
| [compose-navigation](skills/compose-navigation/) | Type-safe routes, adaptive nav |
| [compose-testing](skills/compose-testing/) | ViewModel / UI / screenshot tests |
| [compose-migration](skills/compose-migration/) | XML → Compose strategy |
| [hilt-di](skills/hilt-di/) | @Binds / @Provides, convention plugins |
| [coroutines-flow](skills/coroutines-flow/) | StateFlow, parallel sync, change-list |
| [room-offline](skills/room-offline/) | Offline-first, Entity / DTO / Domain mapping |

### Agents — autonomous executors

| Agent | Role |
|-------|------|
| [architect](agents/architect.md) | Module design, dependency graph |
| [coder](agents/coder.md) | Feature implementation |
| [code-reviewer](agents/code-reviewer.md) | Architecture compliance review |
| [tester](agents/tester.md) | Unit / UI / screenshot tests |

---

## Architecture

```
READ:  Room → Repository → UseCase → ViewModel (StateFlow) → Compose UI
WRITE: UI event → ViewModel → Repository → DataStore / Room
SYNC:  WorkManager → SyncWorker → Network → Room
```

<details>
<summary><b>Full project structure</b></summary>

```
MyApp/
├── app/
│   └── src/main/kotlin/com/example/myapp/
│       ├── MainApplication.kt              @HiltAndroidApp
│       ├── MainActivity.kt                 @AndroidEntryPoint
│       └── AppNavHost.kt                   Bottom Nav + routing
│
├── core/
│   ├── model/          SampleItem.kt, UserData.kt
│   ├── domain/         GetSampleItemsUseCase.kt
│   ├── data/           OfflineFirstSampleRepository.kt, DataModule.kt
│   ├── database/       AppDatabase.kt, SampleItemDao.kt, SampleItemEntity.kt
│   ├── network/        NetworkModule.kt, SampleApi.kt, NetworkSampleItem.kt
│   ├── designsystem/   Theme.kt, LoadingWheel.kt, DynamicAsyncImage.kt
│   ├── datastore/      (DataStore preferences)
│   ├── navigation/     (Routes)
│   ├── ui/             (Shared composables)
│   ├── common/         Dispatchers.kt
│   └── testing/        (Test utilities)
│
├── feature/
│   ├── home/
│   │   ├── api/        HomeNavKey.kt
│   │   └── impl/       HomeViewModel.kt, HomeScreen.kt
│   └── settings/
│       └── impl/       SettingsScreen.kt
│
├── sync/work/          (WorkManager sync)
│
├── .claude/            8 rules + 8 skills + 4 agents
├── CLAUDE.md
├── gradle/libs.versions.toml
└── gradle.properties
```

</details>

---

## NowInAndroid Version

Derived from NowInAndroid (March 2026): AGP 9.0, Kotlin 2.3, Compose BOM 2025.09, Hilt 2.59, Room 2.8.3, Navigation3.

## Contributing

Fork → branch → add/improve rules or skills → PR.
All code examples must come from real NowInAndroid source analysis.

## License

MIT

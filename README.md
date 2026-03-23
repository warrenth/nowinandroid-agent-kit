# nowinandroid-agent-kit

AI agent skills, rules, and tools for Android development based on [NowInAndroid](https://github.com/android/nowinandroid) architecture. Works with **Claude Code**, **Gemini CLI**, **Cursor**, and **Copilot**.

All rules and skills are derived from **actual NowInAndroid source code analysis**, not generic best practices.

## Quick Start

### Option 0: Generate a full project (one click)

Generate a complete multi-module Android project with AI agent harness pre-configured:

```bash
git clone https://github.com/warrenth/nowinandroid-agent-kit.git
cd nowinandroid-agent-kit
./generate.sh --name "MyApp" --package "com.example.myapp" --output ~/Projects/MyApp
```

This creates a ready-to-build project with:
- 14 modules (app, 10 core, 2 feature, 1 sync)
- Sample Home feature (ViewModel + Screen + Repository + UseCase)
- Design system (Material 3 theme, components)
- Network module (Retrofit + OkHttp + Kotlinx Serialization)
- Database module (Room + DAOs + offline-first)
- Hilt DI wiring
- `.claude/` with all rules, skills, and agents

**You just add features by copying the `feature/home/` pattern.**

### Option 1: Copy what you need (easiest)

Browse the `skills/` and `rules/` directories and copy individual files to your project:

```bash
# Copy a single skill
cp -r skills/compose-animation/ your-project/.claude/skills/compose-animation/

# Copy a rule
cp rules/architecture.md your-project/.claude/rules/architecture.md
```

### Option 2: Copy everything

```bash
git clone https://github.com/warrenth/nowinandroid-agent-kit.git
cp -r nowinandroid-agent-kit/rules/ your-project/.claude/rules/
cp -r nowinandroid-agent-kit/skills/ your-project/.claude/skills/
cp -r nowinandroid-agent-kit/agents/ your-project/.claude/agents/
```

### Option 3: Claude Code plugin (coming soon)

```bash
claude plugin install warrenth/nowinandroid-agent-kit
```

## What's Included

### Rules (always-loaded constraints)

| Rule | What it enforces |
|------|-----------------|
| [architecture.md](rules/architecture.md) | Clean Architecture layers, ViewModel/UseCase/Repository patterns |
| [module-boundary.md](rules/module-boundary.md) | Feature API/Impl separation, dependency direction |
| [kotlin-style.md](rules/kotlin-style.md) | Formatting, naming, null safety, idioms |
| [compose-rules.md](rules/compose-rules.md) | State hoisting, recomposition, side effects, Lazy lists |
| [coroutines.md](rules/coroutines.md) | Scope management, Flow collection, error handling |
| [naming-convention.md](rules/naming-convention.md) | NIA naming patterns for classes, functions, resources |
| [testing.md](rules/testing.md) | Test strategy, Fakes over Mocks, Turbine |
| [performance.md](rules/performance.md) | Recomposition optimization, image loading, startup |

### Skills (on-demand knowledge)

| Skill | What it teaches |
|-------|----------------|
| [compose-animation](skills/compose-animation/) | 7 animation patterns from NIA (visibility, rotation, keyframes, stagger, state machine, canvas, gesture) |
| [compose-performance](skills/compose-performance/) | Stability, recomposition optimization, graphicsLayer, derivedStateOf |
| [compose-navigation](skills/compose-navigation/) | Navigation3 with serializable NavKeys, adaptive navigation |
| [compose-testing](skills/compose-testing/) | ViewModel tests, UI tests, screenshot tests, Fake repositories |
| [compose-migration](skills/compose-migration/) | XML to Compose migration strategy, component mapping |
| [hilt-di](skills/hilt-di/) | @Binds/@Provides patterns, module organization, convention plugins |
| [coroutines-flow](skills/coroutines-flow/) | StateFlow, combine, parallel sync, change-list pattern |
| [room-offline](skills/room-offline/) | Offline-first with Room, Entity/DTO/Domain mapping, sync |

### Agents (autonomous executors)

| Agent | Model | Role |
|-------|-------|------|
| [architect](agents/architect.md) | opus | Module design, dependency graph, data flow |
| [coder](agents/coder.md) | sonnet | Feature implementation following NIA patterns |
| [code-reviewer](agents/code-reviewer.md) | opus | Architecture compliance review |
| [tester](agents/tester.md) | sonnet | Unit tests, UI tests, screenshot tests |

### Templates

| Template | Use |
|----------|-----|
| [CLAUDE.md.template](templates/CLAUDE.md.template) | Project CLAUDE.md for NIA-style projects |

## Architecture Overview

Based on NowInAndroid's production architecture:

```
:app
├── :feature:*/api          (NavKey exports only)
├── :feature:*/impl         (ViewModel + Screen)
├── :core:model             (Domain data classes)
├── :core:domain            (UseCases, Repository interfaces)
├── :core:data              (Repository impl, DI modules)
├── :core:database          (Room DAOs, Entities)
├── :core:network           (Retrofit, DTOs)
├── :core:datastore         (Proto DataStore)
├── :core:designsystem      (Theme, Material 3)
├── :core:ui                (Shared composables)
└── :core:navigation        (Navigator, NavKey)
```

Data flows:
```
READ:  Room DAO → Repository → UseCase → ViewModel (StateFlow) → Compose UI
WRITE: UI event → ViewModel → Repository → DataStore/Room
SYNC:  WorkManager → SyncWorker → Network → Room (background)
```

## How It Differs from Alternatives

| | [dpconde/claude-android-skill](https://github.com/dpconde/claude-android-skill) | **nowinandroid-agent-kit** |
|---|---|---|
| Skills | 1 skill, 5 references | 8 skills, 8 rules, 4 agents |
| Animation | None | 7 patterns from NIA source |
| Testing | Basic | Fake repos, Turbine, screenshot tests |
| Navigation | Navigation Compose | Navigation3 (latest) |
| Agents | None | architect, coder, reviewer, tester |
| AI Tools | Claude only | Claude, Gemini, Cursor, Copilot |

## NowInAndroid Version

Analysis based on NowInAndroid as of March 2026:
- AGP 9.0.0, Kotlin 2.3.0
- Compose BOM 2025.09.01
- Navigation3, Hilt 2.59, Room 2.8.3

## Contributing

1. Fork the repository
2. Create a feature branch
3. Add or improve rules/skills
4. Submit a pull request

Skills should include code examples from real NowInAndroid source, not generic patterns.

## License

MIT

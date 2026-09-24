# Third-party notices

## MuscleMap anatomy vectors

Vibe Check includes adapted SVG muscle path data from **MuscleMap**:
https://github.com/Jsplice/MuscleMap

The male/female front/back vector anatomy data is used to render the interactive recovery and stretching-recency diagrams natively in Jetpack Compose.

MIT License

Copyright (c) 2026 Jsplice / MuscleMap contributors

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

---

Scope of this license:

- The MIT license above covers EVERYTHING in this repository — all source code,
  the SVG muscle path data in `packages/assets` (original traced vector work),
  and the body photographs in `apps/playground/public/` (`bg-male-*.png`,
  `bg-female-*.png`).
- The body photographs were generated with OpenAI image tooling (from the
  maintainer's own prompts) and manually edited. They are released under MIT as
  well — free to use, copy, modify, and redistribute. See ASSET_PROVENANCE.md
  for details.

---

## Free Exercise DB

Vibe Check includes exercise definitions derived from **Free Exercise DB**:
https://github.com/yuhonas/free-exercise-db

The dataset is released under the Unlicense/public-domain dedication. A copy of
the licence is bundled at `app/src/main/assets/FREE_EXERCISE_DB_LICENSE.md`.
Mappings are normalised into Vibe Check's canonical muscle taxonomy, with
curated overrides for the core programme and calisthenics exercises.

---

## Android runtime dependencies

The release runtime graph was resolved on 22 September 2026 with:

```text
gradle :app:dependencies --configuration releaseRuntimeClasspath
```

The following inventory groups modules from the same project and licence. It
does not replace review of each upstream project's licence and notice files.

| Project or module family | Resolved version(s) | Declared licence | Source |
| --- | --- | --- | --- |
| AndroidX (Activity, Annotation, Arch Core, Collection, Compose, Concurrent Futures, Core, Custom View, DataStore, Emoji2, Fragment, Graphics Path, Hilt integration, Interpolator, Lifecycle, Navigation, Profile Installer, Room, Saved State, SQLite, Startup, Tracing, Versioned Parcelable and ViewPager) | Multiple; direct versions are recorded in `app/build.gradle.kts` and the complete resolved graph is produced by the command above | Apache License 2.0 | https://github.com/androidx/androidx |
| Kotlin standard library and Android extensions runtime | 2.3.21 and 1.9.22 | Apache License 2.0 | https://github.com/JetBrains/kotlin |
| kotlinx.coroutines | 1.8.1 | Apache License 2.0 | https://github.com/Kotlin/kotlinx.coroutines |
| kotlinx.serialization | 1.7.3 | Apache License 2.0 | https://github.com/Kotlin/kotlinx.serialization |
| Dagger and Hilt | 2.60.1 | Apache License 2.0 | https://github.com/google/dagger |
| Okio | 3.4.0 | Apache License 2.0 | https://github.com/square/okio |
| Jakarta Inject API | 2.0.1 | Apache License 2.0 | https://github.com/eclipse-ee4j/injection-api |
| javax.inject | 1 | Apache License 2.0 | https://github.com/javax-inject/javax-inject |
| Guava `listenablefuture` compatibility artifact | 1.0 | Apache License 2.0 | https://github.com/google/guava |
| FindBugs JSR-305 annotations | 3.0.2 | Apache License 2.0 | https://repo1.maven.org/maven2/com/google/code/findbugs/jsr305/3.0.2/jsr305-3.0.2.pom |
| JetBrains annotations | 23.0.0 | Apache License 2.0 | https://github.com/JetBrains/java-annotations |
| JSpecify annotations | 1.0.0 | Apache License 2.0 | https://github.com/jspecify/jspecify |
| Core library desugaring (`desugar_jdk_libs`) | 2.1.5 | Upstream OpenJDK and third-party terms recorded by the project | https://github.com/google/desugar_jdk_libs |

The graph also resolves `jakarta.inject-api` 2.0.1, `javax.inject` 1,
`listenablefuture` 1.0, JSR-305 3.0.2, JetBrains annotations 23.0.0 and
JSpecify 1.0.0 transitively; they are listed explicitly so small annotation
and compatibility artifacts are not hidden by their parent libraries.

Before a signed external release, regenerate the graph, review the licence and
NOTICE files for the exact resolved artifacts (especially
`desugar_jdk_libs`), decide which full licence texts/notices must be bundled,
and obtain the project's legal/release approval. This inventory is engineering
evidence, not legal advice or approval.

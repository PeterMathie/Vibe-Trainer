# Third-party provenance and release checks

`THIRD_PARTY_NOTICES.md` is the repository notice source. The same content is
bundled in the app at `app/src/main/assets/THIRD_PARTY_NOTICES.md`. Keep the
two files byte-for-byte identical.

The notice currently covers:

- adapted MuscleMap anatomy vectors and their MIT attribution;
- Free Exercise DB provenance and its separately bundled Unlicense text; and
- an engineering inventory of the resolved Android release runtime dependency
  families and declared licences.

Regenerate the runtime graph before every external release:

```text
gradle :app:dependencies --configuration releaseRuntimeClasspath
```

Also inspect the separately configured `coreLibraryDesugaring` dependency in
`app/build.gradle.kts`. Dependency upgrades must update both notice copies.

Before signing or publishing, review the exact artifacts' licence and NOTICE
files, decide which full texts must be distributed (especially the OpenJDK and
third-party terms represented by `desugar_jdk_libs`), and obtain the project's
legal/release approval. The inventory is not legal approval.

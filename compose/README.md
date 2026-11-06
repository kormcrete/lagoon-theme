# Lagoon for Compose

`lagoon-compose`: the modern Lagoon for [Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/), on
Android, iOS and desktop (JVM). Part of [lagoon-theme](../README.md), next to the [Swing theme](../swing/README.md).

A light ground, white cards with soft corners, one blue accent, teal for what is good and amber for what needs
attention, Inter as the font. Colours, tile colours, radii and sizes come from the shared design tokens,
[`tokens/lagoon.tokens.json`](../tokens/lagoon.tokens.json): the build generates `LagoonTokens.kt` from them, so the
Swing theme's modern Lagoon and this one stay the same design.

## Use

Not on Maven Central yet: publish it to the local Maven repository from this folder,

```
./gradlew :lagoon-compose:publishToMavenLocal
```

then depend on the snapshot from a project with `mavenLocal()` among its repositories:

```kotlin
commonMain.dependencies {
    implementation("io.github.kormcrete:lagoon-compose:0.1.0-SNAPSHOT")
}
```

Wrap the UI in `LagoonTheme`:

```kotlin
import io.github.kormcrete.lagoon.compose.*

LagoonTheme {
    LagoonCard(padding = PaddingValues(16.dp)) {
        Text("Ada Shop Ltd", style = Lagoon.type.heading)
        StatusPill("Placed", LagoonTone.Info)
        LagoonButton("New order", onClick = { /* ... */ }, icon = LagoonIcons.Plus)
    }
}
```

- **Density:** `LagoonTheme(metrics = ...)` takes `LagoonMetrics.Mobile` (touch sizes) or `LagoonMetrics.Desktop`
  (denser). By default it uses Mobile on Android and iOS and Desktop on the JVM: the tokens' `metrics.mobile` and
  `metrics.desktop`.
- **Theme values:** `Lagoon.colors`, `Lagoon.metrics`, `Lagoon.shapes` and `Lagoon.type` give the current values,
  as `MaterialTheme` does.
- **Material 3:** Material 3 components inside the theme get a matching colour scheme, type scale and shapes.
- **Font:** Inter is bundled (SIL Open Font License, see `lagoon-compose/INTER-OFL.txt`).

## What is in it

- `LagoonTheme.kt`: the theme, `LagoonColors`, `LagoonMetrics`, `LagoonShapes`, the type scale and its mapping
  onto Material 3.
- `LagoonComponents.kt`: card, divider, section label, buttons (`Primary`, `Tonal`, `Outline`, `Danger`, `Night`),
  icon button, status pill and tag in the `LagoonTone`s (info, success, attention, danger, neutral), avatar and icon
  tiles, banner, stepper, segmented control, chip, search and text fields, option card, step progress, amount row.
- `LagoonIcons.kt`: Lagoon's own line icons on a 24-unit grid (back, forward, close, plus, minus, check...).

## Build

In this folder, with a JDK 17 toolchain (Gradle downloads one if it is missing):

```
./gradlew :lagoon-compose:assemble :lagoon-compose:jvmTest
./gradlew :lagoon-compose:compileKotlinIosSimulatorArm64 :lagoon-compose:compileKotlinIosArm64   # on macOS
```

CI runs the same on Linux (Android and JVM) and macOS (iOS).

## Licence

Apache License 2.0 — see [`LICENSE`](../LICENSE) and [`NOTICE`](../NOTICE) at the top of the repository.

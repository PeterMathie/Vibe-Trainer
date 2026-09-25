# Futuristic monochrome dashboard polish

> **Status:** Approved production implementation specification.
> **Stacked baseline:** PR #8 branch `pmathie-cicpilot-polish-feedback-progress` at commit `78941221638bce500a46eee9ca9456b2c54072f9` (`Align Freshness figures with legend`).
> **Scope:** Visual-system and interaction polish only. Product semantics, navigation destinations, persistence, calculations and PR #8's completion guarantees remain unchanged.

## Purpose and authority

This document is the execution plan for a restrained, technical dashboard treatment optimized first for the **Mono dark** preset. It is intentionally separate from:

- [`PRODUCT_REQUIREMENTS.md`](PRODUCT_REQUIREMENTS.md), which remains authoritative for current product behavior and already requires semantic tokens, four complete light/dark palettes, palette-specific Freshness scales and **Follow system** as the default appearance;
- [`FUTURE_IDEAS.md`](FUTURE_IDEAS.md), which remains the home for exploratory product ideas rather than implementation detail; and
- [`APP_REVIEW.md`](APP_REVIEW.md), which documents the current reviewer-facing application rather than a future design.

The user prefers **Mono** and the restrained neutral hierarchy of **VS Code Dark Modern**. That preference sets the optimization and review order, not the app default: theme mode remains **Follow system**, and Ocean, Sunset, Forest and Mono must all remain complete in light and dark appearances. Ocean dark already maps its core roles to VS Code Dark Modern; Mono remains a deliberately near-monochrome preset with palette-derived state colour (`ui/theme/VibeDesignSystem.kt`, `VibePalettes.Ocean`, `VibePalettes.Mono`, and `VibeThemeMode`, baseline commit above).

### Status vocabulary

| Label | Meaning |
|---|---|
| **Approved direction** | Existing product requirement or explicit constraint for this phase; implementation may proceed without reopening the decision. |
| **Recommended proposal** | Concrete default in this plan, precise enough to implement, but requiring visual review before broad migration. |
| **Open decision** | Material alternative that must be resolved at the stated gate. |

## Approved direction

1. The result is an information-first technical dashboard: calm, precise, dense enough to scan, and rewarding only when the data or user action warrants emphasis.
2. Mono dark is the reference treatment. All four palettes and accessible light mode ship together; no screen-specific Mono-only colours or behavior.
3. Depth comes primarily from tonal layering, hairline boundaries and restrained top-edge light. Shadows are secondary and selective.
4. Glow is reserved for active, selected, completion and data-focus states. It is never a persistent decoration across every card or heading.
5. Existing behavior remains intact: local-first persistence; Freshness rather than fatigue claims; schedule-free programmes and habits; finish-gated derived views; current six primary destinations; current drag semantics; and current graph calculations.
6. PR #8's completion sequence remains persistence-first, one-shot, palette-aware and reduced-motion aware. This phase may refine its presentation, not its lifecycle semantics.
7. Accessibility, readable light mode, dynamic font scaling and reduced motion are release requirements, not later cleanup.
8. Tapping a muscle opens a Material modal bottom sheet over Home. Muscle selection must never resize, reflow, zoom or shift either SVG figure; the body maps remain fixed and visible behind the scrim.

## Explicit anti-goals

- No glossy glassmorphism, chrome bevels, holographic rainbow treatment or “gamer” control panel.
- No pervasive neon, bloom around ordinary text, animated scanlines, particle backgrounds or infinite ambient animation.
- No replacing semantic colour with hard-coded cyan, green or white.
- No reduction in text contrast, touch target size, focus visibility or chart legibility to create atmosphere.
- No denser workout input rows at the expense of 200% font-scale use or one-handed accuracy.
- No changes to calculations, data models, navigation information architecture, autosave, reorder persistence, workout completion ordering, backup format or default theme mode.
- No screen-by-screen styling forks. Reusable tokens and primitives must land before broad migration.
- No shadow as the only state signal, and no glow as the only selection, error or completion signal.
- No inline muscle-detail block that changes the Home layout or either SVG's measured bounds.

## Visual foundation

### Elevation model

Compose elevation is not a literal light model. In dark themes, a default shadow can disappear into the page while Material tonal elevation can unintentionally tint a neutral surface. The implementation must therefore separate **surface tone**, **border/highlight** and **physical shadow** instead of assuming that `shadowElevation` alone communicates depth. `VibeSurface` should own shape, clipping, border and shadow order so a clip does not cut off the shadow. Dark surfaces use stronger tonal separation and weaker shadows; light surfaces use smaller tonal shifts and slightly clearer shadows.

The current palette already exposes `background`, `surface`, `surfaceRaised`, `surfaceSelected` and `border`; `VibeCard` currently renders every card with `surface` plus a 1 dp border (`ui/VibeCheckApp.kt`, `VibeCard`). The first implementation slice should add semantic elevation tokens without changing stored palette IDs.

#### Proposed semantic roles

| Level | Intended use | Existing role seed | Mono dark mock | Mono light mock | Border/top edge | Shadow |
|---|---|---|---|---|---|---|
| Page / L0 | Scaffold and full-screen background | `background` | `#171819` | `#F8F9FA` | None | None |
| Inset / L-1 | Chart well, input group, recessed data region | New `surfaceInset`, derived then made explicit | `#141516` | `#F1F3F4` | Inner hairline: on-surface 5% dark / 8% light | None |
| Card / L1 | Ordinary content grouping | `surface` | `#1F2123` | `#FFFFFF` | 1 dp on-surface 9% dark / 11% light; top edge 5% dark | None in lists |
| Raised / L2 | Pressable card, active control, dragged item | `surfaceRaised` | `#282B2E` | `#ECEFF1` | 1 dp on-surface 12%; top edge 8% dark | 2 dp resting only when interaction affordance needs it |
| Selected / L2s | Selected card, active tab/toggle/data point | `surfaceSelected` + accent-derived border | `#33373B` | `#DDE3E7` | 1 dp accent at 55% dark / 65% light | Optional 2 dp; never selection's only cue |
| Floating / L3 | Menu, tooltip, bottom sheet, transient summary | New `surfaceFloating` | `#303337` | `#FFFFFF` | 1 dp on-surface 14%; top edge 10% dark | 6 dp |
| Modal / L4 | Dialog surface above scrim | New `surfaceModal` | `#34373B` | `#FFFFFF` | 1 dp on-surface 16%; top edge 12% dark | 12 dp |

The mock colours show the Mono target, not universal literals. Ocean, Sunset and Forest derive equivalent roles from their own neutral surface ladder, then store explicit reviewed values in each `VibePalette`. Do not calculate final colours at call sites.

#### Proposed elevation and shadow tokens

| Token | Android elevation | Ambient shadow | Key/spot shadow | Offset/blur intent | Use |
|---|---:|---|---|---|---|
| `none` | 0 dp | None | None | None | Page, inset, ordinary list cards |
| `interactiveRest` | 1 dp | Black 14% light / 18% dark | Black 18% light / 24% dark | 0/1 dp, tight | Standalone pressable card only |
| `interactivePressed` | 0 dp | None | None | Surface moves down 1 dp | Button/card press |
| `dragged` | 8 dp | Black 20% light / 24% dark | Black 28% light / 36% dark | 0/4 dp, broad | Active reorder item |
| `floating` | 10 dp | Black 18% light / 24% dark | Black 28% light / 38% dark | 0/6 dp, broad | Menus, tooltips, sheets |
| `modal` | 16 dp | Black 20% light / 28% dark | Black 32% light / 44% dark | 0/8 dp, broad | Dialogs only |

Use `graphicsLayer { shadowElevation; ambientShadowColor; spotShadowColor; shape; clip = false }` or a tested equivalent in one primitive. Validate API 26+ behavior because colored shadow support and rasterization differ by API and device. Do not put a shadow on:

- every card in a scrolling list;
- chart grid lines, dividers, text, icons or disabled controls;
- an element whose parent clips the shadow;
- an animated object when the same hierarchy is clear from tone/border and shadow rasterization harms frame pacing;
- a high-contrast light-mode surface where the border already establishes separation.

### Surface primitive

**Recommended proposal:** introduce `VibeSurface(level, state, onClick?, modifier, content)` and make `VibeCard` a compatibility wrapper around `level = Card`. It should apply, in order: physical shadow outside bounds, shape, fill, optional subtle top-edge highlight, border, clip, interaction semantics and content padding. A top-edge highlight is a 1 dp vertical gradient over at most the top 18% of the shape; it must not resemble a glossy band.

### Selective glow

Glow is a state accent, not an elevation system.

| State | Colour source | Maximum alpha | Maximum blur/spread | Duration | Notes |
|---|---|---:|---:|---:|---|
| Active destination/control | `palette.accent` | 12% dark / 8% light | 8 dp | Static while active | Pair with fill, icon or border change |
| Selected card/data point | Relevant semantic series colour | 16% dark / 10% light | 10 dp | 120 ms in, 90 ms out | One selected target per local context |
| Completion muscle | Existing Freshness colour for that muscle | 24% dark / 14% light | 12 dp | Existing one-shot hold; no loop | Preserve deterministic order |
| PR/completion summary | `accent` or factual series colour | 14% dark / 8% light | 14 dp | 180 ms in, 420 ms hold, 180 ms out | Only when event warrants it |
| Error focus | `danger` | 10% | 6 dp | No pulse | Border and message remain primary |

Rules:

- Derive every glow from the active palette or the represented data series; never use a global neon colour.
- Render at most two glow layers on screen at once, excluding PR #8's bounded muscle sequence.
- Prefer `drawWithCache` and a simple alpha/scale change. Do not allocate paths, brushes or image filters per frame.
- No continuous pulse. No glow behind body copy, disabled controls, ordinary navigation icons or all chart traces.
- Reduced motion removes animated expansion, pulse and travel. It may retain a static 1 dp accent border or a non-blurred 8% halo if contrast remains clear.
- Light mode uses lower alpha and usually a border rather than blur.

## Dashboard typography

Use the platform sans-serif for prose and controls. Introduce a semantic numeric style using the same family with tabular figures; do not add a font dependency solely for atmosphere.

| Role/token | Size | Line height | Weight | Tracking | Use |
|---|---:|---:|---|---:|---|
| `dashboardMetricXL` | 40 sp | 44 sp | 600 | -0.5 sp | One primary number in a roomy summary |
| `dashboardMetric` | 28 sp | 32 sp | 600 | -0.2 sp | Card metric, timer, PR value |
| `dashboardMetricCompact` | 20 sp | 24 sp | 600 | 0 | Dense workout/progress value |
| `dashboardLabel` | 13 sp | 18 sp | 600 | 0.2 sp | Human-readable label |
| `dashboardMicroLabel` | 11 sp | 14 sp | 600 | 0.7 sp | Short section/category label only |
| `dashboardBody` | 15 sp | 22 sp | 400 | 0 | Explanatory content |
| `dashboardAnnotation` | 12 sp | 16 sp | 400 | 0.1 sp | Axes, units, timestamps, metadata |

Numeric styles use `fontFeatureSettings = "tnum"` and preserve locale-aware decimal/group separators. Units are separate annotation spans when doing so improves alignment, but TalkBack receives one natural phrase.

Uppercase micro-labels are permitted only for short, fixed interface labels such as `FRESHNESS`, `ACTIVE WORKOUT` and `WORK TRACKER`: maximum 18 localized characters before falling back to title case, no user-generated strings, no sentences, and no reliance on uppercase alone for hierarchy. Use locale-aware casing. Never uppercase exercise, programme, habit or user-entered names.

Typography constraints:

- At 200% font scale, labels may wrap; metrics may reduce to the compact metric role but never below 18 sp.
- No fixed-height text containers except controls whose tested minimum height expands with font scale.
- Preserve full localized strings; ellipsis is acceptable only for repeated selectable labels with an accessible full description.
- Use `maxLines = 1` only where an adjacent interaction makes expansion impossible and 320 dp / 200% tests prove usability.
- Keep line length near 45–75 characters for explanatory text.

## Instrument-style graphs

The current `MiniChart` and `RpeBarChart` draw simple axes/traces on `Canvas` and map pointer position directly to data index (`ui/ProgressScreen.kt`, `MiniChart` and `RpeBarChart`). Replace their rendering through shared graph primitives without changing `SessionProgress`, scoring, selected point semantics or data ordering.

### Graph visual specification

| Element | Specification |
|---|---|
| Plot well | Inset/L-1 surface, 12 dp radius, 12 dp internal padding |
| Major grid | 1 dp, `textPrimary` at 9% dark / 12% light; maximum 5 horizontal and 4 vertical lines |
| Minor grid | Optional only above 240 dp plot width; 1 dp at 4% dark / 6% light |
| Axes | 1 dp `textSecondary` at 55%; no heavy L-shaped frame |
| Primary trace | 2 dp, palette `accent`; rounded joins/caps |
| Smoothed/secondary trace | 2.5 dp, palette `secondary`; distinguish additionally by dash or point shape |
| Data points | 5 dp visual dot; selected 7 dp with 2 dp contrasting core |
| Touch target | Minimum 48 × 48 dp logical hit region around nearest point |
| Crosshair | 1 dp dashed/segmented line, `textSecondary` at 60%; only while selected |
| Tooltip | Floating/L3 surface, 12 dp radius, metric + date + unit; constrained inside plot/window |
| Annotation | `dashboardAnnotation`, tabular numerals, no rotated labels |

All palettes must preserve trace distinction without depending on hue. Mono uses luminance, line weight and dash/point shape. Ocean, Sunset and Forest may use their semantic accent/secondary roles but must pass the same grayscale review. Freshness and habit heat-map colours retain their separate centralized scales; graph polish must not remap data meaning.

### Graph states

| State | Required result |
|---|---|
| Empty | Stable plot-height card with plain-language requirement and one relevant action if available; no fake trace |
| Loading | Static skeleton axes and 2–3 plot bands; no shimmering scanline; semantic “Loading chart” |
| Partial / one point | Center the point, show date/value, and omit a misleading trend line |
| Error | Preserve card title and retry/action context; visible error text and icon, no success-shaped empty fallback |
| Selected | Crosshair, tooltip and selected-point semantics; tap/drag and keyboard/accessibility actions select the same datum |
| Reduced motion | Trace and points appear without draw-on animation; selection snaps while tooltip remains |

Initial reveal may draw traces over 280 ms with `FastOutSlowInEasing`; points fade over 160 ms after the trace starts. Selection retargets in 90 ms and must interrupt immediately. Never animate the y-axis domain while the user drags a crosshair. Pointer movement should update only selection overlay state, not rebuild the data path.

## Cards and reusable components

### Shape, spacing and state

**Recommended proposal:** retain the current 16 dp card radius as the compatibility default (`VibeShapes.card`) and introduce a small controlled scale:

| Token | Value | Use |
|---|---:|---|
| `shapeSmall` | 8 dp | Heat cells, compact badges |
| `shapeControl` | 12 dp | Buttons, fields, segmented controls |
| `shapeCard` | 16 dp | Standard cards |
| `shapePanel` | 20 dp | Summary, sheet or large dashboard region |
| `shapePill` | 50% | Status pill only |

Keep the current 4/8/16/24/32 dp spacing scale and add `12 dp` as `compact` only if migration reveals repeated legitimate use. The 16 dp screen gutter remains the phone baseline. Use an 8 dp internal rhythm; 4 dp is reserved for tightly related metadata, not separate actions.

| Component state | Visual and behavioral requirement |
|---|---|
| Resting | Level-specific fill and hairline; no glow |
| Pressed | Translate down 1 dp and reduce shadow; 90 ms; retain ripple unless reduced motion |
| Dragged | L2 fill, 8 dp shadow, 1.015 scale maximum, accent hairline; direct finger tracking remains unanimated |
| Insertion target | 2 dp accent rule plus 6 dp local spacing opening; not a full-card glow |
| Selected | Selected surface, accent border and semantic selected state |
| Completed | Check/status text plus brief palette-derived emphasis; never colour alone |
| Disabled | Content 38%, container 50%, no elevation/glow; semantics remain disabled |
| Keyboard/accessibility focus | 2 dp high-contrast focus ring outside component; independent of selected state |

Create reusable primitives before changing screens:

- `VibeSurface` and `VibeCard` compatibility wrapper;
- `VibeMetric`, `VibeMicroLabel` and numeric style helpers;
- `VibeSectionHeader`, `VibeDivider` and `VibeStatusPill`;
- `VibeGraph`, `VibeGraphTooltip` and `VibeGraphState`;
- `VibeSkeleton` with static reduced-motion mode;
- `TechnicalBackdrop` with strictly bounded texture;
- shared interaction modifiers for press depth, focus ring and selective glow.

Migration must be mechanical and incremental. A screen may not introduce its own elevation, border or glow constants unless the shared primitive cannot express a documented requirement and the exception is added here first.

## Interaction and motion

### Timing table

| Interaction | Duration | Easing | Reduced-motion equivalent |
|---|---:|---|---|
| Button/card press in | 70 ms | Linear-out-slow-in | Immediate fill/border change |
| Button/card release | 110 ms | Fast-out-slow-in | Immediate |
| Tab/toggle selection | 140 ms | Fast-out-slow-in | Immediate selected state |
| Card expand/collapse | 180 ms | Standard decelerate | Content swaps without size animation |
| Page content transition | 180 ms enter / 120 ms exit | Fade + 8 dp shared-axis travel | No travel; optional 80 ms crossfade |
| Muscle detail sheet | 180 ms enter / 140 ms exit | Material emphasized decelerate/accelerate slide + scrim fade | Immediate sheet placement with an optional 80 ms scrim crossfade |
| Drag lift | 120 ms | Fast-out-slow-in | Immediate L2/border |
| Displaced reorder row | 160 ms | Fast-out-slow-in | Immediate placement |
| Drop settle | 180 ms | Emphasized decelerate | Immediate placement |
| Number change | 160 ms | Decelerate | Text replacement without rolling |
| Skeleton state change | 160 ms | Linear crossfade | Immediate |
| Graph reveal | 280 ms max | Fast-out-slow-in | Immediate |
| Completion summary enter | 220 ms | Emphasized decelerate | Immediate |

Rules:

- User input always interrupts and retargets from the current visual value; no queued decorative animations.
- Direct manipulation follows the pointer exactly. Only displaced rows and release settlement ease.
- Number changes use a short crossfade/vertical shift of at most 4 dp, never an odometer for ordinary inputs.
- Skeletons use a static tonal step by default. If shimmer is approved later, cap it at 900 ms, one pass, and disable it for reduced motion and battery saver.
- Tab, toggle and page transitions preserve focus and do not announce unchanged content again.
- Coordinate haptics through `VibeHaptics`; visual modifiers never fire haptics. Preserve the current drag-start, threshold-cross and drop events, and ensure each semantic action triggers at most one event (`ui/ActionControls.kt`, `ReorderHandle`; `ui/VibeHaptics.kt`).

### Reorder specification

PR #8's direct handle dragging, accessibility Move earlier/Move later actions, completion-only persistence and reduced-motion behavior are approved behavior. Polish it as follows:

1. Lift the dragged item to L2 with the `dragged` shadow and maximum 1.015 scale.
2. Show a 2 dp insertion rule before/after the prospective index and open 6 dp of space.
3. Keep the current threshold hysteresis so direction changes do not oscillate.
4. On drop, persist once, play one drop haptic, settle within 180 ms and restore the ordinary card surface.
5. On cancellation, restore source order with no persistence or success haptic.
6. Reduced motion keeps direct translation, insertion rule and state border but snaps displaced rows and drop settlement.

## Muscle selection bottom sheet

**Approved decision:** `HomeScreen` must replace its current inline selected-muscle detail with a Material 3 modal bottom sheet. Opening and closing the sheet overlays the existing page; it must not alter the measurement, placement, scale, viewport, alignment or front/back relationship of either `MuscleMap`.

### Sheet behavior

- Use Material 3 `ModalBottomSheet` with a visible drag handle, modal scrim and a remembered `SheetState`.
- On portrait phones, open partially expanded when the platform can show the title, Freshness state and last-training fact without clipping. An upward swipe or the explicit expansion affordance expands it to show longer contribution details.
- Expand immediately when the required content cannot fit in the partial state, including landscape and large-font cases. Do not truncate required facts merely to preserve a partial state.
- Dismiss by downward swipe, system Back, scrim tap or an explicit Close action. All paths must execute the same state cleanup and focus-restoration behavior.
- Preserve the selected Home date, displayed month, Strength/Stretch mode, front/back map state, map scroll/viewport and any completion state across open/dismiss. Dismissal must not navigate or reset the date to today.
- The scrim uses the standard modal semantic role and a maximum black alpha of 44% in dark mode / 32% in light mode. Both body figures remain recognizable behind it.
- The selected muscle retains its existing clear outline/highlight behind the scrim for the entire time the sheet is visible. Do not add zoom, scale or a second layout copy of the anatomy.
- **Recommended selection lifecycle:** retain `selectedMuscleId` while the sheet is open; on dismissal, restore accessibility focus to the tapped muscle and then clear the visual selection. Preserve the muscle ID only as a short-lived focus-restoration target, not as persistent Home state. A subsequent tap opens a fresh detail sheet.
- Trigger one `VibeHapticEvent.SELECTION` only after a valid tappable muscle resolves and the sheet-open request succeeds. Sheet expansion, collapse and dismissal do not trigger additional haptics.

### Content hierarchy

The sheet is factual and ordered for scanning:

1. Drag handle and a header row containing the localized muscle name and explicit Close action.
2. Current Freshness state: palette colour swatch, human-readable band label and a plain-language meaning such as “trained within 24 hours.” Colour is supplementary, never the only state.
3. “Last trained” or “Last stretched” date/time according to the current Strength/Stretch mode; show **No data** when no qualifying finished session exists.
4. Current seven-day set-equivalents when available, clearly labelled as training dose rather than fatigue or recovery.
5. Contributing recent exercises or sessions when the current derived state provides them. Use a short list ordered by recency; collapse overflow behind an in-sheet disclosure rather than increasing the underlying page height.
6. A navigation/action row only when an equivalent destination or action is already supported by the current app, such as opening an existing historical day. Do not invent muscle-specific coaching, exercise recommendations, scheduling, recovery advice or a new destination.

The accessible title combines muscle and mode, for example “Chest Freshness details” or “Hamstrings Stretch details.” The Freshness state exposes one meaningful phrase, not separate unlabelled colour and age nodes.

### Accessibility and responsive layout

- Opening moves accessibility and keyboard focus into the sheet, beginning at its title. Background content is modal and unavailable to traversal while the sheet is open.
- Traversal order is title/Close, current state, meaning, last event, dose, contributions, then any supported action.
- The Close action has a localized content description and minimum 48 × 48 dp target.
- Dismissal restores focus to the exact front/back muscle target that opened the sheet. If that node is no longer available because the underlying mode changed externally, restore focus to the relevant map heading rather than dropping focus.
- Each tappable map region retains its current muscle name, selected state and Freshness semantics for TalkBack. Opening the sheet must announce its meaningful title and current state once, without replaying the whole map.
- At 1.3× font scale and above, allow the sheet to expand and scroll internally. Do not compress type, overlap the drag handle or hide Close.
- At 320 dp width, use full available sheet width and wrap metadata. In landscape, use the platform modal bottom-sheet width cap, start expanded, respect cutout/navigation insets and keep content vertically scrollable.
- On 600 dp+ windows, constrain the sheet to the Material-recommended maximum width and keep it bottom anchored; do not turn it into an inline side panel in this phase, because that could move the maps.

### Motion and interruption

- Enter over 180 ms with the Material bottom-sheet slide and scrim fade; exit over 140 ms. The selected-muscle highlight appears before the scrim reaches full alpha so the source remains visually connected.
- Dragging directly controls sheet position. Back, scrim tap or a second muscle-selection request interrupts and retargets from the current sheet position; never queue sheet animations.
- If another valid muscle is tapped through an accessibility action after dismissal begins, complete dismissal, restore focus, then open the new muscle once. Do not render two sheets.
- Reduced motion places/removes the sheet without slide travel; an optional scrim crossfade is capped at 80 ms. Content, modal semantics, focus movement and focus restoration are identical.

### Stable geometry requirement

For a fixed window, density, font scale, selected date and mode, capture the root-coordinate bounds of the front and back SVG semantics nodes:

`boundsBefore == boundsDuringSheet == boundsAfterDismissal`

Equality means all four edges differ by no more than **0.5 physical pixel** to account for test rounding. Their internal scale/viewport parameters must also remain identical. The sheet may cover part of the figures visually with its scrim/surface; it may not cause either figure to remeasure or move.

## Technical texture

Texture is optional and subordinate to content.

| Treatment | Allowed | Prohibited |
|---|---|---|
| Grid | Page/header negative space, graph plot wells | Behind forms, body copy, muscle map, photos, dialogs |
| Radial falloff | One static accent-derived 2–4% wash behind Home summary/completion summary | Every card, list rows, light-mode reading surfaces |
| Scanline | At most a static 1 px line pattern at 1–2% in an approved graph well experiment | Animated scanlines, full-screen overlay, text/input backgrounds |
| Noise | Not in initial scope | Runtime random noise, bitmap allocation per frame |

Recommended grid: 32 dp major spacing, optional 8 dp minor spacing, 1 physical pixel strokes, on-surface at 2.5% dark / 2% light. Clip to one container, cache paths/brushes with `drawWithCache`, and stop rendering when fully covered. No infinite animation, runtime blur or per-frame allocation. Texture must disappear in high-contrast review if it competes with data and may be disabled on low-RAM devices without changing hierarchy.

## Completion experience and PR #8 integration

### Preserved contract

PR #8 persists a valid workout before returning Home, emits a one-shot `WorkoutCompletionEvent`, consumes it once, highlights only affected mapped muscles in deterministic order, provides a generic acknowledgement when no muscles are mapped, announces through a polite live region, removes confetti and stagger under reduced motion, and coordinates a single success haptic. These guarantees are implemented by `TrainingRepository.finishWorkout`, `MainViewModel.finishWorkout`, `OneShotEventState`, `HomeScreen`, `completionAnimationPlan`, `CompletionConfetti` and `MuscleMap` at the stacked baseline.

This phase must not:

- emit before persistence succeeds;
- replay after configuration/process recreation once consumed;
- reorder affected muscle IDs non-deterministically;
- delay navigation or data refresh until animation completes;
- duplicate the success haptic;
- add confetti to reduced motion;
- infer praise, recovery or coaching beyond stored facts.

### Recommended polished flow

1. Home renders the persisted Freshness state immediately.
2. Affected muscles receive the existing one-shot palette-derived illumination, refined to a 1 dp bright core plus a maximum 12 dp halo at the limits above.
3. Existing restrained confetti remains for coloured palettes, but Mono dark uses 12–16 short monochrome line/dot particles sampled from `textPrimary`, `textSecondary`, `accent` and the affected Freshness values. No rainbow and no screen-filling burst.
4. Completion confirmation must not add a card, banner, compact row or other document-flow content to Home. The one-shot affected-muscle illumination, bounded confetti, success haptic and polite accessibility announcement are the confirmation, and the Freshness map keeps identical bounds before, during and after completion.
5. The summary auto-collapses after 3 seconds only if focus is not inside it; it remains available in the freshly updated Home content until dismissed or navigation changes. The one-shot animation and durable summary visibility must be modeled separately.
6. Reduced motion shows final muscle colours and the summary immediately, with a static check icon/accent border and no particles, stagger, scale or travel.
7. Sessions with no mapped muscles use the same summary card and generic acknowledgement without a body-map halo.

**Approved decision (supersedes the earlier recommended default):** do not show a completion summary, “Session saved” row, notification card or visible acknowledgement bubble. No completion state reserves Home layout space. When no muscles are mapped, use only the same non-layout-shifting polite accessibility announcement.

## Iconography, density and system UI

- Continue Material outlined icons for ordinary navigation and actions. Use filled variants only for the currently selected primary destination when a matching pair exists.
- Standard icon sizes: 20 dp compact, 24 dp default, 28 dp primary metric status. Minimum interactive container remains 48 dp.
- Every icon-only action requires a localized content description; decorative icons are null semantics.
- Do not mix line weights within an action cluster. Custom technical icons require a separate reviewed asset task.
- Dividers are 1 physical pixel where possible, on-surface 10% dark / 12% light, inset to align with text. Prefer spacing over dividers between unrelated cards.
- Default density remains 16 dp screen gutter, 16 dp card padding, 8 dp related-item gap and 16 dp section gap. Active workout inputs may use 12 dp card padding but not smaller touch targets.
- Keep edge-to-edge. `Scaffold` content must consume system insets once; bottom navigation must not double-apply navigation insets.
- Fix the stale `MainActivity` assumption that the app is always dark: system-bar icon appearance must follow the resolved palette from `VibeCheckTheme`, including Follow system and explicit Light. System and navigation bar backgrounds remain transparent only when contrast is verified against the content behind them.
- Large screens use a centered content column (recommended max width 840 dp) and may place independent dashboard cards in two columns. Do not merely stretch phone graphs or workout fields.
- At 320 dp width and 200% font scale, switch paired controls to vertical layout before truncating essential labels.

## Screen-by-screen application matrix

| Surface | Change in this phase | Must remain untouched |
|---|---|---|
| Home / Freshness | L1 Freshness panel first at an invariant height, restrained technical backdrop in unused header space, improved metric/annotation hierarchy, refined date control, current map illumination, one-shot non-layout-shifting completion effects, then the compact active-workout resume card when present, then crisp Work tracker cells; constrained layouts scroll rather than shrink the SVG. Replace inline muscle details with the approved modal bottom sheet while keeping both SVG bounds fixed | Freshness bands, continuous OKLab scrub interpolation, neutral no-data, selected-date/month/front-back authority, month bounds, figure geometry/hit regions, Strength/Stretch semantics |
| Programme list | Shared pressable cards, selection/focus ring, technical micro-metadata, refined expand/collapse and drag lift/insertion feedback | Schedule-free model, preview contents, direct start/edit, persisted ordering, single-workout simplification |
| Programme editor | Shared card/surface levels, clearer prescription metrics, common action hierarchy and reorder states | Assignment ownership, add/edit/remove/duplicate/archive behavior, start/resume/discard logic |
| Active Workout / Stretch | Highest information-density treatment: compact tabular metrics, inset set rows, stronger active timer state, pressed depth, static saved-state acknowledgement | Autosave, durable row IDs, finish validation, input configuration, rest behavior, notes ownership, no incomplete-workout warning |
| Habits | Shared cards, icon/metric alignment, selected choice/focus states, more legible heat cells | Today-only entry, no schedules/streak pressure, field semantics, auto-save, user colours and choice boundaries |
| Progress graphs / heat maps | Shared instrument graph, plot well/grid, crosshair/tooltip, chart states, tabular axes, grayscale distinction | Scoring, baselines, rolling trend, PR definitions, variation filtering, card order, per-habit colour meaning |
| History / day detail | Timeline-like metadata rhythm, shared surfaces/dividers, improved empty/search/error treatment | Search/edit behavior, reconstructed maps, correction propagation, stored facts |
| Body / measurements | Shared graph, metric typography, selected calendar state, photo card hierarchy | Record precision, linked Progress records, photo storage/export/delete behavior, persisted card order |
| More / Settings / Style | Section grouping, consistent list rows, palette preview depth, correct light/dark system bars | Six primary destinations, visible data controls, all four palettes, Follow system default, preference persistence/backup |
| Dialogs / sheets / menus | L3/L4 surfaces, focus ring, inset content groups, consistent actions, responsive max width | Validation, dismiss semantics, destructive confirmations and explicit errors |
| Empty / error / loading | Shared `VibeStatePanel`; stable layout, actionable copy, static skeletons, explicit retry/error | No fake data, no silent fallback, no dead controls |

### Pointer-anchored reorder behavior

All drag-to-reorder tile and card lists use the same pointer-anchored reorder primitive. The active item is an elevated overlay whose root-coordinate translation preserves the original grab point through every keyed list relayout; only displaced neighbors animate into their new positions. Candidate crossings use measured item geometry plus a directional dead zone, with one haptic per accepted crossing and one haptic after a successful drop. Persistence occurs once, on successful drop only. Cancellation, Back, or pointer interruption restores the source order without persistence.

When the pointer approaches the visible scrolling viewport edge, the list scrolls automatically at a bounded speed that increases smoothly with edge proximity. The viewport is measured after system insets and includes the actual visible list bounds. Auto-scroll stops immediately away from the edge, on drop, or on cancellation, and crossing candidates are recomputed after layout updates. Stable keys, partially visible first/last items, rapid direction reversal, 200% font scale, reduced motion, and accessibility Move earlier/Move later actions retain equivalent behavior.

### Stable habit choice intensity

“Choose from a list” fields store every option with a stable ID, an explicit `LIGHT`, `MEDIUM`, or `DARK` bucket, and a stable position within that bucket. The editor presents three labelled drag groups with counts, explanations, and empty drop targets. Reordering within a group changes only local order; moving between groups changes only that option’s explicit intensity. New options start in Light, empty groups are valid, and deletion never reclassifies surviving options. Duplicate labels remain invalid under the existing product rule.

Room migration 13→14 converts legacy positional options deterministically using the prior visible boundary algorithm and snapshots the selected option ID and intensity onto existing daily values. New logs always snapshot both, so later option moves or deletion cannot recolour history. Legacy backups without the new fields are upgraded during import; exports always emit explicit option metadata and logged snapshots. No-response days remain neutral, and all palettes use their audited three-step habit ramp.

## Architecture and file-level implementation map

All paths and symbols below describe the PR #8 stacked baseline commit stated at the top.

| Current path/symbol | Planned responsibility/change |
|---|---|
| `app/src/main/java/com/petermathie/vibecheck/ui/theme/VibeDesignSystem.kt` — `VibePalette`, `VibeSpacing`, `VibeShapes`, `VibeTypography`, `VibeCheckTheme` | Add explicit surface/effect roles, semantic typography roles and composition locals for elevation/motion. Preserve palette IDs and `VibeThemeMode.SYSTEM`. |
| `app/src/main/java/com/petermathie/vibecheck/ui/theme/FreshnessColors.kt` | Keep Freshness and habit scales separate; add no generic dashboard remapping here. |
| Proposed `ui/theme/VibeElevation.kt` | `VibeSurfaceLevel`, `VibeElevationTokens`, per-mode shadow/highlight values. |
| Proposed `ui/theme/VibeMotion.kt` | Named duration/easing tokens and reduced-motion resolution; no business-event ownership. |
| Proposed `ui/components/VibeSurface.kt` | Surface/card, focus, press depth and selective-glow implementation; compatibility `VibeCard`. |
| Proposed `ui/components/VibeTypography.kt` | `VibeMetric`, `VibeMicroLabel`, tabular numeral helper and accessible combined semantics. |
| Proposed `ui/components/VibeGraph.kt` | Cached paths, grid/axes/traces, selection overlay, tooltip and empty/loading/error states. |
| Proposed `ui/components/VibeStatePanel.kt` | Shared empty/loading/error content and static skeleton. |
| Proposed `ui/components/TechnicalBackdrop.kt` | Cached bounded grid/radial texture with performance guardrails. |
| Proposed `ui/components/MuscleDetailsSheet.kt` | `MuscleDetailsUiState`, modal sheet content hierarchy, partial/expanded policy, common dismissal callback and accessibility semantics. It receives already-derived facts and owns no repository access. |
| `app/src/main/java/com/petermathie/vibecheck/ui/VibeCheckApp.kt` — `VibeCard`, `HomeScreen`, `ActivityHeatmap`, `MonthlyActivityHeatmap`, `PrimaryNavigationBar` | Move generic card implementation to components; adopt tokens; integrate completion effects without document-flow UI; replace inline selected-muscle content with `MuscleDetailsSheet`; preserve map measurement, navigation and calendar semantics. |
| `app/src/main/java/com/petermathie/vibecheck/ui/ProgressScreen.kt` — `MiniChart`, `RpeBarChart`, `ProgressCardShell` | Migrate to shared graph APIs and state model without changing domain inputs. |
| `app/src/main/java/com/petermathie/vibecheck/ui/ActionControls.kt` — `VibeActionButton`, `ReorderHandle`, `reorderItemFeedback` | Apply press/elevation tokens and insertion target while preserving reorder state/persistence and haptic events. |
| `app/src/main/java/com/petermathie/vibecheck/ui/CompletionCelebration.kt` | Keep deterministic one-shot plan; add palette/reduced-motion visual parameters and Mono particle style. |
| `app/src/main/java/com/petermathie/vibecheck/ui/anatomy/MuscleMap.kt` | Refine bounded celebration halo only; keep geometry, taps, selection and interpolation. |
| `app/src/main/java/com/petermathie/vibecheck/ui/MainViewModel.kt`, `OneShotEventState.kt` | Preserve event lifecycle; expose only already-persisted factual summary fields if repository result already owns them. |
| `app/src/main/java/com/petermathie/vibecheck/ui/VibeHaptics.kt` | Keep single coordinator and throttle; add no visual ownership. |
| `app/src/main/java/com/petermathie/vibecheck/MainActivity.kt` | Remove dark-only system-bar assumption; let resolved theme control icon appearance. |
| `ProgrammeEditor.kt`, `WorkoutEditor.kt`, `TrackerScreen.kt`, `HistoryScreen.kt`, `HistoryDayScreen.kt`, `SettingsScreen.kt`, `StyleScreen.kt`, `ArchiveScreen.kt` | Migrate screen surfaces only after primitives and golden/reference states are approved. |

Do not move domain calculations or persistence into visual components. No Room migration or backup-format change is expected. Adding factual completion summary fields is compatible only if it uses the already persisted completion result; otherwise defer it.

## Delivery sequence

Each production slice should be a small reviewable commit or stacked PR. Do not combine all screen migration into one review.

| Slice | Content | Dependency | Review/rollback boundary |
|---|---|---|---|
| 0. Visual fixtures | Screenshot test host, deterministic demo states, token preview composable | PR #8 baseline | Test-only; removable without product impact |
| 1. Tokens/primitives | Palette surface roles, elevation/motion/type tokens, `VibeSurface`, compatibility `VibeCard` | Slice 0 | Revert returns old rendering with no screen behavior change |
| 2. Graph system | Shared graph renderer and Progress/Body migration | Slice 1 | Revert graph package and two call-site migrations; domain untouched |
| 3. Home/completion | Home hierarchy, fixed-geometry muscle detail sheet, texture and refined non-layout-shifting Mono one-shot visuals | Slices 1–2; PR #8 event contract | Revert presentation while retaining PR #8 completion behavior |
| 4. Interaction migration | Buttons, programme drag states, tabs/toggles, skeleton/state panels | Slice 1 | Per-component migration can revert independently |
| 5. Remaining screens | Workout, Habits, History, More/Settings/Style, dialogs/sheets | Slices 1 and 4 | Commit per screen family |
| 6. Accessibility/performance hardening | Font scale, contrast, benchmark and real-device fixes | All prior slices | Must land before feature is declared complete |

Production implementation should branch from PR #8's merge commit if #8 has merged; otherwise it must stack from exact commit `78941221638bce500a46eee9ca9456b2c54072f9` and clearly declare that dependency. Do not cherry-pick only the completion visuals without the event/persistence tests.

## Compatibility and migration

- No database migration.
- No preference key rename and no change to backed-up `palette`, `themeMode`, `reducedMotion` or `haptic` values.
- Existing custom/legacy palette normalization remains Ocean.
- New palette roles are compile-time data and must be supplied for all eight palette-mode combinations in the same commit.
- Existing `VibeCard` callers remain source compatible during migration.
- Screenshot changes are expected; interaction semantics and test tags/content descriptions should remain stable unless an accessibility improvement requires a documented update.
- Rollback of any visual slice must not require data repair.

## Acceptance criteria

### Functional and visual

1. Mono dark matches the approved reference direction: neutral layered surfaces, crisp hairlines, restrained accent, no pervasive glow and no glossy treatment.
2. Ocean, Sunset, Forest and Mono render every migrated component in light and dark; no fallback to another preset and no hard-coded Mono colour.
3. Appearance still defaults to Follow system on a fresh install and explicit Dark/Light choices persist and round-trip through backup.
4. Home Freshness, date scrubbing, heat-map counts, graph values, programme/workout/habit behavior and navigation are unchanged.
5. PR #8 completion remains persistence-first, one-shot and deterministic; reduced motion has no confetti, stagger, travel, pulse or draw-on chart animation.
6. Every loading, empty and error state keeps stable hierarchy and truthful copy; errors remain explicit.
7. Every interactive target is at least 48 × 48 dp, including graph points through logical hit regions.
8. Opening, partially expanding, fully expanding and dismissing muscle details does not resize, reflow, zoom or shift either SVG. Front/back root bounds before, during and after differ by no more than 0.5 physical pixel per edge.
9. Muscle-sheet dismissal by swipe, Back, scrim and Close preserves selected date, month, mode and map viewport; focus returns to the tapped muscle and the recommended visual selection then clears.

### Contrast

- Body and label text: WCAG 2.2 AA contrast at least 4.5:1.
- Large text at least 24 sp regular or 18.66 sp bold: at least 3:1.
- Icons, focus indicators, chart traces against their plot surface, selected borders and meaningful graphics: at least 3:1.
- Disabled content is exempt from contrast minimums but must remain distinguishable.
- Heat-map and Freshness adjacent states must remain distinguishable by the existing palette tests plus grayscale and colour-vision-deficiency inspection; data meaning never relies on glow.

### Performance

Measure release-like builds on a mid-range physical Android device; emulator results are functional evidence only.

| Target | Acceptance |
|---|---|
| Frame pacing | At least 95% of frames at or below 16.7 ms during graph selection, Home completion and reorder; no frame above 50 ms in a 10-second scripted interaction after warm-up |
| Recomposition | Pointer movement recomposes only selection/tooltip state; static card content and graph data path do not recompose per pointer event |
| Allocation | No bitmap/path/brush allocation per animation frame; graph/reorder steady-state allocation under 1 KB/frame after warm-up |
| Startup | No more than 3% median warm-start regression against PR #8 baseline over 10 Macrobenchmark iterations |
| Memory | No retained completion particle state after event end; no growth across 20 repeated graph selections/reorders |

Use `drawWithCache`, immutable token objects, stable inputs and precomputed graph geometry. If blur/shadow cannot meet the target on supported devices, remove the blur before reducing data or interaction quality.

### Test plan

#### Unit tests

- Every palette supplies every surface/effect role and valid luminance ordering for its mode.
- Contrast thresholds for text, graph traces, focus rings, borders and selected states.
- Motion resolver returns zero/snap equivalents under reduced motion.
- Elevation/state resolver maps resting, pressed, dragged, selected, disabled and focus combinations deterministically.
- Graph coordinate/domain calculation, one-point/NaN/gap handling, nearest-point selection and tooltip clamping.
- Completion plan preserves one-shot timing limits, Mono particle count/colour source and reduced-motion suppression.
- Muscle-detail state maps Strength/Stretch wording, Freshness meaning, no-data and available contribution facts without inventing actions.

#### Compose/UI tests

- Pressed/selected/disabled/focus semantics for shared components.
- Reorder drag, insertion target, cancel, one persistence call, accessibility Move earlier/later and one haptic event per phase.
- Graph touch target, drag selection, accessibility datum navigation, tooltip content, empty/loading/error states and reduced motion.
- Home completion consumes once, announces once, shows persisted data first, and has generic no-muscle handling.
- Muscle tap opens exactly one modal sheet, fires one selection haptic, exposes the required title/state/facts and keeps the selected outline visible behind the scrim.
- Front/back SVG semantic bounds and viewport values are identical before opening, in partial state, in expanded state and after dismissal within the 0.5 physical-pixel tolerance.
- Back, downward swipe, scrim tap and Close each dismiss; all preserve date/month/mode state and restore focus to the originating muscle.
- Muscle sheet at 320 dp width, 1.3× and 2.0× font scale, portrait and landscape expands/scrolls without clipping or background traversal.
- 320 dp width at 100% and 200% font scale for Home, Programme editor, active Workout, Habits, Progress and dialogs.
- Light/dark system-bar icon appearance and single inset consumption.

#### Visual regression matrix

Commit deterministic screenshots with clocks, locale and animation clock controlled.

| Tier | Palettes/modes | Devices/layouts | States |
|---|---|---|---|
| Required per component | Mono dark + Mono light | 411 × 891 dp phone | Resting, pressed, selected, disabled, focus, error, muscle sheet partial/expanded |
| Required per screen | All four palettes × light/dark | 411 × 891 dp phone | Populated canonical state |
| Accessibility | Mono light/dark + Ocean light/dark | 320 dp phone at 200% font | Canonical and dialog states |
| Responsive | Mono light/dark | 600 dp portrait and 840 dp landscape | Home, Progress, Workout, Settings |
| Completion and map details | All four palettes × light/dark | 411 × 891 dp | Completion initial/peak/settled/reduced motion; muscle sheet before/partial/expanded/after |

The full populated screen matrix covers Home/Freshness, Programme list/editor, active Strength workout, active Stretch, Habits, Progress graph/heat maps, History list/day, Body, More, Settings, Style, Archive, dialog, sheet, empty, loading and error.

### QA

Emulator QA after each screen-family slice:

- API 35 reference device, light/dark, all palettes;
- navigation/insets, keyboard-open forms, process recreation and rotation;
- TalkBack traversal/actions, 200% font, display size increase and reduced motion;
- muscle sheet focus entry/restoration, swipe/Back/scrim/Close dismissal, retained Home state and invariant SVG bounds;
- screenshot comparison against deterministic fixtures.

Real-device release gate:

- one mid-range 60 Hz phone and one current high-refresh phone;
- OLED dark-mode banding/shadow review and outdoor light-mode readability;
- gesture navigation and three-button navigation;
- haptic coordination, locked/unlocked completion return, battery saver and reduced motion;
- frame pacing via Macrobenchmark/Perfetto, not visual impression alone.

The phase is not complete from emulator screenshots alone.

## Risks and mitigations

| Risk | Impact | Mitigation/default |
|---|---|---|
| Dark shadows disappear or band on OLED | Depth becomes muddy | Tonal ladder + hairline first; shadow only L2–L4 |
| Glow becomes visual noise | “Gamer” look and reduced legibility | State allow-list, alpha/blur caps, maximum two concurrent layers |
| Added palette roles drift across eight variants | One preset becomes second-class | Compile-time completeness and per-role contrast tests in the token commit |
| Typography breaks dense inputs/localization | Clipping or unusable workout logging | 320 dp/200% gate, adaptive stacks, no fixed text heights |
| Graph polish changes perceived data | Misleading trends | Domain untouched; deterministic coordinate tests and old/new value parity |
| Texture/blur causes jank | Poor training-time usability | Static cached drawing, performance gate, delete effect before accepting regression |
| Completion presentation duplicates one-shot event | Replay or stale success | Keep confirmation entirely ephemeral and preserve `OneShotEventState`; never derive a persistent Home banner from it |
| Broad migration creates inconsistent intermediate UI | Review and rollback become difficult | Primitive-first slices and screen-family commits |
| System-bar ownership is split | Wrong icon contrast in light mode | Resolve icon appearance from active palette in one theme-owned path |
| Bottom sheet remeasures Home or loses focus | Visible map jump and accessibility regression | Overlay-only composition, stable map constraints, bounds assertions and explicit focus requester restoration |

## Open decisions and recommended defaults

| Decision | Recommended default | Review gate |
|---|---|---|
| Mono accent strength | Keep current cool `#9CCFE8` accent for actions/focus; use grayscale for ordinary hierarchy | Token preview before Slice 1 |
| Static texture | One 32 dp, 2.5% grid only in bounded Home/graph regions | Mono dark screenshot review |
| Completion confirmation | No banner, card, compact row or visible bubble; preserve fixed map geometry and use the one-shot map effect, haptic and polite accessibility announcement | Explicit product decision |
| Page transitions | 8 dp shared-axis + fade; no navigation-scale zoom | Reduced-motion and 200% font review |
| Light-mode shadows | Borders for L1, shadow only interactive/floating/modal | All-palette component matrix |
| Chart secondary-series distinction | Colour + dash/point shape | Grayscale/accessibility review |
| Large-screen composition | Two-column dashboard at 600 dp+, centered max width 840 dp | Responsive screenshots |
| Scanline treatment | Do not ship initially | Reopen only with a static graph-only prototype and performance evidence |

Approval of this plan should explicitly resolve these defaults or accept them as written. No production styling should begin before the token preview and Mono dark/light reference screenshots are reviewed.

## Sources

- [`PRODUCT_REQUIREMENTS.md`](PRODUCT_REQUIREMENTS.md), especially Product truth, Homepage and history, Progress, Data and platform, and Visual system; baseline commit `78941221638bce500a46eee9ca9456b2c54072f9`.
- [`APP_REVIEW.md`](APP_REVIEW.md), especially Home, Programmes, Active workout, Progress, Habits, Settings, Personalization and Honest limitations; same baseline.
- [`FUTURE_IDEAS.md`](FUTURE_IDEAS.md), Direction and safeguards and completion experiment history; same baseline.
- `app/src/main/java/com/petermathie/vibecheck/ui/theme/VibeDesignSystem.kt`, `VibePalette`, `VibePalettes`, `VibeThemeMode`, `VibeSpacing`, `VibeShapes`, `VibeTypography` and `VibeCheckTheme`; same baseline.
- `app/src/main/java/com/petermathie/vibecheck/ui/theme/FreshnessColors.kt`, Freshness interpolation and habit heat-map roles; same baseline.
- `app/src/main/java/com/petermathie/vibecheck/ui/VibeCheckApp.kt`, `HomeScreen`, `MonthlyActivityHeatmap`, `ActivityHeatmap`, `PrimaryNavigationBar` and `VibeCard`; same baseline.
- `app/src/main/java/com/petermathie/vibecheck/ui/ProgressScreen.kt`, `ProgressCardShell`, `MiniChart` and `RpeBarChart`; same baseline.
- `app/src/main/java/com/petermathie/vibecheck/ui/ActionControls.kt`, `ReorderState`, `ReorderHandle` and `reorderItemFeedback`; same baseline.
- `app/src/main/java/com/petermathie/vibecheck/ui/CompletionCelebration.kt`, `completionAnimationPlan` and `CompletionConfetti`; same baseline.
- `app/src/main/java/com/petermathie/vibecheck/ui/anatomy/MuscleMap.kt`, colour interpolation and `celebratedMuscleIds`; same baseline.
- `app/src/main/java/com/petermathie/vibecheck/ui/MainViewModel.kt`, `WorkoutCompletionEvent` and `finishWorkout`; `app/src/main/java/com/petermathie/vibecheck/ui/OneShotEventState.kt`; same baseline.
- `app/src/main/java/com/petermathie/vibecheck/ui/VibeHaptics.kt`, centralized haptic event mapping and throttling; same baseline.
- `app/src/test/java/com/petermathie/vibecheck/ui/CompletionCelebrationTest.kt` and `app/src/androidTest/java/com/petermathie/vibecheck/EndUserControlsTest.kt`, reduced-motion and one-shot completion coverage; same baseline.

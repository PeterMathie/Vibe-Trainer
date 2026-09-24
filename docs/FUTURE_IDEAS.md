# Future personality and product ideas

> **Status:** Exploratory and deferred. These are not committed requirements or an implementation backlog. They are intended primarily to help Vibe Trainer develop more personality and emotional reward after the current phone beta has been used in real life.

[Product requirements](PRODUCT_REQUIREMENTS.md) remains authoritative for current behavior. In particular, trackers currently have no schedules or missed-day penalties. Any idea below that changes that model requires an explicit future product decision before implementation. See the [reviewer showcase](APP_REVIEW.md) for the current product.

## Direction and safeguards

Vibe Trainer should feel **calm, observant, precise and quietly satisfying**. It should reward evidence rather than hype.

| Prefer | Avoid |
|---|---|
| Factual acknowledgement of work recorded | Generic motivational copy |
| Rare, meaningful emphasis | Constant celebration or confetti |
| Data-backed summaries | Fake recovery, fatigue or coaching claims |
| Calm visual and tactile feedback | Mascots, guilt or missed-day punishment |
| User-controlled motion, sound and haptics | Feedback that becomes noise or cannot be disabled |

Every future treatment must have an accessible reduced-motion equivalent, respect sound/haptic opt-outs, and make deterministic claims from actual stored data.

## Likely first experiment: workout completion

After real-phone dogfooding, the first personality experiment should be a signature **Finish workout** moment:

1. Show the previous muscle-map state transitioning to the newly derived recency colours.
2. Use a restrained completion animation and light haptic.
3. Present a concise factual summary: work recorded, duration, exercises and sets.
4. Reserve stronger tiers for a genuine PR, newly established baseline or programme milestone.
5. Provide an immediate reduced-motion equivalent with the same information and hierarchy.

This is explicitly deferred until the existing beta has been used during real training. The experiment should validate whether the map transition feels rewarding and informative without delaying exit from the workout.

## Feedback and reflection ideas

### Save feedback

- Use a brief saved pill or checkmark with a light haptic for routine edits.
- Avoid interruptive success modals.
- Use stronger, persistent confirmation for backups, imports and other important edits where users need confidence about data safety.

### Flexible weekly habit goals

Explore an optional target of **N days or entries per week**, such as “work done 6 times this week,” without converting habits into rigid daily schedules.

- Count weekly-goal consistency rather than consecutive-day streak pressure.
- Give a factual end-of-week reflection, not praise or blame.
- Surface one deterministic, meaningful habit spotlight based on actual records.
- Never punish or shame a missed day.

This would change the current no-schedule tracker model and therefore needs an explicit product decision, including semantics for partial weeks, edits, time zones and multiple fields.

### Individual habit heatmaps

Prefer one compact heatmap per habit over a multicolour aggregate:

- Use the habit's chosen colour with intensity representing its own recorded value or completion.
- Keep cards collapsed for scanning and expand one for detail.
- Retain the existing Home heatmap as the overall activity view.
- Define deterministic intensity rules for Boolean, numeric, choice and targetless habits before implementation.

## Platform extensions

### Pixel and Android widgets

Potential configurable widgets:

| Widget | Useful content/action |
|---|---|
| Habit heatmap | One selected habit and recent intensity |
| Quick entry | Boolean completion or one value field |
| Training recency | Compact current muscle map |
| Weekly summary | Factual training and habit totals |

Widget design must consider lock-screen and launcher privacy, stale-data disclosure, battery cost, update cadence and behavior when the app database is unavailable.

### External app launch and deep links

A habit could optionally launch a relevant app or deep link, such as a meditation app, Strava or Kindle. Launching another app must **not** automatically mark the habit complete. Completion requires an explicit user entry unless a reliable, permissioned integration can establish the relevant activity with clear semantics.

Open integration questions include Android package availability, fallback behavior, user consent, imported-data provenance and whether read access is worth the maintenance/privacy cost.

## Validation questions

### Personality

- Does feedback feel calm and earned rather than gamified?
- Is the completion moment still useful after the fiftieth workout?
- Are milestone tiers rare enough to remain meaningful?
- Can every claim be traced to a stored record and explained?

### Habits

- Do weekly goals add flexibility without recreating schedule pressure?
- What does a user expect after editing an earlier day or changing a goal midweek?
- Can each habit heatmap be interpreted without a legend or competing colours?
- Is one weekly spotlight genuinely informative rather than arbitrary?

### Accessibility and platform

- Is the reduced-motion path equally clear and emotionally complete?
- Are haptic and sound opt-outs respected everywhere?
- Do widgets reveal sensitive training, body or habit data on shared/locked devices?
- Does an external-app launch remain understandable when no completion data returns?

## Suggested phased order

| Phase | Scope | Gate before continuing |
|---|---|---|
| 1. Dogfood | Use the current phone beta during real workouts and ordinary weeks | Identify repeated emotional/feedback gaps from actual use |
| 2. Completion experiment | Muscle-map transition, factual summary, restrained milestone tiers, reduced-motion equivalent | Repeated-use feedback shows it remains useful and unobtrusive |
| 3. Everyday feedback | Saved indicator, important-edit confirmation, optional weekly reflection | Semantics and accessibility validated without missed-day pressure |
| 4. Extensions | Widgets and carefully bounded app launches/integrations | Privacy, cadence, permission and reliability decisions recorded |

Do not start a later phase merely because it is technically available; each phase should respond to observed use of the one before it.

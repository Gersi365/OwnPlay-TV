# OwnPlay TV — Product & Source Authority

## Document Role

This is the primary durable authority for OwnPlay TV product behavior and implementation direction.

This document is intentionally stable across routine commits. Do not update it merely because a branch HEAD, CI run, version, temporary defect, or work phase changes.

Update it only when a durable product decision, architecture rule, supported feature, or intended interaction changes.

Current repository state, exact HEAD, workflow status, and active defects must be verified directly from GitHub when engineering work begins.

---

# 1. Product Identity

OwnPlay TV is an Android TV / Google TV / TV Box media player and playlist organizer for user-provided legitimate media sources and credentials.

OwnPlay TV does not sell subscriptions, provide channels, bundle provider content, or distribute provider media.

Core product areas include:

- Xtream-compatible sources;
- M3U / M3U8 playlists;
- Live channels;
- Movies / VOD;
- Series and episodes;
- EPG where available;
- favorites;
- hidden/restored content;
- manual ordering;
- custom groups;
- local display names and logos;
- playback progress;
- Continue Watching;
- backup/restore of supported personalization.

Application ID:

`app.ownplay.tv`

Target devices:

- Android TV;
- Google TV;
- compatible Android TV boxes.

---

# 2. Technology Baseline

The established implementation uses:

- Kotlin;
- Jetpack Compose;
- Android Media3 / ExoPlayer;
- Room;
- DataStore;
- WorkManager where shared infrastructure requires it;
- Coroutines / Flow;
- repository-driven data access.

Do not replace the established playback or persistence architecture without explicit approval.

Reliability, remote ergonomics, focus correctness, and playback continuity take priority over ornamental refactoring.

---

# 3. Product Principles

OwnPlay TV should be:

- simple;
- serious;
- professional;
- media-first;
- remote-first;
- fast on large playlists;
- predictable;
- readable from TV viewing distance;
- non-destructive toward provider data;
- reliable before visually clever.

Avoid:

- touch-first interaction patterns;
- dense phone-like forms;
- overloaded dashboards;
- unnecessary gradients;
- gimmicky animation;
- provider-like branding;
- hidden focus behavior;
- scale-heavy focus animation;
- destructive playlist behavior;
- visual polish that risks remote navigation or playback reliability.

---

# 4. Primary Navigation

Primary navigation is:

- **Live**
- **Movies**
- **Series**
- **Settings**

Primary navigation is a fixed vertical rail on the left edge of the TV UI. The rail shows icons only; destination text labels are not persistently rendered in the rail. Global navigation must not be placed across the top of the screen.

The rail must preserve fixed item geometry. Focus and selected destination state are expressed primarily through color and non-geometric emphasis.

Primary navigation must be remote-friendly, focus-stable, and visually predictable.

---

# 5. TV-Only Product Boundaries

OwnPlay TV does not expose product UI for:

- Downloads management;
- Offline media management;
- Offline filtering in Library;
- Picture-in-Picture.

Shared backend code may remain where removing it would create unrelated architectural churn, but these are not OwnPlay TV product surfaces.

---

# 6. Visual System

OwnPlay TV should feel:

- dark;
- minimal;
- modern;
- serious;
- professional;
- media-first;
- visually stable under focus and selection changes.

## Stable Geometry Rule

Focus and selection should be expressed primarily through color and non-geometric emphasis.

Do not introduce focused-only changes to:

- shape;
- scale;
- border thickness;
- padding;
- card/poster dimensions;
- row height;
- typography metrics;
- icon dimensions;
- surrounding layout.

Do not add `Active` / `Selected` labels to communicate focus or current state.

---

# 7. Live Browsing

Supported Live browse modes:

- List;
- Compact;
- Gallery.

Live browsing should preserve access to the established concepts where applicable:

- Search;
- provider categories;
- Favorites;
- supported local sort/order;
- Custom groups as local personalization;
- View mode;
- Channel selection.

Provider category taxonomy is authoritative for provider content browsing. OwnPlay TV must not synthesize replacement content categories. When a provider exposes categories, the UI displays those provider-supplied category names and membership. Local personalization such as hiding, ordering, Favorites, or Custom groups remains separate from the provider taxonomy.

Channel focus and current-channel state must preserve geometry.

Keep fixed:

- logo size;
- padding;
- row/card dimensions;
- typography geometry.

Use color to differentiate focused/current state.

---

# 8. Live Preview

Preview is presentation-only.

It must not render visible:

- Play/Pause controls;
- Next/Previous controls;
- fullscreen button;
- close button;
- generic Media3 controller.

The native `PlayerView` controller should remain disabled.

## Remote Activation Contract

First OK/Enter on a different channel:

**Open Preview while keeping channel browsing usable.**

Second OK/Enter on that same currently previewed channel:

**Open fullscreen Live.**

Back closes Preview before leaving the Live browsing hierarchy.

---

# 9. Preview + EPG

The selected-channel EPG belongs with Preview but remains a distinct information region from the video.

Requirements:

- load EPG for the selected channel;
- present selected-channel EPG alongside/below Preview according to the TV layout;
- keep Preview itself free of playback buttons;
- preserve a route to the fuller guide experience where supported;
- do not allow Preview to steal focus from the channel-browsing path unexpectedly.

---

# 10. Fullscreen Live

Fullscreen Live is video-first and intentionally not a generic media-controller screen.

Requirements:

- video owns the screen;
- native generic controller stays disabled;
- no generic playback control bar;
- EPG is the transient interaction layer;
- EPG may appear on entry or channel change;
- EPG may auto-hide;
- Back returns to Preview.

## Remote Contract

- `CH+` / `KEYCODE_CHANNEL_UP` → next Live channel;
- `CH-` / `KEYCODE_CHANNEL_DOWN` → previous Live channel;
- OK/Enter → reveal EPG;
- Down → enter EPG timeline;
- Left/Right → browse EPG programs when timeline is focused;
- Up → leave the EPG timeline;
- D-pad Up/Down must not directly zap channels.

---

# 11. Live Playback Continuity

For the same source and same channel, presentation changes should preserve the active playback session.

Preview → fullscreen and fullscreen → Preview should not stop/start playback solely because presentation changed.

Surface ownership must remain explicit:

- one player;
- one active video target;
- old destination relinquishes ownership;
- destination surface becomes the sole active target;
- no competing video surfaces;
- no duplicate audio.

Actual source/content replacement may stop/restart where required.

---

# 12. Back Hierarchy

Back must be owned by the deepest active presentation first.

Typical hierarchy:

- fullscreen Live → Preview;
- Preview → channel browsing;
- nested Live hierarchy → previous hierarchy level;
- nested Library screen → previous Library level;
- nested Settings screen → Settings root;
- app exit confirmation only at the actual root where defined.

Do not show exit confirmation while a deeper layer still owns Back.

---

# 13. Library & On-Demand Playback

Movies and Series should remain easy to browse with a remote and readable from TV viewing distance.

Where the active provider exposes Movie or Series categories, those provider-supplied categories are the browsing taxonomy. OwnPlay TV must not invent generic replacement categories for Movies or Series. Continue Watching is a local progress surface and is not a provider category.

On-demand playback should be video-first and consistent across Movies and Series episodes.

Requirements:

- custom transient controls;
- no unnecessary persistent header blocking video;
- seek behavior suitable for D-pad/remote use;
- clear Play/Pause/Retry behavior;
- safe Back behavior;
- persisted playback progress;
- Resume from incomplete saved progress;
- Play from beginning starts at zero without prematurely deleting saved progress;
- Continue Watching remains core.

---

# 14. Settings — TV-First Structure

Settings is a dedicated TV-first experience and must not be treated as a generic landscape settings screen.

The top-level Settings menu is:

1. **Playlists**
2. **Live Management**
3. **Backup & Restore**
4. **About**

Do not keep a top-level category that contains no meaningful user action.

The TV Settings root should optimize for direct remote navigation rather than category nesting.

## Root Layout

Recommended structure:

- left side: vertical list of the four top-level settings destinations;
- right side: short contextual description and useful status for the currently focused destination;
- OK/Enter: open the destination as a dedicated page;
- Back: return to Settings root and restore focus to the item that opened the page.

The right-side area is informational. Do not fill it with a dense touch-style settings form.

## Initial Focus

When Settings opens, focus should land predictably on **Playlists** unless a restored Settings focus destination is intentionally supported.

## Root Remote Behavior

- Up/Down → move through Settings destinations;
- OK/Enter → open focused destination;
- Back → leave Settings according to root navigation;
- Left/Right should not be required for ordinary top-level Settings navigation.

---

# 15. Settings — Playlists

Playlists is a direct top-level destination.

It may include:

- configured sources;
- add source;
- edit source;
- refresh source;
- enable/disable source;
- source status;
- open-source context action where supported.

The presentation must be remote-first.

Avoid dense phone-style inline forms. Prefer clear full-page or focused-step editing flows when text entry is required.

Text input must work with normal Android TV keyboard/input behavior.

---

# 16. Settings — Live Management

Live Management is a direct top-level destination.

It covers the established organization surfaces such as:

- Categories;
- Channels;
- hidden/restored state;
- manual ordering;
- custom groups;
- supported local organization actions.

Focus must remain deterministic during reorder/management operations.

Destructive operations require clear confirmation where appropriate.

---

# 17. Settings — Backup & Restore

Backup & Restore is a direct top-level destination.

It should provide a small number of explicit actions:

- Create backup;
- Restore backup.

The page should briefly explain what personalization is covered.

Restore should prefer merge/upsert behavior where appropriate.

Do not silently restore provider credentials or source secrets from personalization backup unless a later explicit decision changes that policy.

TV file/document selection flows must remain usable by remote and compatible with platform document providers where applicable.

---

# 18. Settings — About

About is a direct top-level destination.

It may show:

- OwnPlay product statement;
- build/version information;
- concise legal/product clarification as needed.

Keep it simple and readable.

---

# 19. Settings Focus Contract

Settings must be fully operable with D-pad + OK + Back.

Requirements:

- every actionable item must be focusable;
- focus order must be deterministic;
- no focus traps;
- no invisible focusable elements;
- no requirement for touch gestures;
- no scale animation that changes geometry;
- focused state uses color/background/tint emphasis;
- row height, padding, icons, and typography remain fixed;
- entering a subpage places focus on a predictable first meaningful control;
- returning restores focus to the originating Settings item where practical.

Scrolling must follow focus automatically when necessary.

---

# 20. Personalization & Refresh

Provider/source refresh must preserve supported local personalization.

Examples include:

- favorites;
- hidden/restored state;
- manual order;
- custom groups;
- local display names;
- local logos;
- playback progress where applicable.

Refresh must not silently erase user organization.

---

# 21. Performance & Reliability

Large playlists and content libraries must remain responsive on TV hardware.

Prefer:

- repository-layer access;
- cache-first behavior where appropriate;
- request coalescing;
- demand-driven refresh;
- stable keys/order;
- bounded work;
- deterministic focus;
- lifecycle-safe playback ownership;
- recoverable state transitions.

Reliability and remote ergonomics take precedence over decorative complexity.

---

# 22. Documentation Maintenance Rule

Do not update this file because:

- HEAD changed;
- CI passed;
- a commit was created;
- a version changed;
- a temporary bug was fixed;
- a work phase completed.

Update it only when the durable OwnPlay TV product or behavior contract changes.

For current repository state, inspect GitHub live.

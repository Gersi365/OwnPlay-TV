# OwnPlay TV — UI & Remote Interaction Contract

## Document Role

Durable interaction specification for OwnPlay TV.

Update this file only when intended TV interaction changes, not when implementation checkpoints change.

---

# 1. Visual Direction

OwnPlay TV should be:

- dark;
- minimal;
- modern;
- serious;
- professional;
- media-first;
- remote-first;
- readable from TV viewing distance;
- visually stable during focus movement.

Avoid focus treatments that make the interface jump or feel delayed.

---

# 2. Stable Geometry

For navigation, channel rows, media cards, category/season/episode rows, Settings rows, dialogs, and management controls:

**Focus and selection should change visual emphasis primarily through color while geometry stays fixed.**

Do not change only-on-focus:

- shape;
- scale;
- padding;
- border thickness;
- card dimensions;
- row height;
- icon dimensions;
- text metrics;
- surrounding layout.

Do not add `Active` or `Selected` text to communicate focus.

---

# 3. Remote Fundamentals

The product must be fully usable with:

- D-pad Up/Down/Left/Right;
- OK/Enter;
- Back;
- channel keys where explicitly supported.

General rules:

- focus must always be visible;
- focus order must be deterministic;
- screens must not depend on hover or touch gestures;
- scroll containers must follow focus;
- modal/dialog focus must stay inside the modal until dismissal;
- after returning from a nested screen, restore meaningful previous focus where practical.

---

# 4. Primary Navigation

Primary navigation is:

- Live;
- Library;
- Settings.

Primary navigation items must preserve:

- minimum width;
- shape;
- padding;
- icon/text layout.

Selected and focused states should be color-driven.

Do not use scale animation as the primary focus treatment.

---

# 5. Live Channel Browsing

Channel rows/cards must preserve geometry while current/focused state changes.

Keep fixed:

- logo size;
- padding;
- row/card dimensions;
- typography metrics.

Current EPG metadata may appear without changing row geometry.

Supported modes:

- List;
- Compact;
- Gallery.

---

# 6. Live Preview

Preview is presentation-only.

No visible:

- Play/Pause;
- Next/Previous;
- fullscreen action;
- close action;
- native generic media controller.

First OK on a different channel opens Preview while preserving browse usability.

Second OK on that same previewed channel opens fullscreen Live.

Back closes Preview first.

Preview must not unexpectedly steal focus from the channel-browsing path.

---

# 7. Preview + EPG

Selected-channel EPG is shown with Preview as a separate region.

The Preview video itself remains control-free.

EPG presentation must be readable at distance and must not create ambiguous focus ownership.

---

# 8. Fullscreen Live

Fullscreen Live is video-first.

Requirements:

- no generic playback control bar;
- EPG is the transient interaction layer;
- EPG may appear on entry/channel change and auto-hide;
- Back returns to Preview.

Remote behavior:

- CH+ → next channel;
- CH- → previous channel;
- OK → reveal EPG;
- Down → enter EPG timeline;
- Left/Right → browse programs in timeline;
- Up → leave timeline;
- D-pad Up/Down never directly zap channels.

---

# 9. Live Continuity & Surface Ownership

Same-channel Preview ↔ fullscreen transitions should preserve playback.

Do not restart the stream merely because presentation changed.

Maintain:

- one player;
- one active video target;
- no duplicate audio;
- no stale competing surfaces.

---

# 10. Back Safety

Back follows interaction depth.

Examples:

- fullscreen Live → Preview;
- Preview → browsing;
- nested management/page → parent;
- root navigation only after deeper layers are closed.

Never show an exit confirmation while a deeper active layer still owns Back.

---

# 11. Library & On-Demand

Movies and Series must be remote-first and readable from distance.

Playback controls should be transient, consistent, and focus-safe.

Back should return to the originating details/context without trapping focus.

Resume/progress actions must remain understandable and predictable.

---

# 12. Settings Root — New TV Contract

Settings uses a dedicated TV-first root menu.

Top-level destinations:

1. Playlists
2. Live Management
3. Backup & Restore
4. About

Do not use an extra category layer when it adds no meaningful decision.

## Root Layout

Use a simple two-region composition:

- **left:** vertical destination list;
- **right:** contextual description/status for the focused destination.

The right region is informational, not a dense settings form.

## Initial Focus

Default focus is Playlists unless an intentionally restored Settings focus state exists.

## Remote Behavior

- Up/Down → move destination focus;
- OK/Enter → open focused destination;
- Back → leave Settings at root;
- Left/Right are not required for ordinary root navigation.

---

# 13. Settings Destination Rows

Each top-level Settings row should include:

- fixed-size icon;
- title;
- optional short detail/status;
- stable row height.

Focused state may change:

- background color;
- icon tint;
- title/detail color.

Focused state must not change:

- row shape;
- row dimensions;
- scale;
- padding;
- icon size;
- typography metrics.

---

# 14. Settings Subpages

Each top-level destination opens as a dedicated page.

On entry:

- focus lands on the first meaningful actionable control;
- title/context remains readable but does not steal focus;
- D-pad navigation follows a predictable vertical or grid path.

On Back:

- return to Settings root;
- restore focus to the destination that opened the subpage.

Avoid nested category chains where a direct page is clearer.

---

# 15. Playlists Interaction

Playlist management should prefer a clear list-detail or full-page action model.

Remote flow should make these easy:

- select source;
- add;
- edit;
- refresh;
- enable/disable;
- inspect status.

Text entry should delegate to compatible Android TV keyboard/input behavior.

Do not present a dense collection of small inline edit fields that require pointer-style precision.

---

# 16. Live Management Interaction

Live Management should support Categories, Channels, ordering, hidden/restored state, and Custom Groups without focus loss.

Reorder operations must make current move target obvious.

After a mutation:

- preserve or restore a meaningful focus target;
- do not jump unpredictably to the top of a large list unless unavoidable.

---

# 17. Backup & Restore Interaction

Use a small number of large, explicit actions.

Recommended actions:

- Create backup;
- Restore backup.

Destructive/overwriting implications must be explained before confirmation where applicable.

File/document provider navigation must remain remote-usable.

---

# 18. About Interaction

About is informational and simple.

It should not contain a complicated focus graph.

If links/actions are added later, they must have clear remote focus and Back behavior.

---

# 19. Focus Restoration

Focus restoration is a product requirement, not cosmetic polish.

Important transitions should restore useful prior focus:

- Settings subpage → originating root destination;
- management detail → originating list row where practical;
- Preview close → channel browsing;
- fullscreen → Preview/browse destination according to the established Live contract.

Avoid multiple competing automatic focus requests.

---

# 20. Density & Readability

TV UI should be compact enough to show useful context but spacious enough for distance viewing.

Avoid:

- tiny controls;
- excessive text blocks;
- overly wide paragraphs;
- dense form layouts;
- visually heavy selected pills;
- excessive empty chrome.

Use typography hierarchy and spacing rather than nested cards for every setting.

---

# 21. Interaction Maintenance Rule

Do not update this contract because a composable moved, a helper was renamed, or a regression test was added.

Update it only when intended OwnPlay TV interaction changes.

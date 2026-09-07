# OwnPlay TV — Engineering Workflow

## Document Role

Stable engineering operating procedure for OwnPlay TV.

This file intentionally contains no static current HEAD, workflow run, version, or temporary work queue.

Repository state must be verified live before work begins.

---

# 1. Authority Order

When evidence conflicts, use:

1. latest explicit user decision;
2. `OwnPlay_TV_SOURCE.md`;
3. `OwnPlay_TV_UI_INTERACTION_CONTRACT.md`;
4. verified current GitHub repository state;
5. focused current QA/audit evidence;
6. historical notes.

Do not silently reconcile conflicts.

---

# 2. Repository Identity

Primary repository:

`Gersi365/OwnPlay-TV`

TV application ID:

`app.ownplay.tv`

Never treat a commit SHA copied into documentation or an old conversation as automatically current.

Before any source mutation, verify the authoritative source ref and exact HEAD directly from GitHub.

---

# 3. Audit-First Workflow

For meaningful engineering work:

1. inspect actual repository state;
2. verify current authoritative source ref;
3. verify exact HEAD;
4. inspect the active TV execution path;
5. read the relevant durable TV contract;
6. establish evidence for the requested issue;
7. distinguish verified cause from hypothesis;
8. limit the change to the requested scope;
9. make the smallest deterministic and recoverable fix;
10. add focused regression coverage where practical;
11. commit forward-only;
12. run relevant no-APK validation;
13. verify final validation belongs to exact final HEAD;
14. report what changed and what remains unverified physically.

If HEAD changes after validation, the previous run becomes historical evidence only.

---

# 4. Scope Discipline

Prefer changes in:

- active TV execution paths;
- TV-specific presentation/focus code;
- shared code only when TV behavior genuinely depends on it;
- focused regression tests.

Do not refactor unrelated modules merely for consistency.

This repository is TV-only. Do not preserve mobile flavors, mobile source sets, smartphone profiles, touch-first fallback shells, PiP/mobile window policy, or portrait/landscape presentation branches as inactive legacy. Remove such form-factor legacy when it is found, while respecting separate safety boundaries such as destructive database migrations.

Once an approved TV presentation replaces an older presentation or reference path, remove the superseded presentation/reference scaffolding rather than preserving it only because it is inactive. Active behavior must be migrated and validated before its previous execution path is deleted. Preserve domain, provider, playback, persistence, and migration logic unless the approved TV behavior genuinely requires a change.

---

# 5. TV-First Engineering Principle

Do not solve remote-navigation problems by adding more device-condition branches inside a touch-oriented layout when a dedicated TV presentation is clearer and safer.

Shared business/repository logic may remain shared.

TV presentation, focus routing, D-pad behavior, and screen hierarchy should be explicitly TV-first where form-factor differences are material.

---

# 6. Explicit Approval Required

Do not perform these without explicit user authorization:

- merge;
- ready-for-review transition;
- production deployment;
- public/store publication;
- release publication;
- QA APK generation;
- signing changes;
- force-push;
- reset;
- rebase;
- history rewrite;
- destructive repository rewrite;
- destructive database migration;
- user-data deletion;
- authentication/account cutover;
- architecture replacement;
- breaking backup-format changes;
- large unrelated dependency/refactor work.

When authorization is absent, remain inside source-only no-APK work.

---

# 7. APK Rule

Do not generate an APK merely to validate source.

A TV QA/update APK may be created only after explicit user authorization.

When authorized:

- preserve package `app.ownplay.tv`;
- preserve established signing identity unless a signing migration is explicitly approved;
- use valid monotonic versioning;
- build from the exact intended source candidate;
- report artifact identity and checksums where available.

---

# 8. Validation

After source changes, use focused tests plus the broader no-APK validation lane as appropriate.

Validation may include:

- unit tests;
- contract/focus tests;
- Kotlin compilation;
- lint/static checks;
- Android SDK verification;
- Room schema verification when persistence is touched;
- explicit zero-APK verification.

TV variant compilation is mandatory for changes affecting shared UI/data contracts used by TV.

Final evidence must correspond to final HEAD.

---

# 9. Physical QA Boundary

Source inspection and CI do not prove physical remote/device behavior.

Physical QA remains required where relevant for:

- D-pad focus traversal;
- focus restoration;
- remote key delivery;
- scroll-follow-focus behavior;
- Live Preview/fullsreen continuity;
- actual video surface ownership;
- real EPG readability;
- Back hierarchy;
- text entry with TV keyboards;
- real provider/network behavior;
- perceived latency;
- 10-foot readability.

Never report physical PASS without physical evidence.

---

# 10. Settings Redesign Engineering Boundary

The TV Settings redesign is a presentation/navigation restructuring, not a repository/business-logic rewrite.

Preferred implementation direction:

- create/use a dedicated TV Settings presentation path;
- reuse existing source management, Live management, backup/restore, and About logic where appropriate;
- remove unnecessary category indirection from the TV flow;
- preserve existing data semantics;
- preserve Back safety;
- add explicit focus policy and focus restoration;
- do not introduce pointer/touch dependencies.

Do not create a new architecture layer unless existing boundaries cannot safely support the TV presentation.

---

# 11. Settings QA Requirements

Physical TV QA for Settings should verify:

- Settings opens with deterministic focus;
- Up/Down traverses all four root destinations in order;
- OK opens the focused destination;
- Back returns to root;
- root focus returns to the originating destination;
- no focus trap in scrollable pages;
- focus remains visible after list mutation/refresh where practical;
- Playlists actions are remote-usable;
- Live Management remains navigable with large lists;
- backup/restore confirmation and document selection are remote-usable;
- About has a simple focus graph;
- no hidden Downloads/Offline route appears;
- no geometry-changing focus animation.

---

# 12. Documentation Maintenance Policy

Do not turn the durable TV markdown into a current-state diary.

Do not update it because:

- HEAD changed;
- a commit was added;
- CI passed;
- a work phase changed;
- a version changed;
- a temporary defect was fixed without changing the durable contract.

Current repository checkpoints belong in GitHub.

Update documentation only when product scope, intended behavior, architecture policy, QA policy, engineering/safety policy, or a durable routing rule changes.

---

# 13. Reporting

After meaningful work, report:

- what was audited;
- what changed;
- files/areas changed;
- commit and final HEAD if source changed;
- validation performed;
- explicit APK status;
- remaining physical-QA boundary;
- one best next step.

Do not promise asynchronous/background work.

At the end of a meaningful OwnPlay TV stage, ask exactly:

**“A të vazhdoj me këtë?”**

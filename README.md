# Witchery (Forge 1.20.1 Port)

This repository is a breadth-first port of the original Witchery mod from Forge 1.7.10 to Forge 1.20.1.

## What This Project Is For

- Rebuild Witchery on modern Forge without losing legacy content coverage.
- Preserve legacy registry names/content where practical to reduce migration risk.
- Port wide first (all systems present), then deepen behavior/parity.

Legacy source used for porting lives in:
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10`

## Current Porting Status

Status is tracked in phase form:
- `Phase 0` discovery/inventory: complete.
- `Phase 1` registration scaffolding: complete.
- `Phase 2` assets/data scaffolding: in progress.
- `Phase 3+` systems/behavior parity: pending.

Inventory baseline from manifest:
- Blocks: `121`
- Items: `102`
- Fluids: `6`
- Entities: `55`
- Effects/Potions: `44`

## Source of Truth Docs

Use these files in this order each session:

1. `/Users/cyberpwn/development/workspace/AuramMods/Witchery/PORTING_MEMORY.md`
2. `/Users/cyberpwn/development/workspace/AuramMods/Witchery/PORTING.md`
3. `/Users/cyberpwn/development/workspace/AuramMods/Witchery/PORTING_MANIFEST.md`

Roles:
- `PORTING_MEMORY.md`: session-critical reminders and constraints.
- `PORTING.md`: phase checklist and near-term plan.
- `PORTING_MANIFEST.md`: legacy surface map (blocks/items/fluids/registries/anchors).

## How We Work

- Breadth-first, then depth-first.
- Keep compile health at each step.
- Validate with `compileJava` during scaffolding work.
- Keep the three docs updated whenever scope or status changes.

Validation rule:
- Use `./gradlew compileJava` as the routine check.
- Do not rely on `runClient` for routine scaffolding validation.

## How To Check Progress

### 1) Check phase checklist
- Open `/Users/cyberpwn/development/workspace/AuramMods/Witchery/PORTING.md`
- Look at `Phase Checklist` and `Immediate Next Actions`.

### 2) Check current build health
```bash
./gradlew compileJava
```

### 3) Check resource surface coverage (quick sanity)
```bash
find src/main/resources/assets/witchery/blockstates -type f | wc -l
find src/main/resources/assets/witchery/models/block -type f | wc -l
find src/main/resources/assets/witchery/models/item -type f | wc -l
```

### 4) Check working tree changes
```bash
git status --short
```

## Project Structure (Porting-Relevant)

- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10`: legacy source reference.
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/registry`: modern registry scaffolding.
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/resources/assets/witchery`: modern assets (blockstates/models/textures/lang).

## Goal

Get to a complete 1.20.1 Witchery port with:
- Full registry surface present
- Stable compile/build
- Progressive visual/behavior parity against 1.7.10

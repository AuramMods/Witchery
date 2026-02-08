# Witchery 1.7.10 -> 1.20.1 Porting Plan

## Working Style
- Breadth first first, depth second.
- Prefer creating full vertical skeletons (all systems present, minimal behavior) before polish.
- Check `/Users/cyberpwn/development/workspace/AuramMods/Witchery/PORTING_MEMORY.md` before starting work each session.
- Check `/Users/cyberpwn/development/workspace/AuramMods/Witchery/PORTING_MANIFEST.md` before hunting for source details.

## Current Snapshot (2026-02-07)
- Legacy source inventory completed for blocks, items, fluids, entities, key registries, and bootstrap flow.
- Manifest + durable memory created.
- No 1.20 content registration has been started yet (this is intentional for phase ordering).

## Phase Checklist

### Phase 0 - Discovery + Inventory (complete)
- [x] Map legacy startup lifecycle (`preInit/init/postInit/serverLoad`).
- [x] Enumerate legacy block registrations.
- [x] Enumerate legacy item registrations.
- [x] Enumerate fluid registrations and fluid-container bindings.
- [x] Enumerate entity registrations.
- [x] Enumerate high-impact registries (rites, brews, potions, predictions, creature powers, packets, dimensions, integrations).
- [x] Capture findings in manifest + memory docs.

### Phase 1 - 1.20 Registration Skeleton (breadth)
- [ ] Create `DeferredRegister` structure for blocks.
- [ ] Create `DeferredRegister` structure for items.
- [ ] Create `DeferredRegister` structure for fluids + fluid types.
- [ ] Create `DeferredRegister` structure for block entities.
- [ ] Create `DeferredRegister` structure for entity types.
- [ ] Create `DeferredRegister` structure for menu types.
- [ ] Create `DeferredRegister` structure for mob effects (potions/effects).
- [ ] Create creative mode tab wiring.
- [ ] Register placeholder instances for every legacy registry entry (names first, behavior later).

### Phase 2 - Data + Assets Skeleton (breadth)
- [ ] Add placeholder blockstates/models/lang entries for all registered blocks/items.
- [ ] Add minimal data-gen providers (loot, recipes, tags).
- [ ] Add placeholder spawn egg / bucket items where needed.
- [ ] Ensure game boots with full registry surface present.

### Phase 3 - Systems Migration Skeleton (breadth)
- [ ] Network channel + packet stubs (all legacy packet message intents represented).
- [ ] GUI/menu stubs for all legacy GUI IDs.
- [ ] Dimension/key migration for Dream/Torment/Mirror.
- [ ] Event bus hooks migrated with no-op or minimal behavior.
- [ ] Capability/data attachment plan for replacing `ExtendedPlayer`/custom NBT patterns.

### Phase 4 - Feature Behavior Pass (depth)
- [ ] Blocks/tile entity behavior parity pass.
- [ ] Item behavior parity pass (including metadata-subitem conversions).
- [ ] Brewing/kettle/distillery/spinning/brazier parity.
- [ ] Ritual and circle magic parity.
- [ ] Infusion, prediction, creature power parity.
- [ ] Entity AI/combat/spawn tuning parity.

### Phase 5 - Integration + Stabilization
- [ ] Optional mod compat strategy (modern equivalents where relevant).
- [ ] Save migration / missing mapping strategy.
- [ ] Balance and progression validation.
- [ ] Multiplayer sync and desync hardening.
- [ ] Test matrix + release checklist.

## Immediate Next Actions
- [ ] Build 1.20 registry scaffolding package layout.
- [ ] Start with blocks/items/fluids/entity types as empty placeholders matching legacy names from manifest.
- [ ] Keep this file updated as phases move from breadth to depth.

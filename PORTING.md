# Witchery 1.7.10 -> 1.20.1 Porting Plan

## Working Style
- Breadth first first, depth second.
- Prefer creating full vertical skeletons (all systems present, minimal behavior) before polish.
- Check `/Users/cyberpwn/development/workspace/AuramMods/Witchery/PORTING_MEMORY.md` before starting work each session.
- Check `/Users/cyberpwn/development/workspace/AuramMods/Witchery/PORTING_MANIFEST.md` before hunting for source details.

## Current Snapshot (2026-02-08)
- Legacy source inventory completed for blocks, items, fluids, entities, key registries, and bootstrap flow.
- Manifest + durable memory created.
- Phase 1 registry scaffolding is in place and compiling.
- Phase 2 asset skeleton is partially in place:
  - generated placeholder `blockstates`, `models/block`, and `models/item` for registered content.
  - copied legacy textures into modern resource path.
  - normalized resource-location usage to lowercase-safe paths to avoid model parse failures.
  - migrated model texture refs and assets to singular atlas paths (`block`/`item`) for 1.20.1 compatibility.
  - added temporary barrier-based placeholder textures for missing legacy sprite names to eliminate purple-black fallback during scaffolding.
  - completed lowercase filename normalization in `textures/block` + `textures/item` and validated clean model bake log.
  - started targeted 1.7 visual parity fixes for high-priority blocks (`wolftrap`, `icedoor`, `witchlog`, `mindrake`, `wallgen`).
  - updated `icedoor` to use 1.20.1 door model parent chain (`door_*`) and vanilla door-state rotation mapping.
  - replaced simplified `wolftrap` placeholder with geometry based on legacy `ModelBeartrap` dimensions.
  - converted legacy crop family to staged crop parity assets + crop block classes:
    - crop blockstates now use `age=*` variants instead of cube placeholders for `belladonna`, `mandrake`, `artichoke`, `snowbell`, `wormwood`, `wolfsbane`, `garlicplant`.
    - added stage model sets for those crops (`*_stageN`) and switched crop item models to flat generated icons using mature-stage textures.
    - mapped crop max-age behavior in block scaffolding (`4`, `5`, or `7` depending on legacy crop).
  - expanded `witchlog` parity to multi-wood variants via `wood_type` blockstate (`rowan`, `alder`, `hawthorn`) with dedicated alder/hawthorn model sets.
  - switched `wallgen` block model texture from hardcoded iron placeholder to `witchery:block/wallgen` for visual parity testing.
  - replaced major structural cube placeholders with 1.20.1 state-driven models + block classes:
    - stairs family (`stairswoodrowan`, `stairswoodalder`, `stairswoodhawthorn`, `icestairs`, `snowstairs`)
    - slab family (`witchwoodslab`, `iceslab`, `snowslab`)
    - fence/fence gate (`icefence`, `icefencegate`)
    - pressure plates (`icepressureplate`, `snowpressureplate`, `cwoodpressureplate`, `cstonepressureplate`, `csnowpressureplate`)
    - buttons (`cbuttonwood`, `cbuttonstone`)

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
- [x] Create `DeferredRegister` structure for blocks.
- [x] Create `DeferredRegister` structure for items.
- [x] Create `DeferredRegister` structure for fluids + fluid types.
- [x] Create `DeferredRegister` structure for block entities.
- [x] Create `DeferredRegister` structure for entity types.
- [x] Create `DeferredRegister` structure for menu types.
- [x] Create `DeferredRegister` structure for mob effects (potions/effects).
- [x] Create creative mode tab wiring.
- [x] Register placeholder instances for every legacy registry entry (names first, behavior later).

### Phase 2 - Data + Assets Skeleton (breadth)
- [x] Add placeholder blockstates/models/lang entries for all registered blocks/items.
- [ ] Add minimal data-gen providers (loot, recipes, tags).
- [ ] Add placeholder spawn egg / bucket items where needed.
- [~] Ensure game boots with full registry surface present.
  - All static resource checks pass (no missing model parent/texture references).
  - Fresh client run verified: no Witchery model/texture bake errors after latest `icedoor`/`wolftrap` asset fixes.

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
- [ ] Add placeholder bucket/block hookups for fluids (if needed for world interaction/testing).
- [ ] Add placeholder block items for non-itemized legacy blocks where useful for QA visibility.
- [x] Re-run client and validate `run/logs/latest.log` after the lowercase resource-location fix.
- [x] Re-run client and validate `run/logs/latest.log` after `icedoor` parent-model and `wolftrap` geometry parity fixes.
- [~] Continue replacing placeholder block models with old 1.7 equivalents (breadth pass before behavior depth).
  - completed crop-family stage model parity pass.
  - completed structural families (stairs/slabs/fence/fence-gate/pressure-plate/button) with 1.20.1 state models.
  - next breadth target: non-cube decorative/functional blocks still on generic cube placeholders.
- [ ] Keep this file updated as phases move from breadth to depth.

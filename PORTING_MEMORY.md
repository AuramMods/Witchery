# READ THIS FIRST EVERY SESSION
Always read this file first, then check `/Users/cyberpwn/development/workspace/AuramMods/Witchery/PORTING.md`, then `/Users/cyberpwn/development/workspace/AuramMods/Witchery/PORTING_MANIFEST.md` before making porting changes.

## Session Memory (2026-02-07)
- Port target is Forge 1.20.1.
- Legacy source is fully available at `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10`.
- Strategy is breadth-first: register everything first, behavior parity later.

## Critical Architecture Facts
- Legacy bootstrap class: `com.emoniph.witchery.Witchery`.
- Lifecycle pattern:
  - `preInit`: config, worldgen setup, villages, packet preInit, construct Potions/Fluids/Blocks/Items/Entities, recipe+potion preInit.
  - `init`: packet init, entity init, GUI handler, dimension registration, seeds, proxy hooks, mod hooks init, potions init, recipes init, brew registry init.
  - `postInit`: recipes postInit, mod hooks postInit, biome integration, event registration, dispenser behaviors, chunk loading callback.
  - `serverLoad`: server command + runtime registries (`PowerSources`, `AreaMarkerRegistry`, worldgen initiate).

## High-Risk Porting Points
- 1.7 code uses side-effect registration in overridden name setters:
  - `BlockBase.func_149663_c` and `BlockBaseContainer.func_149663_c` register blocks.
  - `ItemBase.func_77655_b` registers items.
- Many game objects are metadata variants under `ItemGeneral` (not separate item registries).
- Block entities are often auto-registered through `BlockBaseContainer` using block names.
- Several systems depend on custom singleton registries (rites, predictions, infusions, creature powers, symbol effects).
- Legacy dimension registration uses `DimensionManager.registerProviderType/registerDimension` and must be redesigned for modern dimension keys + datapack dimensions.

## Inventory Totals to Remember
- Blocks: 121 legacy block registry names.
- Items: 102 top-level item registry names.
- Fluids: 6.
- Entities: 55 mod entities (IDs 92-146).
- Potion/effect registrations: 44.
- `ItemGeneral` metadata subitems: 163 variants (includes dream weaves).
- Rites: 96.
- Predictions: 17.
- Creature powers: 25.
- Kettle recipes: 39.
- Distillery recipes: 9.
- Spinning recipes: 4.
- Brew registry effects: 158.
- Network packets: 19.

## Legacy Registry Anchors (where to look)
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/WitcheryBlocks.java`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/WitcheryItems.java`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/WitcheryFluids.java`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/WitcheryEntities.java`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/WitcheryRecipes.java`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/brewing/WitcheryBrewRegistry.java`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/brewing/potions/WitcheryPotions.java`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/infusion/infusions/symbols/EffectRegistry.java`

## Staging Notes For Next Work Block
- First coding phase should build modern `DeferredRegister` scaffolding for all content types.
- Keep legacy registry names aligned where possible for migration continuity.
- Convert metadata-heavy items (`ItemGeneral`, `ItemPoppet`) into either:
  - separate items, or
  - data-component/nbt-driven subtypes with clear modern rendering + localization strategy.
- Do not deep-port behavior until full registry surface exists and game starts cleanly.

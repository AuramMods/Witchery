# Witchery 1.7.10 Legacy Manifest (Breadth-First Port Baseline)

## Scope
This document inventories legacy registry surfaces for the port.

Primary source root:
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery`

## Source Footprint
- Total files under `old-1.7.10`: `1597`
- Total Java files under `old-1.7.10`: `803`

## Package File Counts (top-level)
| Package | Java Files |
|---|---:|
| `(root)` | 7 |
| `blocks` | 85 |
| `brewing` | 132 |
| `client` | 177 |
| `common` | 10 |
| `crafting` | 12 |
| `dimension` | 6 |
| `entity` | 91 |
| `familiar` | 2 |
| `infusion` | 35 |
| `integration` | 25 |
| `item` | 61 |
| `network` | 20 |
| `predictions` | 12 |
| `ritual` | 55 |
| `util` | 49 |
| `worldgen` | 24 |

## Bootstrap + Lifecycle Map
Main bootstrap class:
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/Witchery.java`

Lifecycle stages:
- `preInit`
  - config setup
  - world generator registration
  - village structure handlers
  - packet pipeline preInit
  - construct `WitcheryPotions`, `WitcheryFluids`, `WitcheryBlocks`, `WitcheryItems`, `WitcheryEntities`
  - recipe + potion preInit
- `init`
  - packet init
  - entity init
  - GUI handler registration
  - Dream/Torment/Mirror dimensions registration
  - grass seed entries
  - proxy handlers/renderers
  - mod hooks registration + init
  - potions init, recipes init, brew registry init
- `postInit`
  - recipes postInit
  - mod hooks postInit
  - external biome hook
  - proxy postInit + event registration
  - dispenser behavior bindings
  - chunkloading callback setup
- `serverLoad`
  - server command registration
  - `PowerSources.initiate()`
  - `BlockAreaMarker.AreaMarkerRegistry.serverStart()`
  - worldgen runtime init

## Core Legacy Registry Owners
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/WitcheryBlocks.java`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/WitcheryItems.java`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/WitcheryFluids.java`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/WitcheryEntities.java`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/WitcheryRecipes.java`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/brewing/WitcheryBrewRegistry.java`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/brewing/potions/WitcheryPotions.java`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/infusion/infusions/symbols/EffectRegistry.java`

## Registry Counts (Legacy)
| Registry Surface | Count |
|---|---:|
| Blocks (`func_149663_c`) | 121 |
| Items (`func_77655_b`) | 102 |
| Fluids | 6 |
| Entity registrations | 55 |
| Potions/effects | 44 |
| `ItemGeneral` metadata variants | 163 |
| `RiteRegistry.addRecipe` | 96 |
| `PredictionManager.addPrediction` | 17 |
| `CreaturePower.Registry.add` | 25 |
| `KettleRecipes.addRecipe` | 39 |
| `DistilleryRecipes.addRecipe` | 9 |
| `SpinningRecipes.addRecipe` | 4 |
| `WitcheryBrewRegistry.register` | 158 |
| Symbol effects (`EffectRegistry`) | 31 |
| Packet messages | 19 |

The runtime registry from the shipped 0.24.1 server contains 121 Witchery block IDs and 217 item IDs, including automatic block items that the source count of 102 item declarations omits. Forge 1.20.1 retains all those IDs and adds the internal Apothecary Shelf block. The Witchery tab displays 436 stacks. All 163 Ingredient subtype mappings were checked. Raw and cooked mutton use the corresponding vanilla items, and the two bound Waystones stay hidden. The Rowan Door, Alder Door, Ice Door, Chalice and Candelabra each show their placeable item once in place of the old Ingredient subtype. The generic Ingredient ID and 23 extra spawn egg IDs remain available for saves. All 12 original Poppet choices and 32 original spawn eggs are visible.

## Registration Mechanics (Important)
- `BlockBase.func_149663_c` auto-registers blocks via `BlockUtil.registerBlock`.
- `BlockBaseContainer.func_149663_c` auto-registers blocks + tile entities.
- `ItemBase.func_77655_b` auto-registers items via `ItemUtil.registerItem`.
- Namespaced IDs are trimmed to path for legacy `GameRegistry` (`witchery:name` -> `name`).
- `witchwoodslab`, `iceslab`, `snowslab`, and their three legacy double IDs appear in both lists. The modern item IDs match their blocks. The importer keeps the top or bottom half of the original single slabs and the Rowan, Alder, or Hawthorn species of both witchwood IDs. The packaged import and dedicated restart kept those variants, and a migrated Hawthorn double slab dropped two Hawthorn slabs. See [Witchwood Slabs and Door Save Import](PORTING.md#witchwood-slabs-and-door-save-import-2026-11-21).
- Legacy tree metadata `0/1/2` maps to `rowan/alder/hawthorn` on `witchsapling`, `witchlog`, `witchleaves`, and `witchwood`. Non-rowan item stacks preserve the variant in `BlockStateTag.wood_type`. Untagged stacks retain the rowan default. Sapling growth stage and leaves persistence use their modern vanilla block properties.
- Once a legacy wood stack has its namespaced ID, the shared Witchery inventory loader reads its legacy top-level `Damage` and writes `BlockStateTag.wood_type`. Existing explicit wood state and unrelated tags take precedence. Saved cauldron mixtures use the same loader for Witchery saplings.
- The opt-in world importer reads each save's top-level `FML.ItemData` before modernization. The first byte of `K` distinguishes blocks (`0x01`) from items (`0x02`), and `V` carries that world's ID. Chunk conversion reads the low byte in `Blocks`, the high nibble in `Add`, and the metadata nibble in `Data`. It restores the supported blocks in vanilla's flattened palette. Chunk inventories and both kinds of player inventory resolve through the item table and the shared loader. Witchery block entities use an explicit vanilla item fix for mixed inventories. See the Legacy Save Fixtures in `PORTING.md` for verified coverage.
- Original item names and lore are literal strings. The standalone item upgrade used by mixed Witchery workstations and specialized inventories converts them to modern text components. Vanilla items in those workstations need their lore converted after the first vanilla migration pass. In previously imported worlds with the legacy registry snapshot, the chunk loader repairs raw display text that remains in Witchery block entities and preserves already formatted text.
- Applied and stored vanilla enchantments in untyped Witchery inventories use Minecraft's numeric ID table during import. The saved registry also lets earlier port worlds recover those entries while keeping existing named levels.
- Dream parks its two inventories in `WITCSpiritWorld`, outside the player inventory schema. On a normal login to an earlier port save, the captured registry resolves numeric item IDs and old top-level `Damage` fields in both lists. Each item keeps its saved slot. Already-modern stacks in those lists retain their item data. The same conversion recognizes old metadata in previously imported Prior Incarnation drops.
- Original Pit Dirt metadata `2` uses the Podzol variant under the same block and item IDs. The `podzol` block property travels with Silk Touch, pick block, and imported stacks. Loaded Pit Dirt and Pit Grass recompute their soil or snow cover from the block above. Ordinary Pit Dirt mining follows the original plain-drop rule.
- Witchery's five stair blocks recompute their corner shapes after legacy chunk import. Imported Witchery leaves keep their species and persistent bit. Their distance starts at one while the normal scheduled leaf updates rebuild the distance from the surrounding logs.
- Forge 1.7.10 stores Witchery's chunk entity IDs with a dot and the original registration's capitalization. The importer converts those registered IDs to the modern namespaced form. It uses vanilla's entity data fixes for UUIDs, names, animals, equipment and passenger trees, and fixes mixed Hobgoblin trades using the world item table.
- Original block spawners and spawner minecarts can store these dotted IDs in their active mob and weighted spawn rules. The importer preserves those rules and the embedded mob state. It converts the old cart's spawner data separately from its vehicle data to avoid a vanilla upgrade crash. An ordinary 1.20.1 load repairs dotted IDs that survive in either spawner type. Wicker Man generation accepts the same IDs in `strawmanSpawnerRules`.
- Original Coven Witch quest pets store their trophy stack in `ForgeData.WITCExtraDrops`. When the pet dies, the drop handler upgrades the vanilla stack and its literal display name before reading it. Previously upgraded saves with untouched trophy records use the same path and keep the two original witch UUID fields for quest turn-in.
- The importer records the three optional `DimensionID` settings from the original `witchery.cfg`. Numeric folders move into namespaced storage before the chunk pass. Dream uses its own legacy Overworld-style structure data. Torment and Mirror use fixed modern biome palettes. Player tags, Waystones, Door Keys, key rings, and Mirror exits use the saved world's numeric mapping.
- The three witchwood stair IDs use their corresponding plank textures and original four-stair recipes. Recipes read the plank stack's wood state and require all six inputs to match. Hawthorn stairs retain the original lower fire spread and ignition values.
- The Water and Lava Diviners retain `witchery:divinerwater` and `witchery:divinerlava`. Their legacy top-level item damage converts to modern durability. The wear values and unrelated stack data were verified with numeric IDs from an original Forge save and again after a normal dedicated restart.
- The original `witchery:stewraw` and `witchery:stew` retain their IDs, count, and item tags. The original raw mutton ingredient (158) already maps to `minecraft:mutton`, which fits the raw stew recipe.
- The Poppet Shelf Compass retains `witchery:shelfcompass`. An original stack's old metadata recorded the displayed icon. Its count and custom tags survive import, and the six modern models select their icon from the current world.
- The Creative Bat/Wolf Token retains `witchery:wolftoken`. A stack imported from an original numeric ID was used through both rank cycles, retained its custom tag, and survived a packaged dedicated restart with both ranks saved.
- The Staff of Duplication retains `witchery:dupstaff`, the original marker coordinates, rotation, and nested schematic. A real twelve-cell capture survived numeric ID import into a chest and placed after an ordinary dedicated restart. Modern captures hold a palette of block states in the same coordinate order as the original list.

## Spawn Egg Model Coverage (2026-11-23)
- Runtime spawn egg item registration source:
  - `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/registry/WitcheryItems.java` (`registerLegacySpawnEgg` from `WitcheryEntities.all()`).
- Asset model source:
  - `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/resources/assets/witchery/models/item/*_spawn_egg.json`.
- The packaged Forge JAR has one `minecraft:item/template_spawn_egg` model for each of the 55 legacy entity paths. This includes the 32 original eggs and the item IDs retained for older port saves.

## Legacy Texture and Sound Inventory

- The packaged port retains the original bytes of all 687 PNGs, all ten animation metadata files, and all 88 Ogg clips. Five original PNGs use modern lowercase resource paths, including `textures/entities/witchhand.png`.
- `witchery:witchhand` uses the original fleshed hand geometry and its first-person and third-person finger poses. Death's Hand shares the model geometry with its original thinner bones and its separate deployed scythe state. The Brew Bag uses the original translucent bottle model, dye tint and untinted stopper. Both ordinary items use their original sprites in inventory and as drops.
- `witchery:earmuffs` binds the original five-piece geometry and `textures/entities/earmuffs.png` to Forge's head armor slot. It keeps the same registry ID and leather armor properties. The client sound hook and the Banshee and Mandrake handlers use that existing item ID.
- The original Apothecary texture uses Forge's villager profession layer with full-hat metadata. Native villager sleeping uses the 1.20.1 bed position and render pose in place of the original custom villager bed rendering.
- Dark Marks still laugh with the original `mob.torment.laugh` event. The separate original `mob.darkmark.idle` event and clip are available in the sound registry. The Love forecast broadcasts `random.loved` at half volume in the neutral category. Both the Love and Torment death samples come from the full 0.24.1 release JAR, which contains two clips missing from the extracted reference tree.

## Block Model Parity Anchors (current focus)
- `witchery:altar`
  - The ordinary cube and the formed cube use the original side, top and underside textures. Formed members draw green sparks from nearby plants using the first eight frames of `textures/particle/power.png`. The Forge particle atlas extracts the frames from the original sheet.
  - The Altar searches for mounted upgrades from the saved core in north, south, east and west order. Among competing skulls, lights and chalices, each first match supplies its own bonus.
- `witchery:cauldron`
  - Boiling liquid emits two bubbles tinted by the brew and positioned at its actual fill height. The atlas extracts sixteen bubble sprites and eight spark sprites from the original `power.png` sheet.
  - A brew with sufficient power emits a spark. The count builds to six circling sparks as a ritual progresses. Block entity updates carry the liquid amount, tint and spark count, which are derived from the existing cauldron save data and current power.
- `witchery:silvervat`
  - The placed Vat and its item share the original basin and animated blue surface. The item shows all four spouts. The placed model shows one spout per neighboring block entity and up to eight silver pieces based on the stored dust.
- `witchery:grassper` and `witchery:crystalball`
  - The items use their placed 3D models. The Grassper item shows the empty plant. A placed plant displays its held offering, which turns in Fancy and Fabulous graphics. The Crystal Ball shows its stand and three nested translucent cubes.
- `witchery:kettle`
  - The item shows the original empty basin. The placed model adds chains, a crossbar when the space above is open, up to two bottles, and four animated liquid frames. Liquid tint and particles distinguish water, brewing, ready and ruined batches. The block entity saves the original `Powered` flag and tracks power availability without charging a partial recipe.
- `witchery:distilleryidle` and `witchery:distilleryburning`
  - Both variants use the original still, frame, angled tube and up to four jars from `ModelDistillery`. The renderer reads the jar count from the synced block state and includes the original tilt on the second jar. Both item forms show the empty machine.
- `witchery:witchesovenidle` and `witchery:witchesovenburning`
  - Both IDs and their items share the original `ModelWitchesOven` body, lid, chimney and four feet. The baked model maps the original atlas to the corresponding side and rotates with the block's facing. The burning state adds the original smoke and flame positions through block particles.
- `witchery:spinningwheel`
  - The placed block and item share `SpinningWheelRenderer` with the original seat, legs, thread and mirrored wheel. The wheel and thread step their angles every 75 ms and 50 ms while the server reports active spinning. The item uses the stationary model.
- `witchery:fumefunnel` and `witchery:filteredfumefunnel`
  - Both use the original `ModelFumeFunnel` body, filter case, two side pipes and chimney cuboids with the shared `witchery:block/fumefunnel` atlas.
  - `FumeFunnelModel` bakes the corresponding parts with the block's facing. Forge model data reads both side positions and the block below to match the original renderer. The idle and burning oven IDs both connect. Item models use the standalone body for their variant.
- `witchery:wolfaltar`
  - Uses the original Wolf Altar geometry and texture through its block entity renderer and inventory renderer. The facing determines the front of the statue and the reward position.
  - The survival recipe accepts either Wolf Head variant in each trophy slot. The original offerings and trials advance from rank one through rank ten. The gold reward creates `witchery:mooncharm`, which retains its legacy wear value.
- `witchery:beartrap` and `witchery:wolftrap`
  - Legacy class: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/blocks/BlockBeartrap.java` (`new BlockBeartrap(true)`).
  - Legacy render model: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/client/model/ModelBeartrap.java`.
  - Legacy texture: `assets/witchery/textures/blocks/beartrap.png`.
  - Both variants use the original plate, hinges, jaws and teeth in a shared block entity renderer. The silvered variant uses the full-moon sheep lure and only springs on its recorded Wolfman.
- 2026-02-09 placeholder texture remap set (reported visual breakage triage):
  - `witchery:tormentstone` -> `minecraft:block/mycelium_top`.
  - `witchery:refillingchest` -> `witchery:block/leechchest`.
  - `witchery:brewliquid` -> `witchery:block/brew_still` + `witchery:block/brew_flow`.
  - `witchery:mirrorblock` -> `witchery:block/mirror`.
  - `witchery:mirrorblock2` -> `witchery:block/mirror2`.
- Fetish renderer/model anchors:
  - Legacy renderer: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/client/renderer/RenderFetish.java`.
  - Legacy models: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/client/model/ModelFetishTrent.java`, `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/client/model/ModelFetishScarecrow.java`.
  - `FetishRenderer` uses the legacy `64x64` Trent and Scarecrow textures, and the crossed Witch's Ladder texture. It renders both the items and placed models, with facing, dye and spectral opacity.
  - `LegacyDyeColors` shares the original sixteen colors across the fetishes, brooms, and Witchery pets. Spectral Scarecrow fabric renders at 0.7 opacity and its plain parts at 0.6. Spectral Trent wood renders at 0.6 and its face stays opaque. The Witch's Ladder uses the cutout texture in both forms.
  - Rites 84–86 bind the five ordinary effects to the existing `scarecrow`, `trent` and `witchsladder` IDs. The hidden sixth result summons Death or a player equipped with the three Death armor pieces and Death's Hand.
- `witchery:brazier` keeps its original block and block entity IDs. Slots 0–2 hold the ingredients and slot 3 persists the ignition ash. The state exposes `lit` for client flames. The eight recipes live in `BrazierRecipe`.
  - `BrazierRenderer` uses the bowl, four angled feet, stand and ash from `ModelBrazier` with the original `textures/block/brazier.png` atlas. The item uses the empty model in all display contexts. Vanilla block entity updates carry the count of occupied offering slots to reproduce the legacy ash heights.
- `witchery:spectre`, `witchery:banshee` and `witchery:poltergeist` use distinct entity types backed by `SpectralUndead`. The Spectral Stone's legacy metadata maps to `SpectralContents`, with the original species values 1 through 3 and matching item overrides. `witchery:wormy_apple` and `witchery:graveyard_dust` map legacy ingredient values 91 and 128.
- `witchery:icedoor`
  - Legacy class: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/blocks/BlockPerpetualIceDoor.java`.
  - Legacy textures: `assets/witchery/textures/blocks/icedoor_lower.png`, `icedoor_upper.png`.
  - 1.20.1 model parent requirement: `minecraft:block/door_*` parents, not `template_door_*`.
- `witchery:witchlog`
  - Legacy class: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/blocks/BlockWitchLog.java`.
  - Legacy rowan textures: `assets/witchery/textures/blocks/log_rowan.png`, `log_rowan_top.png`.
  - Current 1.20.1 scaffold uses `wood_type=rowan|alder|hawthorn` plus `axis` to mirror legacy multi-wood log variants.
- `witchery:mindrake`
  - Legacy class: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/blocks/BlockWitchCrop.java`.
  - Legacy stage textures: `assets/witchery/textures/blocks/mindrake_stage_0..4.png`.
- `witchery:wallgen`
  - Legacy class: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/worldgen/WorldHandlerVillageDistrict.java` (nested `BlockVillageWallGen`).
  - Internal worldgen helper block; legacy registration texture fallback was `iron_block`.
- Legacy crop family (`BlockWitchCrop`)
  - Legacy class: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/blocks/BlockWitchCrop.java`.
  - Legacy stage textures live under `assets/witchery/textures/blocks/*_stage_N.png`.
  - Current 1.20.1 staged parity coverage:
    - `witchery:belladonna` -> stage `0..4` (`age=5..7` mapped to stage `4`).
    - `witchery:mandrake` -> stage `0..4` (`age=5..7` mapped to stage `4`).
    - `witchery:artichoke` -> stage `0..4` (`age=5..7` mapped to stage `4`).
    - `witchery:snowbell` -> stage `0..4` (`age=5..7` mapped to stage `4`).
    - `witchery:wormwood` -> stage `0..4` (`age=5..7` mapped to stage `4`).
    - `witchery:mindrake` -> stage `0..4` (`age=5..7` mapped to stage `4`).
    - `witchery:wolfsbane` -> stage `0..7`.
    - `witchery:garlicplant` -> stage `0..5` (`age=6..7` mapped to stage `5`).
- Structural model/state families (2026-02-08 breadth pass)
  - The original and packaged Forge servers agree on effective blast resistance for all 121 shared default block states. Perpetual Ice and ordinary ice constructions use resistance 3, witchwood planks and stairs use 2, Bear and Wolf Traps use 6, and both Stockades and the Statue of Worship use 12.
  - Pistons leave the Candelabra and both Fume Funnels in place, and break the Heart Glyph, chalk glyphs, Dream Catcher, Ember Moss, Garlic Garland, Glint Weed, Plant Mine, and snow slabs. Dream Catchers drop their fitted weave. Garlic Garland and Glint Weed drop one item each. Both double snow slab forms drop two slabs.
  - The 28 legacy block IDs that Quark made movable declare a blocking piston reaction. This preserves the original immovability of altar members, workstations, wards, Poppet Shelves, Placed Items, and the other affected block entities when Quark moves ordinary containers.
  - Stairs IDs:
    - `witchery:stairswoodrowan`, `witchery:stairswoodalder`, `witchery:stairswoodhawthorn`, `witchery:icestairs`, `witchery:snowstairs`
    - blockstate shape source pattern: vanilla 1.20.1 `oak_stairs` variant matrix.
  - Slab IDs:
    - `witchery:witchwoodslab`, `witchery:iceslab`, `witchery:snowslab`
    - blockstate includes `type=bottom/top/double`; `double` points at legacy double slab cube models (`witchwooddoubleslab`, `icedoubleslab`, `snowdoubleslab`).
    - Snow slabs accept ordinary block items in their clicked cells. A matching slab merges on the exposed top or bottom face or with an adjacent half. Other clicks can change the half or leave it unchanged. The flattened and legacy double-slab forms both accept a single slab in their place.
  - Fence/FenceGate IDs:
    - `witchery:icefence`, `witchery:icefencegate`
    - fence uses multipart `north/east/south/west`; gate uses `facing/open/in_wall`.
  - Pressure plate IDs:
    - `witchery:icepressureplate`, `witchery:snowpressureplate`, `witchery:cwoodpressureplate`, `witchery:cstonepressureplate`, `witchery:csnowpressureplate`
    - blockstate property: `powered=true/false`.
    - The legacy importer keeps the original release delay and priority for pressed ice and snow plates as well as cursed controls.
  - Button IDs:
    - `witchery:cbuttonwood`, `witchery:cbuttonstone`
    - blockstate properties: `face`, `facing`, `powered`.
- Broad non-cube placeholder pass snapshot (2026-02-08)
  - converted from full-cube placeholders to non-full approximations:
    - plants/flat: `bramble`, `somniancotton`, `spanishmoss`, `vine`, `web`, `witchsapling`, `lilypad`, `leapinglily`, `cactus`
    - technical/invisible-like: `barrier`, `light`, `slurp`, `force`, `brewgas`
    - fluid-like visuals: `brew`, `brewliquid`, `disease`, `spiritflowing`, `hollowtears`
    - portal geometry: `spiritportal`, `tormentportal`
    - decorative/machine approximations: `altar`, `kettle`, `spinningwheel`, `statueofworship`, `coffinblock`, `refillingchest`, `mirrorwall`, `clever`, `distilleryidle`, `distilleryburning`, `fumefunnel`, `filteredfumefunnel`, `witchesovenidle`, `witchesovenburning`
  - `witchery:infinityegg` keeps the original inset shape and five-tick falling delay. It consumes use without teleporting, remains supported by grass, and multiplies altar capacity and recharge by ten when it lands on a formed altar. Legacy queued falls keep their named block tick. The legacy falling entity keeps its position, motion, and block type. An entity saved before its first update resumes from a single scheduled source block.
  - Force, Spirit Portal, Torment Portal, brew gas, and liquid brew return empty creative picks. The placed-item block copies its mounted stack and picks an Arthana when empty.
  - fixed slab item model placeholders:
    - `icedoubleslab`, `snowdoubleslab`, `witchwooddoubleslab` now parent block models instead of placeholder `taglockkit`.
  - remaining `cube_all` set narrowed to:
    - `bloodedwool`, `icedoubleslab`, `perpetualice`, `pitdirt`, `pitgrass`, `shadedglass`, `shadedglass_active`, `snowdoubleslab`, `tormentstone`, `wallgen`, `wickerbundle`, `witchleaves`, `witchwood`, `witchwooddoubleslab`.
- Horizontal-facing scaffold parity (2026-02-08)
  - code scaffold: `LegacyHorizontalFacingBlock` in `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/registry/WitcheryBlocks.java`
  - IDs now using placement-facing state + rotated blockstate variants:
    - `altar`
    - `kettle`
    - `spinningwheel`
    - `distilleryidle`
    - `distilleryburning`
    - `witchesovenidle`
    - `witchesovenburning`
    - `fumefunnel`
    - `filteredfumefunnel`
    - `coffinblock`
    - `statueofworship`
    - `refillingchest`
    - `leechchest`
- Wood/bundle variant-state parity (2026-02-08)
  - code scaffolds in `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/registry/WitcheryBlocks.java`:
    - `LegacyWitchWoodVariantBlock` (`wood_type` enum property)
    - `LegacyWickerBundleBlock` (`bundle_type` enum property)
  - blockstate coverage:
    - `witchwood` -> `wood_type=rowan|alder|hawthorn`
    - `witchleaves` -> `wood_type=rowan|alder|hawthorn`
    - `witchsapling` -> `wood_type=rowan|alder|hawthorn`
    - `wickerbundle` -> `bundle_type=plain|bloodied`
  - model coverage:
    - `witchwood_rowan|alder|hawthorn` using `planks_rowan|alder|hawthorn`
    - `witchleaves_rowan|alder|hawthorn` using `leaves_rowan|alder|hawthorn`
    - `witchsapling_rowan|alder|hawthorn` using `sapling_rowan|alder|hawthorn`
    - `wickerbundle_plain|bloodied` using `wicker_block_*` side/top textures
- Shaded glass color-state parity (2026-02-08)
  - code scaffold: `LegacyShadedGlassBlock` with enum property `color` in `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/registry/WitcheryBlocks.java`
  - IDs covered:
    - `shadedglass`
    - `shadedglass_active`
  - 16 legacy colors mapped:
    - `white`, `orange`, `magenta`, `light_blue`, `yellow`, `lime`, `pink`, `gray`, `silver`, `cyan`, `purple`, `blue`, `brown`, `green`, `red`, `black`
  - model families:
    - `shadedglass_<color>` -> texture `shadedglassoff_<color>`
    - `shadedglass_active_<color>` -> texture `shadedglass_<color>`
- Plant solidity/culling parity (2026-02-08)
  - code scaffold added in `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/registry/WitcheryBlocks.java`:
    - `LegacyNonSolidShapeBlock` (custom shape + collision control + no occlusion)
    - `LegacyWitchSaplingVariantBlock` (`wood_type` + no collision + no occlusion)
  - IDs remapped from default full-solid placeholder behavior:
    - `bloodrose`, `plantmine`, `glintweed`, `crittersnare`, `bramble`, `somniancotton`, `spanishmoss`, `vine`, `web`, `embermoss`, `lilypad`, `leapinglily`, `grassper`, `witchsapling`
  - model parity update to cross renderer:
    - `bloodrose`, `glintweed`, `embermoss`, `crittersnare`, `plantmine`
  - launch hotfix (2026-02-09):
    - fixed Forge block-state cache validation crash for `witchery:plantmine` (`offset type + collision shape`) by marking collision-enabled `LegacyNonSolidShapeBlock` variants as `dynamicShape`.
- Client transparency/cutout layer registration (2026-02-08)
  - file: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/client/WitcheryClient.java`
  - render-layer groups:
    - `cutout`: crops/flowers/vines/web/sapling/lily pads/doors (legacy plant-like translucent-alpha assets)
    - `cutoutMipped`: `witchleaves`
    - `translucent`: `shadedglass`, `shadedglass_active`, `perpetualice`

## Block Registry List (`WitcheryBlocks`)
Source: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/WitcheryBlocks.java`

| Field | Legacy Name |
|---|---|
| `CROP_BELLADONNA` | `witchery:belladonna` |
| `CROP_MANDRAKE` | `witchery:mandrake` |
| `CROP_ARTICHOKE` | `witchery:artichoke` |
| `CROP_SNOWBELL` | `witchery:snowbell` |
| `CROP_WORMWOOD` | `witchery:wormwood` |
| `CROP_MINDRAKE` | `witchery:mindrake` |
| `CROP_WOLFSBANE` | `witchery:wolfsbane` |
| `CROP_GARLIC` | `witchery:garlicplant` |
| `SAPLING` | `witchery:witchsapling` |
| `LOG` | `witchery:witchlog` |
| `LEAVES` | `witchery:witchleaves` |
| `VOID_BRAMBLE` | `witchery:voidbramble` (owner in tile `WITCPlacer`, 32-block circle rite suppression) |
| `BRAMBLE` | `witchery:bramble` |
| `GLINT_WEED` | `witchery:glintweed` (light 14, local spreading limit) |
| `SPANISH_MOSS` | `witchery:spanishmoss` (wall bits south 1, west 2, north 4, east 8, ceiling 0) |
| `LEAPING_LILY` | `witchery:leapinglily` |
| `TRAPPED_PLANT` | `witchery:plantmine` (twelve crafting, placed, and Boline-recoverable variants) |
| `EMBER_MOSS` | `witchery:embermoss` (light six, contact ignition, shear harvest) |
| `CRITTER_SNARE` | `witchery:crittersnare` |
| `GRASSPER` | `witchery:grassper` |
| `BLOOD_ROSE` | `witchery:bloodrose` |
| `WISPY_COTTON` | `witchery:somniancotton` |
| `WEB` | `witchery:web` |
| `VINE` | `witchery:vine` |
| `CACTUS` | `cactus` |
| `LILY` | `lilypad` |
| `DEMON_HEART` | `witchery:demonheart` |
| `WOLFHEAD` | `witchery:wolfhead` |
| `PLANKS` | `witchery:witchwood` |
| `STAIRS_ROWAN` | `witchery:stairswoodrowan` |
| `STAIRS_ALDER` | `witchery:stairswoodalder` |
| `STAIRS_HAWTHORN` | `witchery:stairswoodhawthorn` |
| `WOOD_SLAB_SINGLE` | `witchery:witchwoodslab` |
| `WOOD_SLAB_DOUBLE` | `witchery:witchwooddoubleslab` |
| `DOOR_ROWAN` | `witchery:rowanwooddoor` (location-bound lock, key from item placement) |
| `DOOR_ALDER` | `witchery:alderwooddoor` (emits redstone when open) |
| `CHALICE` | `witchery:chalice` |
| `CANDELABRA` | `witchery:candelabra` |
| `PLACED_ITEMSTACK` | `witchery:placeditem` |
| `ALLURING_SKULL` | `witchery:alluringskull` (mount in block metadata, active state in tile `SkullType`, floor rotation in `Rot`) |
| `WICKER_BUNDLE` | `witchery:wickerbundle` |
| `GLOW_GLOBE` | `witchery:glowglobe` |
| `STATUE_GODDESS` | `witchery:statuegoddess` (facing in metadata, owner in tile `WITCPlacer`) |
| `STOCKADE` | `witchery:stockade` (wood subtypes 0–8, contact damage) |
| `STOCKADE_ICE` | `witchery:icestockade` (Ice World conversion, contact damage) |
| `PERPETUAL_ICE` | `witchery:perpetualice` |
| `PERPETUAL_ICE_DOOR` | `witchery:icedoor` (hand and redstone operation, ingredient subtype 152) |
| `PERPETUAL_ICE_STAIRS` | `witchery:icestairs` |
| `PERPETUAL_ICE_SLAB_SINGLE` | `witchery:iceslab` |
| `PERPETUAL_ICE_SLAB_DOUBLE` | `witchery:icedoubleslab` |
| `PERPETUAL_ICE_FENCE` | `witchery:icefence` |
| `PERPETUAL_ICE_FENCE_GATE` | `witchery:icefencegate` |
| `PERPETUAL_ICE_PRESSURE_PLATE` | `witchery:icepressureplate` |
| `SNOW_PRESSURE_PLATE` | `witchery:snowpressureplate` |
| `SNOW_STAIRS` | `witchery:snowstairs` |
| `SNOW_SLAB_SINGLE` | `witchery:snowslab` |
| `SNOW_SLAB_DOUBLE` | `witchery:snowdoubleslab` |
| `INFINITY_EGG` | `witchery:infinityegg` |
| `SLURP` | `witchery:slurp` |
| `PIT_DIRT` | `witchery:pitdirt` |
| `PIT_GRASS` | `witchery:pitgrass` |
| `GARLIC_GARLAND` | `witchery:garlicgarland` |
| `BLOODED_WOOL` | `witchery:bloodedwool` |
| `SHADED_GLASS` | `witchery:shadedglass` |
| `SHADED_GLASS_ON` | `witchery:shadedglass_active` |
| `MIRROR_WALL` | `witchery:mirrorwall` |
| `REFILLING_CHEST` | `witchery:refillingchest` |
| `FORCE` | `witchery:force` |
| `TORMENT_STONE` | `witchery:tormentstone` |
| `BARRIER` | `witchery:barrier` |
| `LEECH_CHEST` | `witchery:leechchest` |
| `ALTAR` | `witchery:altar` |
| `KETTLE` | `witchery:kettle` |
| `POPPET_SHELF` | `witchery:poppetshelf` |
| `DREAM_CATCHER` | `witchery:dreamcatcher` |
| `CRYSTAL_BALL` | `witchery:crystalball` |
| `SPIRIT_PORTAL` | `witchery:spiritportal` |
| `TORMENT_PORTAL` | `witchery:tormentportal` |
| `SPINNING_WHEEL` | `witchery:spinningwheel` |
| `BRAZIER` | `witchery:brazier` |
| `OVEN_IDLE` | `witchery:witchesovenidle` |
| `OVEN_BURNING` | `witchery:witchesovenburning` |
| `OVEN_FUMEFUNNEL` | `witchery:fumefunnel` |
| `OVEN_FUMEFUNNEL_FILTERED` | `witchery:filteredfumefunnel` |
| `DISTILLERY_IDLE` | `witchery:distilleryidle` |
| `DISTILLERY_BURNING` | `witchery:distilleryburning` |
| `FETISH_SCARECROW` | `witchery:scarecrow` |
| `FETISH_TREANT_IDOL` | `witchery:trent` |
| `FETISH_WITCHS_LADDER` | `witchery:witchsladder` |
| `DECURSE_TELEPORT` | `witchery:decurseteleport` |
| `DECURSE_DIRECTED` | `witchery:decursedirected` |
| `STATUE_OF_WORSHIP` | `witchery:statueofworship` |
| `CAULDRON` | `witchery:cauldron` |
| `WOLF_ALTAR` | `witchery:wolfaltar` |
| `SILVER_VAT` | `witchery:silvervat` |
| `BEARTRAP` | `witchery:beartrap` |
| `WOLFTRAP` | `witchery:wolftrap` |
| `WALLGEN` | `witchery:wallgen` |
| `COFFIN` | `witchery:coffinblock` |
| `LIGHT` | `witchery:light` (invisible level-15 light, with a temporary state for moving sources) |
| `DAYLIGHT_COLLECTOR` | `witchery:daylightcollector` (`charge` 0–15 in block state) |
| `BLOOD_CRUCIBLE` | `witchery:bloodcrucible` |
| `MIRROR` | `witchery:mirrorblock` |
| `MIRROR_UNBREAKABLE` | `witchery:mirrorblock2` |
| `CIRCLE` | `witchery:circle` |
| `GLYPH_RITUAL` | `witchery:circleglyphritual` |
| `GLYPH_OTHERWHERE` | `witchery:circleglyphotherwhere` |
| `GLYPH_INFERNAL` | `witchery:circleglyphinfernal` |
| `FLOWING_SPIRIT` | `witchery:spiritflowing` |
| `HOLLOW_TEARS` | `witchery:hollowtears` |
| `DISEASE` | `witchery:disease` |
| `BREW` | `witchery:brew` |
| `BREW_GAS` | `witchery:brewgas` |
| `BREW_LIQUID` | `witchery:brewliquid` |
| `CURSED_LEVER` | `witchery:clever` |
| `CURSED_WOODEN_DOOR` | `witchery:cwoodendoor` |
| `CURSED_WOODEN_PRESSURE_PLATE` | `witchery:cwoodpressureplate` |
| `CURSED_STONE_PRESSURE_PLATE` | `witchery:cstonepressureplate` |
| `CURSED_SNOW_PRESSURE_PLATE` | `witchery:csnowpressureplate` |
| `CURSED_BUTTON_WOOD` | `witchery:cbuttonwood` |
| `CURSED_BUTTON_STONE` | `witchery:cbuttonstone` |

## Item Registry List (`WitcheryItems`)
Source: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/WitcheryItems.java`

The original `witchhat`, `witchrobe`, `necromancerrobe`, and `babashat` IDs equip the modeled clothing. The corresponding ingredient mapping uses `impregnated_leather` for 72 and `brew_of_raising` for 65. `necromantic_stone` issues commands to owned undead.
Witch clothing models hide on invisible wearers and remain visible on armor stands. Vanilla armor visibility and crouching scale the legacy mob search for nearby invisible players. The existing `VampireLevel` data also controls whether zombies target a visible player at rank ten.

| Field | Legacy Name |
|---|---|
| `GENERIC` | `witchery:ingredient` |
| `TAGLOCK_KIT` | `witchery:taglockkit` |
| `POPPET` | `witchery:poppet` |
| `POTIONS` | `witchery:potion` (Universal Antidote, subtype zero, removes Poison and Wither) |
| `SLAB_SINGLE` | `witchery:witchwoodslab` |
| `SLAB_DOUBLE` | `witchery:witchwooddoubleslab` |
| `PERPERTUAL_ICE_SLAB_SINGLE` | `witchery:iceslab` |
| `PERPERTUAL_ICE_SLAB_DOUBLE` | `witchery:icedoubleslab` |
| `SNOW_SLAB_SINGLE` | `witchery:snowslab` |
| `SNOW_SLAB_DOUBLE` | `witchery:snowdoubleslab` |
| `COFFIN` | `witchery:coffin` |
| `STEW` | `witchery:stew` |
| `STEW_RAW` | `witchery:stewraw` |
| `MIRROR` | `witchery:mirror` |
| `CHALK_RITUAL` | `witchery:chalkritual` |
| `CHALK_OTHERWHERE` | `witchery:chalkotherwhere` |
| `CHALK_INFERNAL` | `witchery:chalkinfernal` |
| `CHALK_GOLDEN` | `witchery:chalkheart` |
| `BOLINE` | `witchery:boline` |
| `ARTHANA` | `witchery:arthana` |
| `HUNTSMANS_SPEAR` | `witchery:huntsmanspear` |
| `CROSSBOW_PISTOL` | `witchery:handbow` |
| `DEATH_HAND` | `witchery:deathshand` |
| `SILVER_SWORD` | `witchery:silversword` |
| `CANE_SWORD` | `witchery:canesword` |
| `WITCH_HAND` | `witchery:witchhand` |
| `CIRCLE_TALISMAN` | `witchery:circletalisman` |
| `MYSTIC_BRANCH` | `witchery:mysticbranch` |
| `DIVINER_WATER` | `witchery:divinerwater` |
| `DIVINER_LAVA` | `witchery:divinerlava` |
| `POLYNESIA_CHARM` | `witchery:polynesiacharm` |
| `DEVILS_TONGUE_CHARM` | `witchery:devilstonguecharm` |
| `MUTATING_SPRIG` | `witchery:mutator` |
| `PARASYTIC_LOUSE` | `witchery:louse` |
| `BREW_BAG` | `witchery:brewbag` |
| `SPECTRAL_STONE` | `witchery:spectralstone` |
| `SHELF_COMPASS` | `witchery:shelfcompass` |
| `KOBOLDITE_PICKAXE` | `witchery:kobolditepickaxe` |
| `DUP_STAFF` | `witchery:dupstaff` |
| `BREW` | `witchery:brewbottle` |
| `BREW_FUEL` | `witchery:brew.fuel` |
| `BREW_ENDLESS_WATER` | `witchery:brew.water` |
| `BIOME_BOOK` | `witchery:bookbiomes2` (stable `WitcheryBiome` ID and legacy `CurrentPage` import) |
| `BIOME_NOTE` | `witchery:biomenote` (legacy atlas `Damage` index imports to `WitcheryBiome`) |
| `CAULDRON_BOOK` | `witchery:cauldronbook` |
| `LEONARDS_URN` | `witchery:leonardsurn` (legacy tiers 0–3, stored splash brews and Mystic Branch slots) |
| `PLAYER_COMPASS` | `witchery:playercompass` |
| `MOON_CHARM` | `witchery:mooncharm` |
| `HORN_OF_THE_HUNT` | `witchery:hornofthehunt` |
| `CREATIVE_WOLF_TOKEN` | `witchery:wolftoken` |
| `BLOOD_GOBLET` | `witchery:glassgoblet` |
| `SUN_GRENADE` | `witchery:sungrenade` (from the loaded Daylight Collector) |
| `VAMPIRE_BOOK` | `witchery:vampirebook` (`Damage` 0–9 becomes `VampireBookPages`; retains `pageStack` bookmarks) |
| `DUP_GRENADE` | `witchery:dupgrenade` (Mirror-charged sphere with `Owner` tag) |
| `WITCH_HAT` | `witchery:witchhat` |
| `BABAS_HAT` | `witchery:babashat` |
| `WITCH_ROBES` | `witchery:witchrobe` |
| `NECROMANCERS_ROBES` | `witchery:necromancerrobe` |
| `BITING_BELT` | `witchery:bitingbelt` (dyeable leggings, two charges in `WITCPotion` and `WITCPotion2`) |
| `BARK_BELT` | `witchery:barkbelt` (dyeable leggings, renewable `WITCBarkPieces` ward) |
| `ICY_SLIPPERS` | `witchery:iceslippers` (boots that freeze water, solidify lava, and activate perpetual ice plates) |
| `RUBY_SLIPPERS` | `witchery:rubyslippers` (boots that cast a return-home or Waystone chant using infusion charge) |
| `SEEPING_SHOES` | `witchery:seepingshoes` (boots that turn Poison and Wither into nearby plant growth) |
| `HUNTER_HAT` | `witchery:hunterhat` |
| `HUNTER_COAT` | `witchery:huntercoat` |
| `HUNTER_LEGS` | `witchery:hunterlegs` |
| `HUNTER_BOOTS` | `witchery:hunterboots` |
| `HUNTER_HAT_SILVERED` | `witchery:hunterhatsilvered` |
| `HUNTER_COAT_SILVERED` | `witchery:huntercoatsilvered` |
| `HUNTER_LEGS_SILVERED` | `witchery:hunterlegssilvered` |
| `HUNTER_BOOTS_SILVERED` | `witchery:hunterbootssilvered` |
| `HUNTER_HAT_GARLICKED` | `witchery:hunterhatgarlicked` |
| `HUNTER_COAT_GARLICKED` | `witchery:huntercoatgarlicked` |
| `HUNTER_LEGS_GARLICKED` | `witchery:hunterlegsgarlicked` |
| `HUNTER_BOOTS_GARLICKED` | `witchery:hunterbootsgarlicked` |
| `DEATH_HOOD` | `witchery:deathscowl` |
| `DEATH_ROBE` | `witchery:deathsrobe` |
| `DEATH_FEET` | `witchery:deathsfeet` |
| `MOGS_QUIVER` | `witchery:quiverofmog` |
| `GULGS_GURDLE` | `witchery:gurdleofgulg` |
| `KOBOLDITE_HELM` | `witchery:kobolditehelm` |
| `EARMUFFS` | `witchery:earmuffs` |
| `VAMPIRE_HAT` | `witchery:vampirehat` |
| `VAMPIRE_HELMET` | `witchery:vampirehelmet` |
| `VAMPIRE_COAT` | `witchery:vampirecoat` |
| `VAMPIRE_COAT_FEMALE` | `witchery:vampirecoat_female` |
| `VAMPIRE_COAT_CHAIN` | `witchery:vampirechaincoat` |
| `VAMPIRE_COAT_FEMALE_CHAIN` | `witchery:vampirechaincoat_female` |
| `VAMPIRE_LEGS` | `witchery:vampirelegs` |
| `VAMPIRE_LEGS_KILT` | `witchery:vampirelegs_kilt` |
| `VAMPIRE_BOOTS` | `witchery:vampireboots` |
| `SEEDS_BELLADONNA` | `witchery:seedsbelladonna` |
| `SEEDS_MANDRAKE` | `witchery:seedsmandrake` |
| `SEEDS_ARTICHOKE` | `witchery:seedsartichoke` |
| `SEEDS_SNOWBELL` | `witchery:seedssnowbell` |
| `SEEDS_WORMWOOD` | `witchery:seedswormwood` |
| `SEEDS_MINDRAKE` | `witchery:seedsmindrake` |
| `SEEDS_WOLFSBANE` | `witchery:seedswolfsbane` |
| `SEEDS_GARLIC` | `witchery:garlic` |
| `BUCKET_FLOWINGSPIRIT` | `witchery:bucketspirit` |
| `BUCKET_HOLLOWTEARS` | `witchery:buckethollowtears` |
| `BUCKET_BREW` | `witchery:bucketbrew` |

## ItemGeneral Metadata Variants (`Witchery.Items.GENERIC`)
Source: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/item/ItemGeneral.java`

This is one registered item (`witchery:ingredient`) with many metadata subtypes.

All 163 registered subtypes resolve during a legacy stack upgrade. The port assigns 161 individual Witchery item IDs and uses vanilla raw or cooked mutton for the other two. The `witchery:ingredient` registration preserves existing unconverted stacks.

The `mutandisAdditionalBlocks` server setting ports `MutandisAdditionalBlocks`. It defaults to Glintweed and Fern, the flattened equivalent of `tallgrass,2`. Each entry can be both an input and an outcome for ordinary Mutandis and Extremis. Repeated entries add weight. The setting accepts modern IDs and states and uses the legacy block-state converter for old IDs and metadata. The base plants remain data driven through the Mutandis block tags.

| Legacy metadata | 1.20.1 item ID |
|---:|---|
| 0 | `witchery:candelabra` |
| 1 | `witchery:chalice` |
| 2 | `witchery:filled_chalice` |
| 10 | `witchery:attuned_stone` |
| 11 | `witchery:charged_attuned_stone` |
| 14 | `witchery:mutandis` |
| 15 | `witchery:mutandis_extremis` |
| 16 | `witchery:quicklime` |
| 17 | `witchery:gypsum` |
| 18 | `witchery:wood_ash` |
| 21 | `witchery:belladonna_flower` |
| 22 | `witchery:mandrake_root` |
| 24 | `witchery:bat_wool` |
| 25 | `witchery:dog_tongue` |
| 26 | `witchery:soft_clay_jar` |
| 27 | `witchery:clay_jar` |
| 28 | `witchery:foul_fume` |
| 29 | `witchery:diamond_vapour` |
| 30 | `witchery:oil_of_vitriol` |
| 31 | `witchery:exhale_of_the_horned_one` |
| 32 | `witchery:breath_of_the_goddess` |
| 33 | `witchery:hint_of_rebirth` |
| 34 | `witchery:whiff_of_magic` |
| 35 | `witchery:reek_of_misfortune` |
| 36 | `witchery:odour_of_purity` |
| 37 | `witchery:tear_of_the_goddess` |
| 38 | `witchery:refined_evil` |
| 39 | `witchery:drop_of_luck` |
| 40 | `witchery:redstone_soup` |
| 60 | `witchery:brew_of_ink` |
| 63 | `witchery:rowan_berries` |
| 66 | `witchery:spectral_dust` |
| 67 | `witchery:ender_dew` |
| 69 | `witchery:globe_artichoke` |
| 73 | `witchery:fume_filter` |
| 74 | `witchery:creeper_heart` |
| 75 | `witchery:brew_of_love` |
| 77 | `witchery:brew_of_the_depths` |
| 78 | `witchery:icy_needle` |
| 79 | `witchery:frozen_heart` |
| 99 | `witchery:disturbed_cotton` |
| 100 | `witchery:fanciful_thread` |
| 101 | `witchery:tormented_twine` |
| 102 | `witchery:golden_thread` |
| 105 | `witchery:purified_milk` |
| 111 | `witchery:wormwood_leaf` |
| 153 | `witchery:anointing_paste` |
| 156 | `witchery:wolfsbane_flower` |
| 160 | `witchery:torn_vampire_page` |
| 161 | `witchery:dark_cloth` |

Variant 156 consumes one flower when the tester targets a living entity within two blocks. Cursed targets emit flames and the Wolfman howl, including cursed humans and unshifted werevillagers. The `witchery:potion.wolfsbane` effect is a separate use of the same ingredient through general cauldron brewing.

| Meta | Unlocalized Key |
|---:|---|
| 0 | `candelabra` |
| 1 | `chalice` |
| 2 | `chaliceFull` |
| 3 | `weaveMoveFast` |
| 4 | `weaveDigFast` |
| 5 | `weaveSaturation` |
| 6 | `weaveNightmares` |
| 7 | `boneNeedle` |
| 8 | `broom` → `witchery:broom` |
| 9 | `broomEnchanted` → `witchery:enchanted_broom` |
| 10 | `attunedStone` |
| 11 | `attunedStoneCharged` |
| 12 | `waystone` → `witchery:waystone` |
| 13 | `waystoneBound` → `witchery:bound_waystone` |
| 14 | `mutandis` |
| 15 | `mutandisExtremis` |
| 16 | `quicklime` |
| 17 | `gypsum` |
| 18 | `ashWood` |
| 21 | `belladonna` |
| 22 | `mandrakeRoot` |
| 23 | `demonHeart` |
| 24 | `batWool` |
| 25 | `dogTongue` |
| 26 | `clayJarSoft` |
| 27 | `clayJar` |
| 28 | `foulFume` |
| 29 | `diamondVapour` |
| 30 | `oilOfVitriol` |
| 31 | `exhaleOfTheHornedOne` |
| 32 | `breathOfTheGoddess` |
| 33 | `hintOfRebirth` |
| 34 | `whiffOfMagic` |
| 35 | `reekOfMisfortune` |
| 36 | `odourOfPurity` |
| 37 | `tearOfTheGoddess` |
| 38 | `refinedEvil` |
| 39 | `dropOfLuck` |
| 40 | `redstoneSoup` |
| 41 | `flyingOintment` → `witchery:flying_ointment` |
| 42 | `ghostOfTheLight` → `witchery:ghost_of_the_light` |
| 43 | `soulOfTheWorld` → `witchery:soul_of_the_world` |
| 44 | `spiritOfOtherwhere` → `witchery:spirit_of_otherwhere` |
| 45 | `infernalAnimus` → `witchery:infernal_animus` |
| 46 | `bookOven` → `witchery:book_collecting_fumes` |
| 47 | `bookDistilling` → `witchery:book_distilling` |
| 48 | `bookCircleMagic` → `witchery:book_circle_magic` |
| 49 | `bookInfusions` → `witchery:book_brews_and_infusions` |
| 50 | `oddPorkchopRaw` |
| 51 | `oddPorkchopCooked` |
| 52 | `doorRowan` → `witchery:rowanwooddoor` |
| 53 | `doorAlder` → `witchery:alderwooddoor` |
| 54 | `doorKey` → `witchery:door_key` |
| 55 | `rock` → `witchery:rock` |
| 56 | `web` → `witchery:thrown_web` |
| 57 | `brewVines` |
| 58 | `brewWeb` |
| 59 | `brewThorns` |
| 60 | `brewInk` |
| 61 | `brewSprouting` |
| 62 | `brewErosion` |
| 63 | `berriesRowan` |
| 64 | `necroStone` |
| 65 | `brewRaising` |
| 66 | `spectralDust` |
| 67 | `enderDew` |
| 69 | `artichoke` maps to `witchery:globe_artichoke`. Eating applies Hunger III based on food restored. |
| 70 | `seedsTreefyd` → `witchery:treefyd_seeds` |
| 71 | `brewGrotesque` |
| 72 | `impregnatedLeather` |
| 73 | `fumeFilter` |
| 74 | `creeperHeart` maps to `witchery:creeper_heart`. Eating gives brief Fire Resistance and a configurable blast. |
| 75 | `brewLove` |
| 76 | `brewIce` |
| 77 | `brewDepths` |
| 78 | `icyNeedle` |
| 79 | `frozenHeart` maps to `witchery:frozen_heart`. Eating gives brief Fire Resistance and clears the three Imp effects. |
| 80 | `infernalBlood` |
| 81 | `bookHerbology` → `witchery:book_herbology` |
| 82 | `entbranch` |
| 83 | `mysticunguent` → `witchery:mystic_unguent` |
| 84 | `doorKeyring` → `witchery:door_keyring` |
| 85 | `brewFrogsTongue` |
| 86 | `brewCursedLeaping` |
| 87 | `brewHitchcock` |
| 88 | `brewInfection` |
| 89 | `owletsWing` |
| 90 | `toeOfFrog` |
| 91 | `appleWormy` |
| 92 | `quartzSphere` → `witchery:quartz_sphere` |
| 93 | `happenstanceOil` → `witchery:happenstance_oil` |
| 94 | `seerStone` → `witchery:seer_stone` |
| 95 | `brewSleep` |
| 96 | `brewFlowingSpirit` |
| 97 | `brewWasting` |
| 98 | `sleepingApple` maps to `witchery:sleeping_apple`. Eating it starts an ordinary nightmare in the Dream. |
| 99 | `disturbedCotton` |
| 100 | `fancifulThread` |
| 101 | `tormentedTwine` |
| 102 | `goldenThread` |
| 103 | `mellifluousHunger` |
| 104 | `weaveIntensity` |
| 105 | `purifiedMilk` |
| 106 | `bookBiomes` → `witchery:book_of_biomes` |
| 107 | `bookWands` → `witchery:book_symbology` |
| 108 | `batBall` |
| 109 | `brewBats` |
| 110 | `charmDisruptedDreams` |
| 111 | `wormwood` |
| 112 | `subduedSpirit` → `witchery:subdued_spirit` |
| 113 | `focusedWill` |
| 114 | `condensedFear` |
| 115 | `brewHollowTears` |
| 116 | `brewSolidStone` |
| 117 | `brewSolidDirt` |
| 118 | `brewSolidSand` |
| 119 | `brewSolidSandstone` |
| 120 | `brewSolidErosion` |
| 121 | `infusionBase` → `witchery:infused_brew_base` |
| 122 | `brewSoaring` → `witchery:brew_of_soaring` |
| 123 | `brewGrave` |
| 124 | `brewRevealing` → `witchery:brew_of_revealing` (Kettle splash) |
| 125 | `brewSubstitution` |
| 126 | `brewCongealedSpirit` → `witchery:congealed_spirit` |
| 127 | `bookBurning` → `witchery:book_conjuration_and_fetishes` |
| 128 | `graveyardDust` |
| 129 | `binkyhead` → `witchery:binky_head` (places Binky) |
| 130 | `nullcatalyst` |
| 131 | `nullifiedleather` |
| 132 | `boltStake` |
| 133 | `boltAntiMagic` |
| 134 | `boltHoly` |
| 135 | `boltSplitting` |
| 136 | `brewSoulHunger` |
| 137 | `brewSoulAnguish` |
| 138 | `brewSoulFear` |
| 139 | `brewSoulTorment` |
| 140 | `contract` → `witchery:contract_of_ownership` |
| 141 | `contractTorment` → `witchery:contract_of_torment` |
| 142 | `contractBlaze` → `witchery:contract_living_flame` |
| 143 | `contractResistFire` → `witchery:contract_fiery_tolerance` |
| 144 | `contractEvaporate` → `witchery:contract_evaporation` |
| 145 | `contractFieryTouch` → `witchery:contract_fiery_touch` |
| 146 | `contractSmelting` → `witchery:contract_melting_touch` |
| 147 | `waystoneCreatureBound` → `witchery:creature_bound_waystone` |
| 148 | `kobolditedust` → `witchery:koboldite_dust` |
| 149 | `kobolditenugget` → `witchery:koboldite_nugget` |
| 150 | `kobolditeingot` → `witchery:koboldite_ingot` |
| 151 | `pentacle` → `witchery:koboldite_pentacle` |
| 152 | `doorIce` → `witchery:icedoor` |
| 153 | `annointingPaste` |
| 154 | `subduedSpiritVillage` → `witchery:subdued_village_spirit` |
| 155 | `boltSilver` |
| 156 | `wolfsbane` |
| 157 | `silverdust` → `witchery:silver_dust` |
| 158 | `muttonraw` → `minecraft:mutton` |
| 159 | `muttoncooked` → `minecraft:cooked_mutton` |
| 160 | `vbookPage` |
| 161 | `darkCloth` |
| 162 | `stake` (`witchery:stake`, eight garlic and a stick, `allowStakingVampires`) |
| 163 | `warmBlood` maps to `witchery:warm_blood`. Humans get Hunger, and vampires replenish blood. |
| 164 | `lilithsBlood` maps to `witchery:liliths_blood`. The drink grants a rank or replenishes blood at rank ten. |
| 165 | `heartofgold` → `witchery:heart_of_gold` (Fairest drop and Animal Attraction or Repulsion brew action) |

## Poppet Variants (`Witchery.Items.POPPET`)
Source: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/item/ItemPoppet.java`

| Meta | Key | Display Name |
|---:|---|---|
| 0 | `poppet` | Poppet |
| 1 | `protectEarth` | Earth Protection Poppet → `witchery:earth_protection_poppet` |
| 2 | `protectWater` | Water Protection Poppet → `witchery:water_protection_poppet` |
| 3 | `protectFire` | Fire Protection Poppet → `witchery:fire_protection_poppet` |
| 4 | `protectStarvation` | Hunger Protection Poppet → `witchery:hunger_protection_poppet` |
| 5 | `protectTool` | Tool Protection Poppet → `witchery:tool_protection_poppet` |
| 6 | `protectDeath` | Death Protection Poppet → `witchery:death_protection_poppet` |
| 7 | `protectVoodoo` | Voodoo Protection Poppet → `witchery:voodoo_protection_poppet` |
| 8 | `voodoo` | Voodoo Poppet → `witchery:voodoo_poppet` |
| 9 | `vampiric` | Vampiric Poppet → `witchery:vampiric_poppet` |
| 10 | `protectPoppet` | Poppet Protection → `witchery:poppet_protection` |
| 11 | `protectArmor` | Armor Protection Poppet → `witchery:armor_protection_poppet` |

## Fluids
Source: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/WitcheryFluids.java`

| Field | Fluid Name | Class |
|---|---|---|
| `FLOWING_SPIRIT` | `witchery:fluidspirit` | `Fluid` |
| `HOLLOW_TEARS` | `witchery:hollowtears` | `Fluid` |
| `BREW` | `witchery:brew` | `FluidBrew` |
| `BREW_GAS` | `witchery:brewgas` | `Fluid` |
| `BREW_LIQUID` | `witchery:brewliquid` | `Fluid` |
| `DISEASE` | `witchery:fluiddisease` | `Fluid` |

### Bucket / Container Bindings
Source: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/WitcheryItems.java`

| Item Field | Item Name | Fluid Field |
|---|---|---|
| `BUCKET_FLOWINGSPIRIT` | `witchery:bucketspirit` | `FLOWING_SPIRIT` |
| `BUCKET_HOLLOWTEARS` | `witchery:buckethollowtears` | `HOLLOW_TEARS` |
| `BUCKET_BREW` | `witchery:bucketbrew` | `BREW` |

### 1.20.1 Fluid Scaffold Mapping (current)
Implementation anchors:
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/registry/WitcheryFluids.java`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/registry/WitcheryBlocks.java`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/registry/WitcheryItems.java`

| Legacy Fluid ID | 1.20.1 Source/Flowing IDs | Linked Block ID | Linked Bucket ID |
|---|---|---|---|
| `fluidspirit` | `witchery:fluidspirit`, `witchery:flowing_fluidspirit` | `witchery:spiritflowing` (`LiquidBlock`) | `witchery:bucketspirit` (`BucketItem`) |
| `hollowtears` | `witchery:hollowtears`, `witchery:flowing_hollowtears` | `witchery:hollowtears` (`LiquidBlock`) | `witchery:buckethollowtears` (`BucketItem`) |
| `brew` | `witchery:brew`, `witchery:flowing_brew` | `witchery:brew` (`LiquidBlock`) | `witchery:bucketbrew` (`BrewBucketItem`, ordered mixture and color in its tag) |
| `brewgas` | `witchery:brewgas`, `witchery:flowing_brewgas` | `witchery:brewgas` (`BrewDispersalBlock`) | _(none)_ |
| `brewliquid` | `witchery:brewliquid`, `witchery:flowing_brewliquid` | `witchery:brewliquid` (`BrewDispersalBlock`) | _(none)_ |
| `fluiddisease` | `witchery:fluiddisease`, `witchery:flowing_fluiddisease` | `witchery:disease` (`LiquidBlock`) | _(none)_ |

## 1.20.1 Data Generation
Implementation anchors:
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/data/WitcheryDataGenerators.java`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/data/WitcheryBlockTagsProvider.java`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/data/WitcheryLootTableProvider.java`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/data/WitcheryBlockLoot.java`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/data/WitcheryRecipeProvider.java`

`GatherDataEvent` registers client models for the stockades and server providers for block tags, recipes, and loot. Generated recipes cover Witchwood variants, the progression machines, poppets, and other legacy crafting entries. The packaged 1.20.1 JAR contains 403 recipe JSON files and 92 block loot tables from the generated and handwritten resources together.

## 1.20.1 Spawn Eggs
Implementation anchor:
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/registry/WitcheryItems.java`

The 32 original eggs use their original colors and `<normalized_entity_name>_spawn_egg` IDs. They appear in the Witchery creative tab and resolve the legacy numeric spawn egg metadata during world import. The older port's extra egg IDs stay in the Forge item registry for saves and are omitted from the Witchery tab.

Each original egg has a localized name. Older item-only projectile eggs display as legacy spawn eggs in saved inventories. The functional `broom_spawn_egg` item shares the Enchanted Broom's name and still places the rideable broom.

The creative tab follows the original visibility rules for the registered crop blocks, burning machine states, liquids, and technical placements. Player-facing items such as seeds, chalk, the mirror, and the coffin remain available. The original FML fixture's 121 block names and 217 item names all have modern registrations. Specialized state and subtype verification is tracked under Legacy Save Fixtures in the porting plan.

Examples:
| Legacy Entity ID | Spawn Egg Item ID |
|---|---|
| `demon` | `witchery:demon_spawn_egg` |
| `hornedHuntsman` | `witchery:hornedhuntsman_spawn_egg` |
| `reflection` | `witchery:reflection_spawn_egg` |
| `wingedmonkey` | `witchery:wingedmonkey_spawn_egg` |

## Entity Registry (`WitcheryEntities`)
Source: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/WitcheryEntities.java`

| ID | Field | Class | Legacy Name |
|---:|---|---|---|
| 92 | `DEMON` | `EntityDemon` | `demon` |
| 93 | `BROOM` | `EntityBroom` | `broom` (rideable vehicle, hue, damage and recovery) |
| 94 | `BREW` | `EntityWitchProjectile` | `brew` → `witchery:thrown_brew` (fixed brew or projectile selected by `damageValue`) |
| 95 | `SPECTRAL_FAMILIAR` | `EntityFamiliar` | `familiar` (tamed spectral pig, ore search, `ItemToFind`, `Searches`) |
| 96 | `MANDRAKE` | `EntityMandrake` | `mandrake` |
| 97 | `TREEFYD` | `EntityTreefyd` | `treefyd` (planted guardian, Taglock training, Boline patrol, heart upgrades) |
| 98 | `HUNTSMAN` | `EntityHornedHuntsman` | `hornedHuntsman` → `witchery:hornedhuntsman` (Horn of the Hunt, entrance, boss combat and rank-five trial) |
| 99 | `SPELL` | `EntitySpellEffect` | `spellEffect` → `witchery:spelleffect` |
| 100 | `ENT` | `EntityEnt` | `ent` (Witchwood harvest awakening, legacy `Owner` field retained) |
| 101 | `ILLUSION_CREEPER` | `EntityIllusionCreeper` | `illusionCreeper` |
| 102 | `ILLUSION_SPIDER` | `EntityIllusionSpider` | `illusionSpider` |
| 103 | `ILLUSION_ZOMBIE` | `EntityIllusionZombie` | `illusionZombie` |
| 104 | `OWL` | `EntityOwl` | `owl` (Mutating Sprig, taming, feathers, Waystone delivery, Infernal power 24) |
| 105 | `TOAD` | `EntityToad` | `toad` (Mutating Sprig, taming, amphibious behavior, Infernal power 25) |
| 106 | `CAT_FAMILIAR` | `EntityWitchCat` | `cat` |
| 107 | `LOUSE` | `EntityParasyticLouse` | `louse` (original arthropod model and mutation, saved `BitePotionEffect`) |
| 108 | `EYE` | `EntityEye` | `eye` |
| 109 | `BABA_YAGA` | `EntityBabaYaga` | `babayaga` |
| 110 | `COVEN_WITCH` | `EntityCovenWitch` | `covenwitch` (spawn, appearance, recruitment, quests, defensive potions, visits, ritual seats, and member death) |
| 111 | `PLAYER_CORPSE` | `EntityCorpse` | `corpse` |
| 112 | `NIGHTMARE` | `EntityNightmare` | `nightmare` |
| 113 | `SPECTRE` | `EntitySpectre` | `spectre` |
| 114 | `POLTERGEIST` | `EntityPoltergeist` | `poltergeist` |
| 115 | `BANSHEE` | `EntityBanshee` | `banshee` |
| 116 | `SPIRIT` | `EntitySpirit` | `spirit` (Dream spawn, focus attraction, loot, structure guide) |
| 117 | `DEATH` | `EntityDeath` | `death` (hidden fetish rite, boss combat and reward drops) |
| 118 | `CROSSBOW_BOLT` | `EntityBolt` | `bolt` |
| 119 | `WITCH_HUNTER` | `EntityWitchHunter` | `witchhunter` |
| 120 | `BINKY_HORSE` | `EntityDeathsHorse` | `deathhorse` (persistent tame mount, saddle and reusable skull) |
| 121 | `LORD_OF_TORMENT` | `EntityLordOfTorment` | `lordoftorment` |
| 122 | `SOULFIRE` | `EntitySoulfire` | `soulfire` |
| 123 | `IMP` | `EntityImp` | `imp` (rite, ownership, gift progression, power, and five signed spell contracts restored) |
| 124 | `DARK_MARK` | `EntityDarkMark` | `darkmark` |
| 125 | `MINDRAKE` | `EntityMindrake` | `mindrake` (Sprig mutation, three-second bulbs, owner-directed attacks and bursts) |
| 126 | `GOBLIN` | `EntityGoblin` | `goblin` |
| 127 | `GOBLIN_MOG` | `EntityGoblinMog` | `goblinmog` (ranged patron boss, `Invul` and `WitcheryTarget` saved) |
| 128 | `GOBLIN_GULG` | `EntityGoblinGulg` | `goblingulg` (melee patron boss, `Invul` and `WitcheryTarget` saved) |
| 129 | `BREW2` | `EntityBrew` | `brew2` → `witchery:thrown_brew` (saved `Brew` stack and `Spell` flight mode) |
| 130 | `ITEM_WAYSTONE` | `EntityItemWaystone` | `item` (legacy dropped Waystone, item subtype and age) |
| 131 | `DROPLET` | `EntityDroplet` | `droplet` |
| 132 | `SPLATTER` | `EntitySplatter` | `splatter` |
| 133 | `LEONARD` | `EntityLeonard` | `leonard` (cauldron summon, spells, shield and Urn loot) |
| 134 | `LOST_SOUL` | `EntityLostSoul` | `lostsoul` (three vulnerabilities, legacy color and expiry) |
| 135 | `WOLFMAN` | `EntityWolfman` | `wolfman` |
| 136 | `HELLHOUND` | `EntityHellhound` | `hellhound` |
| 137 | `WERE_VILLAGER` | `EntityVillagerWere` | `werevillager` |
| 138 | `VILLAGE_GUARD` | `EntityVillageGuard` | `villageguard` |
| 139 | `VAMPIRE_VILLAGER` | `EntityVampire` | `vampire` |
| 140 | `GRENADE` | `EntityGrenade` | `grenade` (Sun and Duplicate Grenades, saved `Mode`, `Impacted`, `Remaining`, original `BlockPlaced` coordinates and `ownerName`) |
| 141 | `LILITH` | `EntityLilith` | `lilith` |
| 142 | `FOLLOWER` | `EntityFollower` | `follower` (Elle, four named Fairest variants, and type-five duplicate with saved `Skin` and `TicksToLive`) |
| 143 | `WINGED_MONKEY` | `EntityWingedMonkey` | `wingedmonkey` (cocoa mutation, taming, Taglock pickup and Waystone transport) |
| 144 | `ATTACK_BAT` | `EntityAttackBat` | `attackbat` |
| 145 | `MIRROR_FACE` | `EntityMirrorFace` | `mirrorface` (inhabited Mirror conversation and saved lifetime) |
| 146 | `REFLECTION` | `EntityReflection` | `reflection` (copies armor, weapons, effects, disguise, and Wolfman form) |

- Legacy Demon bargain stacks and Wolfman trade stacks are upgraded before the chunk's entity data is loaded. The Demon keeps both buy slots and the original offer limits. A Wolfman with saved trades gains the minimum modern villager experience so the restored villager keeps its profession and offers without a job site. Legacy Imps use the tameable entity schema so their owner UUID, sitting state, and home remain available after import.
- An infected villager saved in human form uses the villager schema to upgrade its profession and trade stacks. Existing experience is preserved. A legacy novice with saved offers keeps its profession through its first job site check, the full-moon cycle, and a cure.
- A leashed Hobgoblin lets the player retrieve its tool or cargo. Any portion that cannot fit in the inventory drops at the Hobgoblin. If Forge rejects that spawn, the remainder stays in its hand for another interaction. Spawn eggs pass through to the ordinary entity interaction.

The modern registry also keeps `witchery:brew` and `witchery:brew2` as working projectile types for earlier 1.20.1 saves. Their fixed and custom payload decoding is shared with the 1.7.10 importer. The older port's two spawn egg item IDs remain readable. Protocol 89 covers the changed entity data.

## Potions / Effects (`WitcheryPotions`)
Source: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/brewing/potions/WitcheryPotions.java`

| Field | Legacy Key | Class |
|---|---|---|
| `PARALYSED` | `witchery:potion.paralysed` | `PotionParalysis` |
| `WRAPPED_IN_VINE` | `witchery:potion.wrappedinvine` | `PotionWrappedInVine` |
| `SPIKED` | `witchery:potion.spiked` | `PotionSpiked` |
| `SPROUTING` | `witchery:potion.sprouting` | `PotionSprouting` |
| `GROTESQUE` | `witchery:potion.grotesque` | `PotionGrotesque` |
| `LOVE` | `witchery:potion.love` | `PotionLove` |
| `SUN_ALLERGY` | `witchery:potion.allergysun` | `PotionSunAllergy` |
| `CHILLED` | `witchery:potion.chilled` | `PotionChilled` |
| `SNOW_TRAIL` | `witchery:potion.snowtrail` | `PotionSnowTrail` |
| `FLOATING` | `witchery:potion.floating` | `PotionFloating` |
| `NETHER_BOUND` | `witchery:potion.hellishaura` | `PotionHellishAura` |
| `BREWING_EXPERT` | `witchery:potion.brewingexpertise` | `PotionBrewingExpertise` |
| `DOUBLE_JUMP` | `witchery:potion.doublejump` | `PotionBase` |
| `FEATHER_FALL` | `witchery:potion.featherfall` | `PotionFeatherFall` |
| `DARKNESS_ALLERGY` | `witchery:potion.allergydark` | `PotionDarknessAllergy` |
| `REINCARNATE` | `witchery:potion.reincarnate` | `PotionReincarnate` |
| `INSANITY` | `witchery:potion.insane` | `PotionInsanity` |
| `KEEP_INVENTORY` | `witchery:potion.keepinventory` | `PotionKeepInventory` |
| `SINKING` | `witchery:potion.sinking` | `PotionSinking` |
| `OVERHEATING` | `witchery:potion.overheating` | `PotionOverheating` |
| `WAKING_NIGHTMARE` | `witchery:potion.wakingnightmare` | `PotionWakingNightmare` |
| `QUEASY` | `witchery:potion.queasy` | `PotionQueasy` |
| `SWIMMING` | `witchery:potion.swimming` | `PotionSwimming` |
| `RESIZING` | `witchery:potion.resizing` | `PotionResizing` |
| `COLORFUL` | `witchery:potion.colorful` | `PotionColorful` |
| `ENDER_INHIBITION` | `witchery:potion.enderinhibition` | `PotionEnderInhibition` |
| `ILL_FITTING` | `witchery:potion.illfitting` | `PotionIllFitting` |
| `VOLATILITY` | `witchery:potion.volatility` | `PotionVolatility` |
| `ENSLAVED` | `witchery:potion.enslaved` | `PotionEnslaved` |
| `MORTAL_COIL` | `witchery:potion.mortalcoil` | `PotionMortalCoil` |
| `ABSORB_MAGIC` | `witchery:potion.absorbmagic` | `PotionAbsorbMagic` |
| `POISON_WEAPONS` | `witchery:potion.poisonweapons` | `PotionPoisonWeapons` |
| `REFLECT_PROJECTILES` | `witchery:potion.reflectprojectiles` | `PotionReflectProjectiles` |
| `REFLECT_DAMAGE` | `witchery:potion.reflectdamage` | `PotionReflectDamage` |
| `ATTRACT_PROJECTILES` | `witchery:potion.attractprojectiles` | `PotionAttractProjectiles` |
| `REPELL_ATTACKER` | `witchery:potion.repellattacker` | `PotionRepellAttacker` |
| `STOUT_BELLY` | `witchery:potion.stoutbelly` | `PotionStoutBelly` |
| `FEEL_NO_PAIN` | `witchery:potion.feelnopain` | `PotionFeelNoPain` |
| `GAS_MASK` | `witchery:potion.gasmask` | `PotionGasMask` |
| `DISEASED` | `witchery:potion.diseased` | `PotionDiseased` |
| `FORTUNE` | `witchery:potion.fortune` | `PotionFortune` |
| `WORSHIP` | `witchery:potion.worship` | `PotionWorship` |
| `KEEP_EFFECTS` | `witchery:potion.keepeffects` | `PotionKeepEffectsOnDeath` |
| `WOLFSBANE` | `witchery:potion.wolfsbane` | `PotionBase` |

## Spell Symbol Effect Registry (`EffectRegistry`)
Source: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/infusion/infusions/symbols/EffectRegistry.java`

| ID | Key |
|---:|---|
| 1 | `witchery.pott.accio` |
| 2 | `witchery.pott.aguamenti` |
| 3 | `witchery.pott.alohomora` |
| 4 | `witchery.pott.avadakedavra` |
| 5 | `witchery.pott.caveinimicum` |
| 6 | `witchery.pott.colloportus` |
| 8 | `witchery.pott.confundus` |
| 9 | `witchery.pott.crucio` |
| 10 | `witchery.pott.defodio` |
| 12 | `witchery.pott.ennervate` |
| 13 | `witchery.pott.episkey` |
| 15 | `witchery.pott.expelliarmus` |
| 16 | `witchery.pott.flagrate` |
| 17 | `witchery.pott.flipendo` |
| 19 | `witchery.pott.impedimenta` |
| 20 | `witchery.pott.imperio` |
| 21 | `witchery.pott.incendio` |
| 22 | `witchery.pott.lumos` |
| 23 | `witchery.pott.meteolojinxrecanto` |
| 26 | `witchery.pott.nox` |
| 31 | `witchery.pott.protego` |
| 36 | `witchery.pott.stupefy` |
| 39 | `witchery.pott.ignianima` |
| 40 | `witchery.pott.carnosadiem` |
| 41 | `witchery.pott.morsmordre` |
| 42 | `witchery.pott.tormentum` |
| 43 | `witchery.pott.leonard1` |
| 44 | `witchery.pott.leonard2` |
| 45 | `witchery.pott.leonard3` |
| 46 | `witchery.pott.leonard4` |
| 47 | `witchery.pott.attraho` |

- IDs 2, 21, 22, 23, and 26 are active in `SymbolSpell`. The three projectile spells retain `EffectID` and `effectLevel` on saved `witchery:spelleffect` entities. Aguamenti and Incendio accept all original enhanced gestures. Nox and Meteolojinx Recanto take effect immediately when the gesture is released. Lumos uses the original `witchery:glowglobe` block ID and retains the earlier port's item ID for saved inventories.
- IDs 1, 3, 5, 6, and 10 restore Accio, Alohomora, Cave Inimicum, Colloportus, and Defodio. Door keys keep `doorX`, `doorY`, `doorZ`, `doorD`, and `doorDN`, with `doorDimension` for modern dimension IDs. Rings keep `doorKeys`. Protocol 70 includes the `witchery:door_keyring` crafting serializer.
- IDs 4, 9, 12, 13, 16, 20, 31, and 36 finish the legacy spell registry. All 31 IDs and their gesture sets match `EffectRegistry`. IDs 4 and 16 are hidden from Symbology. IDs 4, 9, and 20 require the Infernal Infusion in survival. Imperio keeps `WITCEnslaverName` and uses `WITCEnslaverUUID` for modern ownership.
- Tormentum uses the legacy boss immunity and starts `tormentumLastUse` when it affects an eligible target. Base Ennervate and Stupefy fall during flight, and their Worship-enhanced casts fly straight. Protocol 95 synchronizes the existing `effectLevel` saved field to tracking clients. Cave Inimicum keeps the original exception to Branch deflection.
- Projectile area effects, Accio retrieval, Attraho, and Dark Mark placement on collision use the projectile's position at the start of the collision tick. Terrain effects resolve the struck face. Attraho skips the Horned Huntsman and Ender Dragon. Ordinary spells use the legacy slime particle and sound cues, and curses use the legacy spell, flame, and dragon cues.
- Morsmordre uses the same position when its eight-tick countdown expires in open air. The projectile imports the original `direction` and `lifetime`, with a value of -1 keeping the spell untimed. Earlier 1.20.1 records derive the remaining countdown from `Age`. Protocol 96 sends the current falling acceleration to tracking clients. A Branch deflection starts a new decay sequence, and older port saves convert their launch acceleration using the saved age.
- Leonard, Lilith, the Reflection, and the Lord of Torment launch rank-one spells from the middle of their bodies with 0.1 acceleration. Their aim combines the original strength-based horizontal spread with a separate Gaussian offset on all three axes. Lilith's explosive fireball starts half a block above that height and uses only the horizontal part of her look vector for its forward offset.

## Ritual Registry (`RiteRegistry`)

The modern `witchery:item_rite` recipe type covers item-output rites at the Heart Glyph. Legacy rite 3, the Rite of Charging, is implemented as `witchery:rite_of_charging`. It validates the original 16- and 28-glyph white rings, consumes its five offerings in steps, and draws 2,000 power from the nearest altar.
All 96 IDs registered by Witchery 0.24.1 appear in the modern rite order and resolve to `witchery:item_rite` JSON in the packaged JAR. The progression and regression sections of `PORTING.md` record the runtime checks.
All 96 recipe definitions match their original chalk ring sizes, colors, and initial power cost. The seventy powered rites spend that charge in a separate offering step before the effect, in the order specified by each original registration. The Heart Glyph saves the payment under `AltarPowerOffered`. A cancellation or an item refund after payment leaves the spent power debited.
Rite IDs 10, 11, 14, 15, 20, 39, and 70 through 77 accept an optional Bound Waystone before the altar charge. The active Heart Glyph saves the consumed stack and its destination under `Sacrifices` and `WaystoneTarget`.
Rites 23, 25, 39, 80, and 81 sacrifice their creature before spending power. Rite 95 spends power first. The Heart Glyph saves each finished step under `LivingOffered` and `AltarPowerOffered`. Earlier rite 39 saves past the Waystone step already include the villager offering.
The original offering search uses a one-block-high glyph layer with an eight-block horizontal extent in each direction. The exact radius is measured in three dimensions from the Heart Glyph block's lower corner. Grasspers use a separate 11×11 square on that layer, scanned west to east and then north to south within each column. The activation check reserves sources, and each offering step then looks up the current item.
The Heart Glyph responds to rising redstone edges on neighbor updates and retains its powered block state across restarts. Its active state emits red dust near the glyph. Successful item offerings spawn the modern poof particles and play the modern pop that corresponds to the original `random.pop`. Living offerings pair the same pop with portal particles.
Rites 5 and 91 use a strict three-block sphere from the lower corner of the Heart Glyph. Location Waystones scatter X and Z by independent integer offsets from −3 to 3 and keep the recorded Y. Creature Waystones use the bound target's exact position. Both retain the original Ender Inhibition threshold and Dream and Mirror dimension restrictions. Rites 88 to 90 require a player whose hitbox overlaps the glyph layer within four blocks of its lower corner. The creature rites use the last eligible mob from the entity query when one is present, and otherwise use the same player selection as the statue.
Rite 6 puts the target of its Taglock at the glyph's lower corner even if the target intersects adjacent blocks. Forge checks the destination's current world bounds and retains native Taglock protections before attempting the transfer. The original Waystone and Taglock travel rites send portal particles and the Enderman sound at departure and arrival, or smoke and Witchery's poof for a vampire. They use the original half-block width, two-block height, and 16-block packet range.
Source: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/WitcheryRecipes.java`

| Rite ID | Book Entry | Unlocalized Name |
|---:|---:|---|
| 1 | 0 | `witchery.rite.bindcircle` |
| 2 | 1 | `witchery.rite.bindwaystone` |
| 3 | 3 | `witchery.rite.chargestone` |
| 4 | 4 | `witchery.rite.infusionrecharge` |
| 5 | 5 | `witchery.rite.teleporttowaystone` |
| 6 | 6 | `witchery.rite.teleportentity` |
| 7 | 7 | `witchery.rite.teleportironore` |
| 8 | 8 | `witchery.rite.protection` |
| 9 | 9 | `witchery.rite.imprisonment` |
| 10 | 10 | `witchery.rite.barrier` |
| 11 | 11 | `witchery.rite.barrierlarge` |
| 12 | 12 | `witchery.rite.barrierportable` |
| 13 | 13 | `witchery.rite.volcano` |
| 14 | 14 | `witchery.rite.storm` |
| 15 | 15 | `witchery.rite.stormlarge` |
| 16 | 16 | `witchery.rite.stormportable` |
| 17 | 17 | `witchery.rite.eclipse` |
| 18 | 18 | `witchery.rite.eclipseportable` |
| 19 | 19 | `witchery.rite.partearth` |
| 20 | 20 | `witchery.rite.raiseearth` |
| 21 | 23 | `witchery.rite.banishdemonportable` |
| 22 | 24 | `witchery.rite.banishdemon` |
| 23 | 25 | `witchery.rite.summondemon` |
| 24 | 26 | `witchery.rite.summondemonexpensive` |
| 25 | 27 | `witchery.rite.summonwither` |
| 26 | 28 | `witchery.rite.summonwitherexpensive` |
| 27 | 31 | `witchery.rite.infusionlight` |
| 28 | 32 | `witchery.rite.infusionearth` |
| 29 | 33 | `witchery.rite.infusionender` |
| 30 | 34 | `witchery.rite.infusionhell` |
| 31 | 35 | `witchery.rite.infusionsky` |
| 32 | 36 | `witchery.rite.necrostone` |
| 33 | 30 | `witchery.rite.summonfamiliar` |
| 34 | 2 | `witchery.rite.bindwaystonecopy` |
| 35 | 21 | `witchery.rite.fertility` |
| 36 | 22 | `witchery.rite.fertilityportable` |
| 37 | 37 | `witchery.rite.curseblight` |
| 38 | 38 | `witchery.rite.curseblindness` |
| 39 | 39 | `witchery.rite.hellonearth` |
| 40 | 29 | `witchery.rite.summonwitch` |
| 41 | 1 | `witchery.rite.bindwaystoneportable` |
| 42 | 2 | `witchery.rite.bindwaystonecopyportable` |
| 43 | 22 | `witchery.rite.naturespower` |
| 44 | 36 | `witchery.rite.priorincarnation` |
| 45 | 0 | `witchery.rite.bindcircleportable` |
| 46 | 20 | `witchery.rite.raiseearth` |
| 47 | 20 | `witchery.rite.raiseearth` |
| 48 | 48 | `witchery.rite.cursecreature1` |
| 49 | 49 | `witchery.rite.removecurse1` |
| 50 | 35 | `witchery.rite.infusiontree` |
| 51 | 20 | `witchery.rite.cookfood` |
| 52 | 48 | `witchery.rite.curseinsanity1` |
| 53 | 49 | `witchery.rite.removeinsanity1` |
| 54 | 1 | `witchery.rite.bindfamiliar` |
| 55 | 30 | `witchery.rite.callfamiliar` |
| 56 | 50 | `witchery.rite.corruptvoodooprotection` |
| 57 | 35 | `witchery.rite.infusionfuture` |
| 58 | 20 | `witchery.rite.cookfood` |
| 59 | 48 | `witchery.rite.cursesinking1` |
| 60 | 49 | `witchery.rite.removesinking1` |
| 61 | 35 | `witchery.rite.infusionseerstone` |
| 62 | 48 | `witchery.rite.curseoverheating` |
| 63 | 49 | `witchery.rite.cureoverheating` |
| 64 | 22 | `witchery.rite.climatechange` |
| 65 | 12 | `witchery.rite.iceshell` |
| 66 | 38 | `witchery.rite.rainoffrogs` |
| 67 | 4 | `witchery.rite.glyphictransform` |
| 68 | 7 | `witchery.rite.callbeasts` |
| 69 | 7 | `witchery.rite.manifest` |
| 70 | 22 | `witchery.rite.forestation` |
| 71 | 22 | `witchery.rite.forestation` |
| 72 | 22 | `witchery.rite.forestation` |
| 73 | 22 | `witchery.rite.forestation` |
| 74 | 22 | `witchery.rite.forestation` |
| 75 | 22 | `witchery.rite.forestation` |
| 76 | 22 | `witchery.rite.forestation` |
| 77 | 13 | `witchery.rite.volcano` |
| 78 | 48 | `witchery.rite.cursenightmare` |
| 79 | 49 | `witchery.rite.curenightmare` |
| 80 | 35 | `witchery.rite.infusebrewsoaring` |
| 81 | 35 | `witchery.rite.infusebrewgrave` |
| 82 | 36 | `witchery.rite.spectralstone` |
| 83 | 1 | `witchery.rite.bindspectral` |
| 84 | 1 | `witchery.rite.bindfetish` |
| 85 | 1 | `witchery.rite.bindfetish` |
| 86 | 1 | `witchery.rite.bindfetish` |
| 87 | 26 | `witchery.rite.summonimp` |
| 88 | 1 | `witchery.rite.bindwaystonetoplayer` |
| 89 | 1 | `witchery.rite.bindwaystonetoplayer` |
| 90 | 1 | `witchery.rite.bindstatuetoplayer` |
| 91 | 5 | `witchery.rite.teleporttowaystone` |
| 92 | 48 | `witchery.rite.wolfcurse.book` |
| 93 | 49 | `witchery.rite.wolfcure.book` |
| 94 | 49 | `witchery.rite.vampirecure.book` |
| 95 | 35 | `witchery.rite.infusionmirror` |
| 96 | 28 | `witchery.rite.summonreflection` |

- Original power lookup uses distance from the altar core. The base range is 16 blocks, or 32 with an Arthana mounted on a member of the altar. The same range applies to powered machines, Crystal Ball readings and scrying, rite activation, and any altar-powered upkeep.
- The Heart Glyph uses the registration IDs to match and queue recipes from distinct offering sources. It saves the pending recipe IDs in `QueuedRites` and tracks the order of independent upkeep in `ProtectionRites` and `UpkeepRites`. IDs 5 and 91 select the location-bound and creature-bound travel recipes independently.
- A loaded Heart Glyph advances its own clock through activation and successive casts. Coven summoning, item and creature offerings, optional Waystones, and altar payments run on 20-tick phases. Moving Earth begins on a 5-tick phase, Ore Transposition on 10, volcanoes on 15, Storm, Eclipse, Rain of Toads and Glyphic Transformation on 30, and Calling the Beasts on 60. The other 82 legacy effects begin on a 20-tick phase.
- Active legacy casts save the clock modulo 120 as `GlyphPhase`, which includes Hell on Earth's 40-tick boundary. Earlier modulo-60 records keep their saved 5-, 10-, 15-, 20-, 30-, and 60-tick phases. A Forge save with only `StepTicks` keeps the delay to its next pending step and synchronizes the glyph at that step. Custom recipe IDs retain their activation-relative timing.
- Relocated legacy effects check Waystone range on their first tick after the offerings and before the effect's periodic pulse. A rejected cast returns the held items and keeps any completed altar payment.
- Hell on Earth expands every five ticks and summons only after reaching full radius on the glyph's 40-tick boundary. `Landscape.HellSpawnOffset` records which expansion stage shares that boundary. An earlier Forge landscape with no offset keeps its previous stage-based spawn schedule. A 1.7.10 upkeep record waits for the original 20-tick activation gate and then resumes its saved stage.
- The original recipes include two daylight rites, nine nighttime rites, and five thunder-only directed curses. Hell on Earth also requires the Overworld. The Heart Glyph checks these conditions when it builds the queue. Pending casts keep that selection through weather changes and a save reload.
- For legacy rite eligibility, the End counts as daylight and the Nether, Mirror, and Torment count as nighttime. Overworld daylight follows ambient sky darkening. Torment uses the shared Overworld thunder strength. The Nether, End, and Mirror read their own thunder strength, which Forge initializes from the saved weather when the level loads.
- Witchery 0.24.1 saved upkeep rites after their offering phase, with the caster's name in the matching `Initiators` slot. Imported timed upkeep keeps that name as `InitiatorName` and resolves it only in the glyph's dimension. Active offering steps did not have a persistent record in the original save.
- The Circle Magic guide lists 88 rites in the original order. Rites 71–76 share the Forestation chapter with rite 70, and rites 85–86 share the Binding chapter with rite 84. Each chapter lists the valid first offerings from its live recipes. Network protocol 88 carries the rite ID when saving the book's `WitcheryRite` bookmark.
- Rites 34, 41, and 42 restore powered location copying, portable binding, and portable copying. Protocol 73 carries the copy action. `OfferingStep` tracks progress separately from `Sacrifices`, and `OfferingPos` stores each retained offering's origin. The Arthana, Boline, reusable Necromantic Stone, and discharged Attuned Stone drop as their steps finish. Rite 82 still consumes the Necromantic Stone.
- Rites 1 and 45 bind the existing `witchery:circletalisman` item. `CirclePattern` preserves the original three-bit field per ring size, with white, Otherwhere, and Infernal encoded as 1, 2, and 3. Protocol 74 carries the binding action. Right-click placement submits one batch to Forge and returns a blank only when the placement succeeds.
- Rites 8–12 restore `witchery:rite_of_protection`, `witchery:rite_of_imprisonment`, and the three `witchery:rite_of_the_barrier` variants. The Heart Glyph saves active wards in `ProtectionRites` with kind, target, coven size and elapsed upkeep ticks. Unbound wards follow the glyph. Bound wards use their saved coven range. The legacy stage array stores seconds for the portable variant and zero for powered wards. Imported wards wait for their first 20-tick pulse before charging, refreshing or expiring. Their countdown is saved as `ResumeTicks`. Protocol 72 carries the five recipe actions.
- Rites 14–16 restore the focused, greater and portable variants of `witchery:rite_of_the_skys_wrath`. The powered variants accept an optional location-bound Waystone. The Heart Glyph's `Storm` tag saves the target, variant and phase, and the fourth phase starts matching rain and thunder timers in the Overworld. Secondary dimensions resolve the local lightning through the same stages. Rites 17–18 restore the daytime powered and portable variants of `witchery:rite_of_total_eclipse`. A successful End cast leaves the shared clock unchanged because the End uses derived world data. Protocol 71 carries the four new recipe actions.
- Rite 19 uses `witchery:rite_of_broken_earth`. The offering position selects one of eight headings. The `Landscape` record stores `FissureSeed`, `FissureHeading`, and the completed stage. The first cut starts on the 20-tick phase and the remaining cuts run on consecutive loaded ticks. A Forge save without `FissurePerTick` keeps its pending next cut before joining that cadence. `FissureUsesLegacyRandom` pins a cast to the original Java random sequence across profiles. Older Forge records retain their environment's random factory. The older `RitualStages` array migrates with a northward path because 1.7.10 omitted the offering position and path.
- Rites 20, 46, and 47 are the small, medium, and large Rites of Moving Earth. Only the small ring accepts an optional location-bound Waystone. The glyph retains the target, coven size, stage and moved block entity data. Protocol 78 carries the four earth rite actions.
- Rites 21 and 22 restore portable and powered banishment. The strict nine-block sphere includes the original Demon, Death, Lord of Torment, Imp, and Reflection entity types. A banished creature disappears without death loot.
  - Banishment plays the glyph's original low-pitched fizz, then a silent explosion at each removed creature. The built-in summoning chamber covers one to three blocks above the glyph and omits the row three blocks south. A solid center or two other solid cells abort. The creature arrives one block above the glyph at one degree yaw, with a portal and fizz on success or large smoke and a snare when obstructed. A Forge spawn veto returns the held offerings and closes the cast.
- Rites 25 and 26 summon a vanilla Wither from the two Infernal rings. The powered rite follows the original order of sacrificing a villager and paying the altar before checking the chamber clearance. Rites 33 and 40 summon `witchery:familiar` and `minecraft:witch`, respectively. The familiar imports `OwnerUUID`, `ItemToFind`, and `Searches`, with modern ore tags covering stone and deepslate. Protocol 79 carries the four action types for the six recipes.
- Rites 35 and 36 use `witchery:rite_of_fertility` and `witchery:rite_of_fertility_portable`. Their shared `FERTILITY` upkeep saves the expanding stage and imports the corresponding entry from `Initiators` as `InitiatorName`. New casts keep the existing UUID `Initiator`. Vanilla cure preserves modern villager data and invokes Forge's conversion events.
- Rites 37 and 38 use `witchery:curse_of_blight` and `witchery:curse_of_blindness`. Their wave shares Fertility's midpoint-circle surface scan and spherical entity range. It expands to 80 blocks plus two per coven member. Forge conversion events protect villagers and cows during Blight, and conversion to a Zombie Villager preserves the data needed for curing.
- Fertility sends Spell, and Blight, Blindness and Hell on Earth send Mob Spell. Their silent packets use width and height one at the bottom center of the air block above the selected surface. The original delivery radius is 16 for surfaces at or above the wave center and 32 for lower surfaces. Repeated cardinal columns and the upper-first search order are retained.
- Imported Fertility, Blight, Blindness, Hell on Earth and Ice Shell waves wait 20 loaded ticks before their first resumed expansion, then expand every five ticks. A Forge save made during that wait preserves its remaining ticks and the first expansion's activation cue.
  - Fresh casts and newly activated legacy waves play the original fizz at the lower corner of the wave origin with volume 0.5 and pitch between one third and one half. A modern active save keeps its scheduled pulse and activation state.
- Rite 39 uses `witchery:rite_of_hell_on_earth`. The source altar in range of the Heart Glyph pays 5,000. The target altar in range of the destination pays 200 after the wave expands to 20 blocks plus two per coven member. The map-editing path respects Forge protections and the `allowHellOnEarthFires` setting enables the cat familiar's fires by default.
  - The arrival packet plays Large Explode and the Blaze death sound at the destination center, two blocks above its Y. Width is one, height is two, and delivery reaches 16 blocks. It accompanies a paid spawn attempt even if Forge vetoes the entity. An unpaid attempt ends the wave.
- Rite 43 uses `witchery:rite_of_natures_power`. The `witchery:natures_power_replaceable` datapack block tag includes stone, sand and gravel. The restored `naturesPowerReplaceableBlocks` server setting defaults to mycelium. Block IDs select all states, explicit state strings select a variant, and old numeric metadata selects its flattened state. The portable recipe requires a Rowan, Alder and Hawthorn Sapling individually, along with oak, spruce and birch.
- Rite 44 uses `witchery:rite_of_prior_incarnation`. Player data retains `WITCPriIncInv` and the three `WITCPriIncLoc` coordinate fields. Drop stacks accept the original `WITCPriIncUsr` owner name. New stacks also carry `WITCPriIncOwner` and `WITCPriIncDeath` to keep player identity and track the originating death. Protocol 84 carries the recipe action.
  - Player import also converts the vanilla items inside `WITCPriIncInv` using the original Forge registry and Minecraft's item data fixes. An earlier 1.20.1 import repairs numeric recovery stacks at the next login when its saved legacy registry is present.
- Rite 68 uses `witchery:rite_of_calling_the_beasts`. The legacy raw Fish offering with metadata zero is Cod. The target pool includes domestic cats and ocelots to cover both forms of the old ocelot entity. Each pulse can call two eligible animals to integer X and Z within two blocks of the glyph and at its exact Y, including positions inside blocks. Forge checks world bounds. Each departure and arrival sends the original Portal and Enderman cues with width 0.5, height two, and a 16-block reach. The `NATURES_POWER` and `CALL_BEASTS` upkeep read unsigned values from the old `RitualStages` array. Protocol 80 carries these two actions and `FERTILITY`.
- Rite pairs 48–49, 52–53, 59–60, 62–63 and 78–79 use `curse_of_<curse>` and `cure_of_<curse>` recipes for Misfortune, Insanity, Sinking, Overheating and Waking Nightmare, respectively. Targets keep the original `witcheryCursed`, `witcheryInsanity`, `witcherySinking`, `witcheryOverheating` and `witcheryWakingNightmare` keys in entity Forge data or the player's `PlayerPersisted` compound. Protocol 81 sends the eight additional recipe actions and adds Sinking's strength to the infusion status packet.
- Rites 6 and 7 are `witchery:rite_of_transposition_creature` and `witchery:rite_of_transposition_ores`. They use medium and large Otherwhere circles, respectively. The protocol 55 item-rite actions retain the required sacrifices and altar power. An active ore rite migrates legacy `Rituals`, `RitualStages` and `RitualCovens` and saves the completed layer as `OreDepth`.
- Rite 90 is `witchery:rite_of_binding_a_worship_statue`. It uses the small white circle and 4,000 altar power. The bound item keeps `WITCBoundPlayer` and adds the player UUID. The placed `witchery:statueofworship` block entity accepts the original `Owner` string and carries it back onto drops. Protocol 67 adds the recipe action and the Hobgoblin worship pose.
- The Scarecrow, Trent Effigy and Witch's Ladder retain their block entity IDs. The legacy `EffectTypeID` and item `WITCSpiritEffect` identify the active spirit enhancement. Effect 1 applies to carried Voodoo Protection against a forced summon and Death Protection against powered bolts within 16 blocks. Effects 2 through 5 drive Sentinel, Screamer, Twister and Ghost Walker using the alarm's Taglock list. Effect 6 is the hidden Death encounter, checked last. It consumes five of each spectral undead species and creates no bound fetish.
- The Sentinel searches one block northwest of each intruder, looking up through an obstruction and down through air. It requires two open blocks above the chosen floor. A lone intruder triggers two temporary Spectres at the same point, each with the original hitbox, Instant Spell particles, and Spectre sound. The alarm becomes eligible once more than 600 ticks have elapsed since the saved activation.
- Rite 82 uses `witchery:rite_of_necromancy_spectral_stone`. It consumes the Necromantic Stone and produces one blank `witchery:spectralstone`. Protocol 56 carries the explicit `consume_necromantic_stone` field and the Spirit's synchronized color.
- Rite 67 uses `witchery:rite_of_glyphic_transformation`. It consumes Gypsum, an Arthana, and 1,000 altar power. The first color of fresh chalk among nearby drops selects the target color. The number of sticks of that color selects the ring size and one chalk is consumed. Protocol 62 carries the new action.
- Rite 64 uses `witchery:rite_of_climate_change`, the large white circle, and seven offerings without an altar charge. At least four coven witches are required. Four, five, or six witches give a radius of 16, 32, or 48 blocks. The fifth pulse consumes nearby stacks of recognized foci and Glowstone Dust. The last recognized focus selects the family. Dust selects the one-indexed biome variant and clamps to the available choices.
- The climate foci are Oak Sapling, Short Grass, Obsidian, Stone, Slimeball, Water Bucket, Cactus, Icy Needle, Jungle Sapling, Netherrack, Sand, Red Mushroom, and Skeleton Skull. Vanilla variants and current biome tags substitute for the old Forge BiomeDictionary. The magical family falls back to Dark Forest if the tag is empty. Cactus removes surface snow in range and clears rain and thunder. Icy Needle starts precipitation when the weather is clear.
- Climate Change rewrites saved four-block-wide biome columns across all heights and sends Minecraft's biome update to clients tracking the affected chunks. The Heart Glyph imports legacy rite 64 upkeep and keeps the glyph as the center. Protocol 65 adds the action. The server's `allowBiomeModificationRituals` setting defaults to true and can disable the rite.
- Rite 65 uses `witchery:rite_of_ice_shell`. It checks for two coven members and grows a Perpetual Ice shell to radius 8, 12, or 16 according to coven size. The Heart Glyph accepts its legacy upkeep stage. Protocol 64 adds the action. Frozen Heart keeps legacy ingredient subtype 79.
- Rite 66 uses `witchery:rite_of_rain_of_toads`. The Heart Glyph saves the foreground prelude in `ToadRainStage` and the stage-five-to-199 upkeep in `Landscape`. The rite sends four lightning bolts before the first shower and starts rain when cast in a clear Overworld. Temporary toads expire after 30 ticks and poison nearby creatures. The original `SuicideIn` and `Poisonous` fields persist. Protocol 83 carries the four new recipe actions.
- Rites 70–76 use `witchery:forestation/<species>`. They accept oak, spruce, birch, jungle, rowan, alder, and hawthorn in legacy order. Rite 77 uses `witchery:rite_of_raising_a_volcano`. It scans to modern bedrock or the build floor and accepts a lava reservoir that meets the original neighboring-source test.
- Forestation and the powered volcano use the optional legacy location-bound Waystone. `Landscape` saves the current stage, target, and forest species when present. The Heart Glyph imports a supported legacy upkeep entry using `Rituals`, `RitualStages`, `RitualCovens`, and `Locations`. Protocol 63 added the two actions.
- Rite 13 uses `witchery:rite_of_raising_a_volcano_portable` and saves `RAISE_PORTABLE_VOLCANO` as the action at the glyph's own location. Rites 51 and 58 use `witchery:rite_of_cooking` and `witchery:rite_of_cooking_portable`. They use the active `minecraft:smelting` recipes and turn individual burn rolls into Charcoal. Protocol 82 carries the three new actions.
- Cooking selects ground items around the center of the glyph and launches the cooked item and Charcoal from the lower corner at Y plus 0.05, with 0.2 upward velocity and immediate pickup. The original packet sends Flame and the Ghast fireball sound from that corner, with width three, height two, and 16-block reach. Original 0.24.1 saved upkeep rites only. The Forge port can also resume an in-progress Cooking cast from its saved offerings.
- The two Wicker Bundle variants keep `bundle_type` and `axis=x|y|z`. The legacy chunk importer reads the type bit and direction bits separately. Flint and Steel resolves the figure along one horizontal axis, burns the original pattern, and summons a Huntsman at one quarter health. The Wicker entrance has no explosion. The Horn of the Hunt enables its own entrance explosion.
- Rite 96 uses `witchery:rite_of_summoning_a_reflection`, the large Infernal circle, and 5,000 altar power. It accepts the original Mirror or the earlier modern `witchery:mirrorblock` item. The spawned Reflection saves `FreeSpawn` and can live outside the Mirror. Protocol 61 adds the rite action and the Reflection's synchronized skin and Wolfman state.

## Prediction Registry (`PredictionManager`)
Source: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/WitcheryRecipes.java`

| ID | Class | Weight | Key |
|---:|---|---:|---|
| 1 | `PredictionFight` | 13 | `witchery.prediction.zombie` |
| 2 | `PredictionArrow` | 13 | `witchery.prediction.arrowhit` |
| 3 | `PredictionFight` | 3 | `witchery.prediction.ent` |
| 4 | `PredictionFall` | 13 | `witchery.prediction.fall` |
| 5 | `PredictionMultiMine` | 8 | `witchery.prediction.iron` |
| 6 | `PredictionMultiMine` | 3 | `witchery.prediction.diamond` |
| 7 | `PredictionMultiMine` | 3 | `witchery.prediction.emerald` |
| 8 | `PredictionBuriedTreasure` | 2 | `witchery.prediction.treasure` |
| 9 | `PredictionFallInLove` | 2 | `witchery.prediction.love` |
| 10 | `PredictionFight` | 2 | `witchery.prediction.bababad` |
| 11 | `PredictionFight` | 2 | `witchery.prediction.babagood` |
| 12 | `PredictionFight` | 3 | `witchery.prediction.friend` |
| 13 | `PredictionRescue` | 13 | `witchery.prediction.rescued` |
| 14 | `PredictionRescue` | 13 | `witchery.prediction.rescued` |
| 15 | `PredictionWet` | 13 | `witchery.prediction.wet` |
| 16 | `PredictionNetherTrip` | 3 | `witchery.prediction.tothenether` |
| 17 | `PredictionMultiMine` | 13 | `witchery.prediction.coal` |

`Predictions` registers all 17 IDs with their legacy weights and assigns a new forecast only when the active list is empty. `WITCPredict.WITCPreList` entries retain `WITCPreID` and `WITCPreTime`. `CrystalBallBlockEntity` retains `LastUsedTime` and the training rite grants `WITCFTeller`. The two Baba Yaga readings use the original `witchery:babayaga` entity and legacy `Owner` name. New allied visits also retain the UUID and visit age.

Prediction ticks and Crystal Ball readings remove IDs outside the registry. If a restored list includes several registered IDs, the ball reports each once and preserves their saved times and other fields. A new reading is available when the restored list contains only obsolete IDs.

Fall and Wet can force their original three-by-three gravel roof over dirt or farmland. Airborne players use the original block below their feet. Grounded players use the supporting block's height, including the shorter modern farmland surface. Fall clears five layers, and Wet places two layers of water sources outside ultra-warm dimensions. The original minimum heights are relative to the dimension floor. The roof honors Forge group protection, and individual protected blocks below it remain in place.

Mining predictions extend Forge block loot after the harvest. Deepslate and its matching ores extend the legacy stone rules. Buried Treasure places the chest one block below the harvest if all four sides and the bottom are covered. The center can be air or water. Forge protection and protected terrain can prevent placement and leave the forecast active.

Buried Treasure fills the chest immediately with `witchery:chests/mineshaft_corridor`. The table restores the `mineshaftCorridor` hook's three to six rolls, weighted items, count ranges, and level-30 book roll. Slot selection can overwrite an earlier roll, as it did in 1.7.10. The enchantment roll uses the current vanilla enchantment registry. Datapacks can replace the table. Previously placed chests keep their contents or their saved loot table. [Forge 1.7.10 ChestGenHooks](https://raw.githubusercontent.com/MinecraftForge/MinecraftForge/1.7.10/src/main/java/net/minecraftforge/common/ChestGenHooks.java)

## Creature Power Registry
Source: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/WitcheryRecipes.java`

| ID | Power Class |
|---:|---|
| 1 | `CreaturePowerSpider` |
| 2 | `CreaturePowerSpider` |
| 3 | `CreaturePowerCreeper` |
| 4 | `CreaturePowerBat` |
| 5 | `CreaturePowerSquid` |
| 6 | `CreaturePowerGhast` |
| 7 | `CreaturePowerBlaze` |
| 8 | `CreaturePowerPigMan` |
| 9 | `CreaturePowerZombie` |
| 10 | `CreaturePowerSkeleton` |
| 11 | `CreaturePowerJump` |
| 12 | `CreaturePowerJump` |
| 13 | `CreaturePowerSpeed` |
| 14 | `CreaturePowerSpeed` |
| 15 | `CreaturePowerSpeed` |
| 16 | `CreaturePowerSpeed` |
| 17 | `CreaturePowerEnderman` |
| 18 | `CreaturePowerHeal` |
| 19 | `CreaturePowerHeal` |
| 20 | `CreaturePowerHeal` |
| 21 | `CreaturePowerHeal` |
| 22 | `CreaturePowerHeal` |
| 23 | `CreaturePowerHeal` |
| 24 | `CreaturePowerBat` |
| 25 | `CreaturePowerJump` |

- Landing with Magma Cube, Slime or Toad power clears fall damage and sends `sound` at the landing position. It reaches the caster and players strictly within eight blocks in the same dimension. Each receiving client plays the mapped large-slime cue at half volume and picks a pitch between one-third and one-half.
- Infernal casting feedback reaches the caster. Ordinary fizz and rejection cues use the original half volume and pitch range. Squid blindness uses its own full-volume fizz. Spider and Skeleton attacks use the original bow sound, the latter with draw-strength pitch scaling.

## Infusion Registry (`Infusion.Registry`)
Source: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/WitcheryRecipes.java`

| ID | Class |
|---:|---|
| 1 | `InfusionLight` |
| 2 | `InfusionOverworld` |
| 3 | `InfusionOtherwhere` |
| 4 | `InfusionInfernal` |

All four infusion recipes, their personal charge and the HUD are implemented. The Witch's Hand uses `witchery:witchhand` and restores Otherwhere movement, Light's cloak and barriers, Overworld's terrain and metal powers, and Infernal minion control and creature powers. Its recall point survives serialization in `WITCRecallD`, `WITCRecallX`, `WITCRecallY` and `WITCRecallZ`. Its right-click cooldown keeps the `WITCCooldown` field. The `witchery:barrier` block retains `remainingTicks`, `blocksPlayers` and `owner` in its block entity, with a UUID for new placements. Infernal minions read `WITCEnslaverName` and save the owner UUID for new bindings. Creature powers retain `WITCBeastPower` and `WITCBeastPowerCharges`. Their projectiles use the migrated Rock and Thrown Web ingredients.

The four original infusion rites and Infusion Recharging check a four-block sphere from the lower corner of the Heart Glyph and require the player's hitbox to overlap its one-block-high layer. Manifestation has a five-block sphere and grants 150 seconds plus 25 per coven member. Recharging gives at most ten charge per player for 40 altar power per pulse. Its cached altar is chosen again when invalid or on the one-in-five refresh. A save retains the loaded ticks until the next pulse and finds a fresh altar when the world opens. The existing legacy particle packet sends the infusion explosion, Manifestation's fizz, and the recharge pling to nearby players.

The Infernal Hand and Imperio reject Baba Yaga, Lilith, Death, the Horned Huntsman, Mog, Gulg, Leonard, the Lord of Torment, and the Reflection. The shared boss check also reads Forge's boss tag. Golems, Demons, ordinary witches, Imps, Ents and Elder Guardians retain their separate binding exclusions.

`witchery:poppetshelf` reads legacy `Items` and its nine `Slot` indices. It shares taglock matching and `WITCDamage` with handheld wards. A Forge block ticket replaces the old `poppetX`, `poppetY` and `poppetZ` ticket fields and keeps the owning block position in the dimension's `chunks.dat`.

On an explicit legacy import, Witchery reads its `TicketList` records from each original `forcedchunks.dat` and restores a ticking ticket for every surviving Poppet Shelf. It also loads shelves outside spawn so their stored wards can become active before a player visits.

The Vampiric Poppet keeps both legacy Taglock indices. The first tracks the protected player, and the second tracks the damage recipient. Player bindings read `WITCPlayer1` and `WITCPlayer2` with optional `WITCPlayerUUID1` and `WITCPlayerUUID2`. Creature bindings retain the `WITCEntityIDm`, `WITCEntityIDl` and `WITCDisplayName` fields for each index.

`witchery:grassper` reads its single legacy `Items` entry at `Slot:0b`. The rite heart retains legacy `Sacrifices` item entries and writes `GrassperPos` for offerings it takes from a Grassper. Critter Snare item damage 1 through 4 becomes the `critter` block state. The older bat subtype 9 also sets `stocked`, which restores `WitcheryShopStock` when that bat is released.

Both charm IDs use the same 50-durability item for animal shops. Stock is stored on the mob at `ForgeData.WitcheryShopStock.Recipes`. The importer normalizes numeric Witchery and vanilla item stacks in the original `buy`, `buyB` and `sell` fields and preserves `uses` and `maxUses`. An existing empty compound marks a spent shop. Modern horse variants use the original Horse pool, and Cats use the Ocelot pool. Mooshrooms use the Cow pool's wheat currency, matching the original branch order.

One customer can access an animal's stock at a time. The modern menu closes if the creature disappears or the customer moves more than eight blocks away. Its shift-click path uses the animal's own voice because the menu wraps an ordinary mob. Demon bargains retain their separate merchant and Devil's Tongue discount.

The Owl reads `FeatherColor` and the Toad reads `SkinColor`. Both retain `Familiar` and `SuicideIn` and use vanilla tamed ownership. The Toad also reads `Poisonous`. The Owl keeps its carried item in its main hand equipment.

The Broom uses `BrushColor` on the vehicle and enchanted item. The vehicle reads legacy plain-text `CustomName` and saves the modern component name. An older port's `witchery:broom_spawn_egg` can place the vehicle. The Infused Brew of Soaring uses `WITCInfusedBrewType:1` and `WITCInfusedBrewStart` in the player record.

Bound Waystones retain `PosX`, `PosY`, `PosZ`, `PosD` and `NameD`. New bindings add the namespaced `Dimension` field. Old Waystones resolve the three vanilla dimensions and Witchery's default legacy IDs `-37` for Dream, `-38` for Torment and `-39` for Mirror. Creature-Bound Waystones share the first Taglock signature fields.
When the world retains its captured legacy registry, loading player, block-entity, and entity storage also repairs unresolved numeric Waystone and Door Key bindings. The same rule applies to each saved keyring entry. An explicit modern dimension takes precedence over the captured mapping.

Owls retain `WITCWaypoint` and their launch coordinates in `HomeX`, `HomeY` and `HomeZ` while delivering. `ReturningFromDelivery` preserves the return leg on modern saves. An Owl throws fixed Witchery brews, splash cauldron brews, and splash or lingering vanilla potions at its destination with the bow cue. Drinkable cauldron brews arrive as intact parcels.

A Creature-Bound Waystone can assign an Owl to an unavailable recipient. If the recipient cannot be resolved during flight, the Owl uses its launch coordinates for the delivery. A dropped Mindrake Bulb lasts 60 ticks. A dropped Mandrake Seed uses the ordinary item lifetime.

The Winged Monkey retains `FeatherColor`, `SuicideIn`, `WITCWaypoint` and the three `Home` coordinates. `MonkeyFlightReturning` identifies the return leg in new saves. An older active Waystone flight stores its destination in the `Home` coordinates. The modern goal uses Forge's flying navigation and only loads routes within the monkey's dimension. The creature identified by a Taglock rides as a passenger without taking control of the monkey.

With Does It Tick installed, running Owl deliveries and Winged Monkey flights can tick beyond the configured player range while their flight tickets keep the relevant chunks loaded. Seated and idle companions and those with AI disabled follow the mod's usual rules. The compatibility hook is optional and uses the same flight state that the companion goals read from saves.

The same optional hook lets a loaded Hellhound finish its golden apple cure beyond player range. It reads the cure flag restored from `ConversionTime`. Untreated Hellhounds use the pack's ordinary tick rules. The cure adds no chunk ticket.

Sun and Duplicate Grenades also finish their flight and fuse while their chunks are loaded beyond player range. A temporary copy keeps ticking until its saved lifetime expires.

Treefyd keeps `Owner`, `SentinalPlant`, `KnownPlayers`, `KnownCreatureTypes`, and `KnownCreatures`. New bindings also save player UUIDs and the namespaced keys of groupable animal species. Legacy individual permissions use the original `CreatureMost` and `CreatureLeast` fields. Planting creates short grass and an owned Treefyd in one interaction. A Forge cancellation keeps the original block and seed.

## Recipe System Registries

The modern datapack contains the altar's shaped recipe. Rowan logs match untagged stacks and stacks with explicit rowan state. Other Witchery wood variants and non-water potions are excluded.
Source: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/WitcheryRecipes.java`

The `witchery:wood_ash` smelting recipe uses `minecraft:saplings` when `addSmeltingForAllSaplingsToWoodAsh` is true in `config/witchery-common.toml`. The false branch accepts the original six vanilla species and Rowan. Both the vanilla furnace and Witch's Oven load the selected recipe through Forge. The oven saves pending experience under `RecipesUsed` and pays it out when a player takes the main result.

The Witch's Oven keeps `CookTime` when its input changes between ticks. Its completion uses the current recipe and input, and an uncookable or unpowered tick resets progress. Each completed batch rolls its fume chance, including batches with an empty jar slot. Same-item fumes retain existing stack data and stop accumulating at 63. The oven still produces the main smelting result and keeps the jar when a fume cannot enter the output.

- `RecipeSorter.register`: 7 custom recipes
- `KettleRecipes.instance().addRecipe`: 39. The port has 39 Kettle recipe IDs. The original Spirit of Otherwhere and Infernal Animus IDs accept their regular potions and appear in Brews & Infusions. The two separate splash-potion IDs follow the original hidden-page rule and brew the same outputs.
- The original Kettle used the first registered recipe that matched the current ingredients and dimension. The 39 built-in JSON recipes carry priorities in that order. When recipes overlap, higher `priority` wins, with recipe ID breaking ties. A datapack recipe that omits `priority` uses zero. The partial mixture takes its tint and power requirement from that choice. The Bramble recipe for doubling Infused Brew Base carries the same one-serving bottling bonus the original Kettle looked up by output item.
- Each Kettle evaluates heat, tint, and recipe power every 20 loaded ticks. A full recipe waits for the nearest altar to pay its cost once. Saves keep the ingredients, finished recipe, and liquid state. The check phase starts fresh when the tile loads.
- Hand-held Water Buckets fill the remaining space in a Kettle or a Witch's Cauldron containing plain water. Survival returns the empty Bucket whenever any water transfers. Using a Water Bucket on a full Kettle rinses its batch while keeping the water, spare bottles, and held Water Bucket. Plain water leaves existing cauldron brews intact.
- A plain Water Bottle contributes up to 1,000 mB to either vessel and is consumed in survival. A Glass Bottle draws exactly 1,000 mB of plain water from either vessel. In a Kettle, a ready brew takes priority. Rinsing a full Kettle with a Water Bottle keeps the held bottle. Emptying a spoiled Kettle clears its batch and preserves its stored bottles.
- Forge fluid transfers update the Kettle's water volume while retaining its batch and bottle inventory. A partly drained ready brew can be bottled by hand. Automated bottling requires a full tank and a glass bottle for every remaining serving. Taking the last serving empties the tank.
- A dropped glass bottle stack enters the Kettle only when the whole stack fits. The buffer keeps the stored stack's item data. A full Kettle accepts dropped ingredients into the first free slot even if a result is ready or the mixture is spoiled. An extra dropped ingredient spoils the batch and leaves the result inaccessible in its saved slot. Rinsing clears the spoiled result. Hoppers merge matching ingredient stacks in a full Kettle while its result slot is empty.
- Accepted ingredient drops send the original splash cue within five blocks of the basin, including the drop that spoils the batch. The existing client packet handler produces twenty splash particles and plays the water sound. Glass Bottle drops and hopper insertion skip this cue.
- Built-in Kettle potion ingredients check the base potion and its drinkable or splash item ID. This preserves the original potion metadata requirements for strength and duration and accepts custom names or other independent item tags.
- `DistilleryRecipes.instance().addRecipe`: 9
- `SpinningRecipes.instance().addRecipe`: 4
- An idle Distillery or Spinning Wheel displays a connection to a formed altar even at zero charge. A runnable batch updates the indicator from its 0.6-power debit and keeps its elapsed ticks during a power interruption. A full output resets elapsed ticks. The Distillery also blocks incompatible output stacks to prevent losing part of the batch. The Spinning Wheel preserves the original `PowerLevel` short alongside `CookTime` and `Items`.
- The Distillery, Spinning Wheel, and Brazier check their idle altar connection every 40 loaded ticks. An active batch reuses a newly selected altar for one more tick unless it becomes invalid or the tile reaches a 100-tick refresh. A closer empty altar pauses progress until it can pay. The source coordinates and tick phase are transient. The Brazier retains the saved `PowerLevel` even while unlit.
- Both machines use the currently valid recipe throughout the batch. Swapping an ingredient directly to another runnable recipe keeps `CookTime` and the new recipe supplies its outputs and ingredient cost. Earlier port saves may include a `Recipe` string. The machines read the saved progress and inventory and resolve the recipe from those items.
- The Brazier uses the original nearby particle packet for fizzle, spectral effects, Graveyard Mist, and Wilting. Mist submits up to 300 poofs in a 32-block-wide, four-block-tall volume per powered tick to viewers within 64 blocks. Wilting uses the common undead rule, including a player under the Infused Brew of the Grave.

### Custom RecipeSorter Keys
- `witchery:bindpoppet`
- `witchery:addpotion`
- `witchery:repair`
- `witchery:addcolor`
- `witchery:addkeys`
- `witchery:attachtaglock`
- `witchery:biomecopy`

## Brew Registry
Source: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/brewing/WitcheryBrewRegistry.java`

- Forge 1.7.10 reports 184 registered brew keys across 88 base items. Newly mixed brews use the captured base item translation keys, including one shared hash across each legacy metadata family. Modern Dead Bush also represents the original Tall Grass metadata-zero variant and uses the original Dead Bush hash. An explicit saved color survives import with all 32 bits, and rendering takes its low 24 bits.
- `witchery:brewbottle` uses an ordered `Items` list for drinkable and instant splash effects. Ingredient stacks inside `Items` support the named legacy `witchery:ingredient` plus `Damage` format. The `Splash` flag can be derived from known dispersal ingredients when absent. Native numeric 1.7.10 item IDs still require the save conversion workflow.
- Bottles use the ordered list for their localized display. They show the dispersal label with the earlier extent and linger modifiers, the original displayed strength and duration, and the quaffing tier. Splash tooltips halve the displayed durations. Earlier bottles keep a positive saved `BrewDrinkSpeed`, and bottles without that field use the recipe's time.
- An accepted stack of dropped legacy ingredients is consumed as one brew action. The ordered brew keeps the original count and item tags, and a Wool stack changes only the color. Fixed recipes keep the complete trigger stack when the cast is saved or moved. Custom datapack recipes retain their one-item consumption per offering.
- Legacy `minecraft:speckled_melon` loads as `minecraft:glistering_melon_slice` in saved mixtures. It retains the original Healing effect or Harming when inverted, with the saved ingredient order and brew color.
- `witchery:brewbag` preserves `WITCColor` and eight numbered slots in `Inventory.Items`. Sneak-use opens `witchery:brew_bag`. Normal use shows the eight-direction selector. The server derives selection from player rotation and throws the matching brew on release. The menu and dye recipe use protocol 75.
- `PotionBottling` retains the original player-data key. Successful player bottling can grant skill according to the batch's used capacity and charges the closest altar once per portion. The Seer Stone reports the skill.
- Brew of the Depths uses the original `ForgeData.PlayerPersisted.witcheryDepths` countdown in seconds. Each pulse grants missing Water Breathing, applies Wither II while dry or removes Wither while submerged. The last pulse removes Water Breathing and Poison. Death clears the countdown. A saved `witchery:depths` effect from an earlier 1.20.1 build converts its remaining ticks to seconds when the player joins.
- The kettle's Brew of Grotesque subtype 71 uses the original `ForgeData.PlayerPersisted.witcheryGrotesque` countdown of 1,200 pulses. It repels creatures within four blocks every fourth world tick and spares ordinary witches, golems, Demons and bosses. The saved aura and mask remain active when the player drinks milk. Earlier port saves convert the fixed brew's hidden, finite level-I effect to the original countdown.
- Infernal Blood subtype 80 adds Fear as the curable `witchery:potion.grotesque` effect. Its four-block pulse runs every fifth world tick and can repel ordinary witches. The mask renders on affected creatures using the original Demon texture. Player mask updates for the fixed brew use the existing player style packet.
- An empty glass bottle in a dispenser can draw a full boiling Witch's Cauldron from any of the five sides other than the outlet. The dispenser uses the saved result of the Cauldron's last power sample and leaves altar charge intact. Fixed throwable brews, splash-format brews, and Sun and Duplicate Grenades leave the outlet ownerless at 1.375 blocks per tick with the original Gaussian spread. Grenades thrown by hand start beside the player's eyes at 0.75 blocks per tick, independent of player movement.
- Bat Wool and Wormwood select gas or liquid dispersal. Both original block entity IDs resolve to brew fluid cells and can read legacy effect, tint, duration, expansion and thrower data. Current saves also retain their age.
- A cauldron's expanding gas wave looks for open footing up to seven blocks above or below each column. A qualifying column applies creature effects and the one-percent terrain roll at the original target height. The silent Spell packet appears one block above that height and reaches viewers within 16 blocks.
- Terrain brews and initial dispersal cells submit captured writes to Forge placement listeners. The snapshot carries the previous block and its data. A veto restores it before Witchery sends placement updates or initializes a cloud.
- Gunpowder, Globe Artichoke, Bat Wool, Wormwood and Zombie Head remove the earlier dispersal and all earlier Wheat actions when added. This frees the Wheat action's one capacity and removes its Moonshine effect. Later Wheat remains eligible as an ordinary brew ingredient.
- Zombie Head selects triggered dispersal. The original stone and oak buttons, lever, oak, stone and snow plates, and oak door have cursed counterparts with the original block entity IDs. Cursed controls persist the ordered mixture, tint, charge count, duration, expansion and thrower. Named `minecraft:skull` metadata 2 decodes to the Zombie Head ingredient.
- Triggered door conversion submits both replacement halves to Forge together. The curse payload is written after approval. Chalk and Flagrate resolve glyph placement before drawing cues or chalk wear.
- Mystic Branch terrain spells, Fiery Touch, and landscape rites stage placements for Forge protection listeners. Vetoes restore the previous state. Landscape removals still use the break check, and Fiery Touch plays the ignition cue after placement approval.
- Single cursed controls also wait for Forge approval before notifying redstone neighbors. A veto preserves the original signal and clears the staged block entity. An approved button resets its press, while a lever or plate preserves its power and a plate receives its ordinary release tick.
- Tongue of Dog adds 250 base power and starts the item curse with a full triggered brew. The cauldron retains `RitualTriggered` and `RitualTicks` during the warmup. A charged item stores its ordered recipe and remaining charges under the original `WITCCurse` keys `Curse`, `Count` and `Delay`. A named 1.7.10 cauldron's root `FluidName`, `Amount`, `TicksHeated` and nested fluid `Tag` map to the 1.20.1 tank and ingredient data.
- Tongue of Dog also casts instant, gas and liquid dispersals. A Bound Waystone adds 100 base power and supplies the first destination. Coal adds the one-level Extinguish action. Brew rain spawns `witchery:droplet` with saved `Brew` data. The 1.20.1 port keeps the earlier projectile spawn-egg item ID for save compatibility.
- The legacy chunk importer converts numeric vanilla item IDs inside a falling droplet's `Brew.Items` list. The original entity type, ingredient order, and tint survive the normal save upgrade. A droplet starts checking entity collisions on its fifth flight tick. Its effects and impact particles use its position at the start of the collision tick.
- Both original thrown brew types import as `witchery:thrown_brew`. Fixed projectiles resolve their original ingredient subtype. Custom projectiles keep the ordered bottle ingredients and color, and spells retain their gravity-free flight. A saved thrower's name stays with the projectile until that player is available in the same level, then saves as an owner UUID.
- The 27 built-in fixed recipes use `witchery:cauldron_ritual`. The legacy rules retain subset matching, firing priority, and ingredient power. Active fixed casts save their trigger stack in `RitualTrigger`. A dispenser can withdraw a boiling and powered legacy cast with the trigger at the end of `Items`, keeping `RitualTriggered` and the recipe ID in `FixedRitual`. The bottle applies the valid brew effects in order. Brew Fuel retains the original item ID and maps metadata 0–3 to `FuelTier`. Endless Water retains its item ID and maps named legacy top-level `Damage` to modern durability. The Player Compass keeps the original Taglock keys and the supplied 33-frame sprite set.
- Hand bottling uses the player's skill, potion and gear yield. The Cauldron samples the circle-scaled threshold for a full cast. A positive sample allows the hand interaction to debit the nearest altar's live base cost per bottle. Brew Buckets hold the ordered cast and its trigger. A hand-held matching Brew Bucket fills the remaining space and returns empty, even when the tank needs less than 1,000 mB. The cast keeps warmup across a partial drain and refill between pulses. A pulse with a partial fill resets unfinished warmup or clears an interrupted effect already past warmup.
- A full withdrawal clears `Powered`, retains the vessel's heat and warmup, and displays an empty tank. Heat resets on the first loaded tick that finds the vessel empty. Warmup resets at the next ritual pulse if the brew has stayed empty. A full refill before the power sample can resume the warmed cast. Empty saves retain `HeatTicks` and any positive `RitualTicks` in legacy pulse units. Original `TicksHeated` and `RitualTicks` use the same load path. Ritual completion and interruption reset both immediately.
- Cauldron comparators emit 0 when empty, 3 when cold, 6 when boiling without effects, and 9 when a boiling brew has effects and lacks power. A powered brew emits 12 with spare effect capacity or 15 when its effect capacity is full. Active casts and part-filled imports retain these readings. A full cast uses the circle-adjusted altar threshold and a partial cast uses the base threshold.
- The Cauldron samples altar power on its own tick 7 and every 20 loaded ticks thereafter. Legacy rituals advance from tick 8 in 10-tick pulses. Comparators and sparks use the sampled power flag for legacy brews. The first failed debit during execution keeps the warmed brew until the next pulse. Saves retain `Powered` and the recorded progress while the local tick phase starts fresh. Legacy fixed casts keep their Forge elapsed-tick units, including from earlier port saves.
- `witchery:brew.water` spends one use per accepted pour. Hand pours fill vanilla cauldrons and can replace water or lava. Dispensers fill Witchery Kettles and Cauldrons and can replace lava. A Forge placement veto rolls back the water before durability, sound, or fluid updates.
- A `witchery:taglockkit` costs 1,000 base power to cast a brew on the bound creature or player. The cauldron saves the ordered Taglock and warmup in `Ingredients`, `RitualTriggered`, and `RitualTicks`. Both `decursedirected` and `decurseteleport` keep their original block and block entity IDs and save `WITCPlacer` and `Killers`. The new horizontal block states use network protocol 54.
- The Statue of Broken Curses renders the original wolf model, and the Statue of Occluded Summons uses the stationary Mandrake model. Each uses its original statue atlas, with placed rotation from the saved facing and separate item display transforms.
- Survival players can mine the Goddess, both ward statues, and the Wolf Altar at their original 2.5 hardness. A pickaxe speeds up mining and harvests the statue. The Statue of Worship uses the same tool rule at 3.5 hardness. Void Bramble uses its owner reclaim interaction.
- The Kettle, Fume Funnels, Poppet Shelf, Blood Crucible, Bear Trap, Brazier, Candelabra, Chalice, Crystal Ball, Daylight Collector, and cursed stone pressure plate require a pickaxe for drops. The Wolf Trap has the same mining speed rule and its original empty loot list. The Poppet Shelf drops its stored stacks as well as the shelf, and a filled Chalice drops the separate ingredient stack. A pickaxe qualifies against darkened Shaded Glass, with Silk Touch and color handled by its loot table.
- Perpetual Ice construction pieces match the original tool speeds, including an axe on the gate. The original ice slab friction is 0.6 and the stair friction is 0.98. Brew webs drop nothing, a mounted Demon Heart drops its ingredient by hand, and the cursed snow plate follows the shovel rule used by its uncursed form on modern Forge.
- Legacy terrain brew subtypes 57, 58, 59, and 61 use named `brew_of_*` items. The Kettle recipes are under `data/witchery/recipes/kettle/` and their projectiles dispatch to `BrewTerrain`.
- Legacy subtypes 62 and 76 decode to `witchery:brew_of_erosion` and `witchery:brew_of_ice`. Both use the standard thrown brew projectile and Brew Bag slots. The original Ice resistance list lives in the `witchery:brew_of_ice_resistant` entity tag.
- Legacy subtype 88 decodes to `witchery:brew_of_infection`. It uses the original Kettle ingredients, three servings, and no bottling bonus. Loose Quicklime retains subtype 16 and uses the shared projectile on right-click.
- Dropped Quicklime reacts with prepared cauldron brews if a player is nearby. The nearest player is excluded from the block-safe explosion. Stored brewing actions and the active recipe trigger contribute to blast power. Dropped Gypsum subtype 17 clears any filled cauldron and uses its entire item stack.
- The cauldron resolves an original `RitualTriggered` cast using the saved final `Items` entry. Fixed recipes convert each saved warmup pulse to ten ticks. Buckets and Forge fluid handlers transfer pending fixed, Tongue of Dog, and Taglock casts with their ordered stacks and trigger. The receiving cauldron saves the partial cast until it reaches 3,000 mB. Matching requires the same color, ordered stacks and trigger flag. Powered, boiling remainders from legacy saves also support hand bottling and the original dispenser behavior.
- Powered fixed recipes use the original misfortune roll after completion. White and Infernal circles adjust altar cost and risk. The cauldron counts Leonard before producing the cast result and uses the original lightning roll sequence.
- Registered brew actions obey their capacity and ordering when offered to a legacy fixed recipe. Golden Chalk in a single Infernal ring requires 2,111.5 altar power to warm up and debits 2,111 on completion. The cauldron's power indicator uses the same fractional threshold as the cast.
- Completed legacy cauldron casts send spell particles and a fizz within eight blocks. A cast that fails its target check pops with green smoke for distance, red for missing circles, and purple for an unresolved target or general failure. A cast interrupted after warmup drains the brew and pops with olive smoke.
- Legacy subtypes 116–120 decode to the five `brew_of_solid_*` items. In order, they turn Hollow Tears into stone, dirt, sand, sandstone, or air with erosion beneath. The models use the original solidifier overlay. Subtype 125 decodes to `witchery:brew_of_substitution`.
- Plant Mine metadata 0 through 11 uses `BlockStateTag.variant` on the shared item and `variant` in the placed state. It encodes poppy, dandelion, or shrub in groups of four, ordered Webs, Ink, Thorns, and Sprouting. Legacy stack conversion retains other item tags.
- Kettle `bottling_familiar` accepts `cat`, `toad` and `owl`. Finished batches save `FinishedRecipe` alongside the result. The original 85, 86, 87, 97, 108 and 109 ingredient subtypes map to Frog's Tongue, Cursed Leaping, Hitchcock, Wasting, Bat Ball and Bats.
- Revealing uses `witchery:kettle/brew_of_revealing` for the fixed splash. Named vanilla potion damage 8198 imports as regular Night Vision. Odour of Purity adds Revealing to cauldron mixtures. Heart of Gold adds Animal Attraction and Fermented Spider Eye inverts it to Animal Repulsion. The ordered `Items` list retains the action order and inversion.
- Legacy infused brew subtype 123 maps to `witchery:brew_of_the_grave`. `InfusedBrews` uses the original type, start and Grave cooldown keys.
- Icy Needle subtype 78 adds Cold for two effect levels and 200 altar power. It applies three minutes of base Chilled and freezes connected water at a block impact. The range starts at two blocks and increases by two for each strength boost.
- Frozen Heart subtype 79 adds Ice Shell for five effect levels and 500 altar power. It applies ten seconds of base Chilled to susceptible entities. A direct hit or supported block impact forms Perpetual Ice. A glancing splash applies only the shorter Chilled effect. The shell uses the rite's original outline geometry and clears water, brew gas, and brew liquid inside its radius. The `witchery:ice_shell_resistant` entity tag includes blazes, golems, demons, ents, and boss types. Resistant targets receive water at their feet when the block is replaceable and the dimension permits water.
- `witchery:potion.chilled` keeps the original movement modifier, reduction to incoming fire damage, and periodic magic damage for blazes or at level III and above. Both cold brews keep the strength, duration, and particle suppression modifiers that preceded the ingredient.
- Focused Will subtype 113, Redstone Soup subtype 40 and Hint of Rebirth subtype 33 add `potion.keepinventory`, `potion.keepeffects` and `potion.reincarnate`. Wild Bramble metadata 1 adds `potion.illfitting`. These four recipes use the ordered Cauldron ingredient list and the existing modifiers and dispersals.
- `WITCPoSpawn` in `PlayerPersisted` stores pending effect instances using Forge names. Migration accepts the original vanilla and default Witchery effect IDs. Keeping Inventory preserves the real vanilla inventory until the death clone takes its slots. Modern Vanishing and experience loss run at the normal points.
- Whiff of Magic subtype 34, Spectral Dust subtype 66, Ender Bramble metadata zero, Red Mushroom, Foul Fume subtype 28 and Wheat add `potion.absorbmagic`, `potion.reflectdamage`, `potion.repellattacker`, `potion.poisonweapons`, `potion.stoutbelly` and `potion.feelnopain`. The same Bramble item retains both variants in `Items` and the Cauldron guide. Their status icons use the corresponding ingredient sprites in the modern effect atlas.
- `witchery:absorbable_magic` maps the old magic damage flag to modern damage types. Moonshine keeps its legacy brew name and uses 1.20.1 protection and hunger data. Both food effects have an empty milk cure list. All six recipes use the existing ordered ingredient format and recipe modifiers.
- Sugar Cane, Feather, Cod, and Toe of Frog subtype 90 add `potion.floating`, `potion.featherfall`, `potion.swimming` and `potion.doublejump`. Cod is raw fish metadata zero. Named `minecraft:reeds` and `minecraft:web` import as Sugar Cane and Cobweb. Cobweb adds `potion.reflectprojectiles` or, if inverted, `potion.attractprojectiles`. All six continue to save the original ordered bottle ingredients.
- Protocol 85 uses legacy `clear_fall_damage` for the air-jump request and sends a clientbound approval after checking the player's active Double Jump effect and ground takeoff. Durations and amplifiers use the existing saved effect format and no new player NBT keys.
- Drop of Luck subtype 39 adds `potion.brewingexpertise` or the inverted `potion.insane`. Disturbed Cotton subtype 99, Embermoss, and Mellifluous Hunger subtype 103 add `potion.sinking`, `potion.overheating`, and `potion.wakingnightmare`. A Taglock cast reconstructs the original long duration and hidden particles from the ordered recipe and applies its coven strength limit.
- The original curse handler preserves the preceding duration and particle modifiers for later ingredients in a Taglock cast. This applies to both Drop of Luck variants, the environmental curses, and the coloring dyes. Ordinary drinks and the next ordinary potion effect consume those modifiers.
- Soul Sand adds `potion.allergydark` and Salmon adds `potion.allergysun`. Named legacy `minecraft:fish` metadata 1 resolves to Salmon. `minecraft:golden_apple` metadata 1 resolves to the Enchanted Golden Apple and preserves the separate Health Boost recipe. The ordinary Golden Apple produces Absorption.
- Snowball adds the terrain snow action and the entity effect `potion.snowtrail`. Brick stops later creature effects, Nether Brick stops later terrain actions, and Tropical Fish lifts the individual effect strength ceiling. The original ordered ingredient format carries these modifiers without extra NBT fields. Legacy `minecraft:netherbrick` and `minecraft:fish` metadata 2 load as the corresponding modern items.
- Pumpkin, Blue Orchid, and Silver Dust subtype 157 restore the species-targeted damage actions. Named `minecraft:red_flower` metadata 1 resolves to Blue Orchid. `witchery:silver_brew` keeps the original silver classification and armor reduction for shifted players, Wolfmen, and Wolfman reflections. Silver Dust also starts rain or thunder on a block-targeted ritual. These recipes retain the same ordered ingredient format.
- Garlic uses `witchery:garlic` and applies Weakness to vampires, including vampire reflections. A vampire player also loses the original strength-scaled amount of blood. The modern capability clamps the value and synchronizes it to the client.
- Purified Milk subtype 105 and Reek of Misfortune subtype 35 restore Remove Debuffs and Remove Buffs. Taglock purification uses the original removal and worsening rolls. A boosted ordinary purification can cure the Diseased effect and remove nearby disease fluid. Drinking Purified Milk by itself has a 50% chance to select one effect and retains a player's protected effect if selected.
- Subdued Spirit subtype 112 adds Wasting or, after Fermented Spider Eye, Fullness. Condensed Fear subtype 114 lowers Witchery infusion charge according to capacity, strength, and exposure. Creeper Heart subtype 74 extends active buffs and adds the original Queasy penalty. Its cleanup keeps the player's incurable debuffs with more than five ticks remaining. The standalone Brew of Wasting retains its own projectile behavior.
- Skeleton Skull and Creeper Head add Steal Buffs and Spread Debuffs. Named legacy `minecraft:skull` metadata 0 and 4 restore these recipes inside saved mixtures. The actions preserve donor and recipient selection, amplifier limits, remaining durations, and the exposure-scaled number of effects.
- Rotten Flesh adds the `witchery:potion.diseased` curse. That effect uses the original attack-damage modifier UUID and overlay texture. Tracking clients receive updates through vanilla effect packets. Disease fluid and the ordinary Rotten Flesh brew respect the same creature immunities. An inverted Rotten Flesh brew bypasses the immunity check as in 1.7.10. `diseaseBlockRemovalChance` sets the fluid's scheduled removal roll.
- All sixteen wools set the original RGB without spending power, consuming capacity, or entering the ordered `Items` list. The cauldron persists the separate `Color` field and reads the legacy nested fluid `Tag.Color`. Bottle and dispersal tags retain their saved color.
- The twelve original coloring dyes add `witchery:potion.colorful` for 1,800 ticks and one effect level at zero power. The amplifier is the original dye ordinal. The color label and tint use that ordinal. Named `minecraft:dye` and `minecraft:wool` metadata resolve to their modern items. Ink Sac, Cocoa Beans, Lapis Lazuli, and Bone Meal retain their original ingredient identities.
- Breath of the Goddess subtype 32, Slimeball, and Stick restore Air Hike, Frog Tongue, and Knockback. The current delivery path carries the impact position separately from the caster, including for gas cells, ritual rain, and triggered brews.
- Bat Ball subtype 108 and Owlet's Wing subtype 89 restore Bat Burst and Bodega. Bats use the original `WITCNoDrops` key. Bodega uses active Owl familiar mastery and the existing temporary Owl entity. Sunflower adds Poison Toad with the original `SuicideIn` and `Poisonous` fields. Named `minecraft:double_plant` metadata zero imports as Sunflower.
- Bone adds Raising. The raised creatures retain the original weighted Zombie, Skeleton, and Zombified Piglin selection. Normal throws use the existing enslaver name and UUID. Ritual minions use `witchery:potion.mortalcoil` for the strength-scaled lifetime. Spawns require room for the creature in the modern world.
- Poppy, Dead Bush, Clay Ball, and Emerald add Love, Volatility, Fortune, and Resizing. Named legacy Red Flower metadata zero maps to Poppy. Named Tall Grass metadata zero and `minecraft:deadbush` map to Dead Bush. Love uses separate courtship from the fixed Kettle recipe. Volatility's block damage follows the server option. Fortune adjusts the block's loot tool before evaluating the actual loot table. Resizing uses the original scale levels and saves active dimensions independently of the ordered recipe.
- Dandelion, Bone Meal, Apple, Dirt, and Wheat Seeds restore Flowers, Fertilization, Harvesting, Tilling, and Planting. They use the original overlapping horizontal scanlines, individual vertical ranges, and strength-dependent footprint. Planting uses nearby dropped seeds and ordinary Forge placement. Named legacy Yellow Flower and Bone Meal map into the ordered recipe. Dirt metadata one and two stay separate from the ordinary Dirt recipe.
- Brown Mushroom, String, and Flint restore Pruning, Felling, and Pulverisation. Pruning and Felling use their original separate sphere sizes. Pulverisation visits its open sphere once and advances each eligible block by a single conversion. All eight recipes use one effect level.
- Stone adds Dissipate for one level and zero power. It clears Witchery gas within its vertical disk and deals five magic damage per strength tier to spectral undead. At strength one it also recognizes other gaseous fluids through Forge's lighter-than-air property. Sand restores Tide Hold for one level and zero power. Cobblestone restores Lava Hold for two levels and 100 power. The two holds replace only the matching vanilla fluid, with an open sphere of splash radius plus two. `witchery:slurp` preserves the exact fluid level and remaining loaded ticks, including original `Timeout`, `blockName` and `blockMeta` saves. Legacy Sand metadata one maps to Red Sand and stays a separate item.
- Terrain and creature impacts accumulate duration boosts independently. A potion effect resets the creature boost and leaves the terrain boost available to a later terrain action. Tide Hold and Lava Hold use the terrain multiplier and reset it for the next action.
- Terrain-damaging explosions destroy all three brew blocks and `witchery:slurp` and can reach blocks behind them. Destroyed holds lose their saved fluid. Intact holds continue to restore their exact state. Disease fluid retains its original blast protection.
- Vanilla Lily Pad adds Lilify for one level and 200 power. Named legacy `minecraft:waterlily` imports as `minecraft:lily_pad`. The brew scans up from the adjacent face or target creature and tries the first free water surface. Witchery Lily Pads require source water to survive. Their `growth` state retains the original two-bit range and schedules spread to nearby source water where space and protections permit it.
- Oil of Vitriol and Netherrack add Erosion and Levelling for two levels and 200 power each. The original Erosion bytecode reuses the impact Y on each pass, so the outer disk defines the cleared layer. It returns only Obsidian and wears the legacy five equipment slots according to exposure. Levelling clears raised terrain and fills the open cells in original material order. Its ritual reduces the working Y by one and adds `(strength + 1) * 3` to the radius. Both use the existing ordered item format and leave the standalone Kettle recipes intact.
- Thrown Web, Vine, and Cactus add Webs, Vines, and Thorns for two levels. They restore `witchery:web`, `witchery:vine` and `witchery:cactus` as their distinct original blocks. Webs expand by strength from the center to faces and then edges, including when a previous splash already filled the center. The center and each new cell honor Forge protection. Vines store a horizontal facing and become climbable without the natural vine growth rules. Thorns grow three plus strength blocks high and extend existing Witchery cactus. Vines and Thorns also restore their 90-second targeted effects and reuse the original overlay textures.
- The Vine's facing determines its two-pixel collision strip and the two visible planes on either side of its mount. The model uses biome foliage color and the vanilla Vine sprite. The Web uses the Cobweb sprite. The Cactus uses the three vanilla cactus face sprites. Earlier port saves use the same block IDs and facing values.
- Ent Branch subtype 82 adds Sprouting for two levels and 350 power. Terrain casts grow oriented logs of the struck species with side twigs. Creature casts use the 15-second `witchery:potion.sprouting` effect and periodically grow and lift from beneath the target.
- Oak, Spruce, Birch, Jungle, Acacia, Dark Oak, Rowan, Alder, and Hawthorn Saplings each add a separate tree-growth brew for two levels and 200 power. Forge configured features provide the modern tree forms and respect growth and placement events. Oak and Spruce change at effective strength two. Jungle changes at raw block strength three. Named legacy Sapling and Witchery Sapling stacks normalize their metadata to the modern item or `BlockStateTag.wood_type`.
- Poisonous Potato and Nether Quartz add Blight and Raise Land for four levels and 2,000 power each. Blight uses the original single impact layer, withering and soil conversions, villager and cow conversions, and chance of magic damage to animals. Raise Land moves one column for an ordinary splash or creature cast and uses the original strict disk for a block-targeted ritual. It retains the vertical block states and rolls back movement on a Forge veto.
- Ender Pearl adds Transpose for four levels and 1,000 power. Normal creature casts use three attempts to find footing around the impact, including candidates in chunks that need to load. Taglock rituals use the first Waystone and fixed inhibition threshold three. Block rituals use the first and second Waystones in ingredient order, require the original 24-glyph Otherwhere circle at the first site, and move the original three-layer footprint. The shared teleport used by these creature casts emits smoke and the original Witchery poof for vampires at both ends. Other creatures get portal particles and the Enderman sound.
- Iron Ingot adds Transpose Ore for four levels and 2,000 power. Strength selects the original Iron, Gold, Lapis, and Emerald order, including each modern Deepslate form. A Waystone ritual expands the footprint with the coven and resumes the four-block descent using the saved cauldron pulse.
- Glint Weed adds Inferno for three levels and 750 power. The legacy `witchery:splatter` projectile launches from the impact, sets supported terrain or struck creatures on fire, and splits at higher strengths. Original saves with the unused `Color:0` field recover the default dark red sprite tint. Explicit nonzero colors and saved strengths are preserved.
- Refined Evil adds Hell Gate for five levels and 3,000 power. Creature casts apply the original Hellish Aura with synchronized green fire. An instant Dog Tongue ritual summons a Demon at a valid medium Infernal circle.
- Gold Ingot adds Blast for five levels and 500 power. Brew strength controls the radius of the ordinary destructive explosion. Block casts use the adjacent face and creature casts use the original exposure-scaled chance.
- A `witchery:biomenote` adds Changing Seasons for eight effect levels and 5,000 base altar power. Its `WitcheryBiome` persists in the ordered `Items` list. The block-targeted action forces instant dispersal and supports a Tongue of Dog ritual at a Waystone. The biome writer updates and resends every intersecting quart column, and dry biomes clear snow layers within the original block footprint.

### Optional legacy brew integrations

- Drain Magic and powered Anti-Magic Bolts debit the optional Blood Magic Soul Network using the original Life Essence tiers. Thaumaturge wands use the original whole-Vis debit loop and the current wand's consumption modifier.
- With Ars Magica Legacy installed, both attacks drain a fraction of the target's maximum mana, with the original minimum debit of one. The original in-wall damage exception applies to small Resizing players and players in wolf or bat form when their feet are in a block that does not render as solid.

## Network Packets (`PacketPipeline`)
Source: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/network/PacketPipeline.java`

| ID | Packet Class | Side |
|---:|---|---|
| 1 | `PacketBrewPrepared` | `SERVER` |
| 2 | `PacketParticles` | `CLIENT` |
| 3 | `PacketCamPos` | `CLIENT` |
| 4 | `PacketItemUpdate` | `SERVER` |
| 5 | `PacketPlayerStyle` | `CLIENT` |
| 6 | `PacketPlayerSync` | `CLIENT` |
| 7 | `PacketPushTarget` | `CLIENT` |
| 8 | `PacketSound` | `CLIENT` |
| 9 | `PacketSpellPrepared` | `SERVER` |
| 10 | `PacketClearFallDamage` | `SERVER` |
| 11 | `PacketSyncEntitySize` | `CLIENT` |
| 12 | `PacketSyncMarkupBook` | `SERVER` |
| 13 | `PacketExtendedPlayerSync` | `CLIENT` |
| 14 | `PacketHowl` | `SERVER` |
| 15 | `PacketExtendedVillagerSync` | `CLIENT` |
| 16 | `PacketSelectPlayerAbility` | `SERVER` |
| 17 | `PacketExtendedEntityRequestSyncToClient` | `SERVER` |
| 18 | `PacketPartialExtendedPlayerSync` | `CLIENT` |
| 19 | `PacketSetClientPlayerFacing` | `CLIENT` |

### 1.20.1 Network
Implementation anchors:
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/network/WitcheryNetwork.java`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/registry/LegacyRegistryData.java`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/Witchery.java`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/client/packet/WitcheryClientPacketHandlers.java`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/client/event/WitcheryClientCameraHooks.java`


- Uses one `SimpleChannel` (`witchery:main`) with protocol version `"98"`. Matching builds register the Cauldron particle type with its tint and form. The extended player payload carries vampire rank, blood power, blood reserve, quest progress, vision, ultimate choice and charges, the transient selected power and the saved creature form. `VampireQuestChunks` preserves the original packed `Location` values and records a modern dimension ID. Player style sync updates the other clients that track a transformed player. The vampire messages select or activate a power and query or display a nearby target's blood. Legacy markup book intent 12 saves the trail through the held journal's unlocked chapter and raises the reader's saved cap. The infusion status includes the active infused brew and remaining duration. The Kettle recipe codec carries the priority and optional bottling familiar.
- The sleeping body sends its saved owner name alongside an optional owner UUID. This lets tracking clients render bodies imported from 1.7.10, where the save used the owner name alone. The renderer uses the connected owner's UUID when available and uses the shared profile lookup for a named owner.
- Legacy intent 17 requests the blood value for a nearby target, and intent 15 delivers it. Vanilla synchronizes the sleeping pose. Witchery sends player style updates to clients tracking the player.
- The vampire HUD selects the closest pickable entity along the original five-block entity trace, including through solid blocks. Only villagers, guards and eligible players display a count. Another pickable entity can block the reading. Feeding uses the ordinary entity interaction and the original 1.3-block bite range, or 2.1 blocks against a paralyzed target.
- Pressing the first hotbar binding while that slot is selected cycles the vampire powers available at the current rank, then returns to the held item. Other slot bindings clear the selection. The HUD shows the selected power's name, and an active power replaces normal block use while preserving ordinary mining. Mesmerize leaves Werevillagers unaffected and spends no blood on them.
- The hotbar draws its selection outline on the active vampire power or on the held inventory slot. The powers leave room for the 1.20.1 offhand and attack indicator and wrap on narrow screens. With the power clear, a survival vampire's attack on a Garlic Garland is canceled and lights the player for one second. Rank-six shattering also requires a clear power selection and uses Forge's mining speed, harvest and exhaustion hooks.
- Villagers and Werevillagers read their original root `WitcheryExtendedVillager.Blood` when loaded and save it under `ForgeData`. An existing modern count takes precedence. The packaged migration check verified the saved counts in the vampire HUD and through feeding before and after a dedicated restart.
- Crystal Ball viewing sends the existing `cam_pos` message for the transient `witchery:eye` or the living subject of a Taglock Kit. Server chunk and entity tracking follow the viewed entity, and the client centers terrain rendering on it. The observing player stays beside the Crystal Ball.
- The client associates each Crystal Ball view with the local player, level and target entity ID. The stop packet, a player or level replacement, target loss and the original three-second heartbeat timeout all restore the normal camera and the viewer's previous gravity setting. A matching heartbeat can resume the existing view once its target is tracked again.
- The Mystic Branch recognizes gestures from server-side player rotations. Protocol 12 retires the prepared-spell packet while recording the old intent in `LegacyRegistryData.PACKETS`. The client previews the shared stroke rules from local rotations and displays the recognized spell. `branchGlyphSet` selects the original texture bank with value 1 and defaults to bank 0. Cancelling the use clears the drawing.
- A recognized Mystic Branch symbol plays the original harp at the caster before release. Rejected casts and insufficient infusion charge play the half-volume snare. These cues reach the caster and nearby players at the original varying pitch.
- All four Leonard's Urn spells launch the stored splash brew from the original eye offset. It starts at 0.75 blocks per tick with Gaussian spread, takes no caster motion or gravity, and expires on its 200th air tick. In-flight saves retain the projectile's recorded velocity.
- The hand and Brew Bag use the original eye offset and Gaussian spread. Fixed brews and Quicklime start at 1.5 blocks per tick along the aim. Custom Cauldron bottles start at 0.75 with the 20-degree pitch offset and expire on their 200th air tick.
- Custom splash brews and Urn projectiles can hit living entities and blocks. Fixed brews can also hit nonliving entities such as boats. Both regain collision with their thrower on the fifth air tick.
- The Brew Bag selects the occupied gesture slot from server-side player rotations and casts it on release. Its original `brew_prepared` intent stays in the migration catalog, and discriminator zero stays reserved so the active message IDs remain stable.
- Selecting a filled Brew Bag slot plays the original pop once at the caster at full volume and the original varying pitch. Drawing toward an empty slot stays quiet and leaves the next gesture available.
- Brew Bags and Leonard's Urns store bottles under `tag.Inventory.Items`. The legacy world import applies Minecraft's item data fixes to each numeric vanilla ingredient inside those bottles. Later loads repair the same records in player inventories and block entities using the captured Forge registry.
- The `render3dGlintEffect` client option applies to the held Mystic Branch, Huntsman Spear, and enchanted Cane Sword models. It defaults to true and preserves the inventory icons' standard foil. The Huntsman and goblin model glint use their existing independent setting.
- Validates packet receive direction against legacy flow metadata and logs mismatches.
- Represents all legacy packet intents in `LegacyRegistryData.PACKETS` as migration metadata.
- Player synchronization payloads:
  - `player_sync` -> `(UUID playerId, int syncRevision, CompoundTag playerData)`
  - `extended_player_sync` -> `(UUID playerId, boolean initialized, int syncRevision, CompoundTag playerData)`
  - `partial_extended_player_sync` -> `(UUID playerId, int syncRevision, CompoundTag playerData)`
- Interaction payloads:
  - `item_update` -> `(int slotIndex, Volume volume, int pageIndex, ResourceLocation entryId)` (serverbound, optional biome ID for the atlas or rite ID for Circle Magic)
  - `sync_entity_size` -> `(int entityId, float scale)` (clientbound, protocol 86)
  - `set_client_player_facing` -> `(float yaw, float pitch)` (clientbound)
  - `cam_pos` -> `(boolean active, boolean updatePosition, int entityId)` (clientbound; aligned with legacy packet shape)
  - `push_target` -> `(int entityId, double velocityX, double velocityY, double velocityZ)` (clientbound)
  - `sound` -> `(String soundId, double x, double y, double z, float volume, float pitch)` (clientbound)
- Utility payloads:
  - `particles` -> `(int particleEffectId, int soundEffectId, double x, double y, double z, double width, double height, int color)` (clientbound)
  - `player_style` -> `(String username, int grotesqueTicks, int nightmare, boolean ghost, int creatureType, int blood, String playerSkin)` (clientbound)
  - `sync_markup_book` -> `(int slot, optional List<String> trail)` (serverbound, protocol 97)
- Requests with empty payloads:
  - `clear_fall_damage` (serverbound)
  - `howl` (serverbound)
- Clientbound behavior:
  - `particles` now dispatches to client packet bridge with legacy `ParticleEffect`/`SoundEffect` id mapping, effect-count scaling, and colored spell particle support.
  - `player_sync`, `extended_player_sync`, and `partial_extended_player_sync` apply serialized `WitcheryPlayerData` snapshots, reject stale revisions, and update the corresponding persistent tags.
  - `player_style` applies the synchronized style to the player with the matching username.
  - `sound` resolves the sound ID and plays the registered event at the transmitted position.
  - `push_target` now dispatches to client packet bridge to apply motion vectors to client entities.
  - `set_client_player_facing` now dispatches to client packet bridge to apply local player rotation.
  - `sync_entity_size` restores the active Resizing scale on the client. The client uses its local pose and creature form to compute physical dimensions from that scale.
  - On dimension change, the server replays the player's active tracked Witchery effects to the replacement local player through vanilla effect packets. This keeps Resizing, Colorful, Grotesque, Disease, Hellish Aura, and the plant brew effects available immediately after direct teleports.
  - `cam_pos` now dispatches to client packet bridge with legacy semantics (`active`, `updatePosition`, `entityId`) and stages camera-active/target data in `WitcheryClientCameraState`.
  - `WitcheryClientCameraHooks` applies the current camera target each client tick and restores the local player as the camera when the view ends or its target becomes invalid.
- Serverbound gameplay handlers:
  - `clear_fall_damage` requests an airborne jump. The server validates the active Double Jump effect and remaining jumps from a ground takeoff before approving the motion.
  - `item_update` validates the held slot and guide volume. Atlas requests resolve the supplied biome ID against the server's current list and save both `CurrentPage` and `WitcheryBiome`. Network protocol 87 carries the atlas ID.
  - `sync_markup_book` validates the actual visited trail against the held journal's unlocked chapters. An empty trail returns to the cover. Closing an untouched book keeps its existing `pageStack` verbatim. The server learns the rank cap from the held book in either case. Navigation follows recognized unlocked entries in their saved order, including for earlier port stacks with a single bookmark.
  - `item_update` records the accepted page in `WitcheryPlayerData` and sends the updated revision.
  - `howl` checks the player's pose, form, rank, trial status, paralysis, and selected vampire power. A successful howl uses the original varying pitch and counts a new nighttime trial chunk, paralyzes nearby targets, or summons a pack. A blocked repeat plays the original snare alongside the port's action-bar message and keeps the cooldown timestamp. A pack rerolls the limit at each spawn attempt and needs two clear blocks above the floor.

Legacy intent catalog:
| Legacy ID | Intent Key | Flow |
|---:|---|---|
| 1 | `brew_prepared` | `SERVERBOUND` |
| 2 | `particles` | `CLIENTBOUND` |
| 3 | `cam_pos` | `CLIENTBOUND` |
| 4 | `item_update` | `SERVERBOUND` |
| 5 | `player_style` | `CLIENTBOUND` |
| 6 | `player_sync` | `CLIENTBOUND` |
| 7 | `push_target` | `CLIENTBOUND` |
| 8 | `sound` | `CLIENTBOUND` |
| 9 | `spell_prepared` | `SERVERBOUND` |
| 10 | `clear_fall_damage` | `SERVERBOUND` |
| 11 | `sync_entity_size` | `CLIENTBOUND` |
| 12 | `sync_markup_book` | `SERVERBOUND` |
| 13 | `extended_player_sync` | `CLIENTBOUND` |
| 14 | `howl` | `SERVERBOUND` |
| 15 | `extended_villager_sync` | `CLIENTBOUND` |
| 16 | `select_player_ability` | `SERVERBOUND` |
| 17 | `extended_entity_request_sync_to_client` | `SERVERBOUND` |
| 18 | `partial_extended_player_sync` | `CLIENTBOUND` |
| 19 | `set_client_player_facing` | `CLIENTBOUND` |

## GUI IDs
Sources:
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/common/CommonProxy.java`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/client/ClientProxy.java`

| ID | Server | Client |
|---:|---|---|
| 0 | `null` | `BlockAltarGUI` |
| 1 | `null` | `GuiScreenWitchcraftBook` |
| 2 | `ContainerWitchesOven` | `BlockWitchesOvenGUI` |
| 3 | `ContainerDistillery` | `BlockDistilleryGUI` |
| 4 | `ContainerSpinningWheel` | `BlockSpinningWheelGUI` |
| 5 | `ContainerBrewBag` | `ItemBrewBagGUI` |
| 6 | `null` | `GuiScreenBiomeBook` |
| 7 | `null` | `GuiScreenMarkupBook` |
| 8 | `ContainerLeonardsUrn` | `ItemLeonardsUrnGUI` |

### 1.20.1 Forge Menus and Book Screens

The six container menus are registered in `WitcheryMenus` and bound to their screens in `WitcheryClient`. The original three book GUI IDs opened client readers from held items. Guide volumes and the atlas use the held book packet to open `GuideBookScreen`. The vampire journal opens `VampireBookScreen` from the held item. Page and atlas-selection data live on those items and use bookmark packets. The earlier port's three unused book menu types were removed.

The ten guide volumes and the Vampire Journal have localized text in English, German, Spanish, Russian, and Simplified Chinese. The Circle Magic chapter labels use the original rite names where they identify the chapter, with qualifiers to distinguish variants. Rewritten instructions describe the live recipes. Brew ingredient pages include the individual splash, gas, liquid, and triggered dispersals. Kettle pages resolve both the recipe's familiar species and dimension at display time.

| Legacy GUI ID | Forge menu or client screen |
|---:|---|
| 0 | `witchery:altar` |
| 1 | `GuideBookScreen` for guide volumes |
| 2 | `witchery:witches_oven` |
| 3 | `witchery:distillery` |
| 4 | `witchery:spinning_wheel` |
| 5 | `witchery:brew_bag` with eight slots backed by the held bag |
| 6 | `GuideBookScreen` for the atlas |
| 7 | `VampireBookScreen` for the vampire journal |
| 8 | `witchery:leonards_urn` with the original layout backed by the held urn |

## Dimensions
Source: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/Witchery.java`

- Dream: `WorldProviderDreamWorld`
- Torment: `WorldProviderTorment`
- Mirror: `WorldProviderMirror`

### 1.20.1 Dimension Key Scaffold (current)
Implementation anchor:
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/world/WitcheryDimensions.java`

| Legacy Dimension | Level Key | LevelStem Key | DimensionType Key |
|---|---|---|---|
| Dream | `witchery:dream` | `witchery:dream` | `witchery:dream` |
| Torment | `witchery:torment` | `witchery:torment` | `witchery:torment` |
| Mirror | `witchery:mirror` | `witchery:mirror` | `witchery:mirror` |

### 1.20.1 Dimension Datapack Scaffold (current)
Implementation anchors:
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/resources/data/witchery/dimension_type/dream.json`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/resources/data/witchery/dimension_type/torment.json`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/resources/data/witchery/dimension_type/mirror.json`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/resources/data/witchery/dimension/dream.json`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/resources/data/witchery/dimension/torment.json`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/resources/data/witchery/dimension/mirror.json`

### 1.20.1 Dimension Travel
Implementation anchors:
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/world/WitcheryDimensionTravelHooks.java`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/event/WitcheryEventHooks.java`

Current travel behavior:
- `TormentTravel` processes collision requests and chooses one of six generated entrances or the Overworld respawn point. It retains the numeric legacy `PlayerPersisted.WITCForceTorment` and `WITCForceTormentLevel` fields. `witchery_torment_maze` saves which footprint a world uses.
- Forge event wiring observes both travel attempts and completed dimension transitions for Witchery dimensions.
- `DreamTravel` handles Brew of Sleeping and waking in the server player tick. It records both inventories, health, hunger, the Overworld body coordinates and the nightmare flag under `PlayerPersisted.WITCSpiritWorld`, and retains `witchery:corpse` for the sleeping body.
- Dream transitions search the entire original column for an arrival, preferring the upper spot at equal distances. The port can use a neighboring column or the heightmap when that search finds no suitable space. Arrival is centered one-tenth of a block above the selected floor. The server resets its movement check to the destination after teleporting. Sleeping and waking then apply a `(0, 0.1, 0)` push. Manifestation and returning as a ghost only apply the position update.
- `witchery:spiritportal` uses a snow frame in the Dream and manifests only a spirit walker with remaining rite time. `WITCManifested` and `WITCManifestDuration` persist in the Forge player tag. The portal retains its block ID and adds a horizontal `axis` property.
- `witchery:tormentportal` uses `TormentPortalBlock`. It retains the block ID and adds `axis=x` and `axis=z` orientations.
- Brew of Flowing Spirit uses legacy ingredient damage 96. The Dream-only brewing restriction uses the optional `dimension` field on the datapack recipe and synchronizes it to clients. Focused Will, Condensed Fear, and Brew of Hollow Tears use damage values 113–115.
- `MirrorTravel` opens a linked chamber when the player walks into a correctly mounted `witchery:mirrorblock`. A generated `witchery:mirrorblock2` links back to the exterior. The room is allocated in saved data and the two mirrors store their connection and Reflection defeat state.

Status:
- resource keys and breadth datapack scaffold are now present for Dream/Torment/Mirror.
- Dream and Nightmare cotton drops are available from naturally generated Somnian Cotton. The ordinary Nightmare now uses the original `witchery:nightmare` key with a dedicated entity and renderer. Dreamweaves use `witchery:dreamcatcher` and preserve the five original `WITCWeaveID` values. Demonic nightmares add fireballs overhead and can summon `witchery:demon`. The Waking Nightmare curse and status effect use the same entity outside the Dream.
- `witchery:demon` can also be summoned by two large Infernal rites in the Overworld. The entity persists bargains, remaining stock and whether it came from a rite. Demon Heart and Infernal Blood use legacy ingredient damage values 23 and 80. The heart block retains `witchery:demonheart` and drops the canonical `witchery:demon_heart`. The old port's heart item ID still works in both Infernal Blood recipes.
- Bone Needle and Brew of Grotesque map legacy ingredient damage values 7 and 71. Taglock Kit uses the original `witchery:taglockkit` ID and shows the filled icon when bound. `WITCPlayer1` and the creature UUID halves in `WITCEntityIDm1` and `WITCEntityIDl1` work with the bound-taglock recipe ingredient. The new player UUID is authoritative when present. The Curse of Waking Nightmare persists under `PlayerPersisted.witcheryWakingNightmare`.
- The `witchery:dream` dimension type references its registered client effects for fog and sky. The server chooses the shared mode from the active dreamers, independently of player-style packets. The same mode gives the server and client a day angle of zero in normal Dream and midnight in both nightmare modes, including actual sky darkening.
- The Mirror uses the `witchery:mirror` chunk generator and biome. New chunks contain the original stacked chambers. The dimension uses the same modern build bounds as the previous port, including for chunks saved before the generator change. The opt-in `shrinkMirrorWorld` server setting restores eight tiers and caps wall generation at Y 127 after a restart. Room allocation retains the `NextCell` saved index and honors the shorter span for new links.
- The Rite of the Mirror creates `witchery:mirror`. The previous port's `witchery:mirrorblock` item also mounts two halves. The block entities preserve `DimCoords`, `Connected`, and `DemonSlain` and use a UUID to verify both ends. `PlayerPersisted.MirrorWorldEntryPoint` retains the chamber entry. `witchery:reflection` saves its pose, equipment, health, and identity. The encounter waits for saved entities to load before checking whether the room has a Reflection.
- Inhabited exterior Mirrors keep `Favorite`, `Fairest`, `PlayersSeen`, and `LastFairestSpawnTime` for conversations. `witchery:mirrorface` retains its age in `WITCTicksAlive`. The `newFairestOfThemAllSpawnChance` server setting controls the original 1% chance after the two-minute cooldown.
- `witchery:reflection` retains the legacy `FreeSpawn`, `LivingTicks`, `Owner`, `Model`, and `Vampire` fields. It saves the contemporary owner's UUID and the copied skin separately. A freely spawned Reflection leaves the linked chamber's defeat record alone.
- Inhabited exterior Mirrors use a Taglock Kit to apply creature form 4 for 4,000 power. `LastPlayerSkin` imports into `OtherPlayerSkin`. The `allowChatMasquerading` and `allowNameplateMasquerading` server settings control the display names. Quartz Spheres charge into named Duplicate Grenades for 2,000 power.
- Torment uses `witchery:torment` as its maze generator and biome. The original `witchery:force`, `witchery:tormentstone`, `witchery:tormentportal`, and `witchery:refillingchest` IDs appear in six stacked mazes. Refilling Chests preserve `Items` and `WITCLifeTicks`. Legacy import also converts literal custom names and reconnects double chests after both chunks load. The old Torment build bounds and saves remain readable.
- `witchery:lordoftorment` and `witchery:soulfire` retain their legacy IDs. The Lord preserves its attacker names under `WITCAttackers`. `PendingBossFloors` in `witchery_torment_maze` holds the queued second phase until the destination entities load. The Brew of Soul Torment uses ingredient damage 139 and writes `PlayerPersisted.WITCSpellBook.tormentum` on drinking.
- The Lord's ranged attack fires a rank-one Ignianima projectile and three Soulfire fireballs, with a one-in-ten chance of nine fireballs. Soulfire launches half a block above the spell, and the volley also works at close range. Earlier 1.20.1 Soulfire entities with the saved `Ignianima` flag can finish their existing flight.
- `witchery:cat` is the familiar subtype of a vanilla cat. The cat, Owl and Toad carry the legacy `Familiar` flag. `FamiliarBinding` reads and writes `PlayerPersisted.WITCFamiliar`, including the original `UUIDMost`, `UUIDLeast`, `FamiliarType`, `FamiliarColor`, `FamiliarName` and `FamiliarSummoned` fields. Species IDs remain `1` for cat, `2` for Toad and `3` for Owl. Both familiar rites use the medium white circle. The calling rite recalled a loaded cat from Torment without changing its UUID. Dismissing an Owl drops any item it is carrying.
  - Binding checks that a pet overlaps the one-block-high glyph layer and that both pet and owner are within seven blocks of its lower corner. Calling selects owners whose hitboxes overlap the whole square, including its corners. A dismissed familiar returns at the glyph center with a 0.001-block Y offset. A live familiar travels to its owner's exact position. Binding sends hearts and a fizz, calling sends instant spell particles and a fizz, and recreation or dismissal pairs instant spell particles with the Enderman teleport sound.
  - Familiar damage sharing uses the saved UUID halves. The 1% reduction applies even after dismissal. A loaded familiar at a coordinate distance of 24 blocks or less grants 10%, including between dimensions. The familiar takes its share only when the redirected amount reaches one point. Species mastery still requires a summoned familiar.
- The Leech Chest and Blood Rose keep the original `witchery:leechchest` and `witchery:bloodrose` block and item IDs. Their block entities retain `WITCPlayers` with `Player` entries. The chest item keeps that list after harvesting and preserves inventory contents as separate drops. The Blood Rose uses `full` for its visible sample state and the Boline drops an empty plant.
- The placeable `witchery:coffin` uses two halves of `witchery:coffinblock` and the original `Opened` block entity field. `part`, `facing` and `occupied` follow modern bed states. `paired` distinguishes a placed pair from the previously saved decorative block. The shared bed Taglock path resolves the head from either half.
- `witchery:glassgoblet` keeps the original `WITCBloodUUID` and `WITCBloodName` tags. `witchery:follower` retains `FollowerType`, `Skin` and `TicksToLive` and saves the lava destination and transformation time. `witchery:lilith` keeps `Invul`, `Lifetime` and `Friendly`. The player capability accepts the rank and blood keys from the legacy `WitcheryExtendedPlayer` compound.
- A Large Fireball reflected by a player interrupts Lilith's recovery before fire immunity blocks its direct damage. She resumes healing on the tenth healing pulse after impact. The 1.20.1 save retains the remaining interruption under `WeaknessTicks`.
- Lilith's eight innate armor points retain the original 32% reduction for armor-sensitive damage. The normal Forge armor calculation handles any added armor before applying the innate reduction. Armor-bypassing sources skip both. Defeat removes curable effects and preserves incurable ones. Her book bargain keeps the count and tags and drops the enchanted books individually to respect their 1.20.1 stack limit.
- The rank-ten offering turns an eligible villager or Village Guard into `witchery:vampire` and clears both blood tags from the goblet. The entity keeps `BaseX`, `BaseY` and `BaseZ` as its home and saves `Hunting` and `HuntBites`. Older port entities without the home tags adopt their saved position.
  - A hunt can use an occupied modern village site or a village center from the original `data/villages.dat`. The archived village search includes any center within 128 blocks plus the saved village radius. During that hunt the vampire patrols and targets villagers within the saved radius. `HuntVillage` stores the center and radius while the hunt is active.
  - The vampire checks the selected column at its starting height, then alternates one to eight blocks above and below. The first solid surface with two air blocks above receives the vampire 1.05 blocks above the surface. Smoke and Witchery's poof reach 16 blocks at both endpoints. Drawing blood sends Red Dust and the drink sound within 16 blocks. After five bites it ends the hunt and deposits five blood in the first Blood Crucible in the original X, Z, Y order within six blocks of home. A blocked return still completes the hunt and fills the vessel.
- `witchery:bloodcrucible` saves its original `BloodLevel` from 0 to 20. The player's `VampireUltimate` preserves legacy IDs 0 for none, 1 for Thunderstorm, 2 for Bat Swarm and 3 for travel. `VampireUltimateCharges` retains the remaining uses.
- The travel ultimate uses saved legacy village centers alongside modern village locations. It checks the original column in order, sends smoke and the Witchery poof at both endpoints, and spends a use after successful travel.
- `witchery:attackbat` saves its caster under the original `Owner` GameProfile tag and its remaining life under `SwarmTicks`. The reader converts the legacy string `Owner.Id` to the modern UUID array and retains profile properties. Summoned attack bats carry `WITCNoDrops` in their entity data.
- A bone selects ultimate ID 3. Coffin and village travel uses the existing modern respawn fields and the village information available in loaded chunks. A dimension change refreshes the replacement client's Witchery player data.
- The nine vampire garments retain their original item IDs. Armor uses the vanilla `Damage` and `display.color` item tags. Vampire mobs retain their equipped garments in `ArmorItems`.
- `witchery:vampire_fire` and `witchery:sunlight` identify the two legacy vampire damage sources. The server's `vampireDeathItemKeepAliveMins` ranges from five to thirty minutes and defaults to twelve. Dropped items save their configured lifespan through Forge's `Lifespan` tag.
- `witchery:canesword` retains the per-item `WITCBladeDeployed` flag. The player's `BloodReserve` retains the original tag and a maximum of 250.
- `witchery:silvervat` stores dust in the original `Items` slot zero and remembers the four adjacent gold output counts in `ObservedGold`. The optional Silver Sword recipe uses `forge:ingots/silver` when another mod supplies that tag.
- Legacy rite 94 maps to `witchery:remove_vampirism`. The normal circle save stores the active recipe, caster, coven and offerings. The cure resets the existing vampire capability fields and retains the learned rank cap.
- `witchery:garlicgarland` keeps its block and item ID. The block state saves a horizontal `facing` and defaults to north for existing 1.20.1 saves without that property.
- `witchery:villageguard` reads and writes the legacy `GuardType` byte and `BloodLevel` value. It stores equipment in `HandItems` and `ArmorItems`. Earlier scaffold saves without a blood value initialize at 500.
- `witchery:vampire_bite` preserves the attacking player's attribution. Protocol 41 adds guard targets to the existing vampire blood display. Guard feeding uses the same saved `BloodLevel` as direct-kill reserve collection and the drained-target requirement for conversion. The original `BloodReserve` belongs to the player. Direct kills of villagers, guards and players fill it up to 250, including while the attacker is mortal. A vampire uses a sheathed Cane Sword to drink it and can complete the rank-one trial when it fills their blood power.
- Attempting to feed from an unshifted or shifted werewolf player, a Werevillager, a Wolfman, or a Wolfman Reflection deals four damage to the vampire and preserves both blood stores. The target emits flame particles and the blood-drinking cue.
- General village defense uses the villagers' existing `Gossips` save data and the same territory used for recruitment. The server remembers attributed attacks on residents in memory for 300 ticks. No extra village save file is required for those short-lived attacks.
- `witchery:handbow` reads and writes legacy `WITCBoltTypeCurrent` and `WITCBoltTypePreferred` IDs 1 through 5 for wooden, anti-magic, holy, splitting and silver ammunition. Ingredient subtypes 130 and 132–135 and 155 map to Null Catalyst and the five bolts. The dedicated `witchery:bolt` projectile keeps the original `boltType` IDs 0 through 4 and uses modern projectile owner and collision state. The older scaffold's `witchery:bolt_spawn_egg` stack remains readable. Protocol 42 includes the ammunition, sounds and projectile data.
- Hunter hats, coats, trousers and boots retain their twelve original item IDs. Ingredient subtype 131 maps to `witchery:nullified_leather`. `witchery:hunter_clothing_upgrade` copies the input armor's NBT into the crafted variant, including `Damage` and `display.color`. Protocol 43 includes those recipes and the restored clothing items.
- `witchery:witchhunter` keeps `HunterType` and `HunterTarget`, and adds `HunterTargetUUID` for stable player targeting. Legacy name-only targets still load. The original `PlayerPersisted.WITCHunterTrigger` uses Overworld game time. The server setting `vampireHunterSpawnChance` defaults to 0.01 per interval. Protocol 44 includes the restored entity and its arrival sound. Wolfman Reflections count as werewolves for pursuit and Silver Bolt selection. Reflections retain their last copied form and vampire state when an owner leaves range, then refresh those fields when they copy a player again.
- Original `data/villages.dat` records supply the shared per-village reputation used by Witchery. Guard recruitment and defense use that score alongside local gossip. Hunter reports use the nearer legacy village, and rank-seven vampires count the original center chunk for each visit.
- The Witch Hunter and Village Guard entity loot tables use `witchery:looting_bonus` for the original integer ammunition and player-credited rare-drop rolls. Both tables continue to use Forge's effective Looting level.
- Hellhound entity ID 136 maps to `witchery:hellhound` and retains `ConversionTime`. An active cure clears Weakness and grants Strength when the saved countdown is loaded. The Wolf Head block keeps `witchery:wolfhead` and stores the trophy's `hellhound`, `facing` and `rotation` properties. Ordinary Wolf Heads retain their item ID. Legacy item metadata 1 maps to `witchery:hellhound_head` in the Witchery stack loader. Protocol 45 includes the new trophy item and block states.
- The `hellhoundSpawnWeight` server setting feeds the Forge Nether biome modifier when the world loads. It defaults to 25, clamps to at least one, and preserves the original groups of one to three.
- Rite IDs 92 and 93 map to `witchery:curse_of_the_wolf` and `witchery:cure_lycanthropy`. Player saves preserve `WerewolfLevel` and the original wolf ordinal 1. Legacy nested data also reads `CreatureType`. Server setting `allowVampireWerewolfHybrids` defaults to true. Protocol 46 includes the two rites and original howl.
- Entity IDs 135 and 137 map to `witchery:wolfman` and `witchery:werevillager`. `Infectious` remains a legacy save field. `FormerVillager` stores the modern villager snapshot and reads the old numeric `FormerProfession` when available. `WITCWereVillagerUUID` preserves the pre-curse Taglock identity through each form and cure. Protocol 47 includes the creatures and their sounds.

## 1.20.1 Forge Event Hooks

- `WitcheryEventHooks` restores infusion state, death brews, and vampire and wolf forms on login or respawn. On dimension change it closes scrying, records Witchery dimension transitions, checks Nether predictions, and syncs infusion and creature state. Tracking clients receive the creature, Nightmare, or ghost appearance when they begin tracking the player.
- The server player tick advances vampirism, lycanthropy, and other effects and processes Dream, Mirror, and Torment travel. Block interactions post the relevant cursed item and Fiery Touch checks. Mirror activation uses the registered circle rite and portal logic.
- `allowInterModBlockBreakEvents` defaults to true. Forge block-break cancellation is honored when Witchery terrain effects, spells, or rites would remove or replace blocks. The toggle applies to events posted by Witchery's scripted changes.

## 1.20.1 Player Capability and Migration

- `WitcheryPlayerDataProvider` attaches the `witchery:player_data` capability to players. `PlayerEvent.Clone` copies the data and restores death-protected inventory when applicable. Login sends `extended_player_sync` and `player_sync`. Respawn and dimension changes send `partial_extended_player_sync`.
- `LegacyWorldIds.upgradePlayer` converts Witchery stacks and effects before vanilla's player data fixer. It copies `WitcheryExtendedPlayer` into the modern capability when one is absent, and moves a restorable `CachedInventory2` into the modern Death Brew inventory record. The original Dream session items and pending `WITCPoSpawn` effects go through the same upgrade path.
- An earlier 1.20.1 player record may still hold Keep Inventory's armed cache at the root, in the original capability wrapper, or in the pending Death Brew list. The normal loader repairs the pending list with the saved registry. An existing pending list takes precedence. The list survives saving while dead and is consumed on restoration.
- The importer converts each entity in an original player `Riding` chain with the same entity restoration used for chunks. It links the converted entities through `Passengers`, writes the outer vehicle as `RootVehicle.Entity`, and keeps the direct mount UUID under `RootVehicle.Attach`. This applies to dedicated player files and the single-player owner record in `level.dat`.
- The original potion ID table also resolves `CustomPotionEffects` in legacy vanilla potion stacks before the vanilla item conversion splits drinkable and splash bottles. Pending `WITCPoSpawn` records are Witchery-owned and can still be repaired from the captured table after the player save reaches the current data version.
- Earlier port saves can retain unnamed numeric `ActiveEffects` in player files, saved mounts, and entity regions. Ordinary load resolves those entries through the captured potion table, including passengers. Existing `forge:id` values remain authoritative. Newly mapped effects apply their current attribute modifiers, which persist with the named effects in Forge's normal save format. Recover an effect that was already resaved with the wrong name from the original record.
- `WitcheryPlayerData` reads the original creature form, alternate skin, blood stores, vampire and werewolf ranks, potion bottling progress, vampire vision and trial counters. Village records retain their packed `Location` and default to the Overworld when the old record has no dimension. Wolf trial chunks retain each signed `Location`. `PotionThrowing` is retained as dormant save data. The wolf's `LastHowl` and `LastBoneFind` use wall-clock milliseconds for the original one-minute cooldown. Earlier port saves honor their `LastHowlGameTime` and `LastBoneFindGameTime` through the next use of each ability.
- The legacy `MirrorWorldEntryPoint` moves to `ForgeData.PlayerPersisted` when the imported player is inside Mirror. Both escape cooldowns retain their original world tick. Existing modern saved entries and timers take precedence.
- An inhabited Mirror reads the original `Favorite` UUID in string form and preserves the profile's properties and signatures across block and item saves. A conversation with the same player refreshes properties supplied by that player and retains the remaining saved properties.
- A portable 1.7.10 Mirror can reconnect to its original room when the old exterior is gone. The room saves the new exterior position and both frames share a connection UUID. A frame already remounted by an earlier port can repair the link when first used. An existing reciprocal frame or an active modern connection keeps its room.
- See Legacy Save Fixtures in `PORTING.md` for the imported vampire, wolf and Dream records verified in the packaged game. The broader comparison of legacy player fields and multiplayer packet behavior remains in the release checklist.

## Mod Hook Integrations (`modHooks.register`)
Source: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/Witchery.java`
- `ModHookArsMagica2` maps its mana debit and suffocation exception to Ars Magica Legacy 1.5.0, the build in Auram. The modern API provides mana access for living entities. The inspected 1.5.0 build contains no identified equivalents for the Reconstructor or its Soulbound enchantment hook.
- `ModHookBloodMagic` maps to Blood Magic 3.3.5-47. Powered Anti-Magic Bolts debit Life Essence, Carnosa Diem trades health for Life Essence, and a vampire using the Sacrificial Dagger pays blood power when its reserve covers the altar offering.
- `ModHookForestry` had empty initialization and magic-drain methods in 0.24.1. The Witch's Oven separately read the active species from `Genome.Chromosomes[0].UID0`. Oak, spruce, and birch retain their original fumes. Forestry CE saplings use the active species supplied by its individual capability when optional integrations are enabled. Other genetic species produce Wood Ash and leave the jar intact. JEI lists the Forestry variants under their actual results. [Forestry CE 2.11.2](https://modrinth.com/mod/forestry-community-edition/version/2.11.2)
- The original Witch's Oven special case for `tile.bop.saplings` at metadata six selects Biomes O' Plenty Fir. Auram's Biomes O' Plenty 19.0.0.96 registers this variant as `biomesoplenty:fir_sapling`. The oven produces Wood Ash and Hint of Rebirth, and JEI shows the same outputs. The legacy importer resolves named and numeric Fir item stacks when the modern item is registered. [Original Biomes O' Plenty sapling metadata](https://github.com/Glitchfiend/BiomesOPlenty/blob/fc7f1f6392a16224d19538900acedd152ff4f350/src/main/java/biomesoplenty/common/blocks/BlockBOPSapling.java#L26)
- `ModHookMineFactoryReloaded` maps to Industrial Foregoing. The Sower preserves the three saved sapling variants. Forge's crop and bonemeal interfaces and the log and leaf tags cover the eight crops and three trees. The Crusher, Imprisonment Tool, and Duplicator use their respective entity type tags for the 29, 25, and 30 original exclusions. Witchery makes the Crusher apply its full exclusion tag before selecting a target. [Industrial Foregoing 3.5.21](https://www.curseforge.com/minecraft/mc-mods/industrial-foregoing/files/7068937)
- `ModHookMystCraft` excludes Flowing Spirit, Hollow Tears, Brew, liquid brew and brew gas from automatic sea symbols in Auram's Mystcraft 2 Legacy 0.8.0. Mystcraft still reads server-authored symbol definitions and registers the other fluids.
- `ModHookThaumcraft4` maps to Thaumaturge 1.1.3 in Auram. The integration drains carried wands and supplies the base item and entity aspects through Thaumaturge's data maps. The modern Harvest Seal recognizes Witchery crops through `CropBlock.isMaxAge`. Dream and Torment preserve the original dimension exclusions for features, structure starts, initial aura, and natural spawning. Thaumaturge's item aspect lookup reads the current stack variant for saplings, Wicker Bundles, mines, Brambles, and Circle Talismans.
- `ModHookTinkersConstruct` offset the Bark Belt icons for its legacy replacement health display. Auram's Tinkers' Construct 3.11.2.166 has a hotbar overlay for equipment. The port places Bark Belt charges above the current health and armor rows using Forge's `leftHeight` counter.
- `ModHookTreecapitator` maps to the optional Enhanced Falling Trees 0.6.0. Log and leaf tags expose the three species to the felling mod. Witchery's wood state distinguishes adjacent species, and harvesting each log can still awaken an Ent. [Enhanced Falling Trees 0.6.0](https://www.curseforge.com/minecraft/mc-mods/enhanced-falling-trees/files/5899084)
- `NEIWitcheryConfig` maps the five machine recipe handlers to the optional JEI plugin. The Oven uses the same eligible outputs and fume resolver as gameplay. The Distillery shows its simultaneous outputs and jar count. The Kettle shows bottling requirements, the Cauldron identifies the final offering and summoned entity, and the Spinning Wheel shows fibre quantities and modifiers. JEI distinguishes the three Witchery wood variants when looking up inputs.
- JEI's vanilla crafting category also displays Witchery's custom shapeless recipes. The optional extension links each example's inputs and output so changing the focus retains the corresponding taglock signature, book page count, Urn tier, potion, dye or biome.
- `ModHookWaila` maps to the optional Jade plugin on Forge 1.20.1. It presents the original cover identities for trapped plants, Alder Doors, both pit blocks, the three illusions, and werevillagers. Rowan Doors use Jade's normal item lookup.
- Right Click Harvest 4.6.1 and Quark 4.0-462 leave the wheat interaction to the held Mutating Sprig when all four cotton plants and all four diagonal water sources satisfy the original Wormwood pattern. Ordinary crop harvesting follows the installed mods' behavior for other clicks.
- Psychedelicraft 1.20.1-port.4 skips its target modifier lookup when a mob still refers to a disconnected player. Forge has already invalidated that player's capability by the time the stale reference is ticked.
- Ars Magica Legacy 1.5.0 and L2 Library 2.5.1 preserve Forge player capabilities while the player remains in the world. Their temporary capability lookups still restore invalidation after actual removal. This keeps Thaumaturge and Psychedelicraft state available when a player rejoins Auram before respawning.
- `ModHookMorph` kept the original first-person renderer swap from running while a player had a Morph shape. Woodwalkers is the Forge 1.20.1 counterpart. Witchery synchronizes its Resizing scale with the live Woodwalkers shape, refreshes collision bounds when it changes, and lets Woodwalkers render the shape and determine its camera height. The scale also applies to Woodwalkers' pose-specific eye-height query for crouching humanoids. [Woodwalkers 5.8.13 for Forge](https://modrinth.com/mod/woodwalkers/version/1.20.1-forge-5.8.13)
- `ModHookBaubles` uses the optional Curios inventory on Forge 1.20.1. The `witchery:vampire_mortality_accessories` item tag contains the three surviving Botania accessories. Botania removed the Ring of the Aesir. [Botania changelog](https://botaniamod.net/changelog.html)

The original `AllowModIntegration` gates `ModHook.init`. On Forge 1.20.1, `allowModIntegration` in `config/witchery-common.toml` controls the corresponding hooks as well as their contributed Thaumaturge aspect maps and Industrial Foregoing tags. The built-in `witchery:mod_integration` pack keeps a stable ID across restarts and exposes its data when integration is enabled. The client also honors `allowModJEI` in `config/witchery-client.toml` as the counterpart of `AllowModNEI`. Both options default to true. The other legacy per-mod settings have no readers outside the original configuration class. Forge's vanilla tags and cross-mod crash fixes operate independently of the switches.

## Worldgen Registry Touchpoints
- World generator root: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/worldgen/WitcheryWorldGenerator.java`
- Overworld structure handlers used by generator:
  - `WorldHandlerWickerMan`
  - `WorldHandlerCoven`
  - `WorldHandlerShack`
  - `WorldHandlerClonedStructure(ComponentGoblinHut)`
- `witchery:surface_structures` is a Forge placed feature with one candidate chunk in each region of `8 + worldGenFrequency` chunks. The default frequency is 12 and the salt is 10387312. The seeded choice allocates one quarter of candidates to each original structure. An enabled structure can take over when another choice is disabled or its terrain is unsuitable. The earlier `witchery:wicker_man` feature remains available to datapacks. Surface placement evaluates the original biome exclusions with current tags.
- The feature builds the legacy hay effigy, grass and poppies or sand and dead bushes, and a vanilla spawner under one foot. The active chunk generator supplies the sea level for foundation height. `strawmanSpawnerRules` accepts modern entity IDs and old vanilla names. Repeated entries retain their weight and missing choices fall back to zombies.
- `generateWickerMen` and `worldGenInTwilightForest` default to true. Generation checks the actual dimension, including when a custom dimension reuses Overworld biomes. Twilight Forest biomes are included through its optional tag. [Twilight Forest 1.20.1 biome tag](https://github.com/TeamTwilight/twilightforest/blob/1bc3a4c21213bc443967e92125613cb9ef47891e/src/generated/resources/data/twilightforest/tags/worldgen/biome/in_twilight_forest.json)
- `generateCovens` and `generateShacks` default to true. The 11-block coven circle has two vanilla witch spawners, a water basin and an east-facing dispenser. New dispensers use the same `witchery:chests/mineshaft_corridor` pool as Buried Treasure. They resolve their saved seed on first inventory access through the modern container lifecycle. Each roll chooses one of the dispenser's nine slots, and a subsequent roll can replace an earlier stack. Dispensers saved by earlier port builds retain their existing table and seed or their stored items. The circle's sandstone variant follows the old desert palette. The 7-block shack keeps its rotated door, spruce roof, connected windows and `witchery:chests/witch_shack` loot table. The shack table uses the original eight entries and one to three rolls.
- The coven circle and shack can generate one persistent coven witch with their original 90% and 75% chances. Their generation checks whether the site is dry at the four corners.
- `generateHobgoblinHuts` defaults to true. The fourth regional choice uses the original 7-by-7-by-6 schematic and spawns one persistent `witchery:goblin` with a profession from zero through three. Wild Hobgoblins may use all five professions. Network protocol 66 carries the restored entity's synced profession and mining animation.
- Village handlers bootstrapped in `Witchery.preInit`:
  - `WorldHandlerVillageApothecary`
  - `WorldHandlerVillageWitchHut`
  - `WorldHandlerVillageBookShop`
  - `WorldHandlerVillageDistrict`
- `witchery:village/bookshop` is a 10-by-8-by-9 template with the modern `minecraft:building_entrance` connector. The village pool hook appends one weight-15 entry to each of the five ordinary house pools. Jigsaw assembly limits each village to one shop. This preserves the ordinary legacy limit. The old custom world-size exception for a second shop has no equivalent in standard 1.20.1 village generation.
- `generateBookShops` controls new village assembly. `townBookshopAllowedBooks` accepts extra registered item IDs and the old `id,metadata` form for recognized Witchery subtypes. The processor fills the original chest and four frames during placement. The chest rolls five to ten entries and guarantees `witchery:vampirebook`. The original three Torn Page entries have weights three, two, and one. Frames draw uniformly from the catalogue entries.
- `witchery:village/apothecary` is a 9-by-9-by-8 template with the original sign, furnishings, and chest beneath the filled cauldron. Its pool weight is 15 in all five ordinary village styles. `generateApothecaries` defaults to true, and the one-per-village limit is independent of the bookshop. Current villages use the ordinary legacy cap of one.
- The registered `witchery:apothecary` profession uses the birch-styled `witchery:apothecary_shelf` at the original shelf position. The generated resident spawns with all eight registered trade choices. Each starts with seven uses and can restock at the shelf. A shapeless recipe using one Birch Slab and one Glass Bottle lets players provide the modern workstation in an imported shop. The chest uses `witchery:chests/village_apothecary`, with the original two to five rolls, weights, and stack sizes.
- The opt-in importer maps the original numeric Apothecary profession to `witchery:apothecary` for vanilla villagers, cursed villagers, and a Wolfman's saved villager. It uses 2435 by default and reads custom `I:ApothecaryVillagerID` values via `-Dwitchery.legacyConfig=/path/to/witchery.cfg`. The world snapshot keeps the mapping for later chunk upgrades. The importer preserves existing stock and usage counts, supplies the current resident catalogue when no offers were saved, and grants a minimum of one villager XP to keep the profession while the villager looks for a shelf.
- `witchery:village/witch_hut` is a 4-by-6-by-6 template. The legacy 4-by-5 footprint gains a front approach step for its road connector. The furnishings are a water cauldron, Rowan log, and potted spruce. The template spawns one persistent `witchery:covenwitch`. The seeded village limit is zero or one, and the hut has weight 10 when eligible. `generateWitchHuts` defaults to true.
- Auram's Trek B0.6 datapack gives its villages separate house pools. The village hook uses the existing `minecraft:building_entrance` connector for the Plains, Desert, Savanna, Snowy, Taiga, Swamp, and Cherry Trek house pools. Each accepts the Witchery buildings and the configured weights, per-village limits, and style-specific processors.
- Coven Witches and unleashed Hobgoblins use occupied village POIs to find settled areas. Sheltering examines the covered side of actual wooden doors and uses the usual door-opening pathfinder, so a hut with no bed can provide shelter. The daytime door restriction applies to both species.
- `witchery:village/keep` uses a 16-by-27-by-16 template that includes the legacy keep's top clearance. `townKeepMode` defaults to one, which gives each start a seeded zero-or-one limit at weight 100. Mode two permits one keep wherever the village geometry fits it. Mode zero disables the building. Its ten-entry chest rolls three to eight times. Six guards spawn at the courtyard and east rampart posts.
- `witchery:village/watchtower` uses the 8-by-23-by-8 legacy layout. The `townGuardTowerCluster` settings default to four entries of weight 20 and independently roll a limit of zero or one for each. Each entry stops competing once its own quota is filled. Its eighteen-entry chest rolls two to five times, and four armed guards spawn on the upper lookout.
- The village buildings share the `witchery:village_foundation` processor. Each processor list sets the original support footprint. The keep and watchtower use cobblestone, sandstone, or snow block foundations according to the village style. Desert pieces convert their stonework to sandstone and plankwork to birch. Snowy pieces restore snow construction, with packed ice supports and perpetual ice fence.
- `townWallMode` and `townWallWeight` restore the wall's defaults of seeded eligibility and weight 100. The village roads assemble with a 2-by-7-by-2 anchor. The start replaces it with one saved `witchery:village_wall` piece containing the exterior contour, gate columns, elevations, material palette, and guard posts. Each generating chunk places its part of the same plan. The old `wallgen` ID remains registered for existing data.
- The district footprint extends 20 blocks sideways and seven blocks past street ends. Modern building bounds are included with a small margin. The wall closes short gaps and forms three-wide openings aligned with outward roads. Stone bricks, cut sandstone, and snow blocks match their corresponding legacy palettes. Stair parapets and gate trim follow the relevant wall material, and snowy gate railings use perpetual ice fences.
- The `witchery:village_biomes` Forge structure modifier uses the existing Overworld and village tags. Sandy and badlands additions use desert villages. Snowy, savanna, and taiga additions use their respective styles, with plains as the general land style. Each `townBiome…Allowed` server setting applies to additions. Jungle defaults to false and supports an explicit opt-in. Existing vanilla and datapack structure memberships continue to supply their original eligibility.
- Vanilla building groups use the legacy `town…ClusterGroups`, `Weight`, `Min`, and `Max` settings. The modern equivalents are a small house with a yard for `GardenHouse`, libraries for `House`, other small houses for `WoodHut`, butcher shops for `Hall`, and medium or large homes for `House3`. `SingleField` follows legacy `Field1` and uses the larger plot. `DoubleField` follows `Field2` and uses the smaller plot. `Blacksmith` uses weaponsmiths and `Church` uses temples. Copies keep the original pool element type, projection, and processors. The 1.7.10 Forge registry replaced repeated handlers for the same class. The modern port honors each configured group with its own quota.
- `townZombieAttackReductionMode` defaults to one. Mode zero uses vanilla damage. Modes one and two set zombie damage to a villager to 0.5 health points, and mode two blocks zombie targeting of villagers. Golems ignore Witchery village guards.

## Tile Entity Registration Surfaces
- Auto-registration path: `BlockBaseContainer.func_149663_c` -> `GameRegistry.registerTileEntity(clazzTile, blockName)`.
- Protocol 92 removes six placeholder block-entity registrations. The Candelabra, both Fume Funnels, and Garlic Garland use their placed blocks and models. The original village wall marker kept its pending plan in memory, and its saved tile contains no plan to import. Both original and earlier 1.20.1 saves load `witchery:wolftrap` tiles through the shared `witchery:beartrap` type used by new traps.
- The altar keeps the original `CoreX`, `CoreY`, and `CoreZ` links and the core's power and `EnhancementLevel`. A Mystic Branch mounted on any member adds one enhancement. The original source exposes that value without using it to alter spells. Existing modern altars without core links still resolve to their previously selected coordinate.
- Placement and removal recalculate the connected group immediately. An invalid group clears the loaded members' formed state, links, and charge. A valid placement chooses its newly placed block as the core. A removal that leaves six eligible blocks reforms around the first member reached from the removed cell in north, south, east, west order.
- The core scans loaded chunks within 14 blocks on formation and when opened more than 100 loaded ticks after the last scan. Multiple opens on the scan's own tick can also rescan, as in 0.24.1. Artifacts refresh when changed or when the altar opens. Placing an Arthana, Mystic Branch, or Pentacle updates the altar after the mounted item is stored. The core gains ten power per recharge multiplier every 20 loaded ticks. Excess charge after a capacity change lasts until the next recharge, and the recharge phase starts fresh on load.
- The read-only Altar screen uses Forge's menu flow. With Adapt 2.2.7 installed, it skips Adapt's inventory-opening hook, which requires a backing inventory. Witchery menus that expose inventory slots use the normal hook.
- Altar capacity groups the modern blocks derived from the original sapling, log, leaf, dirt, tallgrass, red flower, and pumpkin IDs. Flattened mushroom stems draw on unused slots in the two original giant mushroom caps because both old stem variants load as the same block.
- Standalone water contributes one point per block, up to 50. The altar checks the chunk's fluid tick queue to recognize settled water at every level, matching the original still-water block.
- Manual registration found:
  - `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/blocks/BlockRefillingChest.java`

### Block classes with direct `TileEntity*.class` constructor binding
| Class | Tile Entity Class |
|---|---|
| `BlockAlluringSkull` | `BlockAlluringSkull.TileEntityAlluringSkull` |
| `BlockAltar` | `BlockAltar.TileEntityAltar` |
| `BlockBarrier` | `BlockBarrier.TileEntityBarrier` |
| `BlockBeartrap` | `BlockBeartrap.TileEntityBeartrap` |
| `BlockBloodCrucible` | `BlockBloodCrucible.TileEntityBloodCrucible` |
| `BlockBloodRose` | `BlockBloodRose.TileEntityBloodRose` |
| `BlockBrazier` | `BlockBrazier.TileEntityBrazier` |
| `BlockBrewGas` | `TileEntityBrewFluid` |
| `BlockBrewLiquidEffect` | `TileEntityBrewFluid` |
| `BlockButtonBase` | `TileEntityCursedBlock` |
| `BlockCandelabra` | `BlockCandelabra.TileEntityCandelabra` |
| `BlockCauldron` | `TileEntityCauldron` |
| `BlockChalice` | `BlockChalice.TileEntityChalice` |
| `BlockCircle` | `BlockCircle.TileEntityCircle` |
| `BlockCoffin` | `BlockCoffin.TileEntityCoffin` |
| `BlockCrystalBall` | `BlockCrystalBall.TileEntityCrystalBall` |
| `BlockDemonHeart` | `BlockDemonHeart.TileEntityDemonHeart` |
| `BlockDistillery` | `BlockDistillery.TileEntityDistillery` |
| `BlockDoorBase` | `TileEntityCursedBlock` |
| `BlockDreamCatcher` | `BlockDreamCatcher.TileEntityDreamCatcher` |
| `BlockFetish` | `BlockFetish.TileEntityFetish` |
| `BlockFumeFunnel` | `BlockFumeFunnel.TileEntityFumeFunnel` |
| `BlockGarlicGarland` | `BlockGarlicGarland.TileEntityGarlicGarland` |
| `BlockGrassper` | `BlockGrassper.TileEntityGrassper` |
| `BlockKettle` | `BlockKettle.TileEntityKettle` |
| `BlockLeechChest` | `BlockLeechChest.TileEntityLeechChest` |
| `BlockLeverBase` | `TileEntityCursedBlock` |
| `BlockMirror` | `BlockMirror.TileEntityMirror` |
| `BlockPlacedItem` | `BlockPlacedItem.TileEntityPlacedItem` |
| `BlockPoppetShelf` | `BlockPoppetShelf.TileEntityPoppetShelf` |
| `BlockPressurePlateBase` | `TileEntityCursedBlock` |
| `BlockSilverVat` | `BlockSilverVat.TileEntitySilverVat` |
| `BlockSlurp` | `BlockSlurp.TileEntitySlurp` |
| `BlockSpinningWheel` | `BlockSpinningWheel.TileEntitySpinningWheel` |
| `BlockStatueGoddess` | `BlockStatueGoddess.TileEntityStatueGoddess` |
| `BlockStatueOfWorship` | `BlockStatueOfWorship.TileEntityStatueOfWorship` |
| `BlockStatueWerewolf` | `BlockStatueWerewolf.TileEntityStatueWerewolf` |
| `BlockVoidBramble` | `BlockVoidBramble.TileEntityVoidBramble` |
| `BlockWitchesOven` | `BlockWitchesOven.TileEntityWitchesOven` |
| `BlockWolfHead` | `BlockWolfHead.TileEntityWolfHead` |
| `WorldHandlerVillageDistrict.Wall.BlockVillageWallGen` | `WorldHandlerVillageDistrict.Wall.BlockVillageWallGen.TileEntityVillageWallGen` |

### Special-case dynamic tile bindings
- `BlockAreaMarker` constructor accepts tile class and is instantiated twice in `WitcheryBlocks`:
  - `TileEntityAreaTeleportPullProtect` (`DECURSE_TELEPORT`)
  - `TileEntityAreaCurseProtect` (`DECURSE_DIRECTED`)

## Runtime Singleton Registries (non-Forge)
- `RiteRegistry.instance()`
- `PredictionManager.instance()`
- `Infusion.Registry.instance()`
- `CreaturePower.Registry.instance()`
- `KettleRecipes.instance()`
- `DistilleryRecipes.instance()`
- `SpinningRecipes.instance()`
- `WitcheryBrewRegistry.INSTANCE`
- `EffectRegistry.instance()`
- `PowerSources.instance()` (client/server split instance)
- `BlockAreaMarker.AreaMarkerRegistry.instance()` (client/server split instance)

## Notes For 1.20 Port Setup
- Legacy metadata-heavy content (`ItemGeneral`, `ItemPoppet`, slabs, brew variants) needs explicit 1.20 representation strategy.
- Preserve legacy names as migration aliases where practical.
- Expect dimension and worldgen systems to be redesigns, not direct ports.

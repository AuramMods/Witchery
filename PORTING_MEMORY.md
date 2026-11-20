# READ THIS FIRST EVERY SESSION
Always read this file first, then check `/Users/cyberpwn/development/workspace/AuramMods/Witchery/PORTING.md`, then `/Users/cyberpwn/development/workspace/AuramMods/Witchery/PORTING_MANIFEST.md` before making porting changes.
Validation workflow for this project should use `./gradlew compileJava` (do not rely on `runClient` during routine scaffolding checks).

## Session Memory (2026-11-19)

- The playable port starts with the Witch's Oven. The original recipes are in `old-1.7.10/com/emoniph/witchery/WitcheryRecipes.java` and the machine behavior is in `blocks/BlockWitchesOven.java`.
- Treat the earlier phase snapshots as history. `runData` now runs against Witchery and Forge by default, writes run logs under `build/run-data`, and generates the packaged datapack under `src/generated/resources`.
- Use `-PwithAuramMods` to add the bundled Auram development mods to a launch. Use `./gradlew jar` for packaging because `build` automatically deploys to the local Auram instance.
- Use a disposable, headless server for gameplay checks. Launch client tests with zero master volume in a background or isolated test session.
- Oven-related ingredients use stable individual IDs through `WitcheryIngredient`. The manifest records the old damage-to-ID mapping. The original `witchery:ingredient` ID remains registered for unconverted stacks.
- `WitcheryRecipeProvider` generates the oven, clay jar, wood ash, witch log charcoal, attuned stone, and funnel recipes. Sapling smelting follows the legacy default of using the shared sapling tag. Charged attuned stones still depend on restoring the charging rite.
- `WitchesOvenBlockEntity` provides the oven's five slots and persists burn, cook, and inventory state. Headless server checks passed for the seven fume outputs, acacia ash with the jar left intact, restart progress, and known and unknown ingredient metadata fixtures.
- The oven reads legacy ingredient metadata after the base item ID is resolved to `witchery:ingredient`. General conversion of numeric 1.7.10 save IDs still needs its own verified workflow.
- A packaged Forge client with master volume zero and the OpenAL null driver can join the isolated server under Linux Xvfb. Survival crafting, firing jars, oven placement, shift-click input and output, the updated oven model, and wood-variant plank recipes were verified there.
- Rowan, alder, and hawthorn use the existing `wood_type` state and retain the existing four family IDs. Non-rowan inventory stacks carry `BlockStateTag.wood_type`. Growable saplings use modern configured trees, with straight trunks for rowan and narrower and broader fancy trunks for alder and hawthorn.
- The initial source of Witchery saplings is Mutandis. Craft Anointing Paste from artichoke, belladonna, mandrake, and snowbell seeds and use it on a vanilla cauldron. Add three buckets of water, heat it with fire for five seconds, then drop in mandrake root and Exhale of the Horned One in either order. An egg added last starts the 210-tick ritual, which yields six Mutandis. Its water and ritual state survive a restart.
- Ordinary Mutandis plant choices come from the `witchery:mutandis_plants` block tag plus all three Witchery sapling variants. Potted plants use the original pot pool. General cauldron brews and arbitrary legacy cauldron NBT import still need porting.
- The muted survival client showed the restored cauldron basin and water surface. It crafted and used the paste, emptied three water buckets, dropped in the ingredients, picked up the six Mutandis, and used one to mutate oak into alder.
- The client harvested the mutated alder sapling and retained its wood variant. Potted plants run Mutandis before vanilla's pot interaction so they mutate in place. The isolated survival client verified the mutation and item consumption.
- The altar forms when six adjoining blocks make a valid 2-by-3 rectangle. The core is the first member in position order. It scans loaded chunks within 14 blocks for plant and natural block contributions and supplies nearby machines within 16 blocks. The recipe requires two rowan logs, four stone bricks, a water bottle, and the birch and oak fumes.
- The altar's original menu shows live power, capacity, and recharge multiplier. Its structure and power storage passed 15 production Forge checks and were verified in the muted multiplayer client. Skull and torch bonuses work. The Mystic Branch and Koboldite Pentacle still need their item and placement behavior.
- The distillery uses the nearest altar in range and spends 0.6 power per tick for 800 ticks. It accepts either order for two ingredients and shows up to four clay jars on its model. A batch preserves its progress through power loss and a restart. A blocked output stops the batch with its inputs intact. Existing idle and burning block and block-entity IDs are supported.
- All nine original `witchery:distilling` recipes are in `data/witchery/recipes/distilling`. The last two accept the old port's Demon Heart ID as well as the canonical heart. Quicklime crafts from Wood Ash. Dropping gypsum into a filled Witch's Cauldron empties it, including when it is cold.
- Ritual Chalk crafts from Wood Ash, gypsum, and Tear of the Goddess. The three colored chalk conversions use Ritual Chalk as the final cauldron ingredient. Golden Chalk uses a mandrake root and a gold nugget. Otherwhere uses nether wart, Tear of the Goddess, and an ender pearl. Infernal uses nether wart and blaze powder. The powered rituals wait for the nearest altar to meet the cost and use the legacy cauldron circle discounts.
- All twelve appearances for each white, purple, and red glyph use the supplied texture assets. Chalk draws on solid surfaces and the glyph disappears if its support is removed. The chalk sound is carried over. Legacy misfortune and coven rules for powered cauldron rituals still need their gameplay systems.
- The Heart Glyph uses the original small and medium rite rings of 16 and 28 glyphs. These rings differ from the 12- and 24-glyph cauldron rings. Right-clicking the heart or pulsing it with redstone starts the matching rite. The first rite consumes an Attuned Stone, glowstone dust, redstone, wood ash, quicklime, and 2,000 altar power. Its staged offerings persist on restart and return when cancelled or when power is insufficient.
- Arthana has gold-tier combat attributes and iron durability. Use it on top of an altar to mount it and double the altar range from 16 to 32 blocks. The placed item uses the original `WITCPlacedItem` tag, persists damage and other item tags, and drops the original stack on removal. It also boosts creature ingredient drops on direct kills. Mob tags define the bats and canines for this purpose.
- The disposable Forge GameTests covered Arthana durability and placement, direct player kills with Looting and the legacy no-drop flag, stacked chalk drawing, the Rite of Charging use interaction, and two Mutandis Extremis tests. Extremis brews from nether wart followed by ordinary Mutandis. It preserves applicable crop ages, toggles grass and mycelium, and can turn submerged dirt and its four neighbors into clay.
- The kettle retains `witchery:kettle` for the block, item and block entity. The Forge fluid handler accepts a bucket of water. Six thrown ingredient stacks form an unordered recipe when heated by fire underneath. A completed 1,000-power batch drains the altar once. One manual glass bottle takes one portion, and automation draws bottles from the kettle's buffer. The last portion empties the kettle and preserves any spare bottles.
- The kettle recognizes legacy `Items`, `Ruined`, `LiquidColor`, and flat water `FluidName` and `Amount` fields after legacy item IDs have been normalized. Partial and finished batches, water, and empty bottles persisted across a restart. All 18 production checks passed. Forge's fire tag also accepts soul fire.
- Kettle recipes live in `data/witchery/recipes/kettle`. Brew of Ink and Brew of the Depths yield three portions with no altar charge. Redstone Soup yields one. Its Drop of Luck is made in the Witch's Cauldron using mandrake root, nether wart, Tear of the Goddess, Refined Evil, and a Mutandis Extremis trigger. The base cauldron cost is 1,800 before the circle multiplier. Kettle coven and equipment bonuses and the remaining recipe effects are pending.
- The muted multiplayer client crafted, placed, heated, filled and bottled a kettle using the regular survival controls. It threw the completed Brew of Ink and blinded a nearby husk for less time than the direct target. A supplied Brew of the Depths gave Wither II on land, then removed it while maintaining air underwater. Testing used supplied starting materials. All nine disposable Forge GameTests passed.
- The empty Chalice and Candelabra retain their previous block and item IDs. Redstone Soup crafts a separate `witchery:filled_chalice` item that places the same chalice block with `filled=true`. The first chalice found by the altar adds one to capacity if empty or two if filled. One Candelabra adds two to recharge and emits light level 15. Legacy `WITCFilled` data sets the new block state when the chalice loads. Twelve packaged checks, two more Forge GameTests, and crafting and placement in the muted client passed.
- The Spinning Wheel retains `witchery:spinningwheel` and the `witchery:spinning_wheel` menu. It costs 0.6 altar power per tick for 300 ticks and retains progress when the altar stops supplying it. Hopper fibre goes in the top, modifiers go in any of the three original slots, and output extracts horizontally. The `witchery:spinning` data has all four original recipes.
- Fifteen packaged spinning checks, twelve disposable Forge GameTests, the bundled Auram development smoke test, and the muted survival client's manual wheel craft and Golden Thread batch passed. The installed Prism Auram profile uses Forge 47.4.10 and contains 538 enabled mod JARs. It still needs its own isolated packaged smoke test.
- Dream entry ingredients are available. Mature snowbell has a separate 20% Icy Needle drop. The original milk-bucket and cake recipes make three Purified Milk jars. Drinking a jar has a 50% chance to clear one active effect. The unpowered Brew of Love kettle recipe yields three throwable brews. Its splash resets the breeding cooldown for adult animals and pairs adult villagers without a bed search. The enslaved zombie interaction remains pending. Six packaged checks and the complete 14-test Forge suite passed.
- The original Brew of Sleeping recipe yields three drinks. Drinking it in the Overworld leaves a sleeping body and switches to the player's Dream inventory. Icy Needles and Mutandis carry in. Icy Needles and both cotton drops carry out. Other items stay with their respective inventories. The transition gives the opposite item a one-second cooldown so holding use cannot immediately undo the transition. The previous direct spirit portal shortcut is removed while real manifestation is pending.
- Somnian Cotton generates only in the Dream and spreads near source Flowing Spirit. It drops Disturbed Cotton while a player there is having a nightmare, and Wispy Cotton otherwise. The clean packaged server generated both dimensions, found natural cotton only in the Dream, and verified a Wispy Cotton drop. The Forge suite passed all 15 tests, including a seeded spreading check.
- The muted multiplayer client brewed three Sleeping brews by hand. On a packaged server, the player restarted during a nightmare with both inventories and the sleeping body intact. The Icy Needle returned the player to the body with the original health, damaged Overworld gear and harvested Disturbed Cotton. The next entry recovered the separate Dream inventory. Body destruction and lethal Dream damage both woke the player. Large position changes occasionally logged a vanilla movement warning during the dimension transition.
- The ordinary Nightmare retains `witchery:nightmare`. It spawns near a player with an active nightmare, persists the victim by name and UUID, and drops Mellifluous Hunger in the Dream. The original defense, damage cap, armor swipe, Charm of Disrupted Dreams, model and sounds are present. The 17-test suite passed, and the muted client verified natural spawning, blocked follow-up damage, and returning with the dropped ingredient. Huntsman's Spear is recognized for the defense reduction. Its source and own weapon behavior still depend on the Horned Huntsman port.
- Dreamweaves use horizontal facing and five weave variants on `witchery:dreamcatcher`. The original `WITCWeaveID` restores the variant during ticking. Breaking the support drops the matching item. All five original recipes are available. The muted client crafted and hung the Dreamweave of Nightmares and confirmed its interaction with the Fleet Feet weave when using a bed. With the Nightmare weave, two cotton plants and three Flowing Spirit sources, a Sleeping brew produced a normal dream. The player harvested and took home both cotton variants in separate trips, added three supplied fibre to each batch, and collected both original threads from powered wheels.
- Brew of Flowing Spirit uses the Dream-restricted kettle recipe and carries across waking. The distillery accepts it and Oil of Vitriol with two clay jars. The two throwable fluid brews place the source at impact and apply the original Regen or Weakness effects on contact, subject to vanilla undead effect immunity. Flowing Spirit converts dropped Disturbed Cotton to Wispy Cotton and preserves its stack count. Registered fluids now have client sprite bindings, which resolved the earlier tesselation crash. Fourteen packaged checks passed. The muted client bottled the brew, woke with it, and threw it to make the green pool.
- A two-by-two interior of a snow-block frame ignites in the Dream when a Flowing Spirit source hits its bottom. The portal can use either horizontal axis and collapses if its frame is broken. The 1,000-power Necromantic Stone rite requires night. The 5,000-power Manifestation rite uses the small Otherwhere ring with six offerings and returns the Necromantic Stone. It grants 150 seconds to players in range and refunds offerings if nobody is there. Covens and the stone's undead control are pending.
- Enter an active spirit portal with manifestation time to visit the Overworld as a ghost. The sleeping body remains, the Dream inventory is parked, and only Icy Needles come through. Returning drops ordinary acquired items, restores the Dream inventory, and preserves the remaining timer. Needle return, countdown, restart during manifestation, expiry, and the final wake with an original damaged shield and diamonds all passed in the muted client. Arrival prefers safe footing near the anchor when its immediate column is obstructed.
- Both Demon summons need a 40-glyph Infernal ring and 3,000 altar power. The first route takes a villager from within the ring. The second uses a blaze rod and both Attuned Stones in place of the blaze powder and villager. Obstructing the summon or omitting the required villager returns the offerings. The packaged headless checks covered both rites.
- A Demon trades two Demon Hearts at most. Its generated offers and remaining stock are saved in `Bargains`. Devil's Tongue lowers eligible costs and loses five durability per completed bargain. The Demon takes at most 15 damage per hit and can throw large fireballs. Blaze-rod and magma-cream bargains can make it explode after 50 ticks. Animal trading through the legacy charms remains pending.
- A Demon Heart can be eaten or sneak-placed to pulse and shed light. Breaking the block drops the canonical ingredient. Its original ingredient metadata is 23 and Infernal Blood is 80. The Wicker Bundle's interaction with Infernal Blood is still pending.
- The muted multiplayer client activated the no-villager rite, saw the original Demon model, and bought its stock of two hearts for two magma creams each while holding Devil's Tongue. It placed both hearts and drank a Sleeping brew beside the legacy demonic setup. Both fireball sizes, an ordinary Nightmare and a Demon spawned naturally. The fight pushed the player off the first elevated test platform and the server disconnected them during the fall. With a wider platform, the player stayed connected, woke with an Icy Needle, and the remaining Dream Demons despawned. The three Dream atmosphere variants still need work.
- Bone and flint craft eight Bone Needles. A needle and glass bottle craft one Taglock Kit. Use a kit on a creature or a player's registered vanilla bed, sneak-use at a bed for a self sample, or approach a player directly with the old facing and sneak odds. Collecting from a stack fills one kit. Creature samples use the original UUID halves and name, and player samples preserve the original name with the UUID saved alongside it. The muted client crafted and collected these samples. Coffins, Blood Roses, Leech Chests, and Crystal Ball scrying still depend on their gameplay systems.
- Brew of Grotesque yields three drinks from its 500-power kettle recipe. Drinking one applies the four-minute repulsion and Demon mask. The model uses the player head layer, and the effect migrates remaining legacy `witcheryGrotesque` time on login. The muted client bottled the original brew and verified the effect and rendering.
- Curse of Waking Nightmare uses a medium Infernal ring during thunder and 10,000 power. The cure uses a small white ring and 2,000 power. Both require a filled Taglock Kit and return the staged items when the target cannot be resolved. The rite writes the legacy `witcheryWakingNightmare` value. Waking Nightmares are assigned to their victim, share the original outside-Dream damage and defense, and cannot accumulate around one afflicted target. The curse's familiar and coven bonuses and protections are still pending.
- The packaged server validated empty and legacy name-only taglocks and refunded the five offerings when a legacy target was offline. The muted client used its own collected taglock to apply the curse, then retained it through death and a server restart. A supplied status effect spawned a Waking Nightmare, which remained after the effect was removed while the rite curse persisted. A second self taglock and the cure ritual removed the curse and Weakness, and the Nightmare despawned. All 24 disposable Forge GameTests passed on a fresh world.
- The Dream uses Forge's dimension effects with the original pink daylight, green nightmare fog, red demonic fog, and a starless sky. The dedicated server sends the highest active nightmare level to Dream clients once per second. Both muted clients showed the shared green fog. One remained a normal dreamer and rejoined during the other's demonic nightmare with red fog. Waking the other player restored pink daylight. Additive blending fixed the dark edges around the sun and lunar phases. One initial Xvfb run ended in a native allocator abort. The relaunched client tested both celestial textures and stayed connected alongside the observer for more than six minutes.
- New Mirror chunks use the original 16-block cell grid from Y 0 through 254. At each level the room interior occupies X and Z offsets 4 through 12 and Y offsets 6 through 14. Their solid, unbreakable walls use the original animated brew textures and tint. Seventeen fresh-world and restart checks passed, as did all three checks on upgrading a disposable save from the previous port. Existing chunks retain their saved contents and new ones use the chamber generator. The legacy shorter Mirror configuration is still pending.
- The 2,000-power medium white Rite of the Mirror consumes a living Demon, Brew of Hollow Tears, gold, and glass. The muted client used that rite and mounted its reward as a two-block mirror, then walked into its assigned chamber. The previous port's `mirrorblock` item uses the same placement logic. The source and exit retain the old position and slain fields and share a connection UUID. Players keep only their own chamber entry in `PlayerPersisted.MirrorWorldEntryPoint`. The old right-click shortcuts have been removed.
- The Reflection in the room copies the player's appearance and gear, has 100 health, caps incoming hits at six before armor, and drops one Demon Heart. Its initial spawn waits for the room chunk's entity data to load. This prevented a duplicate reproduced in the muted client. The same UUID survived death, re-entry, and a clean server restart. The client used a real axe hit and collected the Heart after the test fixture shortened the fight. Both ends recorded the defeat. The player returned to the exterior, recovered both halves as one item, and remounted at a new coordinate without losing the linked chamber. A second facing mirror gave the defeated frame an Overworld path. The dedicated-server movement baseline resets on crossing to avoid the original position correction.
- Forge submits the chat event from a background executor, so the Mirror schedules recognized phrases on the main server thread. “Mirror, mirror, send me home” returns to the chamber entrance and has a five-minute cooldown. “Mirror, mirror, I give up” returns to the respawn location with a one-hour cooldown. Both phrases and cooldown messages worked in the muted client. `MirrorEscape2` survived a restart and retained its remaining time. All 26 Forge GameTests passed in a disposable world.
- The muted client walked into a defeated mirror and emerged five blocks higher through a second mirror with the same facing. In the Mirror, its eastern frame blocked entry when the altar had only 1,000 power. With sufficient power it crossed the solid chamber wall and spent 3,000. During a two-client test, the second player mined and carried the exterior mirror to another position. The first player walked through the disconnected room exit and emerged at the carrier. Remounting the mirror restored the original chamber. The second player used the exit phrase to return to the configured respawn point. The full legacy Mirror Face, Reflection moveset, and shorter world configuration are still pending.
- The Torment generator builds six independently seeded 31-by-31-cell mazes with floors at Y 10, 25, 40, 55, 70, and 85. The entry is near (8,8) and the exit near (8,68). Mazes have original force walls and roofs, mycelium-textured Torment Stone, rare vanilla mycelium, and three cache rooms each. In 100 seed fixtures all 600 floors had routes from the entry to the exit and all three caches. The fixed modern seed replaces the legacy population order so neighboring chunks agree.
- The registered Refilling Chest uses the normal chest screen, renderer and inventory. It persists its original `WITCLifeTicks` counter, fills immediately in Torment, and refills every 72,000 loaded ticks when empty. The modern dungeon loot table supplies two to five stacks. All 59 packaged headless checks passed, including real chest loot, an empty-cache refill, skipping a still-stocked cache, and save persistence. Portal physics and the boss progression are pending.
- The muted survival client opened an actual generated Torment chest, saw its lid and inventory render, and collected bread from it. The upgrade fixture preserved its old diamond marker and found persisted Nether terrain at chunk (0,4), which would otherwise contain the new maze exits. The next travel cycle must choose a fresh maze footprint when upgrading that terrain.
- Torment Portal retains its block ID and adds a horizontal axis, full collision routing, light, flame particles, and the original red tint on the animated vanilla portal sprite. `TormentTravel` writes the legacy `WITCForceTorment` request and processes it on the server tick. Entry chooses among the six real maze starts. Exits ordinarily use the Overworld respawn location. The five-percent branch sends the player to a maze entrance again. The muted client entered every floor and made 32 ordinary exits. A seeded GameTest verified the rare request and that the two portal blocks cannot reroll one collision. The 27 required tests passed. The Dream loot fixture waits for its entity chunk to load before checking for the existing drop.
- Upgraded Torment saves retain their original Nether-like chunks. Travel checks the original maze footprint and saves an alternate X offset of 1024 if any of its chunks predate the generator. The muted client saw the animated red portal, arrived at X 1032.5 in the alternate maze, and walked through the generated exit to its configured respawn point. A flushed save retained the offset. After restarting, the same portal reached another floor in the alternate layout and the old diamond marker remained.
- The Lord of Torment uses its original four-arm winged model and lives at `witchery:lordoftorment`. It has 500 health, caps ordinary hits at five and demonic hits at eight, and ignores fire. The original laugh and hurt samples are present. The archive references a missing death sample, so the port uses the hurt sample at a lower pitch. Its volley includes one Ignianima pulse and three or nine Soulfire projectiles. Soulfire uses the original entity ID. The old scaffold's projectile spawn egg ID remains readable.
- When an Overworld Lord reaches half health, it sends nearby survival players and recorded attackers to the same Torment floor. Saved pending encounters wait for the maze chamber's entities to load and spawn one Lord at 250 health. Two muted survival clients verified this transition on the upgraded maze and saw one Lord. The test supplied the initial boss and shortened the fight around the threshold. Real attacks verified a five-point cap, an actual ranged volley, and the threshold trigger. All 29 Forge GameTests passed, including separate checks for both damage caps, fire immunity, the full volley and the four loot drops.
- A dedicated restart retained both players in the same maze and the same single Lord UUID at 250 health. After shortening the final fight in the fixture, the player killed it with an axe and picked up two enchanted books, a Demon Heart and the Brew of Soul Torment. Drinking the brew consumed it and saved `PlayerPersisted.WITCSpellBook.tormentum`. The player returned to their Overworld respawn through the generated exit. The coven, Imp, contract and actual spellcasting remain pending.
- The medium white Rite of Binding a Familiar costs 8,000 power and uses Tear of the Goddess, Odour of Purity, Whiff of Magic, a diamond, and Infernal Blood. It converts the player's tamed vanilla cat within seven blocks into `witchery:cat` and preserves its name and owner. The original cat variant is the modern black coat. The muted player tamed Bramble with cod and bound it with the actual rite, which had previously refunded all five items and power without a pet. The bound cat had 50 health, took one point of the owner's ten-point injury, and healed with cod. The original five-point armor bonus, fire immunity and offline owner protection passed Forge GameTests.
- The medium white Rite of Calling a Familiar costs 1,000 power and uses Breath of the Goddess, Hint of Rebirth, and Whiff of Magic. The muted client called back Bramble from a loaded Torment chunk with the same UUID. The same client dismissed the seated cat by sneaking and interacting, then called a replacement with the same name, 50 health, and a new UUID. The two rites extend the recipe actions on protocol version 7. All 31 required Forge GameTests passed. Owl and toad familiars are pending.
- A dedicated restart retained the same cat UUID, 50 health, and matching owner record. A fatal hit in the disposable fixture dismissed the familiar, cleared `FamiliarSummoned`, and killed its online owner. The record kept the name for a later call.
- The stable `witchery:covenwitch` registry ID now has a neutral 30-health witch with five legacy skins, original voice, forest and swamp spawns, and the seven original quests. An active familiar is required to receive an offer. Accepted quests preserve their player name, recipient UUID and remaining count. The two 100-health quest pets save trophies keyed to the originating witch's UUID. Six members fit in `PlayerPersisted.WITCCoven`, using the original `WitchName`, `Skin` and `Quest` fields. The entity's synchronized skin advances network compatibility to protocol 8. All 34 required GameTests passed, including actual recipe matching, biome registration, a legacy name-only bones quest, and the saved zombie trophy.
- Quartz Sphere uses quartz, quartz blocks and glass at a crafting table. Happenstance Oil uses Redstone Soup, extended Night Vision, an Eye of Ender, a Golden Carrot, a Spider Eye and Mandrake Root in a 2,000-power kettle batch. The night-only, medium white Rite of the Crystal Ball uses Quartz Sphere, Gold Ingot, Happenstance Oil and 2,000 power. Actual predictions remain pending. The original Imp rite requires activation with the Seer Stone and at least one coven member. A matching signed Contract of Ownership and 25 experience levels bind the summoned Imp before it can exchange gifts. Its final reward, the Contract of Torment, consumes personal infusion charge.
- The muted survival client crafted a Quartz Sphere at the crafting table, bottled Happenstance Oil from a kettle, and dropped its own ingredients into the medium white circle before activating it at night. The Crystal Ball was the request Angelique Redfern gave after the player recalled Bramble. The player collected and offered the ball, and a dedicated restart retained the membership with skin 4 and quest ID 3. The used ball stayed consumed and the recruited witch stayed dismissed.
- The small white Seer Stone rite costs 2,000 power at night and uses Quartz Sphere, Obsidian and Happenstance Oil. The muted client collected the stone, read its familiar Bramble, one coven member and Tormentum knowledge, and held use to recall Angelique for one minute. Using the stone on a circle reused the same entity UUID and original position offset for the first coven member during the Rite of the Necromantic Stone. Her summoned body left on expiry, her coven record remained, and the rite produced its item. Thirty-five Forge GameTests passed, including timed calls, reuse, saved lifetime and departure. Potion bottling skill and Crystal Ball predictions remain pending.
- The medium Infernal Imp rite uses Refined Evil, Infernal Blood, Ender Pearl, Attuned Stone and 5,000 altar power. The muted client activated the real rite and verified the four offerings were refunded without the Seer Stone. With an active coven and the stone it summoned a 50-health Imp. The client used its own bed to fill the Taglock Kit and crafted the signed Contract of Ownership in its inventory. The same contract survived a restart, was refused with six levels, and bound Zul after 25 levels were paid. New registry and recipe data use protocol 9.
- The Imp's saved state tracks affection, the absolute gift time, spells taught, ownership, home, and its Demon Heart power or Icy Needle suppression. The muted survival client waited for the first real exchange, observed the next gift being refused during the three-minute cooldown, and advanced the saved gift timer for the remaining test exchanges. It collected all four original rewards and drank the three brews to save Carnosa Diem, Morsmordre and Ignianima. Casting these spells and the Imp's other signed contracts remain pending.
- The Contract of Torment checks the original standing-stone pattern and draws ten personal infusion charge every 40 ticks through tick 280. In the muted client, releasing the contract early spent twenty charge and kept the item. An uninterrupted channel spent eighty more, consumed the contract, and summoned one 500-health Lord. With mob griefing disabled, the test preserved the stones. The disposable player's saved legacy infusion supplied the initial 200 charge. Survival infusion acquisition and recharging are the next prerequisite for the full Torment route. All 38 required Forge GameTests passed, with separate coverage for the loaded Imp rite, signing, gifts, power, and contract use.
- A dedicated restart retained Zul's UUID, owner, and four rewards given, along with the player's four spell flags and 100 remaining infusion charge. The consumed contract stayed gone and the single 500-health Lord kept its UUID.
- Soul of the World now brews two portions in a 4,000-power kettle with Redstone Soup, extended Regeneration, Enchanted Golden Apple, Attuned Stone, Mandrake Root and Rowan Sapling. The old enchanted apple metadata maps to the modern loot item. Forge recipe tests check both Alder and Hawthorn are rejected. The muted survival client filled the kettle with a bucket and bottled both portions.
- The muted survival client crafted a Poppet and Death Protection Poppet with the original recipes, filled a Taglock Kit at the bed and bound the poppet to itself. The Overworld infusion sacrificed one serving of Soul of the World and used 4,000 altar power with small and medium white rings. Its lethal magic consumed the poppet, restored the player from six to ten health, and saved 200 charge and infusion ID 2. Reading the save before the rite showed no infusion. The same client then spent 20 of its new charge by starting and releasing the Contract of Torment in the standing stones.
- The new infusion recipe actions use protocol 10. All 40 required Forge GameTests passed, including old poppet subtype 6 conversion, a bound poppet triggering only on lethal damage, the two ring recipes, and the charge cap. Other infusion powers and Poppet Shelf support remain pending.
- Recharge uses a standard Regeneration potion, the same two white rings, 4,000 initial altar power and 40 power per second. It adds ten charge per second within four blocks. The muted client stayed at 180 charge outside the ring. After restarting the dedicated server, it found the rite still active and the same charge. It reentered, recharged to 200, and canceled the rite with a click.
- Voodoo Poppets and Voodoo Protection Poppets retain the legacy target data and `WITCDamage` wear. The muted client crafted a Voodoo Poppet, collected a pig's sample, and bound the poppet. It pricked the pig from ten to nine-and-a-half health, consumed one Bone Needle, and pushed the pig about seven blocks. Binding, lava and underwater behavior use the original actions. Poppet Shelves and the Witch Hunter clothing and response remain pending.
- Infernal Animus yields two servings from Redstone Soup, strong Harming, Voodoo Poppet, Demon Heart, Refined Evil and Blaze Rod at 4,000 altar power. Both drinking and splash variants of strong Harming match. The muted player put its used, bound poppet through a hopper and bottled both servings from a splash-potion batch. The small and medium Infernal rite used the brewed tonic and a bound Death Protection Poppet, healed its owner to ten health, and changed infusion ID 2 to ID 4 with 200 charge. All 42 required Forge GameTests passed. Protocol 11 includes the new Infernal action.
- In a disposable dedicated-server fixture, the muted client crafted and bound a Voodoo Protection Poppet and a Voodoo Poppet to itself. The ward reflected a real cast and both poppets saved 350 wear.
- The Mystic Branch uses server-side rotation samples at the legacy seven-degree threshold while the player holds use. The 24 variants have the original spell IDs 39 through 42 and each learned spell accepts any infusion. The original Infernal requirement applies to curses without knowledge keys. Protocol 12 retires the old client-prepared packet. The preserved `spelleffect_spawn_egg` item keeps previous inventories loadable.
- The muted client brewed and bottled Mystic Unguent, performed the nighttime two-ring rite with an Ent Branch and retained the Mystic Branch after a dedicated restart. Carnosa Diem cost two health, Ignianima took three health from a pig and Morsmordre spawned a Dark Mark. All 46 required Forge GameTests passed, plus `runData` and the reobfuscated JAR.
- The dedicated test used two muted survival clients for Tormentum. The projectile moved the second player to the maze at X 1032.5, removed the pig it hit, and spent 25 charge. A repeat during the 30-minute cooldown left charge unchanged. The saved player files retained `tormentumLastUse` as a wall-clock tick field and the second player's dimension. The client also rendered the Dark Mark skull and smoke.
- The isolated Auram profile snapshot contains 540 enabled mod JARs and the installed pack's scripts and configuration. The baseline with commit `87f52ab` reached render-thread mod initialization. Pause or stop the pack before running two clean muted clients together. The full pack's title screen and fresh survival world still need verification.
- The same Auram snapshot with `7e8f179` reported an Evolved Mekanism 1.2.1-fix3 initialization failure during Forge construction. Its JEI plugin initialized `EMItems` with a null `QIODriveTier`. The comparison profile contains the same 540 mod JARs before adding Witchery. Test the comparison separately from the clean client.
- The matched 540-mod Auram profile reproduced the same Evolved Mekanism startup exception with no Witchery JAR installed. The muted Xvfb test used a 4 GiB Java heap and 6 GiB container limit after a previous comparison was killed by Docker under memory pressure. Pack title and fresh survival validation remain open.
- Light and Otherwhere acquired their original infusion IDs 1 and 3 with 200 charge using the 2,000-power white rite and the 4,000-power Otherwhere rite. The kettle made two servings of each brew at 4,000 power. The original kettle assigns one slot to each dropped stack, so the Eyes of Ender go in separately. Both the drinking and splash versions of extended Swiftness match.
- The muted survival client crafted and Taglock-bound a Fire Protection Poppet. It survived lethal fire, consumed the ward and gained Fire Resistance. A second crafted ward went into Ghost of the Light. The two infusion rites consumed Death Protection Poppets and raised six health to ten. Carnosa Diem worked with the Light infusion. All 49 required Forge GameTests passed. Protocol 13 adds the two infusion actions.
- The dedicated server restored the same Otherwhere ID 3, 200 charge and remaining Light and Otherwhere brews after a full restart.
- The background test client runs in Xvfb with master volume zero and the OpenAL null driver. Keep the dedicated `--nogui` server on host loopback and connect from the client through `host.docker.internal`. Running both JVMs in the same two-CPU Docker profile caused watchdog stalls. The separate server supported manual chalk and blade crafting, glyph drawing, Arthana placement, and collecting the charged stone in survival.
- `WitchCrop` maps the existing crop and seed IDs. The Forge grass loot modifier preserves the legacy weight distribution. Harvest a mature mandrake in daylight for a 90% chance to spawn the creature outside Peaceful. It drops the root and seeds, and earmuffs prevent its nausea attack.

### Witch's Hand and Otherwhere Powers (2026-11-20)

- Protocol 14 sends authoritative infusion charge on login, respawn, grant, recharge and use. The meter uses Snow, Dirt, Nether Portal and Netherrack, with left-side placement by default. The clean muted client displayed the Otherwhere meter. The survival player killed a witch with an Arthana and collected the dropped Hand.
- The Hand supports Otherwhere blink for one charge, point recall for two, lifting the caster and a creature for two, and companion recall for four. Hold use while sneaking for three seconds to remember the position, then release. Short sneak use recalls the player, and sneak attack recalls with the attacked creature. It keeps `WITCRecallD`, `WITCRecallX`, `WITCRecallY` and `WITCRecallZ` in persisted player data, and uses `WITCCooldown` on the item. Vanilla recall dimensions preserve their numeric values, and modded dimensions use resource IDs. Three special dimensions block recall. Ender Inhibition III prevents the affected participant from teleporting.
- All 52 required Forge GameTests passed, including legacy recall coordinates and cooldown, blink, lift and separate Arthana drop chance coverage. Data generation and the reobfuscated JAR succeeded.
- The packaged clean Forge test used the naturally infused Otherwhere player and the Hand collected from a witch. The four successful actions spent 1, 2, 2 and 4 charge. The dedicated server saw the caster and pig return from the Nether to X 123.5, Y 110, Z 32.5 in the Overworld with 191 charge, and the Hand, both entities, recall fields and charge were confirmed on reconnect after a dedicated restart. Ender Inhibition III stopped recall and preserved charge. Lowering only the scratch client's graphics settings restored responsive controls after a previous client timeout.

### Light Infusion Hand Powers (2026-11-20)

- The Light mode cloaks after 30 ticks and refreshes every 30 ticks for one charge. Its barrier shield and cage use the original translucent sprite and disappear after 200 loaded ticks. Short sneak use costs three, and an attack that raises a creature costs five. A barrier remembers the owner by UUID and reads legacy name-only saves. Other survival players and mobs collide with it.
- The clean muted client performed the Light rite again with its second bottled Ghost of the Light before using the Witch's Hand. It spent two charge while cloaked. Releasing ended Invisibility. It rendered and walked through its three-by-three shield, watched it expire, raised a pig four blocks into a cage, caged a second pig in place, and projected 15 blocks from a stone face. The server verified barrier ownership and charge 200 to 184 for those five actions.
- All 55 required Forge GameTests passed. Targeted coverage includes clearing a zombie's recent aggression, canceling the cloak while preserving ordinary Invisibility, shield collision and expiry, legacy ownership and a blocked raised cage. Data generation and JAR reobfuscation passed.

### Overworld Infusion Hand Powers (2026-11-20)

- The Overworld Hand uses two charge to disarm a mob within four blocks, two or four to repel metal equipment, three to launch stone from an exposed side, and two to lift a supported terrain column. Hold for two seconds while looking clear to charge a shockwave at six charge per second. The soft-landing power costs five, or ten for a sneaking impact explosion.
- A short sneak-use at an ore costs two and produces two ingots. After 40 ticks of sneak-channeling, a magnet pulse costs one every four ticks and each ore converted during the pulse costs two. Use Forge ore and ingot tags in place of the Ore Dictionary and preserve the surrounding stone type for deepslate and Nether ores. Legacy ingredient damage 55 migrates to `witchery:rock`; the projectile deals six damage. The Java 17 build and all 60 required GameTests passed.
- The muted survival client performed the Overworld rite with the bottled brew and bound poppet from the fixture. It collected two targeted gold ingots and drew in four iron ingots including one from the channeled ore. A zombie dropped its sword, its chainmail accepted the upward shove, and a rock took a pig from ten health to four. A raised column moved terracotta three blocks and preserved the chest. A two-second wave took another pig from ten to two. Both soft landings preserved ten health. The sneak landing spent ten charge and blew open the snow and underlying stone, leaving 151 charge. A dedicated restart preserved the charge, Hand, raised terracotta and adjacent chest.

### Infernal Infusion Hand Powers (2026-11-20)

- The Infernal Hand binds for five charge and sacrifices a matching bound creature for one. Normal attacks command nearby controlled mobs for one charge, and a short sneak-use on the ground moves them without cost. Legacy `WITCEnslaverName` loads alongside the newer `WITCEnslaverUUID`. On the muted dedicated server, a zombie attacked the chosen pig after a real order and moved toward an aimed block after the move order.
- The 25 creature powers keep `WITCBeastPower` and `WITCBeastPowerCharges`, capped at 20. Protocol 15 sends the power and reserve alongside infusion charge. The muted client saw the original pig icon, healed from six to twenty, and preserved both pools after an already depleted attempt. A bat sacrifice switched to power 4 and ten charges. Night Vision spent one reserve and one infusion charge. Owl and Toad still depend on their pending creature ports.
- All 65 required GameTests passed. Data generation and reobfuscation succeeded with the access transformer for the Bat and Owl sustained-jump behavior. The tests verified old owner-name restore, target protection, relocation orders, sacrifice and healing, charge caps, lightning and drowning responses, and the three fireball or arrow cases plus cobweb placement. Legacy web subtype 56 now uses `witchery:thrown_web` alongside the existing placeable block item.
- The packaged dedicated restart kept the minion's owner UUID and effect, the player's bat power, 180 infusion charge and nine reserve charges. With `allow-flight=false`, the muted player held jump for 6.8 seconds and rose almost 59 blocks without disconnecting. Falling back to the arena cost two health and preserved both charge pools.

### Poppet Shelf (2026-11-20)

- The shelf uses the original `witchery:poppetshelf` block and block entity, nine slots, and a persistent Forge ticking ticket for the shelf's chunk. Its recipe uses Attuned Stones in the corners, Nether Bricks on the edges and green wool in the center. It renders stored poppets and exposes their fullness to a comparator. The muted survival player placed the crafted shelf and loaded three bound wards through its menu.
- Wards in the player's inventory take priority. Shelves protect across dimensions, with the original default limiting eligible shelves to the Overworld, Nether, End, and Dream. The server setting can allow other dimensions. The real client survived lethal fire 480 blocks from its shelf, and after a restart the same shelf's Death Protection Poppet blocked lethal damage in the Nether. Forge loaded the shelf before anyone joined.
- All 68 required Forge GameTests passed, and data generation and JAR reobfuscation succeeded. GameTests covered the legacy shelf name and ward metadata, inventory priority, the comparator, automation's poppet filter and ticket removal.
- A real Voodoo cast reflected from the remote shelf and added 350 wear to both dolls. The client mined and recovered the shelf and the worn ward. The chunk unloaded when the player left. The final packaged restart verified that startup retains the valid shelf's ticket and prunes a stale same-chunk ticket injected into the disposable world's `chunks.dat`.

### Specialized Protection Poppets (2026-11-20)

- Restore legacy Earth (1), Water (2), Hunger (4), Tool (5) and Armor (11) poppet variants, original shaped recipes and single-Taglock binding. They work in player inventory and on shelves, with inventory first. Earth protects from lethal falls and falling blocks without healing. Water protects from lethal drowning and adds three seconds of Water Breathing. Hunger protects when starvation would leave two health or less and adds three seconds of Saturation. The Death ward's fallback effects last six seconds.
- Tool repair runs on player interaction, and armor repair runs on injury. Eligible equipment at the original 90% wear threshold gets a full repair. A bound ward is consumed when used. All 72 required Forge GameTests passed, and data generation and reobfuscation succeeded.
- The muted survival client crafted and personally Taglock-bound the Earth ward. The test supplied already-bound Water, Hunger, Tool and Armor wards. Dedicated damage commands verified unhealed Earth protection, Water Breathing and Saturation. A real stone block break brought a pickaxe from 235 wear to one, and an incoming hit brought an iron chestplate from 222 wear to zero. A full dedicated restart kept both repairs and the equipment wards remained consumed.

### Vampiric Poppet and Corrupting Voodoo Protection (2026-11-20)

- Legacy poppet subtypes 9 and 10 load as `witchery:vampiric_poppet` and `witchery:poppet_protection`. The Vampiric recipe requires a Taglock for the protected player followed by one for the recipient in crafting grid order. A valid use costs 66 wear. A creature can receive up to 15 damage at a time and its signature is cleared if it dies. The muted survival client crafted the poppet, collected both Taglocks, and passed three zombie damage tests without losing health. The first signature and 198 wear persisted through a packaged restart.
- The muted client also crafted Poppet Protection and bound it at its own bed. Its Corrupting Voodoo Protection test used a medium infernal ring and five offerings. An active Cat familiar supplies Curse Mastery. The rite returned all offerings while Bramble was dismissed. A successful cast spends 7,000 altar power. A matching Poppet Protection reflects it at the caster as lightning and takes 350 wear. An undefended target can lose up to ten matching Voodoo Protection Poppets.
- All 77 required Forge GameTests passed, including a full ritual that refunds, reflects and then strips an undefended Voodoo Protection Poppet. Java 17 data generation and JAR reobfuscation succeeded. Protocol 16 includes the new rite action. Ward lookup uses the target's inventory before eligible Poppet Shelves. Witch Hunter, spirit enhancement, wolf form, vampire and draining-bolt interactions depend on legacy systems still to be ported.

### Ent and Ent Branch (2026-11-20)

- `witchery:ent` now uses the original 200 health, attack knock-up, damage cap before the triple axe bonus, and two drops. It renders its original leaf canopy, roots, and attack pose. Witchwood harvest can awaken an Ent at 1% plus 1% for each adjacent Witchwood log, capped at 5%. Its clearing search keeps the original 8 to 16 block offsets per horizontal axis and requires loaded, supported space.
- All 80 required Forge GameTests passed and the Java 17 data generation and reobfuscated build succeeded. The muted dedicated client harvested Hawthorn with an axe, awakened an Ent and was killed by its charge. After respawning, the player used a supplied axe to defeat that Ent once it was positioned in the arena, then collected its Ent Branch and Alder Sapling. The packaged server also loaded a separate Ent with the legacy `Owner` field and a maximum of 200 health.
- A packaged restart kept the Ent Branch and Alder Sapling variant in inventory and the separate Ent's legacy owner and 200 health. The muted client dropped its collected Ent Branch and the supplied Mystic Unguent, activated the original two white rings at night, and received a Mystic Branch.

## Session Memory (2026-02-08)
- Port target is Forge 1.20.1.
- Legacy source is fully available at `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10`.
- Strategy is breadth-first: register everything first, behavior parity later.

## Session Memory (2026-02-09 update)
- Fluid breadth scaffold is now wired end-to-end (registry-level):
  - `WitcheryFluids` keeps explicit legacy mapping in `LEGACY_FLUID_CONFIGS` for block + bucket bindings.
  - critical legacy name mismatches to remember:
    - fluid `fluidspirit` maps to block `spiritflowing`.
    - fluid `fluiddisease` maps to block `disease`.
- Fluid-like block IDs now use `LiquidBlock` scaffolds in `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/registry/WitcheryBlocks.java`:
  - `spiritflowing`, `hollowtears`, `disease`, `brew`, `brewgas`, `brewliquid`.
- Legacy fluid bucket items are no longer generic placeholders:
  - `bucketspirit`, `buckethollowtears`, `bucketbrew` now register as `BucketItem` in `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/registry/WitcheryItems.java`.
- `./gradlew compileJava` passes after this change (only existing deprecation warning remains for `ItemBlockRenderTypes.setRenderLayer`).
- Launch crash hotfix (2026-02-09):
  - fixed `IllegalStateException: witchery:plantmine has a collision shape and an offset type, but is not marked as dynamicShape in its properties`.
  - root cause: `plantmine` copied `Blocks.DANDELION` offset behavior and used collision-enabled `LegacyNonSolidShapeBlock`, but the scaffold constructor did not set `dynamicShape`.
  - fix: in `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/registry/WitcheryBlocks.java`, `LegacyNonSolidShapeBlock` now applies `.dynamicShape()` when `hasCollision=true`.
  - verification:
    - `./gradlew compileJava` passes.
    - `./gradlew runClient` launch smoke reached full registry freeze/atlas initialization without the previous `plantmine` exception (manual stop after startup).
- Guardrail: if any fluid ID is added/renamed, update `LEGACY_FLUID_CONFIGS` immediately or startup will fail fast with `Missing fluid config for legacy fluid`.
- Minimal server data-generation pipeline now exists in `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/data`:
  - `WitcheryDataGenerators` subscribes to `GatherDataEvent`.
  - `WitcheryBlockTagsProvider` writes `witchery:legacy_blocks`.
  - `WitcheryLootTableProvider` + `WitcheryBlockLoot` generate breadth-first self-drop tables for registered blocks with items.
  - `WitcheryRecipeProvider` is intentionally empty for now (recipe migration deferred to depth pass).
- Placeholder spawn eggs are now auto-generated in `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/registry/WitcheryItems.java`:
  - one `ForgeSpawnEggItem` per legacy entity placeholder using `<normalized_entity_name>_spawn_egg`.
  - spawn eggs are appended to `WitcheryItems.allForCreativeTab()` so QA can access them without manual commands.
- Spawn egg missing-model fix (2026-02-09):
  - generated matching item model files under `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/resources/assets/witchery/models/item`.
  - scope is now exactly the normalized legacy entity list (`LegacyRegistryData.ENTITIES`) -> `55` `*_spawn_egg.json` files.
  - corrected a failed prior generation attempt that over-produced invalid spawn egg model names from non-entity lists.
- Reported placeholder-texture block model remap pass (2026-02-09):
  - updated model texture assignments for `tormentstone`, `refillingchest`, `brewliquid`, `mirrorblock`, `mirrorblock2`, `wolftrap`, `trent`, and `scarecrow`.
  - intent is breadth-level visual scaffolding: remove transparent/purple-black placeholder rendering while keeping behavior parity work for later phases.
- Fetish model UV correction pass (2026-02-09):
  - replaced temporary `cube_all` versions of `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/resources/assets/witchery/models/block/trent.json` and `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/resources/assets/witchery/models/block/scarecrow.json`.
  - both models now use explicit multi-element cuboids with per-face UV coordinates derived from legacy `ModelFetishTrent`/`ModelFetishScarecrow` atlas regions (64x64 texture space scaled to block-model UV space).
  - this addresses the "texture loaded but mapped incorrectly" artifact seen on `trent`.
- Phase 3 network scaffold is now initialized:
  - new file `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/network/WitcheryNetwork.java`.
  - channel bootstrap is called from `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/Witchery.java`.
  - legacy packet intents (`19`) are represented in `LegacyRegistryData.PACKETS` with `legacyId`, normalized key, and flow (`CLIENTBOUND`/`SERVERBOUND`).
  - transport now uses typed packet stubs (one per legacy intent key) with direction mismatch warnings.
  - codec-backed payload/handler scaffolding is now implemented for:
    - `player_sync` (`playerId`, `syncRevision`, serialized `WitcheryPlayerData` snapshot)
    - `extended_player_sync` (`playerId`, `initialized`, `syncRevision`, serialized `WitcheryPlayerData` snapshot)
    - `partial_extended_player_sync` (`playerId`, `syncRevision`, serialized `WitcheryPlayerData` snapshot)
    - `item_update` (`slotIndex`, `damageValue`, `pageIndex`) (aligned to legacy packet shape)
    - `sync_entity_size` (`entityId`, `width`, `height`)
    - `set_client_player_facing` (`yaw`, `pitch`)
    - `cam_pos` (`active`, `updatePosition`, `entityId`) (aligned to legacy packet shape)
    - `push_target` (`entityId`, `velocityX`, `velocityY`, `velocityZ`)
    - `sound` (`soundId`, `x`, `y`, `z`, `volume`, `pitch`)
    - `particles` (`particleEffectId`, `soundEffectId`, `x`, `y`, `z`, `width`, `height`, `color`)
    - `player_style` (`username`, `grotesqueTicks`, `nightmare`, `ghost`, `creatureType`, `blood`, `playerSkin`)
    - `spell_prepared` (`effectId`, `level`)
    - `sync_markup_book` (`slot`, `pages`)
  - explicit no-payload codec records + handlers now exist for:
    - `clear_fall_damage`
    - `howl`
  - clientbound packet handling now has first functional client bridge:
    - new file `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/client/packet/WitcheryClientPacketHandlers.java`.
    - new file `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/client/WitcheryClientCameraState.java` for explicit client camera-mode staging.
    - new file `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/client/event/WitcheryClientCameraHooks.java` to drive camera-entity override/reset from client tick.
    - `particles` now uses legacy ordinal mapping for `ParticleEffect` + `SoundEffect`, scales particle count using legacy width rules, and supports colored spell particles.
    - `player_sync`, `extended_player_sync`, and `partial_extended_player_sync` now apply incoming sync state client-side:
      - hydrates `WitcheryPlayerData` from serialized sync snapshots (`CompoundTag`) sent with each sync packet.
      - drops stale/out-of-order sync packets using revision checks on both client persistent tags and capability data.
      - mirrors sync/style state (`syncRevision`, `initialized`, `grotesqueTicks`, `nightmareLevel`, `ghost`) into client persistent tags for staged visibility and renderer compatibility.
    - `player_style` now applies staged style data to target player capability + persistent tags on client by username lookup.
    - `sound` now resolves legacy/modern sound ids and plays client-local scaffold audio with legacy volume/pitch defaults.
    - `push_target`, `set_client_player_facing`, and `sync_entity_size` now run first-pass client behavior hooks (motion, rotation, staged size metadata).
    - `cam_pos` now follows legacy semantics (`active`, `updatePosition`, `entityId`) and stages camera-active/target state via `WitcheryClientCameraState` (replacing temporary persistent-tag camera staging).
    - camera follow/reset behavior is now applied from `WitcheryClientCameraHooks` (client tick): active state switches to target camera entity, inactive/missing target resets to local player camera.
  - serverbound packet handlers now apply first functional behavior:
    - `clear_fall_damage` clears sender `fallDistance`.
    - `item_update` now mutates real inventory stack page state via legacy key `CurrentPage` (with slot + damage validation).
    - `sync_markup_book` now mutates real inventory stack page-list state via legacy key `pageStack`.
    - `item_update`, `spell_prepared`, `sync_markup_book`, and `howl` still stage sync state into `WitcheryPlayerData`, bump revision, and push `player_sync` (now including serialized capability snapshot payload).
    - `spell_prepared` and `howl` also write minimal persistent tags on the sender for migration visibility.
- Phase 3 dimension scaffolding is now initialized:
  - new file `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/world/WitcheryDimensions.java`.
  - keys are defined for `dream`, `torment`, `mirror` across `Level`, `LevelStem`, and `DimensionType`.
  - datapack scaffolding now exists for all three dimensions:
    - `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/resources/data/witchery/dimension_type/dream.json`
    - `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/resources/data/witchery/dimension_type/torment.json`
    - `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/resources/data/witchery/dimension_type/mirror.json`
    - `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/resources/data/witchery/dimension/dream.json`
    - `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/resources/data/witchery/dimension/torment.json`
    - `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/resources/data/witchery/dimension/mirror.json`
  - runtime travel hook scaffold now exists in `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/world/WitcheryDimensionTravelHooks.java`:
    - route resolver for `dream`, `torment`, `mirror` plus return-to-overworld behavior.
    - portal collision trigger path now uses `PlayerTickEvent` + `resolvePortalCollisionTrigger(...)` for `spiritportal` and `tormentportal`.
    - right-click trigger path is now limited to mirror-rite scaffolds: `mirrorblock`, `mirrorblock2`, `mirrorwall`, `circleglyphotherwhere`.
    - portal collision routing includes persistent cooldown gating (`WITCLastPortalCollisionTick`) to avoid rapid retrigger loops.
  - authentic rite-completion activation and provider parity behavior are still TODO.
- Phase 3 event skeleton now has no-op Forge hooks:
  - new file `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/event/WitcheryEventHooks.java`.
  - placeholder handlers now exist for `AttachCapabilitiesEvent<Entity>`, `PlayerLoggedInEvent`, `PlayerEvent.Clone`, `TickEvent.PlayerTickEvent`, `PlayerInteractEvent.RightClickBlock`, `EntityTravelToDimensionEvent`, `PlayerChangedDimensionEvent`, and `LevelEvent.Load`.
  - this creates stable attachment points for capability/data migration and runtime bootstrap without adding behavior yet.
- Capability scaffold now exists for `ExtendedPlayer` replacement:
  - `WitcheryCapabilities` registers capability type `WitcheryPlayerData` on MOD bus.
  - `WitcheryPlayerDataProvider` is attached to players via `AttachCapabilitiesEvent<Entity>` and copied during `PlayerEvent.Clone`.
  - `WitcheryPlayerData` now stores staged legacy groups with NBT serialization:
    - base sync: `initialized`, `syncRevision`
    - style/state: `creatureTypeOrdinal`, `humanBlood`, `grotesqueTicks`, `nightmareLevel`, `ghost`, `otherPlayerSkin`
    - spell state: `preparedSpellEffectId`, `preparedSpellLevel`
    - inventory/book staging: `lastItemUpdate*`, `lastMarkupBook*`
    - misc runtime: `lastHowlGameTime`
  - event hooks now send typed sync packets instead of intent-only placeholders:
    - login sends `extended_player_sync` + `player_sync`.
    - clone sends `partial_extended_player_sync`.
- GUI scaffold now preserves legacy GUI IDs explicitly:
  - `LegacyRegistryData.MENUS` is now metadata (`legacyGuiId`, `key`) rather than plain strings.
  - `WitcheryMenus` now registers typed `LegacyPlaceholderMenu` entries and lookup by both key + legacy GUI ID.
  - `WitcheryClient` registers `LegacyPlaceholderScreen` for all placeholder menus so GUI IDs `0..8` all have routable client stubs.
- Near-term look-ahead queue:
  - replace temporary mirror right-click routing with rite-completion activation hooks (portal collision path is now scaffolded).
  - harden camera override lifecycle in `WitcheryClientCameraHooks` for edge cases (dimension swap, target despawn timeout, spectator transitions).
  - audit offset-type + collision-shape scaffolds so collision-enabled custom-shape blocks remain `dynamicShape`-safe.
  - run a fresh client launch and re-scan `run/logs/latest.log` specifically for spawn egg model loads and the remapped block-model texture set.
  - evaluate `witchsladder` against the same fetish-model UV/shape parity standard and decide whether to migrate it off the current simple pillar scaffold.
  - revisit fetish rotation/tint parity (legacy TESR had per-part rotations and color overlays not yet represented in static JSON models).
  - add targeted round-trip validation for sync snapshot payloads (`login`, `clone`, `item_update`, `spell_prepared`, `sync_markup_book`, `howl`) in both singleplayer and dedicated-server sessions.
  - keep migrating high-priority `ExtendedPlayer` groups into `WitcheryPlayerData` (combat/state/progression slices) before depth pass.

## Phase 1 Completed
- Phase 1 scaffolding is implemented and `./gradlew compileJava` passes.
- New registry scaffolding package:
  - `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/registry/WitcheryBlocks.java`
  - `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/registry/WitcheryItems.java`
  - `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/registry/WitcheryFluids.java`
  - `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/registry/WitcheryBlockEntities.java`
  - `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/registry/WitcheryEntities.java`
  - `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/registry/WitcheryMenus.java`
  - `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/registry/WitcheryEffects.java`
  - `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/registry/WitcheryCreativeTabs.java`
  - `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/registry/LegacyRegistryData.java`
- Mod bootstrap now routes through `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/Witchery.java` -> `WitcheryRegistries.register(...)`.

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

## Asset/Rendering Memory (critical)
- Purple-black render state observed in client was tied to stale `run/logs/latest.log` (timestamp: 2026-02-07 19:57) plus invalid uppercase resource locations in model texture refs (example old value: `witchery:items/taglockKit`).
- Additional 1.20.1 critical rule: use `textures/block` and `textures/item` atlas paths, not legacy-style `textures/blocks` and `textures/items` for model sprite references.
- Additional 1.20.1 door-model rule: do not use `minecraft:block/template_door_*` parents (missing in this runtime); use `minecraft:block/door_bottom_left`, `door_bottom_right`, `door_top_left`, `door_top_right` and their `_open` variants.
- Fixed by:
  - rewriting model refs from `witchery:blocks/*` -> `witchery:block/*` and `witchery:items/*` -> `witchery:item/*`.
  - copying legacy texture trees into singular folders (`textures/block`, `textures/item`).
  - generating barrier-based placeholder textures for missing block sprite names so all model refs resolve during scaffolding.
  - renaming actual texture filenames to lowercase in `textures/block` and `textures/item` (not just refs). This was required because many legacy files were camelCase (`taglockKit.png`, `voidBramble.png`, etc.).
- MacOS case-insensitive filesystem trap:
  - simple file existence checks can falsely report success for wrong-case names.
  - use exact `find` output and real case-rename passes, then validate with fresh client log.
- Current resource status:
  - `assets/witchery/blockstates`: 121 files.
  - `assets/witchery/models/block`: 273 files.
  - `assets/witchery/models/item`: 272 files.
  - spawn egg item models present: 55 (`*_spawn_egg.json`).
  - legacy textures copied under `assets/witchery/textures`.
- Targeted parity notes:
  - `wolftrap` in 1.7 is `BlockBeartrap(true)` with TESR `ModelBeartrap` + `textures/blocks/beartrap.png`; current 1.20 model is a static approximation of the unsprung TESR geometry.
  - `wolftrap` placement in 1.20 is now aligned to player facing direction (`getHorizontalDirection()`) for closer legacy behavior.
  - `icedoor` blockstate now mirrors vanilla iron-door rotation mapping to keep open/hinge visuals correct in 1.20.1.
  - crop family parity pass added staged crop assets + age blockstates for:
    - `belladonna` (max stage `4`)
    - `mandrake` (max stage `4`)
    - `artichoke` (max stage `4`)
    - `snowbell` (max stage `4`)
    - `wormwood` (max stage `4`)
    - `wolfsbane` (max stage `7`)
    - `garlicplant` (max stage `5`)
  - corresponding crop scaffolding now maps those names to `CropBlock` placeholders with explicit `getMaxAge()` values in `WitcheryBlocks`.
  - `witchlog` scaffolding now uses an explicit `wood_type` property with `rowan/alder/hawthorn` variants, plus dedicated alder/hawthorn log model files.
  - `wallgen` model now uses `witchery:block/wallgen` during visual parity testing instead of the prior hardcoded iron texture placeholder.
  - structural block families now use proper 1.20.1 block classes + blockstates instead of static cube placeholders:
    - `StairBlock`: `stairswoodrowan`, `stairswoodalder`, `stairswoodhawthorn`, `icestairs`, `snowstairs`.
    - `SlabBlock`: `witchwoodslab`, `iceslab`, `snowslab`.
    - `FenceBlock`/`FenceGateBlock`: `icefence`, `icefencegate`.
    - `PressurePlateBlock`: `icepressureplate`, `snowpressureplate`, `cwoodpressureplate`, `cstonepressureplate`, `csnowpressureplate`.
    - `ButtonBlock`: `cbuttonwood`, `cbuttonstone`.
  - corresponding assets were migrated to vanilla 1.20.1 template parents (`stairs`, `inner_stairs`, `outer_stairs`, `slab`, `fence_*`, `template_fence_gate*`, `pressure_plate_*`, `button*`) and full property-driven blockstates.
- Texture/model reference validation passed:
  - no missing model parents referenced by blockstates/models.
  - no missing textures referenced by models (`TOTAL_MISSING=0` after singular-path migration + placeholders).
  - JSON resource locations currently lowercase-safe.
- When purple-black reports happen, always verify log timestamp first and then re-check `latest.log` after a fresh client run.
- Verified on fresh run (`2026-02-07 20:18`): `Missing textures in model` / `Unable to load model` / `FileNotFoundException` count for Witchery assets is now `0`.
- Broad non-cube parity pass completed on `2026-02-08`:
  - reduced `models/block` `cube_all` placeholders from `51` -> `14`.
  - converted placeholder cubes to non-full approximations for:
    - plant-like: `bramble`, `somniancotton`, `spanishmoss`, `vine`, `web`, `witchsapling`, `lilypad`, `leapinglily`, `cactus`.
    - technical/invisible-like: `barrier`, `light`, `slurp`, `force`, `brewgas`.
    - fluid-like: `brew`, `brewliquid`, `disease`, `spiritflowing`, `hollowtears`.
    - portal-like: `spiritportal`, `tormentportal`.
    - decorative/machine approximations: `altar`, `kettle`, `spinningwheel`, `statueofworship`, `coffinblock`, `refillingchest`, `mirrorwall`, `infinityegg`, `clever`, `distilleryidle`, `distilleryburning`, `fumefunnel`, `filteredfumefunnel`, `witchesovenidle`, `witchesovenburning`.
  - fixed invalid slab item placeholders:
    - `models/item/icedoubleslab.json`, `models/item/snowdoubleslab.json`, `models/item/witchwooddoubleslab.json` now parent their block models (removed `taglockkit` placeholder ref).
  - remaining intentionally-full `cube_all` list:
    - `bloodedwool`, `icedoubleslab`, `perpetualice`, `pitdirt`, `pitgrass`, `shadedglass`, `shadedglass_active`, `snowdoubleslab`, `tormentstone`, `wallgen`, `wickerbundle`, `witchleaves`, `witchwood`, `witchwooddoubleslab`.
- Validation after this pass:
  - `./gradlew compileJava` passes.
  - all checked JSON files under `assets/witchery/blockstates`, `assets/witchery/models/block`, `assets/witchery/models/item` parse successfully.
- Horizontal-facing parity scaffold added (2026-02-08):
  - new reusable block class in `WitcheryBlocks` for placement-facing state (`HorizontalDirectionalBlock`).
  - IDs switched to this scaffold:
    - `altar`, `kettle`, `spinningwheel`, `distilleryidle`, `distilleryburning`, `witchesovenidle`, `witchesovenburning`, `fumefunnel`, `filteredfumefunnel`, `coffinblock`, `statueofworship`, `refillingchest`, `leechchest`.
  - matching blockstates now use `facing=north/east/south/west` variants with `y` rotations and `uvlock`.
  - compile validation after this change: `./gradlew compileJava` succeeded.
- Wood/bundle variant-state parity added (2026-02-08):
  - `witchwood`, `witchleaves`, `witchsapling` now use `wood_type=rowan|alder|hawthorn`.
  - `wickerbundle` now uses `bundle_type=plain|bloodied`.
  - new variant model files added for:
    - planks: `witchwood_rowan`, `witchwood_alder`, `witchwood_hawthorn`
    - leaves: `witchleaves_rowan`, `witchleaves_alder`, `witchleaves_hawthorn`
    - saplings: `witchsapling_rowan`, `witchsapling_alder`, `witchsapling_hawthorn`
    - wicker bundles: `wickerbundle_plain`, `wickerbundle_bloodied`
  - defaults for base block/item models now point to legacy default variants (`rowan` / `plain`).
  - compile + JSON validation after this change succeeded (`./gradlew compileJava`, JSON parse pass).
- Shaded glass color-state parity added (2026-02-08):
  - `shadedglass` and `shadedglass_active` now use enum property `color` (16 legacy values: white, orange, magenta, light_blue, yellow, lime, pink, gray, silver, cyan, purple, blue, brown, green, red, black).
  - generated variant model families:
    - `models/block/shadedglass_<color>.json` -> `shadedglassoff_<color>` textures
    - `models/block/shadedglass_active_<color>.json` -> `shadedglass_<color>` textures
  - item defaults now target white variant models.
  - compile + JSON validation succeeded after this pass.
- Plant solidity/culling parity pass added (2026-02-08):
  - root issue addressed: plant-style models were rendering as full-block solids because many legacy IDs still used default `Block` behavior.
  - `WitcheryBlocks` now maps plant-like IDs to non-occluding bounded block scaffolds:
    - `bloodrose`, `plantmine`, `glintweed`, `crittersnare`, `bramble`, `somniancotton`, `spanishmoss`, `vine`, `web`, `embermoss`, `lilypad`, `leapinglily`, `grassper`.
  - `witchsapling` moved from generic variant block to dedicated non-colliding sapling variant scaffold (`wood_type` preserved).
  - plant-family models updated to crossed render parents where appropriate:
    - `bloodrose`, `glintweed`, `embermoss`, `crittersnare`, `plantmine`.
- Client render layers added (2026-02-08):
  - new file: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/client/WitcheryClient.java`.
  - registers `RenderType.cutout()` for crop/plant/door family, `RenderType.cutoutMipped()` for `witchleaves`, and `RenderType.translucent()` for `shadedglass`, `shadedglass_active`, `perpetualice`.
  - note: API call is currently deprecated (`ItemBlockRenderTypes.setRenderLayer`) but valid for current Forge target.

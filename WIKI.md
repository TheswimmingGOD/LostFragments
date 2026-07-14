# Lost Fragments Wiki

Lost Fragments is a Fabric mod for Minecraft 26.1.2 by **tsg0d**. Its main mechanic is amethyst infusion: ordinary equipment and utility items keep their original identity and statistics but gain new abilities.

## Quick start

1. Craft an Infusion Table.
2. Put an eligible item in the left slot.
3. Put amethyst shards in the middle slot.
4. Take the infused result from the right slot.

Supplying the full shard cost always produces a stable infusion. Supplying fewer shards can produce a failed/fractured infusion.

## Infusion Table

The Infusion Table is used for every infusion in this mod.

### Recipe

| Slot | Left | Center | Right |
|---|---|---|---|
| Top | Amethyst Block | Echo Shard | Amethyst Block |
| Middle | Obsidian | Crafting Table | Obsidian |
| Bottom | Obsidian | Obsidian | Obsidian |

### Properties

- Can only be harvested with a pickaxe of any tier.
- Has extremely high explosion resistance.
- Uses the custom Lost Fragments table model and textures.
- The interface shows supplied shards, required shards, and failure risk.

## Infusion costs

| Item | Amethyst shards |
|---|---:|
| Book | 1 |
| Shovel | 2 |
| Hoe | 2 |
| Clock | 2 |
| Pickaxe | 3 |
| Axe | 3 |
| Sword | 3 |
| Compass | 3 |
| Helmet | 4 |
| Boots | 4 |
| Fishing Rod | 4 |
| Bundle of any colour | 4 |
| Leggings | 5 |
| Chestplate | 6 |
| Ender Chest | 8 |
| Cracked Catmen Talisman | 8 |

Infusion supports vanilla equipment and modded tools or armor that use the normal Minecraft tool and armor tags.

## Stable and failed infusions

Infused items do not receive an enchantment glint. A purple corner overlay distinguishes stable infused items in inventories.

When fewer shards than required are supplied, the failure chance is:

`missing shards / required shards`

For example, using two shards on an item that requires four gives a 50% failure chance.

A failed infusion is clearly identified by:

- A red inventory overlay.
- A red name beginning with **Failed Infusion**.
- A bold red **INFUSION FAILED** tooltip.

Failed equipment has **none of the positive infused abilities**. It can be placed back into the Infusion Table and retried. A retry costs twice the item's normal shard cost; supplying that full retry cost guarantees success.

Failed items still receive the applicable penalty until repaired:

- Failed tools and most held utility items cause Weakness I while held in either hand.
- Failed armor causes Slowness I while worn and pulses Darkness for five seconds every twenty seconds.
- Potion-effect particles are hidden.
- Failed armor cannot contribute to a matching full-set health bonus.
- Failed bundles use their special reduced-storage rules and do not cause Weakness.

## Infused Pickaxe

- Keeps the original pickaxe statistics, material, enchantments, and durability.
- Sneak while breaking a block to mine a 3×3 area.
- The plane follows the direction the player is looking.
- Only blocks the pickaxe can correctly harvest are included.
- Extra blocks consume durability normally.
- Purple particles appear only when at least one additional block is mined.

## Infused Shovel

- Keeps the original shovel statistics and material.
- Sneak while breaking a block to dig a 3×3 area.
- Uses the same view-dependent plane and correct-tool checks as the pickaxe.
- Particles appear only when the ability digs an additional block.

## Infused Axe

- Sneak while breaking a log to fell connected logs.
- Connected diagonal logs are included, allowing large jungle trees to be felled.
- The search is limited to 1,024 logs, a 16-block horizontal radius, 64 blocks above the first log, and 6 blocks below it.
- The axe intentionally cannot distinguish a natural tree from a hand-built log structure. Connected player-built logs can therefore be felled too.
- Particles appear only when additional logs are broken.

## Infused Hoe

The hoe has two 3×3 abilities. Both work horizontally regardless of the direction the player faces.

### Tilling

- Sneak-right-click the top of a tillable block.
- Tills the surrounding 3×3 area using normal hoe rules.

### Harvesting and clearing

- Sneak while breaking a supported plant.
- Breaks matching plants in a 3×3 area on the same level.
- Supports crops, flowers, short grass, tall grass, short and tall dry grass, ferns, large ferns, and leaf litter.
- Particles appear only when the ability changes additional blocks.

## Infused Sword

- Right-click to release a knockback pulse in a 2.5-block radius.
- The pulse deals no direct damage.
- It ignores the user, allies, pets/allied entities, and targets behind walls.
- Every activation costs four sword durability and starts a six-second cooldown, even when it misses.
- Purple particles appear only if an entity is actually moved.

## Infused Armor

All pieces keep their original armor statistics and material.

### Helmet

- Grants Night Vision while worn.
- Night Vision is maintained with a longer duration so it does not constantly flicker.

### Chestplate

- Grants Resistance I while worn.

### Leggings

- Increase walking speed by 10%.
- Increase crouching speed by 10%.
- Improve swimming movement by 0.1.

### Boots

- Reduce fall damage by 25%.
- Prevent the wearer from trampling farmland.

### Matching full-set bonus

Wearing an infused helmet, chestplate, leggings, and boots made from the same armor material grants bonus maximum health. Purple particles pulse around the player once per second to indicate an active matching set.

| Armor tier/material | Bonus health |
|---|---:|
| Leather or similarly weak modded set | 1 point (half a heart) |
| Gold or Chainmail | 2 points (1 heart) |
| Copper or Iron | 3 points (1.5 hearts) |
| Diamond | 4 points (2 hearts) |
| Netherite | 5 points (2.5 hearts) |

Unknown modded materials are assigned a tier from their total armor value. Every piece must have a successful infusion for the full-set bonus to activate.

## Book of Infusion

- Created by infusing a normal Book with one amethyst shard.
- Opens like a written book when used.
- Contains a beginner guide to the Infusion Table, shard costs, failure risks, tool and armor abilities, utility items, controls, fragment locations, Resonant Ender Chests, bundles, and Catmen Talismans.
- Written by tsg0d and designed to be carried as an in-game reference.

## Infused Fishing Rod

The infused rod stores an Ender Pearl and one personal teleport point.

### Controls

1. Hold the infused rod and hold an Ender Pearl in the other hand.
2. Sneak-right-click to load and consume the pearl.
3. Cast the fishing rod.
4. When the hook lands or settles, its location is stored in that exact rod and the hook retracts.
5. The rod can now be moved anywhere in the inventory.
6. Right-click with that rod to teleport to its saved point.

Successful teleportation consumes the loaded pearl and clears the saved point. Multiple rods can hold separate points.

### Restrictions

- Teleportation must remain in the same dimension.
- The destination must have room for the player.
- The base maximum distance is 1,500 blocks.
- Infused rods only retain Unbreaking, Mending, and Ender Reach. Other enchantments are removed.

### Ender Reach

| Level | Maximum distance |
|---|---:|
| No Ender Reach | 1,500 blocks |
| Ender Reach I | 3,000 blocks |
| Ender Reach II | 4,500 blocks |
| Ender Reach III | 6,000 blocks |

## Infused Compass

- Use the compass on another player to bind it to that player.
- Its needle follows the selected player while the target is online and in the same dimension.
- The needle spins when the target is offline or in another dimension.
- Sneak-right-click the air to clear the target.
- The tooltip displays the tracked player's name.

## Infused Clock

- Shows the current Overworld day and time in its tooltip.
- Right-click to display the same information in the action bar.
- Always uses Overworld time, even when carried in another dimension.

## Resonant Ender Chest

Infusing a normal Ender Chest produces a Resonant Ender Chest. It uses the normal Minecraft Ender Chest model and texture.

### Frequencies

- On first opening, place an item in the special frequency slot.
- The item type becomes the chest's frequency key.
- Chests using the same key share 27 storage slots.
- A frequency supports up to four registered chests.
- Frequency storage works across dimensions in the same world/server.
- The frequency item is locked in place and drops when its chest is broken.

### Storage and automation

- Players can insert and remove items normally.
- Hoppers and droppers can insert into and extract from the 27 shared storage slots.
- Automation cannot access the frequency slot.
- Comparators read the chest's contents.
- Breaking one of several linked chests does not drop the shared inventory.
- Breaking the last chest on a frequency drops all stored contents into the world.

## Infused Bundle

Any vanilla bundle colour can be infused. The result preserves its colour and displays a name such as **Infused Red Bundle**.

### Stable bundle

- Holds up to eight non-stackable items.
- Rejects stackable items.
- Rejects nested container items.

### Failed bundle

- Cannot store non-stackable items.
- Stores stackable items only.
- Receives a permanent random capacity between 16 and 48 total items when created.
- Does not cause Weakness.

### Inventory controls

- Primary-click with an item to insert as much as the bundle can accept.
- Secondary-click with an empty destination to remove the last stored stack.
- The tooltip displays current usage and maximum capacity.

## Lost Corner Fragment

A rare component used in the four corners of the Cracked Catmen Talisman recipe.

## Lost Side Fragment

A rare component used on the four sides of the Cracked Catmen Talisman recipe.

## Fragment locations

Each eligible loot source has an 8% chance to produce one randomly selected Corner or Side Fragment.

- Ancient City chests and ice boxes in the Deep Dark.
- End City treasure chests.
- Desert Temple chests.
- Jungle Temple chests.
- Buried treasure.
- Shipwreck map, supply, and treasure chests.
- Suspicious sand and gravel using the Desert Well, Desert Pyramid, Trail Ruins, Warm Ocean Ruin, and Cold Ocean Ruin archaeology loot tables.

## Cracked Catmen Talisman

The cracked talisman is useless until infused.

### Recipe

| Slot | Left | Center | Right |
|---|---|---|---|
| Top | Lost Corner Fragment | Lost Side Fragment | Lost Corner Fragment |
| Middle | Lost Side Fragment | Empty | Lost Side Fragment |
| Bottom | Lost Corner Fragment | Lost Side Fragment | Lost Corner Fragment |

Infuse the result with eight amethyst shards to create a Catmen Talisman.

## Catmen Talisman

- Works from anywhere in the player's normal inventory; it does not need to be held.
- Prevents any lethal damage while it has uses remaining.
- Each talisman has eight death saves.
- The tooltip shows the number of remaining uses.
- The talisman disappears after its eighth activation.
- Activation leaves the player at 1 health, extinguishes fire, clears existing status effects, and grants Regeneration II, Absorption II, and Fire Resistance.
- Uses the normal Totem of Undying screen animation with the Catmen Talisman's own texture, plays the Totem sound, and creates a large purple particle burst instead of vanilla Totem particles.
- A void activation teleports the player to the Overworld world spawn before applying the rescue effects.

## Visual language

- Stable infused items: purple inventory-corner overlay.
- Failed infused items: red overlay, red name, and failure tooltip.
- No forced enchantment glint.
- Tool ability particles appear only when the ability affects something, except effects such as the full armor-set aura and talisman rescue that intentionally indicate an active state.

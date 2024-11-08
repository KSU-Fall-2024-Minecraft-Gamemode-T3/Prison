# Minecraft Old School Prison Plugin

This Prison gameplay mode features old school Prison features and\ where there is a limited number of mines, this server plugin runs along a Paper API server jar. The plugin allows the use of the LuckPerms API for the rank administration as well as Vault for the economy.

### Features

**Prison Menu:** Players have a custom GUI which they can interact with and perform such functions as selling items, teleporting to spawn, and going up the prison ranks. 

**PVP Zones:** Players are able to battle in special zones to steal each others items.

**Ranks:** A member can see the ranks, their progression and the costs for the new ranks, money can be spent to gain a higher rank.

**Integration With LuckPerms:** This manages the player ranks. It mainly deals with the rank-ups granting new mines.

**Integration With Vault Economy:** This manages in-game money for rank-ups and shops.

**Custom Menus:** This feature has adjustable menus and rank progression which advancement is stored in the config.yml.

**Custom Events:** When a player tries to drop or attempt to move the compass they are denied.

**Shop Villagers:** When a player wants to sell the items they worked hard to mine, they will go to these special villagers that will happily buy your stock

### Commands

>/prison ksu

Tells the player "Go Owls!" in green and gold.

>/prison

Open the main prison menu.

>/prison help

When help is requested this menu shows the available commands for use depending on permissions.

>/ranks

This shows rank progression and the cost to buy up into the next ranks.

>/rankup

This will take the player to the next immediate rank if they have the money to buy it.

>/prison reload

Reloads this plugin and configuration files.

>/mines

Lists the current mines that are listed in the mines.yml

>/mine reset <minename>

Refills a specified mine, the name of the mine must be in the mines.yml file

>/warp <location>

Teleports the user to a specified mine or to the cells area

### Configuration

The ranking system and its corresponding specification as to how much they will cost for each rank available to players have been implemented in the config.yml under the ranks list. The prices of the items they collect and sell are also values that can be changed in the config.yml file as well as the price of rent a prison cell and how long you can rent it.

The permissions for admins are set in the plugin.yml file as well as defining each of the commands specific to this plug-in such as /prison.

Example: 
```
#RANKS AND PRICING
ranks:
  D:
    next_rank: C
    price: 18000
  C:
    next_rank: B
    price: 50000
  B:
    next_rank: A
    price: 190000
  A:
    next_rank: K
    price: 500000
  K:
    next_rank: S
    price: 1300000
  S:
    next_rank: U
    price: 2500000

# Define cell rental prices and durations by region
cells:
  d:
    price: 1000       # Price for D cells
    duration: 7       # Duration in days for renting D cells
  c:
    price: 1500       # Price for C cells
    duration: 7       # Duration in days for renting C cells
  b:
    price: 2500       # Price for B cells
    duration: 7       # Duration in days for renting B cells
  a:
    price: 4000       # Price for A cells
    duration: 7       # Duration in days for renting A cells

#ITEMS AND PRICING
sellable-items:
  COBBLESTONE:
    price: 1.00
  OAK_LOG:
    price: 1.20
  IRON_INGOT:
    price: 7.70
  IRON_ORE:
    price: 4.60
  COAL:
    price: 3.40
  LAPIS_LAZULI:
    price: 2.80
```

### Code Structure

**Prison.java:** This is where the main plugin logic resides which is the one instantiating the plugin, managing commands, and setting up managers.

**Menus.java:** This class is in charge of creating all unique menus and defining their operations and actions.

--**Managers**--

**EconomyManager.java:** Sets up the Economy and all the interactions that are executed and controlled by Vault. Players attempting to buy something will have their request taken here. 

**RankManager.java:** Management of ranks and the process of raising a player's rank to the next level.

**Mine.java:** Class that handles the logic of all the commands of the /mines command with multiple constructors for each command. Includes the logic to teleport players to a safe spot when resetting a mine.

**MineManager.java:** Contains the methods of listing the available mines in-game. Contains the method to restore a specified mine using worldguard and FAWE.

**ShopVillagerManager.java:** Creates the shop villagers players sell their items at specified locations across the map.

-- **Listeners** --

**EventListener.java:** This class is responsible for handling events caused by the Player such as clicking items in an inventory, a player joining, the dropping of their items, and others.

**FishingListener.java:** This class tracks if a player is fishing and will change any caught item into a raw cod.

**SellMenuListener.java:** The class that manages the shop menu created by interacting with a shop villager. Implements several checks to make sure the player is not able to move around important items in both their menu and the shop. This is to protect the player from accidentally losing their items.

**SellMenuHolder.java:** An abstract class that defines an InventoryHolder for the sell menu proper.

**ShopListener.java:** A class that allows the player to interact with a shop villager and open their shop menu.  It also contains the method that makes the shop villagers invincible.

--**Commands**--

**CellsComand.java:** The class that allows a player to rent out a specified prison cell. Requires the specific cell number to funciton and reads from the config.yml file for pricing and rent time.

**MinesCommand.java:** |Administrator Only| A class that lists out a number of options an administrator can use for mines. The options include listing out the current mines, reset a mine back to full, reload the mines.yml file, make a new mine, or set a mine's position coordinates on your current position

**RanksCommand.java:** A class that will read the config.yml file for the available ranks and their current prices. The listed prices will reflect the prices in the config file.

**RankUpCommand.java:** Small class that will activate the rankup command on the player before returning.

**SellAllCommand.java:** Depreciated class that would sell all of the player's inventory that can be sold in the shop menu. Not in use anymore

**SellCommand.java:** Depreciated class that used to open the shop menu on player command, not currently in use anymore.

### Dependencies
```
Paper (Minecraft server jar)
LuckPerms (Permissions system)
Vault (Economy management)
FAWE (Mines management)
```

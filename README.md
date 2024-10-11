# Minecraft Old School Prison Plugin

This Prison gameplay mode features old school Prison features and\ where there is a limited number of mines, this server plugin runs along a Paper API server jar. The plugin allows the use of the LuckPerms API for the rank administration as well as Vault for the economy.

### Features

**Prison Menu:** Players have a custom GUI which they can interact with and perform such functions as selling items, teleporting to spawn, and going up the prison ranks. 

**Ranks:** A member can see the ranks, their progression and the costs for the new ranks, money can be spent to gain a higher rank.

**Integration With LuckPerms :** This manages the player ranks. It mainly deals with the rank-ups granting new mines.

**Integration With Vault Economy:** This manages in-game money for rank-ups and shops.

**Custom Menus:** This feature has adjustable menus and rank progression which advancement is stored in the config.yml.

**Custom Events:** When a player tries to drop or attempt to move the compass they are denied.

### Commands

>/ksu

Tells the player "Go Owls!" in gold.

>/prison

Open the main prison menu.

>/prison help

When help is requested this menu shows the available commands for use depending on permissions.

>/ranks

This shows rank progression and the how much ranks can be bought.

>/rankup

This will help the player to get to the next level, if they has enough money.

>/prison reload

Reloads this plugin and configuration files.

>/sell

Opens up a gui where items can be sold individually.

>sellall

Sells all defined items in the players inventory

>/mines

Lists the current mines that are listed in the mines.yml

>/minereset <minename>

Refills the mine defined

### Configuration

The ranking system and its corresponding specification as to how much they will cost for each rank available to players have been implemented in the config.yml under the ranks list.

Example: 
```
ranks:
  D:
    next_rank:C
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
```

### Code Structure

**Prison.java:** This is where the main plugin logic resides which is the one instantiating the plugin, managing commands, and setting up managers.

**Menus.java:** This class is in charge of the menus creation and its operations and actions.

**EconomyManager.java:** Economy interations are executed and controlled by Vault.

**RankManager.java:** Management of ranks and the process of promotions are done by the means of LuckPerms.

**EventListener.java:** This is element is responsible for handling events which occur like clicking items in an inventory, a player joining, dropping of items and others.

### Dependencies
```
Paper (Minecraft server jar)
LuckPerms (Permissions system)
Vault (Economy management)
FAWE (Mines management)
```

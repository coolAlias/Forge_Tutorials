/**
 * Adding Custom Trades to Villagers
 */
/*
Adding trades to villagers is easy. All you need to do is create a class that
implements IvillageTradeHandler and register it to the VillagerRegistry.

I will also show you how to add a variety of trades, including using metadata items,
adding variable stacksizes and adding trades for more than one item.
*/

/**
 * Step 1: Registering your custom TradeHandler for each villager type
 */
@EventHandler
public void preInit(FMLPreInitializationEvent event)
{
	// iterate through all the villager types and add their new trades
	for (int i = 0; i < 5; ++i) {
		VillagerRegistry.instance().registerVillageTradeHandler(i, new TradeHandler());
	}
}

/**
 * Step 2: Writing your TradeHandler class
 *
 */
public class TradeHandler implements IVillageTradeHandler
{
	@Override
	public void manipulateTradesForVillager(EntityVillager villager, MerchantRecipeList recipeList, Random random)
	{
		switch(villager.getProfession()) {
		case 0: // FARMER
			// standard trade
			recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 2), new ItemStack(YourMod.youritem, 1)));
			break;
		case 1: // LIBRARIAN
			// use metadata in either case
			recipeList.add(new MerchantRecipe(new ItemStack(Item.dye, 4, 15), // dye of metadata 15 is bonemeal, so we need 4 bonemeals
					new ItemStack(YourMod.youritem, 1, 6))); // to buy 1 mod item of metadata value 6
			break;
		case 2: // PRIEST
			// trading two itemstacks for one itemstack in return
			recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 6), new ItemStack(YourMod.youritem1, 2), new ItemStack(YourMod.youritem2, 2)));
			break;
		case 3: // BLACKSMITH
			// using the passed in Random to randomize amounts; nextInt(value) returns an int between 0 and value (non-inclusive)
			recipeList.add(new MerchantRecipe(new ItemStack(Item.emerald, 6 + random.nextInt(6)), new ItemStack(YourMod.youritem1, 5 + random.nextInt(4)), new ItemStack(YourMod.youritem2, 1)));
			break;
		case 4: // BUTCHER
			// You can also add directly to the villager with 2 different methods:

			// Method 1: takes the list, an item ID that may be bought OR sold, rand, and a float value that
			// determines how common the trade is. The price of the item is determined in the HashMap
			// blacksmithSellingList, which we'll add our custom Item to first:
			villager.blacksmithSellingList.put(Integer.valueOf(YourMod.yourItem.itemID), new Tuple(Integer.valueOf(4), Integer.valueOf(8)));
			// Then add the trade, which will buy or sell for between 4 and 8 emeralds
			villager.addBlacksmithItem(recipeList, ItemToTrade.itemID, random, 0.5F);

			// Method 2: Basically the same as above, but only for selling items and at a fixed price of 1 emerald
			// However, the stack sold will have a variable size determined by the HashMap villagerStockList,
			// to which we first need to add our custom Item:
			villager.villagerStockList.put(Integer.valueOf(YourMod.YourItem.itemID), new Tuple(Integer.valueOf(16), Integer.valueOf(24)));
			// Then add the trade, which will sell between 16 and 24 of our Item for 1 emerald
			villager.addMerchantItem(recipeList, ItemToSell.itemID, random, 0.5F);
			break;
		default:
			break;
		}
	}
}

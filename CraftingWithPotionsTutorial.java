/**
 * Using Potions in Crafting Recipes
 */
/*
For this tutorial, I will explain how to use a potion in a crafting recipe, either
as an ingredient, or as a result. 

Just in case you don't already know, we will first cover how to set up a basic
recipe, then move on to potions and then on to something more advanced.
*/

/**
 * PART 1: Basic Crafting and Crafting with Potions
 */
// Recipes are usually declared from within the load method in your main mod class:
@EventHandler
public void load(FMLInitializationEvent event)
{
	/*
	addShapelessRecipe takes an ItemStack argument; this is the item that will be crafted.
	Note that you can also modify the stacksize of the crafting result and set metadata:
		new ItemStack(YourModName.yourItemName, stacksize, metadata)
	You can just use the first argument and stacksize will default to 1, metadata to 0
	
	The following arguments are all of the blocks / items needed to craft it, in any order.
	In the example, a block of dirt and a stick will craft yourItemName.
	*/
	GameRegistry.addShapelessRecipe(new ItemStack(YourModName.yourItemName), Block.dirt, Item.stick);
	
	/*
	addRecipe takes the same first argument, followed by 3 strings.
	Each string represents a horizontal line in the crafting grid, using different characters to represent the different items needed. Here we've used "x", "#" and "z", but you can use whatever you want. Use as many different characters as you need.

	Following this representation of the crafting grid, we define our variables like so: 'x', Object1, '#', Object2, etc.,
	where each Object is a Block, Item or ItemStack. You can use your custom items in the recipe simply by referring to the instance you created in your class, i.e. using its name.
	 */
	GameRegistry.addRecipe(new ItemStack(YourModName.yourItemName), "xxx", "###", "zzz", 'x', Block.dirt, '#', Item.stick, 'z', yourItemName);
}
// Here's a concrete example of a very basic item, a ThrowingRock
// This declares the item; it goes in the main mod
public static final Item throwingRock = new ItemThrowingRock(modItemIndex++).setUnlocalizedName("throwingRock");

// This recipe is shapeless and turns one cobblestone block into 9 throwingRocks.
// Note that we're using the second argument of ItemStack to return a stack of 9 throwingRocks
// but we can still leave out the metadata value
GameRegistry.addShapelessRecipe(new ItemStack(MyMod.throwingRock, 9), Block.cobblestone);

// This recipe has a shape (all 9 squares of the crafting grid) and turns 9 throwing rocks
// back into a cobblestone block.
GameRegistry.addRecipe(new ItemStack(Block.cobblestone), "xxx", "xxx", "xxx", 'x', throwingRock);

/*
Remember to add recipes in the load method.

Ok, now we're ready to tackle using potions in our recipes. It's actually quite simple.
We need to use the 3rd argument in ItemStack - itemDamage. While most items use this to
keep track of wear and tear, potions use it as a secondary ID.

For example, we can craft a damaged wooden sword from a stick like so:
*/
GameRegistry.addShapelessRecipe(new ItemStack(Item.swordWood,1,25), Item.stick);
/*
The sword will already have 25 damage to its durability when crafted 'brand new'

We need to make a stack of Item.potion with an itemDamage value that matches the'id' of
the potion we want. But what are the potion ID values? The easiest course of action is
to check the minecraft wiki here: http://www.minecraftwiki.net/wiki/Potions

Halfway down the page is a Data Value Table that shows the itemDamage values for every
potion currently in game, as well as those in the code. Let's look at an example.

Let's say I want to use a Potion of Poison in my recipe. Looking at the wiki table above,
I see a Regular Potion of Poison has an iD of 8196. So my recipe looks like this:
*/
// This one will craft my mod item from a Potion of Poison:
GameRegistry.addShapelessRecipe(new ItemStack(YourMod.yourModItem), new ItemStack(Item.potion,1,8196));
// This one will craft a Potion of Poison from my mod item:
GameRegistry.addShapelessRecipe(new ItemStack(Item.potion,1,8196), new ItemStack(YourMod.yourModItem));
/*
Obviously, you can make your recipes as simple or complex as you like.

Well, that's pretty easy to do, but not very intuitive. It's not obvious just from
looking at the code what (Item.potion,1,8196) is, which I personally don't like.

If you're up for something a little more advanced that will allow you to replace 8196
with a readable name, read on. Otherwise, you should be good to go - just check the wiki
for data values of potions anytime you need them!
*/
/**
 * PART 2: 
 */
/*
For this, we're going to take advantage of the type 'enum.' This allows us to define
constant values in the format of CONSTANT_NAME(value1, value2,...). So we'll set up a
very basic enum class called EnumPotionID like so:
*/
public enum EnumPotionID
{

	POISON(8196),

	private final int potionID;

	private EnumPotionID(int par1) {
		this.potionID = par1;
	}

	public int id()
	{
		return this.potionID;
	}
}
/*
For more information about the enum type, see: http://docs.oracle.com/javase/tutorial/java/javaOO/enum.html

This allows us to call EnumPotionID.POISON.id(), which will return the
value 8196, much like calling Item.swordWood returns the correct Item, which we can
then call functions for with the same syntax.

So now our recipe will look like this:
*/
GameRegistry.addShapelessRecipe(new ItemStack(Item.potion,1,EnumPotionID.POISON.id()), new ItemStack(YourMod.yourModItem));
/*
Much more readable, isn't it?

There is also the advantage that if the developers ever change the potion iDs, you will
only need to update your EnumPotionID class values and everywhere else in your code will
automatically be updated.

Well that's all fine and dandy, but what if you want to make an item craftable from ANY
version of a potion? Say, for example, you want a Potato + any version of a Poison
potion to make a Poisoned Potato. You could code all the recipes by hand. But that
wouldn't be very interesting.

First, you will need to implement the EnumPotionID class as above, or simply input all
the values by hand. Here's my full version of EnumPotionID:
*/
public enum EnumPotionID
{
	/* POTION DATA VALUES from http://www.minecraftwiki.net/wiki/Potions
	 * EXT means "Extended" version of potion
	 * REV means "Reverted" version of potion
	 */
	POTION_AWKWARD(16),
	POTION_THICK(32),
	POTION_MUNDANE(128),
	POTION_MUNDANE_EXT(64),

	/*
	 * HELPFUL POTIONS
	 */
	POTION_REGEN(8193),
	POTION_REGEN_II(8225),
	POTION_REGEN_EXT(8257),
	POTION_REGEN_II_EXT(8289),
	POTION_REGEN_SPLASH(16385),
	POTION_REGEN_SPLASH_II(16417),
	POTION_REGEN_SPLASH_EXT(16449),

	POTION_SWIFTNESS(8194),
	POTION_SWIFTNESS_II(8226),
	POTION_SWIFTNESS_EXT(8258),
	POTION_SWIFTNESS_II_EXT(8290),
	POTION_SWIFTNESS_SPLASH(16386),
	POTION_SWIFTNESS_SPLASH_II(16418),
	POTION_SWIFTNESS_SPLASH_EXT(16450),

	POTION_FIRERESIST(8195),
	POTION_FIRERESIST_REV(8227),
	POTION_FIRERESIST_EXT(8259),
	POTION_FIRERESIST_SPLASH(16387),
	POTION_FIRERESIST_SPLASH_REV(16419),
	POTION_FIRERESIST_SPLASH_EXT(16451),

	POTION_HEALING(8197),
	POTION_HEALING_II(8229),
	POTION_HEALING_REV(8261),
	POTION_HEALING_SPLASH(16389),
	POTION_HEALING_SPLASH_II(16421),
	POTION_HEALING_SPLASH_REV(16453),

	POTION_NIGHTVISION(8198),
	POTION_NIGHTVISION_REV(8230),
	POTION_NIGHTVISION_EXT(8262),
	POTION_NIGHTVISION_SPLASH(16390),
	POTION_NIGHTVISION_SPLASH_REV(16422),
	POTION_NIGHTVISION_SPLASH_EXT(16454),

	POTION_STRENGTH(8201),
	POTION_STRENGTH_II(8233),
	POTION_STRENGTH_EXT(8265),
	POTION_STRENGTH_II_EXT(8292),
	POTION_STRENGTH_SPLASH(16393),
	POTION_STRENGTH_SPLASH_II(16425),
	POTION_STRENGTH_SPLASH_EXT(16457),

	POTION_INVISIBILITY(8206),
	POTION_INVISIBILITY_REV(8238),
	POTION_INVISIBILITY_EXT(8270),
	POTION_INVISIBILITY_SPLASH(16398),
	POTION_INVISIBILITY_SPLASH_REV(16430),
	POTION_INVISIBILITY_SPLASH_EXT(16462),

	/*
	 * HARMFUL POTIONS
	 */
	POTION_POISON(8196),
	POTION_POISON_II(8228),
	POTION_POISON_EXT(8260),
	POTION_POISON_SPLASH(16388),
	POTION_POISON_SPLASH_II(16420),
	POTION_POISON_SPLASH_EXT(16452),

	POTION_WEAKNESS(8200),
	POTION_WEAKNESS_REV(8232),
	POTION_WEAKNESS_EXT(8264),
	POTION_WEAKNESS_SPLASH(16392),
	POTION_WEAKNESS_SPLASH_REV(16424),
	POTION_WEAKNESS_SPLASH_EXT(16456),

	POTION_SLOWNESS(8202),
	POTION_SLOWNESS_REV(8234),
	POTION_SLOWNESS_EXT(8266),
	POTION_SLOWNESS_SPLASH(16394),
	POTION_SLOWNESS_SPLASH_REV(16426),
	POTION_SLOWNESS_SPLASH_EXT(16458),

	POTION_HARM(8204),
	POTION_HARM_II(8236),
	POTION_HARM_REV(8268),
	POTION_HARM_SPLASH(16396),
	POTION_HARM_SPLASH_II(16428),
	POTION_HARM_SPLASH_REV(16460);

	private final int potionID;

	private EnumPotionID(int par1) {
		this.potionID = par1;
	}

	public int id()
	{
		return this.potionID;
	}
}

// Next, we're going to make an array "poisonPotions" like so:
public static final Object[] poisonPotions = {
		new ItemStack(Item.potion,1,EnumPotionID.POISON.id()),
		new ItemStack(Item.potion,1,EnumPotionID.POISON_II.id()),
		new ItemStack(Item.potion,1,EnumPotionID.POISON_EXT.id()),
		new ItemStack(Item.potion,1,EnumPotionID.POISON_SPLASH.id()),
		new ItemStack(Item.potion,1,EnumPotionID.POISON_SPLASH_II.id()),
		new ItemStack(Item.potion,1,EnumPotionID.POISON_SPLASH_EXT.id())
		};

// Then you would add recipes like this:
for (int i = 0; i < EnumPotionID.poisonPotions.length; ++i)
{
	GameRegistry.addShapelessRecipe(new ItemStack(Item.poisonousPotato), Item.potato, EnumPotionID.poisonPotions[i]);
}
/*
If you don't know about arrays, here's a brief explanation:

An array is basically an ordered list of objects of any type. The array is accessed
using brackets and the place of the object you want in the array, such that arrayName[0]
returns the first object in the array, and arrayName[arrayName.length-1] returns the
last object in the array.

Arrays are very powerful tools, but if you're new to them, you're likely crash to your
game by throwing null pointer exceptions and not know why, as they won't show up as
errors in your code. Array errors can be difficult to pinpoint, and a solid understanding
of their functionality will go a long way in helping you.

Learn more here: http://docs.oracle.com/javase/tutorial/java/nutsandbolts/arrays.html

Ok, so we've used an array to auto-generate our crafting recipes for us. Great. But say for some reason you want every potion type craftable into your new item? For example, you want to make a new FoodItems that confers potion effects based on the potion it was crafted with. Assuming you have your newFoodItem class set up and only need to generate recipes, here's what you could do:
*/
/*
* Object[][] potionMatrix is set up so that the first
* bracket identifies the potion type (e.g. POISON) and
* the second bracket the specific instance of that potion
* (e.g. POISON_EXT)
*/
Object[][] potionMatrix =
{{	new ItemStack(Item.potion,1,EnumPotionID.POISON.potionID()),
	new ItemStack(Item.potion,1,EnumPotionID.POISON_II.potionID()),
	new ItemStack(Item.potion,1,EnumPotionID.POISON_EXT.potionID()),
	new ItemStack(Item.potion,1,EnumPotionID.POISON_SPLASH.potionID()),
	new ItemStack(Item.potion,1,EnumPotionID.POISON_SPLASH_II.potionID()),
	new ItemStack(Item.potion,1,EnumPotionID.POISON_SPLASH_EXT.potionID())
},
{	new ItemStack(Item.potion,1,EnumPotionID.WEAKNESS.potionID()),
	new ItemStack(Item.potion,1,EnumPotionID.WEAKNESS_REV.potionID()),
	new ItemStack(Item.potion,1,EnumPotionID.WEAKNESS_EXT.potionID()),
	new ItemStack(Item.potion,1,EnumPotionID.WEAKNESS_SPLASH.potionID()),
	new ItemStack(Item.potion,1,EnumPotionID.WEAKNESS_SPLASH_REV.potionID()),
	new ItemStack(Item.potion,1,EnumPotionID.WEAKNESS_SPLASH_EXT.potionID())
},
{etc.}};

/*
* Object[] yourModItems contains the corresponding new food items for
* each potion effect. There MUST be the same number of new items as
* there are Potion types in potionMatrix[][] or you WILL throw null
* pointer exceptions and crash your game.
*/
Object[] yourModItems = {YourMod.breadPoison, YourMod.breadWeakness, etc.};

// Iterates through the base potion types
for (int j = 0; j < potionMatrix.length; ++j)
{
	// Iterates through the specific potions of a single type
	// Be sure to check the length of this array as well, because it may not be the
	// same for every index [j]
	for (int k = 0; k < potionMatrix[j].length; ++k)
	{
		Object potioninrecipe = potionMatrix[j][k];
		GameRegistry.addShapelessRecipe(new ItemStack(yourModItems[j]), new Object[] {Item.bread, potioninrecipe});
	}
}
/*
This would generate all the recipes you need to make your Weak Bread, Poisoned Bread,
etc., as you define in your own class. If you're like me and don't like to have lots of
code mucking up your main mod class, just create a new class RegisterCraftingRecipes
with an addRecipe() function and call it like so:
*/
(new RegisterCraftingRecipes()).addRecipes();
/*
Then you can put all that stuff there and your main mod will look nice and tidy!

Good luck with your Potion recipes :) 
*/
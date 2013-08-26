/**
 * Extended Entity Properties Tutorial
 */
/*
In this tutorial I will cover how to add variables to an entity by using Forge's
IExtendedEntityProperties. How did I learn all this stuff, you ask? Well, it's
all pretty well documented within the Forge code itself. Hover your mouse over
most Forge methods and you'll get a great 'tooltip' pop up that explains pretty
much everything about it. Or just open up the class you're curious about. Go ahead,
give it a try! If it still doesn't make sense, then read on :)

We will add mana to all players, show how to use it, and add gold coins to all
EntityLivingBase entities.

Prerequisites:
1. Know how to set up and use Forge Events. See my tutorial on creating an EventHandler.
2. Willingness to read carefully.

Step 1: Create a class that implements IExtendedEntityProperties
Since we are first making variables specific to EntityPlayer, we will call this
class "ExtendedPlayer" so it's always obvious what kind of entity can use it. This
will be important if you add different variables to different entities.
*/
public class ExtendedPlayer implements IExtendedEntityProperties
{
	/*
	Here I create a constant EXT_PROP_NAME for this class of properties
	You need a unique name for every instance of IExtendedEntityProperties
	you make, and doing it at the top of each class as a constant makes
	it very easy to organize and avoid typos. It's easiest to keep the same
	constant name in every class, as it will be distinguished by the class
	name: ExtendedPlayer.EXT_PROP_NAME vs. ExtendedEntity.EXT_PROP_NAME
	
	Note that a single entity can have multiple extended properties, so each
	property should have a unique name. Try to come up with something more
	unique than the tutorial example.
	*/
	public final static String EXT_PROP_NAME = "ExtendedPlayer";
	
	// I always include the entity to which the properties belong for easy access
	// It's final because we won't be changing which player it is
	private final EntityPlayer player;
	
	// Declare other variables you want to add here
	
	// We're adding mana to the player, so we'll need current and max mana
	private int currentMana, maxMana;
	
	/*
	The default constructor takes no arguments, but I put in the Entity
	so I can initialize the above variable 'player'
	
	Also, it's best to initialize any other variables you may have added,
	just like in any constructor.
	*/
	public ExtendedPlayer(EntityPlayer player)
	{
		this.player = player;
		// Start with max mana. Every player starts with the same amount.
		this.currentMana = this.maxMana = 50;
	}
	
	// Save any custom data that needs saving here
	@Override
	public void saveNBTData(NBTTagCompound compound)
	{
		// We only need to save current and max mana
		compound.setInteger("CurrentMana", this.currentMana);
		compound.setInteger("MaxMana", this.maxMana);
	}

	// Load whatever data you saved
	@Override
	public void loadNBTData(NBTTagCompound compound)
	{
		this.currentMana = compound.getInteger("CurrentMana");
		this.maxMana = compound.getInteger("MaxMana");
		// Just so you know it's working, add this line:
		System.out.println("[TUT PROPS] Mana from NBT: " + this.currentMana + "/" + this.maxMana);
	}
	
	/*
	I personally have yet to find a use for this method. If you know of any,
	please let me know and I'll add it in! 
	*/
	@Override
	public void init(Entity entity, World world)
	{
	}
	
	/*
	That's it for the IExtendedEntityProperties methods, but we need to add
	a few of our own in order to interact with our new variables. For now,
	let's make one method to consume mana and one to replenish it.
	 */
	
	/**
	 * Returns true if the amount of mana was consumed or false
	 * if the player's current mana was insufficient
	 */
	public boolean consumeMana(int amount)
	{
		// Does the player have enough mana?
		boolean sufficient = amount <= this.currentMana;
		// Consume the amount anyway; if it's more than the player's current mana,
		// mana will be set to 0
		this.currentMana -= (amount < this.currentMana ? amount : this.currentMana);
		// Return if the player had enough mana
		return sufficient;
	}
	
	/**
	 * Simple method sets current mana to max mana
	 */
	public void replenishMana()
	{
		this.currentMana = this.maxMana;
	}
}
/*
Step 2: Register the ExtendedPlayer class in your EventHandler
In order to access our newly created extended player properties, we need to
register them for every instance of EntityPlayer. That just means we're
creating a new instance of the class so we can access it.

Registering of IExtendedEntityProperties is all done in the EntityConstructing event.
*/
public class TutEventHandler
{
	@ForgeSubscribe
	public void onEntityConstructing(EntityConstructing event)
	{
		/*
		Be sure to check if the entity being constructed is the correct type
		for the extended properties you're about to add!
		The null check may not be necessary - I only use it to make sure
		properties are only registered once per entity
		*/
		if (event.entity instanceof EntityPlayer &&
				event.entity.getExtendedProperties(ExtendedPlayer.EXT_PROP_NAME) == null)
		{
			/*
			Note that the constant EXT_PROP_NAME is used all over the place
			Each time you use it is one less chance you have to make a typo
			and one less instance to change if you ever change the name :)
			*/
			// This is how extended properties are registered:
			event.entity.registerExtendedProperties(ExtendedPlayer.EXT_PROP_NAME,
					new ExtendedPlayer((EntityPlayer) event.entity));
			// That will call the constructor as well as cause the init() method
			// to be called automatically
		}
	}
}
/*
That's it! All players will now start with a pool of mana, so we just have to do
something with it.

Step 3.1: Using our new ExtendedPlayer Properties in an Item
For the sake of demonstration, we'll make a very basic item called... wait for it...
'ItemUseMana'. I should be naming mountains with this kind of stuff.

Anyways, here's our new ItemUseMana class. Since you probably know all about items
already, I will only discuss things related to IExtendedEntityProperties.
*/
public class ItemUseMana extends Item
{
	public ItemUseMana(int par1) {
		super(par1);
	}
	
	@Override
	public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer player)
    {
		if (!world.isRemote)
		{
			/*
			Due to the length of code needed to get extended entity properties, I always find it
			handy to create a local variable named 'props' for whatever properties I need.
			
			Also, getExtendedProperties("name") returns the type 'IExtendedEntityProperties', so
			you need to cast it as your extended properties type for it to work.
			 */
			ExtendedPlayer props = (ExtendedPlayer) player.getExtendedProperties(ExtendedPlayer.EXT_PROP_NAME);
			
			// Here we'll use the method we made to see if the player has enough mana to do something
			// We'll print something to the console for debugging, but I'm sure you'll find a much
			// better action to perform.
			if (props.consumeMana(15))
			{
				System.out.println("[MANA ITEM] Player had enough mana. Do something awesome!");
			}
			else
			{
				System.out.println("[MANA ITEM] Player ran out of mana. Sad face.");
				props.replenishMana();
			}
		}
		
        return itemstack;
    }
}
/*
Try it out and check the console. Hooray! It should have worked. If not, double-check
that you have registered your EventHandler in your main mod load method.

For the sake of convenience, here is the code:
*/
@EventHandler
public void load(FMLInitializationEvent event)
{
	MinecraftForge.EVENT_BUS.register(new TutEventHandler());
}
/*
Step 3.2: Using our new ExtendedPlayer Properties in an Event
Events are another really great place to use additional properties.
Since we only added mana, we will pretend that it is in fact a spell
that prevents damage from falling up to a certain distance.

In order to do this, we'll need to add a getCurrentMana method to
our ExtendedPlayer class. It just returns 'this.currentMana'

Next, we add a LivingFallEvent to our EventHandler:
*/
@ForgeSubscribe
public void onLivingFallEvent(LivingFallEvent event)
{
	// Remember that so far we have only added ExtendedPlayer properties
	// so check if it's the right kind of entity first
	if (event.entity instanceof EntityPlayer)
	{
		ExtendedPlayer props = (ExtendedPlayer) event.entity.getExtendedProperties(ExtendedPlayer.EXT_PROP_NAME);
		
		// This 'if' statement just saves a little processing time and
		// makes it so we only deplete mana from a fall that would injure the player
		if (event.distance > 3.0F && props.getCurrentMana() > 0)
		{
			// Some debugging statements so you can see what's happening
			System.out.println("[EVENT] Fall distance: " + event.distance);
			System.out.println("[EVENT] Current mana: " + props.getCurrentMana());
			
			/*
			We need to make a local variable to store the amount to reduce both
			the distance and mana, otherwise when we reduce one, we have no way
			to tell by how much to reduce the other
			
			Alternatively, you could just try to consumeMana for the amount of the
			fall distance and, if it returns true, set the fall distance to 0,
			but today we're going for a cushioning effect instead.

			If you want mana to be used efficiently, you would only reduce the fall
			distance by enough to reduce it to 3.0F (3 blocks), thus ensuring the
			player will take no damage while minimizing mana consumed.
				
			Be sure you put (event.distance - 3.0F) in parentheses or you'll have a
			nasty bug with your mana! It has to do with the way "x < y ? a : b"
			parses parameters.
			*/
			float reduceby = props.getCurrentMana() < (event.distance - 3.0F) ? props.getCurrentMana() : (event.distance - 3.0F);
			event.distance -= reduceby;
			
			// Cast reduceby to 'int' to match our method parameter
			props.consumeMana((int) reduceby);
			
			System.out.println("[EVENT] Adjusted fall distance: " + event.distance);
		}
	}
}
/*
Step 4: Adding another kind of Extended Properties
Now we're going to add a variable to all EntityLivingBase entities in addition
to the ones we added above for EntityPlayer. We only want players to have mana,
but we want every creature under the sun to have riches for us to plunder!

Well, guess what it will be named? That's right, ExtendedLivingBase! this
class, as it will be almost exactly like the one we just made, but with one difference:
we're going to use the init() method so we can use the Random from World object to
randomize the amount of gold each entitylivingbase has.
*/
public class ExtendedLivingBase implements IExtendedEntityProperties
{
	public final static String EXT_PROP_NAME = "ExtendedLivingBase";
	
	private final EntityLivingBase entity;
	
	private int gold;

	public ExtendedLivingBase(EntityLivingBase entity)
	{
		this.entity = entity;
	}

	@Override
	public void saveNBTData(NBTTagCompound compound)
	{
		compound.setInteger("Gold", this.gold);
	}

	@Override
	public void loadNBTData(NBTTagCompound compound)
	{
		this.gold = compound.getInteger("Gold");
		System.out.println("[LIVING BASE] Gold from NBT: " + this.gold);
	}

	@Override
	public void init(Entity entity, World world)
	{
		// Gives a random amount of gold between 0 and 15
		this.gold = world.rand.nextInt(16);
		System.out.println("[LIVING BASE] Gold: " + this.gold);
	}
}
// Be sure to register it in your EventHandler!
// onEntityConstructing should now look like this:
@ForgeSubscribe
public void onEntityConstructing(EntityConstructing event)
{
	// From last time:
	if (event.entity instanceof EntityPlayer &&
			event.entity.getExtendedProperties(ExtendedPlayer.EXT_PROP_NAME) == null)
	{
		event.entity.registerExtendedProperties(ExtendedPlayer.EXT_PROP_NAME,
				new ExtendedPlayer((EntityPlayer) event.entity));
	}
	/* New stuff:
	Be sure not to use 'else if' here. A player, for example, is both an EntityPlayer
	AND an EntityLivingBase, so should have both extended properties
	*/ 
	if (event.entity instanceof EntityLivingBase)
	{
		/*
		Just like above but we change 'ExtendedPlayer' to 'ExtendedLivingBase'
		and cast event.entity to EntityLivingBase.
		Isn't it nice to use the constant variable EXT_PROP_NAME in all
		of our IExtendedEntityProperty classes? So easy to remember and it
		stores a different name for each class. Nice.
		*/
		event.entity.registerExtendedProperties(ExtendedLivingBase.EXT_PROP_NAME,
				new ExtendedLivingBase((EntityLivingBase) event.entity));
		// Remember, this will also call the init() method automatically
	}
}
/*
Finished! Go ahead and give it a try, see how much gold everyone is getting!

Wait, what the heck?! What's with all these errors, you ask? Well, to be honest,
I'm not really sure. Something to do with the way the init() method is called leads
to the ExtendedProperties being null somehow. No idea.

Good news is, I know how to get around it. Move everything that seems like it should
go in init() to your EventHandler onEntityJoinWorldEvent. Be sure to leave the init
method totally empty.
*/
@ForgeSubscribe
public void onEntityJoinWorld(EntityJoinWorldEvent event)
{
	if (event.entity instanceof EntityLivingBase)
	{
		ExtendedLivingBase props = (ExtendedLivingBase) event.entity.getExtendedProperties(ExtendedLivingBase.EXT_PROP_NAME);
		// Gives a random amount of gold between 0 and 15
		props.addGold(event.entity.worldObj.rand.nextInt(16));
		System.out.println("[LIVING BASE] Gold: " + props.getGold());
	}
}
/*
Be sure to create the 'addGold(int)' and 'getGold()' methods in ExtendedLivingBase.
Now try it. Yay! It works!

In conjunction with a custom ItemGoldCoin and LivingDropsEvent, I'm sure you can see
how this could be used.

FINAL NOTE:
If you use IExtendedEntityProperties to change something that affects the CLIENT-side of things,
such as an entity's step height or player.capabilities such as allowFlying, it's been my experience
that you need to send a Packet to the player letting them know about their new abilities.

Rest assured that all of the things I showed above will work fine without packets, but be warned
that not everything necessarily will. Server/Client communications is not my area of expertise,
or I would gladly try to explain it. As it is, I barely understand it myself and am constantly
battling to figure out what information I need to send where, if I even need to send it at all.

If anyone cares to share their knowledge, please do! Thanks!
*/

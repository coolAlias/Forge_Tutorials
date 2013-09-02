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
*/
/**
 * Step 1: Create a class that implements IExtendedEntityProperties
 */
/*
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
/**
 * Step 2: Register the ExtendedPlayer class in your EventHandler
 */
/*
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
*/
/**
 * Step 3.1: Using our new ExtendedPlayer Properties in an Item
 */
/*
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
/**
 * Step 3.2: Using our new ExtendedPlayer Properties in an Event
 */
/*
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
/**
 * Step 3.3: Using our ExtendedPlayer Properties in a Gui Overlay
 */
/*
Here we will create a mana bar display in the upper-left corner of the screen using
currentMana and maxMana from our ExtendedPlayer class.

The first part of this section is from http://www.minecraftforge.net/wiki/Gui_Overlay
I highly recommend you read that tutorial before continuing on, as it contains lots
of great information related to this topic.
*/
@SideOnly(Side.CLIENT)
public class GuiManaBar extends Gui
{
	private Minecraft mc;
	/* (my added notes:)
	ResourceLocation takes 2 arguments: your mod id and the path to your texture file,
	starting from the folder 'textures/' from '/src/minecraft/assets/yourmodid/'
	
	The texture file must be 256x256 (or multiples thereof)
	
	I have provided a functional (but ugly) mana_bar.png file to use with this tutorial.
	Download it from Forge_Tutorials/textures/gui
	 */
	private static final ResourceLocation texturepath = new ResourceLocation("tutorial", "textures/gui/mana_bar.png");

	public GuiManaBar(Minecraft mc)
	{
		super();
		// We need this to invoke the render engine.
		this.mc = mc;
	}

	//
	// This event is called by GuiIngameForge during each frame by
	// GuiIngameForge.pre() and GuiIngameForce.post().
	//
	@ForgeSubscribe(priority = EventPriority.NORMAL)
	public void onRenderExperienceBar(RenderGameOverlayEvent event)
	{
		// We draw after the ExperienceBar has drawn.  The event raised by GuiIngameForge.pre()
		// will return true from isCancelable.  If you call event.setCanceled(true) in
		// that case, the portion of rendering which this event represents will be canceled.
		// We want to draw *after* the experience bar is drawn, so we make sure isCancelable() returns
		// false and that the eventType represents the ExperienceBar event.
		if (event.isCancelable() || event.type != ElementType.EXPERIENCE)
		{
			return;
		}
		
		/** Start of my tutorial */
		
		// Get our extended player properties and assign it locally so we can easily access it
		ExtendedPlayer props = (ExtendedPlayer) this.mc.thePlayer.getExtendedProperties(ExtendedPlayer.EXT_PROP_NAME);
		
		// If for some reason these properties don't exist (perhaps in multiplayer?)
		// or the player doesn't have mana, return. Note that I added a new method
		// 'getMaxMana()' to ExtendedPlayer for this purpose
		if (props == null || props.getMaxMana() == 0)
		{
			return;
		}

		// Starting position for the mana bar - 2 pixels from the top left corner.
		int xPos = 2;
		int yPos = 2;
		
		// The center of the screen can be gotten like this during this event:
		// int xPos = event.resolution.getScaledWidth() / 2;
		// int yPos = event.resolution.getScaledHeight() / 2;

		// Be sure to offset based on your texture size or your texture will not be truly centered:
		// int xPos = (event.resolution.getScaledWidth() + textureWidth) / 2;
		// int yPos = (event.resolution.getScaledHeight() + textureHeight) / 2;
		
		// setting all color values to 1.0F will render the texture as it appears in your texture file
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		
		// Somewhere in Minecraft vanilla code it says to do this because of a lighting bug
		GL11.glDisable(GL11.GL_LIGHTING);
		
		// This magic formula here binds the texture to the renderer
		this.mc.func_110434_K().func_110577_a(texturepath);
		
		/*
		The parameters for drawTexturedModalRect are as follows:
		
		drawTexturedModalRect(int x, int y, int u, int v, int width, int height);
		
		x and y are the on-screen position at which to render.
		u and v are the coordinates of the most upper-left pixel in your texture file from which to start drawing.
		width and height are how many pixels to render from the start point (u, v)
		 */
		// First draw the background layer. In my texture file, it starts at the upper-
		// left corner (x=0, y=0), is 50 pixels long and 4 pixels thick (y value)
		this.drawTexturedModalRect(xPos, yPos, 0, 0, 50, 4);
		// Then draw the foreground; it's located just below the background in my
		// texture file, so it starts at x=0, y=4, is only 2 pixels thick and 50 length
		// Why y=4 and not y=5? Y starts at 0, so 0,1,2,3 = 4 pixels for the background
		
		// However, we want the length to be based on current mana, so we need a new variable:
		int manabarwidth = (int)(((float) props.getCurrentMana() / props.getMaxMana()) * 50));
		System.out.println("[GUI MANA] Current mana bar width: " + manabarwidth);
		// Now we can draw our mana bar at yPos+1 so it centers in the background:
		this.drawTexturedModalRect(xPos, yPos + 1, 0, 4, manabarwidth, 2);
	}
}
/*
You will need to add this code to your main mod class postInit method in order to register
your new GuiManaBar overlay as an active event (just like registering your EventHandler),
otherwise nothing will appear.
 */
@EventHandler
public void postInit(FMLPostInitializationEvent event)
{
	MinecraftForge.EVENT_BUS.register(new GuiManaBar(Minecraft.getMinecraft()));
}
/*
Alright, try it out. There should be a horizontal mana bar in the upper-left corner of 
your screen. Now use our ItemUseMana once or twice. Notice the mana bar doesn't update.

What's going on?

Well, currently only the server knows how much mana the player has. We never told the client
that the player even has mana, let alone how much! For many purposes, this is fine. However,
since a Gui is only rendered client side, this is a case where we will need to use packets
to synchronize the server/client.

Please take a few minutes to read up on Packet Handling, as I'm not going to cover it in
much detail here:
http://www.minecraftforge.net/wiki/Tutorials/Packet_Handling

Ok, moving on.
Make a packet handler class like in the above tutorial:
*/
public class TutorialPacketHandler implements IPacketHandler
{
	// Don't need to do anything here.
	public TutorialPacketHandler() {}

	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player)
	{
		// This is a good place to parse through channels if you have multiple channels
		if (packet.channel.equals("tutchannel")) {
			handleExtendedProperties(packet, player);
		}
	}
	
	// Making different methods to handle each channel helps keep things tidy:
	private void handleExtendedProperties(Packet250CustomPayload packet, Player player)
	{
		DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(packet.data));
		
		ExtendedPlayer props = ((ExtendedPlayer)((EntityPlayer) player).getExtendedProperties(ExtendedPlayer.EXT_PROP_NAME));

		// Everything we read here should match EXACTLY the order in which we wrote it
		// to the output stream in our ExtendedPlayer sync() method.
		try {
			props.setMaxMana(inputStream.readInt());
			props.setCurrentMana(inputStream.readInt());
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		// Just so you can see in the console that it's working:
		System.out.println("[PACKET] Mana from packet: " + props.getCurrentMana() + "/" + props.getMaxMana());
	}
}
/*
Then modify the following line in your main mod class to the below:
*/
@NetworkMod(clientSideRequired=true, serverSideRequired=false, channels = {"tutchannel"}, packetHandler = TutorialPacketHandler.class)
/*
That's it for setting up the Packet Handler framework, now we'll set up a method to send
packets from within our ExtendedPlayer class. I like to give this method the same name
in all of my IExtendedEntityProperties classes, just to make it easy on myself.
*/
/**
 * Sends a packet to the client containing information stored on the server
 * for ExtendedPlayer
 */
public final void syncExtendedProperties()
{
	ByteArrayOutputStream bos = new ByteArrayOutputStream(8);
	DataOutputStream outputStream = new DataOutputStream(bos);
	
	// We'll write max mana first so when we set current mana client
	// side, it doesn't get set to 0 (see methods below)
	try {
		outputStream.writeInt(this.maxMana);
		outputStream.writeInt(this.currentMana);
	} catch (Exception ex) {
		ex.printStackTrace();
	}

	Packet250CustomPayload packet = new Packet250CustomPayload();
	packet.channel = "tutchannel";
	packet.data = bos.toByteArray();
	packet.length = bos.size();

	Side side = FMLCommonHandler.instance().getEffectiveSide();
	
	// We only want to send from the server to the client
	if (side == Side.SERVER) {
		EntityPlayerMP player1 = (EntityPlayerMP) player;
		PacketDispatcher.sendPacketToPlayer(packet, (Player) player1);
	}
}
/*
Okay, well that will send a packet whenever we call it. Problem is, we don't call it
anywhere yet. You could do it in onLivingUpdate or some such, but that would unnecessarily
spam packets which would be no good. We only want to call it when any of the information
stored in ExtendedPlayer changes, in our case, when current or max mana is modified.

Here you can see my implementations for setCurrentMana and setMaxMana:
 */
/**
 * Sets current mana to amount or maxMana, whichever is lesser
 */
public void setCurrentMana(int amount)
{
	this.currentMana = (amount < this.maxMana ? amount : this.maxMana);
	this.syncExtendedProperties();
}

/**
 * Sets max mana to amount or 0 if amount is less than 0
 */
public void setMaxMana(int amount)
{
	this.maxMana = (amount > 0 ? amount : 0);
	this.syncExtendedProperties();
}
/*
Note that we add a call to syncExtendedProperties() in each of these methods because they
changed our stored variables. We also need to sync the properties in any other methods that
do so, like consumeMana and replenishMana.

Another time we need to sync properties is after the entity is loaded from NBT, as that is
only done server side and we want the information for our GuiManaBar. Because we can't
send and receive packets before everything is loaded, we can't do it from within the
readFromNBT method. Guess where we can do it from? That's right, our EventHandler!

Add this to your EventHandler's onEntityJoinWorldEvent method, as this event occurs
after everything (the world, entities, etc) is loaded but before anything really happens
in the game. As a bonus, it's only called once per entity, so you're not spamming packets.
*/
@ForgeSubscribe
public void onEntityJoinWorld(EntityJoinWorldEvent event)
{
	//Only need to synchronize when the world is remote (i.e. we're on the server side)
	// and only for player entities, as that's what we need for the GuiManaBar
	if (!event.entity.worldObj.isRemote && event.entity instanceof EntityPlayer) {
		((ExtendedPlayer)(event.entity.getExtendedProperties(ExtendedPlayer.EXT_PROP_NAME))).syncExtendedProperties();
	}
}
/*
Anyways, that's a lot of work just to get a little mana bar, but it should all be working
correctly now. Fire it up and try for yourself!
*/
/**
 * Step 4: Adding another kind of Extended Properties
 */
/*
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
/**
 * Step 5: Getting your custom data to persist through player death
 */
/*
First, many thanks and godly praise upon the legend that is Mithion, the creator of IExtendedEntityProperties.
Without him, not only this particular section, but all of this stuff would not be possible. Truly amazing work
by him that allows us to do so much with ease.

Just had to be said. Anyways, as some have noticed, if you die and respawn, the data you so carefully created,
registered, sent in packets and saved to NBT is RESET to the initial values when the player dies. This has to
do with the way player NBT is stored and retrieved during death, and there's nothing we can do about it. At
least, nothing directly.

We need to find a way to store the IExtendedEntityProperties data outside of the player when the player dies,
and retrieve it when the player respawns. I knew this, but I never would have thought of where to store it without
Mithion's assistance. Though I guess it could be stored anywhere that persists...

Enough blabbing. The most convenient way to store extended properties is bundled as NBT, and we'll be storing it
in a HashMap with the player's username as the key. We'll store this map in our CommonProxy class.
*/
public class CommonProxy implements IGuiHandler
{
	/** Used to store IExtendedEntityProperties data temporarily between player death and respawn or dimension change */
	private static HashMap<String, NBTTagCompound> extendedEntityData = new HashMap<String, NBTTagCompound>();
	
	public void registerRenderers() {}
	
	public int addArmor(String string) {
		return 0;
	}

	@Override
	public Object getServerGuiElement(int guiId, EntityPlayer player, World world, int x, int y, int z)
	{
		return null;
	}

	@Override
	public Object getClientGuiElement(int guiId, EntityPlayer player, World world, int x, int y, int z)
	{
		return null;
	}
	
	/**
	 * Adds an entity's custom data to the map for temporary storage
	 * @param compound An NBT Tag Compound that stores the IExtendedEntityProperties data only
	 */
	public void storeEntityData(String name, NBTTagCompound compound)
	{
		extendedEntityData.put(name, compound);
	}
	
	/**
	 * Removes the compound from the map to prevent bloat and multiple occurrences of the same key
	 * Returns the NBT tag stored for name or null if none exists
	 */
	public NBTTagCompound getEntityData(String name)
	{
		NBTTagCompound entityData = extendedEntityData.get(name);
		extendedEntityData.remove(name);
		return entityData;
	}
}
/*
Ok, now we have the framework up that will store our data externally to the player's NBT, which we can access
from our EventHandler. Now all we need to do is store the data when the player dies and retrieve it when the
player re-joins the world. We don't use LivingSpawnEvent because that event is not triggered during the process
of dying and being respawned, as much as you would think otherwise.
*/
// These are methods in the EventHandler class, in case you don't know that by now

// we need to add this new event - it is called for every living entity upon death
@ForgeSubscribe
public void onLivingDeathEvent(LivingDeathEvent event)
{
	// we only want to save data for players (most likely, anyway)
	if (!event.entity.worldObj.isRemote && event.entity instanceof EntityPlayer) {
		// create a new NBT Tag Compound to store the IExtendedEntityProperties data
		NBTTagCompound playerData = new NBTTagCompound();
		// write the data to the new compound
		((ExtendedPlayer)(event.entity.getExtendedProperties(ExtendedPlayer.EXT_PROP_NAME))).saveNBTData(playerData);
		// and store it in our proxy
		proxy.storeEntityData(((EntityPlayer) event.entity).username, playerData);
	}
}

// we already have this event, but we need to modify it some
@ForgeSubscribe
public void onEntityJoinWorld(EntityJoinWorldEvent event)
{
	if (!event.entity.worldObj.isRemote && event.entity instanceof EntityPlayer) {
		// before syncing the properties, we must first check if the player has some saved in the proxy
		// recall that 'getEntityData' also removes it from the map, so be sure to store it locally
		NBTTagCompound playerData = proxy.getEntityData(((EntityPlayer) event.entity).username);
		// make sure the compound isn't null
		if (playerData != null) {
			// then load the data back into the player's IExtendedEntityProperties
			((ExtendedPlayer)(event.entity.getExtendedProperties(ExtendedPlayer.EXT_PROP_NAME))).loadNBTData(playerData);
		}
		// finally, we sync the data between server and client (we did this earlier in 3.3)
		((ExtendedPlayer)(event.entity.getExtendedProperties(ExtendedPlayer.EXT_PROP_NAME))).syncExtendedProperties();
	}
}
/*
And that's how it's done, folks. Happy modding. :D
*/

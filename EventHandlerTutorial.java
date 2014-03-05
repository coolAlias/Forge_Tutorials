/** Implementing and Using Forge's EventHandler */

/*
If you want to change any type of default Minecraft behavior, chances are there is a Forge Event that handles it.

There are Player Events, Living Events, Item Events, World Events, TerrainGenEvents, Minecart Events... there's just
so much you can do with these it's incredible.

I personally prefer using EventHandler over TickHandler for this very reason - most things you could ever want to do
already have a framework built to handle it, whereas Tick Handler you have to build it all yourself.

This tutorial will cover:
1. Building and using an Event Handler
2. Advanced information for handling events
3. A sampling of Event types and their possible uses
*/

/**
 * Step 1: Create TutEventHandler class
 */
/*
IMPORTANT!!! Do NOT name your EventHandler 'EventHandler' - that's already the name of a Forge class.
Also, do NOT edit any of the Forge classes. You need to create a new class to handle events.
*/

public class TutEventHandler
{
}

/**
 * Step 2: Registering your EventHandler
 */
/*
Resgister your event handler class to EVENT_BUS either in 'load' or 'postInit' methods in your main mod. Note that this step is the same in both 1.6.4 and 1.7.2, but see below for more information on the different event buses.
 */

@EventHandler
public void load(FMLInitializationEvent event)
{
	// IMPORTANT: Be sure to register your handler on the correct bus!!! (see below)

	// the majority of events use the MinecraftForge event bus:
	MinecraftForge.EVENT_BUS.register(new TutEventHandler());

	// but some are on the FML bus:
	FMLCommonHandler.instance().bus().register(new YourFMLEventHandler());
}

/*
NOTE: Registering to the correct BUS

You have followed all the steps and your event handling methods just do not seem to be working, what could possibly be going on? Well, each event is posted to a different event bus, and if your event handler is registered to the incorrect bus, then your method will never get called. The vast majority of events are posted to the MinecraftForge.EVENT_BUS, but there are several other event buses:

1. MinecraftForge.EVENT_BUS: Most events get posted to this bus.

2. MinecraftForge.TERRAIN_GEN_BUS: Most world generation events happen here, such as Populate, Decorate, etc., with the strange exception that Pre and Post events are on the regular EVENT_BUS

3. MinecraftForge.ORE_GEN_BUS: Ore generation, obviously

4. FML Events: these become very important in 1.7.2, as this is where TickEvents and KeyInputEvents are posted, with TickHandler and KeyHandler no longer existing.

It is very important to register your event handler to the correct event bus, and only put those events that get posted to a certain event bus in a handler registered to that bus, or your event handling will fail.

You're finished! That was easy  But it doesn't do anything right now, so on to step 3.
*/
/**
 * Step 3: Add events to your event handler (an example)
 */
/*
Look through MinecraftForge event types for ones you want to use and add them to your EventHandler by creating a new method with the appropriate Event as a parameter.

1.6.4: It must be prefaced by "@ForgeSubscribe" so that it gets called automatically at the right times.
1.7.2: It must be prefaced by "@SubscribeEvent" so that it gets called automatically at the right times.

Do not, I repeat do NOT edit the event classes directly. Also, you do NOT need to make a class extending the Event class.
 */

// In your TutEventHandler class - the name of the method doesn't matter
// Only the Event type parameter is what's important (see below for explanations of some types)
@ForgeSubscribe
public void onLivingUpdateEvent(LivingUpdateEvent event)
{
	// This event has an Entity variable, access it like this:
	event.entity;

	// do something to player every update tick:
	if (event.entity instanceof EntityPlayer)
	{
		EntityPlayer player = (EntityPlayer) event.entity;
		ItemStack heldItem = player.getHeldItem();
		if (heldItem != null && heldItem.itemID == Item.arrow.itemID) {
			player.capabilities.allowFlying = true;
		}
		else {
			player.capabilities.allowFlying = player.capabilities.isCreativeMode ? true : false;
		}
	}
}

// If you're ever curious what variables the Event stores, type 'event.'
// in Eclipse and it will bring up a menu of all the methods and variables.
// Or go to the implementation by ctrl-clicking on the class name.

/**
 * Step 4: Using Events in your custom classes
 */
/*
Forge Events are all hooked into automatically from vanilla code, but say
you made a custom Bow and want it to use ArrowNock and ArrowLoose events?
You need to post them to the event bus in your item code.
 */

/** ArrowNockEvent should be placed in 'onItemRightClick' */
@Override
public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer player)
{
	// Create the event and post it
	ArrowNockEvent event = new ArrowNockEvent(player, itemstack);
	MinecraftForge.EVENT_BUS.post(event);

	if (event.isCanceled())
	{
		// you could do other stuff here as well
		return event.result;
	}

	player.setItemInUse(itemstack, this.getMaxItemUseDuration(itemstack));

	return itemstack;
}

/** ArrowLooseEvent should be placed in 'onPlayerStoppedUsing' */
@Override
public void onPlayerStoppedUsing(ItemStack itemstack, World world, EntityPlayer player, int par4)
{
	// Ticks in use is max duration minus par4, which is equal to max duration - 1 for every tick in use
	int ticksInUse = this.getMaxItemUseDuration(itemstack) - par4;

	ArrowLooseEvent event = new ArrowLooseEvent(player, itemstack, ticksInUse);
	MinecraftForge.EVENT_BUS.post(event);

	if (event.isCanceled()) { return; }

	// ticksInUse might be modified by the Event in your EventHandler, so reassign it here:
	ticksInUse = event.charge;

	// Do whatever else you want with the itemstack like fire an arrow or cast a spell
}

/**
 * Step 5: Adding events to your EventHandler
 */
/*
Look through MinecraftForge event types for ones you want to use and add them to your EventHandler by creating a new method with the appropriate Event as a parameter.

1.6.4: It must be prefaced by "@ForgeSubscribe" so that it gets called automatically at the right times.
1.7.2: It must be prefaced by "@SubscribeEvent" so that it gets called automatically at the right times.

Do not, I repeat do NOT edit the event classes directly. Also, you do NOT need to make a class extending the Event class.

The following is a template for whatever event method you want to make. Name it whatever you want, but use the correct Event Type from above.

Event variables are accessed by using 'event.variableName' I give the variable names for many Events below.
 */

@ForgeSubscribe
public void methodName(EventType event)
{
	// do whatever you want here
}

/*
 * Advanced Information: Setting Priority
 */
/*
Note that event priority works exactly the same in 1.7.2, other than the primary annotation changing to @SubscribeEvent.

Priority is the order in which all listeners listening to a posted event are called. A listener is a method with the
@ForgeSubscribe annotation, and is said to be actively listening if the class containing the method was registered to
the MinecraftForge EVENT_BUS. Whenever an event is posted matching the parameters of the listener, the listener's
method is called.

When there are multiple listeners listening to a single event, the order in which they are called is important if one
listener's functionality relies on having first or last access to the event in process, or if it relies on information
set by a prior listener. This can be especially useful with Cancelable events.

A single Event can be handled multiple times by different handlers or even within the same handler provided the methods
have different names:
*/
@ForgeSubscribe
public void onLivingHurt(LivingHurtEvent event) {}

@ForgeSubscribe
public void onPlayerHurt(LivingHurtEvent event) {}
/*
Both methods will be called each time a LivingHurtEvent is posted to the EVENT_BUS in the order they are added to
the event listener (see IEventListener), which in the case above is simply their order in the code. The order can
be controlled by appending (priority=VALUE) to the @ForgeSubscribe annotation, where VALUE is defined in the
EventPriority enum class. HIGHEST priority is always called first, while LOWEST priority is called last.
*/
// this method will now be called after 'onPlayerHurt()'
@ForgeSubscribe(priority=LOWEST)
public void onLivingHurt(LivingHurtEvent event) {}

@ForgeSubscribe(priority=HIGHEST)
public void onPlayerHurt(LivingHurtEvent event) {}
/*
If two listeners have the same priority level, then the order is again controlled by the order in which they are added.
In order to control the flow for such a case within a mod, the methods can be placed in separate 'event handler'
classes to be registered in the order desired:
*/
// In the case of identical priority levels, PlayerHurtHandler will process first
MinecraftForge.EVENT_BUS.register(new PlayerHurtHandler());
MinecraftForge.EVENT_BUS.register(new LivingHurtHandler());
// For multiple Mods that affect the same events, the order of mod registration would have the same effect.
/*
 * Advanced Information: Cancelable Events
 */
/*
Events with the @Cancelable annotation have the special quality of being cancelable. Once an event is canceled,
subsequent listeners will not process the event unless provided with special annotation:
*/
@ForgeSubscribe // default priority, so it will be called first
public void onLivingHurt(LivingHurtEvent event) {
event.setCanceled(true);
}

@ForgeSubscribe(priority=LOWEST, receiveCanceled=true)
public void onPlayerHurt(LivingHurtEvent event) {
// un-cancel the event
event.setCanceled(false);
}
/*
By controlling the order in which each listener method is called, it is usually possible to avoid un-canceling a
previously canceled event, although exceptional circumstances do arise; in those cases, extra care must be taken
to avoid making logical errors.

More will be added as I learn. Thanks to GotoLink for his excellent explanations regarding priority.
*/

/*
 Now for some examples of Event types, their variables, when they are called and what you might do with them, but
 first a word of warning: many of these events ONLY get called on one side or the other, so if something is not
 working as expected, check which side(s) the event is being called on and it may surprise you.

 An easy way to check is to put a line of debugging code at the beginning of each event method, so long as that event
 has access to some kind of entity:
*/
@ForgeSubscribe
public void someEventMethod(SomeEvent event) {
	System.out.println("Some event called; is this the client side? " + event.entity.worldObj.isRemote);
}
/*
Now on to the events! But first, a word of warning: many of these events ONLY get called on one side or the other, so if something is not working as expected, check which side(s) the event is being called on and it may surprise you.

An easy way to check is to put a line of debugging code at the beginning of each event method, so long as that event has access to some kind of entity:
*/
@ForgeSubscribe
public void someEventMethod(SomeEvent event) {
	System.out.println("Some event called; is this the client side? " + event.entity.worldObj.isRemote);
}
/*

IMPORTANT: The following events are from 1.6.4; while many have not changed, some most certainly have.
Always check the net.minecraftforge.event package for the available events, no matter what version
of Minecraft you are modding for.

1. ArrowNockEvent
Variables: EntityPlayer player, ItemStack result
Usually called from 'onItemRightClick'.
Uses: It is cancelable, so if some conditions are not met (e.g. no arrows in inventory) you could cancel it and stop
the player from setting the item in use. One thing I use it for is to set a boolean 'isAiming', which I can then
interrupt if the player takes damage or some such (using another boolean 'wasInterrupted')

2. ArrowLooseEvent
Variables: EntityPlayer player, ItemStack bow, int charge
Usually called from 'onPlayerStoppedUsing'. It is also cancelable.
Uses: I use it in tandem with the above to check if the player was interrupted, and if so, cancel the event.

3. EntityConstructing
Variables: Entity entity
Called for every Entity when its constructor is called.
Uses: Useful if you need to add ExtendedEntityProperties.

4. EntityJoinWorldEvent
Variables: Entity entity, World world
Called when an entity joins the world for the first time.
Uses: Useful for synchronizing ExtendedEntityProperties, giving your player an item when spawned or any other number
of things.

5. LivingUpdateEvent
Variables: EntityLivingBase entity
Called every tick at the beginning of the entity's onUpdate method.
Uses: This is probably the most useful Event. You can allow player's to fly if holding an item or wearing your armor
set, you can modify a player's fall speed here, add potion effects or anything else you can imagine. It's really
really handy.

6. LivingDropsEvent
Variables: EntityLivingBase entity, DamageSource source, ArrayList<EntityItem> drops, int lootingLevel, boolean
recentlyHit, int specialDropValue
Called when an entity is killed and drops items.
Uses: Handy if you want to modify a vanilla mobs drops or only drop your custom item if it was killed from your custom
DamageSource. You can also remove items from drops, adjust it based on the looting enchantment level of the item used
to kill it, etc. Pretty useful.

7. LivingFallEvent
Variables: EntityLivingBase entity, float distance
Called when the entity hits the ground after a fall, but before damage is calculated.
SPECIAL NOTE: This event is NOT called while in Creative Mode; PlayerFlyableFallEvent is called instead
Uses: It is cancelable, so 'event.setCanceled(true)' will preclude further processing of the fall.
You can also modify the distance fallen here, but keep in mind this is ONLY on impact. If you want
to modify fall distance only while certain conditions are met, better to do it in LivingUpdateEvent.
Also, be sure you are modifying 'event.distance' and NOT 'entity.fallDistance' or you won't change the outcome of the
fall.

8. LivingJumpEvent
Variables: EntityLivingBase entity
Called whenever entity jumps.
Uses: Useful for entity.motionY += 10.0D. Just give it a try 

9. LivingAttackEvent
Variables: EntityLivingBase entity, DamageSource source, float ammount
Called when an entity is attacked, but before any damage is applied
Uses: Cancelable. Here you can do pre-processing of an attack before LivingHurtEvent is called. The source entity of
the attack is stored in DamageSource, and you can adjust the damage to be dealt however you see fit. Basically the
same uses as LivingHurtEvent, but done sooner.

10. LivingHurtEvent
Variables: EntityLivingBase entity, DamageSource source, float ammount
Called when an entity is damaged, but before any damage is applied
Uses: Another super useful one if you have custom armor that reduces fire damage, increases damage taken from magic,
do something if ammount is greater than current health or whatever.

11. LivingDeathEvent
Variables: EntityLivingBase entity, DamageSource source
Called when an entity dies; cancelable!
Uses: Recall that DamageSource has lots of variables, too, such as getEntity() that returns the entity that caused the
damage and thus that killed the current entity. All sorts of things you could do with that. This is also the place to
cancel death and resurrect yourself, or set a timer for resurrection. If you have data you want to persist through
player death, such as IExtendedEntityProperties, you can save that here as well.

12. EntityInteractEvent
Variables: EntityPlayer player, Entity target
Called when the player right-clicks on an entity, such as a cow
Uses: Gee, you could do anything with this. One use could be getting milk into your custom bucket...

13. EntityItemPickupEvent
Variables: EntityPlayer player, EntityItem item
Called when the player picks up an item
Uses: This one is useful for special items that need handling on pickup; an example would be if you made something
similar to experience orbs, mana orbs for instance, that replenish mana rather than adding an item to inventory

14. HarvestCheck
Variables: EntityPlayer player, Block block, boolean success
Called when the player breaks a block before the block releases its drops
Uses: Coupled with the BreakSpeed event, this is perhaps the best way to change the behavior of mining.
*/
/**
 * 1.7.2 TickEvents and Creating a TickHandler
 */
/*
STOP!!! Chances are, you do NOT need to create a tick handler for whatever it is you are doing. There are many methods built-in to Minecraft that already act as tick handlers, and it is ALWAYS better to use them when you can. Why? Because they tick only when the object in question actually exists, whereas a generic tick handler processes every tick no matter what. Here are some of the pre-made tickers at your disposal:

Entity#onUpdate: called every tick for each Entity; to manipulate vanilla entities, use LivingUpdateEvent
TileEntity#onUpdate: called for tile entities every tick unless you tell it not to tick
Item#onUpdate: called every tick while the specific item is in a player's inventory
Item#onArmorTick: called only for armor each tick that it is equipped
Block#updateTick: may be called randomly based on the block's tick rate, or it may be scheduled

As you can see, nearly everything you could ever want to tick already has that capability. If what you want to do simply cannot be handled using one of the built-in tick methods, then and ONLY THEN should you consider creating a tick handler. Here's how to do so.

In 1.7.2, as I'm sure many of you have noticed, the TickHandler class is gone, replaced by what appears to be a single TickEvent that we must now subscribe to in order to achieve the same functionality. Below, I will break it down and explain what exactly is going on and how to use it effectively.
*/
/**
 * Step 1: Determine Which TickEvent to Use
 */
/*
There are actually five different subclasses of TickEvent, each called on a specific in-game tick and on a specific side or sides. It is very important to understand the difference between these events and to use the appropriate one:

ServerTickEvent: called on the server side only
ClientTickEvent: called on the client side only
WorldTickEvent: both sides
PlayerTickEvent: both sides
RenderTickEvent: client side only and called each render tick

When creating a TickHandler, be sure NOT to subscribe to the generic "TickEvent", as that will listen to every single tick type on every single tick, which is just wasting processing time.

Once you decide which tick you need, then it is time to create a new TickHandler class.
*/
/**
 * Step 2: Create a TickHandler
 */
/*
For now, we are just going to use the old name TickHandler, though we are really creating an event handler to listen to a specific tick. As an example, I will create a RenderTickEvent handler, since that is what I used in Zelda Sword Skills to make the spin attack motion smooth.
*/
// RenderTick is client side only, so we can place the SideOnly annotation here if we want:
@SideOnly(Side.CLIENT)
public class RenderTickHandler {
	// we only need one method here, and you can name it whatever you want, but
	// I like to name it according to the tick to which I am listening:
	@SubscribeEvent
	public void onRenderTick(RenderTickEvent event) {
		// now you can do whatever you want during each render tick, such as rotate the player's view
	}
}
/*
If you need to do different things at the beginning of the tick than at the end of the tick, you can check the tick event's phase in your onWhateverTick method; this is applicable for all TickEvents.
*/
if (event.phase == Phase.START) {
	// this is the equivalent of tickStart
}
if (event.phase == Phase.END) {
	// and this is the equivalent of tickEnd
}

// if you have a lot of stuff going on in each of those, you could separate them into separate methods:
@SubscribeEvent
public void onRenderTick(RenderTickEvent event) {
	if (event.phase == Phase.START) {
		onTickStart();
	} else {
		onTickEnd();
	}
}
/*
One other thing to mention here is that since we are on the client side, we might be using Minecraft.getMinecraft() quite a lot to access things like the world, player, etc. Since it would be very wasteful to have to do this each and every tick, a better way is to store the instance of Minecraft when you construct your tick handler:
*/
@SideOnly(Side.CLIENT)
public class RenderTickHandler {
	/** Stores an instance of Minecraft for easy access */
	private Minecraft mc;

	// create a constructor that takes a Minecraft argument; now we have it whenever we need it
	public RenderTickHandler(Minecraft mc) {
		this.mc = mc;
	}

	@SubscribeEvent
	public void onRenderTick(RenderTickEvent event) {
		if (event.phase == Phase.START) {
			// we will make the player spin constantly in circles:
			mc.thePlayer.rotationYaw += 10.0F;
		}
	}
}

/**
 * Step 3: Registering Your TickHandler
 */
/*
All TickEvents need to be registered to the FMLCommonHandler event bus, NOT the MinecraftForge EVENT_BUS. For our client-sided render tick handler, we will do this in the ClientProxy, because we will crash the game if we try to register it in a method that is run on the server.
*/
public class CommonProxy {
/**
 * We will call this method from our main mod class' FMLPreInitializationEvent method
 */
public void initialize() {
	// since we are not registering a tick handler that ticks on the server, we will not put anything here for now
	// but if you had a WorldTickEvent or PlayerTickEvent, for example, this is where you should register it
	// if you try to register the RenderTickHandler here, your game WILL crash
	}
}

public class ClientProxy extends CommonProxy {
	// Our ClientProxy method only gets run on the client side, so it is safe to register our RenderTickHandler here
	@Override
	public void initialize() {
	// calling super will register any 2-sided tick handlers you have that are registered in the CommonProxy
	// this is important since the CommonProxy will only register it on the server side, and you will need it
	// registered on the client as well; however, we do not have any at this point
	super.initialize();

	// here we register our RenderTickHandler - be sure to pass in the instance of Minecraft!
	FMLCommonHandler.instance().bus().register(new RenderTickHandler(Minecraft.getMinecraft()));

	// this is also an ideal place to register things like KeyBindings
	}
}
/*
That should be all you need to use TickEvents successfully and efficiently. Remember, NEVER subscribe to TickEvent, ONLY subscribe to the specific type of tick event that you really need, just as you would never subscribe to Event...

Good luck with 1.7.2!
*/
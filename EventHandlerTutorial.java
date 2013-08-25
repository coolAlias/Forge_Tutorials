/** Implementing and Using Forge's EventHandler */

/*
There still seems to be lots of questions about how to make and use an EventHandler,
so here's another tutorial.

I personally prefer using EventHandler over TickHandler because it gives you much
finer specificity as well as more parameters to work with.

Here's how to make one:
*/

/**
 * Step 1: Create EventHandler class
 */

public class EventHandler
{
}

/**
 * Step 2: Register your event handler class to EVENT_BUS either in
 * 'load' or 'postInit' methods in your main mod
 */

@EventHandler
public void load(FMLInitializationEvent event)
{
	MinecraftForge.EVENT_BUS.register(new EventHandler());
}

// You're finished! That was easy  But it doesn't do anything right now,
// so on to step 3.

/**
 * Step 3: Look through MinecraftForge event types for ones you want to use
 * and add them to your EventHandler
 */

// In your EventHandler class - the name of the method doesn't matter
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
 * Forge Events are all hooked into automatically from vanilla code, but say
 * you made a custom Bow and want it to use ArrowNock and ArrowLoose events?
 * You need to post them to the event bus in your item code.
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
/* A template for whatever event method you want to make. Name it whatever you want,
   but use the correct Event Type from above. Event variables are accessed by using 
   'event.variableName' I give the variable names for many Events below.
 */

@ForgeSubscribe
public void methodName(EventType event)
{
	// do whatever you want here
}

/*
 Now for some examples of Event types, their variables, when they are called and
 what you might do with them:

1. ArrowNockEvent
Variables: EntityPlayer player, ItemStack result
Usually called from 'onItemRightClick'.
Uses: It is cancelable, so if some conditions are not met (e.g. no arrows in inventory) you could cancel it and stop the player from setting the item in use. One thing I use it for is to set a boolean 'isAiming', which I can then interrupt if the player takes damage or some such (using another boolean 'wasInterrupted')

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
Uses: Useful for synchronizing ExtendedEntityProperties, giving your player an item when spawned or any other number of things.

5. LivingUpdateEvent
Variables: EntityLivingBase entity
Called every tick at the beginning of the entity's onUpdate method.
Uses: This is probably the most useful Event. You can allow player's to fly if holding an item or wearing your armor set, you can modify a player's fall speed here, add potion effects or anything else you can imagine. It's really really handy.

6. LivingDropsEvent
Variables: EntityLivingBase entity, DamageSource source, ArrayList<EntityItem> drops, int lootingLevel, boolean recentlyHit, int specialDropValue
Called when an entity is killed and drops items.
Uses: Handy if you want to modify a vanilla mobs drops or only drop your custom item if it was killed from your custom DamageSource. You can also remove items from drops, adjust it based on the looting enchantment level of the item used to kill it, etc. Pretty useful.

7. LivingFallEvent
Variables: EntityLivingBase entity, float distance
Called when the entity hits the ground after a fall, but before damage is calculated.
SPECIAL NOTE: This event is NOT called while in Creative Mode; PlayerFlyableFallEvent is called instead
Uses: It is cancelable, so 'event.setCanceled(true)' will preclude further processing of the fall.
You can also modify the distance fallen here, but keep in mind this is ONLY on impact. If you want
to modify fall distance only while certain conditions are met, better to do it in LivingUpdateEvent.
Also, be sure you are modifying 'event.distance' and NOT 'entity.fallDistance' or you won't change the outcome of the fall

8. LivingJumpEvent
Variables: EntityLivingBase entity
Called whenever entity jumps.
Uses: Useful for entity.motionY += 10.0D. Just give it a try 

9. LivingHurtEvent
Variables: EntityLivingBase entity, DamageSource source, float ammount
Called when an entity is damaged, but before any damage is applied
Uses: Another super useful one if you have custom armor that reduces fire damage, increases damage taken from magic, resurrects the entity if ammount is greater than current health or whatever.
*/

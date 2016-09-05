/*
IMPORTANT: The following tutorial was created for Minecraft 1.6.4 - notes on updating to newer versions can be found at the bottom of the page, but you should follow the main tutorial first.

Have you ever wished you had a few extra inventory slots, say for a shield or amulet? You've come to the right place.

In this tutorial I will show you how to add custom slots to the player's inventory and effectively override the default 
inventory screen.

A word of warning: there are lots of different aspects involved in this process, so if you've just started modding, I 
HIGHLY suggest you come back later after becoming comfortable with ALL of the things listed as prerequisites. You will 
thank yourself for it.

Prerequisites:

1 - You should already have your Main Mod, CommonProxy and ClientProxy set up. If you don't know about that, check 

out TechGuy543's excellent tutorial: http://www.minecraftforum.net/topic/960286-techguys-modding-tutorials/

Alternatively, check the beginning of my "InventoryItemTutorial" for a basic setup.

2 - Know how to use IExtendedEntityProperties; best if you already have one set up. If not, see my tutorial: 

http://www.minecraftforum.net/topic/1952901-eventhandler-and-iextendedentityproperties/#entry24051513

3 - Know how to use KeyBindings; best if you already have one set up:

http://www.minecraftforum.net/topic/1798625-162sobiohazardouss-forge-keybinding-tutorial/

4 - Know how to use Packets; best if you've already set up a PacketHandler: 

http://www.minecraftforge.net/wiki/Packet_Handling

5 - Familiarity with IInventory and Containers is helpful, but not essential.

As always, I try to provide as much information as I can so that if you haven't met all the prerequisites you should still be 
able to follow along; however, if you are having trouble getting it to work, please make sure you understand and have 
followed the tutorials above before asking for help.

NOTE: Throughout this tutorial, I am using ItemUseMana as the custom Item - this is an Item I made in my 
IExtendedEntityProperties tutorial, so if you want to follow along exactly, go grab the code for it; otherwise, just 
substitute your custom Item(s) for it wherever it appears.

Let's get started.
*/
/**
 * Step 1: Create a custom IInventory class
 */
/*
This is the class that will handle all the custom ItemStacks saved in your custom slots. If you've ever worked with 
IInventory before, you probably won't see anything new here. It's a very basic setup.
*/
public class InventoryCustomPlayer implements IInventory
{
	/** The name your custom inventory will display in the GUI, possibly just "Inventory" */
	private final String name = "Custom Inventory";

	/** The key used to store and retrieve the inventory from NBT */
	private final String tagName = "CustomInvTag";

	/** Define the inventory size here for easy reference */
	// This is also the place to define which slot is which if you have different types,
	// for example SLOT_SHIELD = 0, SLOT_AMULET = 1;
	public static final int INV_SIZE = 2;

	/** Inventory's size must be same as number of slots you add to the Container class */
	private ItemStack[] inventory = new ItemStack[INV_SIZE];

	public InventoryCustomPlayer()
	{
		// don't need anything here!
	}

	@Override
	public int getSizeInventory()
	{
		return inventory.length;
	}

	@Override
	public ItemStack getStackInSlot(int slot)
	{
		return inventory[slot];
	}

	@Override
	public ItemStack decrStackSize(int slot, int amount)
	{
		ItemStack stack = getStackInSlot(slot);
		if (stack != null)
		{
			if (stack.stackSize > amount)
			{
				stack = stack.splitStack(amount);
				this.onInventoryChanged();
			}
			else
			{
				setInventorySlotContents(slot, null);
			}
		}
		return stack;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot)
	{
		ItemStack stack = getStackInSlot(slot);
		setInventorySlotContents(slot, null);
		return stack;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack itemstack)
	{
		this.inventory[slot] = itemstack;

		if (itemstack != null && itemstack.stackSize > this.getInventoryStackLimit())
		{
			itemstack.stackSize = this.getInventoryStackLimit();
		}

		this.onInventoryChanged();
	}

	@Override
	public String getInvName()
	{
		return name;
	}

	@Override
	public boolean isInvNameLocalized()
	{
		return name.length() > 0;
	}

	/**
	 * Our custom slots are similar to armor - only one item per slot
	 */
	@Override
	public int getInventoryStackLimit()
	{
		return 1;
	}

	@Override
	public void onInventoryChanged()
	{
		for (int i = 0; i < getSizeInventory(); ++i)
		{
			if (getStackInSlot(i) != null && getStackInSlot(i).stackSize == 0) {
				inventory[i] = null;
			}
		}
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer)
	{
		return true;
	}

	@Override
	public void openChest() {}

	@Override
	public void closeChest() {}

	/**
	 * This method doesn't seem to do what it claims to do, as
	 * items can still be left-clicked and placed in the inventory
	 * even when this returns false
	 */
	@Override
	public boolean isItemValidForSlot(int slot, ItemStack itemstack)
	{
		// If you have different kinds of slots, then check them here:
		// if (slot == SLOT_SHIELD && itemstack.getItem() instanceof ItemShield) return true;

		// For now, only ItemUseMana items can be stored in these slots
		return itemstack.getItem() instanceof ItemUseMana;
	}
	
	public void writeToNBT(NBTTagCompound compound)
	{
		NBTTagList items = new NBTTagList();

		for (int i = 0; i < getSizeInventory(); ++i)
		{
			if (getStackInSlot(i) != null)
			{
				NBTTagCompound item = new NBTTagCompound();
				item.setByte("Slot", (byte) i);
				getStackInSlot(i).writeToNBT(item);
				items.appendTag(item);
			}
		}
		
		// We're storing our items in a custom tag list using our 'tagName' from above
		// to prevent potential conflicts
		compound.setTag(tagName, items);
	}

	public void readFromNBT(NBTTagCompound compound)
	{
		NBTTagList items = compound.getTagList(tagName);

		for (int i = 0; i < items.tagCount(); ++i)
		{
			NBTTagCompound item = (NBTTagCompound) items.tagAt(i);
			byte slot = item.getByte("Slot");

			if (slot >= 0 && slot < getSizeInventory()) {
				inventory[slot] = ItemStack.loadItemStackFromNBT(item);
			}
		}
	}
}
/**
 * Step 2.1: Create a Container class for your custom IInventory
 */
/*
Again, if you've ever worked with Containers before, this will be very familiar. All we're doing here is adding our 
custom slots to the container along with all the slots you'd see in the normal player inventory screen.

One thing to note is you will need to create a custom Slot class for each type of Item you want to add, so if you are adding 
a Shield and an Amulet slot, you'll want SlotShield and SlotAmulet classes to match.

Another thing worthy of note is you need to create a public version of the SlotArmor class in order add armor slots, as the 
vanilla class is not public by default. To avoid editing base classes, I simply copied the code into a new class stored in 
the same package as my other inventory classes.
*/
public class ContainerCustomPlayer extends Container
{
	/** Avoid magic numbers! This will greatly reduce the chance of you making errors in 'transferStackInSlot' method */
	private static final int ARMOR_START = InventoryCustomPlayer.INV_SIZE, ARMOR_END = ARMOR_START+3,
			INV_START = ARMOR_END+1, INV_END = INV_START+26, HOTBAR_START = INV_END+1,
			HOTBAR_END = HOTBAR_START+8;

	public ContainerCustomPlayer(EntityPlayer player, InventoryPlayer inventoryPlayer, InventoryCustomPlayer inventoryCustom)
	{
		int i;

		// Add CUSTOM slots - we'll just add two for now, both of the same type.
		// Make a new Slot class for each different item type you want to add
		this.addSlotToContainer(new SlotCustom(inventoryCustom, 0, 80, 8));
		this.addSlotToContainer(new SlotCustom(inventoryCustom, 1, 80, 26));

		// Add ARMOR slots; note you need to make a public version of SlotArmor
		// just copy and paste the vanilla code into a new class and change what you need
		for (i = 0; i < 4; ++i)
		{
			this.addSlotToContainer(new SlotArmor(player, inventoryPlayer, inventoryPlayer.getSizeInventory() - 1 - i, 8, 8 + i * 18, 

					i));
		}

		// Add vanilla PLAYER INVENTORY - just copied/pasted from vanilla classes
		for (i = 0; i < 3; ++i)
		{
			for (int j = 0; j < 9; ++j)
			{
				this.addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
			}
		}

		// Add ACTION BAR - just copied/pasted from vanilla classes
		for (i = 0; i < 9; ++i)
		{
			this.addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 142));
		}
	}

	/**
	 * This should always return true, since custom inventory can be accessed from anywhere
	 */
	@Override
	public boolean canInteractWith(EntityPlayer player)
	{
		return true;
	}

	/**
	 * Called when a player shift-clicks on a slot. You must override this or you will crash when someone does that.
	 * Basically the same as every other container I make, since I define the same constant indices for all of them 
	 */
	public ItemStack transferStackInSlot(EntityPlayer player, int par2)
	{
		ItemStack itemstack = null;
		Slot slot = (Slot) this.inventorySlots.get(par2);

		if (slot != null && slot.getHasStack())
		{
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			// Either armor slot or custom item slot was clicked
			if (par2 < INV_START)
			{
				// try to place in player inventory / action bar
				if (!this.mergeItemStack(itemstack1, INV_START, HOTBAR_END + 1, true))
				{
					return null;
				}

				slot.onSlotChange(itemstack1, itemstack);
			}
			// Item is in inventory / hotbar, try to place either in custom or armor slots
			else
			{
				// if item is our custom item
				if (itemstack1.getItem() instanceof ItemUseMana)
				{
					if (!this.mergeItemStack(itemstack1, 0, InventoryCustomPlayer.INV_SIZE, false))
					{
						return null;
					}
				}
				// if item is armor
				else if (itemstack1.getItem() instanceof ItemArmor)
				{
					int type = ((ItemArmor) itemstack1.getItem()).armorType;
					if (!this.mergeItemStack(itemstack1, ARMOR_START + type, ARMOR_START + type + 1, false))
					{
						return null;
					}
				}
				// item in player's inventory, but not in action bar
				else if (par2 >= INV_START && par2 < HOTBAR_START)
				{
					// place in action bar
					if (!this.mergeItemStack(itemstack1, HOTBAR_START, HOTBAR_START + 1, false))
					{
						return null;
					}
				}
				// item in action bar - place in player inventory
				else if (par2 >= HOTBAR_START && par2 < HOTBAR_END + 1)
				{
					if (!this.mergeItemStack(itemstack1, INV_START, INV_END + 1, false))
					{
						return null;
					}
				}
			}

			if (itemstack1.stackSize == 0)
			{
				slot.putStack((ItemStack) null);
			}
			else
			{
				slot.onSlotChanged();
			}

			if (itemstack1.stackSize == itemstack.stackSize)
			{
				return null;
			}

			slot.onPickupFromSlot(player, itemstack1);
		}

		return itemstack;
	}
}
/**
 * Step 2.2: Create the custom Slot class(es)
 */
/*
Here, I will only show a single custom Slot for ItemUseMana and the SlotArmor class. They are both very simple.
*/
// Custom Slot:
public class SlotCustom extends Slot
{
	public SlotCustom(IInventory inventory, int slotIndex, int x, int y)
	{
		super(inventory, slotIndex, x, y);
	}

	/**
	 * Check if the stack is a valid item for this slot. Always true beside for the armor slots
	 * (and now also not always true for our custom inventory slots)
	 */
	@Override
	public boolean isItemValid(ItemStack stack)
	{
		// We only want our custom item to be storable in this slot
		return stack.getItem() instanceof ItemUseMana;
	}
}
// Armor Slot:
public class SlotArmor extends Slot
{
	/** The armor type that can be placed on that slot, it uses the same values of armorType field on ItemArmor. */
	final int armorType;

	/** The parent class of this slot, ContainerPlayer, SlotArmor is a Anon inner class. */
	final EntityPlayer player;

	public SlotArmor(EntityPlayer player, IInventory inventory, int slotIndex, int x, int y, int armorType)
	{
		super(inventory, slotIndex, x, y);
		this.player = player;
		this.armorType = armorType;
	}

	/**
	 * Returns the maximum stack size for a given slot (usually the same as getInventoryStackLimit(), but 1 in the case
	 * of armor slots)
	 */
	public int getSlotStackLimit()
	{
		return 1;
	}

	/**
	 * Check if the stack is a valid item for this slot. Always true beside for the armor slots.
	 */
	public boolean isItemValid(ItemStack itemstack)
	{
		Item item = (itemstack == null ? null : itemstack.getItem());
		return item != null && item.isValidArmor(itemstack, armorType, player);
	}

	/**
	 * Returns the icon index on items.png that is used as background image of the slot.
	 */
	@SideOnly(Side.CLIENT)
	public Icon getBackgroundIconIndex()
	{
		return ItemArmor.func_94602_b(this.armorType);
	}
}

/**
 * Step 3: Create the Gui class
 */
/*
This one is almost 100% copy paste from any vanilla gui. You only need to update the constructor parameters to match 
your custom classes and the resource location with your gui texture path. You may also want to change where the Strings 
are drawn on the screen.
*/
public class GuiCustomPlayerInventory extends GuiContainer
{
	/** x size of the inventory window in pixels. Defined as float, passed as int */
	private float xSize_lo;

	/** y size of the inventory window in pixels. Defined as float, passed as int. */
	private float ySize_lo;

	/** Normally I use '(ModInfo.MOD_ID, "textures/...")', but it can be done this way as well */
	private static final ResourceLocation iconLocation = new ResourceLocation("tutorial:textures/gui/custom_inventory.png");

	/** Could use IInventory type to be more generic, but this way will save an import... */
	private final InventoryCustomPlayer inventory;

	public GuiCustomPlayerInventory(EntityPlayer player, InventoryPlayer inventoryPlayer, InventoryCustomPlayer 

			inventoryCustom)
	{
		super(new ContainerCustomPlayer(player, inventoryPlayer, inventoryCustom));
		this.inventory = inventoryCustom;
		// if you need the player for something later on, store it in a local variable here as well
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void drawScreen(int mouseX, int mouseY, float f)
	{
		super.drawScreen(mouseX, mouseY, f);
		xSize_lo = mouseX;
		ySize_lo = mouseY;
	}

	/**
	 * Draw the foreground layer for the GuiContainer (everything in front of the items)
	 */
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		// This method will simply draw inventory names on the screen - you could do without it entirely
		// if that's not important to you, since we are overriding the default inventory rather than
		// creating a specific type of inventory

		String s = this.inventory.isInvNameLocalized() ? this.inventory.getInvName() : I18n.getString(this.inventory.getInvName());
		// with the name "Custom Inventory", the 'Cu' will be drawn in the first slot
		this.fontRenderer.drawString(s, this.xSize - this.fontRenderer.getStringWidth(s), 12, 4210752);
		// this just adds "Inventory" above the player's inventory below
		this.fontRenderer.drawString(I18n.getString("container.inventory"), 80, this.ySize - 96, 4210752);
	}

	/**
	 * Draw the background layer for the GuiContainer (everything behind the items)
	 */
	protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(iconLocation);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		drawPlayerModel(guiLeft + 51, guiTop + 75, 30, guiLeft + 51 - xSize_lo, guiTop + 25 - ySize_lo, mc.thePlayer);
	}

	/**
	 * Copied straight out of vanilla - renders the player model on screen
	 */
	public static void drawPlayerModel(int x, int y, int scale, float yaw, float pitch, EntityLivingBase entity) {
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		GL11.glPushMatrix();
		GL11.glTranslatef(x, y, 50.0F);
		GL11.glScalef(-scale, scale, scale);
		GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
		float f2 = entity.renderYawOffset;
		float f3 = entity.rotationYaw;
		float f4 = entity.rotationPitch;
		float f5 = entity.prevRotationYawHead;
		float f6 = entity.rotationYawHead;
		GL11.glRotatef(135.0F, 0.0F, 1.0F, 0.0F);
		RenderHelper.enableStandardItemLighting();
		GL11.glRotatef(-135.0F, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(-((float) Math.atan(pitch / 40.0F)) * 20.0F, 1.0F, 0.0F, 0.0F);
		entity.renderYawOffset = (float) Math.atan(yaw / 40.0F) * 20.0F;
		entity.rotationYaw = (float) Math.atan(yaw / 40.0F) * 40.0F;
		entity.rotationPitch = -((float) Math.atan(pitch / 40.0F)) * 20.0F;
		entity.rotationYawHead = entity.rotationYaw;
		entity.prevRotationYawHead = entity.rotationYaw;
		GL11.glTranslatef(0.0F, entity.yOffset, 0.0F);
		RenderManager.instance.playerViewY = 180.0F;
		RenderManager.instance.renderEntityWithPosYaw(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
		entity.renderYawOffset = f2;
		entity.rotationYaw = f3;
		entity.rotationPitch = f4;
		entity.prevRotationYawHead = f5;
		entity.rotationYawHead = f6;
		GL11.glPopMatrix();
		RenderHelper.disableStandardItemLighting();
		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
	}
}
/**
 * Step 4.1: Finishing Up - Odds and Ends
 */
/*
Everything is ready to go, but there are several things left to do before we can use our new Inventory in game. I won't be 
putting full code here, so if something doesn't make sense to you, please check the prerequisites section for help.
First, much like EnderChest inventory is stored in EntityPlayer, we need to store the custom IInventory in an EntityPlayer 
version of IExtendedEntityProperties. To do so, we only add 3 lines to our existing ExtendedPlayer class (you have one, 
right? If not, check the prerequisites for help):
*/
// This goes with all other variables declared at the beginning of the class, such as currentMana and maxMana
/** Custom inventory slots will be stored here - be sure to save to NBT! */
public final InventoryCustomPlayer inventory = new InventoryCustomPlayer();

// Next, add lines to write/read NBT methods
// 'properties' is the NBTTagCompound variable name I used in my other tutorial on IExtendedEntityProperties
// Feel free to substitute it with the NBTTagCompound from the method parameter

// Write custom inventory to NBT
this.inventory.writeToNBT(properties);

// Read custom inventory from NBT
this.inventory.readFromNBT(properties);

/*
You don't need to worry about syncing the inventory data with packets or trying to get it to persist through player
death - that should all happen automatically.

Next, you need to register a GuiHandler in your main mod; I use my CommonProxy for this.
*/
NetworkRegistry.instance().registerGuiHandler(this, new CommonProxy());
/*
Now, in your registered GuiHandler, you need to return the correct gui element for both server and client, depending on 
the gui id. Since I despise magic numbers, I always define constant id values for my gui's. In this case, they are stored in 
my main mod.
*/
// In main mod:
/** This is used to keep track of GUIs that we make*/
private static int modGuiIndex = 0;

/** Custom GUI indices: */
public static final int GUI_CUSTOM_INV = modGuiIndex++;

// And now in CommonProxy, the class I registered as my GuiHandler:
@Override
public Object getServerGuiElement(int guiId, EntityPlayer player, World world, int x, int y, int z)
{
	if (guiId == TutorialMain.GUI_CUSTOM_INV) {
		return new ContainerCustomPlayer(player, player.inventory, ExtendedPlayer.get(player).inventory);
	} else {
		return null;
	}
}

@Override
public Object getClientGuiElement(int guiId, EntityPlayer player, World world, int x, int y, int z)
{
	if (guiId == TutorialMain.GUI_CUSTOM_INV) {
		return new GuiCustomPlayerInventory(player, player.inventory, ExtendedPlayer.get(player).inventory);
	} else {
		return null;
	}
}
/**
 * Step 4.2: Finishing Up - Setting up a KeyHandler
 */
/*
Finally, you need a way to open your gui. Since we're not using a special Item or Block to access our inventory upon 
activation, the only real alternative is to use a KeyBinding and KeyHandler class. If you're not sure what any of these are, 
please refer back to the prerequisites and follow the tutorial there.

Don't forget to register your KeyHandler in your main mod, but only on the client side!

NOTE: The TutKeyHandler class is 1.6.4 code - for 1.7 and above, KeyHandler no longer exists; instead, you must
subscribe to the KeyInputEvent. See the Tutorial Demo for an example.
*/
// TutKeyHandler class
@SideOnly(Side.CLIENT)
public class TutKeyHandler extends KeyHandler
{
	/** Store Minecraft so we don't have to get it each time */
	private final Minecraft mc;

	/** Not really important. I use it to store/find keys in the config file */
	public static final String label = "Tutorial Key";

	/** Key index for easy handling */
	public static final int CUSTOM_INV = 0;

	/** Key descriptions */
	private static final String[] desc = {"Custom Inventory"};

	/** Default key values */
	private static final int[] keyValues = {Keyboard.KEY_O};

	/** Stores custom keybindings for easy reference */
	public static final KeyBinding[] keys = new KeyBinding[desc.length];

	/**
	 * This will initialize all key bindings and create a new key handler; you can
	 * pass in a Configuration file if you want to read default key values from
	 * your config, but the settings can be changed in game, too.
	 */
	public static void init() {
		boolean[] repeat = new boolean[desc.length];
		for (int i = 0; i < desc.length; ++i) {
			keys[i] = new KeyBinding(desc[i], keyValues[i]);
			repeat[i] = false;
		}

		KeyBindingRegistry.registerKeyBinding(new TutKeyHandler(keys, repeat));
	}

	private TutKeyHandler(KeyBinding[] keyBindings, boolean[] repeatings) {
		super(keyBindings, repeatings);
		this.mc = Minecraft.getMinecraft();
	}

	@Override
	public String getLabel() {
		return this.label;
	}

	@Override
	public void keyDown(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd, boolean isRepeat)
	{
		if (tickEnd)
		{
			// if you don't want the key firing while in chat or a gui is open, check if the game is in focus:
			if (mc.inGameHasFocus) {
				if (kb == keys[CUSTOM_INV]) {
					// Send a packet to the server using a method we'll create in the next step
					TutorialPacketHandler.sendOpenGuiPacket(TutorialMain.GUI_CUSTOM_INV);
					// opening the gui server side automatically opens the client side as well,
					// so we don't need to do anything else
				}
			} else {
				// a gui is open; in 1.6.4 and earlier, you can close the GUI from here:
				if (kb == keys[CUSTOM_INV] && player.openContainer instanceof ContainerCustomPlayer) {
					// in 1.7.2, you need to do this from your custom GUI class instead
					player.closeScreen();
				}
			}
		}
	}

	@Override
	public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd) {
		// Don't need to do anything here!
	}

	@Override
	public EnumSet<TickType> ticks() {
		// We're only interested in player ticks, as that's when the keyboard will fire
		return EnumSet.of(TickType.PLAYER);
	}
}

// IMPORTANT!!!
// Now that it's all set up, don't forget to register your KeyHandler!!!
// Register KeyHandler in your client proxy, since it is client side only:
public class ClientProxy extends CommonProxy {

	@Override
	public void registerRenderers() {
		// feel free to create a separate method, such as init() or whatever, if you do not want to lump
		// key bindings and other stuff together with all the render registration code
		TutKeyHandler.init();
	}
}

/**
 * Step 4.3: Finishing Up - Handling your Packet
 */
/*
If you followed the IExtendedEntityProperties tutorial, you already have a PacketHandler set up. We'll start from here, so 

if you haven't done so, read that tutorial now, as well as the tutorial on Packets.

Let's start with the PacketHandler class.
 */
// The first change we make is to define some packet id's:
/** Defining packet ids allow for subtypes of Packet250CustomPayload all on single channel */
public static final byte EXTENDED_PROPERTIES = 1, OPEN_SERVER_GUI = 2;

// Now, our onPacketData method needs some changes:
// Adding this code will allow us to easily distinguish between various CustomPayload packets
DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(packet.data));
byte packetType;

try {
	// Read the packet type
	packetType = inputStream.readByte();
} catch (IOException e) {
	e.printStackTrace();
	return;
}

// Now we check which channel, in case you have more than one registered
if (packet.channel.equals("tutchannel"))
{
	// Handle each case appropriately:
	switch(packetType) {
	case EXTENDED_PROPERTIES: handleExtendedProperties(packet, player, inputStream); break;
	case OPEN_SERVER_GUI: handleOpenGuiPacket(packet, (EntityPlayer) player, inputStream); break;
	default: System.out.println("[PACKET][WARNING] Unknown packet type " + packetType);
	}
}

// Notice that handleExtendedProperties now has a DataInputStream type argument, so you can remove the
// new instance of one in that method
// As for handleOpenGuiPacket, here's that method:
/**
 * This method will open the appropriate server gui element for the player
 */
private void handleOpenGuiPacket(Packet250CustomPayload packet, EntityPlayer player, DataInputStream inputStream)
{
	int guiID;
	// inputStream is already open, so we don't need to do anything other than continue reading from it:
	try {
		guiID = inputStream.readInt();
	} catch (IOException e) {
		e.printStackTrace();
		return;
	}
	
	// Now we can open the server gui element, which will automatically open the client element as well:
	player.openGui(TutorialMain.instance, guiID, player.worldObj, (int) player.posX, (int) player.posY, (int) player.posZ);
}

// Here's the method we used in the last step to send the packet:
/**
 * Sends a packet to the server telling it to open gui for player
 */
public static final void sendOpenGuiPacket(int guiId)
{
	ByteArrayOutputStream bos = new ByteArrayOutputStream();
	DataOutputStream outputStream = new DataOutputStream(bos);
	
	try {
		outputStream.writeByte(OPEN_SERVER_GUI);
		outputStream.writeInt(guiId);
	} catch (Exception ex) {
		ex.printStackTrace();
	}
	
	PacketDispatcher.sendPacketToServer(PacketDispatcher.getPacket("tutchannel", bos.toByteArray()));
}
/**
 * Step 5: Troubleshooting
 */
/*
There are lots of things that could cause your custom inventory not to work. I'll try to hit as many as I can here,
but the best bet is to re-read the tutorial and all related material. Make sure you've done ALL the steps.

1. Re-read the tutorial and see if you missed anything. Read supplementary material listed under prerequisites.

2. Did you remember to register ALL the different classes?
	- GuiHandler
 	- IExtendedEntityProperties / EventHandler
	- KeyBindings
	- PacketHandler

3. Is all of your mod annotation correct? @Mod, @NetworkMod, @Instance, @SidedProxy

4. If you didn't use constant variables to define all of your mod information, such as ModID, Channel, all the numbered
index values I showed you, etc., shame on you. Half your problems would be solved already had you done so.

5. Still having problems? Post your crash/error log and relevant classes in [ spoiler ] and [ code ]. Include a short
description of the issue and I'll see what I can do. In the meantime, RE-READ the tutorial and compare every line of
your code to it. If you did anything differently, chances are that is where the problem lies, as I have tested and used
this same code with no problems. (not to imply that it's perfect - I'm always open to suggestions and fixes if a
problem does exist!)

Well that's it. Hopefully you have now overridden the default inventory screen with numerous awesome item slots! I'd
love to see what you've done with it, so post links to your mods if you have them :D
 */

//==================== UPDATING ==================//
/*
Each of the following sections is a sequential update, starting from the code above made for Minecraft 1.6.4.
*/

//==================== UPDATING: Forge 804 --> 871 [both 1.6.4] ==================//
/*
Three things you'll need to change in the GUI files:
1. I18n.func_135053_a() is now I18n.getString()
2. mc.func_110434_K() is now mc.renderEngine OR mc.getTextureManager()
3. renderEngine.func_110577_a() is now renderEngine.bindTexture()
*/

//==================== UPDATING: 1.6.4 --> 1.7.2 ==================//
/*
First, make sure you have thoroughly read and understood the tutorial for 1.6.4, as this section is for updating a functioning item or player inventory only, not for setting it up from scratch.
*/

/*
 * Updating the Main Class
 */
/*
Remove the @NetworkMod stuff. That's all gone.
*/

/*
 * Updating the Item
 */
/*
Everything is pretty much exactly the same, with a few minor differences:
1. Remove the int parameter from the constructor; item IDs are history
2. Change 'Icon' to 'IIcon' (two I's)
3. Change 'IconRegister' to 'IIconRegister' (two I's)
4. Update imports
5. Be sure to change all your item references from itemID to the Item itself[/spoiler]
*/

/*
 * Updating the Gui
 */
/*
In the Gui, fontRenderer is now fontRendererObj, and you need to change to I18n.format if you were using that to translate the names at all. Use the updated IInventory method names for getInventoryName(), etc
*/

/*
 * Updating the IInventory
 */
/*
In all your Iinventory classes:
1. Change isInvNameLocalized() to hasCustomInventoryName()
2. Change getInvName() to getInventoryName()
3. Change onInventoryChanged() to markDirty()
4. Change open/closeChest() to open/closeInventory()
5. Reading NBTTagLists has changed slightly:
*/
public void readFromNBT(NBTTagCompound compound) {
	// now you must include the NBTBase type ID when getting the list; NBTTagCompound's ID is 10
	NBTTagList items = compound.getTagList(tagName, compound.getId());
	for (int i = 0; i < items.tagCount(); ++i) {
		// tagAt(int) has changed to getCompoundTagAt(int)
		NBTTagCompound item = items.getCompoundTagAt(i);
		byte slot = item.getByte("Slot");
		if (slot >= 0 && slot < getSizeInventory()) {
			inventory[slot] = ItemStack.loadItemStackFromNBT(item);
		}
	}
}

/*
 * Updating the Container and Slot
 */
/*
Nothing to do here, unless you checked for a specific itemID somewhere.
Change any reference to itemID to ItemStack.getItem() and the Item itself.
*/

/*
 * Updating the KeyHandler and KeyBindings
 */
/*
The KeyHandler class is history (as in you cannot extend it anymore), so we will combine our KeyBinding class directly into our KeyHandler class:
*/
public class KeyHandler
{
	/** Key index for easy handling */
	public static final int CUSTOM_INV = 0;

	/** Key descriptions; use a language file to localize the description later */
	private static final String[] desc = {"key.tut_inventory.desc"};

	/** Default key values  these can be changed using the in-game menu */
	private static final int[] keyValues = {Keyboard.KEY_P};

	private final KeyBinding[] keys;

	public KeyHandler() {
		// the advantage of doing it with the above static arrays is now we can just loop through
		// creating and registering all of our keybindings automatically
		keys = new KeyBinding[desc.length];
		for (int i = 0; i < desc.length; ++i) {
			// create the new KeyBinding:
			keys[i] = new KeyBinding(desc[i], keyValues[i], "key.tutorial.category");
			// and be sure to register it to the ClientRegistry:
			ClientRegistry.registerKeyBinding(keys[i]);
		}
	}

	// rather than the old KeyHandler class doing it for us
	// now we must subscribe to the KeyInputEvent ourselves
	@SubscribeEvent
	public void onKeyInput(KeyInputEvent event) {
		// first check that the player is not using the chat menu
		// you can use this method from before:
		// if (FMLClientHandler.instance().getClient().inGameHasFocus) {
		// or you can use this new one that is available, doesn't really matter
		if (!FMLClientHandler.instance().isGUIOpen(GuiChat.class)) {
			// you can get the key code of the key pressed using the Keyboard class:
			int kb = Keyboard.getEventKey();
			// similarly, you can get the key state, but this will always be true when the event is fired:
			boolean isDown = Keyboard.getEventKeyState();

			// same as before, chain if-else if statements to find which of your custom keys
			// was pressed and act accordingly:
			if (kb == keys[CUSTOM_INV].getKeyCode()) {
				EntityPlayer player = FMLClientHandler.instance().getClient().thePlayer;
				// if (player.openContainer instanceof ContainerCustomPlayer) {
				// before you could close the screen from here, but that no longer seems to be
				// possible instead, you need to do so from within the GUI itself
				// so we will just send a packet to open the GUI:
				TutorialMain.packetPipeline.sendToServer(new OpenGuiPacket(TutorialMain.GUI_CUSTOM_INV));
			}
		}
	}
}

/*
In your ClientProxy, be sure to register your KeyHandler class:
*/
// KeyInputEvent is in the FML package, meaning it's posted to the FML event bus
// rather than the regular Forge event bus:
FMLCommonHandler.instance().bus().register(new KeyHandler());

/*
 * Updating the Network (NOT for 1.7.10!!! Skip this entire section if updating past 1.7.2)
 */
/*
For 1.7.2, check out the wiki tutorial: http://www.minecraftforge.net/wiki/Netty_Packet_Handling

Once you have copied and pasted that code (seriously, it's that simple) into your project, you will need to make a new package for your packets and register them with the PacketPipeline. I do it like so:
*/
// modify this method from the wiki in the PacketPipeline class:
public void initialise() {
	// NOTE: Be sure to change the channel from "TUT" to whatever you are using!!!
	this.channels = NetworkRegistry.INSTANCE.newChannel("TUT", this);

	// line added by me to register all my packets:
	registerPackets();
}

// And add this method right below it
// IMPORTANT: Always remember to add your newly created packet classes here or you WILL crash
public void registerPackets() {
	registerPacket(OpenGuiPacket.class);
	registerPacket(SyncPlayerPropsPacket.class);
}

/*
That's really all you have to do. Then you just make the packet classes themselves.
*/

// OpenGuiPacket
public class OpenGuiPacket extends AbstractPacket
{
	// this will store the id of the gui to open
	private int id;

	// The basic, no-argument constructor MUST be included to use the new automated handling
	public OpenGuiPacket() {}

	// if there are any class fields, be sure to provide a constructor that allows
	// for them to be initialized, and use that constructor when sending the packet
	public OpenGuiPacket(int id) {
		this.id = id;
	}

	@Override
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
		// basic Input/Output operations, very much like DataOutputStream
		buffer.writeInt(id);
	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
		// basic Input/Output operations, very much like DataInputStream
		id = buffer.readInt();
	}

	@Override
	public void handleClientSide(EntityPlayer player) {
		// for opening a GUI, we don't need to do anything here
	}

	@Override
	public void handleServerSide(EntityPlayer player) {
		// because we sent the gui's id with the packet, we can handle all cases with one line:
		player.openGui(TutorialMain.instance, id, player.worldObj, (int) player.posX, (int) player.posY, (int) player.posZ);
	}
}

// SyncPlayerProps Packet
public class SyncPlayerPropsPacket extends AbstractPacket
{
	// Previously, we've been writing each field in our properties one at a time,
	// but that is really annoying, and we've already done it in the save and load
	// NBT methods anyway, so here's a slick way to efficiently send all of your
	// extended data, and no matter how much you add or remove, you'll never have
	// to change the packet / synchronization of your data.

	// this will store our ExtendedPlayer data, allowing us to easily read and write
	private NBTTagCompound data;

	// The basic, no-argument constructor MUST be included to use the new automated handling
	public SyncPlayerPropsPacket() {}

	// We need to initialize our data, so provide a suitable constructor:
	public SyncPlayerPropsPacket(EntityPlayer player) {
		// create a new tag compound
		data = new NBTTagCompound();
		// and save our player's data into it
		ExtendedPlayer.get(player).saveNBTData(data);
	}

	@Override
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
		// ByteBufUtils provides a convenient method for writing the compound
		ByteBufUtils.writeTag(buffer, data);
	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
		// luckily, ByteBufUtils provides an easy way to read the NBT
		data = ByteBufUtils.readTag(buffer);
	}

	@Override
	public void handleClientSide(EntityPlayer player) {
		// now we can just load the NBTTagCompound data directly; one and done, folks
		ExtendedPlayer.get(player).loadNBTData(data);
	}

	@Override
	public void handleServerSide(EntityPlayer player) {
		// we never send this packet to the server, so do nothing here
	}
}

/*
Updating IextendedEntityProperties and the EventHandler
Not much has changed here:
*/
private static final String getSaveKey(EntityPlayer player) {
	// no longer a username field, so use the command sender name instead:
	return player.getCommandSenderName() + ":" + EXT_PROP_NAME;
}

public static final void loadProxyData(EntityPlayer player) {
	ExtendedPlayer playerData = ExtendedPlayer.get(player);
	NBTTagCompound savedData = CommonProxy.getEntityData(getSaveKey(player));
	if (savedData != null) { playerData.loadNBTData(savedData); }
	// we are replacing the entire sync() method with a single line; more on packets later
	// data can by synced just by sending the appropriate packet, as everything is handled internally by the packet class
	TutorialMain.packetPipeline.sendTo(new SyncPlayerPropsPacket(player), (EntityPlayerMP) player);
}

// remove the public void sync() method; it is no longer needed
/*
For the EventHandler, pretty much all you have to do is replace @ForgeSubscribe with @SubscribeEvent and fix your imports. Everything else should work the same as before. Easy.
*/

//==================== UPDATING: 1.7.2 --> 1.7.10 ==================//
//You should only need to update the network code; see:
//http://www.minecraftforum.net/forums/mapping-and-modding/mapping-and-modding-tutorials/2137055

//==================== UPDATING: 1.7.10 --> 1.8 ==================//
/*
 * Updating an IInventory class
 */
/*
IInventory has the most notable changes, so we'll start there:

1. 'Name' methods have 'Inventory' removed from them, and are inherited from IWorldNameable
2. open/closeInventory now take an EntityPlayer parameter
3. IInventory defines 4 new methods, only one of which we need to truly implement.

Code to change in your IInventory classes:
*/
// Step 1: Update methods for IWorldNameable
@Override
// public String getInventoryName() {
public String getName() {
	return name;
}

@Override
//public boolean hasCustomInventoryName() {
public boolean hasCustomName() {
	return name.length() > 0;
}

@Override
public IChatComponent getDisplayName() {
	return new ChatComponentText(name);
}

// Step 2: Add EntityPlayer parameter to open/close inventory (and yes, they still do nothing, usually)
@Override
//public void openInventory() {}
public void openInventory(EntityPlayer player) {}

@Override
//public void closeInventory() {}
public void closeInventory(EntityPlayer player) {}

// Step 3: Add new methods defined by IInventory:
@Override
public int getField(int id) {
	return 0;
}

@Override
public void setField(int id, int value) {}

@Override
public int getFieldCount() {
	return 0;
}

// This is the only one you need to implement, and it just needs to set every stack to null
@Override
public void clear() {
	for (int i = 0; i < inventory.length; ++i) {
		inventory[i] = null;
	}
}

/*
 * Updating a GUI class
 */
/*
1. GuiInventory.func_147046_a finally has a name: 'GuiInventory.drawEntityOnScreen'
2. keyTyped needs to thrown an IOException
3. If you used your inventory's custom name, update the method calls

Changes in code:
*/
// 1: GuiInventory.func_147046_a finally has a name: 'GuiInventory.drawEntityOnScreen'
//GuiInventory.func_147046_a(guiLeft + 51, guiTop + 75, 30, guiLeft + 51 - xSize_lo, guiTop + 25 - ySize_lo, mc.thePlayer);
GuiInventory.drawEntityOnScreen(guiLeft + 51, guiTop + 75, 30, guiLeft + 51 - xSize_lo, guiTop + 25 - ySize_lo, mc.thePlayer);

// 2: keyTyped needs to thrown an IOException
//protected void keyTyped(char c, int keyCode) {
protected void keyTyped(char c, int keyCode) throws IOException {

// 3: If you used your inventory's custom name, update the method calls
//String s = inventory.hasCustomInventoryName() ? inventory.getInventoryName() : I18n.format(inventory.getInventoryName());
String s = inventory.hasCustomName() ? inventory.getName() : I18n.format(inventory.getName());

/*
 * Updating IMPORTS
 */
/*
Last but most importantly, be sure to update your 'cpw.mods.fml...' imports EVERYWHERE - change them to 'net.minecraftforge.fml...'; for example, here are the imports from our IGuiHandler:
*/
//import cpw.mods.fml.common.network.IGuiHandler;
//import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/*
That's all you should need to get your inventories up and running! Good luck with 1.8!
*/

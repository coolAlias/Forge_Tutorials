/*
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

NOTE: Updating from Forge 804 to 871
Three things you'll need to change in the GUI files:
1. I18n.func_135053_a() is now I18n.getString()
2. mc.func_110434_K() is now mc.renderEngine OR mc.getTextureManager()
3. renderEngine.func_110577_a() is now renderEngine.bindTexture()

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
*/
// RegisterKeyBinding class
public class RegisterKeyBindings
{
	
}

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

		KeyBindingRegistry.registerKeyBinding(new TutKeyHandler(key, repeat));
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





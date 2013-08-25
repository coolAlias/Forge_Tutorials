import java.util.Arrays;

/**
 * TUTORIAL: Creating a Multi-Input Furnace with Variable-Input Recipes
 */
/*
Do you need a furnace that can handle 3 inputs? How about 5? 10? Do some of your
furnace recipes require 2 input itemstacks to smelt, while other recipes call for
more or only 1? Look no further � below you will learn how to do just that!

But fist, if you've never made even a single-input furnace or other TileEntity-based Container/Gui, please do that before trying to follow this tutorial. I'm not going to cover all aspects of the Gui, Block and other such things here as there are other tutorials on that.

On to the tutorial.
*/
/**
 * Step 1: Make a Container Class
 */
public class ContainerArcaneInscriber extends Container
{
	private TileEntityArcaneInscriber inscriber;
	private int lastProgressTime;
	private int lastBurnTime;
	private int lastItemBurnTime;

	// NOTE that here you could add as many slots as you want.
	// I have a complementary discharge slot array because the items I use as fuel
	// are rechargeable and I want to get them back
	public static final int INPUT[] = {0,1,2,3,4,5,6};
	public static final int DISCHARGE[] = {7,8,9,10,11,12,13};
	public static final int RUNE_SLOTS = INPUT.length, BLANK_SCROLL = RUNE_SLOTS*2, RECIPE = BLANK_SCROLL+1,
			OUTPUT = RECIPE+1, INV_START = OUTPUT+1, INV_END = INV_START+26, HOTBAR_START = INV_END+1,
			HOTBAR_END= HOTBAR_START+8;

	public ContainerArcaneInscriber(InventoryPlayer inventoryPlayer, TileEntityArcaneInscriber par2TileEntityArcaneInscriber)
	{
		int i;

		this.inscriber = par2TileEntityArcaneInscriber;

		// ADD CUSTOM SLOTS
		for (i = 0; i < RUNE_SLOTS; ++i) {
			this.addSlotToContainer(new Slot(par2TileEntityArcaneInscriber, INPUT[i], 43 + (18*i), 15));
		}
		for (i = 0; i < RUNE_SLOTS; ++i) {
			this.addSlotToContainer(new SlotArcaneInscriberDischarge(par2TileEntityArcaneInscriber, DISCHARGE[i], 43 + (18*i), 64));
		}

		this.addSlotToContainer(new Slot(par2TileEntityArcaneInscriber, BLANK_SCROLL, 63, 39));
		this.addSlotToContainer(new SlotArcaneInscriberRecipe(par2TileEntityArcaneInscriber, RECIPE, 17, 35));
		this.addSlotToContainer(new SlotArcaneInscriber(inventoryPlayer.player, par2TileEntityArcaneInscriber, OUTPUT, 119, 39));

		// ADD PLAYER INVENTORY
		for (i = 0; i < 3; ++i)
		{
			for (int j = 0; j < 9; ++j)
			{
				this.addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
			}
		}

		// ADD PLAYER ACTION BAR
		for (i = 0; i < 9; ++i)
		{
			this.addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 142));
		}
	}

	public void addCraftingToCrafters(ICrafting iCrafting)
	{
		super.addCraftingToCrafters(iCrafting);
		iCrafting.sendProgressBarUpdate(this, 0, this.inscriber.inscribeProgressTime);
	}

	/**
	 * Looks for changes made in the container, sends them to every listener.
	 */
	public void detectAndSendChanges()
	{
		super.detectAndSendChanges();

		for (int i = 0; i < this.crafters.size(); ++i)
		{
			ICrafting icrafting = (ICrafting)this.crafters.get(i);

			if (this.lastProgressTime != this.inscriber.inscribeProgressTime)
			{
				icrafting.sendProgressBarUpdate(this, 0, this.inscriber.inscribeProgressTime);
			}
		}

		this.lastProgressTime = this.inscriber.inscribeProgressTime;
	}

	@SideOnly(Side.CLIENT)
	public void updateProgressBar(int par1, int par2)
	{
		if (par1 == 0)
		{
			this.inscriber.inscribeProgressTime = par2;
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer)
	{
		return this.inscriber.isUseableByPlayer(entityplayer);
	}

	/**
	 * Called when a player shift-clicks on a slot. You must override this or you will crash when someone does that.
	 */
	public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par2)
	{
		ItemStack itemstack = null;
		Slot slot = (Slot)this.inventorySlots.get(par2);

		if (slot != null && slot.getHasStack())
		{
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			// If item is in TileEntity inventory
			if (par2 < INV_START)
			{
				// try to place in player inventory / action bar
				if (!this.mergeItemStack(itemstack1, INV_START, HOTBAR_END+1, true))
				{
					return null;
				}

				slot.onSlotChange(itemstack1, itemstack);
			}
			// Item is in player inventory, try to place in inscriber
			else if (par2 > OUTPUT)
			{
				// if it is a charged rune, place in the first open input slot
				if (TileEntityArcaneInscriber.isSource(itemstack1))
				{
					if (!this.mergeItemStack(itemstack1, INPUT[0], INPUT[RUNE_SLOTS-1]+1, false))
					{
						return null;
					}
				}
				// if it's a blank scroll, place in the scroll slot
				else if (itemstack1.itemID == ArcaneLegacy.scrollBlank.itemID)
				{
					if (!this.mergeItemStack(itemstack1, BLANK_SCROLL, BLANK_SCROLL+1, false))
					{
						return null;
					}
				}
				// item in player's inventory, but not in action bar
				else if (par2 >= INV_START && par2 < HOTBAR_START)
				{
					// place in action bar
					if (!this.mergeItemStack(itemstack1, HOTBAR_START, HOTBAR_END+1, false))
					{
						return null;
					}
				}
				// item in action bar - place in player inventory
				else if (par2 >= HOTBAR_START && par2 < HOTBAR_END+1 && !this.mergeItemStack(itemstack1, INV_START, HOTBAR_START, false))
				{
					return null;
				}
			}
			// In one of the inscriber slots; try to place in player inventory / action bar
			else if (!this.mergeItemStack(itemstack1, INV_START, HOTBAR_END+1, false))
			{
				return null;
			}

			if (itemstack1.stackSize == 0)
			{
				slot.putStack((ItemStack)null);
			}
			else
			{
				slot.onSlotChanged();
			}

			if (itemstack1.stackSize == itemstack.stackSize)
			{
				return null;
			}

			slot.onPickupFromSlot(par1EntityPlayer, itemstack1);
		}
		return itemstack;
	}
}

/**
 * Step 2: Make your Custom Slots, if needed
 */
public class SlotArcaneInscriber extends Slot
{
	/** The player that is using the GUI where this slot resides. */
	private EntityPlayer thePlayer;
	private int field_75228_b;

	public SlotArcaneInscriber(EntityPlayer par1EntityPlayer, IInventory par2IInventory, int par3, int par4, int par5)
	{
		super(par2IInventory, par3, par4, par5);
		this.thePlayer = par1EntityPlayer;
	}

	/**
	 * Check if the stack is a valid item for this slot. Always true beside for the armor slots.
	 */
	public boolean isItemValid(ItemStack par1ItemStack)
	{
		return false;
	}

	/**
	 * Decrease the size of the stack in slot (first int arg) by the amount of the second int arg. Returns the new
	 * stack.
	 */
	public ItemStack decrStackSize(int par1)
	{
		if (this.getHasStack())
		{
			this.field_75228_b += Math.min(par1, this.getStack().stackSize);
		}

		return super.decrStackSize(par1);
	}

	public void onPickupFromSlot(EntityPlayer par1EntityPlayer, ItemStack par2ItemStack)
	{
		this.onCrafting(par2ItemStack);
		super.onPickupFromSlot(par1EntityPlayer, par2ItemStack);
	}

	/**
	 * the itemStack passed in is the output - ie, iron ingots, and pickaxes, not ore and wood. Typically increases an
	 * internal count then calls onCrafting(item).
	 */
	protected void onCrafting(ItemStack par1ItemStack, int par2)
	{
		this.field_75228_b += par2;
		this.onCrafting(par1ItemStack);
	}

	/** The itemStack passed in is the output - ie, iron ingots, and pickaxes, not ore and wood. */
	protected void onCrafting(ItemStack par1ItemStack)
	{
		par1ItemStack.onCrafting(this.thePlayer.worldObj, this.thePlayer, this.field_75228_B);

		if (!this.thePlayer.worldObj.isRemote)
		{
			int i = this.field_75228_b;
			float f = SpellRecipes.spells().getExperience(par1ItemStack);
			int j;

			if (f == 0.0F)
			{
				i = 0;
			}
			else if (f < 1.0F)
			{
				j = MathHelper.floor_float((float)i * f);

				if (j < MathHelper.ceiling_float_int((float)i * f) && (float)Math.random() < (float)i * f - (float)j)
				{
					++j;
				}

				i = j;
			}

			while (i > 0)
			{
				j = EntityXPOrb.getXPSplit(i);
				i -= j;
				this.thePlayer.worldObj.spawnEntityInWorld(new EntityXPOrb(this.thePlayer.worldObj, this.thePlayer.posX, this.thePlayer.posY + 0.5D, this.thePlayer.posZ + 0.5D, j));
			}
		}

		this.field_75228_b = 0;
	}
}

class SlotArcaneInscriberDischarge extends Slot
{
	private int field_75228_b;

	public SlotArcaneInscriberDischarge(IInventory par2IInventory, int par3, int par4, int par5)
	{
		super(par2IInventory, par3, par4, par5);
	}

	/**
	 * Check if the stack is a valid item for this slot. Always true beside for the armor slots.
	 */
	public boolean isItemValid(ItemStack par1ItemStack)
	{
		return false;
	}

	/**
	 * Decrease the size of the stack in slot (first int arg) by the amount of the second int arg. Returns the 
	 * new stack.
	 */
	public ItemStack decrStackSize(int par1)
	{
		if (this.getHasStack())
		{
			this.field_75228_b += Math.min(par1, this.getStack().stackSize);
		}

		return super.decrStackSize(par1);
	}

	public void onPickupFromSlot(EntityPlayer par1EntityPlayer, ItemStack par2ItemStack)
	{
		super.onPickupFromSlot(par1EntityPlayer, par2ItemStack);
	}
}

class SlotArcaneInscriberRecipe extends Slot
{
	private int field_75228_b;

	public SlotArcaneInscriberRecipe(IInventory par2IInventory, int par3, int par4, int par5)
	{
		super(par2IInventory, par3, par4, par5);
	}

	/**
	 * Check if the stack is a valid item for this slot. Always true beside for the armor slots.
	 */
	public boolean isItemValid(ItemStack par1ItemStack)
	{
		return false;
	}

	/**
	 * Return whether this slot's stack can be taken from this slot.
	 */
	public boolean canTakeStack(EntityPlayer par1EntityPlayer)
	{
		return false;
	}

	/**
	 * Decrease the size of the stack in slot (first int arg) by the amount of the second int arg. Returns the new
	 * stack.
	 */
	public ItemStack decrStackSize(int par1)
	{
		if (this.getHasStack())
		{
			this.field_75228_b += Math.min(par1, this.getStack().stackSize);
		}

		return super.decrStackSize(par1);
	}

	public void onPickupFromSlot(EntityPlayer par1EntityPlayer, ItemStack par2ItemStack)
	{
		// can't be picked up so nothing here
	}
}

/**
 * Step 3: Your Tile Entity Class
 */
public class TileEntityArcaneInscriber extends TileEntity implements ISidedInventory
{
	private static final int[] slots_top = new int[] {0};
	private static final int[] slots_bottom = new int[] {2, 1};
	private static final int[] slots_sides = new int[] {1};

	/** Array bounds = number of slots in ContainerArcaneInscriber */
	private ItemStack[] inscriberInventory = new ItemStack[ContainerArcaneInscriber.INV_START];

	/** Time required to scribe a single scroll */
	private static final int INSCRIBE_TIME = 100, RUNE_CHARGE_TIME = 400;

	/** The number of ticks that the inscriber will keep inscribing */
	public int currentInscribeTime;

	/** The number of ticks that a charged rune will provide */
	public int inscribeTime = 400;

	/** The number of ticks that the current scroll has been inscribing for */
	public int inscribeProgressTime;

	private String displayName = "Arcane Inscriber";

	public TileEntityArcaneInscriber() {
	}

	@Override
	public int getSizeInventory() {
		return inscriberInventory.length;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return this.inscriberInventory[slot];
	}

	@Override
	public ItemStack decrStackSize(int slot, int amt)
	{
		ItemStack stack = getStackInSlot(slot);
		if (stack != null) {
			if (stack.stackSize <= amt) {
				setInventorySlotContents(slot, null);
			} else {
				stack = stack.splitStack(amt);
				if (stack.stackSize == 0) {
					setInventorySlotContents(slot, null);
				}
			}
		}
		return stack;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot)
	{
		ItemStack stack = getStackInSlot(slot);
		if (stack != null) { setInventorySlotContents(slot, null); }
		return stack;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack)
	{
		inscriberInventory[slot] = stack;
		if (stack != null && stack.stackSize > getInventoryStackLimit()) {
			stack.stackSize = getInventoryStackLimit();
		}  
	}

	@Override
	public String getInvName() {
		return this.isInvNameLocalized() ? this.displayName : "container.arcaneinscriber";
	}

	/**
	 * If this returns false, the inventory name will be used as an unlocalized name, and translated into the player's
	 * language. Otherwise it will be used directly.
	 */
	public boolean isInvNameLocalized()
	{
		return this.displayName != null && this.displayName.length() > 0;
	}

	/**
	 * Sets the custom display name to use when opening a GUI linked to this tile entity.
	 */
	public void setGuiDisplayName(String par1Str)
	{
		this.displayName = par1Str;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	/**
	 * Returns an integer between 0 and the passed value representing how 
	 * close the current item is to being completely cooked
	 */
	@SideOnly(Side.CLIENT)
	public int getInscribeProgressScaled(int par1)
	{
		return this.inscribeProgressTime * par1 / INSCRIBE_TIME;
	}

	/**
	 * Returns an integer between 0 and the passed value representing how much burn time is left on the current fuel
	 * item, where 0 means that the item is exhausted and the passed value means that the item is fresh
	 */
	@SideOnly(Side.CLIENT)
	public int getInscribeTimeRemainingScaled(int par1)
	{
		return this.currentInscribeTime * par1 / this.INSCRIBE_TIME;
	}

	/**
	 * Returns true if the furnace is currently burning
	 */
	public boolean isInscribing()
	{
		return this.currentInscribeTime > 0;
	}

	/**
	 * Allows the entity to update its state. Overridden in most subclasses, e.g. the mob spawner uses this to count
	 * ticks and creates a new spawn inside its implementation.
	 */
	public void updateEntity()
	{
		boolean flag = this.currentInscribeTime > 0;
		boolean flag1 = false;

		if (this.currentInscribeTime > 0)
		{
			--this.currentInscribeTime;

			// Container recipe doesn't match current non-null InscribingResult
			flag1 = (this.inscriberInventory[ContainerArcaneInscriber.RECIPE] != this.getCurrentRecipe() && this.getCurrentRecipe() != null);
			// Recipe changed - reset timer and current recipe slot
			if (flag1)
			{
				this.inscriberInventory[ContainerArcaneInscriber.RECIPE] = this.getCurrentRecipe();
				this.onInventoryChanged();
				this.currentInscribeTime = 0;
			}
		}

		if (!this.worldObj.isRemote)
		{
			if (this.currentInscribeTime == 0)
			{
				flag1 = (this.inscriberInventory[ContainerArcaneInscriber.RECIPE] != this.getCurrentRecipe());
				this.inscriberInventory[ContainerArcaneInscriber.RECIPE] = this.getCurrentRecipe();
				// Recipe changed - update inventory
				if (flag1) { this.onInventoryChanged(); }

				if (this.canInscribe()) {
					this.currentInscribeTime = this.getInscriberChargeTime(this.inscriberInventory[0]);
				}

				if (this.currentInscribeTime > 0)
				{
					flag1 = true;

					// Decrement input slots, increment discharge slots
					for (int i = 0; i < ContainerArcaneInscriber.RUNE_SLOTS; ++i)
					{
						if (this.inscriberInventory[ContainerArcaneInscriber.INPUT[i]] != null)
						{
							--this.inscriberInventory[ContainerArcaneInscriber.INPUT[i]].stackSize;
							if (this.inscriberInventory[ContainerArcaneInscriber.DISCHARGE[i]] != null) {
								++this.inscriberInventory[ContainerArcaneInscriber.DISCHARGE[i]].stackSize;
							}
							else {
								ItemStack discharge = new ItemStack(ArcaneLegacy.runeBasic,1,this.inscriberInventory[ContainerArcaneInscriber.INPUT[i]].getItemDamage());
								this.inscriberInventory[ContainerArcaneInscriber.DISCHARGE[i]] = discharge.copy();
							}

							if (this.inscriberInventory[ContainerArcaneInscriber.INPUT[i]].stackSize == 0)
							{
								this.inscriberInventory[ContainerArcaneInscriber.INPUT[i]] = this.inscriberInventory[ContainerArcaneInscriber.INPUT[i]].getItem().getContainerItemStack(inscriberInventory[ContainerArcaneInscriber.INPUT[i]]);
							}
						}
					}
				}
			}

			if (this.isInscribing() && this.canInscribe())
			{
				++this.inscribeProgressTime;
				if (this.inscribeProgressTime == INSCRIBE_TIME)
				{
					this.inscribeProgressTime = 0;
					this.inscribeScroll();
					flag1 = true;
				}
			}
			else
			{
				this.inscribeProgressTime = 0;
			}

			if (flag != this.currentInscribeTime > 0)
			{
				flag1 = true;
				BlockArcaneInscriber.updateInscriberBlockState(this.currentInscribeTime > 0, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
			}
		}

		if (flag1)
		{
			this.onInventoryChanged();
		}
	}

	/**
	 * Returns true if the inscriber can inscribe a scroll;
	 * i.e. has a blank scroll, has a charged rune, destination stack isn't full, etc.
	 */
	private boolean canInscribe()
	{
		boolean canInscribe = true;
		// Still time remaining to inscribe current recipe
		if (this.isInscribing() && this.inscriberInventory[ContainerArcaneInscriber.RECIPE] != null)
		{
			canInscribe = (this.inscriberInventory[ContainerArcaneInscriber.BLANK_SCROLL] == null ? false : true);
		}
		// No charged rune in first input slot
		else if (this.inscriberInventory[ContainerArcaneInscriber.INPUT[0]] == null)
		{
			canInscribe = false;
		}
		// No blank scrolls to inscribe
		else if (this.inscriberInventory[ContainerArcaneInscriber.BLANK_SCROLL] == null)
		{
			canInscribe = false;
		}
		// Check if any of the discharge slots are full
		else
		{
			for (int i = 0; i < ContainerArcaneInscriber.RUNE_SLOTS && canInscribe; ++i)
			{
				if (this.inscriberInventory[ContainerArcaneInscriber.DISCHARGE[i]] != null)
				{
					// Check if input[i] and discharge[i] are mismatched
					if (this.inscriberInventory[ContainerArcaneInscriber.INPUT[i]] != null)
					{
						canInscribe = ((this.inscriberInventory[ContainerArcaneInscriber.INPUT[i]].getItemDamage() == this.inscriberInventory[ContainerArcaneInscriber.DISCHARGE[i]].getItemDamage())
								&& this.inscriberInventory[ContainerArcaneInscriber.DISCHARGE[i]].stackSize < this.inscriberInventory[ContainerArcaneInscriber.DISCHARGE[i]].getMaxStackSize());
					}
					else
					{
						canInscribe = this.inscriberInventory[ContainerArcaneInscriber.DISCHARGE[i]].stackSize < this.inscriberInventory[ContainerArcaneInscriber.DISCHARGE[i]].getMaxStackSize();
					}
				}
			}
		}

		if (canInscribe) {
			ItemStack itemstack = getCurrentRecipe();
			if (itemstack == null) { itemstack = this.inscriberInventory[ContainerArcaneInscriber.RECIPE]; }
			// Invalid recipe
			if (itemstack == null) return false;
			// Recipe is different from the current recipe
			if (this.inscriberInventory[ContainerArcaneInscriber.RECIPE] != null && !this.inscriberInventory[ContainerArcaneInscriber.RECIPE].isItemEqual(itemstack)) return false;
			// Output slot is empty, inscribe away!
			if (this.inscriberInventory[ContainerArcaneInscriber.OUTPUT] == null) return true;
			// Current scroll in output slot is different than recipe output
			if (!this.inscriberInventory[ContainerArcaneInscriber.OUTPUT].isItemEqual(itemstack)) return false;
			// Inscribing may surpass stack size limit
			int result = inscriberInventory[ContainerArcaneInscriber.OUTPUT].stackSize + itemstack.stackSize;
			return (result <= getInventoryStackLimit() && result <= itemstack.getMaxStackSize());
		}
		else
		{
			return canInscribe;
		}
	}

	public ItemStack getCurrentRecipe() {
		eturn SpellRecipes.spells().getInscribingResult(this.inscriberInventory);
	}

	/**
	 * Inscribe a blank scroll with the last current recipe
	 */
	public void inscribeScroll()
	{
		if (this.canInscribe())
		{
			ItemStack inscribeResult = this.inscriberInventory[ContainerArcaneInscriber.RECIPE];

			if (inscribeResult != null)
			{
				if (this.inscriberInventory[ContainerArcaneInscriber.OUTPUT] == null)
				{
					this.inscriberInventory[ContainerArcaneInscriber.OUTPUT] = inscribeResult.copy();
				}
				else if (this.inscriberInventory[ContainerArcaneInscriber.OUTPUT].isItemEqual(inscribeResult))
				{
					inscriberInventory[ContainerArcaneInscriber.OUTPUT].stackSize += inscribeResult.stackSize;
				}

				--this.inscriberInventory[ContainerArcaneInscriber.BLANK_SCROLL].stackSize;

				if (this.inscriberInventory[ContainerArcaneInscriber.BLANK_SCROLL].stackSize <= 0)
				{
					this.inscriberInventory[ContainerArcaneInscriber.BLANK_SCROLL] = null;
				}
			}
		}
	}

	/**
	 * Returns the number of ticks that the supplied rune will keep
	 * the inscriber running, or 0 if the rune isn't charged
	 */
	public static int getInscriberChargeTime(ItemStack rune)
	{
		if (rune != null && rune.itemID == ArcaneLegacy.runeCharged.itemID) {
			return RUNE_CHARGE_TIME;
		} else { return 0; }
	}

	/**
	 * Return true if item is an energy source (i.e. a charged rune)
	 */
	public static boolean isSource(ItemStack itemstack)
	{
		return getInscriberChargeTime(itemstack) > 0;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player)
	{
		return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this &&
				player.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) < 64;
	}

	@Override
	public void openChest() {}

	@Override
	public void closeChest() {}

	/**
	 * Returns true if automation is allowed to insert the given stack (ignoring stack size) into the given slot.
	 */
	public boolean isItemValidForSlot(int slot, ItemStack itemstack)
	{
		boolean isValid = false;

		if (slot >= ContainerArcaneInscriber.INPUT[0] && slot <= ContainerArcaneInscriber.INPUT[ContainerArcaneInscriber.RUNE_SLOTS-1]) 
		{
			isValid = itemstack.getItem().itemID == ArcaneLegacy.runeCharged.itemID;
		}
		else if (slot == ContainerArcaneInscriber.BLANK_SCROLL)
		{
			isValid = itemstack.getItem().itemID == ArcaneLegacy.scrollBlank.itemID;
		}
		return isValid;
	}

	/**
	 * Returns an array containing the indices of the slots that can be accessed by automation on the given side of this
	 * block.
	 */
	public int[] getAccessibleSlotsFromSide(int par1)
	{
		return par1 == 0 ? slots_bottom : (par1 == 1 ? slots_top : slots_sides);
	}

	/**
	 * Returns true if automation can insert the given item in the given slot from the given side.
	 * Args: Slot, item, side
	 */
	public boolean canInsertItem(int par1, ItemStack par2ItemStack, int par3)
	{
		return this.isItemValidForSlot(par1, par2ItemStack);
	}

	/**
	 * Returns true if automation can extract the given item in the given slot from the given side.
	 * Args: Slot, item, side
	 */
	public boolean canExtractItem(int slot, ItemStack itemstack, int side)
	{
		return (slot == ContainerArcaneInscriber.OUTPUT || (slot >= ContainerArcaneInscriber.DISCHARGE[0] && slot <= ContainerArcaneInscriber.DISCHARGE[ContainerArcaneInscriber.RUNE_SLOTS-1]));
	}

	@Override
	public void readFromNBT(NBTTagCompound tagCompound)
	{
		super.readFromNBT(tagCompound);
		NBTTagList nbttaglist = tagCompound.getTagList("Items");
		this.inscriberInventory = new ItemStack[this.getSizeInventory()];

		for (int i = 0; i < nbttaglist.tagCount(); ++i)
		{
			NBTTagCompound nbttagcompound1 = (NBTTagCompound)nbttaglist.tagAt(i);
			int b0 = nbttagcompound1.getInteger("Slot");

			f (b0 >= 0 && b0 < this.inscriberInventory.length)
			{
				this.inscriberInventory[b0] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
			}
		}

		this.currentInscribeTime = tagCompound.getShort("IncribeTime");
		this.inscribeProgressTime = tagCompound.getShort("InscribeProgress");
		// this.inscribeTime = INSCRIBE_TIME;

		if (tagCompound.hasKey("CustomName"))
		{
			this.displayName = tagCompound.getString("CustomName");
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound tagCompound)
	{
		super.writeToNBT(tagCompound);
		tagCompound.setShort("InscribeTime", (short)this.currentInscribeTime);
		tagCompound.setShort("InscribeProgress", (short)this.inscribeProgressTime);
		NBTTagList nbttaglist = new NBTTagList();

		for (int i = 0; i < this.inscriberInventory.length; ++i)
		{
			if (this.inscriberInventory[i] != null)
			{
				NBTTagCompound nbttagcompound1 = new NBTTagCompound();
				nbttagcompound1.setInteger("Slot", i);
				this.inscriberInventory[i].writeToNBT(nbttagcompound1);
				nbttaglist.appendTag(nbttagcompound1);
			}
		}

		tagCompound.setTag("Items", nbttaglist);

		if (this.isInvNameLocalized())
		{
			tagCompound.setString("CustomName", this.displayName);
		}
	}
}

/**
 * Step 4: Your Recipe class
 */
/*
This is where much of the magic happens
We'll use HashMaps to store an array of item metadata values to return an ItemStack
result. If the items in your recipes will have different Item IDs, then add those
to the hashmap as well. I only needed metadata values because all my recipes only
involve a single rune item with many subtypes.
 */
public class SpellRecipes
{
	private static final SpellRecipes spells = new SpellRecipes();
	// This creates a HashMap whose Key is a specific, ordered List of Integers
	private HashMap<List<Integer>, ItemStack> metaInscribingList = new HashMap<List<Integer>, ItemStack>();
	// Same as above except it gives us the experience for each crafting result
	private HashMap<List<Integer>, Float> metaExperience = new HashMap<List<Integer>, Float>();

	/**
	 * Used to call methods addInscribing and getInscribingResult.
	 */
	public static final SpellRecipes spells() {
		return spells;
	}

	/**
	 * Adds all recipes to the HashMap
	 */
	private SpellRecipes()
	{
		// Note that I have defined constant integer values in my ItemRune class
		// such that ItemRune.RUNE_NAME returns the appropriate int value for the
		// corresponding metadata

		// This one only takes 2 Items to craft:
		this.addInscribing(Arrays.asList(ItemRune.RUNE_CREATE,ItemRune.RUNE_FIRE),new ItemStack(ArcaneLegacy.scrollCombust), 0.3F);

		// This one takes 7 Items to craft (the max number of slots currently in my Arcane Inscriber, but I could easily add more):
		this.addInscribing(Arrays.asList(ItemRune.RUNE_AUGMENT,ItemRune.RUNE_AUGMENT,ItemRune.RUNE_CREATE,ItemRune.RUNE_AUGMENT,ItemRune.RUNE_LIFE,ItemRune.RUNE_SPACE,ItemRune.RUNE_TIME),new ItemStack(ArcaneLegacy.scrollHealAuraI), 1.0F);

		// Here's a generic format for adding both item ID and metadata:
		this.addInscribing(Arrays.asList(Item1.itemID, metadata1, Item2.itemID, metadata2, ... etc.), new ItemStack(craftResult.itemID, stacksize, metadata), XP);

		// You could also skip the addInscribing method and add directly to the HashMap:
		metaInscribingList.put(Arrays.asList(Item1.itemID, meta1, Item2.itemID, meta2,... ItemN.itemID, metaN), ItemStack(craftResult.itemID,stacksize,metadata));
		// Note that XP must be added each time as well, effectively doubling the lines of code in this method
		metaExperience.put(Arrays.asList(ItemResult.itemID, ItemResult.getItemDamage()), experience);
	}

	/**
	 * Adds an array of runes, the resulting scroll, and experience given
	 */
	public void addInscribing(List<Integer> runes, ItemStack scroll, float experience)
	{
		// Check if recipe already exists and print conflict information:
		if (metaInscribingList.containsKey(runes))
		{
			System.out.println("[WARNING] Conflicting recipe: " + runes.toString() + " for " + metaInscribingList.get(runes).toString());
		}
		else
		{
			// Add new recipe to the HashMap... wow, it looks so simple like this :)
			metaInscribingList.put(runes, scroll);
			metaExperience.put(Arrays.asList(scroll.itemID, scroll.getItemDamage()), experience);
		}
	}

/**
* Used to get the resulting ItemStack form a source inventory (fed to it by the contents of the slots in your container)
* @param item The Source inventory from your custom furnace input slots
* @return The result ItemStack
*/
	public ItemStack getInscribingResult(ItemStack[] runes)
	{
		// count the recipe length so we can make the appropriate sized array
		int recipeLength = 0;
		for (int i = 0; i < runes.length && runes[i] != null && i < ContainerArcaneInscriber.RUNE_SLOTS; ++i)
		{
			// +1 for metadata value of itemstack, add another +1 if you also need the itemID
			++recipeLength;
		}
		// make the array and fill it with the integer values from the passed in ItemStacks
		// Note that I'm only using the metadata value as all my runes have the same itemID
		Integer[] idIndex = new Integer[recipeLength];
		for (int i = 0; i < recipeLength; ++i) {
			// if you need itemID as well put this:
			// idIndex[i] = (Integer.valueOf(runes[i].itemID));
			// be sure to increment i before you do the metadata if you added an itemID
			idIndex[i] = (Integer.valueOf(runes[i].getItemDamage()));
		}
		// And use it as the key to get the correct result from the HashMap:
		return (ItemStack) metaInscribingList.get(Arrays.asList(idIndex));
	}

	/**
	 * Grabs the amount of base experience for this item to give when pulled from the furnace slot.
	 */
	public float getExperience(ItemStack item)
	{
		if (item == null || item.getItem() == null)
		{
			return 0;
		}
		float ret = -1; // value returned by "item.getItem().getSmeltingExperience(item);" when item doesn't specify experience to give
		if (ret < 0 && metaExperience.containsKey(Arrays.asList(item.itemID, item.getItemDamage())))
		{
			ret = metaExperience.get(Arrays.asList(item.itemID, item.getItemDamage()));
		}

		return (ret < 0 ? 0 : ret);
	}

	public Map<List<Integer>, ItemStack> getMetaInscribingList()
	{
		return metaInscribingList;
	}
}
/*
And that's it! Congratulations, you can now make a ridiculously flexible furnace.

HashMaps are our friend :D
*/

/**
 * Rendering a Custom Item Texture
 */
/*
In this tutorial, we will make a custom, Item-based projectile, a throwing rock. It covers all the steps
necessary to add any projectile entity into the game, including how to get it to render on the screen properly.

For this tutorial, we're going to make a new item ThrowingRock and get it to render
a custom texture when thrown. We'll assume you've got your main mod space set up.
If not, I HIGHLY suggest you go over to TechGuy543's tutorial at
http://www.minecraftforum.net/topic/960286-techguys-modding-tutorials/
and make sure you set up your main mod correctly or you will NOT be able to get
your custom item rendering.
*/

/**
 * Step 1: Creating a custom Item, e.g. ItemThrowingRock
 */
/*
Like I said, we're going to make a ThrowingRock. To do this, we just make a new
class called ThrowingRock in one of our packages. This class needs to exend Item,
but we're going to make it extend another class, BaseModItem, which extends Item.
The magic of inheritance will allow our ThrowingRock to extend Item through
BaseModItem. Here is BaseModItem's code.
*/
public class BaseModItem extends Item
{
	// no more IDs
	public BaseModItem() {
		super();
	}

	// IconRegister renamed to IIconRegister
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister iconRegister) {
		itemIcon = iconRegister.registerIcon("tutorial:" + getUnlocalizedName().substring(5).toLowerCase());
	}
}
/*
This allows us to avoid re-writing the registerIcon function in every single new
item we make. Nifty. Now here's what ItemThrowingRock will look like.
 */
public class ItemThrowingRock extends BaseModItem
{
	// no more ID parameter
	public ItemThrowingRock()
	{
		super();
		// we'll set the max stack size and creative tab from here:
		setMaxStackSize(18);
		setCreativeTab(CreativeTabs.tabCombat);
	}
}
/*
We set the maxStackSize and CreativeTab in the constructor above, but you could also
do it when you declare the item in your main mod instance:
*/

public static Item throwingRock;

// ALL Blocks, Items, etc. must be initialized during FML Pre-Initialization Event
@EventHandler
public void preInit(FMLPreInitializationEvent event) {
	throwingRock = new ItemThrowingRock().setUnlocalizedName("throwingRock").
		setMaxStackSize(18).setCreativeTab(CreativeTabs.tabCombat);
}

/*
So far our ThrowingRock doesn't do anything. We want this class to throw rocks, much
like snowballs, so we'll steal ItemSnowball's onImpact function and paste it into our
ItemThrowingRock class:
*/
/**
 * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
 */
public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer player)
{
	if (!player.capabilities.isCreativeMode)
	{
		--itemstack.stackSize;
	}

	world.playSoundAtEntity(player, "random.bow", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

	// IMPORTANT! Only spawn new entities on the server. If the world is not remote,
	// that means you are on the server:
	if (!world.isRemote)
	{
		world.spawnEntityInWorld(new EntitySnowball(world, player));
	}

	return itemstack;
}
/*
Ok, run the code and you'll see your ThrowingRocks spawn snowballs. That's not what
we want. Change "EntitySnowball" to "EntityThrowingRock." You'll get an error, but
we'll fix that in step 2.
*/

/**
 * Step 2: Creating a custom Entity: EntityThrowingRock
 */
// Ok, we want our ThrowingRock to behave similarly to snowballs, so let's look at that class.
public class EntitySnowball extends EntityThrowable
{
	public EntitySnowball(World par1World)
	{
		super(par1World);
	}
	public EntitySnowball(World par1World, EntityLivingBase par2EntityLivingBase)
	{
		super(par1World, par2EntityLivingBase);
	}
	public EntitySnowball(World par1World, double par2, double par4, double par6)
	{
		super(par1World, par2, par4, par6);
	}
	/**
	 * Called when this EntityThrowable hits a block or entity.
	 */
	protected void onImpact(MovingObjectPosition par1MovingObjectPosition)
	{
		if (par1MovingObjectPosition.entityHit != null)
		{
			byte b0 = 0;
			if (par1MovingObjectPosition.entityHit instanceof EntityBlaze)
			{
				b0 = 3;
			}

			par1MovingObjectPosition.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), (float)b0);
		}
		for (int i = 0; i < 8; ++i)
		{
			this.worldObj.spawnParticle("snowballpoof", this.posX, this.posY, this.posZ, 0.0D, 0.0D, 0.0D);
		}
		if (!this.worldObj.isRemote)
		{
			this.setDead();
		}
	}
}
/*
Well, we can just copy and paste this into our mod package and rename it to
EntityThrowingRock. Perfect. Here are some ways you can tweak it to make it more
rock-like:

Tweak 1: Do you see "byte b0" in the code? That's how much damage our entity will inflict. For snowballs, it is zero unless you hit a blaze. Well, we don't need that, so delete all that stuff about the blaze as well as the related import. Let's rename "byte b0" to "float rockDamage." Now it's obvious what it does. Now you have to change "(float)b0" to "rockDamage."
Tweak 2: Change the damage. Right now, it says "float rockDamage = 0"; let's change it to 2 so as not to make it too powerful, but you can change it to whatever you want.
Tweak 3: Our rock spawns snowballpoofs on impact. Lame. Just change "snowballpoof" to "crit" for now. It won't look awesome, but it's better than snow.

Here's what our new EntityThrowingRock code looks like:
*/
public class EntityThrowingRock extends EntityThrowable
{
	public EntityThrowingRock(World par1World)
	{
		super(par1World);
	}
	public EntityThrowingRock(World par1World, EntityLivingBase par2EntityLivingBase)
	{
		super(par1World, par2EntityLivingBase);
		// TODO Auto-generated constructor stub
	}
	public EntityThrowingRock(World par1World, double par2, double par4, double par6)
	{
		super(par1World, par2, par4, par6);
		// TODO Auto-generated constructor stub
	}
	/**
	 * Called when this EntityThrowable hits a block or entity.
	 */
	@Override
	protected void onImpact(MovingObjectPosition par1MovingObjectPosition)
	{
		if (par1MovingObjectPosition.entityHit != null)
		{
			float rockDamage = 2;
			par1MovingObjectPosition.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), rockDamage);
		}
		for (int l = 0; l < 4; ++l)
		{
			this.worldObj.spawnParticle("crit", this.posX, this.posY, this.posZ, 0.0D, 0.0D, 0.0D);
		}
		if (!this.worldObj.isRemote)
		{
			this.setDead();
		}
	}
}
/*
Note the "@Override" before our onImpact function - it's not critical here, as EntityThrowable.onImpact()
doesn't have anything in it, but it's good practice to put this here; if the method name or signature ever
changes in a Minecraft update, you will know immediately because your old method will give you an error.
This means you need to find the new method signature and change your method name - simply removing @Override
will get rid of the error, too, but then your method will never be called!
*/

/**
 * Step 3: Registering your custom entity and renderer.
 */
/*
We've set up our custom Entity class, but Minecraft doesn't know about it yet. So we have to let it know in using
EntityRegistry.registerModEntity in our main mod load method and tell it what renderer to use using
RenderingRegistry.registerEntityRenderingHandler in our ClientProxy. Here's how:
 */
@EventHandler
public void preInit(FMLPreInitializationEvent event)
{
	// Config, Blocks, Items go here:
	throwingRock = new ItemThrowingRock().setUnlocalizedName("throwingRock");
	
	
	// I like using an incrementable index to set my IDs rather than writing 1, 2, 3, etc., so I never have
	// to worry about order or if I missed a number (doesn't really matter though)
	int modEntityID = 0;

	// If you have a lot of Entities to register, consider creating a class with a static 'initEntity' method
	// so your main class stays tidy and readable
	EntityRegistry.registerModEntity(EntityThrowingRock.class, "Throwing Rock", ++modEntityID, this, 64, 10, true);
	
	// Now that we've registered the entity, tell the proxy to register the renderer
	proxy.registerRenderers();
}
// Now to the ClientProxy:
public class ClientProxy extends CommonProxy
{
	@Override
	public void registerRenderers()
	{
		RenderingRegistry.registerEntityRenderingHandler(EntityThrowingRock.class, new RenderSnowball(YourModName.throwingRock));
	}
}
/*
registerModEntity(...) takes the class, a name (just make it up here), a unique ID,
the instance of YourMod, tracking range, frequency of tracking updates, and whether
to send velocity information.

RenderSnowball(...) requires the item to be rendered as an argument; since we haven't
declared any items locally, we use the declaration from the instance of our mod.

NOTE: See this thread http://www.minecraftforum.net/topic/1417041-mod-entity-problem-updated-with-forge/page__st__140#entry18822284
for an explanation of Mod Entity vs. Global Entity IDs.

NOTE: A good reference to decide what tracking update frequency to use:
	https://docs.google.com/spreadsheet/pub?key=0Ap8gssssFFPAdFRXREZGSzZRY3k1WE8wcUE4S09xWXc&single=true&gid=0&output=html
(from the post mentioned in the previous note). For projectiles, ranges of 10-20 are the norm.

NOTE: If you want your custom item to render differently from a vanilla item, you will
need to make a custom render class. See the next step.

Your custom entity should now render correctly in the world! Congratulations!
*/

/**
 * Step 4: Rendering: The Power of Inheritance
 */
/*
Okay, so you've got everything working correctly, but you want your Item to exhibit
some custom behavior when it renders. We need to create a new Render class. The easiest
way to do this is to copy from an existing Renderer and paste it into your new class.
In our case, ThrowingRock behaves like Snowballs, so we will copy RenderSnowball and
rename it to RenderThrowingRock:
 */
@SideOnly(Side.CLIENT)
public class RenderThrowingRock extends Render
{
	private Item field_94151_a;
	private int field_94150_f;

	public RenderThrowingRock(Item par1Item, int par2)
	{
		this.field_94151_a = par1Item;
		this.field_94150_f = par2;
	}

	public RenderThrowingRock(Item par1Item)
	{
		this(par1Item, 0);
	}

	public void doRender(Entity par1Entity, double par2, double par4, double par6, float par8, float par9)
	{
		Icon icon = this.field_94151_a.getIconFromDamage(this.field_94150_f);

		if (icon != null)
		{
			GL11.glPushMatrix();
			GL11.glTranslatef((float)par2, (float)par4, (float)par6);
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
			GL11.glScalef(0.5F, 0.5F, 0.5F);
			
			// this.func_110777_b(par1Entity); // worked in Forge 804, but no longer; use this:
                	 this.bindEntityTexture(par1Entity);

			Tessellator tessellator = Tessellator.instance;

			if (icon == ItemPotion.func_94589_d("bottle_splash"))
			{
				int i = PotionHelper.func_77915_a(((EntityPotion)par1Entity).getPotionDamage(), false);
				float f2 = (float)(i >> 16 & 255) / 255.0F;
				float f3 = (float)(i >> 8 & 255) / 255.0F;
				float f4 = (float)(i & 255) / 255.0F;
				GL11.glColor3f(f2, f3, f4);
				GL11.glPushMatrix();
				this.func_77026_a(tessellator, ItemPotion.func_94589_d("overlay"));
				GL11.glPopMatrix();
				GL11.glColor3f(1.0F, 1.0F, 1.0F);
			}

			this.func_77026_a(tessellator, icon);
			GL11.glDisable(GL12.GL_RESCALE_NORMAL);
			GL11.glPopMatrix();
		}
	}
	
	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
        	return TextureMap.locationItemsTexture;
	}


	private void func_77026_a(Tessellator par1Tessellator, Icon par2Icon)
	{
		float f = par2Icon.getMinU();
		float f1 = par2Icon.getMaxU();
		float f2 = par2Icon.getMinV();
		float f3 = par2Icon.getMaxV();
		float f4 = 1.0F;
		float f5 = 0.5F;
		float f6 = 0.25F;
		GL11.glRotatef(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
		par1Tessellator.startDrawingQuads();
		par1Tessellator.setNormal(0.0F, 1.0F, 0.0F);
		par1Tessellator.addVertexWithUV((double)(0.0F - f5), (double)(0.0F - f6), 0.0D, (double)f, (double)f3);
		par1Tessellator.addVertexWithUV((double)(f4 - f5), (double)(0.0F - f6), 0.0D, (double)f1, (double)f3);
		par1Tessellator.addVertexWithUV((double)(f4 - f5), (double)(f4 - f6), 0.0D, (double)f1, (double)f2);
		par1Tessellator.addVertexWithUV((double)(0.0F - f5), (double)(f4 - f6), 0.0D, (double)f, (double)f2);
		par1Tessellator.draw();
	}
}

/**
Pure copy-paste. Genius. Be sure to import everything necessary. But now I'll let you in on an even BETTER way,
that you should use whenever you can: make a new class that EXTENDS whatever class you want to emulate, rather
than copying and pasting:
 */
@SideOnly(Side.CLIENT)
public class RenderThrowingRock extends RenderSnowball
{
	public RenderThrowingRock(Item item) {
		this(item, 0);
	}
	
	public RenderThrowingRock(Item item, int par2) {
		super(item, par2);
	}
	
	// now you can override the render methods if you want
	// call super to get the original functionality, and/or add some stuff of your own
	// I'll leave that up to you to experiment with
}
/**
Wow, that's so much simpler. You barely have to do anything. Remember that for the future. Of course you don't
really have to even create a Render class for the throwing rock, as you can just pass the Item directly to
RenderSnowball when you register, but you can use this technique in many places, overriding methods to add new
functionality while getting the benefits of the old.
 */

// Now, in our ClientProxy, we need to change this:
RenderingRegistry.registerEntityRenderingHandler(EntityThrowingRock.class, new RenderSnowball(YourModName.throwingRock));

// to this:
RenderingRegistry.registerEntityRenderingHandler(EntityThrowingRock.class, new RenderThrowingRock(YourModName.throwingRock));

// Or, if not using a custom render, use RenderSnowball with your Item as the parameter:
RenderingRegistry.registerEntityRenderingHandler(EntityThrowingRock.class, new RenderSnowball(YourModName.throwingRock));

/**
 * Step 5: Rendering a Model
 */
/*
If you want to do some different rendering than that offered by RenderSnowball, for example if you want a model,
you will need to create a new class that extends Render, or one of Render's subclasses.

You will need a ResourceLocation for your texture; this takes 2 parameters: ModId and the path to your texture at
the location: "src/minecraft/assets/modid/"

You can also set it with a single parameter, the string of the path to the texture:
"mymodid:textures/entity/mytexture.png" or (mymodid + ":textures/entity/mytexture.png").

In any class that extends Render, or TileEntitySpecialRenderer, you can bind the texture simply by using
"this.bindTexture(yourResourceLocation);"

On to the class itself:
*/
@SideOnly(Side.CLIENT)
public class RenderCustomEntity extends Render
{
	// ResourceLocations are typically static and final, but that is not an absolute requirement
	private static final ResourceLocation texture = new ResourceLocation("yourmodid", "textures/entity/yourtexture.png");
	
	// if you want a model, be sure to add it here:
	private ModelBase model;
	
	public RenderCustomEntity() {
			// we could have initialized it above, but here is fine as well:
	model = new ModelCustomEntity();
	}
	
	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		// this method should return your texture, which may be different based
		// on certain characteristics of your custom entity; if that is the case,
		// you may want to make a second method that takes your class:
		return getCustomTexture((CustomEntity) entity);
	}
	
	private ResourceLocation getCustomTexture(CustomEntity entity) {
		// now you have access to your custom entity fields and methods, if any,
		// and can base the texture to return upon those
		return texture;
	}
	
	// in whatever render method you are using; this one is from Render class:
	@Override
	public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTick) {
		// again, if you need some information from your custom entity class, you can cast to your
		// custom class, either passing off to another method, or just doing it here
		// in this example, it is not necessary
		
		// if you are going to do any openGL matrix transformations, be sure to always Push and Pop
		GL11.glPushMatrix();
		
		// bind your texture:
		bindTexture(texture);
		
		// do whatever transformations you need, then render
		
		// typically you will at least want to translate for x/y/z position:
		GL11.glTranslated(x, y, z);
		
		// if you are using a model, you can do so like this:
		model.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
		
		// note all the values are 0 except the final argument, which is scale
		// vanilla Minecraft almost excusively uses 0.0625F, but you can change it to whatever works
		
		GL11.glPopMatrix();
	}
}

/**
 * Step 6: HELP!!! A.k.a Common Problems and Troubleshooting.
 */
/*
First, please read ALL sections of the above tutorial carefully. Then see if you have done all of the below:

1. Read ALL sections of the tutorial carefully. 
2. Make sure you have created your texture using GIMP, Paint, or whatever and saved in .png format.
3. Make sure your texture is in the correct folder; for forge, it is:
	forge/mcp/src/minecraft/assets/yourmodid/textures/items/
4. Make sure your folder "yourmodid" is all lower-case.
5. Re-read the tutorial. Did you follow ALL of the steps EXACTLY?
6. Did you follow TechGuy's tutorial (linked at the top) for setting up your main mod,
	CommonProxy and ClientProxy?
7. Is your modid the same throughout all of your code? See my tutorial on creating a ModInfo
	file to prevent common errors of this sort.

Other problems will be noted below. I will update this section if anyone has further problems that aren't addressed
elsewhere.

PROBLEM: Null Pointer Exception at line 42 of your Render class
at Icon icon = this.field_94151_a.getIconFromDamage(this.field_94150_f);

SOLUTION: Be sure to finish initializing all Items before registering the Render class, otherwise the Item's icon
will be null when the Render is registered, resulting in an NPE.

*/

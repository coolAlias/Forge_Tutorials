/**
 * Rendering a Custom Item Texture
 */
/*
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
package your.mod.package.here;
import your.mod.name.here;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.item.Item;
import net.minecraft.util.Icon;
public class BaseModItem extends Item
{
	public BaseModItem(int par1)
	{
		super(par1);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IconRegister iconRegister)//updateIcons
	{
		this.itemIcon = iconRegister.registerIcon("yourmodid:" + this.getUnlocalizedName().substring(5));
		System.out.println("[YOUR MOD] Registering Icon: yourmodid:" + this.getUnlocalizedName().substring(5);
	}
}
/*
This allows us to avoid re-writing the registerIcon function in every single new
item we make. Nifty. Now here's what ItemThrowingRock will look like.
 */
package your.mod.package.here;
import net.minecraft.creativetab.CreativeTabs;

public class ItemThrowingRock extends BaseModItem
{
	public ItemThrowingRock(int par1)
	{
		super(par1);
		this.maxStackSize = 18;
		this.setCreativeTab(CreativeTabs.tabCombat);
	}
}
/*
We set the maxStackSize and CreativeTab in the constructor here, but you could also
do it when you declare the item in your main mod instance:
*/

public static final Item throwingRock = new ItemThrowingRock(5030).setUnlocalizedName("throwingRock").setMaxStackSize(64).setCreativeTab(CreativeTabs.tabCombat);

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
package net.minecraft.entity.projectile;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

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
Note the "@Override" before our onImpact function - it's not critical here, as
EntityThrowable.onImpact() doesn't have anything in it, but it's good practice
to put this here if you're using the same name as one of the parent functions to
ensure your method is the one called.
*/

/**
 * Step 3: Registering your custom entity and renderer.
 */
/*
We've set up our custom Entity class, but Minecraft doesn't know about it yet. So we have to let it know in using
EntityRegistry.registerModEntity in our main mod load method and tell it what renderer to use using
RenderingRegistry.registerEntityRenderingHandler in our ClientProxy. Here's how:
 */
// First in your main mod class:
// I like using an incrementable index to set my IDs rather than writing 1, 2, 3, etc., so I never have
// to worry about order or if I missed a number (doesn't really matter though)
private static int modEntityID = 0;

@EventHandler
public void load(FMLInitializationEvent event)
{
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
 * Step 3.1: A Note about ResourceLocation
 */
/*
Although you most likely won't be using these for rendering thrown entities, I think this deserves mention.
For Minecraft 1.6.2, textures are no longer bound, but set using Resource Location. If you are rendering anything
that doesn't use an Item Icon, you will need to use this to get your custom texture showing up.

ResourceLocation takes 2 parameters: ModId and the path to your texture at the location:
"src/minecraft/assets/modid/"

You can also set it with a single parameter, the string of the path to the texture:
"mymodid:textures/entity/mytexture.png" or (mymodid + ":textures/entity/mytexture.png").
*/
ResourceLocation iconLocation = new ResourceLocation("yourmodname", "textures/entity/yourtexture.png");

// For Tile Entity Special Renders, the resource location is 'bound' using this code:
// this.func_110628_a(iconLocation); // Forge 804
// now (Forge 871) uses bindTexture:
this.bindTexture(iconLocation);

// For Gui's, the resource location is 'bound' using this code:
// this.mc.func_110434_K().func_110577_a(iconLocation); // Forge 804
// As of Forge 871 at the latest, you can use renderEngine and bindTexture:
this.mc.renderEngine.bindTexture(iconLocation)

/**
 * Step 4: Creating a custom Render, e.g. RenderThrowingRock
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
/*
Pure copy-paste. Genius. Be sure to import everything necessary.

Now, in our ClientProxy, we need to change the Render class to which you registered
your item:

// Change this:
RenderingRegistry.registerEntityRenderingHandler(EntityThrowingRock.class, new RenderSnowball(YourModName.throwingRock));
// to this:
RenderingRegistry.registerEntityRenderingHandler(EntityThrowingRock.class, new RenderThrowingRock(YourModName.throwingRock));

Now all you need to do is play around with the variables and see what you can get it to do. 
*/

/**
 * Step 5: HELP!!! A.k.a Common Problems and Troubleshooting.
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
*/

package armortutorial;

import java.util.logging.Logger;

import net.minecraft.item.EnumArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.EnumHelper;
import armortutorial.item.ItemCustomArmor;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(modid = ArmorTutorial.MODID, name = "Armor Tutorial", version = ArmorTutorial.VERSION)
public class ArmorTutorial
{
	public static final String MODID = "armortutorial";
	public static final String VERSION = "1.0";

	@Instance(MODID)
	public static ArmorTutorial instance;

	@SidedProxy(clientSide = MODID + ".ClientProxy", serverSide = MODID + ".CommonProxy")
	public static CommonProxy proxy;

	public static Logger logger = Logger.getLogger("ARMOR TUTORIAL");

	// Creating a new armor material is done using EnumHelper
	// Creating a custom cloth armor material is necessary because using vanilla CLOTH
	// expects an overlay layer, without which your game will crash :\
	// 1.7.2: EnumArmorMaterial renamed to ArmorMaterial, otherwise exactly the same
	public static final EnumArmorMaterial CLOTH_CUSTOM = EnumHelper.addArmorMaterial("Cloth", 5, new int[]{1, 3, 2, 1}, 15);
	
	/** Example armor with a model: a skirt */
	public static Item sampleArmor;
	/** A set of leather bandit / cowboy armor */
	public static Item banditHelm, banditChest, banditLegs, banditBoots;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		// I'm using this id as an expedient for the tutorial; if you are coding in 1.6.4,
		// you should at least get the starting item id index from config, or provide
		// individual config settings for each item id
		// 1.7.2: Don't need to worry about IDs! Hooray!
		int id = 17500;

		// this value seems to be unimportant when using custom textures and/or models
		// but we'll get a value unique from vanilla render indices anyway:
		int renderIndex = proxy.addArmor("custom_armor");

		// note that the unlocalized name format may be very important for textures,
		// depending how you set it up; in this example, it is a key point for simplifying
		// texture registration. See {@link ItemCustomArmor} for more information.
		// 1.7.2: Remove the "id++" from the constructors
		sampleArmor = new ItemCustomArmor(id++, CLOTH_CUSTOM, renderIndex, 2).setUnlocalizedName("armor_sample_legs");
		GameRegistry.registerItem(sampleArmor, sampleArmor.getUnlocalizedName().substring(5));

		// Here we have a full armor set, using the SAME class as the other armor!
		// This is how proper Object-Oriented Programming works - there is usually no
		// need to create a new class for each armor piece or even set that you add.
		// Because we are using the unlocalized name to register textures, return armor texture,
		// etc., we are able to easily add many armors with one class and no special cases.
		banditHelm = new ItemCustomArmor(id++, CLOTH_CUSTOM, renderIndex, 0).setUnlocalizedName("armor_bandit_helm");
		banditChest = new ItemCustomArmor(id++, CLOTH_CUSTOM, renderIndex, 1).setUnlocalizedName("armor_bandit_chest");
		banditLegs = new ItemCustomArmor(id++, CLOTH_CUSTOM, renderIndex, 2).setUnlocalizedName("armor_bandit_legs");
		banditBoots = new ItemCustomArmor(id++, CLOTH_CUSTOM, renderIndex, 3).setUnlocalizedName("armor_bandit_boots");
		GameRegistry.registerItem(banditHelm, banditHelm.getUnlocalizedName().substring(5));
		GameRegistry.registerItem(banditChest, banditChest.getUnlocalizedName().substring(5));
		GameRegistry.registerItem(banditLegs, banditLegs.getUnlocalizedName().substring(5));
		GameRegistry.registerItem(banditBoots, banditBoots.getUnlocalizedName().substring(5));

		// Be sure to register any special renderers/models you may have
		// We need to do this after the Items are initialized, since we will
		// be using each Item with a model as a key in a map.
		proxy.registerRenderers();

		// Add recipes after all blocks and items have been initialized
		// otherwise, you may get a null pointer if you try to use one before it exists
		// Note that "new Object[]{...}" is not necessary, just list everything directly:
		GameRegistry.addRecipe(new ItemStack(banditHelm), "RRR", "C C",'R', Item.leather, 'C', Item.silk);
		GameRegistry.addRecipe(new ItemStack(banditChest), "R R", "CRC","CCC", 'R', Item.leather, 'C', Item.silk);
		GameRegistry.addRecipe(new ItemStack(banditLegs), "RRR", "C C","C C", 'R', Item.leather, 'C', Item.silk);
		GameRegistry.addRecipe(new ItemStack(banditBoots), "C C", "R R", 'R', Item.leather, 'C', Item.silk);
	}
}

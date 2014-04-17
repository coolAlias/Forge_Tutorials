package armortutorial.item;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumArmorMaterial;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import armortutorial.ArmorTutorial;
import armortutorial.ClientProxy;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * Armors that only need to specify a material and texture can ALL use one class.
 * 
 * The texture can be returned based on the unlocalized name; in this example, I
 * specify that each armor name should be suffixed by "_type", where "type" is
 * "helm", "chest", "legs", or "boots".
 * 
 * There is no need to make separate classes for each armor piece or even set unless
 * the armor set is very unique and requires special handling.
 *
 */
public class ItemCustomArmor extends ItemArmor
{
	/**
	 * Armor types as used on player: 0 boots, 1 legs, 2 chest, 3 helm
	 * Armor types as used in armor class: 0 helm, 1 chest, 2 legs, 3 boots
	 */
	public ItemCustomArmor(int id, EnumArmorMaterial material, int renderIndex, int type) {
		super(id, material, renderIndex, type);
		// TODO if you want different armors to have different tabs, set the creative tab
		// during item initialization to override the one set here
		// it's not a bad idea to have a default one, though, just in case you forget
		setCreativeTab(CreativeTabs.tabCombat);
	}

	@Override
	public void onArmorTickUpdate(World world, EntityPlayer player, ItemStack stack) {
		// this can be used to add a variety of effects while the armor is worn
		// but since this is supposed to be a generic armor class for all kinds
		// of armor, we will not be using it
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ModelBiped getArmorModel(EntityLivingBase entity, ItemStack stack, int armorSlot) {
		// adding all the armor models to a Map allows this method to be handled cleanly
		// if the Item is not in the map, get(this) returns null, which is perfect, since
		// returning null defaults to using the vanilla armor model
		return ClientProxy.armorModels.get(this);
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, int slot, int layer) {
		// Assuming all armors are named as "armor_name_chest", "armor_name_legs", etc.
		// then using the following format, the armor texture files should be named
		// "armor_name_layer_1" and "armor_name_layer_2"; layer 2 is for the legs
		String name = getUnlocalizedName().substring(5, getUnlocalizedName().lastIndexOf("_"));
		return String.format("%s:textures/armor/%s_layer_%d.png", ArmorTutorial.MODID, name, (slot == 2 ? 2 : 1));
		// If you are not familiar with String.format, the above statement is equivalent to:
		/*
		if (slot == 2) {
			return ArmorTutorial.MODID + ":textures/armor/" + name + "_layer_2.png";
		} else {
			return ArmorTutorial.MODID + ":textures/armor/" + name + "_layer_1.png";
		}
		
		// which could also be written as:
		
		return ArmorTutorial.MODID + ":textures/armor/" + name + (slot == 2 ? "_layer_2.png" : "_layer_1.png");
		 */
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register) {
		itemIcon = register.registerIcon(ArmorTutorial.MODID + ":" + getUnlocalizedName().substring(5));
	}
}

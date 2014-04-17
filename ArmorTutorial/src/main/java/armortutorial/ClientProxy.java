package armortutorial;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import armortutorial.client.model.ModelSkirt;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.item.Item;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy
{
	/** A Map allows for easy handling of many armor models */
	public static final Map<Item, ModelBiped> armorModels = new HashMap<Item, ModelBiped>();

	@Override
	public void registerRenderers() {
		// Model classes are all client-side only, so we must register them on the client side
		// Be sure to add a model for each Armor Item that requires one:
		addArmorModel(ArmorTutorial.sampleArmor, new ModelSkirt());
	}

	@Override
	public int addArmor(String armor) {
		return RenderingRegistry.addNewArmourRendererPrefix(armor);
	}

	/**
	 * Adds a mapping for an ItemArmor to a ModelBase for rendering
	 * @param armor The armor Item
	 * @param model The model should not be null and must extend ModelBiped
	 */
	private void addArmorModel(Item armor, ModelBiped model) {
		if (model == null) {
			// technically, you CAN add a null model, but the default is already to return null, so it would be redundant
			ArmorTutorial.logger.log(Level.WARNING, String.format("Error adding model for %s: Cannot add a NULL armor model!", armor.getUnlocalizedName()));
			return;
		}
		// better let yourself / users know when overwriting an entry, as it is likely to be a mistake!
		if (armorModels.containsKey(armor)) {
			ArmorTutorial.logger.log(Level.WARNING, String.format("A model for %s has already been registered! It will now be overwritten.", armor.getUnlocalizedName()));
		}
		armorModels.put(armor, model);
	}
}

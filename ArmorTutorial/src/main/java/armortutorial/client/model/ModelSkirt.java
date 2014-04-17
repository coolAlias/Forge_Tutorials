package armortutorial.client.model;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

/**
 * 
 * This model is a cute skirt, done by RazzleberryFox.
 * 
 * Unfortunately, the rotation angles are slightly off, so the
 * skirt renders at an angle rather than straight up and down,
 * and I don't feel like messing with it at the moment, but it
 * suffices to demonstrate the main points.
 *
 */
public class ModelSkirt extends ModelBiped
{
	/** An array of all the skirt model parts */
	private ModelRenderer[] parts;

	/**
	 * Since I'm too lazy to try and derive a formula from these, I've
	 * decided to put them in an array instead. The y rotation is the
	 * only value that differs between each shape, so we can use this
	 * to make an array of shapes, which is compact and handy to use.
	 */
	private static final float[] ry = {
		0.122173F,
		-3.089233F,
		0.7853982F,
		-2.443461F,
		1.413717F,
		-1.815142F,
		2.042035F,
		-1.186824F,
		2.670354F,
		-0.5585054F
	};

	public ModelSkirt()
	{
		textureWidth = 64;
		textureHeight = 32;
		
		// assign a new array based on the number of parts we should have
		// i.e. one for each y rotation
		parts = new ModelRenderer[ry.length];
		for (int i = 0; i < parts.length; ++i) {
			parts[i] = new ModelRenderer(this, 56, 16);
			// The final parameter of addBox is scale:
			// typical armor uses 1.0F, boots use 0.5F, but it can be anything you need
			// In this case, the skirt is a bit small, so I've scaled it to 1.8F
			parts[i].addBox(0.1F, 0F, -2.1F, 2, 6, 2, 1.8F);
			parts[i].setRotationPoint(0F, 10F, 0F);
			parts[i].setTextureSize(64, 32);
			parts[i].mirror = true;
			
			// and here we use our rotation y array:
			setRotation(parts[i], -0.4363323F, ry[i], -0.4363323F);
		}
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		super.render(entity, f, f1, f2, f3, f4, f5);
		setRotationAngles(f, f1, f2, f3, f4, f5, entity);
		// see how nice arrays are?
		for (int i = 0; i < parts.length; ++i) {
			parts[i].render(f5);
		}
	}

	@Override
	public void setRotationAngles(float par1, float par2, float par3, float par4, float par5, float par6, Entity entity) {
		super.setRotationAngles(par1, par2, par3, par4, par5, par6, entity);
		// TODO set rotation angles of model based on riding, sneaking, etc.
		// See {@link ModelBiped} for examples
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}

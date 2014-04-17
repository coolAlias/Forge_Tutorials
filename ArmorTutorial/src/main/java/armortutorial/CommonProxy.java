package armortutorial;

public class CommonProxy
{
	public void registerRenderers() {}

	// this just lets us override the method in the ClientProxy,
	// since the RenderingRegistry only needs to be done client-side
	public int addArmor(String string) {
		return 0;
	}
}

/** Error Prevention: Creating a ModInfo file */

/*
This has been covered in other tutorials, but I feel it's important enough to mention again.

I always make a ModInfo class that defines my mod variables such as my mod id then I reference that instead of
hard-coding the ID everwhere.

Not only does this prevent me from making typos, but it also allows me to change my mod id, mod name or other
variable in one single location and all of my code will still be correct.

Here's how you can make one for yourself:
*/
@Mod(modid = ModInfo.ID, name = ModInfo.NAME, version = ModInfo.VERSION)
@NetworkMod(clientSideRequired=true, serverSideRequired=false,
channels = {ModInfo.CHANNEL}, packetHanlder = ALPacketHandler.class)

public final class ArcaneLegacy
{
  @Instance(ModInfo.ID)
  public static ArcaneLegacy instance;

  // rest of main class here

}

// And ModInfo would look like this (in a separate class, not in the main mod):

public class ModInfo
{
  public static final String ID = "coolaliasarcanelegacy";
  public static final String NAME = "Arcane Legacy";
  public static final String VERSION = "0.1.0";
	public static final String CLIENT_PROXY = "coolalias.arcanelegacy.client.ClientProxy";
	public static final String COMMON_PROXY = "coolalias.arcanelegacy.common.CommonProxy";
  public static final String CHANNEL = "ChannelCAAL";
}

/*
You can of course add any other information related to your mod here as well, such as CHANNELS for your packet handler.
Whenever you would put "modid" in your code, change it to ModInfo.ID, such as in the Item method registerIcons:
*/

@Override
@SideOnly(Side.CLIENT)
public void registerIcons(IconRegister iconRegister)
{
  this.itemIcon = iconRegister.registerIcon(ModInfo.ID + ":" + this.getUnlocalizedName().substring(5));
}

/*
If you ever change your mod id, now you only need to change it in one place, ModInfo, and ALL of your code will still
be 100% correct. Also you won't ever have to worry about typos  

*/

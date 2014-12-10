MODDING WITH APIs

In this tutorial, I will cover how to install an API in a manner that does not require you to place the entire API directly in your project directory, how to build and package your mod independently from the API so that you are not distributing other people's mods with your own, and how to utilize an API such that your mod will still function without it, but will still be able to take advantage of the extra features when it is present.

I struggled with this myself while working on my own mod, but thanks to GotoLink's guidance and extreme patience I finally got everything working, and I figure the things I learned will be greatly beneficial to anyone else looking to hook into another mod's API.

Before we start, do note that this tutorial assumes that you are already capable of writing your own mod; I will not be providing any support here for the basics of setting up a mod. Also, this tutorial assumes that you are using the latest versions of Forge, meaning that you are also using Gradle, as the vast majority of mods with APIs all use Forge.

If you would like to follow along with the tutorial step-by-step, you can download the API that I will be using here:
https://github.com/coolAlias/ZeldaSwordSkills-1.6.4/releases

Step 1: Installing the API

1. Assuming you already have your workspace set up, create a new folder named "/libs" in your project directory. That's the directory that contains your /bin, /build, /gradle, and /src folders, and now also has a /libs folder.

2. Ask the mod author(s) for a binary distributable / source file of their mod and place it in the /libs folder you just created.

3. In Eclipse, right-click on your project, select "Build Path" and then "Configure Build Path". Click the "Libraries" tab, then "Add External JARs" and find the binary file that you just placed in the /libs folder.

4. If the API author provided a source file, open the "Referenced Libraries" tree in your package explorer, find the API binary that you just referenced and right-click on it; go to "Properties" -> "External location" -> "External file" and navigate to wherever you stored the source file, preferably right next to the binary in the /libs folder.

5. Run your debug configuration and see if everything is still working; if so, great! If not, you may need to run setupDev and setupDecomp workspace one more time:

 a. gradlew setupDevWorkspace
 b. gradlew setupDecompWorkspace
 c. gradlew eclipse // will vary depending on your IDE

Once you have the debug client working, you should see that both your mod and the API are loaded; if you start a new game, anything added by the API mod will be in that game as well and should be fully functional. This is extremely handy while developing an addon or simply testing compatibility between mods.

You are now ready to start using the API in your mod!

NOTE: Not all APIs will be able to use this setup; core mods, for example, can be tricky to work with due to their access transformers needing to modify the vanilla jar before being usable.

Let's take Battlegear2's API as an example.

1. For BG2, the binary distributable should be placed not in the project directory, but wherever you have designated as the working directory in a "/mods" folder. For most people using Eclipse, this will be your "projectDirectory/eclipse/mods", or if you followed Lex's multi-project workspace video, in "workspaceLocation/run/mods", though I recommend pointing your API-dependent mod's workspace at its own local eclipse directory instead of the /run directory so you don't have to perform all of the following steps for every single mod in your workspace.

2. Then, follow steps 3, 4, and 5 above, run the debug client, start a game and try to open the inventory. The game should crash at this point with an Illegal Access Error - that's the access transformer failing.

3. Find the "battlegear_at.cfg" file, place that in your resources directory and re-run all of the commands from step 5, then try again.

4. If it crashes again, check in your /build directory for a "deobfuscated-bin.jar" file - this is the modified Minecraft jar - and change your library reference from "forgeSrc..." to that file instead. If you don't see that file, you may need to close Eclipse and run the commands again with "--refresh-dependencies". Note that in 1.7.2, I have never found this file and it still seems to work, but in 1.6.4 it seems to be required. Your mileage may vary.

5. At this point it should work, but if it doesn't, make sure that you are using EXACTLY the same version of Forge for which the core mod was written, because if any of the fields it tries to modify have different names, the process will fail and you will most likely get an exception when attempting to access that field.

It can be very frustrating and time-consuming working with core mod APIs, sometimes requiring those same steps to be repeated over and over again even after you have successfully set it up once when, for example, you clean your project or otherwise re-reference the Forge Minecraft jar instead of the one modified by the access transformers.

Step 2: Implementing an API Interface

Our first order of business will be creating a new Item that can pick up blocks using Zelda Sword Skills' ILiftBlock interface. Simply create a new Item class and implement ILiftBlock. You should be able to import it right away, but you may need to explicitly tell Eclipse where to look, or fix your project setup if you forgot to add the API as a referenced library. Be sure to link the API source if you have it so you get readable names as method parameters, rather than arg0, d1, etc., and then let Eclipse add the unimplemented methods for you.

[code]
public class ItemLifter extends Item implements ILiftBlock {

	public ItemLifter(int id) {
		super(id);
	}

	@Override
	public BlockWeight getLiftStrength(EntityPlayer player, ItemStack stack, Block block, int meta) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ItemStack onLiftBlock(EntityPlayer player, ItemStack stack, Block block, int meta) {
		// TODO Auto-generated method stub
		return null;
	}
}
[/code]
I'll let you handle the basics of the Item and focus on the API methods. There are just two methods to implement for this interface, and the first one requires another API class, BlockWeight, which should already have been imported. Just type in BlockWeight followed by a period to see a list of options; we'll choose "EXTREME_II" just for fun, even though there aren't any blocks in that category (yet).

The second method asks us to return an ItemStack, and the java-docs explain that this is the stack that will be returned to the player when the block is placed. We want to get our item back, so we will return "stack" instead of "null", but first we will damage the stack by 1 so that it will eventually break. Now our methods look like this:

[code]
@Override
public BlockWeight getLiftStrength(EntityPlayer player, ItemStack stack, Block block, int meta) {
return BlockWeight.EXTREME_II;
}

@Override
public ItemStack onLiftBlock(EntityPlayer player, ItemStack stack, Block block, int meta) {
stack.damageItem(1, player);
return stack;
}
[/code]

Go ahead and start up the client in Eclipse; you should have an item that picks up any solid block that you right-click on, and returns the same item but slightly damaged when you place the block down.

Now, if you were to build the mod and attempt to play it in Minecraft without Zelda Sword Skills installed, your game will crash with a No Class Definition Found error, since we are trying to access several API classes that are not present. So, before we build the mod, we will first add some code that will strip the API interface and methods if the required mod classes are not present.

Step 3: Stripping Interfaces

Luckily for us, FML is capable of stripping interfaces by using cpw's awesome @Optional.Interface annotation.

There are 3 fields we need to fill out: iface, modid, and striprefs:

1. "iface" is the complete package path of the interface we wish to strip, so it is best to simply copy the import path and paste that in to avoid any typos. If you get the path incorrect, it will not work.

2. "modid" is obviously the modid of the mod that owns the interface to strip. Again, be sure to spell it correctly.

3. "striprefs" set this to true to strip interface and method references that are not found. I don't know why anyone would ever set this to false, but I'm sure there are uses for that as well.

Add the annotation above the class declaration:
[code]
@Optional.Interface(iface="zeldaswordskills.api.item.ILiftBlock", modid="zeldaswordskills", striprefs=true)
public class ItemLifter extends Item implements ILiftBlock {
[/code]

Make sure you import "cpw.mods.fml.common.Optional" and not the google Optional class.

One last thing we should do is strip the API methods from the class, though the mod will probably still work just fine even if you do not. It's just one simple line: @Method(modid="apiModId"), and you absolutely should use it if the API method in question is not part of an interface that you are stripping.

[code]
// put this same line above all API methods:
@Method(modid="zeldaswordskills")
@Override
public BlockWeight getLiftStrength(EntityPlayer player, ItemStack stack, Block block, int meta) {
return BlockWeight.EXTREME_II;
}
[/code]

Step 4: Load Order

Once you have your API-utilizing mod all working in the debug environment, there are a few things that should be done before building and packaging the final product. The first is to make sure your mod will load after the mod whose API you are using; this is done in the mcmod.info file. Let's take a look at some of the available fields:

1. "requiredMods": [ "Forge", "someOtherMod" ],
Any mods you list here will be required for your mod to load; your mod cannot load without them. Since we want our mod to be functional even if the other mod is not present, we will not be using this field here, but it is very useful if your mod requires some other mod's functionality in order to be usable.

2. "dependencies": [ "zeldaswordskills" ],
Any mods that your mod is dependent upon will be loaded before yours; this is very important if you need to know that a mod is present or not during the pre-initialization stages, and is generally a good idea to put any mod that your mod might rely upon as a dependency, even if you do not need to do anything with it in the early stages of mod loading.

3. "dependants": [ "ModSubmodule" ]
Any mods listed here are mods that depend upon and will be loaded after your mod; useful if you make a submodule of your own mod. Note that the submodule should list the parent mod as a dependency and possibly required mod.

4. "useDependencyInformation": "true"
You MUST set this to "true" or the above three fields will be meaningless as far as mod order is concerned. If you omit this field, chances are someone will crash when your mod is installed alongside the dependency, even if you don't. This is because if our mod loads before the api, when we try to access the api methods, they will not yet exist.

You can learn all about how FML uses the mcmod.info file and other things that you can do with it on the wiki:
https://github.com/MinecraftForge/FML/wiki/FML-mod-information-file

For this tutorial, we will only add the "dependencies" and "useDependencyInformation" lines, ensuring that the API we want to use is loaded first if present.
[code]
"dependencies": ["zeldaswordskills"],
"useDependencyInformation": "true"
[/code]

For many APIs, assigning dependencies in mcmod.info should be enough to guarantee load order. This is the case for any API that does not require access during the pre-initialization stages of a mod, for example if you were using a structure generation or mob animation API. Before using any methods or classes from such APIs, it would be sufficient simply to check if the mod is loaded:

[code]
if (Loader.isModLoaded("api_modid")) {
// do something with the API
}

// If I need to access this frequently, I typically store the mod loaded state during pre-initialization:
public static boolean isWhateverApiLoaded;

@EventHandler
public void preInit(FMLPreInitializationEvent event) {
isWhateverApiLoaded = Loader.isModLoaded("whateverAPI");
// note that this only means that the mod has been recognized, not necessarily that it is fully
// initialized and ready to go yet; you should NOT try to use anything to do with the API
// until the FMLInitializationEvent at the earliest, even with dependency information sorting
// the load order, just to be safe
}
[/code]

However, since we require the API to be loaded in time for our new Item, we need access to a fully-functional API during our mod's pre-initialization event. The only way to load an API prior to pre-init is if the author included API markers for each of the API packages:
[code]
// filename is "package-info.java" for every one of these files
@API(owner = "zeldaswordskills", provides = "ZeldaAPI", apiVersion = "0.1")
package zeldaswordskills.api;

import cpw.mods.fml.common.API;
[/code]
There should be one of these files in every package that provides API features; if not, you will need to contact the API author and ask them to provide these files, or your mod is quite likely to crash if not for you, then for the majority of people using your mod. Remember, these markers are ONLY needed if you require the API during pre-initialization, such as for implementing specific interfaces in your Items or Blocks, so don't trouble the author if the API does not truly require them.

Step 5: Building the Mod

The final step is of course building the mod. Since our mod is dependent upon an API, we need to let the compiler know where to find that code during the build process or we will get lots of Class Not Found and similar errors.

To do so, simply add any dependencies to a "compile files()" method in the build.gradle file, using the full path relative to your project directory and separating each file path with a comma. Note that you can use "../" to move up a folder if, for example, you have a dependency in the working directory instead of the project directory.

"libs/zeldaswordskills-1.6.4-0.6.3.jar" -> located in /workingDirectory/projectDirectory/libs/

"../run/mods/zeldaswordskills-1.6.4-0.6.3.jar" -> located in /workingDirectory/run/mods/

[code]
version = "1.0"
group= "com.google.coolalias008.modwithapi"
archivesBaseName = "modwithapi"

dependencies {
	compile files (
		"libs/zeldaswordskills-1.6.4-0.6.3.jar"
	)
}
[/code]

Once the build file is saved, open up a command console and run "gradlew build"; it should compile with no errors, but if it doesn't, double-check the file paths and make sure nothing is misspelled.

Time to load it up in Minecraft and give it a go! Try first with just your mod alone and make sure that is working, then exit the Minecraft launcher completely (just to be safe), add the API-providing mod to your /mods folder, and launch once more. If you followed the tutorial and are using my ZeldaAPI, you should now have an item that can pick up any solid block, just by implementing a single interface! Pretty awesome.

Step 6: Multiple Interfaces

Alright, you can do a single interface, but what about when you want to implement several API interfaces in a single item, block, or other class? You cannot simply stack @Optionals on top of each other, but you can use an InterfaceList:
[code]
@Optional.InterfaceList(value={
	@Optional.Interface(iface="zeldaswordskills.api.block.ILiftable", modid="zeldaswordskills", striprefs=true),
	@Optional.Interface(iface="zeldaswordskills.api.block.ISmashable", modid="zeldaswordskills", striprefs=true)
})
[/code]
The individual @Optional.Interfaces are exactly the same as before, but separtated by commas and nested inside of an array, all enclosed by the @Optional.InterfaceList annotation.

Alright, so let's make a block that is both liftable AND smashable by implementing ILiftable and ISmashable. Import those two classes and let Eclipse add the unimplemented methods for you, and be sure to set up the rest of the Block class (texture, creative tab, etc.). I will only cover the API methods here.
[code]
@Method(modid="zeldaswordskills")
@Override
public BlockWeight getSmashWeight(EntityPlayer player, ItemStack stack, int meta) {
// let's make our block very easy to smash, since we do not have any smashing items yet, the only
// way to smash our block would be using one of the Zelda Hammers
return BlockWeight.VERY_LIGHT;
}

@Method(modid="zeldaswordskills")
@Override
public Result onSmashed(World world, EntityPlayer player, ItemStack stack, int x, int y, int z, int side) {
// for the sake of simplicity, we will just use the default smashing mechanics
return Result.DEFAULT;
}

@Method(modid="zeldaswordskills")
@Override
public BlockWeight getLiftWeight(EntityPlayer player, ItemStack stack, int meta) {
// we want our block to be extremely difficult to lift, giving our custom itemLifter a purpose;
// not even any of the items in Zelda can lift our block! mwa ha ha!
return BlockWeight.EXTREME_II;
}

@Method(modid="zeldaswordskills")
@Override
public void onLifted(World world, EntityPlayer player, ItemStack stack, int x, int y, int z, int meta) {
// if you need to handle a tile entity or do anything else before the block is lifted,
// you can do so here; however, we do not need to for our simple block
}

@Method(modid="zeldaswordskills")
@Override
public void onHeldBlockPlaced(World world, ItemStack stack, int x, int y, int z, int meta) {
// if you want to do something special when the block is placed, you can do so here,
// but we will leave it empty for this tutorial
}
[/code]
That's it for the API methods. Once the rest of the block is set up and registered, you can go ahead and give it a try! You should have a block that can only be lifted with the special itemLifter that we made earlier, but can be smashed even with just the wooden hammer from Zelda. If you run your mod by itself, you will still have the item and block in the game, but without the lifting and smashing mechanics provided by the API. This is great if your items and blocks have other functions that still make them useful independently.

You should now be able to work with pretty much any mod API in a manner that will still allow your mod to function independently should the API-providing mod not be present, as well as avoid including the API code in your mod directly. Good luck! Don't forget to give GotoLink kudos if you see him around - he likes to hang out over on MinecraftForge forums, so go bump up his karma from time to time!
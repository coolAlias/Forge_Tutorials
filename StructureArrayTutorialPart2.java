/*
STRUCTURE GENERATOR TOOL TUTORIAL PART 2: USING CUSTOM HOOKS

In this tutorial we will take a look at some of the additional features available with this
tool using the basic furnished home we created in Part 1 as the foundation.

First, we'll take a look at how to implement the StructureGenerator class, as that forms the
foundation for using custom hooks.
*/
/**
 * Step 1: Create a class extending StructureGeneratorBase
 */
/*
If you downloaded the source code, there is already a class that does this, but for the sake
of demonstration, we'll start from scratch. We'll call the new class 'MyStructureGenerator'
and just let Eclipse automatically add all the methods we need.

We're also going to need some way to store our available structures, otherwise we have to set
all of the structure variables each time we want to generate a structure. We're going to use
a List of Structure objects with a static initializer.
*/
public class MyStructureGenerator extends StructureGeneratorBase
{
    /** List storing all structures currently available */
    public static final List<Structure> structures = new LinkedList();

    public MyStructureGenerator() {
        // TODO Auto-generated constructor stub
    }

    public MyStructureGenerator(Entity entity, int[][][][] blocks) {
        super(entity, blocks);
        // TODO Auto-generated constructor stub
    }

    public MyStructureGenerator(Entity entity, int[][][][] blocks, int structureFacing) {
        super(entity, blocks, structureFacing);
        // TODO Auto-generated constructor stub
    }

    public MyStructureGenerator(Entity entity, int[][][][] blocks, int structureFacing, int offX, int offY, int offZ) {
        super(entity, blocks, structureFacing, offX, offY, offZ);
        // TODO Auto-generated constructor stub
    }

    @Override
    public int getRealBlockID(int fakeID, int customData1) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void onCustomBlockAdded(World world, int x, int y, int z, int fakeID, int customData1, int customData2) {
        // TODO Auto-generated method stub
    
    }
    
    /** This is where you add your structures to the List we made at the top */
    static {
        // A temporary object to store a Structure before adding it to the List
        Structure structure;
        
        // For each structure you need to set 'structure' to a new instance of Structure
        structure = new Structure("Tutorial Home");
        
        // Add all your structure's block arrays to the structure, starting from the bottom
        // and working up; our home, however, only has one array
        structure.addBlockArray(StructureArrayTutorial.blockArrayTutorial);
        
        // Remember to set the structure facing
        structure.setFacing(StructureGeneratorBase.WEST);
        
        // Finally, add the structure to the List
        structures.add(structure);
    }
}
/*
Now you can create a 'public static final MyStructureGenerator gen = new MyStructureGenerator()'
in your main mod class or anywhere else you like, and you will have access to your structures
from anywhere you need via the List called 'structures'.

For example, from a block's onBlockActivated method, you could generate your house like so:
*/
@Override
public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9)
{
// You only need to generate on the server
if (!world.isRemote) {
// Store the structure we want to generate in a local variable for ease of reference
// Use care when accessing your List.get(index) method or you will get null pointer exceptions
Structure structure = YourMod.gen.structures.get(1);

// Add player facing to rotation calculations so your structure faces you when generated
YourMod.gen.setPlayerFacing(player);

// Sets the structure to the structure at index '1' in the List; i.e. the Tutorial Home
YourMod.gen.setStructure(structure);

// Optionally, set your structure's offset coordinates
YourMod.gen.setDefaultOffset(structure.getOffsetX(), structure.getOffsetY(), structure.getOffsetZ());

// Now generate the structure at the x/y/z provided by the block's method parameters
YourMod.gen.generate(world, world.rand, x, y, z);
}
}
/**
 * Step 2: Setting up a Custom Hook
 */
/*
Custom hooks are all about definitions. As such, we're going to create a 'library' or reference
class that does nothing but hold our definitions, much like some do for Item IDs and names.
This way all of our custom hook types are in one place and we can easily check for conflicts.

Each custom hook id acts as a 'fake' block id and is used in lieu of a real block ID within
the block array. Instead of using 'Block.chest.blockID' we will use our custom id, returning
the real chest blockID from the 'getRealBlockID' method in MyStructureGenerator.

Since Forge uses block IDs from 0-4095, we will start our custom hook indices from 4096. Being
outside of the normal block range acts as a flag, telling the structure generator that it needs
to handle this 'block' in a special way; that is, by calling the 'onCustomBlockAdded' method.

Let's set up our CustomHooks reference class first, with a single custom id.
*/
public class CustomHooks {
    public static final int CUSTOM_CHEST = 4096;
}
/*
Simple. Now we need to make sure 'getRealBlockID' returns the correct id.
*/
@Override
public int getRealBlockID(int fakeID, int customData1)
{
    switch(fakeID) {
    case CustomHooks.CUSTOM_CHEST: return Block.chest.blockID;
    default: return 0;
    }
}
/*
Finally, we need to define what happens when CUSTOM_CHEST id is placed in the world. For this,
we use the 'onCustomBlockAdded' method. Think of it in the same way you do the vanilla methods
'onBlockAdded', 'onBlockPlacedBy' etc.
*/
@Override
public void onCustomBlockAdded(World world, int x, int y, int z, int fakeID, int customData1, int customData2)
{
    // For now we'll ignore all the other parameters and only use the fakeID, i.e. our custom hook id
    switch(fakeID) {
    case CustomHooks.CUSTOM_CHEST:
        // Using the pre-made method addItemToTileInventory adds items to the first slot available

        // Let's load our chest with goodies! We'll take advantage of addItemToTileInventory's
        // return value: true if the item was added, false if there was no more room
        boolean canAdd;
        do {
            canAdd = addItemToTileInventory(world, new ItemStack(Item.diamond, 64), x, y, z);
            if (canAdd) canAdd = addItemToTileInventory(world, new ItemStack(Item.emerald, 64), x, y, z);
        } while (canAdd);
        break;
    }
}
/*
Fire up Minecraft and generate your structure. You should now see... nothing? Why not? Well,
we didn't add our custom hook into our structure array! Open up StructureArrayTutorial and
replace 'Block.chest.blockID' with 'CustomHooks.CUSTOM_CHEST'. Try generating your structure
again. You should have lots of goodies this time.

Don't feel limited by my above implementation. You could just as well create your own lists
of WeightedRandomChestContents from which to select items, rather than hard-coding the chest
contents like I did here.
*/
/**
 * Step 3: Using CustomData1 and CustomData2
 */
/*
Ok, we've got a chest generating with stuff inside, but what if you want some variety in your
chest contents? Do you need to define a new custom hook id for every chest? No and yes. You
don't need to define a new custom hook id with the accompanying real block id; we can simply
use CUSTOM_CHEST for any custom chest. We do, however, have to define our chest somehow. I
think of it like 'subtypes' for my hook id, and I store it in customData1.

Recall that the final array in the blockArray holds up to 4 int values:
{blockID, metadata, customData1, customData2}

So far, we've totally ignored the two custom data elements. To demonstrate quickly how they
work, let's change our CUSTOM_CHEST onCustomBlockAdded code a little.
*/
@Override
public void onCustomBlockAdded(World world, int x, int y, int z, int fakeID, int customData1, int customData2)
{
    // For now we'll ignore all the other parameters and only use the fakeID, i.e. our custom hook id
    switch(fakeID) {
    case CustomHooks.CUSTOM_CHEST:
        addItemToTileInventory(world, new ItemStack(customData1, customData2, 0), x, y, z);
        break;
    }
}
/*
In this code, we are creating a new ItemStack with an itemID defined by customData1 and with
a stack size defined by customData2. Instead of a set stack size, you could randomize it by
using 'world.rand.nextInt(customData2) + 1' or whatever algorithm you want to use.

Note we also have to include the damage/metadata value, here '0', because customData1 is not
an Item, but an int, and that's just how the ItemStack constructor works.

Go back to StructureArrayTutorial and change the custom chest array to:
{CustomHooks.CUSTOM_CHEST,2,Item.appleGold.itemID,16}

Go ahead and try it out. You should be getting 16 golden apples in your chest. Using this,
you could have any number of chests all with different, specific contents, but as you can
see this method limits you to one type of item per chest.

A good alternative is to use customData1 to identify the chest, then add a number of items
defined by customData2 from a weight list like vanilla chests use. I suggest defining all
of your subtypes in the CustomHooks reference class.
*/
public class CustomHooks {
    public static final int CUSTOM_CHEST = 4096;
    
    // I use negative values here so I can still use customData1 to define itemIDs in generic CUSTOM_CHESTs
    public static final int
        CHEST_HOUSE_1 = -1, // These will define two separate sets of contents for our house chest
        CHEST_HOUSE_2 = -2;
}
/*
Add this chest to the block array at x=3, right under the other chest we have:
{CustomHooks.CUSTOM_CHEST,2,CustomHooks.CHEST_HOUSE_1}

Now there should be two chests in the block array, one that's custom and one that sets the
contents to 16 golden apples.

Note that we're not using customData2 in our sub-chest  because I don't plan on setting up
a weighted list at this point. Recall that the '2' is the metadata value of the chest block
which we  need for it to be oriented correctly in our house.

Now we just need to alter the CUSTOM_CHEST case to account for the new possibility of subtypes.
*/
@Override
public void onCustomBlockAdded(World world, int x, int y, int z, int fakeID, int customData1, int customData2)
{
    // For now we'll ignore all the other parameters and only use the fakeID, i.e. our custom hook id
    switch(fakeID) {
    case CustomHooks.CUSTOM_CHEST:
        // Depending on how many subtypes you have, you could use a switch or call another
        // method you define yourself that handles all chest cases
        if (customData1 == CustomHooks.CHEST_HOUSE_1) {
            // Let's give our custom house chest some potions! Check the Minecraft wiki
            // data values page for potion ids.
            addItemToTileInventory(world, new ItemStack(Item.potion,1,8206), x, y, z);
            addItemToTileInventory(world, new ItemStack(Item.potion,1,8270), x, y, z);
            addItemToTileInventory(world, new ItemStack(Item.potion,1,8193), x, y, z);
            addItemToTileInventory(world, new ItemStack(Item.potion,1,16385), x, y, z);
        }
        else if (customData1 == CustomHooks.CHEST_HOUSE_2) {
            // For this one, let's put in our family sword + armor, worn from years of disuse
            addItemToTileInventory(world, new ItemStack(Item.swordIron,1,128), x, y, z);
            addItemToTileInventory(world, new ItemStack(Item.plateIron,1,128), x, y, z);
            addItemToTileInventory(world, new ItemStack(Item.helmetIron,1,72), x, y, z);
            addItemToTileInventory(world, new ItemStack(Item.legsIron,1,128), x, y, z);
            addItemToTileInventory(world, new ItemStack(Item.bootsIron,1,72), x, y, z);
        }
        else {
            // We can still use customData1/2 as itemID and stack size for generic chests
            addItemToTileInventory(world, new ItemStack(customData1, customData2, 0), x, y, z);
        }
        break;
    }
}
/*
Finally, to take advantage of our second custom chest, copy and paste the blockArrayTutorial
in your StructureArrayTutorial class. Append a '1' to the first array name and a '2' to the
second, then change CUSTOM_CHEST_1 to CUSTOM_CHEST_2 in the second array. Now you have to add
the new array to your StructureGenerator static initializer:
*/
/** This is where you add your structures to the List we made at the top */
static {
    Structure structure;
    // Our first structure:
    structure = new Structure("Tutorial Home 1");
    structure.addBlockArray(StructureArrayTutorial.blockArrayTutorial1);
    structure.setFacing(StructureGeneratorBase.WEST);
    structures.add(structure);
    
    // Our second structure is almost exactly the same:
    structure = new Structure("Tutorial Home 2");
    structure.addBlockArray(StructureArrayTutorial.blockArrayTutorial2);
    structure.setFacing(StructureGeneratorBase.WEST);
    structures.add(structure);
}
/*
Now in your block or item that generates the structure, you can choose to generate either
'YourMod.gen.structures.get(1)' OR 'YourMod.gen.structures.get(2)', or you could make a new
block/item for the second structure. Generate both of them in your world and you'll see they
both have unique chest contents.
*/
/**
 * Step 4: Other Custom Hooks / A Summary
 */
/*
Now you know how to set up and use custom hooks for chests, and it's exactly the same for any
other kind of custom block you want to make. Do you want a sign with text on it? Define an id
'CUSTOM_SIGN = 4097' in your CustomHooks reference class and, if you want, define subtypes
'SIGN_1 = 1', 'SIGN_2 = 2', etc. Then all that's left is to define what happens when your
custom block is added to the world in the 'onCustomBlockAdded' method. Just add a new case
for each custom hook id.

Take a look at the source code provided on github, specifically 'StructureGenerator.java' and
'CustomHooks.java', for further examples and ideas on using Custom Hooks.

Steps Required for Creating a Custom Hook:
1. Define your custom hook id, starting from 4096
2. Define real id to return for your hook in 'getRealBlockID' method
3. Write code you want to occur when your custom block is placed
4. Optionally, define subtypes for your hook

===================
SOME USEFUL METHODS
===================
The following are methods designed to make handling onCustomBlockAdded cases easier:

1. addItemToTileInventory(World world, ItemStack itemstack, int x, int y, int z)

    Use this method to conveniently add items to any TileEntity that has an inventory
    (i.e. implements either IInventory or ISidedInventory).
    
    Items are added to the first slot available and the method returns false if the
    stack was not able to be added entirely or if there was an error.

2.1 spawnEntityInStructure(World world, Entity entity, int x, int y, int z)

    Spawns the passed in entity within the structure such that it doesn't spawn inside of
    walls by using the method setEntityInStructure below. If no valid location was found,
    the entity will still spawn but the method will return false.
    
2.2 setEntityInStructure(World world, Entity entity, int x, int y, int z)

     Sets an entity's location so that it doesn't spawn inside of walls, but doesn't spawn
     the entity. Automatically removes placeholder block at coordinates x/y/z.
     Returns false if no suitable location found so user can decide whether to spawn or not.

3. setHangingEntity(World world, ItemStack hanging, int x, int y, int z)

    Places a hanging entity in the world based on the arguments provided; orientation
    will be determined automatically based on the dummy blocks data, so a WALL_MOUNTED
    block id (such as Block.torchWood.blockID) must be returned from getRealBlockID().
    
    This method returns the direction in which the entity faces for use with the methods
    'setItemFrameStack' and 'setPaintingArt'. It is not needed for wall signs.

4. setItemFrameStack(World world, ItemStack itemstack, int x, int y, int z, int direction,
    int itemRotation)

    Finds the correct ItemFrame in the world for the coordinates and direction given and
    places the itemstack inside with the rotation provided, or default if no itemRotation
    value is given. 'direction' parameter is value returned from setHangingEntity

5. setPaintingArt(World world, String name, int x, int y, int z, int direction)

    Sets the art for a painting at location x/y/z and sends a packet to update players.
    'direction' parameter is value returned from setHangingEntity
    Returns false if 'name' didn't match any EnumArt values.

6. setSignText(World world, String[] text, int x, int y, int z)

    Adds the provided text to a sign tile entity at the provided coordinates, or returns
    false if no TileEntitySign was found. String[] must be manually set for each sign, as
    there is currently no way to store this information within the block array.

7.1 setSkullData(World world, String name, int type, int x, int y, int z)

    Sets the skull type and player username (if you can get one) for the tile entity at
    the provided coordinates. Returns false if no TileEntitySkull was found.
    
7.2 setSkullData(World world, String name, int type, int rot, int x, int y, int z)

    As above but with additional rotation data (rot). This only applies to skulls sitting
    on the floor, not mounted to walls.
*/

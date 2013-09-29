/*
In this tutorial I will cover how to build your structure using an array from scratch.
Please also refer to the instructions in 'StructureArrays.java' for more information.
*/

/*
 * Step 1: Create a new class and array for your structure
 */
/*
Just create a completely empty class. We'll name it 'StructureArrayTutorial' for now.
*/
public class StructureArrayTutorial
{

}
/*
Yep, totally empty right now. Next, look in StructureArrays.java, find 'blockArrayTemplate',
and copy and paste that into your class. It should now look like this: 
*/
public class StructureArrayTutorial
{
    public static final int[][][][] blockArrayTemplate =
    {
        { // y
            { // x
                {} // z
            }
        }
    };
}

/*
 * Step 2: Create a basic structure
 */
 /*
 In this step, we're going to create a 5x5 platform. Recall that the z axis runs
 north to south, and the x axis runs east to west. All we need to do is place a
 block at five consecutive points along the z axis, making a north-south column,
 and repeat this column five times along the x axis (east-west).
 */
public static final int[][][][] blockArrayTemplate =
{
    { // y
        { // x
            // z: place 5 blocks here that will run north to south
            // This first z column is stored at x = 0 in our block array
            {Block.wood.blockID}, // here's one, be sure to add a comma ','
            {Block.wood.blockID}, // 2
            {Block.wood.blockID}, // 3
            {Block.wood.blockID}, // 4
            {Block.wood.blockID} // 5
            // That finishes one column, but we need four more columns
            // The next column will be at x = 1
        }, // Again, be sure to add a comma ','
        { // This is the start of x = 1
            // The brackets within start off the z array once again
            // Just copy and paste from above!
            {Block.wood.blockID}, // here's one, be sure to add a comma ','
            {Block.wood.blockID}, // 2
            {Block.wood.blockID}, // 3
            {Block.wood.blockID}, // 4
            {Block.wood.blockID} // 5
            // Just like that! Now copy an entire x subsection and paste it
            // 3 more times. Don't forget to add that comma!
        },
        { // This is the start of x = 1
            // The brackets within start off the z array once again
            // Just copy and paste from above!
            {Block.wood.blockID}, // here's one, be sure to add a comma ','
            {Block.wood.blockID}, // 2
            {Block.wood.blockID}, // 3
            {Block.wood.blockID}, // 4
            {Block.wood.blockID} // 5
            // Just like that! Now copy an entire x subsection and paste it
            // 3 more times. Don't forget to add that comma!
        },
        { // This is the start of x = 1
            // The brackets within start off the z array once again
            // Just copy and paste from above!
            {Block.wood.blockID}, // here's one, be sure to add a comma ','
            {Block.wood.blockID}, // 2
            {Block.wood.blockID}, // 3
            {Block.wood.blockID}, // 4
            {Block.wood.blockID} // 5
            // Just like that! Now copy an entire x subsection and paste it
            // 3 more times. Don't forget to add that comma!
        },
        { // This is the start of x = 1
            // The brackets within start off the z array once again
            // Just copy and paste from above!
            {Block.wood.blockID}, // here's one, be sure to add a comma ','
            {Block.wood.blockID}, // 2
            {Block.wood.blockID}, // 3
            {Block.wood.blockID}, // 4
            {Block.wood.blockID} // 5 <-- Remove the last comma!
            // Just like that! Now copy an entire x subsection and paste it
            // 3 more times. Don't forget to add that comma!
        }, // <-- Remove the last comma, as there won't be another x sub-array here
    }
}; // <-- There needs to be a semi-colon ';' at the end of your array
/*
Step 3: Add your structure to the StructureGenerator and test it out
*/
/*
To allow your structure to be easily spawned by the ItemStructureSpawner, we
need to add a new Structure to the list. Open the StructureGenerator class and
go to the very bottom. There you'll find a static {...} initializer that adds
all the current Structures. Add the following code after the last structure:
*/
// In StructureGenerator:
static
{
// All the other structures are here, put your code last:
structure = new Structure("Tutorial");
structure.addBlockArray(StructureArrayTutorial.blockArrayTutorial);
structures.add(structure);
}
/*
Easy as that! Note that I changed the name from 'blockArrayTemplate' to
'blockArrayTutorial'. You should do the same. Now launch Minecraft in Creative
mode, grab yourself a hammer (ItemStructureSpawner), press '[' left bracket
and your 'Tutorial' structure should be selected. Right click on a block to
generate it. Well, that's not so great, is it? A 5x5 wooden platform. Yay.
Let's make it better.
*/
/*
 * Step 4: Build a House
 */
/*
First, clean up the array by removing all those random comments, but leave the
'x=0', 'z=0', 'z=1', etc. comments in there, and make sure they are correct.
Trust me, it makes it a LOT easier to keep track of stuff that way.

Now your array should look like this:
*/
public static final int[][][][] blockArrayTutorial =
{
    { // y = 1
        { // x = 1
            {Block.wood.blockID}, // z = 1
            {Block.wood.blockID}, // z = 2
            {Block.wood.blockID}, // z = 3
            {Block.wood.blockID}, // z = 4
            {Block.wood.blockID} // z = 5
        },
        { // x = 2
            {Block.wood.blockID}, // z = 1
            {Block.wood.blockID}, // z = 2
            {Block.wood.blockID}, // z = 3
            {Block.wood.blockID}, // z = 4
            {Block.wood.blockID} // z = 5
        },
        { // x = 3
            {Block.wood.blockID}, // z = 1
            {Block.wood.blockID}, // z = 2
            {Block.wood.blockID}, // z = 3
            {Block.wood.blockID}, // z = 4
            {Block.wood.blockID} // z = 5
        },
        { // x = 4
            {Block.wood.blockID}, // z = 1
            {Block.wood.blockID}, // z = 2
            {Block.wood.blockID}, // z = 3
            {Block.wood.blockID}, // z = 4
            {Block.wood.blockID} // z = 5
        },
        { // x = 5
            {Block.wood.blockID}, // z = 1
            {Block.wood.blockID}, // z = 2
            {Block.wood.blockID}, // z = 3
            {Block.wood.blockID}, // z = 4
            {Block.wood.blockID} // z = 5
        }
    }
};
/*
You should notice that I started counting at 1, even though the array index is
really 0. That's because it doesn't matter as far as we're concerned. We just
need to keep track of the relative order, and it's far more natural to count
from 1 rather than 0. If you'd rather start at 0, that's fine too.

Ok, first copy and paste ALL of the array from x=1 to x=5 into a new y layer,
then do it one more time. You should now have 3 identical 5x5 'layers'.
*/
public static final int[][][][] blockArrayTutorial =
{
    { // y = 1
        { // x = 1
            {Block.wood.blockID}, // z = 1
            {Block.wood.blockID}, // z = 2
            {Block.wood.blockID}, // z = 3
            {Block.wood.blockID}, // z = 4
            {Block.wood.blockID} // z = 5
        },
        { // x = 2
            {Block.wood.blockID}, // z = 1
            {Block.wood.blockID}, // z = 2
            {Block.wood.blockID}, // z = 3
            {Block.wood.blockID}, // z = 4
            {Block.wood.blockID} // z = 5
        },
        { // x = 3
            {Block.wood.blockID}, // z = 1
            {Block.wood.blockID}, // z = 2
            {Block.wood.blockID}, // z = 3
            {Block.wood.blockID}, // z = 4
            {Block.wood.blockID} // z = 5
        },
        { // x = 4
            {Block.wood.blockID}, // z = 1
            {Block.wood.blockID}, // z = 2
            {Block.wood.blockID}, // z = 3
            {Block.wood.blockID}, // z = 4
            {Block.wood.blockID} // z = 5
        },
        { // x = 5
            {Block.wood.blockID}, // z = 1
            {Block.wood.blockID}, // z = 2
            {Block.wood.blockID}, // z = 3
            {Block.wood.blockID}, // z = 4
            {Block.wood.blockID} // z = 5
        }
    },
    { // y = 2
        { // x = 1
            {Block.wood.blockID}, // z = 1
            {Block.wood.blockID}, // z = 2
            {Block.wood.blockID}, // z = 3
            {Block.wood.blockID}, // z = 4
            {Block.wood.blockID} // z = 5
        },
        { // x = 2
            {Block.wood.blockID}, // z = 1
            {Block.wood.blockID}, // z = 2
            {Block.wood.blockID}, // z = 3
            {Block.wood.blockID}, // z = 4
            {Block.wood.blockID} // z = 5
        },
        { // x = 3
            {Block.wood.blockID}, // z = 1
            {Block.wood.blockID}, // z = 2
            {Block.wood.blockID}, // z = 3
            {Block.wood.blockID}, // z = 4
            {Block.wood.blockID} // z = 5
        },
        { // x = 4
            {Block.wood.blockID}, // z = 1
            {Block.wood.blockID}, // z = 2
            {Block.wood.blockID}, // z = 3
            {Block.wood.blockID}, // z = 4
            {Block.wood.blockID} // z = 5
        },
        { // x = 5
            {Block.wood.blockID}, // z = 1
            {Block.wood.blockID}, // z = 2
            {Block.wood.blockID}, // z = 3
            {Block.wood.blockID}, // z = 4
            {Block.wood.blockID} // z = 5
        }
    },
    { // y = 3
        { // x = 1
            {Block.wood.blockID}, // z = 1
            {Block.wood.blockID}, // z = 2
            {Block.wood.blockID}, // z = 3
            {Block.wood.blockID}, // z = 4
            {Block.wood.blockID} // z = 5
        },
        { // x = 2
            {Block.wood.blockID}, // z = 1
            {Block.wood.blockID}, // z = 2
            {Block.wood.blockID}, // z = 3
            {Block.wood.blockID}, // z = 4
            {Block.wood.blockID} // z = 5
        },
        { // x = 3
            {Block.wood.blockID}, // z = 1
            {Block.wood.blockID}, // z = 2
            {Block.wood.blockID}, // z = 3
            {Block.wood.blockID}, // z = 4
            {Block.wood.blockID} // z = 5
        },
        { // x = 4
            {Block.wood.blockID}, // z = 1
            {Block.wood.blockID}, // z = 2
            {Block.wood.blockID}, // z = 3
            {Block.wood.blockID}, // z = 4
            {Block.wood.blockID} // z = 5
        },
        { // x = 5
            {Block.wood.blockID}, // z = 1
            {Block.wood.blockID}, // z = 2
            {Block.wood.blockID}, // z = 3
            {Block.wood.blockID}, // z = 4
            {Block.wood.blockID} // z = 5
        }
    }
};
/*
You can see how the array quickly gets very long, so we're going to scrunch it
together some to save space. Later on, we may have to write it 'longhand' again
in order to more clearly see what's going on. Here's the short version:
*/
public static final int[][][][] blockArrayTutorial =
{
    { // y = 1
        { // x = 1
            {Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID}
        },
        { // x = 2
            {Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID}
        },
        { // x = 3
            {Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID}
        },
        { // x = 4
            {Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID}
        },
        { // x = 5
            {Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID}
        }
    },
    { // y = 2
        { // x = 1
            {Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID}
        },
        { // x = 2
            {Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID}
        },
        { // x = 3
            {Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID}
        },
        { // x = 4
            {Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID}
        },
        { // x = 5
            {Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID}
        }
    },
    { // y = 3
        { // x = 1
            {Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID}
        },
        { // x = 2
            {Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID}
        },
        { // x = 3
            {Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID}
        },
        { // x = 4
            {Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID}
        },
        { // x = 5
            {Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID}
        }
    }
};
/*
Use caution when condensing your arrays in this way, as it's much easier to make errors like this.
I actually recommend you keep your array in the longer format, but for the sake of space in this
tutorial, I will use the condensed version.

Ok, so we wanted to make a house, right? A house is hollow on the inside, so we just need to set
all the blocks inside to air, or id of 0. But where is inside? Easy. The first and last index of
both x and z represent the outer edges of the array, and thus are the outer edges of our structure.
So the first and last block in each z 'column' need to stay wood, as do all of the blocks at the
first and last x arrays. We need to do this for each y 'layer' as well, but we'll leave y=3 totally
solid as a roof.
*/
public static final int[][][][] blockArrayTutorial =
{
    { // y = 1
        { // x = 1
        // We need an entrance to, so let's make it in the center of this column here, so at z=3
            {Block.wood.blockID},{Block.wood.blockID},{0},{Block.wood.blockID},{Block.wood.blockID}
        },
        { // x = 2
            {Block.wood.blockID},{0},{0},{0},{Block.wood.blockID}
        },
        { // x = 3
            {Block.wood.blockID},{0},{0},{0},{Block.wood.blockID}
        },
        { // x = 4
            {Block.wood.blockID},{0},{0},{0},{Block.wood.blockID}
        },
        { // x = 5
            {Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID}
        }
    },
    { // y = 2
        { // x = 1
        // Don't forget to make your entrance at the same x/z location in the next layer!
        // In y=1, we made a hole at x=1, z=3, so let's do the same here for a 2-block tall entrance
            {Block.wood.blockID},{Block.wood.blockID},{0},{Block.wood.blockID},{Block.wood.blockID}
        },
        { // x = 2
            {Block.wood.blockID},{0},{0},{0},{Block.wood.blockID}
        },
        { // x = 3
            {Block.wood.blockID},{0},{0},{0},{Block.wood.blockID}
        },
        { // x = 4
            {Block.wood.blockID},{0},{0},{0},{Block.wood.blockID}
        },
        { // x = 5
            {Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID}
        }
    },
    { // y = 3
        { // x = 1
            {Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID}
        },
        { // x = 2
            {Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID}
        },
        { // x = 3
            {Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID}
        },
        { // x = 4
            {Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID}
        },
        { // x = 5
            {Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID},{Block.wood.blockID}
        }
    }
};
/*
Alright, give it a try and see. Where is your entrance? Probably not in front of you. What's
going on? Well, we didn't set a default facing for our structure. Since we want x=1 (minimum
x value) to be the front of our structure, taking a glance at the StructureArrays.java
instructions tells us we need to set our default facing to WEST. In StructureGenerator where
we added our new "Tutorial" structure, you need to add one line.
*/
structure = new Structure("Tutorial");
structure.addBlockArray(StructureArrays.blockArrayTutorial);
// The following line does just what it says. Best to do it before adding the structure to the List.
structure.setFacing(StructureGeneratorBase.WEST);
structures.add(structure);
/*
Try again, your entrance should be directly in front of you when you generate the structure
no matter which way you turn.

Note that setting a structure facing is only important when you want a specific side to face
the player; if you are generating structures during world generation, it won't matter what
default facing you set, and you can even forego setting a facing altogether. I like to always
have a facing in mind, though, as it makes figuring out the correct metadata values easier.
Once you have the correct value, though, it won't matter which facing you give the structure.
*/
/*
 * Step 5: Embellishments and Furnishings
 */
 /*
 So you've got a hollow wooden box with a hole in it large enough for Steve to walk through.
 Not very exciting. In this step we'll add a door, some windows, a bed, a chest, and a torch.
 
 First, the windows. Obviously they will replace one of the wall blocks, so change some 'wood'
 blocks to 'glass'. I'd suggest doing this at your second y layer, perhaps in the center of
 each wall.
 
 Next, we need to add a door. That's a little more complicated, and will involve using metadata.
 In our array, each 'z' value is actually an array of its own, though we currently only store a
 single block id value in there. To add metadata, simply place a comma ',' after the block id
 followed by the value: {Block.block.blockID, MetaDataValue}
 
 Taking a look at either the Minecraft wiki 'DataValues' page or StructureArrays.java instructions,
 we see: "Bottom block should have a value of 0,1,2,3 facing west, north, east, or south". Hm, well,
 our door is on the front of our structure, and our structure faces west, so it would make sense for
 our door to face west as well. Therefore the bottom block should have a metadata value of '0'. For
 the top block, the metadata value is simply that of the bottom block plus '8'.
 
 For the bed, we'll put it along the back wall, so it's length is along the north-south axis. Let's
 put the head towards the wall, rather than in the center of the room. Refer back to the instruction
 manual to see how to set metadata for beds: "0,1,2,3 facing south, west, north, east; plus 8 for head".
 
 Our bed will be facing 'left' when we look at it from the doorway, which for a structure of facing
 WEST, 'left' is to the north (and also happens to be 'left' in the array, as in z = 1). So the block
 in the corner should have a meta of north+head, or 2+8 = 10. The block to it's right also needs to
 face north, so we'll set it's meta to 2.
 
 Next, we'll place a chest in the opposite corner, just to the right of the door. We want it to be
 easily accessible right when you step inside, so it will face toward the entrance, which just so
 happens to be the same direction our bed is facing, north. Note that chests don't use the same meta
 values as beds, so go find the correct value for a chest facing 'north' and use that.
 
 Finally, we'll place a torch to light up the inside of our little house. A nice place for this might
 be opposite the chest, on the block adjacent to the door and facing inwards. Recall that our door faces
 west, so the torch should face east, since it's facing the opposite direction compared to the door.
 
 Here's a little diagram of the floor layout; your array setup should look very similar! Keep in mind
 that the glass windows and torch should be at eye level, not on the ground.
 
  W W D W W     W: Wood     =: Glass
  W T - C W     D: Door     -: Air
  = - - - =     C: Chest
  W B B - W     B: Bed
  W W = W W     T: Torch
  
  Fire it up and see how it looks! If a block is out of place, try placing it elsewhere in the array
  until you get it right. If a block is facing the wrong way, check StructureArrays.java once again
  and read up on how to set metadata for that block.
  
  If you got everything just right, play around with it and see if you can rearrange the room. Swap the
  bed with the chest, but keep the bed running along the wall. Move the torch to a different wall. Can
  you figure out how to get a torch on the outside of the house?
  
  If you tried and tried and tried but couldn't get the structure correct, I've uploaded the completed
  block array to my Github Forge Tutorials page under 'supplementary' materials:
  
  https://github.com/coolAlias/Forge_Tutorials/tree/master/supplementary
 */

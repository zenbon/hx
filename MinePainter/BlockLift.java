package hx.MinePainter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cpw.mods.fml.common.ObfuscationReflectionHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.BlockPistonMoving;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Facing;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class BlockLift extends Block{
	
	private List<ChunkPosition> checked = new ArrayList<ChunkPosition>();

	public BlockLift(int par1) {
		super(par1, Material.rock);
		setBlockName("blockLift");
		setCreativeTab(CreativeTabs.tabDecorations);
	}

	public int getBlockTextureFromSideAndMetadata(int par1, int par2)
    {
		if(par1 == par2)return 5;
        return Block.pistonBase.getBlockTextureFromSideAndMetadata(par1, par2);
    }
	
	public int getRenderType()
	{
		return 16;
	}
	
	public void onBlockPlacedBy(World par1World, int par2, int par3, int par4, EntityLiving par5EntityLiving)
	{
		int var6 = BlockPistonBase.determineOrientation(par1World, par2, par3, par4, (EntityPlayer)par5EntityLiving);
		par1World.setBlockMetadataWithNotify(par2, par3, par4, var6);
	}

	public void onNeighborBlockChange(World par1World, int par2, int par3, int par4, int par5)
	{
		int meta = par1World.getBlockMetadata(par2, par3, par4);
		par1World.scheduleBlockUpdate(par2, par3, par4, this.blockID, 4);
	}

	public void updateTick(World par1World, int par2, int par3, int par4, Random par5Random)
	{
		if(isIndirectlyPowered(par1World, par2, par3, par4))
			par1World.addBlockEvent(par2, par3, par4, this.blockID, 0, par1World.getBlockMetadata(par2, par3, par4));
	}

	private static Object reflectCall(Object thing, String name, Object... params)
	{
		try
		{
			Class<?>[] classes = new Class<?>[params.length];

			for(int i =0;i<params.length;i++)
			{
				classes[i] = params[i].getClass();
				if(World.class.isAssignableFrom(classes[i]))classes[i] = World.class;
				else if(classes[i].equals(Integer.class))classes[i] = int.class;
				else if(classes[i].equals(Boolean.class))classes[i] = boolean.class;
			}

			Method m = thing.getClass().getDeclaredMethod(name, classes);
			m.setAccessible(true);
			return m.invoke(thing, params);
		}catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}

	public void onBlockEventReceived(World par1World, int par2, int par3, int par4, int par5, int par6)
	{
		System.out.println("blah");
		int x = par2 + Facing.offsetsXForSide[par6 & 7];
		int y = par3 + Facing.offsetsYForSide[par6 & 7] - 1;
		int z = par4 + Facing.offsetsZForSide[par6 & 7];

		if(par5 == 0 && canExtend(par1World,x,y,z, 1, true))
		{
			if (tryExtend(par1World, x,y,z, 1, true))
			{
				par1World.playSoundEffect((double)par2 + 0.5D, (double)par3 + 0.5D, (double)par4 + 0.5D, "tile.piston.out", 0.5F, par1World.rand.nextFloat() * 0.25F + 0.6F);
			}
		}
	}

	private boolean isIndirectlyPowered(World par1World, int par2, int par3, int par4)
	{
		if(par1World.isBlockIndirectlyGettingPowered(par2, par3, par4))return true;
		if(par1World.getBlockId(par2, par3 - 1, par4) == this.blockID)
			return isIndirectlyPowered(par1World,par2,par3 - 1,par4);
		return false;
	}

	private boolean tryExtend(World par1World, int par2, int par3, int par4, int par5, boolean first)
	{
		if(first)checked.clear();
		checked.add(new ChunkPosition(par2,par3,par4));
		
		int var6 = par2 + Facing.offsetsXForSide[par5];
		int var7 = par3 + Facing.offsetsYForSide[par5];
		int var8 = par4 + Facing.offsetsZForSide[par5];
		int var9 = 0;

		while (true)
		{
			int var10;

			if (var9 < 13)
			{
				if (var7 <= 0 || var7 >= par1World.getHeight() - 1)
				{
					return false;
				}

				var10 = par1World.getBlockId(var6, var7, var8);

				if (var10 != 0)
				{
					if (!(Boolean)reflectCall(Block.pistonBase,"canPushBlock",var10, par1World, var6, var7, var8, true))
					{
						return false;
					}

					if (Block.blocksList[var10].getMobilityFlag() != 1)
					{
						if (var9 == 12)
						{
							return false;
						}
						
						//addition start
                        if(var10 == Block.stoneBrick.blockID)
                        for(int i =0;i<6;i++)
                        {
                        	if(i == par5)continue;
                        	if(i == par5+1 && par5%2 == 0)continue;
                        	if(i == par5-1 && par5%2 == 1)continue;
                        	
                        	int o_x = var6 + Facing.offsetsXForSide[i];
                        	int o_y = var7 + Facing.offsetsYForSide[i];
                        	int o_z = var8 + Facing.offsetsZForSide[i];
                        	
                        	if(par1World.getBlockId(o_x, o_y, o_z) == this.blockID)continue;
                        	
                        	o_x -= Facing.offsetsXForSide[par5];
                        	o_y -= Facing.offsetsYForSide[par5];
                        	o_z -= Facing.offsetsZForSide[par5];

                        	if(checked.contains(new ChunkPosition(o_x,o_y,o_z)))continue;
                        	if(!tryExtend(par1World, o_x, o_y, o_z,par5, false))return false;
                        }
                        
                        //addition end

						var6 += Facing.offsetsXForSide[par5];
						var7 += Facing.offsetsYForSide[par5];
						var8 += Facing.offsetsZForSide[par5];
						++var9;
						continue;
					}

					Block.blocksList[var10].dropBlockAsItem(par1World, var6, var7, var8, par1World.getBlockMetadata(var6, var7, var8), 0);
					par1World.setBlockWithNotify(var6, var7, var8, 0);
				}
			}

			while (var6 != par2 || var7 != par3 || var8 != par4)
			{
				var9 = var6 - Facing.offsetsXForSide[par5];
				var10 = var7 - Facing.offsetsYForSide[par5];
				int var11 = var8 - Facing.offsetsZForSide[par5];
				int var12 = par1World.getBlockId(var9, var10, var11);
				int var13 = par1World.getBlockMetadata(var9, var10, var11);

				if (var9 == par2 && var10 == par3 && var11 == par4)
				{
					par1World.setBlockAndMetadataWithUpdate(var6, var7, var8, 0,0, false);
				}
				else
				{
					par1World.setBlockAndMetadataWithUpdate(var6, var7, var8, Block.pistonMoving.blockID, var13, false);
					par1World.setBlockTileEntity(var6, var7, var8, BlockPistonMoving.getTileEntity(var12, var13, par5, true, false));
				}

				var6 = var9;
				var7 = var10;
				var8 = var11;
			}

			return true;
		}
	}
	
	private boolean canExtend(World par0World, int par1, int par2, int par3, int par4, boolean first)
    {
		if(first)checked.clear();
		checked.add(new ChunkPosition(par1,par2,par3));
		
        int var5 = par1 + Facing.offsetsXForSide[par4];
        int var6 = par2 + Facing.offsetsYForSide[par4];
        int var7 = par3 + Facing.offsetsZForSide[par4];
        int var8 = 0;

        while (true)
        {
            if (var8 < 13)
            {
                if (var6 <= 0 || var6 >= par0World.getHeight() - 1)
                {
                    return false;
                }

                int var9 = par0World.getBlockId(var5, var6, var7);

                if (var9 != 0)
                {
                	if (!(Boolean)reflectCall(Block.pistonBase,"canPushBlock",var9, par0World, var5, var6, var7, true))
                    {
                        return false;
                    }

                    if (Block.blocksList[var9].getMobilityFlag() != 1)
                    {
                        if (var8 == 12)
                        {
                            return false;
                        }
                        
                        //addition start
                        if(var9 == Block.stoneBrick.blockID)
                        for(int i =0;i<6;i++)
                        {
                        	if(i == par4)continue;
                        	if(i == par4+1 && par4%2 == 0)continue;
                        	if(i == par4-1 && par4%2 == 1)continue;
                        	
                        	int o_x = var5 + Facing.offsetsXForSide[i];
                        	int o_y = var6 + Facing.offsetsYForSide[i];
                        	int o_z = var7 + Facing.offsetsZForSide[i];
                        	
                        	if(par0World.getBlockId(o_x, o_y, o_z) == this.blockID)continue;
                        	
                        	o_x -= Facing.offsetsXForSide[par4];
                        	o_y -= Facing.offsetsYForSide[par4];
                        	o_z -= Facing.offsetsZForSide[par4];
                        	 
                        	if(checked.contains(new ChunkPosition(o_x,o_y,o_z)))continue;
                        	if(!canExtend(par0World, o_x, o_y, o_z,par4, false))return false;
                        }
                        
                        //addition end

                        var5 += Facing.offsetsXForSide[par4];
                        var6 += Facing.offsetsYForSide[par4];
                        var7 += Facing.offsetsZForSide[par4];
                        ++var8;
                        continue;
                    }
                }
            }

            return true;
        }
    }
}

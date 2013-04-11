package hx.MinePainter;

import hx.utils.Debug;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Icon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public class BlockCanvas extends BlockContainer{
	
	private float dp = 0.01f;

	public BlockCanvas(int id){
		super(id, Material.cloth);
		setUnlocalizedName("blockCanvas");
	}

	@Override
	public TileEntity createNewTileEntity(World var1) {
		return new TileEntityCanvas();
	}
	
	public Icon getBlockTextureFromSideAndMetadata(int par1, int par2)
    {
        return Block.cloth.getBlockTextureFromSideAndMetadata(par1, 0);
    }

	public void setBlockBoundsBasedOnState(IBlockAccess par1IBlockAccess, int par2, int par3, int par4)
    {
        int meta = par1IBlockAccess.getBlockMetadata(par2, par3, par4);

        if (meta >= 8)
        {
            this.setBlockBounds(0, 0f, 0, 1f, dp, 1f);
        }
        else if (meta < 4)
        {
            this.setBlockBounds(0, 1 - dp, 0, 1f, 1f, 1f);
        }
        else if (meta == 4)
        {
            this.setBlockBounds(0, 0f, 1 - dp, 1f, 1f, 1f);
        }
        else if (meta == 5)
        {
            this.setBlockBounds(0, 0f, 0, 1f, 1f, dp);
        }
        else if (meta == 6)
        {
            this.setBlockBounds(1 - dp, 0f, 0, 1f, 1f, 1f);
        }
        else if (meta == 7)
        {
            this.setBlockBounds(0, 0f, 0, dp, 1f, 1f);
        }
    }

    public AxisAlignedBB getCollisionBoundingBoxFromPool(World par1World, int par2, int par3, int par4)
    {
        return null;
    }
    
    @Override
    public boolean renderAsNormalBlock()
    {
        return false;
    }

    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }
    
    @Override
    public int getRenderType()
    {
        return -1;
    }

    public int idDropped(int par1, Random par2Random, int par3)
    {
    	return ModMinePainter.instance.item("Canvas").item().itemID;
    }
    
    public int idPicked(World par1World, int par2, int par3, int par4)
    {
        return ModMinePainter.instance.item("Canvas").item().itemID;
    }
    
    @Override
    public void onNeighborBlockChange(World w, int x, int y, int z, int id)
    {
        int face = w.getBlockMetadata(x, y, z);

        if (face >= 8)
        {
            face = 1;
        }
        else if (face < 4)
        {
            face = 0;
        }
        else
        {
            face -= 2;
        }

        ForgeDirection dir = ForgeDirection.getOrientation(face);

        int bid = w.getBlockId(x - dir.offsetX, y - dir.offsetY, z - dir.offsetZ);
        if (Block.isNormalCube(bid) || Block.blocksList[bid] == BlockSculpture.instance)
            return;

        w.setBlock(x, y, z, 0, 0, 3);
    }
    
    public boolean onBlockActivated(World w, int x, int y, int z, EntityPlayer ep,
    		int face, float _x, float _y, float _z)
    {
    	if(w.isRemote)return false;
    	
    	int code = 0;
    	if(ep.inventory.getCurrentItem() == null)
    		code = 0;
    	else if(ep.getCurrentEquippedItem().itemID == Item.dyePowder.itemID)
    	{
    		code = ep.getCurrentEquippedItem().getItemDamage() | 16;
    	}else if(ep.getCurrentEquippedItem().itemID == Item.slimeBall.itemID)
    	{
    		code = -1;
    	}else return false;
    	
    	float px,py;
    	
    	int meta = w.getBlockMetadata(x, y, z);
    	int index = pixelIndex(_x,_y,_z,meta);
    	
    	TileEntityCanvas tec = (TileEntityCanvas) w.getBlockTileEntity(x, y, z);
    	
    	if(code == -1)
    		tec.image.fill(0);
    	else if(!ep.isWet())
    		tec.image.set(15-index/16, 15-index%16, code);
    	else tec.image.flood(15-index/16, 15-index%16, -1, code);
    	
    	w.markBlockForUpdate(x, y, z);
    	
    	return true;
    }
    
    public static TileEntitySculpture getSculptureOnBack(World w, int x,int y,int z, int face)
    {
    	ForgeDirection dir = ForgeDirection.getOrientation(face);
    	x -= dir.offsetX;
    	y -= dir.offsetY;
    	z -= dir.offsetZ;
    	
    	try{
    		return (TileEntitySculpture) w.getBlockTileEntity(x,y,z);
    	}catch(Exception e)
    	{
    		return null;
    	}
    }
    
    public MovingObjectPosition collisionRayTrace(World w, int x, int y, int z, Vec3 st, Vec3 ed)
    {
    	int face = w.getBlockMetadata(x, y, z);

        if (face >= 8)
        {
            face = 1;
        }
        else if (face < 4)
        {
            face = 0;
        }
        else
        {
            face -= 2;
        }
        
    	TileEntitySculpture tes = getSculptureOnBack(w,x,y,z,face);
    	if(tes != null)
    	{
    		MovingObjectPosition mop =  BlockSculpture.instance.collisionRayTrace(w, tes.xCoord, tes.yCoord,tes.zCoord, st, ed);
    		if(mop != null)
    		{
    			mop.blockX = x;
    			mop.blockY = y;
    			mop.blockZ = z;
    			return mop;
    		}
    	}else return super.collisionRayTrace(w, x, y, z, st, ed);
    	return null;
    }
     
    private static ForgeDirection[] xproj=
    	{
    	ForgeDirection.WEST,
    	ForgeDirection.EAST,
    	ForgeDirection.SOUTH,
    	ForgeDirection.NORTH,

    	ForgeDirection.WEST,
    	ForgeDirection.EAST,
    	ForgeDirection.SOUTH,
    	ForgeDirection.NORTH,

    	ForgeDirection.WEST,
    	ForgeDirection.EAST,
    	ForgeDirection.SOUTH,
    	ForgeDirection.NORTH
    	};
    private static ForgeDirection[] yproj=
    	{
    	ForgeDirection.NORTH,
    	ForgeDirection.SOUTH,
    	ForgeDirection.WEST,
    	ForgeDirection.EAST,

    	ForgeDirection.UP,
    	ForgeDirection.UP,
    	ForgeDirection.UP,
    	ForgeDirection.UP,

    	ForgeDirection.SOUTH,
    	ForgeDirection.NORTH,
    	ForgeDirection.EAST,
    	ForgeDirection.WEST,
    	
    	};
   
    private int pixelIndex(float x,float y,float z,int face)
    {
    	ForgeDirection xdir = xproj[face];
    	ForgeDirection ydir = yproj[face];
    	
    	float xoff = xdir.offsetX * x + xdir.offsetY * y + xdir.offsetZ * z;
    	float yoff = ydir.offsetX * x + ydir.offsetY * y + ydir.offsetZ * z;
    	
    	if(xoff < 0)xoff+=1;
    	if(yoff < 0)yoff+=1;
    	
    	return (int)(xoff*16)*16 + (int)(yoff*16);
    }
}

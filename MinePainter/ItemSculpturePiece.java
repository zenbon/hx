package hx.MinePainter;

import net.minecraft.item.Item;

public class ItemSculpturePiece extends Item{

	public ItemSculpturePiece(int par1) {
		super(par1);
		setUnlocalizedName("itemSculpturePiece");
	}

	public boolean getHasSubtypes()
	{
		return true;
	}
}

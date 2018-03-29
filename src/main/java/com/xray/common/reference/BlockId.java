package com.xray.common.reference;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

/**
 * Identifies a block using Minecraft id and metadata
 */
public class BlockId
{
	public int id;
	public int meta;

	public BlockId( int id, int meta )
	{
		this.id = id;
		this.meta = meta;
	}

	public static BlockId fromBlockState( IBlockState state )
	{
		Block block = state.getBlock();
		return new BlockId( Block.getIdFromBlock( block ), block.getMetaFromState( state ) );
	}

	@Override
        public int hashCode()
        {
                return 37 * id + meta;
        }

        @Override
        public boolean equals(Object o)
        {
                if (o == null || !(o instanceof BlockId)) return false;
                BlockId t = (BlockId) o;
                return id == t.id && meta == t.meta;
        }

	public static Set<BlockId> drawablesFromOreInfo( OreInfo ore )
	{
		Set<BlockId> res = new HashSet();
		if ( !ore.isDrawable() )
			return res;
		if ( ore.useOredict() )
		{
			int[] ids = OreDictionary.getOreIDs( ore.getItemStack() );
			if ( ids != null && ids.length != 0 )
			{
				for ( int id : ids )
					res.addAll( fromDictionary(OreDictionary.getOreName(id)) );
				return res;
			}
		}
		res.add( new BlockId(ore.getId(), ore.getMeta()) );
		return res;
	}

	/**
	 * Creates a list of all blocks corresponding to the given OreDictionary name.
	 * @param forgeName Forge identifier (eg. oreCopper)
	 * @return A set of BlockId for the given ore name, empty list if none were found
	 */
	public static Set<BlockId> fromDictionary( String forgeName )
	{
		Set<BlockId> list = new HashSet();
		for ( ItemStack stack : OreDictionary.getOres( forgeName, false ) )
		{
			int id = Block.getIdFromBlock( Block.getBlockFromItem(stack.getItem()) );
			list.add( new BlockId( id, stack.getItemDamage()) );
		}
		return list;
	}
}

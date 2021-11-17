package lizcraft.immersiveextras.common.blocks;

import java.util.function.Supplier;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.IETileProviderBlock;
import lizcraft.immersiveextras.ImmersiveExtras;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;

public class IExtrasTileBlockBase<T extends TileEntity> extends IETileProviderBlock<T>
{
	public static final EnumProperty<BlockRotation> ROTATION = EnumProperty.create("rotation", BlockRotation.class);

	public IExtrasTileBlockBase(String name, Supplier<TileEntityType<T>> tileType, Properties blockProps) 
	{
		super(name, tileType, blockProps);
	}
	
	@Override
	public ResourceLocation createRegistryName()
	{
		return new ResourceLocation(ImmersiveExtras.MODID, name);
	}

	
	@Override
	protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(IEProperties.FACING_ALL);
	}
	
	public static enum BlockRotation implements IStringSerializable
	{
		DEG_0("0"),
		DEG_90("90"),
		DEG_180("180"),
		DEG_270("270");
		
		private final String name;
		
		private BlockRotation(String name)
		{
			this.name = name;
		}

		@Override
		public String getSerializedName() 
		{
			return this.name;
		}
		
		public BlockRotation next()
		{
			switch (this)
			{
				case DEG_0:
					return DEG_90;
				case DEG_90:
					return DEG_180;
				case DEG_180:
					return DEG_270;
				case DEG_270:
				default:
					return DEG_0;
			}
		}
	}
}

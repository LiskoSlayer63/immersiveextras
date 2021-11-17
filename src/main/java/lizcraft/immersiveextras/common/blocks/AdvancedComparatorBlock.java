package lizcraft.immersiveextras.common.blocks;

import blusunrize.immersiveengineering.api.IEProperties;
import lizcraft.immersiveextras.common.IExtrasTileTypes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.block.material.PushReaction;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.ToolType;

public class AdvancedComparatorBlock extends IExtrasTileBlockBase<AdvancedComparatorTileEntity>
{
	private static final Material material = new Material(MaterialColor.METAL, false, false, true, true, false, false, PushReaction.BLOCK);
	
	private static final VoxelShape south_north = Block.box(0D, 0D, 5D, 16D, 16D, 16D);
	private static final VoxelShape north_north = Block.box(5D, 5D, 0D, 11D, 11D, 5D);
	private static final VoxelShape shape_north = VoxelShapes.or(south_north, north_north);

	private static final VoxelShape north_south = Block.box(0D, 0D, 0D, 16D, 16D, 11D);
	private static final VoxelShape south_south = Block.box(5D, 5D, 11D, 11D, 11D, 16D);
	private static final VoxelShape shape_south = VoxelShapes.or(south_south, north_south);

	private static final VoxelShape west_east = Block.box(0D, 0D, 0D, 11D, 16D, 16D);
	private static final VoxelShape east_east = Block.box(11D, 5D, 5D, 16D, 11D, 11D);
	private static final VoxelShape shape_east = VoxelShapes.or(west_east, east_east);

	private static final VoxelShape east_west = Block.box(5D, 0D, 0D, 16D, 16D, 16D);
	private static final VoxelShape west_west = Block.box(0D, 5D, 5D, 5D, 11D, 11D);
	private static final VoxelShape shape_west = VoxelShapes.or(east_west, west_west);

	private static final VoxelShape upper_down = Block.box(0D, 5D, 0D, 16D, 16D, 16D);
	private static final VoxelShape lower_down = Block.box(5D, 0D, 5D, 11D, 5D, 11D);
	private static final VoxelShape shape_down = VoxelShapes.or(lower_down, upper_down);
	
	private static final VoxelShape lower_up = Block.box(0D, 0D, 0D, 16D, 11D, 16D);
	private static final VoxelShape upper_up = Block.box(5D, 11D, 5D, 11D, 16D, 11D);
	private static final VoxelShape shape_up = VoxelShapes.or(lower_up, upper_up);
	
	public AdvancedComparatorBlock() 
	{
		super("advanced_comparator", IExtrasTileTypes.ADVANCED_COMPARATOR,
				Block.Properties.of(material)
				.strength(5.0F, 6.0F)
				.harvestTool(ToolType.PICKAXE)
				.sound(SoundType.METAL)
				.isRedstoneConductor((s, r, p) -> false));
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) 
	{
		switch((Direction)state.getValue(IEProperties.FACING_ALL))
		{
		case NORTH:
			return shape_north;
		case EAST:
			return shape_east;
		case SOUTH:
			return shape_south;
		case WEST:
			return shape_west;
		case DOWN:
			return shape_down;
		case UP:
		default:
			return shape_up;
		}
	}
}

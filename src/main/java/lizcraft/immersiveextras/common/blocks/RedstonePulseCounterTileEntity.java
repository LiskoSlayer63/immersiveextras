package lizcraft.immersiveextras.common.blocks;

import static lizcraft.immersiveextras.common.blocks.IExtrasTileBlockBase.ROTATION;

import javax.annotation.Nonnull;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHammerInteraction;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IRedstoneOutput;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IScrewdriverInteraction;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import lizcraft.immersiveextras.ImmersiveExtras;
import lizcraft.immersiveextras.common.IExtrasContent;
import lizcraft.immersiveextras.common.IExtrasTileTypes;
import lizcraft.immersiveextras.common.blocks.IExtrasTileBlockBase.BlockRotation;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.Property;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.vector.Vector3d;

public class RedstonePulseCounterTileEntity extends IEBaseTileEntity implements ITickableTileEntity, IAdvancedDirectionalTile, IStateBasedDirectional, IHammerInteraction, IScrewdriverInteraction, IRedstoneOutput
{
	public static enum CountDirection
	{
		UP,
		DOWN
	}
	
	public static enum CountEdge
	{
		RISING,
		FALLING
	}
	
	public CountDirection countDirection = CountDirection.UP;
	public CountEdge countEdge = CountEdge.RISING;
	public boolean loopEnd = true;
	public int countLimit = 15;
	public int countedPulses = 0;
	
	private Direction oldFacing;
	private boolean isUp = false;
	
	public RedstonePulseCounterTileEntity() 
	{
		super(IExtrasTileTypes.REDSTONE_PULSECOUNTER.get());
		
		this.oldFacing = getFacing();
	}
	
	protected void handlePulse()
	{
		int oldCount = countedPulses;
		
		if (countDirection == CountDirection.UP)
			countedPulses++;
		else
			countedPulses--;
		
		if (countedPulses > countLimit)
			countedPulses = loopEnd ? 0 : countLimit;
		
		if (countedPulses < 0)
			countedPulses = loopEnd ? countLimit : 0;
		
		if (countedPulses != oldCount)
			updateAfterConfigure();
	}
	
	protected void updateAfterConfigure()
	{
		if (countedPulses > countLimit)
			countedPulses = countLimit;
		
		setChanged();
		
		this.markContainingBlockForUpdate(null);
		level.blockEvent(getBlockPos(), this.getBlockState().getBlock(), 254, 0);
	}
	
	protected boolean checkOutputSide(Direction side)
	{
		BlockState state = getState();
		Direction facing = state.getValue(IEProperties.FACING_ALL);
		BlockRotation rotation = state.getValue(ROTATION);
		
		switch (side)
		{
			case NORTH:
				if (facing == Direction.UP || facing == Direction.DOWN)
					return rotation == BlockRotation.DEG_0;
				if (facing == Direction.EAST)
					return rotation == BlockRotation.DEG_90;
				if (facing == Direction.WEST)
					return rotation == BlockRotation.DEG_270;
				return false;
			case EAST:
				if (facing == Direction.UP || facing == Direction.DOWN)
					return rotation == BlockRotation.DEG_90;
				if (facing == Direction.SOUTH)
					return rotation == BlockRotation.DEG_90;
				if (facing == Direction.NORTH)
					return rotation == BlockRotation.DEG_270;
				return false;
			case SOUTH:
				if (facing == Direction.UP || facing == Direction.DOWN)
					return rotation == BlockRotation.DEG_180;
				if (facing == Direction.WEST)
					return rotation == BlockRotation.DEG_90;
				if (facing == Direction.EAST)
					return rotation == BlockRotation.DEG_270;
				return false;
			case WEST:
				if (facing == Direction.UP || facing == Direction.DOWN)
					return rotation == BlockRotation.DEG_270;
				if (facing == Direction.NORTH)
					return rotation == BlockRotation.DEG_90;
				if (facing == Direction.SOUTH)
					return rotation == BlockRotation.DEG_270;
				return false;
			case UP:
				return (facing != Direction.DOWN && facing != Direction.UP) && rotation == BlockRotation.DEG_0;
			case DOWN:
				return (facing != Direction.DOWN && facing != Direction.UP) && rotation == BlockRotation.DEG_180;
			default:
				return false;
		}
	}
	
	protected boolean checkInputSide(Direction side)
	{
		return checkOutputSide(side.getOpposite());
	}

	@Override
	public PlacementLimitation getFacingLimitation()
	{
		return PlacementLimitation.SIDE_CLICKED;
	}

	@Override
	public Property<Direction> getFacingProperty() 
	{
		return IEProperties.FACING_ALL;
	}
	
	@Override
	public boolean mirrorFacingOnPlacement(LivingEntity placer)
	{
		return false;
	}

	@Override
	public void tick() 
	{
		int input = getMaxRSInput();
		
		if ((countEdge == CountEdge.RISING && input > 0 && !isUp) || (countEdge == CountEdge.FALLING && input == 0 && isUp))
			handlePulse();
		
		isUp = input > 0;
		
		if (oldFacing != getFacing())
		{
			oldFacing = getFacing();
			
			updateAfterConfigure();
		}
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket) 
	{
		this.countDirection = CountDirection.values()[nbt.getByte("countDirection")];
		this.countEdge = CountEdge.values()[nbt.getByte("countEdge")];
		this.countLimit = nbt.getByte("countLimit");
		this.countedPulses = nbt.getByte("countedPulses");
		this.loopEnd = nbt.getBoolean("loopEnd");
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket) 
	{
		nbt.putByte("countDirection", (byte)this.countDirection.ordinal());
		nbt.putByte("countEdge", (byte)this.countEdge.ordinal());
		nbt.putByte("countLimit", (byte)this.countLimit);
		nbt.putByte("countedPulses", (byte)this.countedPulses);
		nbt.putBoolean("loopEnd", this.loopEnd);
	}
	
	@Override
	public void receiveMessageFromClient(CompoundNBT message)
	{
		if(message.contains("countDirection"))
			this.countDirection = CountDirection.values()[message.getByte("countDirection")];
		
		if(message.contains("countEdge"))
			this.countEdge = CountEdge.values()[message.getByte("countEdge")];
		
		if(message.contains("loopEnd"))
			this.loopEnd = message.getBoolean("loopEnd");
		
		if(message.contains("countLimit"))
			this.countLimit = message.getByte("countLimit");
		
		if(message.contains("countReset") && message.getBoolean("countReset"))
			this.countedPulses = 0;
		
		updateAfterConfigure();
	}
	
	@Override
	public boolean canConnectRedstone(Direction from) 
	{
		return checkOutputSide(from) || checkInputSide(from);
	}
	
	@Override
	public int getRSInput(@Nonnull Direction side)
	{
		return checkInputSide(side) ? super.getRSInput(side) : 0;
	}

	@Override
	public int getStrongRSOutput(Direction from) 
	{
		return checkOutputSide(from.getOpposite()) ? this.countedPulses : 0;
	}

	@Override
	public int getWeakRSOutput(Direction from)
	{
		return checkOutputSide(from.getOpposite()) ? this.countedPulses : 0;
	}
	
	@Override
	public boolean isRSPowered()
	{
		return false;
	}

	@Override
	public ActionResultType screwdriverUseSide(Direction arg0, PlayerEntity arg1, Hand arg2, Vector3d arg3) 
	{
		if(level.isClientSide)
			ImmersiveExtras.proxy.openTileScreen(IExtrasContent.GUIID_RedstonePulseCounter, this);
		
		return ActionResultType.SUCCESS;
	}

	@Override
	public boolean hammerUseSide(Direction arg0, PlayerEntity arg1, Hand arg2, Vector3d arg3) 
	{
		BlockState oldState = getState();
		BlockRotation rot = oldState.getValue(ROTATION);
		
		setRotation(rot.next());
		
		return true;
	}
	
	@Override
	public boolean canHammerRotate(Direction side, Vector3d hit, LivingEntity entity)
	{
		return false;
	}

	@Override
	public boolean canRotate(Direction axis)
	{
		return false;
	}
	
	@Override
	public Direction getFacing() 
	{
		BlockState state = getState();
		
		return state.getValue(getFacingProperty());
	}

	@Override
	public void setFacing(Direction facing) 
	{
		BlockState oldState = getState();
		BlockState newState = oldState.setValue(getFacingProperty(), facing);
		
		setState(newState);
	}
	
	public void setRotation(BlockRotation rot)
	{
		BlockState oldState = getState();
		BlockState newState = oldState.setValue(ROTATION, rot);
		
		setState(newState);
	}

	@Override
	public void onDirectionalPlacement(Direction side, float hitX, float hitY, float hitZ, LivingEntity placer) 
	{
		BlockRotation rot = BlockRotation.DEG_0;
		
		float xFromMid = hitX-.5f;
		float zFromMid = hitZ-.5f;
		float yFromMid = hitY-.5f;
			
		if (side.getAxis() == Axis.Y) // UP / DOWN
		{
			float max = Math.max(Math.abs(xFromMid), Math.abs(zFromMid));
			
			if (max == Math.abs(xFromMid))
				rot = xFromMid < 0 ? BlockRotation.DEG_270 : BlockRotation.DEG_90;
			else
				rot = zFromMid < 0 ? BlockRotation.DEG_0 : BlockRotation.DEG_180;
		}
		
		if (side.getAxis() == Axis.X) // EAST / WEST
		{
			float max = Math.max(Math.abs(yFromMid), Math.abs(zFromMid));
			
			if (max == Math.abs(zFromMid) && side.getAxisDirection() == AxisDirection.NEGATIVE)
				rot = zFromMid < 0 ? BlockRotation.DEG_270 : BlockRotation.DEG_90;
			else if (max == Math.abs(zFromMid) && side.getAxisDirection() == AxisDirection.POSITIVE)
				rot = zFromMid < 0 ? BlockRotation.DEG_90 : BlockRotation.DEG_270;
			else
				rot = yFromMid < 0 ? BlockRotation.DEG_180 : BlockRotation.DEG_0;
		}
		
		if (side.getAxis() == Axis.Z) // NORTH / SOUTH
		{
			float max = Math.max(Math.abs(xFromMid), Math.abs(yFromMid));
			
			if (max == Math.abs(xFromMid) && side.getAxisDirection() == AxisDirection.POSITIVE)
				rot = xFromMid < 0 ? BlockRotation.DEG_270 : BlockRotation.DEG_90;
			else if (max == Math.abs(xFromMid) && side.getAxisDirection() == AxisDirection.NEGATIVE)
				rot = xFromMid < 0 ? BlockRotation.DEG_90 : BlockRotation.DEG_270;
			else
				rot = yFromMid < 0 ? BlockRotation.DEG_180 : BlockRotation.DEG_0;
		}

		setRotation(rot);
	}
}

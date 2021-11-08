package lizcraft.immersiveextras.common.blocks;

import javax.annotation.Nonnull;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IRedstoneOutput;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IScrewdriverInteraction;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import lizcraft.immersiveextras.ImmersiveExtras;
import lizcraft.immersiveextras.common.IExtrasContent;
import lizcraft.immersiveextras.common.IExtrasTileTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.Property;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;

public class RedstonePulseCounterTileEntity extends IEBaseTileEntity implements ITickableTileEntity, IStateBasedDirectional, IScrewdriverInteraction, IRedstoneOutput
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

	@Override
	public Property<Direction> getFacingProperty()
	{
		return IEProperties.FACING_ALL;
	}

	@Override
	public PlacementLimitation getFacingLimitation()
	{
		return PlacementLimitation.PISTON_LIKE;
	}
	
	@Override
	public boolean mirrorFacingOnPlacement(LivingEntity placer)
	{
		return true;
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
		return from == getFacing() || from == getFacing().getOpposite();
	}
	
	@Override
	public int getRSInput(@Nonnull Direction side)
	{
		return side == getFacing() ? super.getRSInput(side) : 0;
	}

	@Override
	public int getStrongRSOutput(Direction from) 
	{
		return from == getFacing() ? countedPulses : 0;
	}

	@Override
	public int getWeakRSOutput(Direction from) 
	{
		return from == getFacing() ? countedPulses : 0;
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
	public boolean canHammerRotate(Direction side, Vector3d hit, LivingEntity entity)
	{
		return true;
	}

	@Override
	public boolean canRotate(Direction axis)
	{
		return true;
	}
}

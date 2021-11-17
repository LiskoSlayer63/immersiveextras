package lizcraft.immersiveextras.common.blocks;

import static lizcraft.immersiveextras.common.blocks.IExtrasTileBlockBase.ROTATION;

import javax.annotation.Nonnull;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.api.utils.SafeChunkUtils;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.redstone.CapabilityRedstoneNetwork;
import blusunrize.immersiveengineering.api.wires.redstone.RedstoneNetworkHandler;
import blusunrize.immersiveengineering.api.wires.redstone.CapabilityRedstoneNetwork.RedstoneBundleConnection;
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
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.Property;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public class RedstoneChannelSwitcherTileEntity extends IEBaseTileEntity implements ITickableTileEntity, IAdvancedDirectionalTile, IStateBasedDirectional, IHammerInteraction, IScrewdriverInteraction, IRedstoneOutput
{
	public static enum ControlMode
	{
		INTERNAL,
		EXTERNAL
	}
	
	public IOSideConfig ioMode = IOSideConfig.INPUT;
	public ControlMode controlMode = ControlMode.INTERNAL;
	public int redstoneChannel = 0;
	
	private NonNullList<Byte> redstoneValues = NonNullList.withSize(DyeColor.values().length, (byte)0);

	protected GlobalWireNetwork globalNet;
	private Direction oldFacing;
	private int oldInputPower;
	private int oldControlPower;
	
	public RedstoneChannelSwitcherTileEntity() 
	{
		super(IExtrasTileTypes.REDSTONE_CHANNELSWITCHER.get());
		
		this.oldFacing = getFacing();
	}
	
	private final LazyOptional<RedstoneBundleConnection> redstoneCap = registerConstantCap(
			new RedstoneBundleConnection()
			{
				@Override
				public void onChange(ConnectionPoint cp, RedstoneNetworkHandler handler, Direction side)
				{
					if(!level.isClientSide && ioMode == IOSideConfig.INPUT && SafeChunkUtils.isChunkSafe(level, worldPosition) && side == getInterfaceFace())
					{
						for (int i = 0; i < redstoneValues.size(); i++)
							redstoneValues.set(i, handler.getValue(i));
						
						setChanged();
						markContainingBlockForUpdate(getBlockState());
						level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
					}
				}
				
				@Override
				public void updateInput(byte[] signals, ConnectionPoint cp, Direction side)
				{
					if(!level.isClientSide && ioMode == IOSideConfig.OUTPUT && SafeChunkUtils.isChunkSafe(level, worldPosition) && side == getInterfaceFace())
					{
						int channel = getChannel();
						
						signals[channel] = (byte)Math.max(signals[channel], getInputPower());
					}
				}
			}
	);
	
	protected void updateAfterConfigure()
	{
		setChanged();
		
		RedstoneNetworkHandler handler = globalNet.getLocalNet(worldPosition.relative(getFacing()))
			.getHandler(RedstoneNetworkHandler.ID, RedstoneNetworkHandler.class);
				
		if (handler != null)
			handler.updateValues();
		
		this.markContainingBlockForUpdate(null);
		level.blockEvent(getBlockPos(), this.getBlockState().getBlock(), 254, 0);
		
		redstoneCap.ifPresent(RedstoneBundleConnection::markDirty);
	}
	
	protected void resetRedstone()
	{
		for (int i = 0; i < redstoneValues.size(); i++)
			redstoneValues.set(i, (byte)0);
	}
	
	protected Direction getInterfaceFace()
	{
		BlockState state = getState();
		Direction facing = state.getValue(IEProperties.FACING_ALL);
		BlockRotation rotation = state.getValue(ROTATION);
		
		switch (facing)
		{
			case NORTH:
				if (rotation == BlockRotation.DEG_0)
					return Direction.UP;
				if (rotation == BlockRotation.DEG_90)
					return Direction.WEST;
				if (rotation == BlockRotation.DEG_180)
					return Direction.DOWN;
				if (rotation == BlockRotation.DEG_270)
					return Direction.EAST;
				return null;
			case EAST:
				if (rotation == BlockRotation.DEG_0)
					return Direction.UP;
				if (rotation == BlockRotation.DEG_90)
					return Direction.NORTH;
				if (rotation == BlockRotation.DEG_180)
					return Direction.DOWN;
				if (rotation == BlockRotation.DEG_270)
					return Direction.SOUTH;
				return null;
			case SOUTH:
				if (rotation == BlockRotation.DEG_0)
					return Direction.UP;
				if (rotation == BlockRotation.DEG_90)
					return Direction.EAST;
				if (rotation == BlockRotation.DEG_180)
					return Direction.DOWN;
				if (rotation == BlockRotation.DEG_270)
					return Direction.WEST;
				return null;
			case WEST:
				if (rotation == BlockRotation.DEG_0)
					return Direction.UP;
				if (rotation == BlockRotation.DEG_90)
					return Direction.SOUTH;
				if (rotation == BlockRotation.DEG_180)
					return Direction.DOWN;
				if (rotation == BlockRotation.DEG_270)
					return Direction.NORTH;
				return null;
			case UP:
				if (rotation == BlockRotation.DEG_0)
					return Direction.NORTH;
				if (rotation == BlockRotation.DEG_90)
					return Direction.EAST;
				if (rotation == BlockRotation.DEG_180)
					return Direction.SOUTH;
				if (rotation == BlockRotation.DEG_270)
					return Direction.WEST;
				return null;
			case DOWN:
				if (rotation == BlockRotation.DEG_0)
					return Direction.NORTH;
				if (rotation == BlockRotation.DEG_90)
					return Direction.EAST;
				if (rotation == BlockRotation.DEG_180)
					return Direction.SOUTH;
				if (rotation == BlockRotation.DEG_270)
					return Direction.WEST;
				return null;
			default:
				return null;
		}
	}
	
	protected Axis getControlInputAxis()
	{
		BlockState state = getState();
		Direction facing = state.getValue(IEProperties.FACING_ALL);
		BlockRotation rotation = state.getValue(ROTATION);
		
		switch (facing)
		{
			case NORTH:
				if (rotation == BlockRotation.DEG_0 || rotation == BlockRotation.DEG_180)
					return Axis.X;
				if (rotation == BlockRotation.DEG_90 || rotation == BlockRotation.DEG_270)
					return Axis.Y;
				return null;
			case EAST:
				if (rotation == BlockRotation.DEG_0 || rotation == BlockRotation.DEG_180)
					return Axis.Z;
				if (rotation == BlockRotation.DEG_90 || rotation == BlockRotation.DEG_270)
					return Axis.Y;
				return null;
			case SOUTH:
				if (rotation == BlockRotation.DEG_0 || rotation == BlockRotation.DEG_180)
					return Axis.X;
				if (rotation == BlockRotation.DEG_90 || rotation == BlockRotation.DEG_270)
					return Axis.Y;
				return null;
			case WEST:
				if (rotation == BlockRotation.DEG_0 || rotation == BlockRotation.DEG_180)
					return Axis.Z;
				if (rotation == BlockRotation.DEG_90 || rotation == BlockRotation.DEG_270)
					return Axis.Y;
				return null;
			case UP:
				if (rotation == BlockRotation.DEG_0 || rotation == BlockRotation.DEG_180)
					return Axis.X;
				if (rotation == BlockRotation.DEG_90 || rotation == BlockRotation.DEG_270)
					return Axis.Z;
				return null;
			case DOWN:
				if (rotation == BlockRotation.DEG_0 || rotation == BlockRotation.DEG_180)
					return Axis.X;
				if (rotation == BlockRotation.DEG_90 || rotation == BlockRotation.DEG_270)
					return Axis.Z;
				return null;
			default:
				return null;
		}
	}
	
	public int getChannel()
	{
		return this.controlMode == ControlMode.INTERNAL ? this.redstoneChannel : this.getControlPower();
	}
	
	protected int getInputPower()
	{
		Direction inputFace = getInterfaceFace().getOpposite();
		
		return this.getRSInput(inputFace);
	}
	
	protected int getControlPower()
	{
		Axis controlInputAxis = getControlInputAxis();
		
		return Math.max(
				this.getRSInput(Direction.fromAxisAndDirection(controlInputAxis, AxisDirection.POSITIVE)), 
				this.getRSInput(Direction.fromAxisAndDirection(controlInputAxis, AxisDirection.NEGATIVE))
				);
	}
	
	protected boolean isRedstoneChanged()
	{
		int inputPower = getInputPower();
		int controlPower = getControlPower();
		
		if (inputPower != oldInputPower || controlPower != oldControlPower)
		{
			oldInputPower = inputPower;
			oldControlPower = controlPower;
			
			return true;
		}
		
		return false;
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
		if (oldFacing != getFacing() || isRedstoneChanged())
		{
			oldFacing = getFacing();
			
			resetRedstone();
			updateAfterConfigure();
		}
	}
	
	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket) 
	{
		this.ioMode = IOSideConfig.values()[nbt.getByte("ioMode")];
		this.controlMode = ControlMode.values()[nbt.getByte("controlMode")];
		this.redstoneChannel = nbt.getByte("redstoneChannel");
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket) 
	{
		nbt.putByte("ioMode", (byte)this.ioMode.ordinal());
		nbt.putByte("controlMode", (byte)this.controlMode.ordinal());
		nbt.putByte("redstoneChannel", (byte)this.redstoneChannel);
	}
	
	@Override
	public void receiveMessageFromClient(CompoundNBT message)
	{
		if(message.contains("ioMode"))
			this.ioMode = IOSideConfig.values()[message.getByte("ioMode")];
		
		if(message.contains("controlMode"))
			this.controlMode = ControlMode.values()[message.getByte("controlMode")];
		
		if(message.contains("redstoneChannel"))
			this.redstoneChannel = message.getByte("redstoneChannel");
		
		updateAfterConfigure();
	}
	
	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing)
	{
		if(capability == CapabilityRedstoneNetwork.REDSTONE_BUNDLE_CONNECTION && facing == getInterfaceFace())
			return redstoneCap.cast();
		
		return super.getCapability(capability, facing);
	}
	
	@Override
	public void setLevelAndPosition(@Nonnull World worldIn, @Nonnull BlockPos pos)
	{
		super.setLevelAndPosition(worldIn, pos);
		
		globalNet = GlobalWireNetwork.getNetwork(worldIn);
	}
	
	@Override
	public boolean canConnectRedstone(Direction from) 
	{
		return true;
	}
	
	@Override
	public int getRSInput(@Nonnull Direction side)
	{
		if (ioMode == IOSideConfig.OUTPUT && side == getInterfaceFace().getOpposite() || side.getAxis() == getControlInputAxis())
			return super.getRSInput(side);
		return 0;
	}

	@Override
	public int getStrongRSOutput(Direction from) 
	{
		int channel = getChannel();
		
		return ioMode == IOSideConfig.INPUT && from == getInterfaceFace() ? redstoneValues.get(channel) : 0;
	}

	@Override
	public int getWeakRSOutput(Direction from)
	{
		return getStrongRSOutput(from);
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
			ImmersiveExtras.proxy.openTileScreen(IExtrasContent.GUIID_RedstoneChannelSwitcher, this);
		
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
	public void onNeighborBlockChange(BlockPos otherPos)
	{
		super.onNeighborBlockChange(otherPos);
		
		if(isRedstoneChanged())
		{
			this.markContainingBlockForUpdate(null);
			redstoneCap.ifPresent(RedstoneBundleConnection::markDirty);
		}
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

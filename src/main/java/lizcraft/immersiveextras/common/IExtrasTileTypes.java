package lizcraft.immersiveextras.common;

import java.util.Collection;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableSet;

import lizcraft.immersiveextras.ImmersiveExtras;
import lizcraft.immersiveextras.common.blocks.AdvancedComparatorTileEntity;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class IExtrasTileTypes 
{
	public static final DeferredRegister<TileEntityType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, ImmersiveExtras.MODID);
	/*
	// Multiblocks
	public static final RegistryObject<TileEntityType<PumpjackTileEntity>> PUMP = register("pumpjack", PumpjackTileEntity::new, IPContent.Multiblock.pumpjack);
	public static final RegistryObject<TileEntityType<DistillationTowerTileEntity>> TOWER = register("distillationtower", DistillationTowerTileEntity::new, IPContent.Multiblock.distillationtower);
	public static final RegistryObject<TileEntityType<CokerUnitTileEntity>> COKER = register("cokerunit", CokerUnitTileEntity::new, IPContent.Multiblock.cokerunit);
	public static final RegistryObject<TileEntityType<HydrotreaterTileEntity>> TREATER = register("hydrotreater", HydrotreaterTileEntity::new, IPContent.Multiblock.hydrotreater);
	*/
	// Normal Blocks
	public static final RegistryObject<TileEntityType<AdvancedComparatorTileEntity>> ADVANCED_COMPARATOR = REGISTER.register("advancedcomparator", makeType(AdvancedComparatorTileEntity::new, () -> IExtrasContent.advancedComparator));
	
	private static <T extends TileEntity> Supplier<TileEntityType<T>> makeType(Supplier<T> create, Supplier<Block> valid)
	{
		return makeTypeMultipleBlocks(create, () -> ImmutableSet.of(valid.get()));
	}

	private static <T extends TileEntity> Supplier<TileEntityType<T>> makeTypeMultipleBlocks(
			Supplier<T> create,
			Supplier<Collection<Block>> valid)
	{
		return () -> new TileEntityType<>(create, ImmutableSet.copyOf(valid.get()), null);
	}
}

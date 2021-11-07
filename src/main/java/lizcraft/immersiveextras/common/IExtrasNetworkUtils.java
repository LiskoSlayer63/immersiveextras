package lizcraft.immersiveextras.common;

import java.util.Optional;
import java.util.function.Function;

import blusunrize.immersiveengineering.common.network.IMessage;
import blusunrize.immersiveengineering.common.network.MessageTileSync;
import lizcraft.immersiveextras.ImmersiveExtras;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraft.network.PacketBuffer;

public class IExtrasNetworkUtils 
{
	public static final String VERSION = "1";
	
	private static int messageId = 0;
	
	public static final SimpleChannel INSTANCE = NetworkRegistry.ChannelBuilder
			.named(new ResourceLocation(ImmersiveExtras.MODID, "main"))
			.networkProtocolVersion(() -> VERSION)
			.serverAcceptedVersions(VERSION::equals)
			.clientAcceptedVersions(VERSION::equals)
			.simpleChannel();
	
	public static void init()
	{
		registerMessage(MessageTileSync.class, MessageTileSync::new);
	}
	
	public static <T extends IMessage> void registerMessage(Class<T> packetType, Function<PacketBuffer, T> decoder)
	{
		 NetworkHandler.registerMessage(packetType, decoder, Optional.empty());
	}
	
	
	public static class NetworkHandler
	{
		private static <T extends IMessage> void registerMessage(Class<T> packetType, Function<PacketBuffer, T> decoder, Optional<NetworkDirection> direction)
		{
			INSTANCE.registerMessage(messageId++, packetType, IMessage::toBytes, decoder, (t, ctx) -> {
				t.process(ctx);
				ctx.get().setPacketHandled(true);
			}, direction);
		}
		
		public static <MSG> void sendToServer(MSG message)
		{
			INSTANCE.sendToServer(message);
		}
	}
}

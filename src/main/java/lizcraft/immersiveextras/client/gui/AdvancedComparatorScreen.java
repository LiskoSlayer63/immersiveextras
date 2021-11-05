package lizcraft.immersiveextras.client.gui;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.client.TextUtils;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.ClientTileScreen;
import blusunrize.immersiveengineering.client.gui.IEContainerScreen;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonBoolean;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonState;
import blusunrize.immersiveengineering.common.network.MessageTileSync;
import lizcraft.immersiveextras.common.blocks.AdvancedComparatorTileEntity;
import lizcraft.immersiveextras.common.blocks.AdvancedComparatorTileEntity.ComparatorMode;
import net.minecraft.client.util.InputMappings;
import net.minecraft.client.util.InputMappings.Input;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;

import static blusunrize.immersiveengineering.client.ClientUtils.mc;

import java.util.ArrayList;
import java.util.function.Consumer;

import com.mojang.blaze3d.matrix.MatrixStack;

import blusunrize.immersiveengineering.ImmersiveEngineering;

public class AdvancedComparatorScreen extends ClientTileScreen<AdvancedComparatorTileEntity> 
{
	public AdvancedComparatorScreen(AdvancedComparatorTileEntity tileEntity, ITextComponent title) 
	{
		super(tileEntity, title);
		
		this.xSize = 100;
		this.ySize = 120;
	}

	private static final ResourceLocation TEXTURE = IEContainerScreen.makeTextureLocation("redstone_configuration");

	private GuiButtonState<ComparatorMode> buttonMode;
	private GuiButtonBoolean[] colorButtons;
	
	@Override
	public void init()
	{
		super.init();
		mc().keyboardHandler.setSendRepeatsToGui(true);
		
		this.buttons.clear();

		buttonMode = new GuiButtonState<ComparatorMode>(guiLeft+41, guiTop+20, 18, 18, new StringTextComponent(""), new ComparatorMode[] { ComparatorMode.MEDIAN, ComparatorMode.SPLIT },
				tileEntity.comparatorMode.ordinal(), TEXTURE, 176, 0, 1,
				btn -> sendConfig("comparatorMode", btn.getNextState().ordinal())
		);
		
		//this.addButton(buttonMode);

		colorButtons = new GuiButtonBoolean[16];
		for(int i = 0; i < colorButtons.length; i++)
		{
			final DyeColor color = DyeColor.byId(i);
			colorButtons[i] = buildColorButton(colorButtons, guiLeft+22+(i%4*14), guiTop+44+(i/4*14),
					tileEntity.redstoneColors.get(i), color, btn -> sendColorConfig(color.getId(), btn.getNextState().booleanValue()));
			this.addButton(colorButtons[i]);
		}
	}
	
	public void sendConfig(String key, int value)
	{
		CompoundNBT message = new CompoundNBT();
		message.putInt(key, value);
		ImmersiveEngineering.packetHandler.sendToServer(new MessageTileSync(tileEntity, message));
	}
	
	public void sendColorConfig(int color, boolean value)
	{
		CompoundNBT message = new CompoundNBT();
		message.putInt("redstoneColor", color);
		message.putBoolean("redstoneValue", value);
		ImmersiveEngineering.packetHandler.sendToServer(new MessageTileSync(tileEntity, message));
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack transform, int mouseX, int mouseY, float partialTick)
	{

	}

	@Override
	protected void drawGuiContainerForegroundLayer(MatrixStack transform, int mouseX, int mouseY, float partialTick)
	{
		ArrayList<ITextComponent> tooltip = new ArrayList<>();

		if(buttonMode.isHovered())
		{
			tooltip.add(new StringTextComponent("Comparator Mode"));
			tooltip.add(TextUtils.applyFormat(
					new StringTextComponent(buttonMode.getState().name()),
					TextFormatting.GRAY
			));
		}

		for(int i = 0; i < colorButtons.length; i++)
			if(colorButtons[i].isHovered())
			{
				tooltip.add(new TranslationTextComponent(Lib.GUI_CONFIG+"redstone_color"));
				tooltip.add(TextUtils.applyFormat(
						new TranslationTextComponent("color.minecraft."+DyeColor.byId(i).getName()),
						TextFormatting.GRAY
				));
			}

		if(!tooltip.isEmpty())
			GuiUtils.drawHoveringText(transform, tooltip, mouseX, mouseY, width, height, -1, font);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers)
	{
		Input mouseKey = InputMappings.getKey(keyCode, scanCode);
		
		if(mc().options.keyInventory.isActiveAndMatches(mouseKey))
		{
			this.onClose();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	public static GuiButtonBoolean buildColorButton(GuiButtonBoolean[] buttons, int posX, int posY, boolean active, DyeColor color, Consumer<GuiButtonBoolean> onClick)
	{
		return new GuiButtonBoolean(posX, posY, 12, 12, "", active,
				TEXTURE, 194, 0, 1,
				btn -> {
					onClick.accept((GuiButtonBoolean)btn);
				})
		{
			@Override
			protected boolean isValidClickButton(int button)
			{
				return true;
			}

			@Override
			public void render(MatrixStack transform, int mouseX, int mouseY, float partialTicks)
			{
				super.render(transform, mouseX, mouseY, partialTicks);
				if(this.visible)
				{
					int col = color.getColorValue();
					if(!getState())
						col = ClientUtils.getDarkenedTextColour(col);
					col = 0xff000000|col;
					this.fillGradient(transform, x+3, y+3, x+9, y+9, col, col);
				}
			}
		};
	}
}

package lizcraft.immersiveextras.client.gui;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.client.TextUtils;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonBoolean;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonState;
import lizcraft.immersiveextras.common.blocks.AdvancedComparatorTileEntity;
import lizcraft.immersiveextras.common.blocks.AdvancedComparatorTileEntity.ComparatorMode;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;

import java.util.ArrayList;
import java.util.function.Consumer;

import com.mojang.blaze3d.matrix.MatrixStack;

public class AdvancedComparatorScreen extends IExtrasClientTileScreen<AdvancedComparatorTileEntity> 
{
	public AdvancedComparatorScreen(AdvancedComparatorTileEntity tileEntity, ITextComponent title) 
	{
		super("advanced_comparator", tileEntity, title);
		
		this.xSize = 100;
		this.ySize = 120;
	}

	private GuiButtonState<ComparatorMode> buttonMode;
	private GuiButtonBoolean[] colorButtons;
	
	@Override
	public void init()
	{
		super.init();
		
		this.buttons.clear();

		buttonMode = new GuiButtonState<ComparatorMode>(guiLeft+41, guiTop+20, 18, 18, new StringTextComponent(""), new ComparatorMode[] { ComparatorMode.AVERAGE, ComparatorMode.SUM },
				tileEntity.comparatorMode.ordinal(), TEXTURE, 0, 0, 1,
				btn -> sendConfig("comparatorMode", btn.getNextState().ordinal())
		);
		
		this.addButton(buttonMode);

		colorButtons = new GuiButtonBoolean[16];
		for(int i = 0; i < colorButtons.length; i++)
		{
			final DyeColor color = DyeColor.byId(i);
			colorButtons[i] = buildColorButton(colorButtons, guiLeft+22+(i%4*14), guiTop+44+(i/4*14),
					tileEntity.redstoneColors.get(i), color, btn -> sendColorConfig(color.getId(), btn.getNextState().booleanValue()));
			this.addButton(colorButtons[i]);
		}
	}
	
	public void sendColorConfig(int color, boolean value)
	{
		CompoundNBT message = new CompoundNBT();
		message.putByte("redstoneColor", (byte)color);
		message.putBoolean("redstoneValue", value);
		sendConfig(message);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(MatrixStack transform, int mouseX, int mouseY, float partialTick)
	{
		ArrayList<ITextComponent> tooltip = new ArrayList<>();

		if(buttonMode.isHovered())
		{
			tooltip.add(getTranslationComponent("mode.title"));
			tooltip.add(TextUtils.applyFormat(
					getTranslationComponent("mode." + buttonMode.getState().name() + ".name"),
					TextFormatting.GRAY
			));
			tooltip.add(TextUtils.applyFormat(
					getTranslationComponent("mode." + buttonMode.getState().name() + ".desc"),
					TextFormatting.GRAY, TextFormatting.ITALIC
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

	public static GuiButtonBoolean buildColorButton(GuiButtonBoolean[] buttons, int posX, int posY, boolean active, DyeColor color, Consumer<GuiButtonBoolean> onClick)
	{
		return new GuiButtonBoolean(posX, posY, 12, 12, "", active,
				TEXTURE_IE, 194, 0, 1,
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

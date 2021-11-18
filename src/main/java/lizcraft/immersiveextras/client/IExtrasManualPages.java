package lizcraft.immersiveextras.client;

import blusunrize.immersiveengineering.api.ManualHelper;
import blusunrize.lib.manual.ManualEntry;
import blusunrize.lib.manual.ManualInstance;
import blusunrize.lib.manual.Tree.InnerNode;
import lizcraft.immersiveextras.common.IExtrasContent;
import net.minecraft.util.ResourceLocation;

public class IExtrasManualPages 
{
	/** ImmersiveExtras's Manual Category */
	private static InnerNode<ResourceLocation, ManualEntry> IEXTRAS_CATEGORY;
	
	public void setupManualPages(){
		ManualInstance man = ManualHelper.getManual();
		
		IEXTRAS_CATEGORY = man.getRoot().getOrCreateSubnode(getLoc("main"), 100);
		
		man.addEntry(IEXTRAS_CATEGORY, getLoc("asphalt"), 5);
	}
	
	protected static ResourceLocation getLoc(String name) 
	{ 
		return IExtrasContent.getResourceLocation(name); 
	}
}

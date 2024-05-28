package pro.mikey.xray;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.IExtensionPoint;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(XRay.MOD_ID)
public class XRay {
	public static final String MOD_ID = "xray";
	public static final String PREFIX_GUI = String.format("%s:textures/gui/", MOD_ID);

	public static Logger logger = LogManager.getLogger();

	public XRay(IEventBus bus) {
		if (!FMLEnvironment.dist.isClient()) {
			return;
		}
		ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> "", (c, b) -> true));
		ClientController.setup(bus);
	}
}

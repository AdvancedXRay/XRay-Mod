package pro.mikey.xray;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pro.mikey.xray.keybinding.KeyBindings;
import pro.mikey.xray.xray.Events;
import pro.mikey.xray.xray.Render;

@Mod(XRay.MOD_ID)
public class XRay {
	public static final String MOD_ID = "xray";
	public static final String PREFIX_GUI = String.format("%s:textures/gui/", MOD_ID);

	public static Logger logger = LogManager.getLogger();

	public XRay(IEventBus eventBus) {
		if (!FMLEnvironment.dist.isClient()) {
			return;
		}

		ModLoadingContext.get().getActiveContainer()
				.registerConfig(ModConfig.Type.CLIENT, Configuration.SPEC);

		eventBus.addListener(ClientController::onSetup);
		eventBus.addListener(KeyBindings::registerKeyBinding);
		eventBus.addListener(this::registerPipeline);

		// Keybindings
		NeoForge.EVENT_BUS.addListener(KeyBindings::eventInput);
		NeoForge.EVENT_BUS.addListener(ClientController::onGameJoin);

		NeoForge.EVENT_BUS.addListener(Events::tickEnd);
		NeoForge.EVENT_BUS.addListener(Events::onWorldRenderLast);
	}

	private void registerPipeline(RegisterRenderPipelinesEvent event) {
		event.registerPipeline(Render.LINES_NO_DEPTH);
	}
}

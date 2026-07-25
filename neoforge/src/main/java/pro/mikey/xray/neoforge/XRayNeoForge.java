package pro.mikey.xray.neoforge;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pro.mikey.xray.ClientController;
import pro.mikey.xray.XRay;
import pro.mikey.xray.core.OutlineRender;
import pro.mikey.xray.screens.HudOverlay;
import pro.mikey.xray.core.ScanController;

@Mod(XRay.MOD_ID)
public class XRayNeoForge {
	private static final Identifier GUI_LAYER_ID = XRay.id("xray_overlay");

	public static final Logger LOGGER = LogManager.getLogger();

	public XRayNeoForge(IEventBus eventBus) {
		if (!FMLEnvironment.getDist().isClient()) {
			return;
		}

		XRay.INSTANCE.init();

		eventBus.addListener(this::registerKeyBinding);
		eventBus.addListener(this::registerPipeline);
		NeoForge.EVENT_BUS.addListener(this::eventInput);
		NeoForge.EVENT_BUS.addListener(this::tickEnd);
		eventBus.addListener(this::onClientSetup);

		NeoForge.EVENT_BUS.addListener(this::onWorldRenderLast);
		eventBus.addListener(this::registerRenderPipeline);
		eventBus.addListener(this::registerGuiLayer);
	}

	private void registerGuiLayer(RegisterGuiLayersEvent event) {
		event.registerAboveAll(GUI_LAYER_ID, (guiGraphics, tickCounter) -> HudOverlay.renderGameOverlayEvent(guiGraphics));
	}

	private void onWorldRenderLast(RenderLevelStageEvent.AfterWeather event) {
		OutlineRender.renderBlocks();
	}

	private void registerRenderPipeline(RegisterRenderPipelinesEvent event) {
		event.registerPipeline(OutlineRender.NO_DEPTH_LINES_PIPELINE);
	}

	public void onClientSetup(FMLClientSetupEvent event) {
		ClientController.onSetup();

		if (ModList.get().isLoaded("iris")) {
			try {
				IrisCompat.register();
				LOGGER.info("Registered X-Ray outline pipeline with Iris's shader compatibility layer");
			} catch (Throwable t) {
				LOGGER.warn("Failed to register Iris shader compatibility - outlines may not render correctly with a shader pack active", t);
			}
		}
	}

	public void registerKeyBinding(RegisterKeyMappingsEvent event) {
		event.register(XRay.TOGGLE_KEY);
		event.register(XRay.OPEN_GUI_KEY);
	}

	public void registerPipeline(RegisterRenderPipelinesEvent event) {
	}

	public void eventInput(InputEvent.Key event) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || Minecraft.getInstance().gui.screen() != null || Minecraft.getInstance().level == null)
			return;

		if (XRay.TOGGLE_KEY.consumeClick()) {
			XRay.INSTANCE.onToggleKeyPressed();
		}

		if (XRay.OPEN_GUI_KEY.consumeClick()) {
			XRay.INSTANCE.onOpenGuiKeyPressed();
		}
	}

	public void tickEnd(ClientTickEvent.Post event) {
		if (Minecraft.getInstance().player != null && Minecraft.getInstance().level != null) {
			ScanController.INSTANCE.requestBlockFinder(false);
		}
	}
}

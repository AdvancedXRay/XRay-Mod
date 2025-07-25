package pro.mikey.xray.neoforge;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pro.mikey.xray.ClientController;
import pro.mikey.xray.XRay;
import pro.mikey.xray.screens.HudOverlay;
import pro.mikey.xray.core.ScanController;
import pro.mikey.xray.core.OutlineRender;

@Mod(XRay.MOD_ID)
public class XRayNeoForge {
	private static final ResourceLocation GUI_LAYER_ID = XRay.id("xray_overlay");

	public static final Logger LOGGER = LogManager.getLogger();

	public XRayNeoForge(IEventBus eventBus) {
		if (!FMLEnvironment.dist.isClient()) {
			return;
		}

		XRay.INSTANCE.init();

		eventBus.addListener(this::registerKeyBinding);
		eventBus.addListener(this::registerPipeline);
		NeoForge.EVENT_BUS.addListener(this::eventInput);
		NeoForge.EVENT_BUS.addListener(this::tickEnd);
		eventBus.addListener(this::onClientSetup);

		NeoForge.EVENT_BUS.addListener(this::onWorldRenderLast);
		eventBus.addListener(this::registerGuiLayer);
	}

	private void registerGuiLayer(RegisterGuiLayersEvent event) {
		event.registerAboveAll(GUI_LAYER_ID, (guiGraphics, tickCounter) -> HudOverlay.renderGameOverlayEvent(guiGraphics));
	}

	private void onWorldRenderLast(RenderLevelStageEvent.AfterWeather event) {
		OutlineRender.renderBlocks(event.getPoseStack());
	}

	public void onClientSetup(FMLClientSetupEvent event) {

		ClientController.onSetup();
	}

	public void registerKeyBinding(RegisterKeyMappingsEvent event) {
		event.register(XRay.TOGGLE_KEY);
		event.register(XRay.OPEN_GUI_KEY);
	}

	public void registerPipeline(RegisterRenderPipelinesEvent event) {
		event.registerPipeline(OutlineRender.LINES_NO_DEPTH);
	}

	public void eventInput(InputEvent.Key event) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || Minecraft.getInstance().screen != null || Minecraft.getInstance().level == null)
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

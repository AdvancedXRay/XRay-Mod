package pro.mikey.xray;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import pro.mikey.xray.compatability.IrisCompat;
import pro.mikey.xray.screens.ScanManageScreen;
import pro.mikey.xray.utils.XPlatShim;
import pro.mikey.xray.core.ScanController;

import java.util.ServiceLoader;

public enum XRay {
	INSTANCE;

	public static final String MOD_ID = "xray";
	private static final Logger LOGGER = LogManager.getLogger();

	public static final XPlatShim XPLAT = ServiceLoader.load(XPlatShim.class).findFirst().orElseThrow();

    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(XRay.id("category"));
	public static final KeyMapping TOGGLE_KEY = new KeyMapping(I18n.get("xray.config.toggle"), GLFW.GLFW_KEY_BACKSLASH, CATEGORY);
	public static final KeyMapping OPEN_GUI_KEY = new KeyMapping(I18n.get("xray.config.open"), GLFW.GLFW_KEY_G, CATEGORY);

	public void init() {
        if (XPLAT.isModLoaded("iris")) {
            LOGGER.info("Iris detected, enabling compatibility features.");
            IrisCompat.init();
        }
	}

	public void onToggleKeyPressed() {
		if (minecraftNotReady()) {
			LOGGER.warn("Cannot toggle X-Ray, Minecraft is not ready.");
			return;
		}

		ScanController.INSTANCE.toggleXRay();
	}

	public void onOpenGuiKeyPressed() {
		if (minecraftNotReady()) {
			LOGGER.warn("Cannot open GUI, Minecraft is not ready.");
			return;
		}

		Minecraft.getInstance().setScreen(new ScanManageScreen());
	}

	private boolean minecraftNotReady() {
		Minecraft mc = Minecraft.getInstance();

		return mc.player == null || Minecraft.getInstance().screen != null || Minecraft.getInstance().level == null;
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	public static Identifier assetLocation(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, "textures/" + path);
	}

	public static Configuration config() {
		return Configuration.INSTANCE;
	}
}

package pro.mikey.xray;

import net.minecraft.client.resources.language.I18n;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.mikey.xray.core.ScanController;

public class ClientController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientController.class);

    public static void onSetup() {
        LOGGER.debug(I18n.get("xray.debug.init"));
        XRay.config().load();

        ScanController.INSTANCE.init();
    }
}

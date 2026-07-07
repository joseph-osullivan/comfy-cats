package com.comfycats;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComfyCats implements ModInitializer {
	public static final String MOD_ID = "comfycats";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// All behaviour is added by CatMixin, which injects a daytime
		// "lie on a cushion" goal into every cat's AI. Nothing to do here.
		LOGGER.info("Comfy Cats loaded - cats will now nap on cushions during the day.");
	}
}

package one.oth3r.sit;

import net.fabricmc.api.ModInitializer;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Sit implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("sit");
	public static final String ENTITY_NAME = "-sit!-entity-";
	public static MinecraftServer server;
	public static CommandManager commandManager;
	public static boolean isClient = true;

	@Override
	public void onInitialize() {
		//todo future:
		// make it so it updates the sitting height and pos based on the block so if it changed while offline it still works (or if stair changes shape)
		// inner stair offset & custom support for that ig
		config.load();
		Events.register();
	}
	public static MutableText lang(String key, Object... args) {
		if (isClient) return Text.translatable(key, args);
		else return LangReader.of(key, args).getTxT();
	}
}
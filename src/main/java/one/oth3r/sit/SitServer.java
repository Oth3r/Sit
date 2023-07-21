package one.oth3r.sit;

import net.fabricmc.api.DedicatedServerModInitializer;

public class SitServer implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        Sit.isClient = false;
        config.load();
        LangReader.loadLanguageFile();
    }
}

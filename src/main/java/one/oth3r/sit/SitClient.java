package one.oth3r.sit;

import net.fabricmc.api.ClientModInitializer;
import one.oth3r.sit.utl.Data;
import one.oth3r.sit.utl.Events;

public class SitClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Data.setClient(true);
        Events.registerClient();
    }


}

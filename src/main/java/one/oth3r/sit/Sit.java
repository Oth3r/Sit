package one.oth3r.sit;

import net.fabricmc.api.ModInitializer;

import one.oth3r.sit.file.FileData;
import one.oth3r.sit.utl.Events;

public class Sit implements ModInitializer {

	@Override
	public void onInitialize() {
		FileData.loadFiles();
		// save the files to populate all missing config options
		FileData.saveFiles();
		Events.registerCommon();
	}
}
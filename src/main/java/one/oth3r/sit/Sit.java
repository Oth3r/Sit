package one.oth3r.sit;

import net.fabricmc.api.ModInitializer;

import one.oth3r.otterlib.file.LanguageReader;
import one.oth3r.otterlib.file.ResourceReader;
import one.oth3r.otterlib.registry.CustomFileReg;
import one.oth3r.otterlib.registry.LanguageReg;
import one.oth3r.sit.file.FileData;
import one.oth3r.sit.file.ServerConfig;
import one.oth3r.sit.file.SittingConfig;
import one.oth3r.sit.utl.Data;
import one.oth3r.sit.utl.Events;

public class Sit implements ModInitializer {

	@Override
	public void onInitialize() {
        LanguageReg.registerLang(Data.MOD_ID,  new LanguageReader(
                new ResourceReader("assets/sit-oth3r/lang/",Sit.class.getClassLoader()),
                new ResourceReader(Data.CONFIG_DIR),"en_us","en_us"));

        /// autoload is off, we will handle loading and saving manually
        CustomFileReg.registerFile(Data.MOD_ID,new CustomFileReg.FileEntry(
                ServerConfig.ID,new ServerConfig(),false,false));
        CustomFileReg.registerFile(Data.MOD_ID,new CustomFileReg.FileEntry(
                SittingConfig.ID,new SittingConfig(),false,false));


		FileData.loadFiles();
		// save the files to populate all missing config options
		FileData.saveFiles();
		Events.registerCommon();
	}
}
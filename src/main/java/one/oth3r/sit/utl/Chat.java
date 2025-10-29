package one.oth3r.sit.utl;

import one.oth3r.otterlib.chat.CTxT;
import one.oth3r.otterlib.registry.LanguageReg;

import java.awt.*;

public class Chat {
    public static CTxT tag() {
        return new CTxT("Sit!").btn(true).color(Color.decode("#c400ff")).append(" ");
    }

    public static CTxT lang(String key, Object... args) {
        return LanguageReg.getLang(Data.MOD_ID).dynamicTranslatable(key, args);
    }
}

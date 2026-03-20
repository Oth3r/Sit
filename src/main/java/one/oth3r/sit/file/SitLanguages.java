package one.oth3r.sit.file;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum SitLanguages {
    en_us,
    it_it,
    pt_br,
    tr_tr,
    zh_tw,
    zh_cn,
    de_de;

    SitLanguages() {}

    public static String getList() {
        return Arrays.stream(values()).map(Enum::toString).collect(Collectors.joining(","));
    }
}

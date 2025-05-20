package one.oth3r.sit.file;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import one.oth3r.otterlib.chat.CTxT;
import one.oth3r.sit.Sit;
import one.oth3r.sit.utl.Data;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LangReader {
    private static final Map<String, String> defaultLangMap = new HashMap<>();
    private static final Map<String, String> languageMap = new HashMap<>();

    private final String translationKey;

    private final Object[] placeholders;

    public LangReader(String translationKey, Object... placeholders) {
        this.translationKey = translationKey;
        this.placeholders = placeholders;
    }

    public CTxT getTxT() {
        String translated = getLanguageValue(translationKey);
        if (placeholders != null && placeholders.length > 0) {
            //removed all double \\ and replaces with \
            translated = translated.replaceAll("\\\\\\\\", "\\\\");
            String regex = "%\\d*\\$?[dfs]";
            Matcher anyMatch = Pattern.compile(regex).matcher(translated);
            Matcher endMatch = Pattern.compile(regex+"$").matcher(translated);

            // Arraylist with all the %(#$)[dfs]
            ArrayList<String> matches = new ArrayList<>();
            while (anyMatch.find()) {
                String match = anyMatch.group();
                matches.add(match);
            }
            //SPLITS the text at each regex and remove the regex
            String[] parts = translated.split(regex);
            //if the last element of the array ends with regex, remove it and add an empty string to the end of the array
            if (endMatch.find()) {
                String[] newParts = Arrays.copyOf(parts, parts.length + 1);
                newParts[parts.length] = "";
                parts = newParts;
            }
            //if there are placeholders specified, and the split is more than 1, it will replace %(dfs) with the placeholder objects
            if (parts.length > 1) {
                CTxT txt = new CTxT("");
                int i = 0;
                for (String match : matches) {
                    int get = i;
                    //if the match is numbered, change GET to the number it wants
                    if (match.contains("$")) {
                        match = match.substring(1,match.indexOf('$'));
                        get = Integer.parseInt(match)-1;
                    }
                    if (parts.length != i) txt.append(parts[i]);
                    //convert the obj into txt
                    txt.append(getTxTFromObj(placeholders[get]));
                    i++;
                }
                if (parts.length != i) txt.append(parts[i]);
                return new CTxT(txt);
            }
        }
        return new CTxT(translated);
    }

    private CTxT getTxTFromObj(Object obj) {
        if (obj instanceof CTxT) return (((CTxT) obj));
        else if (obj instanceof Text) return new CTxT((MutableText) obj);
        else return new CTxT(String.valueOf(obj));
    }

    public static LangReader of(String translationKey, Object... placeholders) {
        return new LangReader(translationKey, placeholders);
    }

    public static void loadLanguageFile() {
        Type tToken = new TypeToken<Map<String, String>>(){}.getType();
        try {
            // load the config language
            Reader selectionReader = new InputStreamReader(getInputStream(false), StandardCharsets.UTF_8);
            languageMap.putAll(new Gson().fromJson(selectionReader, tToken));
            // load the default language as well (fallback)
            Reader defaultReader = new InputStreamReader(getInputStream(true), StandardCharsets.UTF_8);
            defaultLangMap.putAll(new Gson().fromJson(defaultReader, tToken));
        } catch (Exception e) {
            Data.LOGGER.info("ERROR WITH LANGUAGE FILE - PLEASE REPORT WITH THE ERROR LOG");
            Data.LOGGER.info(e.getMessage());
        }
    }

    private static InputStream getInputStream(boolean english) {
        ClassLoader classLoader = Sit.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("assets/sit-oth3r/lang/"+FileData.getServerConfig().getLang()+".json");
        // make null if english
        if (english) inputStream = null;

        // if it cant read (null), try again, but with the english file
        if (inputStream == null) inputStream = classLoader.getResourceAsStream("assets/sit-oth3r/lang/"+new ServerConfig().getLang()+".json");

        // if null after that, throw an exception
        if (inputStream == null) throw new IllegalArgumentException("CANT LOAD THE LANGUAGE FILE. SIT! WILL BREAK.");
        return inputStream;
    }

    public static String getLanguageValue(String key) {
        return languageMap.getOrDefault(key, defaultLangMap.getOrDefault(key, key));
    }
}
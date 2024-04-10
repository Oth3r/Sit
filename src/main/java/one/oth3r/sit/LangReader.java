package one.oth3r.sit;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import one.oth3r.sit.file.Config;

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
    private static final Map<String, String> languageMap = new HashMap<>();
    private final String translationKey;
    private final Object[] placeholders;
    public LangReader(String translationKey, Object... placeholders) {
        this.translationKey = translationKey;
        this.placeholders = placeholders;
    }
    public MutableText getTxT() {
        String translated = getLanguageValue(translationKey);
        if (placeholders != null && placeholders.length > 0) {
            //removed all double \\ and replaces with \
            translated = translated.replaceAll("\\\\\\\\", "\\\\");
            String regex = "%\\d*\\$?[dfs]";
            Matcher anyMatch = Pattern.compile(regex).matcher(translated);
            Matcher endMatch = Pattern.compile(regex+"$").matcher(translated);
            //Arraylist with all the %(#$)[dfs]
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
                MutableText txt = Text.empty();
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
                    Object obj = placeholders[get];
                    if (obj instanceof Text) txt.append((Text) obj);
                    else txt.append(String.valueOf(obj));
                    i++;
                }
                if (parts.length != i) txt.append(parts[i]);
                return txt;
            }
        }
        return Text.empty().append(translated);
    }
    public static LangReader of(String translationKey, Object... placeholders) {
        return new LangReader(translationKey, placeholders);
    }
    public static void loadLanguageFile() {
        try {
            ClassLoader classLoader = Sit.class.getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream("assets/sit/lang/"+ Config.lang+".json");
            if (inputStream == null) {
                inputStream = classLoader.getResourceAsStream("assets/sit/lang/"+ Config.defaults.lang+".json");
                Config.lang = Config.defaults.lang;
            }
            if (inputStream == null) throw new IllegalArgumentException("CANT LOAD THE LANGUAGE FILE. DIRECTIONHUD WILL BREAK.");
            Type type = new TypeToken<Map<String, String>>(){}.getType();
            Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            languageMap.putAll(new Gson().fromJson(reader, type));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static String getLanguageValue(String key) {
        return languageMap.getOrDefault(key, key);
    }
}
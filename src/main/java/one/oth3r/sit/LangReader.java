package one.oth3r.sit;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;

import java.io.InputStream;
import java.util.*;
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
            InputStream inputStream = classLoader.getResourceAsStream("assets/sit/lang/"+ config.lang+".json");
            if (inputStream == null) {
                inputStream = classLoader.getResourceAsStream("assets/sit/lang/"+config.defaults.lang+".json");
                config.lang = config.defaults.lang;
            }
            if (inputStream == null) throw new IllegalArgumentException("CANT LOAD THE LANGUAGE FILE. DIRECTIONHUD WILL BREAK.");
            Scanner scanner = new Scanner(inputStream);
            String currentLine;
            while (scanner.hasNextLine()) {
                currentLine = scanner.nextLine().trim();
                if (currentLine.startsWith("{") || currentLine.startsWith("}")) {
                    continue;
                }
                String[] keyValue = currentLine.split(":", 2);
                String key = keyValue[0].trim();
                key = key.substring(1,key.length()-1).replace("\\","");
                String value = keyValue[1].trim();
                if (value.endsWith(",")) value = value.substring(0, value.length() - 1);
                value = value.substring(1,value.length()-1).replace("\\","");
                languageMap.put(key, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static String getLanguageValue(String key) {
        return languageMap.getOrDefault(key, key);
    }
}
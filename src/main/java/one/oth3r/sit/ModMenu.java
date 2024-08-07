package one.oth3r.sit;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import one.oth3r.sit.file.Config;

public class ModMenu implements ModMenuApi {

    private static MutableText lang(String key) {
        return Text.translatable("config.sit."+key);
    }

    private static MutableText lang(String key, Object... args) {
        return Text.translatable("config.sit."+key,args);
    }

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        // return null if YACL isn't installed to not throw an error
        if (!yaclCheck()) return screen -> null;
        return parent -> YetAnotherConfigLib.createBuilder().save(() -> {
            // save and load to get rid of bad data
            Config.save();
            Config.load();
        })
                .title(Text.of("Sit!"))
                .category(ConfigCategory.createBuilder()
                        .name(lang("category.general"))
                        .tooltip(lang("category.general.tooltip"))
                        .option(Option.<Boolean>createBuilder()
                                .name(lang("general.keep_active"))
                                .description(OptionDescription.of(lang("general.keep_active.description")))
                                .binding(Config.defaults.keepActive, () -> Config.keepActive, n -> Config.keepActive = n)
                                .controller(opt -> BooleanControllerBuilder.create(opt).trueFalseFormatter())
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(lang("general.sit_while_seated"))
                                .description(OptionDescription.of(lang("general.sit_while_seated.description")))
                                .binding(Config.defaults.sitWhileSeated, () -> Config.sitWhileSeated, n -> Config.sitWhileSeated = n)
                                .controller(opt -> BooleanControllerBuilder.create(opt).trueFalseFormatter())
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.of("Sitting with Hand"))
                                .description(OptionDescription.of(Text.of("Toggles the player's ability to sit with their hand.")))
                                .binding(Config.defaults.handSitting,()-> Config.handSitting, n -> Config.handSitting = n)
                                .controller(opt -> BooleanControllerBuilder.create(opt).trueFalseFormatter())
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(lang("general.sittable"))
                                .description(OptionDescription.of(lang("general.sittable.description")))
                                .option(Option.<Boolean>createBuilder()
                                        .name(lang("general.sittable.stairs"))
                                        .binding(Config.defaults.stairsOn, () -> Config.stairsOn, n -> Config.stairsOn = n)
                                        .controller(opt -> BooleanControllerBuilder.create(opt).onOffFormatter())
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(lang("general.sittable.slabs"))
                                        .binding(Config.defaults.slabsOn, () -> Config.slabsOn, n -> Config.slabsOn = n)
                                        .controller(opt -> BooleanControllerBuilder.create(opt).onOffFormatter())
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(lang("general.sittable.carpets"))
                                        .binding(Config.defaults.carpetsOn, () -> Config.carpetsOn, n -> Config.carpetsOn = n)
                                        .controller(opt -> BooleanControllerBuilder.create(opt).onOffFormatter())
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(lang("general.sittable.full_blocks"))
                                        .binding(Config.defaults.fullBlocksOn, () -> Config.fullBlocksOn, n -> Config.fullBlocksOn = n)
                                        .controller(opt -> BooleanControllerBuilder.create(opt).onOffFormatter())
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(lang("general.sittable.custom"))
                                        .description(OptionDescription.of(lang("general.sittable.custom.description")))
                                        .binding(Config.defaults.customOn, () -> Config.customOn, n -> Config.customOn = n)
                                        .controller(opt -> BooleanControllerBuilder.create(opt).onOffFormatter())
                                        .build())
                                .build())
                        .group(ListOption.<String>createBuilder()
                                .name(lang("general.sittable_blocks"))
                                .description(OptionDescription.of(
                                        lang("general.sittable_blocks.description")
                                                .append("\n\n").append(lang("general.sittable_blocks.description_2",
                                                        Text.literal("\"")
                                                                .append(Text.literal("minecraft:campfire").styled(style -> style.withColor(TextColor.fromFormatting(Formatting.AQUA))))
                                                                .append("|")
                                                                .append(Text.literal("0.255").styled(style -> style.withColor(TextColor.fromFormatting(Formatting.RED))))
                                                                .append("|")
                                                                .append(Text.literal("1").styled(style -> style.withColor(TextColor.fromFormatting(Formatting.GREEN))))
                                                                .append("|")
                                                                .append(Text.literal("lit=false").styled(style -> style.withColor(TextColor.fromFormatting(Formatting.GOLD))))
                                                                .append("\"").styled(style -> style.withItalic(true).withColor(TextColor.fromFormatting(Formatting.GRAY)))))
                                                .append("\n\n").append(lang("general.sittable_blocks.description_4").styled(style -> style.withColor(TextColor.fromFormatting(Formatting.AQUA))))
                                                .append("\n").append(lang("general.sittable_blocks.description_5").styled(style -> style.withColor(TextColor.fromFormatting(Formatting.RED))))
                                                .append("\n").append(lang("general.sittable_blocks.description_6").styled(style -> style.withColor(TextColor.fromFormatting(Formatting.GREEN))))
                                                .append("\n").append(lang("general.sittable_blocks.description_7").styled(style -> style.withColor(TextColor.fromFormatting(Formatting.GOLD))))
                                                .append("\n\n").append(lang("general.sittable_blocks.description_8").styled(style -> style.withColor(TextColor.fromFormatting(Formatting.YELLOW))))))
                                .binding(Config.defaults.customBlocks, () -> Config.customBlocks, n -> Config.customBlocks = n)
                                .controller(StringControllerBuilder::create)
                                .initial("")
                                .build())
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(lang("category.main_hand"))
                        .tooltip(lang("category.main_hand.tooltip"))
                        .option(Option.<Config.HandRequirement>createBuilder()
                                .name(lang("hand.requirements"))
                                .description(OptionDescription.of(lang("hand.requirements.description")
                                        .append("\n\n").append(lang("hand.requirements.description_2").styled(style -> style.withColor(TextColor.fromFormatting(Formatting.AQUA))))
                                        .append("\n").append(lang("hand.requirements.description_3").styled(style -> style.withColor(TextColor.fromFormatting(Formatting.GREEN))))
                                        .append("\n").append(lang("hand.requirements.description_4").styled(style -> style.withColor(TextColor.fromFormatting(Formatting.RED))))))
                                .binding(Config.defaults.mainReq, () -> Config.mainReq, n -> Config.mainReq = n)
                                .controller(opt -> EnumControllerBuilder.create(opt).enumClass(Config.HandRequirement.class)
                                        .formatValue(v -> Text.translatable("config.sit."+v.name().toLowerCase())))
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(lang("hand.restrictions"))
                                .description(OptionDescription.of(lang("hand.restrictions.description")))
                                .option(Option.<Boolean>createBuilder()
                                        .name(lang("hand.restrictions.blocks"))
                                        .binding(Config.defaults.mainBlock,()-> Config.mainBlock, n -> Config.mainBlock = n)
                                        .controller(opt -> BooleanControllerBuilder.create(opt).trueFalseFormatter())
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(lang("hand.restrictions.food"))
                                        .binding(Config.defaults.mainFood,()-> Config.mainFood, n -> Config.mainFood = n)
                                        .controller(opt -> BooleanControllerBuilder.create(opt).trueFalseFormatter())
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(lang("hand.restrictions.usable"))
                                        .description(OptionDescription.of(lang("hand.restrictions.usable.description")))
                                        .binding(Config.defaults.mainUsable,()-> Config.mainUsable, n -> Config.mainUsable = n)
                                        .controller(opt -> BooleanControllerBuilder.create(opt).trueFalseFormatter())
                                        .build())
                                .build())
                        .group(ListOption.<String>createBuilder()
                                .name(lang("hand.whitelist"))
                                .description(OptionDescription.of(lang("hand.whitelist.description")
                                        .append("\n\n").append(lang("hand.list.description"))
                                        .append(lang("hand.list.description_2").styled(style -> style.withItalic(true).withColor(TextColor.fromFormatting(Formatting.GRAY))))))
                                .binding(Config.defaults.mainWhitelist, () -> Config.mainWhitelist, n -> Config.mainWhitelist = n)
                                .controller(StringControllerBuilder::create)
                                .initial("")
                                .build())
                        .group(ListOption.<String>createBuilder()
                                .name(lang("hand.blacklist"))
                                .description(OptionDescription.of(lang("hand.blacklist.description")
                                        .append("\n\n").append(lang("hand.list.description"))
                                        .append(lang("hand.list.description_2").styled(style -> style.withItalic(true).withColor(TextColor.fromFormatting(Formatting.GRAY))))))
                                .binding(Config.defaults.mainBlacklist, () -> Config.mainBlacklist, n -> Config.mainBlacklist = n)
                                .controller(StringControllerBuilder::create)
                                .initial("")
                                .build())
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(lang("category.off_hand"))
                        .tooltip(lang("category.off_hand.tooltip"))
                        .option(Option.<Config.HandRequirement>createBuilder()
                                .name(lang("hand.requirements"))
                                .description(OptionDescription.of(lang("hand.requirements.description")
                                        .append("\n\n").append(lang("hand.requirements.description_2").styled(style -> style.withColor(TextColor.fromFormatting(Formatting.AQUA))))
                                        .append("\n").append(lang("hand.requirements.description_3").styled(style -> style.withColor(TextColor.fromFormatting(Formatting.GREEN))))
                                        .append("\n").append(lang("hand.requirements.description_4").styled(style -> style.withColor(TextColor.fromFormatting(Formatting.RED))))))
                                .binding(Config.defaults.offReq, () -> Config.offReq, n -> Config.offReq = n)
                                .controller(opt -> EnumControllerBuilder.create(opt).enumClass(Config.HandRequirement.class)
                                        .formatValue(v -> Text.translatable("config.sit."+v.name().toLowerCase())))
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(lang("hand.restrictions"))
                                .description(OptionDescription.of(lang("hand.restrictions.description")))
                                .option(Option.<Boolean>createBuilder()
                                        .name(lang("hand.restrictions.blocks"))
                                        .binding(Config.defaults.offBlock,()-> Config.offBlock, n -> Config.offBlock = n)
                                        .controller(opt -> BooleanControllerBuilder.create(opt).trueFalseFormatter())
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(lang("hand.restrictions.food"))
                                        .binding(Config.defaults.offFood,()-> Config.offFood, n -> Config.offFood = n)
                                        .controller(opt -> BooleanControllerBuilder.create(opt).trueFalseFormatter())
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(lang("hand.restrictions.usable"))
                                        .description(OptionDescription.of(lang("hand.restrictions.usable.description")))
                                        .binding(Config.defaults.offUsable,()-> Config.offUsable, n -> Config.offUsable = n)
                                        .controller(opt -> BooleanControllerBuilder.create(opt).trueFalseFormatter())
                                        .build())
                                .build())
                        .group(ListOption.<String>createBuilder()
                                .name(lang("hand.whitelist"))
                                .description(OptionDescription.of(lang("hand.whitelist.description")
                                        .append("\n\n").append(lang("hand.list.description"))
                                        .append(lang("hand.list.description_2").styled(style -> style.withItalic(true).withColor(TextColor.fromFormatting(Formatting.GRAY))))))
                                .binding(Config.defaults.offWhitelist, () -> Config.offWhitelist, n -> Config.offWhitelist = n)
                                .controller(StringControllerBuilder::create)
                                .initial("")
                                .build())
                        .group(ListOption.<String>createBuilder()
                                .name(lang("hand.blacklist"))
                                .description(OptionDescription.of(lang("hand.blacklist.description")
                                        .append("\n\n").append(lang("hand.list.description"))
                                        .append(lang("hand.list.description_2").styled(style -> style.withItalic(true).withColor(TextColor.fromFormatting(Formatting.GRAY))))))
                                .binding(Config.defaults.offBlacklist, () -> Config.offBlacklist, n -> Config.offBlacklist = n)
                                .controller(StringControllerBuilder::create)
                                .initial("")
                                .build())
                        .build())
                .build().generateScreen(parent);
    }

    /**
     * check if YACL is installed by getting a class and seeing if it throws
     * @return if YACL is installed
     */
    public static boolean yaclCheck() {
        try {
            Class.forName("dev.isxander.yacl3.platform.Env");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}

package lilypuree.mapatlases;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MapAtlasesMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MapAtlasesConfig {
    public ForgeConfigSpec.IntValue maxMapCount;
    public ForgeConfigSpec.BooleanValue enableEmptyMapEntryAndFill;

    public ForgeConfigSpec.ConfigValue<Integer> forceMiniMapScaling;
    public ForgeConfigSpec.ConfigValue<Integer> miniMapHorizontalOffset;
    public ForgeConfigSpec.ConfigValue<Integer> miniMapVerticalOffset;
    public ForgeConfigSpec.BooleanValue drawMiniMapHUD;
    public ForgeConfigSpec.ConfigValue<Integer> forceWorldMapScaling;
    public ForgeConfigSpec.EnumValue<ActivationLocation> activationLocation;
    public ForgeConfigSpec.EnumValue<Anchoring> miniMapAnchoring;

    public static ForgeConfigSpec COMMON_CONFIG;
    public static ForgeConfigSpec CLIENT_CONFIG;

    public MapAtlasesConfig() {
        ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();

        COMMON_BUILDER.comment("The maximum number of Maps (Filled & Empty combined) allowed to be inside an Atlas.");
        maxMapCount = COMMON_BUILDER.defineInRange("maxMapCount", 128, 0, 99999);

        COMMON_BUILDER.comment("If 'true', Atlases will be able to store Empty Maps and auto-fill them as you explore.");
        enableEmptyMapEntryAndFill = COMMON_BUILDER.define("enableEmptyMapEntryAndFill", true);

        COMMON_CONFIG = COMMON_BUILDER.build();


        ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();

        CLIENT_BUILDER.comment("Scale the mini-map to a given pixel size.");
        forceMiniMapScaling = CLIENT_BUILDER.define("forceMiniMapScaling", 64);

        CLIENT_BUILDER.comment("Scale the world-map to a given pixel size.");
        forceWorldMapScaling = CLIENT_BUILDER.define("forceWorldMapScaling", 128);

        CLIENT_BUILDER.comment("If 'true', the Mini-Map of the Active Map will be drawn on the HUD while the Atlas is on your hot-bar or off-hand.");
        drawMiniMapHUD = CLIENT_BUILDER.define("drawMiniMapHUD", true);

        CLIENT_BUILDER.comment("Controls location where mini-map displays. Any of: 'HANDS', 'HOTBAR', or 'INVENTORY'.");
        activationLocation = CLIENT_BUILDER.defineEnum("activationLocation", ActivationLocation.HOTBAR);

        CLIENT_BUILDER.comment("Set to any of 'Upper'/'Lower' & 'Left'/'Right' to control anchor position of mini-map");
        miniMapAnchoring = CLIENT_BUILDER.defineEnum("minimapAchoring", Anchoring.UPPERRIGHT);

        CLIENT_BUILDER.comment("Enter an integer which will offset the mini-map horizontally");
        miniMapHorizontalOffset = CLIENT_BUILDER.define("minimapHorizontalOffset", 0);

        CLIENT_BUILDER.comment("Enter an integer which will offset the mini-map vertically");
        miniMapVerticalOffset = CLIENT_BUILDER.define("minimapVerticalOffset", 0);

        CLIENT_CONFIG = CLIENT_BUILDER.build();
    }

    public enum ActivationLocation {
        HANDS, HOTBAR, INVENTORY
    }

    public enum Anchoring {
        UPPERRIGHT(false, false), UPPERLEFT(false, true), LOWERRIGHT(true, false), LOWERLEFT(true, true);

        private boolean lower, left;

        Anchoring(boolean lower, boolean left) {
            this.lower = lower;
            this.left = left;
        }

        public boolean isLower() {
            return lower;
        }

        public boolean isLeft() {
            return left;
        }
    }
}

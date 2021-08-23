package pepjebs.dicemc.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
	public static ForgeConfigSpec SERVER_CONFIG;
	
	public static ForgeConfigSpec.IntValue MAX_MAP_COUNT;
	public static ForgeConfigSpec.IntValue FORCE_MINIMAP_SCALING;
	public static ForgeConfigSpec.BooleanValue DRAW_MINIMAP_HUD;
	public static ForgeConfigSpec.BooleanValue ENABLE_EMPTY_MAP_ENTRY_AND_FILL;
	public static ForgeConfigSpec.BooleanValue FORCE_USE_IN_HANDS;
	
	static {
		ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();
		
		SERVER_BUILDER.comment("Server Settings").push("Server");
		MAX_MAP_COUNT = SERVER_BUILDER.comment("The maximum number of Maps (Filled & Empty combined) allowed to be inside an Atlas.")
				.defineInRange("Max Map Count", 128, 1, Integer.MAX_VALUE);
		FORCE_MINIMAP_SCALING = SERVER_BUILDER.comment("Scale the mini-map to a given pixel size. (Default is 64)")
				.defineInRange("Force Minimap Scaling", 64, 1, Integer.MAX_VALUE);
		DRAW_MINIMAP_HUD = SERVER_BUILDER.comment("If 'true', the Mini-Map of the Active Map will be drawn on the HUD while the Atlas is on your hot-bar or off-hand.")
				.define("Draw Minimap HUD", true);
		ENABLE_EMPTY_MAP_ENTRY_AND_FILL = SERVER_BUILDER.comment("If 'true', Atlases will be able to store Empty Maps and auto-fill them as you explore.")
				.define("Enable Empty Map Entry and Fill", true);
		FORCE_USE_IN_HANDS = SERVER_BUILDER.comment("If 'true', Atlases will require to be held in Main or Off Hands to be displayed or updated.")
				.define("Force Use In Hands", false);
		SERVER_BUILDER.pop();
		
		SERVER_CONFIG = SERVER_BUILDER.build();
	}
}

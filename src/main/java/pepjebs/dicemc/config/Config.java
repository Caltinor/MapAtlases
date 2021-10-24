package pepjebs.dicemc.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;


public final class Config {
	public static Config instance;

	private ForgeConfigSpec.IntValue maxMapCount;
	private ForgeConfigSpec.IntValue forceMinimapScaling;
	private ForgeConfigSpec.BooleanValue drawMinimapHud;
	private ForgeConfigSpec.BooleanValue enableEmptyMapEntryAndFill;
	private ForgeConfigSpec.BooleanValue forceUseInHands;

	public static void register(IEventBus modEventBus, ModLoadingContext modLoadingContext) {
		modEventBus.register(Config.class);
		ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
		instance = new Config(builder);
		ForgeConfigSpec config = builder.build();
		modLoadingContext.registerConfig(ModConfig.Type.SERVER, config);
	}

	private Config(ForgeConfigSpec.Builder builder) {
		builder.comment("Server Settings").push("Server");

		maxMapCount = builder
				.comment("The maximum number of Maps (Filled & Empty combined) allowed to be inside an Atlas.")
				.defineInRange("Max Map Count", 128, 1, Integer.MAX_VALUE);

		forceMinimapScaling = builder
				.comment("Scale the mini-map to a given pixel size. (Default is 64)")
				.defineInRange("Force Minimap Scaling", 64, 1, Integer.MAX_VALUE);

		drawMinimapHud = builder
				.comment("If 'true', the Mini-Map of the Active Map will be drawn on the HUD while the Atlas is on your hot-bar or off-hand.")
				.define("Draw Minimap HUD", true);

		enableEmptyMapEntryAndFill = builder
				.comment("If 'true', Atlases will be able to store Empty Maps and auto-fill them as you explore.")
				.define("Enable Empty Map Entry and Fill", true);

		forceUseInHands = builder
				.comment("If 'true', Atlases will require to be held in Main or Off Hands to be displayed or updated.")
				.define("Force Use In Hands", false);

		builder.pop();
	}

	public static int getMaxMapCount() {
		return instance.maxMapCount.get();
	}

	public static int getForceMinimapScaling() {
		return instance.forceMinimapScaling.get();
	}

	public static boolean getDrawMinimapHud() {
		return instance.drawMinimapHud.get();
	}

	public static boolean getEnableEmptyMapEntryAndFill() {
		return instance.enableEmptyMapEntryAndFill.get();
	}

	public static boolean getForceUseInHands() {
		return instance.forceUseInHands.get();
	}
}

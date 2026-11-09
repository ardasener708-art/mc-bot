package dev.turkishmc.mcmod.render;

import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.turkishmc.mcmod.McModClient;
import dev.turkishmc.mcmod.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public final class EspRenderer {
	private static final RenderPipeline ESP_PIPELINE = createEspPipeline();
	private static final RenderType ESP_LINES = RenderType.create(
			"mcmod_entity_esp",
			RenderSetup.builder(ESP_PIPELINE)
					.setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
					.createRenderSetup()
	);

	private static volatile List<BoxSnapshot> boxes = List.of();
	private static volatile List<BoxSnapshot> chestBoxes = List.of();
	private static volatile List<LineSnapshot> tracers = List.of();
	private static volatile List<TagSnapshot> nameTags = List.of();
	private static ClientLevel chestScanWorld;
	private static long lastChestScanTick = Long.MIN_VALUE;
	private static List<BlockPos> cachedChestPositions = List.of();

	private EspRenderer() {
	}

	private static RenderPipeline createEspPipeline() {
		RenderPipeline pipeline = RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
				.withLocation("pipeline/entity_esp")
				.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
				.withDepthWrite(false)
				.build();
		RenderPipelines.register(pipeline);
		return pipeline;
	}

	public static void extract(WorldExtractionContext context) {
		Module esp = McModClient.MODULES.getModule(McModClient.ESP_MODULE_NAME);
		Module tracerModule = McModClient.MODULES.getModule("tracers");
		Module chestModule = McModClient.MODULES.getModule("chest_esp");
		Module nametagModule = McModClient.MODULES.getModule("nametags");

		Minecraft client = Minecraft.getInstance();
		Vec3 cameraPosition = context.camera().position();
		List<BoxSnapshot> extractedBoxes = new ArrayList<>();
		List<BoxSnapshot> extractedChestBoxes = new ArrayList<>();
		List<LineSnapshot> extractedTracers = new ArrayList<>();
		List<TagSnapshot> extractedNameTags = new ArrayList<>();

		for (Entity entity : context.world().entitiesForRendering()) {
			if (entity == client.player || entity.isRemoved()) {
				continue;
			}

			AABB bounds = entity.getBoundingBox().move(
					-cameraPosition.x,
					-cameraPosition.y,
					-cameraPosition.z
			);
			if (esp.isEnabled()) {
				int color = colorFor(entity);
				if (color != 0) {
					extractedBoxes.add(new BoxSnapshot(bounds, color));
				}
			}
			if (tracerModule.isEnabled() && entity.position().distanceTo(cameraPosition)
					<= tracerModule.getSetting("range").getInt()) {
				int color = 0xFF000000 | tracerModule.getSetting("color").getInt();
				extractedTracers.add(new LineSnapshot(
						bounds.getCenter().x, bounds.getCenter().y, bounds.getCenter().z, color));
			}
			if (nametagModule.isEnabled() && entity instanceof LivingEntity living
					&& entity.position().distanceTo(cameraPosition)
					<= nametagModule.getSetting("range").getInt()) {
				String label = living.getDisplayName().getString() + "  "
						+ String.format("%.0f❤  %.0fm", living.getHealth(),
						entity.position().distanceTo(cameraPosition));
				extractedNameTags.add(new TagSnapshot(bounds.getCenter().x,
						bounds.maxY + 0.25, bounds.getCenter().z, label));
			}
		}

		if (chestModule.isEnabled()) {
			int range = chestModule.getSetting("range").getInt();
			int color = 0xFF000000 | chestModule.getSetting("color").getInt();
			ClientLevel world = context.world();
			long gameTime = world.getGameTime();
			if (world != chestScanWorld || gameTime - lastChestScanTick >= 10) {
				cachedChestPositions = scanNearbyChests(world, BlockPos.containing(cameraPosition), range);
				chestScanWorld = world;
				lastChestScanTick = gameTime;
			}
			for (BlockPos pos : cachedChestPositions) {
				if (pos.distToCenterSqr(cameraPosition) > (double) range * range) {
					continue;
				}
				AABB bounds = new AABB(pos).move(-cameraPosition.x, -cameraPosition.y, -cameraPosition.z);
				extractedChestBoxes.add(new BoxSnapshot(bounds, color));
			}
		}

		boxes = List.copyOf(extractedBoxes);
		chestBoxes = List.copyOf(extractedChestBoxes);
		tracers = List.copyOf(extractedTracers);
		nameTags = List.copyOf(extractedNameTags);
		for (Module module : McModClient.MODULES.getModules()) {
			module.extractWorld(context);
		}
	}

	private static List<BlockPos> scanNearbyChests(ClientLevel world, BlockPos center, int range) {
		int verticalRange = Math.min(range, 6);
		List<BlockPos> positions = new ArrayList<>();
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		for (int x = center.getX() - range; x <= center.getX() + range; x++) {
			for (int z = center.getZ() - range; z <= center.getZ() + range; z++) {
				if (!world.hasChunk(Math.floorDiv(x, 16), Math.floorDiv(z, 16))) {
					continue;
				}
				for (int y = Math.max(world.getMinY(), center.getY() - verticalRange);
					 y <= Math.min(world.getMaxY(), center.getY() + verticalRange); y++) {
					cursor.set(x, y, z);
					var block = world.getBlockState(cursor).getBlock();
					if (block == Blocks.CHEST || block == Blocks.TRAPPED_CHEST || block == Blocks.BARREL
							|| block instanceof ShulkerBoxBlock) {
						positions.add(cursor.immutable());
					}
				}
			}
		}
		return List.copyOf(positions);
	}

	public static void render(WorldRenderContext context) {
		if (boxes.isEmpty() && chestBoxes.isEmpty() && tracers.isEmpty() && nameTags.isEmpty()) {
			for (Module module : McModClient.MODULES.getModules()) {
				module.renderWorld(context);
			}
			return;
		}

		MultiBufferSource consumers = context.consumers();
		if (consumers == null) {
			return;
		}

		// 1.21.11'in yeni render yolunda context matrisi null olabilir.
		PoseStack matrices = context.matrices();
		if (matrices == null) {
			matrices = new PoseStack();
		}
		Matrix4f matrix = matrices.last().pose();
		VertexConsumer vertices = consumers.getBuffer(ESP_LINES);

		for (BoxSnapshot box : boxes) {
			drawBox(vertices, matrix, box.bounds(), box.color());
		}
		for (BoxSnapshot box : chestBoxes) {
			drawBox(vertices, matrix, box.bounds(), box.color());
		}
		for (LineSnapshot tracer : tracers) {
			line(vertices, matrix, 0, 0, 0, tracer.x(), tracer.y(), tracer.z(),
					((tracer.color() >>> 16) & 0xFF) / 255.0F,
					((tracer.color() >>> 8) & 0xFF) / 255.0F,
					(tracer.color() & 0xFF) / 255.0F,
					((tracer.color() >>> 24) & 0xFF) / 255.0F);
		}
		if (!nameTags.isEmpty()) {
			Minecraft client = Minecraft.getInstance();
			float scale = McModClient.MODULES.getModule("nametags").getSetting("scale").getInt() / 1000.0F;
			for (TagSnapshot tag : nameTags) {
				matrices.pushPose();
				matrices.translate(tag.x(), tag.y(), tag.z());
				matrices.mulPose(client.gameRenderer.getMainCamera().rotation());
				matrices.scale(-scale, -scale, scale);
				client.font.drawInBatch(tag.label(), -client.font.width(tag.label()) / 2.0F, 0,
						0xFFFFFFFF, false, matrices.last().pose(), consumers,
						net.minecraft.client.gui.Font.DisplayMode.NORMAL, 0x90000000, 15728880);
				matrices.popPose();
			}
		}
		for (Module module : McModClient.MODULES.getModules()) {
			module.renderWorld(context);
		}
	}

	private static int colorFor(Entity entity) {
		if (entity instanceof Player) {
			return colorSetting("color");
		}
		if (entity instanceof Enemy) {
			return colorSetting("hostile_color");
		}
		if (entity instanceof Mob) {
			return colorSetting("passive_color");
		}
		return 0;
	}

	private static int colorSetting(String setting) {
		return 0xFF000000 | McModClient.MODULES.getModule(McModClient.ESP_MODULE_NAME)
				.getSetting(setting).getInt();
	}

	private static void drawBox(VertexConsumer vertices, Matrix4f matrix, AABB box, int color) {
		float red = ((color >>> 16) & 0xFF) / 255.0F;
		float green = ((color >>> 8) & 0xFF) / 255.0F;
		float blue = (color & 0xFF) / 255.0F;
		float alpha = ((color >>> 24) & 0xFF) / 255.0F;

		double x0 = box.minX;
		double y0 = box.minY;
		double z0 = box.minZ;
		double x1 = box.maxX;
		double y1 = box.maxY;
		double z1 = box.maxZ;

		line(vertices, matrix, x0, y0, z0, x1, y0, z0, red, green, blue, alpha);
		line(vertices, matrix, x1, y0, z0, x1, y0, z1, red, green, blue, alpha);
		line(vertices, matrix, x1, y0, z1, x0, y0, z1, red, green, blue, alpha);
		line(vertices, matrix, x0, y0, z1, x0, y0, z0, red, green, blue, alpha);

		line(vertices, matrix, x0, y1, z0, x1, y1, z0, red, green, blue, alpha);
		line(vertices, matrix, x1, y1, z0, x1, y1, z1, red, green, blue, alpha);
		line(vertices, matrix, x1, y1, z1, x0, y1, z1, red, green, blue, alpha);
		line(vertices, matrix, x0, y1, z1, x0, y1, z0, red, green, blue, alpha);

		line(vertices, matrix, x0, y0, z0, x0, y1, z0, red, green, blue, alpha);
		line(vertices, matrix, x1, y0, z0, x1, y1, z0, red, green, blue, alpha);
		line(vertices, matrix, x1, y0, z1, x1, y1, z1, red, green, blue, alpha);
		line(vertices, matrix, x0, y0, z1, x0, y1, z1, red, green, blue, alpha);
	}

	private static void line(
			VertexConsumer vertices,
			Matrix4f matrix,
			double x0,
			double y0,
			double z0,
			double x1,
			double y1,
			double z1,
			float red,
			float green,
			float blue,
			float alpha
	) {
		vertices.addVertex(matrix, (float) x0, (float) y0, (float) z0)
				.setColor(red, green, blue, alpha)
				.setNormal(0.0F, 1.0F, 0.0F)
				.setLineWidth(2.0F);
		vertices.addVertex(matrix, (float) x1, (float) y1, (float) z1)
				.setColor(red, green, blue, alpha)
				.setNormal(0.0F, 1.0F, 0.0F)
				.setLineWidth(2.0F);
	}

	private record BoxSnapshot(AABB bounds, int color) {
	}

	private record LineSnapshot(double x, double y, double z, int color) {
	}

	private record TagSnapshot(double x, double y, double z, String label) {
	}
}

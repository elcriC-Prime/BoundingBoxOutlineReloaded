package com.irtimaled.bbor.client;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.irtimaled.bbor.client.config.ConfigManager;
import com.irtimaled.bbor.client.renderers.AbstractRenderer;
import com.irtimaled.bbor.client.renderers.RenderHelper;
import com.irtimaled.bbor.client.renderers.RenderingContext;
import com.irtimaled.bbor.common.models.AbstractBoundingBox;
import com.irtimaled.bbor.common.models.DimensionId;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joml.Matrix4f;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class AsyncRenderer {

    private static final Logger LOGGER = LoggerFactory.getLogger("BBOR AsyncRenderer");

    private static final Executor EXECUTOR = Executors.newSingleThreadExecutor(
            new ThreadFactoryBuilder().setNameFormat("BBOR Building Thread").setDaemon(true).build()
    );

    private static final RenderingContext DEFAULT = new RenderingContext();

    private static final RenderingContext[] asyncContexts = new RenderingContext[] {
            new RenderingContext(),
            new RenderingContext(),
    };
    private static int currentAsyncContext = -1;
    private static CompletableFuture<Void> buildingFuture = null;
    private static boolean lastActive = false;
    private static boolean toDiscardBuild = false;
    private static long lastBuildTime = System.currentTimeMillis();
    private static AtomicLong lastDurationNanos = new AtomicLong(0L);
    private static DimensionId lastDimID = null;
    private static RenderingContext lastCtx;
    private static volatile List<AbstractBoundingBox> syncRenders = List.of();
    private static final AtomicInteger runningBuilds = new AtomicInteger();
    private static boolean defaultBuilt = false;

    public static void forceRebuild() {
        lastBuildTime = 0;
        toDiscardBuild = false;
        defaultBuilt = false;
    }

    static void render(MatrixStack matrixStack, Matrix4f modelViewMatrix, Matrix4f projectionMatrix, net.minecraft.client.render.Camera camera, DimensionId dimensionId) {
        runCleanup();

        if (!ClientRenderer.getActive()) {
            if (lastActive) {
                lastActive = false;
                defaultBuilt = false;
                DEFAULT.hardReset();
                // invalidate async things
                if (buildingFuture != null) {
                    toDiscardBuild = true;
                }
                currentAsyncContext = -1;
                for (RenderingContext context : asyncContexts) {
                    context.hardReset();
                }
            }
            if (runningBuilds.get() == 0) {
                ClientRenderer.doCleanup();
            }
            return;
        }

        lastActive = true;

        long startTime = System.nanoTime();
        RenderHelper.beforeRender();

        final Boolean useAsync = ConfigManager.asyncBuilding.get();
        if (useAsync) {
            final int i = getNextAsyncContext(dimensionId);
            if (i != -1) {
                final RenderingContext ctx = asyncContexts[i];
                draw(matrixStack, modelViewMatrix, projectionMatrix, camera, ctx);

                buildSyncs(DEFAULT);
                draw(matrixStack, modelViewMatrix, projectionMatrix, camera, DEFAULT);

                lastCtx = ctx;
                defaultBuilt = false;
            } else {
                // Instantly render synchronously when activating so user sees ZERO delay
                if (!defaultBuilt) {
                    build0(dimensionId, DEFAULT, true);
                    defaultBuilt = true;
                }
                draw(matrixStack, modelViewMatrix, projectionMatrix, camera, DEFAULT);
                lastCtx = DEFAULT;
            }
        } else {
            // invalidate async things
            currentAsyncContext = -1;
            toDiscardBuild = true;
            defaultBuilt = false;

            final RenderingContext ctx = DEFAULT;

            build0(dimensionId, ctx, true);

            draw(matrixStack, modelViewMatrix, projectionMatrix, camera, ctx);

            RenderCulling.flushRendering();

            lastCtx = ctx;
        }
        RenderHelper.afterRender();

        lastDurationNanos.set(System.nanoTime() - startTime);
    }

    private static void draw(MatrixStack matrixStack, Matrix4f modelViewMatrix, Matrix4f projectionMatrix, net.minecraft.client.render.Camera camera, RenderingContext ctx) {
        Matrix4f finalModelView = new Matrix4f(modelViewMatrix);
        if (matrixStack != null) {
            finalModelView.mul(matrixStack.peek().getPositionMatrix());
        }
        double camX = camera != null ? camera.getPos().x : Camera.getX();
        double camY = camera != null ? camera.getPos().y : Camera.getY();
        double camZ = camera != null ? camera.getPos().z : Camera.getZ();
        finalModelView.translate(
                (float) (ctx.getBaseX() - camX),
                (float) (ctx.getBaseY() - camY),
                (float) (ctx.getBaseZ() - camZ)
        );
        ctx.doDrawing(finalModelView, projectionMatrix);
    }

    private static int getNextAsyncContext(DimensionId dimensionId) {
        if (dimensionId != lastDimID) {
            currentAsyncContext = -1;
            lastDimID = dimensionId;
            toDiscardBuild = buildingFuture != null;
            defaultBuilt = false;
        }
        if ((buildingFuture == null || buildingFuture.isDone()) && lastBuildTime + 2000 < System.currentTimeMillis()) {
            lastBuildTime = System.currentTimeMillis();
            RenderingContext ctx = asyncContexts[(currentAsyncContext + 1) % asyncContexts.length];
            buildingFuture = CompletableFuture.runAsync(() -> build0(dimensionId, ctx, false), EXECUTOR)
                    .exceptionallyAsync(throwable -> {
                        LOGGER.error("Error occurred while building buffers async", throwable);
                        MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(
                                Text.of("[BBOR] Error occurred while building buffers async, check logs and try re-enabling rendering: " + throwable.toString())
                                        .copy().styled(style -> style.withBold(true).withColor(TextColor.fromFormatting(Formatting.RED))));
                        return null;
                    }, MinecraftClient.getInstance());
        }
        return currentAsyncContext;
    }

    private static void runCleanup() {
        if (buildingFuture != null && buildingFuture.isDone()) {
            if (!toDiscardBuild) {
                currentAsyncContext = (currentAsyncContext + 1) % asyncContexts.length;
            } else {
                toDiscardBuild = false;
            }
            buildingFuture = null;
        }
    }

    private static void build0(DimensionId dimensionId, RenderingContext ctx, boolean onRenderThread) {
        ctx.reset();
        ctx.beginBatch();
        runningBuilds.incrementAndGet();

        try {
            final List<AbstractBoundingBox> boundingBoxes = ClientRenderer.getBoundingBoxes(dimensionId);
            final List<AbstractBoundingBox> syncs = new ObjectArrayList<>();
            RenderCulling.flushPreRendering();
            for (AbstractBoundingBox key : boundingBoxes) {
                AbstractRenderer renderer = key.getRenderer();
                if (renderer != null) {
                    renderer.render(ctx, key);
                    if (key.needSyncRendering()) {
                        if (onRenderThread) renderer.renderSync(ctx, key);
                        else syncs.add(key);
                    }
                }
            }
            syncRenders = syncs;
        } finally {
            runningBuilds.decrementAndGet();
            ctx.endBatch();
        }
    }

    private static void buildSyncs(RenderingContext ctx) {
        ctx.reset();
        ctx.beginBatch();

        try {
            for (AbstractBoundingBox key : syncRenders) {
                AbstractRenderer renderer = key.getRenderer();
                if (renderer != null) {
                    renderer.renderSync(ctx, key);
                }
            }
        } finally {
            ctx.endBatch();
        }
    }

    public static long getLastDurationNanos() {
        return lastDurationNanos.get();
    }

    public static List<String> renderingDebugStrings() {
        final RenderingContext ctx = lastCtx;
        if (ctx == null) {
            return List.of("[BBOR] Preparing rendering...");
        } else {
            if (currentAsyncContext != -1) {
                return List.of("[BBOR] Async: " + ctx.debugString(), "[BBOR] Sync: " + DEFAULT.debugString());
            } else {
                return List.of("[BBOR] " + ctx.debugString());
            }
        }
    }

}

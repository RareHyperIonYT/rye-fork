/*
 * Copyright (c) Rye Client 2024-2025.
 *
 * This file belongs to Rye Client,
 * an open-source Fabric injection client.
 * Rye GitHub: https://github.com/RyeClient/rye-v1.git
 *
 * THIS PROJECT DOES NOT HAVE A WARRANTY.
 *
 * Rye (and subsequently, its files) are all licensed under the MIT License.
 * Rye should have come with a copy of the MIT License.
 * If it did not, you may obtain a copy here:
 * MIT License: https://opensource.org/license/mit
 *
 */

package dev.thoq.module.impl.visual;

import dev.thoq.RyeClient;
import dev.thoq.config.setting.impl.ModeSetting;
import dev.thoq.event.IEventListener;
import dev.thoq.event.impl.Render2DEvent;
import dev.thoq.module.Module;
import dev.thoq.module.ModuleCategory;
import dev.thoq.module.impl.world.NukerModule;
import dev.thoq.module.impl.combat.ReachModule;
import dev.thoq.utilities.render.ColorUtility;
import dev.thoq.utilities.render.RenderUtility;
import dev.thoq.utilities.render.TextRendererUtility;
import dev.thoq.utilities.render.Theme;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SuppressWarnings({"FieldCanBeLocal", "SameParameterValue", "unused"})
public class HUDModule extends Module {

    private long animationStartTime = 0;
    private boolean isAnimatingIn = false;
    private boolean isAnimatingOut = false;
    private boolean wasVisible = false;
    private static final long ANIMATION_DURATION = 200;
    private static final int END_Y_POSITION = 30;
    private static String lastDynamicText = "";
    private final ModeSetting themeSetting;
    private int targetModuleCount = 0;
    private float currentModuleCount = 0;
    private long expansionStartTime = 0;
    private boolean isExpanding = false;
    private static final long EXPANSION_DURATION = 300;

    public HUDModule() {
        super("HUD", "Shows Heads Up Display", ModuleCategory.VISUAL);

        String[] themeNames = Theme.getThemeNames();
        this.themeSetting = new ModeSetting("Theme", "Select the theme for the HUD", "Rye", themeNames);
        this.addSetting(themeSetting);

        this.setEnabled(true);
    }

    private enum Mode {
        NORMAL,
        SCAFFOLD,
        SPEED,
        FLIGHT,
        KILLAURA,
        NUKER,
        REACH,
    }

    private final IEventListener<Render2DEvent> renderEvent = event -> {
        if(mc.player == null) return;

        boolean killauraEnabled = RyeClient.INSTANCE.getModuleRepository().getModuleByName("Killaura").isEnabled();
        boolean scaffoldEnabled = RyeClient.INSTANCE.getModuleRepository().getModuleByName("Scaffold").isEnabled();
        boolean speedEnabled = RyeClient.INSTANCE.getModuleRepository().getModuleByName("Speed").isEnabled();
        boolean flightEnabled = RyeClient.INSTANCE.getModuleRepository().getModuleByName("Flight").isEnabled();
        boolean nukerEnabled = RyeClient.INSTANCE.getModuleRepository().getModuleByName("Nuker").isEnabled();
        boolean reachEnabled = RyeClient.INSTANCE.getModuleRepository().getModuleByName("Reach").isEnabled();

        List<String> dynamicTexts = new ArrayList<>();
        String bps = RyeClient.getBps();

        if(flightEnabled || speedEnabled) {
            String speedFlightMsg = String.format("Going %s b/s", bps);
            dynamicTexts.add(speedFlightMsg);
        }

        if(scaffoldEnabled) {
            int scaffoldSlot = mc.player.getInventory().getSelectedSlot();
            int blocksRemaining = mc.player.getInventory().getStack(scaffoldSlot).getCount();
            String scaffoldMsg = String.format("%s Remaining", blocksRemaining);
            dynamicTexts.add(scaffoldMsg);
        }

        if(killauraEnabled) {
            String killauraMsg = String.format("%.1f/%.1f ♡",
                    mc.player.getHealth() / 2, mc.player.getMaxHealth() / 2);
            dynamicTexts.add(killauraMsg);
        }

        if(nukerEnabled) {
            String nukerMsg = String.format("%s blocks destroyed", NukerModule.getBlocksDestroyed());
            dynamicTexts.add(nukerMsg);
        }

        if(reachEnabled) {
            String reachMsg = String.format("Reached %.1f blocks", ReachModule.getLastReach());
            dynamicTexts.add(reachMsg);
        }

        int newModuleCount = dynamicTexts.size();
        if(newModuleCount != targetModuleCount) {
            targetModuleCount = newModuleCount;
            startExpansionAnimation();
        }

        updateExpansionAnimation();

        String time = RyeClient.getTime();
        String fps = RyeClient.getFps();
        String name = mc.player.getName().getString();

        String clientName = createAnimatedClientName();

        boolean shouldShowDynamic = !dynamicTexts.isEmpty();

        if(shouldShowDynamic && !wasVisible) {
            startShowAnimation();
        } else if(!shouldShowDynamic && wasVisible) {
            startHideAnimation();
        }

        wasVisible = shouldShowDynamic;

        if(shouldShowDynamic || isAnimatingOut) {
            drawAnimatedDynamicModules(
                    dynamicTexts,
                    event
            );
        }

        String displayText = String.format(
                " %s | %s f/s | %s | %s",
                clientName,
                fps,
                name,
                time
        );

        final int padding = 15;
        final int textWidth = TextRendererUtility.getTextWidth(displayText);
        final int textHeight = mc.textRenderer.fontHeight;
        final int xPosition = 2;
        final int yPosition = 2;

        int backgroundColor = ColorUtility.getColor(ColorUtility.Colors.PANEL);

        RenderUtility.drawRect(
                event.getContext(),
                xPosition,
                yPosition,
                textWidth + padding,
                textHeight + padding,
                backgroundColor
        );

        renderAnimatedHUDText(
                event,
                displayText,
                xPosition + padding / 2,
                yPosition + padding / 2
        );
    };

    private void startExpansionAnimation() {
        expansionStartTime = System.currentTimeMillis();
        isExpanding = true;
    }

    private void updateExpansionAnimation() {
        if(!isExpanding) return;

        long elapsed = System.currentTimeMillis() - expansionStartTime;
        float progress = Math.min(elapsed / (float) EXPANSION_DURATION, 1.0f);

        float easedProgress = 1.0f - (float) Math.pow(1.0f - progress, 3);

        currentModuleCount = currentModuleCount + (targetModuleCount - currentModuleCount) * easedProgress;

        if(progress >= 1.0f) {
            currentModuleCount = targetModuleCount;
            isExpanding = false;
        }
    }

    private String createAnimatedClientName() {
        return "§r§l" + RyeClient.getName() + "§r";
    }

    private void renderAnimatedHUDText(Render2DEvent event, String text, int x, int y) {
        float time = System.currentTimeMillis() / 1000.0f;
        float animationFactor = (float) (Math.sin(time * 2.0) + 1.0) / 2.0f;

        String[] parts = text.split("§theme");
        if(parts.length > 1) {
            if(!parts[0].isEmpty()) {
                TextRendererUtility.renderText(
                        event.getContext(),
                        parts[0],
                        ColorUtility.Colors.WHITE,
                        x,
                        y,
                        false
                );
            }

            int firstPartWidth = parts[0].isEmpty() ? 0 : TextRendererUtility.getTextWidth(parts[0]);

            String firstChar = String.valueOf(parts[1].charAt(0));
            int themeColor = Theme.getInterpolatedThemeColorInt(animationFactor);

            TextRendererUtility.renderText(
                    event.getContext(),
                    "§l" + firstChar,
                    themeColor,
                    x + firstPartWidth,
                    y,
                    false
            );

            int firstCharWidth = TextRendererUtility.getTextWidth("§l" + firstChar);

            String remainingText = "§r§l" + parts[1].substring(1) + "§r";
            TextRendererUtility.renderText(
                    event.getContext(),
                    remainingText,
                    ColorUtility.Colors.WHITE,
                    x + firstPartWidth + firstCharWidth,
                    y,
                    false
            );
        } else {
            TextRendererUtility.renderText(
                    event.getContext(),
                    text,
                    ColorUtility.Colors.WHITE,
                    x,
                    y,
                    false
            );
        }
    }

    private void startShowAnimation() {
        animationStartTime = System.currentTimeMillis();
        isAnimatingIn = true;
        isAnimatingOut = false;
    }

    private void startHideAnimation() {
        animationStartTime = System.currentTimeMillis();
        isAnimatingIn = false;
        isAnimatingOut = true;
    }

    private float getAnimationProgress() {
        long elapsed = System.currentTimeMillis() - animationStartTime;
        float progress = Math.min(elapsed / (float) ANIMATION_DURATION, 1.0f);

        return 1.0f - (float) Math.pow(1.0f - progress, 3);
    }

    private void drawAnimatedDynamicModules(
            List<String> dynamicTexts,
            Render2DEvent event
    ) {
        if(mc.player == null || dynamicTexts.isEmpty()) return;

        float progress = getAnimationProgress();

        if(progress >= 1.0f) {
            if(isAnimatingOut) {
                isAnimatingOut = false;
                return;
            }
            if(isAnimatingIn) {
                isAnimatingIn = false;
            }
        }

        float animatedProgress;
        if(isAnimatingIn) {
            animatedProgress = progress;
        } else if(isAnimatingOut) {
            animatedProgress = 1.0f - progress;
        } else {
            animatedProgress = 1.0f;
        }

        final int hudPadding = 15;
        final int hudTextWidth = TextRendererUtility.getTextWidth(String.format(
                " %s | %s f/s | %s | %s",
                createAnimatedClientName(),
                RyeClient.getFps(),
                mc.player.getName().getString(),
                new SimpleDateFormat("hh:mm a").format(new Date())
        ));

        final int hudBottomY = 2 + mc.textRenderer.fontHeight + hudPadding;
        final int startY = hudBottomY - 10;
        final int animatedY = (int) (startY + (END_Y_POSITION - startY) * animatedProgress);

        int alpha = (int) (255 * animatedProgress);
        int baseBackgroundColor = ColorUtility.getColor(ColorUtility.Colors.PANEL);

        String displayText = getString(dynamicTexts);

        final int dynamicPadding = 15;
        final int dynamicTextWidth = TextRendererUtility.getTextWidth(displayText);
        final int dynamicWidth = dynamicTextWidth + dynamicPadding;
        final int screenWidth = mc.getWindow().getScaledWidth();
        final int hudX = 2;
        final int maxAllowedX = screenWidth - dynamicWidth;
        final int alignedX = Math.min(hudX, maxAllowedX);

        drawDynamicRect(
                displayText,
                event,
                alignedX,
                animatedY,
                dynamicPadding,
                baseBackgroundColor,
                alpha
        );
    }

    private @NotNull String getString(List<String> dynamicTexts) {
        int modulesToShow = Math.min(dynamicTexts.size(), (int) Math.ceil(currentModuleCount));

        StringBuilder combinedText = new StringBuilder();
        for(int i = 0; i < modulesToShow; i++) {
            if(i > 0) combinedText.append(" | ");

            String moduleText = dynamicTexts.get(i);

            if(i >= (int) currentModuleCount) {
                float partialProgress = currentModuleCount - (int) currentModuleCount;
                int charactersToShow = (int) (moduleText.length() * partialProgress);
                if(charactersToShow > 0) {
                    combinedText.append(moduleText, 0, Math.min(charactersToShow, moduleText.length()));
                }
            } else {
                combinedText.append(moduleText);
            }
        }

        return combinedText.toString();
    }

    private int applyAlpha(int color, int alpha) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        int originalAlpha = (color >> 24) & 0xFF;

        int combinedAlpha = (originalAlpha * alpha) / 255;

        return (combinedAlpha << 24) | (r << 16) | (g << 8) | b;
    }

    private static void drawDynamicRect(
            String displayText,
            Render2DEvent event,
            int xPosition,
            int yPosition,
            int padding,
            int backgroundColor,
            int textAlpha
    ) {
        int textWidth = TextRendererUtility.getTextWidth(displayText);
        int textHeight = TextRendererUtility.getTextHeight();

        lastDynamicText = displayText;

        RenderUtility.drawRect(
                event.getContext(),
                xPosition,
                yPosition,
                textWidth + padding,
                textHeight + padding,
                backgroundColor
        );

        int textColor = ColorUtility.getColor(ColorUtility.Colors.WHITE);
        int animatedTextColor = (textAlpha << 24) | (textColor & 0x00FFFFFF);

        TextRendererUtility.renderText(
                event.getContext(),
                displayText,
                animatedTextColor,
                xPosition + padding / 2,
                yPosition + padding / 2,
                true
        );
    }
}
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

package dev.thoq.module.impl.utility.nofall;

import dev.thoq.config.setting.impl.ModeSetting;
import dev.thoq.event.IEventListener;
import dev.thoq.event.impl.MotionEvent;
import dev.thoq.module.Module;
import dev.thoq.module.ModuleCategory;
import dev.thoq.module.impl.utility.nofall.vanilla.VanillaNoFall;
import dev.thoq.module.impl.utility.nofall.verus.VerusNoFall;

public class NoFallModule extends Module {
    public NoFallModule() {
        super("NoFall", "No Fall", "Prevents fall damage", ModuleCategory.UTILITY);

        ModeSetting mode = new ModeSetting("Mode", "NoFall mode", "Vanilla", "Vanilla", "Verus");

        addSetting(mode);
    }

    @SuppressWarnings("unused")
    private final IEventListener<MotionEvent> motionEvent = event -> {
        if(!isEnabled() || mc.player == null || !event.isPre()) return;

        String mode = ((ModeSetting) getSetting("Mode")).getValue();
        setPrefix(mode);

        switch(mode) {
            case "Vanilla": {
                VanillaNoFall.vanillaNoFall(mc);
                break;
            }

            case "Verus": {
                VerusNoFall.verusNoFall(mc);
                break;
            }
        }
    };
}
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

package dev.thoq.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;

import java.util.ArrayList;
import java.util.List;

public class CommandRepository {
    private static final List<AbstractCommand> commands = new ArrayList<>();

    public static void registerCommand(AbstractCommand command) {
        commands.add(command);
    }

    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        for(AbstractCommand command : commands) {
            command.register(dispatcher);
        }
    }
}

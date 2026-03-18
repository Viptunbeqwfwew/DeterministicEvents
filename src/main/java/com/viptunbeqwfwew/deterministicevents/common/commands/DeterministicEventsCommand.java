// Copyright (c) 2026 Viptunbeqwfwew
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see <https://www.gnu.org>.
package com.viptunbeqwfwew.deterministicevents.common.commands;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;

import com.viptunbeqwfwew.deterministicevents.DeterministicEvents;

public class DeterministicEventsCommand extends CommandBase {

    static final private ISubCommand commandDump = new CommandDump();
    static final private ISubCommand commandReload = new CommandReload();
    static final private List<String> tabCompletionOptions = new ArrayList<String>() {

        {
            add("dump");
            add("reload");
        }
    };

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public String getCommandName() {
        return DeterministicEvents.MODID;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        if (canCommandSenderUseCommand(sender)) return "command.deterministicevents.usage";
        return "commands.generic.permission";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (canCommandSenderUseCommand(sender)) {
            if (args.length != 1) throw new WrongUsageException("command.deterministicevents.exceptNumArg");
            switch (args[0]) {
                case "dump":
                    commandDump.processCommand(sender);
                    break;
                case "reload":
                    commandReload.processCommand(sender);
                    break;
                default:
                    throw new WrongUsageException("command.deterministicevents.exceptUnknownArg");
            }
        } else throw new WrongUsageException("command.deterministicevents.permission");
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args) {
        if (args.length == 1 && canCommandSenderUseCommand(sender)) return tabCompletionOptions;
        return null;
    }
}

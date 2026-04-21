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

package com.viptunbeqwfwew.deterministicevents.client;

import net.minecraft.command.ICommandSender;

import com.viptunbeqwfwew.deterministicevents.common.commands.DeterministicEventsCommand;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientDeterministicEventsCommand extends DeterministicEventsCommand {

    final private String ownerName;

    public ClientDeterministicEventsCommand(String ownerName) {
        this.ownerName = ownerName;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return ownerName.equals(sender.getCommandSenderName()) || super.canCommandSenderUseCommand(sender);
    }
}

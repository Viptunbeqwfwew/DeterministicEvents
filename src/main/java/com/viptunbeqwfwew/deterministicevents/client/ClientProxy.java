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

import com.viptunbeqwfwew.deterministicevents.DeterministicEvents;
import com.viptunbeqwfwew.deterministicevents.common.CommonProxy;

import cpw.mods.fml.common.event.FMLServerStartingEvent;

public class ClientProxy extends CommonProxy {

    public ClientProxy() {
        DeterministicEvents.LOG.info("Create ClientProxy.");
    }

    @Override
    public void startServer(FMLServerStartingEvent event) {
        event.registerServerCommand(
            new ClientDeterministicEventsCommand(
                event.getServer()
                    .getServerOwner()));
    }

    @Override
    public String getGuiClassName() {
        return "com.viptunbeqwfwew.deterministicevents.config.gui.GuiFactory";
    }
}

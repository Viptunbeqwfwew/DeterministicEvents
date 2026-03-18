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
package com.viptunbeqwfwew.deterministicevents.common;

import java.io.File;

import com.viptunbeqwfwew.deterministicevents.DeterministicEvents;
import com.viptunbeqwfwew.deterministicevents.common.commands.DeterministicEventsCommand;
import com.viptunbeqwfwew.deterministicevents.config.Config;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

public class CommonProxy {

    public File dconfDir;
    public Supervisor supervisor = new Supervisor();

    public CommonProxy() {
        DeterministicEvents.LOG.info("Create CommonProxy.");
    }

    public void preInit(FMLPreInitializationEvent event) {
        File configDir = event.getModConfigurationDirectory();
        dconfDir = new File(configDir, event.getModMetadata().modId);
        Config.load(dconfDir);
        supervisor.allowRegistration();
    }

    public void startServer(FMLServerStartingEvent event) {
        event.registerServerCommand(new DeterministicEventsCommand());
    }

    public String getGuiClassName() {
        return "";
    }
}

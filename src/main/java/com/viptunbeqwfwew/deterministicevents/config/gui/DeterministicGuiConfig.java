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
package com.viptunbeqwfwew.deterministicevents.config.gui;

import java.util.ArrayList;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import com.viptunbeqwfwew.deterministicevents.DeterministicEvents;
import com.viptunbeqwfwew.deterministicevents.utils.DumpUtils;

import cpw.mods.fml.client.config.GuiConfig;

public class DeterministicGuiConfig extends GuiConfig {

    public DeterministicGuiConfig(GuiScreen parent) {
        super(parent, new ArrayList<>(), DeterministicEvents.MODID, false, false, "General");
    }

    @Override
    public void initGui() {
        super.initGui();
        int size = 350;
        this.buttonList.add(new GuiButton(1, this.width / 2 - size / 2, 40, size, 20, "Dump all descriptors."));
        this.buttonList.add(new GuiButton(2, this.width / 2 - size / 2, 64, size, 20, "Reload dconf."));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        super.actionPerformed(button);
        switch (button.id) {
            case 1: {
                DumpUtils.dump();
                break;
            }
            case 2: {
                DeterministicEvents.proxy.supervisor.reload();
                break;
            }
        }
    }
}

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

package com.viptunbeqwfwew.deterministicevents.asm;

import java.io.File;
import java.util.Map;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.MCVersion("1.7.10")
public class CoreDeterministic implements IFMLLoadingPlugin {

    public static boolean isSetup = false;
    public static File source;

    public String[] getASMTransformerClass() {
        return new String[] { "com.viptunbeqwfwew.deterministicevents.asm.transformer.ListenerTransformer",
            "com.viptunbeqwfwew.deterministicevents.asm.transformer.SupervisorTransformer" };
    }

    public String getModContainerClass() {
        return "com.viptunbeqwfwew.deterministicevents.DeterministicEvents";
    }

    public String getSetupClass() {
        return "com.viptunbeqwfwew.deterministicevents.asm.Setup";
    }

    public void injectData(Map<String, Object> data) {
        source = (File) data.get("coremodLocation");
    }

    public String getAccessTransformerClass() {
        return null;
    }
}

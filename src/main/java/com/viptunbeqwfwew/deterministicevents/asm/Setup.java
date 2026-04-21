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

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.LaunchClassLoader;

import com.viptunbeqwfwew.deterministicevents.asm.transformer.SubscriptionTransformer;

import cpw.mods.fml.relauncher.FMLRelaunchLog;
import cpw.mods.fml.relauncher.IFMLCallHook;

@SuppressWarnings("unused")
public class Setup implements IFMLCallHook {

    private LaunchClassLoader classLoader;
    private boolean noTry = false;
    private Field transformers;

    @Override
    public void injectData(Map<String, Object> data) {
        classLoader = (LaunchClassLoader) data.get("classLoader");
        try {
            transformers = LaunchClassLoader.class.getDeclaredField("transformers");
            transformers.setAccessible(true);
        } catch (Exception e) {
            noTry = true;
        }
    }

    @Override
    public Void call() throws Exception {
        if (!noTry) {
            @SuppressWarnings("unchecked")
            List<IClassTransformer> list = (List<IClassTransformer>) transformers.get(classLoader);
            for (int i = 0; i < list.size(); i++) {
                IClassTransformer transformer = list.get(i);
                if (transformer.getClass()
                    .getName()
                    .equals("cpw.mods.fml.common.asm.transformers.EventSubscriptionTransformer")) {
                    list.set(i, new SubscriptionTransformer(transformer));
                    CoreDeterministic.isSetup = true;
                    transformers.setAccessible(false);
                    return null;
                }
            }
            transformers.setAccessible(false);
        }
        FMLRelaunchLog.fine("Setup CoreDeterministic failed.");
        return null;
    }
}

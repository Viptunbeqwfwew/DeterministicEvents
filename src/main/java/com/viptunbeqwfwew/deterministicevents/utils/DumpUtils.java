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

package com.viptunbeqwfwew.deterministicevents.utils;

import java.io.File;
import java.io.PrintWriter;

import com.viptunbeqwfwew.deterministicevents.DeterministicEvents;

import cpw.mods.fml.common.Loader;

public class DumpUtils {

    public static String dump() {
        File dumpFile = new File(
            Loader.instance()
                .getConfigDir(),
            DeterministicEvents.MODID + "/events_dump.txt");

        File parent = dumpFile.getParentFile();
        if (parent != null && !parent.exists()) if (!parent.mkdirs()) {
            DeterministicEvents.LOG.warn("Failed to create directory {}.", parent);
            return null;
        }
        try (PrintWriter writer = new PrintWriter(dumpFile)) {
            for (String descriptor : DeterministicEvents.proxy.supervisor.getAllDescriptor()) {
                writer.println(descriptor);
            }
            DeterministicEvents.LOG
                .info("The event descriptors dump was successfully saved to: {}", dumpFile.getAbsolutePath());
            return dumpFile.getAbsolutePath();
        } catch (Exception e) {
            DeterministicEvents.LOG.error("Error writing event dump!", e);
        }
        return null;
    }
}

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
package com.viptunbeqwfwew.deterministicevents.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.viptunbeqwfwew.deterministicevents.DeterministicEvents;

public class Config {

    final static private HashMap<String, Map<String, List<String>>> setting = new HashMap<>();
    final static private Set<String> orderedGroups = new HashSet<>();

    // TODO: May not warn on the first empty ad.
    static public void load(File workDir) {
        if (!workDir.exists() || !workDir.isDirectory()) return;
        setting.clear();
        orderedGroups.clear();

        Set<String> antiDubSupergroup = new HashSet<>();
        for (File confFile : Objects
            .requireNonNull(workDir.listFiles((directory, fileName) -> fileName.endsWith(".dconf")))) try {
                String content = FileUtils.readFileToString(confFile, "UTF-8");
                List<DeclarativeObject> results = DeclarativeParser.parse(content);
                for (DeclarativeObject obj : results) {
                    if ("supergroup".equals(obj.type)) {
                        if (antiDubSupergroup.contains(obj.name)) {
                            List<String> param = setting.get("supergroup")
                                .get(obj.name);
                            if (!param.isEmpty()) {
                                param.clear();
                                DeterministicEvents.LOG.warn(
                                    "The supergroup \"{}\" has been declared multiple times. The current version will not be used.",
                                    obj.name);
                            }
                            continue;
                        }
                        antiDubSupergroup.add(obj.name);
                    }
                    if ("group".equals(obj.type) && isOrdered(obj.name)) {
                        List<String> params = setting.get("group")
                            .get(obj.name);
                        if (!params.isEmpty()) {
                            params.clear();
                            DeterministicEvents.LOG.warn(
                                "Inconsistent group type for \"{}\". Switching between \"group\" and \"order group\" may cause data corruption or loss of ordering.",
                                obj.name);
                        }
                        continue;
                    }
                    Map<String, List<String>> settingType = setting.computeIfAbsent(obj.type, k -> new HashMap<>());
                    List<String> params = settingType.computeIfAbsent(obj.name, k -> new ArrayList<>());
                    if (obj.order) {
                        orderedGroups.add(obj.name);
                        if (!params.isEmpty()) {
                            params.clear();
                            DeterministicEvents.LOG.warn(
                                "Inconsistent group type for \"{}\". Switching between \"group\" and \"order group\" may cause data corruption or loss of ordering.",
                                obj.name);
                            continue;
                        }
                    }
                    params.addAll(obj.params);
                }
            } catch (IOException e) {
                DeterministicEvents.LOG.error("Error loading declarative file! File: {}", confFile);
            } catch (IllegalArgumentException | InputMismatchException e) {
                DeterministicEvents.LOG.error("{} File: {}", e.getMessage(), confFile);
            }
    }

    static public String[] getParams(String type, String name) {
        Map<String, List<String>> settingType = setting.get(type);
        if (settingType == null) return new String[0];

        List<String> params = settingType.get(name);
        if (params == null || params.size() == 0) return new String[0];

        return params.toArray(new String[0]);
    }

    static public boolean isOrdered(String name) {
        return orderedGroups.contains(name);
    }

    public static boolean checkGroup(String nameGroup, String descriptor) {
        Map<String, List<String>> settingGroup = setting.get("group");
        if (settingGroup == null) return false;

        List<String> params = settingGroup.get(nameGroup);
        if (params == null || params.size() == 0) return false;

        return params.contains(descriptor);
    }
}

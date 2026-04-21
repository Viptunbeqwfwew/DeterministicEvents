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
import org.apache.commons.lang3.tuple.Pair;

import com.viptunbeqwfwew.deterministicevents.Constants;
import com.viptunbeqwfwew.deterministicevents.DeterministicEvents;
import com.viptunbeqwfwew.deterministicevents.config.collector.ACollector;
import com.viptunbeqwfwew.deterministicevents.config.collector.CollectorBody;
import com.viptunbeqwfwew.deterministicevents.config.collector.CollectorContract;
import com.viptunbeqwfwew.deterministicevents.config.collector.CollectorPairBody;
import com.viptunbeqwfwew.deterministicevents.config.collector.IModifier;
import com.viptunbeqwfwew.deterministicevents.config.collector.IPreModifier;
import com.viptunbeqwfwew.deterministicevents.config.collector.ModifierOrder;
import com.viptunbeqwfwew.deterministicevents.config.collector.ModifierReverse;
import com.viptunbeqwfwew.deterministicevents.config.collector.NameSpace;
import com.viptunbeqwfwew.deterministicevents.config.collector.TypeModification;
import com.viptunbeqwfwew.deterministicevents.config.parse.DeclarativeParser;

public class Config {

    final static private HashMap<String, Map<String, List<String>>> setting = new HashMap<>();
    final static private HashMap<String, Map<String, List<Pair<String, String>>>> settingPair = new HashMap<>();
    final static private HashMap<String, Set<String>> flags = new HashMap<>();

    final static private HashMap<Pair<String, String>, Pair<String, String>> pairingContract = new HashMap<>();

    final static private TempStore tempStore = new TempStore();

    static private ACollector factoryCollector(String type) {
        if (tempStore.cacheCollectors.containsKey(type)) return tempStore.cacheCollectors.get(type);
        ACollector collector;
        switch (type) {
            case Constants.SUPERGROUP: {
                collector = new CollectorBody(
                    tempStore.namespace.computeIfAbsent(Constants.SUPERGROUP, ns -> new NameSpace(true, false)),
                    setting.computeIfAbsent(Constants.SUPERGROUP, k -> new HashMap<>()));
                break;
            }
            case Constants.GROUP: {
                collector = new CollectorBody(
                    tempStore.namespace.computeIfAbsent(Constants.GROUP, ns -> new NameSpace(true, true)),
                    setting.computeIfAbsent(Constants.GROUP, k -> new HashMap<>()));
                break;
            }
            case Constants.MUTE: {
                collector = new CollectorBody(setting.computeIfAbsent(Constants.MUTE, k -> new HashMap<>()));
                break;
            }
            case Constants.CONTRACT: {
                collector = new CollectorContract(
                    tempStore.namespace.computeIfAbsent(Constants.GROUP, ns -> new NameSpace(true, true)),
                    pairingContract);
                break;
            }
            case Constants.CONDITION: {
                collector = new CollectorPairBody(
                    tempStore.namespace.computeIfAbsent(Constants.CONDITION, ns -> new NameSpace()),
                    settingPair.computeIfAbsent(Constants.CONDITION, k -> new HashMap<>()));
                break;
            }
            case Constants.MAPPING: {
                collector = new CollectorPairBody(
                    tempStore.namespace.computeIfAbsent(Constants.MAPPING, ns -> new NameSpace()),
                    settingPair.computeIfAbsent(Constants.MAPPING, k -> new HashMap<>()));
                break;
            }
            default:
                return null;
        }
        tempStore.cacheCollectors.put(type, collector);
        return collector;
    }

    static private IModifier factoryModifier(TypeModification typeModification) {
        if (tempStore.cacheModifier.containsKey(typeModification)) return tempStore.cacheModifier.get(typeModification);
        IModifier modifier;
        switch (typeModification) {
            case ORDER: {
                modifier = new ModifierOrder(
                    flags.computeIfAbsent(TypeModification.ORDER.getName(), k -> new HashSet<>()),
                    tempStore.namespace.computeIfAbsent(Constants.GROUP, ns -> new NameSpace(true, true)));
                break;
            }
            default:
                return null;
        }
        tempStore.cacheModifier.put(typeModification, modifier);
        return modifier;
    }

    static private IPreModifier factoryPreModifier(TypeModification typeModification) {
        if (tempStore.cachePreModifier.containsKey(typeModification))
            return tempStore.cachePreModifier.get(typeModification);
        IPreModifier modifier;
        switch (typeModification) {
            case REVERSE: {
                modifier = new ModifierReverse();
                break;
            }
            default:
                return null;
        }
        tempStore.cachePreModifier.put(typeModification, modifier);
        return modifier;
    }

    static private void preLoad() {
        flags.clear();
        setting.clear();
        settingPair.clear();
    }

    static public void load(File workDir) {
        if (!workDir.exists() || !workDir.isDirectory()) return;
        preLoad();

        for (File confFile : Objects
            .requireNonNull(workDir.listFiles((directory, fileName) -> fileName.endsWith(".dconf")))) try {
                String content = FileUtils.readFileToString(confFile, "UTF-8");
                List<DeclarativeObject> results = DeclarativeParser.parse(content);
                List<IModifier> modifiers = new ArrayList<>();
                List<IPreModifier> preModifiers = new ArrayList<>();
                for (DeclarativeObject obj : results) {
                    for (TypeModification typeModification : obj.modifications) {
                        IPreModifier preModifier = factoryPreModifier(typeModification);
                        if (preModifier != null) {
                            preModifiers.add(preModifier);
                        }
                        IModifier modifier = factoryModifier(typeModification);
                        if (modifier != null) {
                            modifiers.add(modifier);
                        }
                    }
                    factoryCollector(obj.type)
                        .ingest(obj, preModifiers.toArray(new IPreModifier[0]), modifiers.toArray(new IModifier[0]));
                    preModifiers.clear();
                    modifiers.clear();
                }
            } catch (IOException e) {
                DeterministicEvents.LOG.error("Error loading declarative file! File: {}", confFile);
            } catch (IllegalArgumentException | InputMismatchException e) {
                DeterministicEvents.LOG.error("{} File: {}", e.getMessage(), confFile);
            }
        tempStore.clear();
    }

    static public String[] getParams(String type, String name) {
        Map<String, List<String>> settingType = setting.get(type);
        if (settingType == null) return new String[0];

        List<String> params = settingType.get(name);
        if (params == null || params.size() == 0) return new String[0];

        return params.toArray(new String[0]);
    }

    @SuppressWarnings("unchecked")
    public static Pair<String, String>[] getPairParams(String type, String name) {
        Map<String, List<Pair<String, String>>> settingType = settingPair.get(type);
        if (settingType == null) return new Pair[0];

        List<Pair<String, String>> params = settingType.get(name);
        if (params == null || params.size() == 0) return new Pair[0];

        return params.toArray(new Pair[0]);
    }

    static public boolean isOrdered(String name) {
        Set<String> set = flags.get("order");
        return set != null && set.contains(name);
    }

    public static boolean checkGroup(String nameGroup, String descriptor) {
        Map<String, List<String>> settingGroup = setting.get(Constants.GROUP);
        if (settingGroup == null) return false;

        List<String> params = settingGroup.get(nameGroup);
        if (params == null || params.size() == 0) return false;

        return params.contains(descriptor);
    }

    public static Pair<String, String> getTargetContract(Pair<String, String> pairNameGroupAndEventTypeSource) {
        return pairingContract.remove(pairNameGroupAndEventTypeSource);
    }

    private static class TempStore {

        final public HashMap<String, ACollector> cacheCollectors = new HashMap<>();
        final public HashMap<TypeModification, IModifier> cacheModifier = new HashMap<>();
        final public HashMap<TypeModification, IPreModifier> cachePreModifier = new HashMap<>();
        final public HashMap<String, NameSpace> namespace = new HashMap<>();

        void clear() {
            namespace.clear();
            cacheCollectors.clear();
            cacheModifier.clear();
            cachePreModifier.clear();
        }
    }
}

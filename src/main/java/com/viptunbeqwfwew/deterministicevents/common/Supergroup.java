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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.viptunbeqwfwew.deterministicevents.common.group.EventListener;
import com.viptunbeqwfwew.deterministicevents.common.group.Group;
import com.viptunbeqwfwew.deterministicevents.common.group.GroupPriority;
import com.viptunbeqwfwew.deterministicevents.common.group.GroupSwitcherPhase;
import com.viptunbeqwfwew.deterministicevents.common.group.IGroup;
import com.viptunbeqwfwew.deterministicevents.common.group.IGroupRegister;
import com.viptunbeqwfwew.deterministicevents.config.Config;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.IEventListener;

public class Supergroup {

    final static private IGroup[] switchers = new IGroup[] { new GroupSwitcherPhase(EventPriority.HIGHEST),
        new GroupSwitcherPhase(EventPriority.HIGH), new GroupSwitcherPhase(EventPriority.NORMAL),
        new GroupSwitcherPhase(EventPriority.LOW), new GroupSwitcherPhase(EventPriority.LOWEST), };
    final private static String slot = "default_slot";
    final private static String next = "next_phase";
    final private IGroupRegister[] priorityGroups;
    private boolean rebuild = false;
    private boolean rebuildGroups = true;
    final Supergroup parent;
    final private ArrayList<Supergroup> childrens;
    final private LinkedHashSet<IGroup> cacheIGroup;
    final private Map<String, IGroupRegister> mapOwnGroup;
    private String[] sequence;
    private String[] ownGroup;
    private IEventListener[] cacheEventListeners;

    public Supergroup(Supergroup parent, String[] namesGroup) {
        this.parent = parent;
        childrens = new ArrayList<Supergroup>();
        priorityGroups = new IGroupRegister[5];
        for (int i = 0; i < 5; i++) priorityGroups[i] = new GroupPriority();
        cacheIGroup = new LinkedHashSet<IGroup>();
        mapOwnGroup = new HashMap<>();
        sequence = compileSequenceNamesGroup(namesGroup);
        ownGroup = extractOwnGroup();
    }

    private String[] extractOwnGroup() {
        Set<String> exclude = new HashSet<>(Arrays.asList(next, slot));

        Set<String> set1 = new HashSet<>((parent != null) ? Arrays.asList(parent.sequence) : new ArrayList<>());
        Set<String> set2 = new HashSet<>(Arrays.asList(sequence));

        return Stream.concat(Arrays.stream((parent != null) ? parent.sequence : new String[0]), Arrays.stream(sequence))
            .filter(s -> (set1.contains(s) ^ set2.contains(s)) && !exclude.contains(s))
            .distinct()
            .toArray(String[]::new);
    }

    private String[] compileSequenceNamesGroup(String[] namesGroup) {
        {
            int countNextPhase = 0;
            for (String nameGroup : namesGroup) {
                if (next.equals(nameGroup)) {
                    countNextPhase++;
                }
            }

            if (countNextPhase > 4) {
                // Reset of default sequence if countNextPhase > 4
                namesGroup = new String[] { slot, next, slot, next, slot, next, slot, next, slot };
            } else if (countNextPhase < 4) {
                // Addition sequence if countNextPhase < 4
                int l0 = (4 - countNextPhase) / 2;
                int l1 = l0 + (4 - countNextPhase) % 2;
                List<String> res = new ArrayList<>();
                for (int i = 0; i < l0; i++) res.add(next);
                Collections.addAll(res, namesGroup);
                for (int i = 0; i < l1; i++) res.add(next);
                namesGroup = res.toArray(new String[0]);
            }
        }

        {
            // Reset duplicate default_slot between next_phase to last.
            boolean previousNextPhase = true;
            boolean isDetectDubDefaultSlot = false;
            int indexPreviousDefaultSlot = -1;
            int c = -1;
            List<String> iterRes = new ArrayList<>();
            List<String> res = new ArrayList<>();
            for (String nameGroup : namesGroup) {
                c++;
                if (next.equals(nameGroup)) {
                    c = -1;
                    if (previousNextPhase) {
                        iterRes.add(slot);
                    } else if (isDetectDubDefaultSlot) {
                        iterRes.add(iterRes.remove(indexPreviousDefaultSlot));
                    }
                    iterRes.add(next);
                    previousNextPhase = true;
                    isDetectDubDefaultSlot = false;
                    res.addAll(iterRes);
                    iterRes.clear();
                } else if (slot.equals(nameGroup)) {
                    if (!previousNextPhase) {
                        isDetectDubDefaultSlot = true;
                        continue;
                    }
                    indexPreviousDefaultSlot = c;
                    previousNextPhase = false;
                    iterRes.add(nameGroup);
                } else {
                    iterRes.add(nameGroup);
                }
            }

            if (previousNextPhase) {
                iterRes.add(slot);
            } else if (isDetectDubDefaultSlot) {
                iterRes.add(iterRes.remove(indexPreviousDefaultSlot));
            }
            res.addAll(iterRes);

            namesGroup = res.toArray(new String[0]);
        }

        List<String> res = new ArrayList<>();
        {
            // Reset other duplicate
            List<String> preRes = new ArrayList<>();
            preRes.add(next);
            List<String> antiDub = new ArrayList<>();
            for (String nameGroup : namesGroup) {
                if (!preRes.contains(nameGroup) || next.equals(nameGroup) || slot.equals(nameGroup)) {
                    preRes.add(nameGroup);
                } else {
                    antiDub.add(nameGroup);
                }
            }

            for (String nameGroup : preRes) if (!antiDub.contains(nameGroup)) res.add(nameGroup);
        }

        if (parent != null) {
            // Inheritance sequence parent
            List<String> resInheritance = new ArrayList<>();
            int size = res.size();
            int i0 = 0, i1 = 1;
            boolean isReadParent = false;
            while (i0 < size || i1 < parent.sequence.length) {
                if (isReadParent) {
                    if (next.equals(parent.sequence[i1])) {
                        isReadParent = false;
                    } else if (slot.equals(parent.sequence[i1])) {
                        resInheritance.add(slot);
                    } else if (!res.contains(parent.sequence[i1])) {
                        resInheritance.add(parent.sequence[i1]);
                    }
                    i1++;
                } else {
                    if (!slot.equals(res.get(i0))) resInheritance.add(res.get(i0));
                    else isReadParent = true;
                    i0++;
                }
            }

            namesGroup = resInheritance.toArray(new String[0]);
        } else {
            namesGroup = res.toArray(new String[0]);
        }

        return namesGroup;
    }

    public void addChildrenSupergroup(Supergroup supergroup) {
        childrens.add(supergroup);
    }

    public void register(String descriptor, IEventListener listener, EventPriority priority) {
        rebuild = rebuild | search(priority, descriptor).register(descriptor, listener, priority);
    }

    public void unregister(IEventListener listener) {
        for (IGroupRegister group : mapOwnGroup.values()) if (group.unregister(listener)) {
            rebuild = true;
            return;
        }
    }

    private IGroupRegister search(EventPriority priority, String descriptor) {
        for (IGroupRegister group : mapOwnGroup.values()) if (group.hasID(descriptor)) return group;
        for (String nameGroup : ownGroup)
            if (!mapOwnGroup.containsKey(nameGroup) && Config.checkGroup(nameGroup, descriptor)) {
                rebuildGroups = true;
                rebuild = true;
                IGroupRegister newIGroupRegister = new Group(
                    Config.getParams("group", nameGroup),
                    Config.isOrdered(nameGroup));
                mapOwnGroup.put(nameGroup, newIGroupRegister);
                return newIGroupRegister;
            }
        return priorityGroups[priority.ordinal()];
    }

    public EventListener search(String descriptor) {
        for (IGroupRegister group : mapOwnGroup.values()) if (group.hasID(descriptor)) return group.search(descriptor);
        for (IGroupRegister group : priorityGroups) if (group.hasID(descriptor)) return group.search(descriptor);
        return null;
    }

    private IGroupRegister getGroupByName(String nameGroup) {
        IGroupRegister group = mapOwnGroup.get(nameGroup);
        if (group != null) return group;
        if (parent != null) return parent.getGroupByName(nameGroup);
        return null;
    }

    private void buildCacheGroups() {
        rebuildGroups = false;
        cacheIGroup.clear();

        int phase = 0;
        for (String nameGroup : sequence) {
            if (next.equals(nameGroup)) cacheIGroup.add(switchers[phase]);
            else if (slot.equals(nameGroup)) {
                Supergroup supergroup = this;

                while (supergroup != null) {
                    cacheIGroup.add(supergroup.priorityGroups[phase]);
                    supergroup = supergroup.parent;
                }

                phase++;
            } else {
                IGroupRegister group = getGroupByName(nameGroup);
                if (group != null) cacheIGroup.add(group);
            }
        }

        for (Supergroup children : childrens) children.buildCacheGroups();
    }

    private void buildCache() {
        rebuild = false;
        if (rebuildGroups) buildCacheGroups();

        ArrayList<IEventListener> listListener = new ArrayList<>();
        for (IGroup group : cacheIGroup) group.transferEventListener(listListener);

        cacheEventListeners = listListener.toArray(new IEventListener[0]);
    }

    public IEventListener[] getListeners() {
        if (rebuild) buildCache();
        return cacheEventListeners;
    }

    public void dispose() {
        for (IGroupRegister group : mapOwnGroup.values()) group.dispose();
        for (IGroupRegister group : priorityGroups) group.dispose();
    }

    public void getAllDescriptor(Set<String> res) {
        for (IGroupRegister group : mapOwnGroup.values()) Collections.addAll(res, group.getAllActiveDescriptors());
        for (IGroupRegister group : priorityGroups) Collections.addAll(res, group.getAllActiveDescriptors());
    }

    public void reload(String[] namesGroup) {
        sequence = compileSequenceNamesGroup(namesGroup);
        ownGroup = extractOwnGroup();
        rebuildGroups = true;

        List<EventListener> transit = new ArrayList<EventListener>();
        for (IGroupRegister group : mapOwnGroup.values()) group.dispose(transit);
        for (IGroupRegister group : priorityGroups) group.dispose(transit);

        mapOwnGroup.clear();
        for (int i = 0; i < 5; i++) priorityGroups[i] = new GroupPriority();

        for (EventListener transitListener : transit) {
            EventPriority priority = transitListener.getPrimePriority();
            for (IEventListener eventListener : transitListener.getEventListeners())
                register(transitListener.getDescriptor(), eventListener, priority);
        }
    }
}

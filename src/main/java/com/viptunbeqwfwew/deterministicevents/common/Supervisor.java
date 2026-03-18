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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraftforge.common.MinecraftForge;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import com.gtnewhorizon.gtnhlib.eventbus.MethodInfo;
import com.viptunbeqwfwew.deterministicevents.DeterministicEvents;
import com.viptunbeqwfwew.deterministicevents.common.group.EventListener;
import com.viptunbeqwfwew.deterministicevents.config.Config;
import com.viptunbeqwfwew.deterministicevents.utils.HelperDescriptor;

import cpw.mods.fml.common.eventhandler.EventBus;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.IEventListener;
import cpw.mods.fml.common.eventhandler.ListenerList;

public class Supervisor {

    final private HashMap<IEventListener, Triple<String, ListenerList, EventPriority>> capture = new HashMap<>();
    final private ArrayList<Triple<ListenerList, ListenerList, String>> captureSupergroup = new ArrayList<>();

    private boolean isProhibitedRegistration = true;

    private boolean isProcessAllow = false;
    private final int redirectId;

    private String descriptor;
    private final ConcurrentHashMap<String, com.viptunbeqwfwew.deterministicevents.common.group.EventListener> descriptorCache = new ConcurrentHashMap<String, com.viptunbeqwfwew.deterministicevents.common.group.EventListener>();
    private final ConcurrentHashMap<IEventListener, com.viptunbeqwfwew.deterministicevents.common.group.EventListener> objCache = new ConcurrentHashMap<IEventListener, com.viptunbeqwfwew.deterministicevents.common.group.EventListener>();
    private final LinkedHashMap<String, Supergroup> supergroups = new LinkedHashMap<>();

    public Supervisor() {
        int redirectId0;
        try {
            Field busID = EventBus.class.getDeclaredField("busID");
            busID.setAccessible(true);
            try {
                redirectId0 = (int) busID.get(MinecraftForge.EVENT_BUS);
            } catch (IllegalAccessException e) {
                redirectId0 = 0;
            }
            busID.setAccessible(false);
        } catch (NoSuchFieldException e) {
            redirectId0 = 0;
        }
        redirectId = redirectId0;
    }

    public void setDescriptor(Method method) {
        descriptor = method.getDeclaringClass()
            .getName() + "@"
            + method.getName()
            + "("
            + method.getParameterTypes()[0].getName()
            + ")";
    }

    public void setDescriptor(MethodInfo methodInfo) {
        descriptor = methodInfo.declaringClass + "@"
            + methodInfo.name
            + "("
            + HelperDescriptor.extractEventTypeGTNH(methodInfo.desc)
            + ")";
    }

    private void setSupergroupOnListenerList(ListenerList listenerList, Supergroup supergroup) {
        // Auto generate code
    }

    private Supergroup getSupergroupFromListenerList(ListenerList listenerList) {
        // Auto generate code
        return new Supergroup(null, new String[0]);
    }

    void allowRegistration() {
        isProcessAllow = true;
        for (Map.Entry<IEventListener, Triple<String, ListenerList, EventPriority>> entry : capture.entrySet())
            entry.getValue()
                .getMiddle()
                .unregister(redirectId, entry.getKey());

        isProhibitedRegistration = false;
        for (Triple<ListenerList, ListenerList, String> tripleDescriptorParentCurrent : captureSupergroup) {
            registerListenerList(
                tripleDescriptorParentCurrent.getLeft(),
                tripleDescriptorParentCurrent.getMiddle(),
                tripleDescriptorParentCurrent.getRight());
        }
        captureSupergroup.clear();

        isProcessAllow = false;
        for (Map.Entry<IEventListener, Triple<String, ListenerList, EventPriority>> entry : capture.entrySet()) {
            descriptor = entry.getValue()
                .getLeft();
            entry.getValue()
                .getMiddle()
                .register(
                    redirectId,
                    entry.getValue()
                        .getRight(),
                    entry.getKey());
        }
        capture.clear();
    }

    public void reload() {
        Config.load(DeterministicEvents.proxy.dconfDir);

        objCache.clear();
        descriptorCache.clear();

        for (Map.Entry<String, Supergroup> entry : supergroups.entrySet()) entry.getValue()
            .reload(Config.getParams("supergroup", entry.getKey()));
    }

    public void putCache(String descriptor,
        com.viptunbeqwfwew.deterministicevents.common.group.EventListener eventListener) {
        descriptorCache.put(descriptor, eventListener);
    }

    public void putCache(IEventListener obj,
        com.viptunbeqwfwew.deterministicevents.common.group.EventListener eventListener) {
        objCache.put(obj, eventListener);
    }

    public void registerListenerList(ListenerList parent, ListenerList current, String eventTypeName) {
        if (isProhibitedRegistration) {
            captureSupergroup.add(new ImmutableTriple<>(parent, current, eventTypeName));
            return;
        }
        Supergroup sParent = (parent != null) ? getSupergroupFromListenerList(parent) : null;
        Supergroup newSupergroup = new Supergroup(sParent, Config.getParams("supergroup", eventTypeName));
        setSupergroupOnListenerList(current, newSupergroup);
        if (sParent != null) sParent.addChildrenSupergroup(newSupergroup);
        supergroups.put(eventTypeName, newSupergroup);
    }

    public boolean register(ListenerList current, int id, EventPriority priority, IEventListener listener) {
        if (id != redirectId) return false;
        if (isProhibitedRegistration) {
            capture.put(listener, new ImmutableTriple<>(descriptor, current, priority));
            return false;
        }
        com.viptunbeqwfwew.deterministicevents.common.group.EventListener eventListener = descriptorCache
            .get(descriptor);
        if (eventListener != null) {
            eventListener.add(listener);
            return true;
        }
        Supergroup supergroup = getSupergroupFromListenerList(current);
        if (supergroup != null) supergroup.register(descriptor, listener, priority);
        else DeterministicEvents.LOG.warn(
            "Failed to register listener {}. The object in the ListenerList ({}) is not present (null).",
            listener,
            current);
        return true;
    }

    public boolean unregister(ListenerList current, int id, IEventListener listener) {
        if (id != redirectId || isProcessAllow) return false;
        if (isProhibitedRegistration) {
            capture.remove(listener);
            return false;
        }
        com.viptunbeqwfwew.deterministicevents.common.group.EventListener eventListener = objCache.get(listener);
        if (eventListener != null) {
            objCache.remove(listener);
            eventListener.remove(listener);
            return true;
        }
        Supergroup supergroup = getSupergroupFromListenerList(current);
        if (supergroup != null) supergroup.unregister(listener);
        else DeterministicEvents.LOG
            .warn("Error unregistering. Listener {} not found in ListenerList {}.", listener, current);
        return true;
    }

    public boolean dispose(int id) {
        if (id != redirectId) return false;
        if (isProhibitedRegistration) {
            capture.clear();
            captureSupergroup.clear();
            return false;
        }
        objCache.clear();
        descriptorCache.clear();
        for (Supergroup supergroup : supergroups.values()) supergroup.dispose();
        return true;
    }

    public com.viptunbeqwfwew.deterministicevents.common.group.EventListener search(String descriptor) {
        EventListener eventListener = descriptorCache.get(descriptor);
        if (eventListener != null) {
            return eventListener;
        }
        Supergroup supergroup = supergroups.get(HelperDescriptor.extractEventType(descriptor));
        if (supergroup == null) return null;
        return supergroup.search(descriptor);
    }

    public IEventListener[] getListeners(ListenerList current, int id) {
        if (id != redirectId || isProhibitedRegistration) return null;
        Supergroup supergroup = getSupergroupFromListenerList(current);
        if (supergroup == null) {
            DeterministicEvents.LOG.warn("Miss supergroup in ListenerList {}.", current);
            return new IEventListener[0];
        }
        return supergroup.getListeners();
    }

    public String[] getAllDescriptor() {
        Set<String> res = new LinkedHashSet<String>();
        for (Supergroup supergroup : supergroups.values()) supergroup.getAllDescriptor(res);
        return res.toArray(new String[0]);
    }
}

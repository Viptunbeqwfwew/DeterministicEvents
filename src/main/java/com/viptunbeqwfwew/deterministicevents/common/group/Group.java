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
package com.viptunbeqwfwew.deterministicevents.common.group;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.IEventListener;

public class Group implements IGroupRegister {

    private final ArrayList<EventListener> eventListeners = new ArrayList<EventListener>();
    private final String[] eventDescriptors;
    private final boolean isOrder;
    private boolean rebuild = false;
    private boolean returnRebuild = false;

    public Group() {
        this(new String[0], false);
    }

    public Group(String[] eventDescriptors, boolean isOrder) {
        this.eventDescriptors = eventDescriptors;
        this.isOrder = isOrder;
    }

    @Override
    @SuppressWarnings("NullPointerException")
    public boolean register(String descriptor, IEventListener eventListener, EventPriority primePriority) {
        returnRebuild = false;
        search(descriptor, false, primePriority).add(eventListener);
        return returnRebuild;
    }

    @Override
    public boolean unregister(IEventListener eventListener) {
        for (EventListener listener : eventListeners) if (listener.remove(eventListener)) return true;
        return false;
    }

    public EventListener search(String descriptor) {
        return search(descriptor, true, EventPriority.NORMAL);
    }

    private EventListener search(String descriptor, boolean notForRegistration, EventPriority primePriority) {
        for (EventListener eventListener : eventListeners) if (descriptor.equals(eventListener.getDescriptor())) {
            if (notForRegistration) eventListener.markSearch();
            return eventListener;
        }
        if (notForRegistration) return null;
        returnRebuild = true;
        rebuild = true;
        EventListener eventListener = new EventListener(descriptor, primePriority);
        eventListeners.add(eventListener);
        return eventListener;
    }

    @Override
    public void transferEventListener(List<IEventListener> list) {
        if (rebuild) buildCash();
        list.addAll(eventListeners);
    }

    public String[] getAllActiveDescriptors() {
        int len = eventListeners.size();
        String[] res = new String[len];

        for (int i = 0; i < len; i++) res[i] = eventListeners.get(i)
            .getDescriptor();

        return res;
    }

    private void buildCash() {
        if (!isOrder) {
            rebuild = false;
            return;
        }

        Map<String, Integer> map = new HashMap<String, Integer>();
        for (int i = 0; i < eventDescriptors.length; i++) map.put(eventDescriptors[i], i);

        eventListeners.sort(Comparator.comparingInt(obj -> map.getOrDefault(obj.getDescriptor(), Integer.MAX_VALUE)));

        rebuild = false;
    }

    public boolean hasID(String descriptor) {
        for (String id : eventDescriptors) if (id.equals(descriptor)) return true;
        return false;
    }

    @Override
    public void dispose() {
        for (EventListener eventListener : eventListeners) eventListener.removeAll();
    }

    @Override
    public void dispose(List<EventListener> transit) {
        transit.addAll(eventListeners);
        eventListeners.clear();
    }
}

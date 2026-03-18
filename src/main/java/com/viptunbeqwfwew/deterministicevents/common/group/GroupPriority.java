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
import java.util.List;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.IEventListener;

public class GroupPriority implements IGroupRegister {

    private final ArrayList<EventListener> eventListeners = new ArrayList<EventListener>();
    private boolean returnRebuild;

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
        EventListener eventListener = new EventListener(descriptor, primePriority);
        eventListeners.add(eventListener);
        return eventListener;
    }

    @Override
    public void transferEventListener(List<IEventListener> list) {
        list.addAll(eventListeners);
    }

    public String[] getAllActiveDescriptors() {
        int len = eventListeners.size();
        String[] res = new String[len];

        for (int i = 0; i < len; i++) res[i] = eventListeners.get(i)
            .getDescriptor();

        return res;
    }

    public boolean hasID(String descriptor) {
        for (EventListener eventListener : eventListeners)
            if (descriptor.equals(eventListener.getDescriptor())) return true;
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

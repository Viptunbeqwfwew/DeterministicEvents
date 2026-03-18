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

import com.viptunbeqwfwew.deterministicevents.DeterministicEvents;
import com.viptunbeqwfwew.deterministicevents.common.AutoOptimizer;
import com.viptunbeqwfwew.deterministicevents.config.Config;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.IEventListener;

public class EventListener implements IEventListener {

    private final AutoOptimizer remove = new AutoOptimizer((short) 10, 60 * 10);
    private final AutoOptimizer search = new AutoOptimizer((short) 10, 60 * 10);
    private final String descriptor;
    private final EventPriority primePriority;
    private final ArrayList<IEventListener> eventListeners = new ArrayList<IEventListener>();
    private boolean isMute = false;
    final private String[] muteDescriptors;

    public EventListener(String descriptor, EventPriority primePriority) {
        this.descriptor = descriptor;
        muteDescriptors = Config.getParams("mute", descriptor);
        this.primePriority = primePriority;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public void add(IEventListener eventListener) {
        if (remove.isCached()) {
            DeterministicEvents.proxy.supervisor.putCache(eventListener, this);
        }
        eventListeners.add(eventListener);
    }

    public boolean remove(IEventListener eventListener) {
        if (!remove.isCached() && remove.markTime()) {
            if (!search.isCached()) DeterministicEvents.proxy.supervisor.putCache(descriptor, this);
            for (IEventListener listener : eventListeners) {
                if (listener != eventListener) {
                    DeterministicEvents.proxy.supervisor.putCache(listener, this);
                }
            }
        }
        return eventListeners.remove(eventListener);
    }

    void markSearch() {
        if (!remove.isCached() && !search.isCached() && search.markTime())
            DeterministicEvents.proxy.supervisor.putCache(descriptor, this);
    }

    @Override
    public void invoke(Event event) {
        if (isMute) return;
        ArrayList<EventListener> muteEvents = new ArrayList<EventListener>();
        for (String muteDescriptor : muteDescriptors) {
            EventListener eventListener = DeterministicEvents.proxy.supervisor.search(muteDescriptor);
            if (!eventListener.isMute) {
                muteEvents.add(eventListener);
                eventListener.isMute = true;
            }
        }
        try {
            for (IEventListener eventListener : eventListeners) eventListener.invoke(event);
        } finally {
            for (EventListener eventListener : muteEvents) eventListener.isMute = false;
        }
    }

    public void removeAll() {
        for (IEventListener eventListener : eventListeners) remove(eventListener);
        remove.reset();
        search.reset();
    }

    public IEventListener[] getEventListeners() {
        return eventListeners.toArray(new IEventListener[0]);
    }

    public EventPriority getPrimePriority() {
        return primePriority;
    }
}

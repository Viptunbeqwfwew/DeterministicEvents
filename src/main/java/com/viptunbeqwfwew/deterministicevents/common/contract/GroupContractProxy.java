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
package com.viptunbeqwfwew.deterministicevents.common.contract;

import java.util.ArrayList;
import java.util.List;

import com.viptunbeqwfwew.deterministicevents.common.group.EventListener;
import com.viptunbeqwfwew.deterministicevents.common.group.IGroupRegister;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.IEventListener;

public class GroupContractProxy implements IGroupRegister, IEventListener {

    final private IGroupRegister proxy;
    final private ContractContext context;
    final private ArrayList<IEventListener> eventListeners = new ArrayList<>();

    public GroupContractProxy(IGroupRegister proxy, ContractContext ctx) {
        this.proxy = proxy;
        context = ctx;
        context.markUpdateListener();
    }

    @Override
    public void transferEventListener(List<IEventListener> list) {
        list.add(this);
    }

    @Override
    public boolean register(String descriptor, IEventListener eventListener, EventPriority primePriority) {
        if (proxy.register(descriptor, eventListener, primePriority)) {
            eventListeners.clear();
            proxy.transferEventListener(eventListeners);
            context.markUpdateListener();
            return true;
        }
        return false;
    }

    @Override
    public boolean unregister(IEventListener eventListener) {
        if (proxy.unregister(eventListener)) {
            eventListeners.clear();
            proxy.transferEventListener(eventListeners);
            context.markUpdateListener();
            return true;
        }
        return false;
    }

    @Override
    public void dispose(List<EventListener> transit) {
        proxy.dispose(transit);
    }

    @Override
    public void dispose() {
        proxy.dispose();
        eventListeners.clear();
        context.markUpdateListener();
    }

    @Override
    public String[] getAllActiveDescriptors() {
        return proxy.getAllActiveDescriptors();
    }

    @Override
    public boolean hasID(String descriptor) {
        return proxy.hasID(descriptor);
    }

    @Override
    public EventListener search(String descriptor) {
        return proxy.search(descriptor);
    }

    @Override
    public void invoke(Event event) {
        if (context.checkConditions(event)) return;

        for (IEventListener eventListener : eventListeners) eventListener.invoke(event);
    }
}

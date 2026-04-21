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

import com.viptunbeqwfwew.deterministicevents.common.group.IActivatedGroup;
import com.viptunbeqwfwew.deterministicevents.common.group.IGroupRegister;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.IEventListener;

public class GroupContractResolver implements IActivatedGroup, IEventListener {

    final private IGroupRegister proxy;

    final private ContractContext context;

    final private ArrayList<IEventListener> eventListeners = new ArrayList<>();

    public GroupContractResolver(IGroupRegister proxy, ContractContext ctx) {
        this.proxy = proxy;
        context = ctx;
        this.proxy.transferEventListener(eventListeners);
    }

    @Override
    public void activate() {
        context.activeContext();
    }

    @Override
    public void invoke(Event event) {
        if (!context.getAllowActive()) return;
        if (context.isUpdateListener()) {
            eventListeners.clear();
            proxy.transferEventListener(eventListeners);
        }
        Event eventContext = context.getEvent(event);
        for (IEventListener eventListener : eventListeners) {
            eventListener.invoke(eventContext);
        }
        if (event.isCancelable()) event.setCanceled(eventContext.isCanceled());
    }

    @Override
    public void transferEventListener(List<IEventListener> list) {
        list.add(this);
    }
}

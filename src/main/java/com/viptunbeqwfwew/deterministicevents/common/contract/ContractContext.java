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

import com.viptunbeqwfwew.deterministicevents.DeterministicEvents;

import cpw.mods.fml.common.eventhandler.Event;

public abstract class ContractContext {

    protected Event event;
    private boolean isAllowActive = false;
    private boolean isUpdateListener = false;
    private boolean isActiveContext = false;

    protected abstract boolean checkASMConditions(Event event); // Auto generate

    public void activeContext() {
        isActiveContext = true;
    }

    public boolean getAllowActive() {
        boolean res = isAllowActive;
        isAllowActive = false;
        return res;
    }

    public boolean checkConditions(Event event) {
        isAllowActive = isActiveContext && checkASMConditions(event);
        if (isAllowActive) this.event = event;
        return isAllowActive;
    }

    protected abstract void updateEventData(Event event); // Auto generate

    public Event getEvent(Event dataEvent) {
        updateEventData(dataEvent);
        if (event.isCancelable()) event.setCanceled(dataEvent.isCanceled());
        return event;
    }

    public void markUpdateListener() {
        isUpdateListener = true;
    }

    public boolean isUpdateListener() {
        boolean res = isUpdateListener;
        isUpdateListener = false;
        return res;
    }

    @SuppressWarnings("unused")
    protected void error() {
        DeterministicEvents.LOG.warn(
            "Updating data for event \"{}\" failed.",
            event.getClass()
                .getCanonicalName());
    }
}

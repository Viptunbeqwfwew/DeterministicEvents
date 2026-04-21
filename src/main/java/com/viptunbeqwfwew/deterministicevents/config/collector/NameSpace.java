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

package com.viptunbeqwfwew.deterministicevents.config.collector;

import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.viptunbeqwfwew.deterministicevents.DeterministicEvents;
import com.viptunbeqwfwew.deterministicevents.config.DeclarativeObject;

public class NameSpace {

    final private boolean isOnce;
    final private boolean isHybrid;
    final private HashSet<String> removedName;
    final private HashSet<String> immutableNames;
    final private HashMap<String, Pair<ACollector, DeclarativeObject>> objects;

    public NameSpace() {
        this(false, false);
    }

    public NameSpace(boolean isOnce, boolean isHybrid) {
        this.isOnce = isOnce || isHybrid;
        this.isHybrid = isHybrid;

        removedName = this.isOnce ? new HashSet<>() : null;
        immutableNames = this.isHybrid ? new HashSet<>() : null;

        objects = new HashMap<>();
    }

    boolean isNameAccepted(DeclarativeObject object, ACollector collector) {
        String name = object.name;

        if (isOnce) {
            if (removedName.contains(name)) {
                return false;
            }

            if (!isHybrid) {
                if (hasConflict(object, collector)) {
                    rollback(object, collector);
                    DeterministicEvents.LOG.warn(
                        "The object '{}' has been declared multiple times. Current and future versions will be ignored.",
                        name);
                    return false;
                }
                objects.put(name, new ImmutablePair<>(collector, object));
                return true;
            }

            if (immutableNames.contains(name) && hasConflict(object, collector)) {
                rollback(object, collector);
                DeterministicEvents.LOG
                    .error("Conflict detected for immune object '{}'. Data removed to prevent corruption.", name);
                return false;
            }
            objects.put(name, new ImmutablePair<>(collector, object));
        }

        return true;
    }

    private void rollback(DeclarativeObject object, ACollector collector) {
        String name = object.name;

        Pair<ACollector, DeclarativeObject> pair = objects.get(name);
        if (pair == null) return;

        collector.rollback(object);
        pair.getLeft()
            .rollback(pair.getRight());
        objects.remove(name);
        removedName.add(name);
    }

    private boolean hasConflict(DeclarativeObject object, ACollector collector) {
        String name = object.name;
        if (objects.containsKey(name)) return collector.hasConflict(
            object,
            objects.get(name)
                .getLeft());
        else return collector.hasConflict(object, collector);
    }

    public void addImmutableName(String name) {
        if (isHybrid) immutableNames.add(name);
    }
}

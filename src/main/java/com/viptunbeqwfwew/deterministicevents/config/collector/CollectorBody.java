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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.viptunbeqwfwew.deterministicevents.config.DeclarativeObject;

public class CollectorBody extends ACollector {

    final private Map<String, List<String>> objects;

    public CollectorBody(Map<String, List<String>> objects) {
        this(new NameSpace(), objects);
    }

    public CollectorBody(NameSpace nameSpace, Map<String, List<String>> objects) {
        super(nameSpace);
        this.objects = objects;
    }

    @Override
    protected void store(DeclarativeObject object) {
        objects.computeIfAbsent(object.name, k -> new ArrayList<>())
            .addAll(object.params);
    }

    @Override
    public void rollback(DeclarativeObject object) {
        objects.remove(object.name);
    }

    @Override
    public boolean hasConflict(DeclarativeObject object, ACollector collector) {
        if (!(collector instanceof CollectorBody)) return true;
        return objects.containsKey(object.name);
    }
}

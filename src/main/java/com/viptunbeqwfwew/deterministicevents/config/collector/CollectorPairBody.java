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

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.viptunbeqwfwew.deterministicevents.config.DeclarativeObject;

public class CollectorPairBody extends ACollector {

    final private Map<String, List<Pair<String, String>>> objects;

    public CollectorPairBody(NameSpace nameSpase, Map<String, List<Pair<String, String>>> objects) {
        super(nameSpase);
        this.objects = objects;
    }

    @Override
    protected void store(DeclarativeObject object) {
        List<Pair<String, String>> pairList = objects.computeIfAbsent(object.name, k -> new ArrayList<>());
        List<String> params = object.params;

        for (int i = 0, size = params.size() - 1; i < size; i += 2)
            pairList.add(ImmutablePair.of(params.get(i), params.get(i + 1)));
    }

    @Override
    public void rollback(DeclarativeObject object) {

    }

    @Override
    public boolean hasConflict(DeclarativeObject object, ACollector collector) {
        return false;
    }
}

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

import com.viptunbeqwfwew.deterministicevents.config.DeclarativeObject;

public class ModifierReverse implements IPreModifier {

    @Override
    public DeclarativeObject[] pre(DeclarativeObject[] declarativeObjects) {
        int size = 0;
        for (DeclarativeObject declarativeObject : declarativeObjects) {
            size += declarativeObject.params.size();
        }
        DeclarativeObject[] res = new DeclarativeObject[size];
        int c = 0;
        for (DeclarativeObject declarativeObject : declarativeObjects) {
            String type = declarativeObject.type;
            String name = declarativeObject.name;
            TypeModification[] typeModifications = declarativeObject.modifications;
            for (String param : declarativeObject.params) {
                DeclarativeObject obj = new DeclarativeObject(type, param, typeModifications);
                obj.params.add(name);
                res[c++] = obj;
            }
        }
        return res;
    }
}

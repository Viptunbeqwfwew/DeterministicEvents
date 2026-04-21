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

public abstract class ACollector {

    final protected NameSpace nameSpace;

    public ACollector(NameSpace nameSpace) {
        this.nameSpace = nameSpace;
    }

    public void ingest(DeclarativeObject object, IPreModifier[] pre, IModifier[] modifiers) {
        DeclarativeObject[] declarativeObjects = new DeclarativeObject[] { object };

        for (IPreModifier preModifier : pre) declarativeObjects = preModifier.pre(declarativeObjects);
        for (IModifier modifier : modifiers)
            for (DeclarativeObject declarativeObject : declarativeObjects) modifier.apply(declarativeObject);

        for (DeclarativeObject declarativeObject : declarativeObjects)
            if (nameSpace.isNameAccepted(declarativeObject, this)) {
                store(declarativeObject);
            }
    }

    abstract protected void store(DeclarativeObject object);

    abstract public void rollback(DeclarativeObject object);

    abstract public boolean hasConflict(DeclarativeObject object, ACollector collector);
}

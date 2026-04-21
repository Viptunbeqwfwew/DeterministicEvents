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

package com.viptunbeqwfwew.deterministicevents.config.parse;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.Arrays;
import java.util.InputMismatchException;

import com.viptunbeqwfwew.deterministicevents.config.DeclarativeObject;
import com.viptunbeqwfwew.deterministicevents.config.collector.TypeModification;

public abstract class AStructureParse {

    final protected String type;
    final protected INameParse nameParse;
    final protected TypeModification[] allowedModifiers;

    public AStructureParse(String type, INameParse nameParse, TypeModification[] allowedModifiers) {
        this.type = type;
        this.nameParse = nameParse;
        this.allowedModifiers = allowedModifiers;
    }

    public DeclarativeObject parse(TypeModification[] typeModifications, StreamTokenizer streamTokenizer) {
        if (!compareTypeModifications(typeModifications, allowedModifiers)) {
            throw new InputMismatchException(
                String.format(
                    "The parser detected an invalid modification for object \"%s\" at line %d.",
                    type,
                    streamTokenizer.lineno()));
        }

        DeclarativeObject declarativeObject;
        try {
            String name = nameParse.parse(streamTokenizer);

            if (name == null) throw new IllegalArgumentException(
                String.format("Token \"name\" is null on line %d.", streamTokenizer.lineno()));

            declarativeObject = new DeclarativeObject(type, name, typeModifications);
            parseAfterName(streamTokenizer, declarativeObject);
        } catch (IOException ignore) {
            return null;
        }

        return declarativeObject;
    }

    protected boolean compareTypeModifications(TypeModification[] typeModifications0,
        TypeModification[] typeModifications1) {
        if (typeModifications1.length == 0) return typeModifications0.length == 0;
        return Arrays.stream(typeModifications0)
            .allMatch(
                obj -> Arrays.stream(typeModifications1)
                    .anyMatch(pattern -> obj == pattern));
    }

    abstract protected void parseAfterName(StreamTokenizer streamTokenizer, DeclarativeObject declarativeObject)
        throws IOException;
}

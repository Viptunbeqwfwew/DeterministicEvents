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
import java.util.InputMismatchException;

import com.viptunbeqwfwew.deterministicevents.config.DeclarativeObject;
import com.viptunbeqwfwew.deterministicevents.config.collector.TypeModification;

public class StructureParseWithParameters extends AStructureParse {

    final private IParameterParse parameterParse;

    public StructureParseWithParameters(String type, INameParse nameParse, IParameterParse parameterParse) {
        this(type, nameParse, parameterParse, new TypeModification[0]);
    }

    public StructureParseWithParameters(String type, INameParse nameParse, IParameterParse parameterParse,
        TypeModification[] allowedModifiers) {
        super(type, nameParse, allowedModifiers);
        this.parameterParse = parameterParse;
    }

    @Override
    protected void parseAfterName(StreamTokenizer streamTokenizer, DeclarativeObject declarativeObject)
        throws IOException {
        if (streamTokenizer.nextToken() == '{') {
            while (streamTokenizer.nextToken() != '}' && streamTokenizer.ttype != StreamTokenizer.TT_EOF) {
                if (streamTokenizer.ttype == '{') throw new InputMismatchException(
                    String.format("Unexpected second opening curly brace on line %d.", streamTokenizer.lineno()));

                streamTokenizer.pushBack();
                parameterParse.parse(declarativeObject, streamTokenizer);
            }
        } else {
            throw new InputMismatchException(
                String.format(
                    "The parser expected \"{\" but got \"%s\" on line %d",
                    streamTokenizer.sval,
                    streamTokenizer.lineno()));
        }
        if (streamTokenizer.ttype != '}') throw new InputMismatchException("Reached end of file while parsing.");
    }
}

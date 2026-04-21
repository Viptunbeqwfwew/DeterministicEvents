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

import com.viptunbeqwfwew.deterministicevents.config.DeclarativeObject;
import com.viptunbeqwfwew.deterministicevents.config.collector.TypeModification;

public class StructureParseContract extends AStructureParse {

    final private IParameterParse typeParse;

    public StructureParseContract(String type, StringValParse typeParse, TypeModification[] allowedModifiers) {
        super(type, typeParse, allowedModifiers);
        this.typeParse = typeParse;
    }

    @Override
    protected void parseAfterName(StreamTokenizer streamTokenizer, DeclarativeObject declarativeObject)
        throws IOException {
        typeParse.parse(declarativeObject, streamTokenizer);
        typeParse.parse(declarativeObject, streamTokenizer);
        typeParse.parse(declarativeObject, streamTokenizer);
    }
}

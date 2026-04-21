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

public class StringValParse implements IParameterParse, INameParse {

    @Override
    public void parse(DeclarativeObject declarativeObject, StreamTokenizer streamTokenizer) throws IOException {
        declarativeObject.params.add(parse(streamTokenizer));
    }

    @Override
    public String parse(StreamTokenizer streamTokenizer) throws IOException {
        streamTokenizer.nextToken();
        if (streamTokenizer.ttype != StreamTokenizer.TT_WORD) {
            throw new IllegalArgumentException(
                String.format("Token \"params\" is null on line %d.", streamTokenizer.lineno()));
        }
        return streamTokenizer.sval;
    }
}

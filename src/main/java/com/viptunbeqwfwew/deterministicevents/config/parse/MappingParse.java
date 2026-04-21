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

public class MappingParse extends StringValParse {

    final private String delimiter;

    public MappingParse(String delimiter) {
        this.delimiter = delimiter;
    }

    @Override
    public void parse(DeclarativeObject declarativeObject, StreamTokenizer streamTokenizer) throws IOException {
        super.parse(declarativeObject, streamTokenizer);
        streamTokenizer.ordinaryChar('-');
        streamTokenizer.wordChars('-', '-');
        streamTokenizer.wordChars('>', '>');
        streamTokenizer.nextToken();
        if (streamTokenizer.ttype != StreamTokenizer.TT_WORD || !streamTokenizer.sval.equals(delimiter)) {
            throw new IllegalArgumentException(
                String.format(
                    "Token \"delimiter\" is not word or \"%s\" on line %d.",
                    delimiter,
                    streamTokenizer.lineno()));
        }
        streamTokenizer.ordinaryChar('-');
        streamTokenizer.parseNumbers();
        streamTokenizer.ordinaryChar('>');
        super.parse(declarativeObject, streamTokenizer);
    }
}

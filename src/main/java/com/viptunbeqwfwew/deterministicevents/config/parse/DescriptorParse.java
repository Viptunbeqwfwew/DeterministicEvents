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

public class DescriptorParse implements IParameterParse, INameParse {

    @Override
    public void parse(DeclarativeObject declarativeObject, StreamTokenizer streamTokenizer) throws IOException {
        String fullToken = parse(streamTokenizer);
        if (fullToken == null) throw new IllegalArgumentException(
            String.format("Token \"params\" is null on line %d.", streamTokenizer.lineno()));

        declarativeObject.params.add(fullToken);
    }

    @Override
    public String parse(StreamTokenizer streamTokenizer) throws IOException {
        streamTokenizer.nextToken();
        if (streamTokenizer.ttype == StreamTokenizer.TT_WORD) {
            String descriptor = streamTokenizer.sval;

            if (streamTokenizer.nextToken() == '(') {
                StringBuilder builderFullDescriptor = new StringBuilder(descriptor);
                builderFullDescriptor.append('(');

                int c = 2;
                while (streamTokenizer.nextToken() != ')' && streamTokenizer.ttype != StreamTokenizer.TT_EOF) {
                    ++c;
                    if (streamTokenizer.ttype == StreamTokenizer.TT_WORD)
                        builderFullDescriptor.append(streamTokenizer.sval);
                    else break;
                }
                if (streamTokenizer.ttype != ')') {
                    while (c != 0) {
                        streamTokenizer.pushBack();
                        --c;
                    }
                    return null;
                }

                builderFullDescriptor.append(")");

                descriptor = builderFullDescriptor.toString();
                return descriptor;
            } else {
                streamTokenizer.pushBack();
                streamTokenizer.pushBack();
            }
        } else {
            streamTokenizer.pushBack();
        }
        return null;
    }
}

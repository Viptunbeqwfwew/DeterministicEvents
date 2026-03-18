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
package com.viptunbeqwfwew.deterministicevents.config;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class DeclarativeParser {

    final private static Map<String, Pair<Boolean, Boolean>> keyMap = new HashMap<String, Pair<Boolean, Boolean>>() {

        {
            put("group", new ImmutablePair<>(false, true));
            put("supergroup", new ImmutablePair<>(false, false));
            put("mute", new ImmutablePair<>(true, true));
        }
    };

    public static List<DeclarativeObject> parse(String input) throws IOException {
        List<DeclarativeObject> objects = new ArrayList<DeclarativeObject>();
        StreamTokenizer streamTokenizer = new StreamTokenizer(new StringReader(input));

        streamTokenizer.ordinaryChar('{');
        streamTokenizer.ordinaryChar('}');
        streamTokenizer.wordChars('_', '_');
        streamTokenizer.wordChars('@', '@');

        while (streamTokenizer.nextToken() != StreamTokenizer.TT_EOF) {
            String type = streamTokenizer.sval;

            boolean order = false;
            if (type != null && type.equals("order")) {
                order = true;
                streamTokenizer.nextToken();
                type = streamTokenizer.sval;

                if (type != null && !type.equals("group")) throw new IllegalArgumentException(
                    String
                        .format("Invalid keyword \"%s\" after \"order\" on line %d.", type, streamTokenizer.lineno()));
            }

            if (type == null) throw new IllegalArgumentException(
                String.format("Token \"type\" is null on line %d.", streamTokenizer.lineno()));

            if (!keyMap.containsKey(type)) throw new IllegalArgumentException(
                String.format("Unknown token \"type\" on line %d.", streamTokenizer.lineno()));

            streamTokenizer.nextToken();
            String name = keyMap.get(type)
                .getLeft() ? getDescriptor(streamTokenizer) : streamTokenizer.sval;

            if (name == null) throw new IllegalArgumentException(
                String.format("Token \"name\" is null on line %d.", streamTokenizer.lineno()));

            DeclarativeObject obj = new DeclarativeObject(type, name, order);

            if (streamTokenizer.nextToken() == '{') {
                while (streamTokenizer.nextToken() != '}' && streamTokenizer.ttype != StreamTokenizer.TT_EOF) {
                    if (streamTokenizer.ttype == '{') throw new InputMismatchException(
                        String.format("Unexpected second opening curly brace on line %d.", streamTokenizer.lineno()));

                    String fullToken = keyMap.get(type)
                        .getRight() ? getDescriptor(streamTokenizer) : streamTokenizer.sval;
                    if (fullToken == null) throw new IllegalArgumentException(
                        String.format("Token \"params\" is null on line %d.", streamTokenizer.lineno()));

                    obj.params.add(fullToken);
                }
            }
            if (streamTokenizer.ttype != '}') throw new InputMismatchException("Reached end of file while parsing.");

            objects.add(obj);
        }
        return objects;
    }

    private static String getDescriptor(StreamTokenizer streamTokenizer) throws IOException {
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

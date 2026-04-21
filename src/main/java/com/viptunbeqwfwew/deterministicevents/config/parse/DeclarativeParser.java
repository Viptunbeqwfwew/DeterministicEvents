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
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.List;

import com.viptunbeqwfwew.deterministicevents.Constants;
import com.viptunbeqwfwew.deterministicevents.config.DeclarativeObject;
import com.viptunbeqwfwew.deterministicevents.config.collector.TypeModification;

public class DeclarativeParser {

    final private static HashMap<String, AStructureParse> keyMap = new HashMap<String, AStructureParse>() {

        {
            DescriptorParse descriptorParse = new DescriptorParse();
            StringValParse stringValParse = new StringValParse();
            MappingParse mappingParse = new MappingParse("->");
            put(
                Constants.GROUP,
                new StructureParseWithParameters(
                    Constants.GROUP,
                    stringValParse,
                    descriptorParse,
                    new TypeModification[] { TypeModification.ORDER }));
            put(
                Constants.SUPERGROUP,
                new StructureParseWithParameters(Constants.SUPERGROUP, stringValParse, stringValParse));
            put(
                Constants.MUTE,
                new StructureParseWithParameters(
                    Constants.MUTE,
                    descriptorParse,
                    descriptorParse,
                    new TypeModification[] { TypeModification.REVERSE }));
            put(
                Constants.CONTRACT,
                new StructureParseContract(Constants.CONTRACT, stringValParse, new TypeModification[0]));
            put(
                Constants.CONDITION,
                new StructureParseWithParameters(
                    Constants.CONDITION,
                    stringValParse,
                    mappingParse,
                    new TypeModification[0]));
            put(
                Constants.MAPPING,
                new StructureParseWithParameters(
                    Constants.MAPPING,
                    stringValParse,
                    mappingParse,
                    new TypeModification[0]));
        }
    };
    final private static HashMap<String, TypeModification> modificationMap = new HashMap<String, TypeModification>() {

        {
            for (TypeModification typeModification : TypeModification.values()) {
                put(typeModification.getName(), typeModification);
            }
        }
    };

    public static List<DeclarativeObject> parse(String input) throws IOException {
        List<DeclarativeObject> objects = new ArrayList<>();
        StreamTokenizer streamTokenizer = new StreamTokenizer(new StringReader(input));

        streamTokenizer.ordinaryChar('{');
        streamTokenizer.ordinaryChar('}');
        streamTokenizer.wordChars('_', '_');
        streamTokenizer.wordChars('@', '@');
        streamTokenizer.slashSlashComments(true);

        ArrayList<TypeModification> typeModifications = new ArrayList<>();
        while (streamTokenizer.nextToken() != StreamTokenizer.TT_EOF) {
            String type = streamTokenizer.sval;

            while (!keyMap.containsKey(type) && modificationMap.containsKey(type)) {
                TypeModification typeModification = modificationMap.get(type);
                if (!typeModifications.contains(typeModification)) typeModifications.add(typeModification);
                else throw new IllegalArgumentException(
                    String.format("The token \"modification\" was duplicated on line %d.", streamTokenizer.lineno()));
                streamTokenizer.nextToken();
                if (streamTokenizer.ttype != StreamTokenizer.TT_WORD) {
                    throw new InputMismatchException(
                        String.format("The next token was not \"word\" at line %d.", streamTokenizer.lineno()));
                }
                type = streamTokenizer.sval;
            }

            if (type == null) throw new IllegalArgumentException(
                String.format("Token \"type\" is null on line %d.", streamTokenizer.lineno()));

            if (!keyMap.containsKey(type)) throw new IllegalArgumentException(
                String.format("Unknown token \"type\" on line %d.", streamTokenizer.lineno()));

            DeclarativeObject obj = keyMap.get(type)
                .parse(typeModifications.toArray(new TypeModification[0]), streamTokenizer);

            objects.add(obj);
            typeModifications.clear();
        }
        return objects;
    }
}

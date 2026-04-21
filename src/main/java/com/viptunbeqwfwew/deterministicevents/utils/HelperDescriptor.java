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

package com.viptunbeqwfwew.deterministicevents.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HelperDescriptor {

    private static final Pattern matcherEventType = Pattern.compile("\\((.*?)\\)$");

    static public String extractEventType(String descriptor) {
        Matcher matcher = matcherEventType.matcher(descriptor);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    public static String extractEventTypeGTNH(String desc) {
        return desc.substring(desc.indexOf("(") + 2, desc.indexOf(";"))
            .replace("/", ".");
    }
}

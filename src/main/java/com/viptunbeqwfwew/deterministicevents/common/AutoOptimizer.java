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
package com.viptunbeqwfwew.deterministicevents.common;

public class AutoOptimizer {

    private boolean cached;
    private final long[] unregister = new long[10];
    private short len = 0;
    private short cursor = 0;
    final private short size;
    final private int time;

    public AutoOptimizer(short size, int time) {
        this.size = size;
        this.time = time;
    }

    public boolean isCached() {
        return cached;
    }

    public void reset() {
        cached = false;
        len = 0;
        cursor = 0;
    }

    public boolean markTime() {
        long milis = System.currentTimeMillis();
        if (len < size) unregister[len++] = milis;
        else {
            unregister[cursor] = milis;
            cursor = (short) ((cursor + 1) % size);
        }
        if ((milis - unregister[cursor]) / 1000 > time) { // 10 minute
            cached = true;
        }
        return cached;
    }
}

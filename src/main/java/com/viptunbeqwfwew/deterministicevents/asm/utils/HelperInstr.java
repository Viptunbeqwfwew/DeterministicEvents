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
package com.viptunbeqwfwew.deterministicevents.asm.utils;

import java.util.ListIterator;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class HelperInstr {

    static public void addInstrCallMethodSupervisorArgsAload(InsnList insnList, int[][] aloadVars, String nameMethod,
        String desc) {
        addInstrGetObjSupervisor(insnList);
        for (int[] aloadVar : aloadVars) insnList.add(new VarInsnNode(aloadVar[0], aloadVar[1]));
        addInstrCallMethodObjSupervisor(insnList, nameMethod, desc);
    }

    static public void addInstrGetObjSupervisor(InsnList insnList) {
        insnList.add(
            new FieldInsnNode(
                Opcodes.GETSTATIC,
                "com/viptunbeqwfwew/deterministicevents/DeterministicEvents",
                "proxy",
                "Lcom/viptunbeqwfwew/deterministicevents/common/CommonProxy;"));
        insnList.add(
            new FieldInsnNode(
                Opcodes.GETFIELD,
                "com/viptunbeqwfwew/deterministicevents/common/CommonProxy",
                "supervisor",
                "Lcom/viptunbeqwfwew/deterministicevents/common/Supervisor;"));
    }

    static public void addInstrCallMethodObjSupervisor(InsnList insnList, String nameMethod, String desc) {
        insnList.add(
            new MethodInsnNode(
                Opcodes.INVOKEVIRTUAL,
                "com/viptunbeqwfwew/deterministicevents/common/Supervisor",
                nameMethod,
                desc,
                false));
    }

    static public boolean insertBeforeOpcode(MethodNode method, InsnList past, int opcode) {
        return insertOpcode(method, past, opcode, true, 0);
    }

    static public boolean insertOpcode(MethodNode method, InsnList past, int opcode) {
        return insertOpcode(method, past, opcode, false, 0);
    }

    static public boolean insertBeforeOpcode(MethodNode method, InsnList past, int opcode, int skipMatch) {
        return insertOpcode(method, past, opcode, true, skipMatch);
    }

    static public boolean insertOpcode(MethodNode method, InsnList past, int opcode, int skipMatch) {
        return insertOpcode(method, past, opcode, false, skipMatch);
    }

    static private boolean insertOpcode(MethodNode method, InsnList past, int opcode, boolean isBefore, int skipMatch) {
        for (ListIterator<AbstractInsnNode> it = method.instructions.iterator(); it.hasNext();) {
            AbstractInsnNode inst = it.next();
            if (inst.getOpcode() == opcode) {
                if (skipMatch-- != 0) continue;
                if (isBefore) method.instructions.insertBefore(inst, past);
                else method.instructions.insert(inst, past);
                return true;
            }
        }
        return false;
    }
}

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
package com.viptunbeqwfwew.deterministicevents.asm.transformer;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;

import java.util.Iterator;

import net.minecraft.launchwrapper.IClassTransformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.viptunbeqwfwew.deterministicevents.asm.CoreDeterministic;

public class SupervisorTransformer implements IClassTransformer {

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (!CoreDeterministic.isSetup || basicClass == null
            || !"com.viptunbeqwfwew.deterministicevents.common.Supervisor".equals(name)) return basicClass;

        ClassReader classReader = new ClassReader(basicClass);
        ClassNode classNode = new ClassNode();
        classReader.accept(classNode, 0);

        Iterator<MethodNode> iterable = classNode.methods.iterator();
        while (iterable.hasNext()) {
            switch (iterable.next().name) {
                case "setSupergroupOnListenerList":
                case "getSupergroupFromListenerList":
                    iterable.remove();
                default:
            }
        }

        MethodNode method = new MethodNode(
            Opcodes.ACC_PRIVATE,
            "setSupergroupOnListenerList",
            "(Lcpw/mods/fml/common/eventhandler/ListenerList;Lcom/viptunbeqwfwew/deterministicevents/common/Supergroup;)V",
            null,
            null);
        method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
        method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 2));
        method.instructions.add(
            new FieldInsnNode(
                Opcodes.PUTFIELD,
                "cpw/mods/fml/common/eventhandler/ListenerList",
                "SUPERGROUP",
                "Lcom/viptunbeqwfwew/deterministicevents/common/Supergroup;"));
        method.instructions.add(new InsnNode(Opcodes.RETURN));
        classNode.methods.add(method);

        method = new MethodNode(
            Opcodes.ACC_PRIVATE,
            "getSupergroupFromListenerList",
            "(Lcpw/mods/fml/common/eventhandler/ListenerList;)Lcom/viptunbeqwfwew/deterministicevents/common/Supergroup;",
            null,
            null);
        method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
        method.instructions.add(
            new FieldInsnNode(
                Opcodes.GETFIELD,
                "cpw/mods/fml/common/eventhandler/ListenerList",
                "SUPERGROUP",
                "Lcom/viptunbeqwfwew/deterministicevents/common/Supergroup;"));
        method.instructions.add(new InsnNode(Opcodes.ARETURN));
        classNode.methods.add(method);

        ClassWriter classWriter = new ClassWriter(COMPUTE_FRAMES);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }
}

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

import net.minecraft.launchwrapper.IClassTransformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

import com.viptunbeqwfwew.deterministicevents.asm.utils.HelperInstr;

public class SubscriptionTransformer implements IClassTransformer {

    final private IClassTransformer original;

    public SubscriptionTransformer(IClassTransformer transformer) {
        original = transformer;
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) return null;
        byte[] newClass = original.transform(name, transformedName, basicClass);
        if (newClass == basicClass) return basicClass;

        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(newClass);
        classReader.accept(classNode, 0);

        rebuildMethodSetup(classNode);

        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }

    private void rebuildMethodSetup(ClassNode classNode) {
        for (MethodNode method : classNode.methods) if ("setup()V".equals(method.name + method.desc)) {
            InsnList insnList = new InsnList();
            insnList.add(new InsnNode(Opcodes.DUP2));
            HelperInstr.insertOpcode(method, insnList, Opcodes.INVOKESPECIAL, 1);

            insnList = new InsnList();
            HelperInstr.addInstrGetObjSupervisor(insnList);
            insnList.add(new InsnNode(Opcodes.DUP_X2));
            insnList.add(new InsnNode(Opcodes.POP));
            insnList.add(new InsnNode(Opcodes.SWAP));
            insnList.add(new LdcInsnNode(classNode.name.replace("/", ".")));
            HelperInstr.addInstrCallMethodObjSupervisor(
                insnList,
                "registerListenerList",
                "(Lcpw/mods/fml/common/eventhandler/ListenerList;Lcpw/mods/fml/common/eventhandler/ListenerList;Ljava/lang/String;)V");
            HelperInstr.insertBeforeOpcode(method, insnList, Opcodes.PUTSTATIC);
            return;
        }
    }
}

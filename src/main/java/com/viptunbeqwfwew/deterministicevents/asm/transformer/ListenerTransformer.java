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
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;

import com.viptunbeqwfwew.deterministicevents.asm.CoreDeterministic;
import com.viptunbeqwfwew.deterministicevents.asm.utils.HelperInstr;

public class ListenerTransformer implements IClassTransformer {

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (!CoreDeterministic.isSetup || basicClass == null
            || (!"cpw.mods.fml.common.eventhandler.EventBus".equals(name)
                && !"cpw.mods.fml.common.eventhandler.ListenerList".equals(name))
                && !"com.gtnewhorizon.gtnhlib.eventbus.AutoEventBus".equals(name))
            return basicClass;

        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(basicClass);
        classReader.accept(classNode, 0);

        switch (name) {
            case "cpw.mods.fml.common.eventhandler.EventBus":
                patchEventBus(classNode);
                break;
            case "cpw.mods.fml.common.eventhandler.ListenerList":
                patchListenerList(classNode);
                break;
            case "com.gtnewhorizon.gtnhlib.eventbus.AutoEventBus":
                patchAutoEventBus(classNode);
                break;
            default:
                throw new RuntimeException("Patcher not found.");
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        classNode.accept(writer);

        return writer.toByteArray();
    }

    private void patchAutoEventBus(ClassNode classNode) {
        for (MethodNode method : classNode.methods)
            if ("register(Lcpw/mods/fml/common/ModContainer;Ljava/lang/Class;Lit/unimi/dsi/fastutil/objects/ObjectSet;)V"
                .equals(method.name + method.desc)) {
                    InsnList insnList = new InsnList();
                    HelperInstr.addInstrCallMethodSupervisorArgsAload(
                        insnList,
                        new int[][] { { Opcodes.ALOAD, 4 } },
                        "setDescriptor",
                        "(Lcom/gtnewhorizon/gtnhlib/eventbus/MethodInfo;)V");
                    HelperInstr.insertBeforeOpcode(method, insnList, Opcodes.ALOAD, 12);
                }
    }

    private void patchEventBus(ClassNode classNode) {
        for (MethodNode method : classNode.methods)
            if ("register(Ljava/lang/Class;Ljava/lang/Object;Ljava/lang/reflect/Method;Lcpw/mods/fml/common/ModContainer;)V"
                .equals(method.name + method.desc)) {
                    InsnList insnList = new InsnList();
                    HelperInstr.addInstrCallMethodSupervisorArgsAload(
                        insnList,
                        new int[][] { { Opcodes.ALOAD, 3 } },
                        "setDescriptor",
                        "(Ljava/lang/reflect/Method;)V");
                    HelperInstr.insertBeforeOpcode(method, insnList, Opcodes.ALOAD);
                    return;
                }
    }

    private void patchListenerList(ClassNode classNode) {
        classNode.fields.add(
            new FieldNode(
                Opcodes.ACC_PUBLIC,
                "SUPERGROUP",
                "Lcom/viptunbeqwfwew/deterministicevents/common/Supergroup;",
                null,
                null));

        for (MethodNode method : classNode.methods) {
            switch (method.name + method.desc) {
                case "getListeners(I)[Lcpw/mods/fml/common/eventhandler/IEventListener;": {
                    InsnList insnList = new InsnList();
                    HelperInstr.addInstrCallMethodSupervisorArgsAload(
                        insnList,
                        new int[][] { { Opcodes.ALOAD, 0 }, { Opcodes.ILOAD, 1 } },
                        "getListeners",
                        "(Lcpw/mods/fml/common/eventhandler/ListenerList;I)[Lcpw/mods/fml/common/eventhandler/IEventListener;");
                    insnList.add(new InsnNode(Opcodes.DUP));
                    LabelNode labelContinue = new LabelNode();
                    insnList.add(new JumpInsnNode(Opcodes.IFNULL, labelContinue));
                    insnList.add(new InsnNode(Opcodes.ARETURN));
                    insnList.add(labelContinue);
                    insnList.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
                    insnList.add(new InsnNode(Opcodes.POP));
                    HelperInstr.insertBeforeOpcode(method, insnList, Opcodes.ALOAD);
                    break;
                }
                case "register(ILcpw/mods/fml/common/eventhandler/EventPriority;Lcpw/mods/fml/common/eventhandler/IEventListener;)V": {
                    InsnList insnList = new InsnList();
                    HelperInstr.addInstrCallMethodSupervisorArgsAload(
                        insnList,
                        new int[][] { { Opcodes.ALOAD, 0 }, { Opcodes.ILOAD, 1 }, { Opcodes.ALOAD, 2 },
                            { Opcodes.ALOAD, 3 } },
                        "register",
                        "(Lcpw/mods/fml/common/eventhandler/ListenerList;ILcpw/mods/fml/common/eventhandler/EventPriority;Lcpw/mods/fml/common/eventhandler/IEventListener;)Z");
                    LabelNode labelContinue = new LabelNode();
                    insnList.add(new JumpInsnNode(Opcodes.IFEQ, labelContinue));
                    insnList.add(new InsnNode(Opcodes.RETURN));
                    insnList.add(labelContinue);
                    insnList.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
                    HelperInstr.insertBeforeOpcode(method, insnList, Opcodes.ALOAD);
                    break;
                }
                case "unregister(ILcpw/mods/fml/common/eventhandler/IEventListener;)V": {
                    InsnList insnList = new InsnList();
                    HelperInstr.addInstrCallMethodSupervisorArgsAload(
                        insnList,
                        new int[][] { { Opcodes.ALOAD, 0 }, { Opcodes.ILOAD, 1 }, { Opcodes.ALOAD, 2 } },
                        "unregister",
                        "(Lcpw/mods/fml/common/eventhandler/ListenerList;ILcpw/mods/fml/common/eventhandler/IEventListener;)Z");
                    LabelNode labelContinue = new LabelNode();
                    insnList.add(new JumpInsnNode(Opcodes.IFEQ, labelContinue));
                    insnList.add(new InsnNode(Opcodes.RETURN));
                    insnList.add(labelContinue);
                    insnList.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
                    HelperInstr.insertBeforeOpcode(method, insnList, Opcodes.ALOAD);
                    break;
                }
                case "clearBusID(I)V": {
                    InsnList insnList = new InsnList();
                    HelperInstr.addInstrCallMethodSupervisorArgsAload(
                        insnList,
                        new int[][] { { Opcodes.ILOAD, 0 } },
                        "dispose",
                        "(I)Z");
                    LabelNode labelContinue = new LabelNode();
                    insnList.add(new JumpInsnNode(Opcodes.IFEQ, labelContinue));
                    insnList.add(new InsnNode(Opcodes.RETURN));
                    insnList.add(labelContinue);
                    insnList.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
                    HelperInstr.insertBeforeOpcode(method, insnList, Opcodes.GETSTATIC);
                    break;
                }
                default:
            }
        }
    }
}

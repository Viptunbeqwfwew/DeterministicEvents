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

import java.lang.reflect.Field;

import org.apache.commons.lang3.tuple.Pair;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.viptunbeqwfwew.deterministicevents.DeterministicEvents;
import com.viptunbeqwfwew.deterministicevents.common.contract.ContractContext;

import cpw.mods.fml.common.eventhandler.Event;

public class HelperCodeGen {

    final private static String abstractContractContext = Type.getInternalName(ContractContext.class);

    public static byte[] genContractContext(String nameGen, String sourceEventType, String endEventType,
        Pair<String, String>[] conditions, Pair<String, String>[] mappings) {
        Class<?> clazzSource;
        try {
            clazzSource = Class.forName(sourceEventType);
        } catch (ClassNotFoundException e) {
            DeterministicEvents.LOG.warn("The class for \"{}\" (SourceEventType) was not found.", sourceEventType);
            return null;
        }

        Class<?> clazzEnd;
        try {
            clazzEnd = Class.forName(endEventType);
        } catch (ClassNotFoundException e) {
            DeterministicEvents.LOG.warn("The class for \"{}\" (ReceivingEventType) was not found.", endEventType);
            return null;
        }

        MethodNode checkASMConditions = new MethodNode(
            Opcodes.ACC_PUBLIC,
            "checkASMConditions",
            "(Lcpw/mods/fml/common/eventhandler/Event;)Z",
            null,
            null);
        {
            InsnList insnList = new InsnList();
            if (conditions.length != 0) {
                LabelNode popLabel = new LabelNode();
                insnList.add(new VarInsnNode(Opcodes.ALOAD, 1));
                addCastLocalVar(insnList, popLabel, Type.getInternalName(clazzSource));
                LabelNode fallLabel = new LabelNode();
                for (Pair<String, String> pair : conditions) {
                    try {
                        Field field = clazzSource.getField(pair.getLeft());
                        Class<?> clazzField = field.getType();
                        Class<?> clazz = Class.forName(pair.getRight());

                        if (clazzField.equals(clazz) || !clazzField.isAssignableFrom(clazz)) {
                            DeterministicEvents.LOG.warn(
                                "Type error for field \"{}\": Expected a strict subclass of {}, but {} was passed.",
                                pair.getLeft(),
                                pair.getRight(),
                                clazzField.getName());
                            return null;
                        }

                        addCheckVar(
                            insnList,
                            fallLabel,
                            Type.getInternalName(clazz),
                            Type.getInternalName(clazzSource),
                            pair.getLeft(),
                            Type.getDescriptor(clazzField));
                    } catch (NoSuchFieldException e) {
                        DeterministicEvents.LOG.warn("Field \"{}\" was not found.", pair.getLeft());
                        return null;
                    } catch (ClassNotFoundException e) {
                        DeterministicEvents.LOG
                            .warn("The class for \"{}\" (ConditionFieldEventType) was not found.", pair.getRight());
                        return null;
                    }
                }
                insnList.add(new InsnNode(Opcodes.ICONST_1));
                insnList.add(new InsnNode(Opcodes.IRETURN));
                insnList.add(popLabel);
                insnList.add(new InsnNode(Opcodes.POP));
                insnList.add(fallLabel);
                insnList.add(new InsnNode(Opcodes.ICONST_0));
                insnList.add(new InsnNode(Opcodes.IRETURN));
            } else {
                insnList.add(new InsnNode(Opcodes.ICONST_1));
                insnList.add(new InsnNode(Opcodes.IRETURN));
            }

            checkASMConditions.instructions.add(insnList);
        }

        MethodNode updateEventData = new MethodNode(
            Opcodes.ACC_PUBLIC,
            "updateEventData",
            "(Lcpw/mods/fml/common/eventhandler/Event;)V",
            null,
            null);
        {
            InsnList insnList = new InsnList();
            if (mappings.length != 0) {
                LabelNode popLabel = new LabelNode();
                insnList.add(new VarInsnNode(Opcodes.ALOAD, 1));
                addCastLocalVar(insnList, popLabel, Type.getInternalName(clazzEnd));
                insnList.add(new VarInsnNode(Opcodes.ALOAD, 0));
                insnList.add(new FieldInsnNode(Opcodes.GETFIELD, nameGen, "event", Type.getDescriptor(Event.class)));
                addCastLocalVar(insnList, popLabel, Type.getInternalName(clazzSource), 3);
                LabelNode errorLabel = new LabelNode();
                InsnList pastBlock = new InsnList();
                for (Pair<String, String> pair : mappings) {
                    try {
                        Field fieldSource = clazzEnd.getField(pair.getLeft());
                        Field fieldEnd = clazzSource.getField(pair.getRight());
                        Class<?> clazzFieldSource = fieldSource.getType();
                        Class<?> clazzFieldEnd = fieldEnd.getType();

                        if (clazzFieldEnd.isAssignableFrom(clazzFieldSource)) {
                            pastBlock.add(new VarInsnNode(Opcodes.ALOAD, 3));
                            pastBlock.add(new VarInsnNode(Opcodes.ALOAD, 2));
                            pastBlock.add(
                                new FieldInsnNode(
                                    Opcodes.GETFIELD,
                                    Type.getInternalName(clazzEnd),
                                    pair.getLeft(),
                                    Type.getDescriptor(clazzFieldSource)));
                            pastBlock.add(
                                new FieldInsnNode(
                                    Opcodes.PUTFIELD,
                                    Type.getInternalName(clazzSource),
                                    pair.getRight(),
                                    Type.getDescriptor(clazzFieldEnd)));
                        } else
                            if (!clazzFieldSource.isPrimitive() && clazzFieldSource.isAssignableFrom(clazzFieldEnd)) {
                                addCheckVar(
                                    insnList,
                                    errorLabel,
                                    Type.getInternalName(clazzFieldEnd),
                                    Type.getInternalName(clazzEnd),
                                    pair.getLeft(),
                                    Type.getDescriptor(clazzFieldSource));
                                pastBlock.add(new VarInsnNode(Opcodes.ALOAD, 3));
                                pastBlock.add(new VarInsnNode(Opcodes.ALOAD, 2));
                                pastBlock.add(
                                    new FieldInsnNode(
                                        Opcodes.GETFIELD,
                                        Type.getInternalName(clazzEnd),
                                        pair.getLeft(),
                                        Type.getDescriptor(clazzFieldSource)));
                                pastBlock.add(new TypeInsnNode(Opcodes.CHECKCAST, Type.getInternalName(clazzFieldEnd)));
                                pastBlock.add(
                                    new FieldInsnNode(
                                        Opcodes.PUTFIELD,
                                        Type.getInternalName(clazzSource),
                                        pair.getRight(),
                                        Type.getDescriptor(clazzFieldEnd)));
                            } else {
                                DeterministicEvents.LOG.warn(
                                    "Incompatible field types: source \"{}\" and target \"{}\" are not derived from each other.",
                                    clazzFieldSource.getCanonicalName(),
                                    clazzFieldEnd.getCanonicalName());
                                return null;
                            }
                    } catch (NoSuchFieldException e) {
                        DeterministicEvents.LOG.warn("Field \"{}\" was not found.", pair.getLeft());
                        return null;
                    }
                }
                insnList.add(pastBlock);
                LabelNode returnLabel = new LabelNode();
                insnList.add(returnLabel);
                insnList.add(new InsnNode(Opcodes.RETURN));
                insnList.add(popLabel);
                insnList.add(new InsnNode(Opcodes.POP));
                insnList.add(errorLabel);
                insnList.add(new VarInsnNode(Opcodes.ALOAD, 0));
                insnList.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, nameGen, "error", "()V", false));
                insnList.add(new JumpInsnNode(Opcodes.GOTO, returnLabel));
            } else {
                insnList.add(new InsnNode(Opcodes.RETURN));
            }
            updateEventData.instructions.add(insnList);
        }

        ClassNode classNode = initClass(nameGen);
        classNode.methods.add(checkASMConditions);
        classNode.methods.add(updateEventData);

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES) {

            @Override
            protected String getCommonSuperClass(String type1, String type2) {
                if (type1.equals(nameGen)) type1 = abstractContractContext;
                if (type2.equals(nameGen)) type2 = abstractContractContext;
                return super.getCommonSuperClass(type1, type2);
            }
        };
        classNode.accept(writer);

        return writer.toByteArray();
    }

    private static ClassNode initClass(String nameGen) {
        ClassNode classNode = new ClassNode(Opcodes.ASM5);
        classNode.version = Opcodes.V1_8;
        classNode.access = Opcodes.ACC_PUBLIC;
        classNode.name = nameGen;
        classNode.superName = abstractContractContext;

        MethodNode constructor = new MethodNode(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        constructor.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        constructor.instructions
            .add(new MethodInsnNode(Opcodes.INVOKESPECIAL, abstractContractContext, "<init>", "()V", false));
        constructor.instructions.add(new InsnNode(Opcodes.RETURN));
        classNode.methods.add(constructor);

        return classNode;
    }

    private static void addCastLocalVar(InsnList insnList, LabelNode fallNode, String castType, int astore) {
        insnList.add(new InsnNode(Opcodes.DUP));
        insnList.add(new TypeInsnNode(Opcodes.INSTANCEOF, castType));
        insnList.add(new JumpInsnNode(Opcodes.IFEQ, fallNode));
        insnList.add(new TypeInsnNode(Opcodes.CHECKCAST, castType));
        insnList.add(new VarInsnNode(Opcodes.ASTORE, astore));
    }

    private static void addCastLocalVar(InsnList insnList, LabelNode fallNode, String castType) {
        addCastLocalVar(insnList, fallNode, castType, 2);
    }

    private static void addCheckVar(InsnList insnList, LabelNode fallNode, String checkType, String clazzOwnerField,
        String nameField, String descField) {
        insnList.add(new VarInsnNode(Opcodes.ALOAD, 2));
        insnList.add(new FieldInsnNode(Opcodes.GETFIELD, clazzOwnerField, nameField, descField));
        insnList.add(new TypeInsnNode(Opcodes.INSTANCEOF, checkType));
        insnList.add(new JumpInsnNode(Opcodes.IFEQ, fallNode));
    }
}

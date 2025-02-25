package com.pranav.javacompletion.parser.classfile;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

import java.util.EnumSet;

/**
 * method_info structure in a .class file.
 *
 * <p>See https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.6
 */
@AutoValue
public abstract class MethodInfo {
    public enum AccessFlag {
        PUBLIC(0x0001),
        PRIVATE(0x0002),
        PROTECTED(0x0004),
        STATIC(0x0008),
        FINAL(0x0010),
        SYNCHRONIZED(0x0020),
        BRIDGE(0x0040),
        VARARGS(0x0080),
        NATIVE(0x0100),
        ABSTRACT(0x0400),
        STRICT(0x0800),
        SYNTHETIC(0x1000),
        ;

        private final int value;

        private AccessFlag(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public abstract EnumSet<AccessFlag> getAccessFlags();

    public abstract int getNameIndex();

    public abstract int getDescriptorIndex();

    public abstract ImmutableList<AttributeInfo> getAttributeInfos();

    public static MethodInfo create(
            EnumSet<AccessFlag> accessFlags,
            int nameIndex,
            int descriptorIndex,
            ImmutableList<AttributeInfo> attributeInfos) {
        return new AutoValue_MethodInfo(accessFlags, nameIndex, descriptorIndex, attributeInfos);
    }
}

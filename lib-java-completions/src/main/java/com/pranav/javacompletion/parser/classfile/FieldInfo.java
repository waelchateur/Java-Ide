package com.pranav.javacompletion.parser.classfile;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

import java.util.EnumSet;

/**
 * field_info structure in a .class file.
 *
 * <p>See https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.5
 */
@AutoValue
public abstract class FieldInfo {
    public enum AccessFlag {
        PUBLIC(0x0001),
        PRIVATE(0x0002),
        PROTECTED(0x0004),
        STATIC(0x0008),
        FINAL(0x0010),
        VOLATILE(0x0040),
        TRANSIENT(0x0080),
        SYNTHETIC(0x1000),
        ENUM(0x4000),
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

    public static FieldInfo create(
            EnumSet<AccessFlag> accessFlags,
            int nameIndex,
            int descriptorIndex,
            ImmutableList<AttributeInfo> attributeInfos) {
        return new AutoValue_FieldInfo(accessFlags, nameIndex, descriptorIndex, attributeInfos);
    }
}

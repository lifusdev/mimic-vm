package com.mimicvm.shared.op;

public interface Opcodes {

    int I32_CONST = 0x1;
    int LOCAL_GET = 0x2;
    int LOCAL_SET = 0x3;

    int I32_ADD = 0x10;
    int I32_SUB = 0x11;
    int I32_MUL = 0x12;

    int RETURN = 0x20;
    int RETURN_VOID = 0x21;

    int CALL = 0x22;

    int JUMP = 0x30;
    int I32_EQ = 0x31;
    int JUMP_IF = 0x32;
}

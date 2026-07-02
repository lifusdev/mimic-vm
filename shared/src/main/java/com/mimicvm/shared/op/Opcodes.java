package com.mimicvm.shared.op;

public interface Opcodes {

    int I32_CONST = 0x1;
    int LOCAL_GET = 0x2;
    int LOCAL_SET = 0x3;

    int I32_ADD = 0x10;
    int I32_SUB = 0x11;
    int I32_MUL = 0x12;
    int I32_DIV = 0x13;

    int RETURN = 0x20;
    int RETURN_VOID = 0x21;

    int CALL = 0x22;

    int JUMP = 0x30;
    int I32_EQ = 0x31;
    int JUMP_IF = 0x32;
    int I32_LT = 0x33;
    int I32_GT = 0x34;
    int I32_LE = 0x35;
    int I32_GE = 0x36;
    int I32_NE = 0x37;
}

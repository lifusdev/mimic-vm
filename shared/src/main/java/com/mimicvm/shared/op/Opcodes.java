package com.mimicvm.shared.op;

public interface Opcodes {

    int I32_CONST = 0x1;
    int LOCAL_GET = 0x2;
    int LOCAL_SET = 0x3;
    int I64_CONST = 0x4;
    int F64_CONST = 0x5;
    int F32_CONST = 0x6;

    int I32_ADD = 0x10;
    int I32_SUB = 0x11;
    int I32_MUL = 0x12;
    int I32_DIV = 0x13;
    int I32_MOD = 0x38;
    int I32_NEG = 0x39;
    int I32_AND = 0x3A;
    int I32_OR = 0x3B;
    int I32_XOR = 0x3C;
    int I32_SHL = 0x3D;
    int I32_SHR = 0x3E;
    int I32_USHR = 0x3F;

    int DUP = 0x40;
    int POP = 0x41;
    int SWAP = 0x42;

    int I2L = 0x50;
    int I2F = 0x51;
    int I2D = 0x52;

    int L2I = 0x53;
    int L2F = 0x54;
    int L2D = 0x55;

    int F2I = 0x56;
    int F2L = 0x57;
    int F2D = 0x58;

    int D2I = 0x59;
    int D2L = 0x5A;
    int D2F = 0x5B;

    int I64_ADD = 0x14;
    int I64_SUB = 0x15;
    int I64_MUL = 0x16;
    int I64_DIV = 0x17;

    int F64_ADD = 0x18;
    int F64_SUB = 0x19;
    int F64_MUL = 0x1A;
    int F64_DIV = 0x1B;

    int F32_ADD = 0x1C;
    int F32_SUB = 0x1D;
    int F32_MUL = 0x1E;
    int F32_DIV = 0x1F;

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

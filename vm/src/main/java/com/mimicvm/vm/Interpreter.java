package com.mimicvm.vm;

import com.mimicvm.shared.code.VMethod;
import com.mimicvm.shared.code.VModule;
import com.mimicvm.shared.op.Opcodes;
import com.mimicvm.shared.type.Type;
import com.mimicvm.shared.type.Value;
import com.mimicvm.shared.utils.DescUtils;
import com.mimicvm.vm.frame.Cursor;
import com.mimicvm.vm.frame.Frame;
import com.mimicvm.vm.heap.Heap;
import com.mimicvm.vm.utils.Utils;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public final class Interpreter implements Opcodes {

    private final VModule module;

    private final Deque<Frame> callStack = new ArrayDeque<>();
    private final Heap heap = new Heap();

    private final Map<Integer, Value> statics = new HashMap<>();
    private final Map<Integer, String> stringObjs = new HashMap<>();

    public Interpreter(VModule module, int methodIdx) {
        this(module, methodIdx, new Value[0]);
    }

    public Interpreter(VModule module, int methodIdx, Value... args) {
        this.module = module;

        //before the first method call
        for (int i = 0; i < module.staticTypes().length; i++) {
            statics.put(i, module.staticTypes()[i].defaultValue());
        }

        final Frame entry = new Frame(module.method(methodIdx));

        for (int i = 0; i < args.length; i++) {
            entry.locals().set(i, args[i]);
        }

        callStack.push(entry);
    }

    public Value run() {
        while (true) {
            final Frame frame = callStack.element();
            final Cursor cursor = frame.cursor();

            if (!cursor.hasNext()) {
                break;
            }

            final int opc = cursor.nextOp();

            switch (opc) {
                case I32_CONST -> frame.stack().push(Value.i32(cursor.nextI32()));

                case I64_CONST -> frame.stack().push(Value.i64(cursor.nextI64()));

                case F64_CONST -> frame.stack().push(Value.f64(Double.longBitsToDouble(cursor.nextI64())));

                case F32_CONST -> frame.stack().push(Value.f32(Float.intBitsToFloat(cursor.nextI32())));

                case LOCAL_GET -> frame.stack().push(frame.locals().get(cursor.nextU8()));

                case LOCAL_SET -> frame.locals().set(cursor.nextU8(), frame.stack().pop());

                case I32_ADD -> {
                    final int b = frame.stack().pop().asI32();
                    final int a = frame.stack().pop().asI32();
                    frame.stack().push(Value.i32(a + b));
                }

                case I32_SUB -> {
                    final int b = frame.stack().pop().asI32();
                    final int a = frame.stack().pop().asI32();
                    frame.stack().push(Value.i32(a - b));
                }

                case I32_MUL -> {
                    final int b = frame.stack().pop().asI32();
                    final int a = frame.stack().pop().asI32();
                    frame.stack().push(Value.i32(a * b));
                }

                case I32_DIV -> {
                    final int b = frame.stack().pop().asI32();
                    final int a = frame.stack().pop().asI32();

                    if (b == 0) {
                        throw new ArithmeticException("Division by zero");
                    }

                    frame.stack().push(Value.i32(a / b));
                }

                case I32_MOD -> {
                    final int b = frame.stack().pop().asI32();
                    final int a = frame.stack().pop().asI32();

                    if (b == 0) {
                        throw new ArithmeticException("Division by zero");
                    }

                    frame.stack().push(Value.i32(a % b));
                }

                case I32_NEG -> {
                    final int a = frame.stack().pop().asI32();
                    frame.stack().push(Value.i32(-a));
                }

                case I32_AND -> {
                    final int b = frame.stack().pop().asI32();
                    final int a = frame.stack().pop().asI32();
                    frame.stack().push(Value.i32(a & b));
                }

                case I32_OR -> {
                    final int b = frame.stack().pop().asI32();
                    final int a = frame.stack().pop().asI32();
                    frame.stack().push(Value.i32(a | b));
                }

                case I32_XOR -> {
                    final int b = frame.stack().pop().asI32();
                    final int a = frame.stack().pop().asI32();
                    frame.stack().push(Value.i32(a ^ b));
                }

                case I32_SHL -> {
                    final int b = frame.stack().pop().asI32();
                    final int a = frame.stack().pop().asI32();
                    frame.stack().push(Value.i32(a << b));
                }

                case I32_SHR -> {
                    final int b = frame.stack().pop().asI32();
                    final int a = frame.stack().pop().asI32();
                    frame.stack().push(Value.i32(a >> b));
                }

                case I32_USHR -> {
                    final int b = frame.stack().pop().asI32();
                    final int a = frame.stack().pop().asI32();
                    frame.stack().push(Value.i32(a >>> b));
                }

                case DUP -> {
                    final Value top = frame.stack().pop();
                    frame.stack().push(top);
                    frame.stack().push(top);
                }

                case DUP_X1 -> {
                    final Value top = frame.stack().pop();
                    final Value below = frame.stack().pop();
                    frame.stack().push(top);
                    frame.stack().push(below);
                    frame.stack().push(top);
                }

                case DUP_X2 -> {
                    final Value top = frame.stack().pop();
                    final Value below = frame.stack().pop();

                    if (below.type().isWide()) {
                        frame.stack().push(top);
                        frame.stack().push(below);
                    } else {
                        final Value bottom = frame.stack().pop();
                        frame.stack().push(top);
                        frame.stack().push(bottom);
                        frame.stack().push(below);
                    }

                    frame.stack().push(top);
                }

                case DUP2 -> {
                    final Value top = frame.stack().pop();

                    if (top.type().isWide()) {
                        // already takes two jvm slots
                        frame.stack().push(top);
                        frame.stack().push(top);
                    } else {
                        final Value below = frame.stack().pop();
                        frame.stack().push(below);
                        frame.stack().push(top);
                        frame.stack().push(below);
                        frame.stack().push(top);
                    }
                }

                case DUP2_X1 -> {
                    final Value top = frame.stack().pop();

                    final Value below = frame.stack().pop();
                    if (!top.type().isWide()) {
                        final Value bottom = frame.stack().pop();


                        frame.stack().push(below);
                        frame.stack().push(top);
                        frame.stack().push(bottom);
                    } else {
                        frame.stack().push(top);
                    }
                    frame.stack().push(below);
                    frame.stack().push(top);
                }

                case DUP2_X2 -> {
                    final Value top = frame.stack().pop();
                    final Value below = frame.stack().pop();

                    if (top.type().isWide()) {
                        if (below.type().isWide()) {
                            frame.stack().push(top);
                        } else {
                            final Value bottom = frame.stack().pop();
                            frame.stack().push(top);
                            frame.stack().push(bottom);
                        }

                    } else {
                        final Value bottom = frame.stack().pop();

                        if (bottom.type().isWide()) {
                            frame.stack().push(below);
                            frame.stack().push(top);
                        } else {
                            final Value fourth = frame.stack().pop();
                            frame.stack().push(below);
                            frame.stack().push(top);
                            frame.stack().push(fourth);
                        }

                        frame.stack().push(bottom);
                    }
                    frame.stack().push(below);
                    frame.stack().push(top);
                }

                case POP -> frame.stack().pop();

                case POP2 -> {
                    final Value top = frame.stack().pop();

                    if (!top.type().isWide()) {
                        frame.stack().pop();
                    }
                }

                case SWAP -> {
                    final Value a = frame.stack().pop();
                    final Value b = frame.stack().pop();
                    frame.stack().push(a);
                    frame.stack().push(b);
                }

                case I2L -> frame.stack().push(Value.i64(frame.stack().pop().asI32()));
                case I2F -> frame.stack().push(Value.f32(frame.stack().pop().asI32()));
                case I2D -> frame.stack().push(Value.f64(frame.stack().pop().asI32()));

                case L2I -> frame.stack().push(Value.i32((int) frame.stack().pop().asI64()));
                case L2F -> frame.stack().push(Value.f32((float) frame.stack().pop().asI64()));
                case L2D -> frame.stack().push(Value.f64((double) frame.stack().pop().asI64()));

                case F2I -> frame.stack().push(Value.i32((int) frame.stack().pop().asF32()));
                case F2L -> frame.stack().push(Value.i64((long) frame.stack().pop().asF32()));
                case F2D -> frame.stack().push(Value.f64(frame.stack().pop().asF32()));

                case D2I -> frame.stack().push(Value.i32((int) frame.stack().pop().asF64()));
                case D2L -> frame.stack().push(Value.i64((long) frame.stack().pop().asF64()));
                case D2F -> frame.stack().push(Value.f32((float) frame.stack().pop().asF64()));

                case I2B -> frame.stack().push(Value.i32((byte) frame.stack().pop().asI32()));
                case I2C -> frame.stack().push(Value.i32((char) frame.stack().pop().asI32()));
                case I2S -> frame.stack().push(Value.i32((short) frame.stack().pop().asI32()));

                case I64_CMP -> {
                    final long b = frame.stack().pop().asI64();
                    final long a = frame.stack().pop().asI64();
                    frame.stack().push(Value.i32(Long.compare(a, b)));
                }

                case F32_CMPL, F32_CMPG -> {
                    final float b = frame.stack().pop().asF32();
                    final float a = frame.stack().pop().asF32();
                    final int nan = opc == F32_CMPL ? -1 : 1;
                    frame.stack().push(Value.i32(Utils.cmp(a, b, nan)));
                }

                case F64_CMPL, F64_CMPG -> {
                    final double b = frame.stack().pop().asF64();
                    final double a = frame.stack().pop().asF64();
                    final int nan = opc == F64_CMPL ? -1 : 1;
                    frame.stack().push(Value.i32(Utils.cmp(a, b, nan)));
                }

                case I64_ADD -> {
                    final long b = frame.stack().pop().asI64();
                    final long a = frame.stack().pop().asI64();
                    frame.stack().push(Value.i64(a + b));
                }

                case I64_SUB -> {
                    final long b = frame.stack().pop().asI64();
                    final long a = frame.stack().pop().asI64();
                    frame.stack().push(Value.i64(a - b));
                }

                case I64_MUL -> {
                    final long b = frame.stack().pop().asI64();
                    final long a = frame.stack().pop().asI64();
                    frame.stack().push(Value.i64(a * b));
                }

                case I64_DIV -> {
                    final long b = frame.stack().pop().asI64();
                    final long a = frame.stack().pop().asI64();

                    if (b == 0) {
                        throw new ArithmeticException("Division by zero");
                    }

                    frame.stack().push(Value.i64(a / b));
                }

                case I64_NEG -> frame.stack().push(Value.i64(-frame.stack().pop().asI64()));

                case I64_REM -> {
                    final long b = frame.stack().pop().asI64();
                    final long a = frame.stack().pop().asI64();

                    if (b == 0) {
                        throw new ArithmeticException("Division by zero");
                    }

                    frame.stack().push(Value.i64(a % b));
                }

                case I64_AND -> {
                    final long b = frame.stack().pop().asI64();
                    final long a = frame.stack().pop().asI64();
                    frame.stack().push(Value.i64(a & b));
                }

                case I64_OR -> {
                    final long b = frame.stack().pop().asI64();
                    final long a = frame.stack().pop().asI64();
                    frame.stack().push(Value.i64(a | b));
                }

                case I64_XOR -> {
                    final long b = frame.stack().pop().asI64();
                    final long a = frame.stack().pop().asI64();
                    frame.stack().push(Value.i64(a ^ b));
                }

                case I64_SHL -> {
                    final int b = frame.stack().pop().asI32();
                    final long a = frame.stack().pop().asI64();
                    frame.stack().push(Value.i64(a << b));
                }

                case I64_SHR -> {
                    final int b = frame.stack().pop().asI32();
                    final long a = frame.stack().pop().asI64();
                    frame.stack().push(Value.i64(a >> b));
                }

                case I64_USHR -> {
                    final int b = frame.stack().pop().asI32();
                    final long a = frame.stack().pop().asI64();
                    frame.stack().push(Value.i64(a >>> b));
                }

                case F32_NEG -> frame.stack().push(Value.f32(-frame.stack().pop().asF32()));
                case F64_NEG -> frame.stack().push(Value.f64(-frame.stack().pop().asF64()));

                case F32_REM -> {
                    final float b = frame.stack().pop().asF32();
                    final float a = frame.stack().pop().asF32();
                    frame.stack().push(Value.f32(a % b));
                }

                case F64_REM -> {
                    final double b = frame.stack().pop().asF64();
                    final double a = frame.stack().pop().asF64();
                    frame.stack().push(Value.f64(a % b));
                }

                case F64_ADD -> {
                    final double b = frame.stack().pop().asF64();
                    final double a = frame.stack().pop().asF64();
                    frame.stack().push(Value.f64(a + b));
                }

                case F64_SUB -> {
                    final double b = frame.stack().pop().asF64();
                    final double a = frame.stack().pop().asF64();
                    frame.stack().push(Value.f64(a - b));
                }

                case F64_MUL -> {
                    final double b = frame.stack().pop().asF64();
                    final double a = frame.stack().pop().asF64();
                    frame.stack().push(Value.f64(a * b));
                }

                case F64_DIV -> {
                    final double b = frame.stack().pop().asF64();
                    final double a = frame.stack().pop().asF64();
                    frame.stack().push(Value.f64(a / b));
                }

                case F32_ADD -> {
                    final float b = frame.stack().pop().asF32();
                    final float a = frame.stack().pop().asF32();
                    frame.stack().push(Value.f32(a + b));
                }

                case F32_SUB -> {
                    final float b = frame.stack().pop().asF32();
                    final float a = frame.stack().pop().asF32();
                    frame.stack().push(Value.f32(a - b));
                }

                case F32_MUL -> {
                    final float b = frame.stack().pop().asF32();
                    final float a = frame.stack().pop().asF32();
                    frame.stack().push(Value.f32(a * b));
                }

                case F32_DIV -> {
                    final float b = frame.stack().pop().asF32();
                    final float a = frame.stack().pop().asF32();
                    frame.stack().push(Value.f32(a / b));
                }

                case CALL -> {
                    final int methodIdx = cursor.nextU8();
                    final VMethod callee = module.method(methodIdx);
                    final Frame calleeFrame = new Frame(callee);

                    for (int i = callee.paramCount() - 1; i >= 0; i--) {
                        calleeFrame.locals().set(i, frame.stack().pop());
                    }

                    callStack.push(calleeFrame);
                }

                case SWITCH -> {
                    final int dflt = cursor.nextI32();
                    final int count = cursor.nextI32();
                    final int key = frame.stack().pop().asI32();

                    int target = dflt;

                    for (int i = 0; i < count; i++) {
                        final int matchKey = cursor.nextI32();
                        final int matchTarget = cursor.nextI32();

                        if (key == matchKey) {
                            target = matchTarget;
                        }
                    }

                    cursor.seek(target);
                }

                case JUMP -> cursor.seek(cursor.nextI32());

                case JUMP_IF -> {
                    final int target = cursor.nextI32();

                    if (frame.stack().pop().asI32() != 0) {
                        cursor.seek(target);
                    }
                }

                case I32_EQ -> {
                    final int b = frame.stack().pop().asI32();
                    final int a = frame.stack().pop().asI32();
                    frame.stack().push(Value.i32(a == b ? 1 : 0));
                }

                case I32_LT -> {
                    final int b = frame.stack().pop().asI32();
                    final int a = frame.stack().pop().asI32();
                    frame.stack().push(Value.i32(a < b ? 1 : 0));
                }

                case I32_GT -> {
                    final int b = frame.stack().pop().asI32();
                    final int a = frame.stack().pop().asI32();
                    frame.stack().push(Value.i32(a > b ? 1 : 0));
                }

                case I32_LE -> {
                    final int b = frame.stack().pop().asI32();
                    final int a = frame.stack().pop().asI32();
                    frame.stack().push(Value.i32(a <= b ? 1 : 0));
                }

                case I32_GE -> {
                    final int b = frame.stack().pop().asI32();
                    final int a = frame.stack().pop().asI32();
                    frame.stack().push(Value.i32(a >= b ? 1 : 0));
                }

                case I32_NE -> {
                    final int b = frame.stack().pop().asI32();
                    final int a = frame.stack().pop().asI32();
                    frame.stack().push(Value.i32(a != b ? 1 : 0));
                }

                case RETURN -> {
                    final Value result = frame.stack().pop();
                    callStack.pop();

                    if (callStack.isEmpty()) {
                        return result;
                    }

                    callStack.element().stack().push(result);
                }

                case RETURN_VOID -> {
                    callStack.pop();

                    if (callStack.isEmpty()) {
                        return null;
                    }
                }

                case ATHROW -> {
                    final Value exRef = frame.stack().pop();
                    throw new RuntimeException("ATHROW: exception obj ref=" + exRef.refId());
                }

                case STRING_CONST -> {
                    final int poolIdx = cursor.nextU8();
                    final String str = module.constant(poolIdx);
                    final int ref = heap.alloc(0); // string has 0 instance fields
                    stringObjs.put(ref, str);
                    frame.stack().push(Value.ref(ref));
                }

                case ACONST_NULL -> frame.stack().push(Value.NULL);

                case NEW -> {
                    final int fieldCount = cursor.nextU8();
                    final int typeIdx = cursor.nextU8();
                    
                    final int ref = module.fieldTypes().length == fieldCount ? heap.alloc(fieldCount, typeIdx, module.fieldTypes()) : heap.alloc(fieldCount, typeIdx);
                    frame.stack().push(Value.ref(ref));
                }

                case NEW_ARRAY -> {
                    final int typeIdx = cursor.nextU8();
                    final int len = frame.stack().pop().asI32();
                    if (typeIdx == 0xFF) {
                        frame.stack().push(Value.ref(heap.alloc(len))); //untyped
                    } else {
                        final Type elementType = DescUtils.arrElementType(module.typeName(typeIdx));
                        final Value initialValue = elementType.defaultValue();
                        frame.stack().push(Value.ref(heap.allocArray(len, typeIdx, initialValue)));
                    }
                }

                case ARRAY_GET -> {
                    final int idx = frame.stack().pop().asI32();
                    final int ref = frame.stack().pop().refId();
                    frame.stack().push(heap.get(ref).field(idx));
                }

                case ARRAY_SET -> {
                    final Value value = frame.stack().pop();
                    final int idx = frame.stack().pop().asI32();
                    final int ref = frame.stack().pop().refId();
                    heap.get(ref).field(idx, value);
                }

                case ARRAY_LEN -> {
                    final int ref = frame.stack().pop().refId();
                    frame.stack().push(Value.i32(heap.get(ref).len()));
                }

                case GET_STATIC -> frame.stack().push(statics.get(cursor.nextU8()));

                case PUT_STATIC -> statics.put(cursor.nextU8(), frame.stack().pop());

                case GET_FIELD -> {
                    final int idx = cursor.nextU8();
                    final int ref = frame.stack().pop().refId();
                    frame.stack().push(heap.get(ref).field(idx));
                }

                case PUT_FIELD -> {
                    final int idx = cursor.nextU8();
                    final Value value = frame.stack().pop();
                    final int ref = frame.stack().pop().refId();
                    heap.get(ref).field(idx, value);
                }

                case INSTANCEOF -> {
                    final int typeIdx = cursor.nextU8();
                    final int ref = frame.stack().pop().refId();
                    final boolean matches = ref != 0 && heap.get(ref).typeIdx() == typeIdx;
                    frame.stack().push(Value.i32(matches ? 1 : 0));
                }

                case CHECKCAST -> {
                    final int typeIdx = cursor.nextU8();
                    final Value value = frame.stack().pop();
                    final int ref = value.refId();

                    if (ref != 0 && heap.get(ref).typeIdx() != typeIdx) {
                        throw new ClassCastException("Cannot cast type " + heap.get(ref).typeIdx() + " to " + typeIdx);
                    }

                    frame.stack().push(value);
                }

                default -> throw new IllegalStateException("unknown opc: " + opc);
            }
        }

        throw new IllegalStateException("missing RETURN");
    }

}

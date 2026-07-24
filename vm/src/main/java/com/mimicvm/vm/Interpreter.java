package com.mimicvm.vm;

import com.mimicvm.shared.call.CtorCall;
import com.mimicvm.shared.call.InstCall;
import com.mimicvm.shared.call.StaticCall;
import com.mimicvm.shared.code.VMethod;
import com.mimicvm.shared.code.VModule;
import com.mimicvm.shared.op.Opcodes;
import com.mimicvm.shared.type.Type;
import com.mimicvm.shared.type.Value;
import com.mimicvm.shared.utils.DescUtils;
import com.mimicvm.vm.call.ICallInvoker;
import com.mimicvm.vm.call.ReflectCallInvoker;
import com.mimicvm.vm.frame.Cursor;
import com.mimicvm.vm.frame.Frame;
import com.mimicvm.vm.heap.Heap;
import com.mimicvm.vm.heap.HostObjects;
import com.mimicvm.vm.utils.Utils;
import com.mimicvm.vm.utils.ValueTranslator;

import java.util.*;

public final class Interpreter implements Opcodes {

    private final VModule module;
    private final ICallInvoker callInvoker;

    private final Deque<Frame> callStack = new ArrayDeque<>();
    private final Heap heap;
    private final ValueTranslator values;

    private final Map<Integer, Value> statics = new HashMap<>();

    public Interpreter(VModule module, int methodIdx) {
        this(module, methodIdx, new Heap(), new HostObjects());
    }

    public Interpreter(VModule module, int methodIdx, Value... args) {
        this(module, methodIdx, new Heap(), new HostObjects(), args);
    }

    private Interpreter(VModule module, int methodIdx, Heap heap, HostObjects hostObjects, Value... args) {
        // Interpreter and java calls share their objs
        this(module, methodIdx, new ReflectCallInvoker(heap, hostObjects), heap, hostObjects, args);
    }

    public Interpreter(VModule module, int methodIdx, ICallInvoker callInvoker, Value... args) {
        this(module, methodIdx, callInvoker, new Heap(), new HostObjects(), args);
    }

    private Interpreter(VModule module, int methodIdx, ICallInvoker callInvoker, Heap heap, HostObjects hostObjects, Value... args) {
        this.module = module;
        this.callInvoker = Objects.requireNonNull(callInvoker, "callInvoker must not be null");
        this.heap = Objects.requireNonNull(heap, "heap must not be null");
        this.values = new ValueTranslator(this.heap, hostObjects);

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

                case CALL_STATIC -> {
                    final StaticCall call = (StaticCall) module.call(cursor.nextU8());
                    final Value[] args = new Value[DescUtils.paramCount(call.desc())];

                    // reverse order
                    for (int i = args.length - 1; i >= 0; i--) {
                        args[i] = frame.stack().pop();
                    }

                    final Value result = callInvoker.invoke(call, args);


                    // void
                    if (result != null) {
                        frame.stack().push(result);
                    }
                }

                case CALL_INSTANCE -> {
                    final InstCall call = (InstCall) module.call(cursor.nextU8());
                    final Value[] args = new Value[DescUtils.paramCount(call.desc())];

                    // Params -> the receiver
                    for (int i = args.length - 1; i >= 0; i--) {
                        args[i] = frame.stack().pop();
                    }

                    final Value receiver = frame.stack().pop();
                    final Value result = callInvoker.invoke(call, receiver, args);


                    if (result != null) {
                        frame.stack().push(result);
                    }
                }

                case CALL_CTOR -> {
                    final CtorCall call = (CtorCall) module.call(cursor.nextU8());
                    final Value[] args = new Value[DescUtils.paramCount(call.desc())];

                    for (int i = args.length - 1; i >= 0; i--) {
                        args[i] = frame.stack().pop();
                    }

                    final Value receiver = frame.stack().pop();
                    callInvoker.invoke(call, receiver, args);
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

//                    final int knownRef = hostObjects.refOf(str);
//
//                    if (knownRef != 0) {
//                        frame.stack().push(Value.ref(knownRef));
//                    } else {
//                        final int ref = heap.alloc(0);
//
//                        hostObjects.put(ref, str);
//                        frame.stack().push(Value.ref(ref));
//                    }

                    frame.stack().push(values.toValue(str, String.class));
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
                        final Value initVal = elementType.defaultValue();
                        frame.stack().push(Value.ref(heap.allocArray(len, typeIdx, initVal)));
                    }
                }

                case MULTI_NEW_ARRAY -> {
                    final int dimensions = cursor.nextU8();
                    final int[] typeIndices = new int[dimensions];
                    final Value[] initVals = new Value[dimensions];

                    // from outside to inside
                    for (int i = 0; i < dimensions; i++) {
                        typeIndices[i] = cursor.nextU8();
                        final Type elementType = DescUtils.arrElementType(module.typeName(typeIndices[i]));
                        initVals[i] = elementType.defaultValue();
                    }

                    final int[] lens = new int[dimensions];

                    // the last dimension is at the top of the stack
                    for (int i = dimensions - 1; i >= 0; i--) {
                        lens[i] = frame.stack().pop().asI32();
                    }

                    frame.stack().push(Value.ref(heap.allocMultiarray(lens, typeIndices, initVals)));
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

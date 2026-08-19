package com.mimicvm.vm.runtime;

import com.mimicvm.shared.code.VModule;
import com.mimicvm.shared.type.Value;
import com.mimicvm.vm.Interpreter;
import com.mimicvm.vm.heap.Heap;
import com.mimicvm.vm.heap.HostObjects;
import com.mimicvm.vm.utils.ValueTranslator;

/**
 * Entrypoint
 */
public final class Vm {

    /**
     * Heap + HostObjects belong to the Vm instance
     */
    private final Heap heap = new Heap();
    private final HostObjects hostObjects = new HostObjects();
    private final ValueTranslator values = new ValueTranslator(heap, hostObjects);

    public static Object run(VModule module, int methodIdx, Object... args) {
        return new Vm().invoke(module, methodIdx, args);
    }

    private static Class<?> primitiveOf(Class<?> wrapper) {
        if (wrapper == Boolean.class) return boolean.class;
        if (wrapper == Byte.class) return byte.class;
        if (wrapper == Character.class) return char.class;
        if (wrapper == Short.class) return short.class;
        if (wrapper == Integer.class) return int.class;
        if (wrapper == Long.class) return long.class;
        if (wrapper == Float.class) return float.class;
        if (wrapper == Double.class) return double.class;
        return null;
    }

    /**
     * Executes a virtualized method => returns java result
     */
    public Object invoke(VModule module, int methodIdx, Object... args) {
        final Value[] vmArgs = new Value[args.length];
        for (int i = 0; i < args.length; i++) {
            vmArgs[i] = toVmArg(args[i]);
        }

        // The interpreter shares the Heap and HostObjects with this Vm instance
        final Value result = new Interpreter(module, methodIdx, heap, hostObjects, vmArgs).run();

        return toJava(result);
    }

    // wrap Java arg into a VM value
    private Value toVmArg(Object arg) {
        if (arg == null) {
            return Value.NULL;
        }

        final Class<?> prim = primitiveOf(arg.getClass());
        if (prim != null) {
            return values.toValue(arg, prim);
        }

        return values.toValue(arg, Object.class);
    }

    // convert VM result back into a Java obj
    private Object toJava(Value result) {
        if (result == null) {
            return null;
        }

        return switch (result.type()) {
            case I32 -> result.asI32();
            case I64 -> result.asI64();
            case F32 -> result.asF32();
            case F64 -> result.asF64();
            case VOID -> null;
            // ref points to a java obj  => simply return it
            case REF -> result.equals(Value.NULL) ? null : values.toJava(result, Object.class);
        };
    }
}

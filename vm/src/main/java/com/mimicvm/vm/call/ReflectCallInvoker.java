package com.mimicvm.vm.call;

import com.mimicvm.shared.call.InstCall;
import com.mimicvm.shared.call.StaticCall;
import com.mimicvm.shared.type.Value;
import com.mimicvm.vm.heap.Heap;
import com.mimicvm.vm.heap.HostObjects;
import com.mimicvm.vm.utils.ValueTranslator;

import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;

/**
 * executes external static java methods
 */
public final class ReflectCallInvoker implements ICallInvoker {

    private final ClassLoader loader;
    private final ValueTranslator values;

    public ReflectCallInvoker() {
        this(Thread.currentThread().getContextClassLoader());
    }

    public ReflectCallInvoker(HostObjects objects) {
        this(new Heap(), objects);
    }

    public ReflectCallInvoker(Heap heap, HostObjects objects) {
        this(Thread.currentThread().getContextClassLoader(), new ValueTranslator(heap, objects));
    }

    public ReflectCallInvoker(ClassLoader loader) {
        this(loader, new ValueTranslator());
    }

    public ReflectCallInvoker(ClassLoader loader, ValueTranslator values) {
        this.loader = Objects.requireNonNull(loader, "loader must not be null");
        this.values = Objects.requireNonNull(values, "values must not be null");
    }

    @Override
    public Value invoke(StaticCall call, Value... args) {
        try {
            final Class<?> owner = Class.forName(call.owner().replace('/', '.'), true, loader);
            final MethodType type = MethodType.fromMethodDescriptorString(call.desc(), loader);
            final Method method = owner.getDeclaredMethod(call.name(), type.parameterArray());

            if (!Modifier.isStatic(method.getModifiers())) {
                throw new IllegalArgumentException("method must be static: " + call);
            }

            // each vm value becomes the corresponding java value
            final Object[] javaArgs = toJavaArgs(type, args);

            final Object result = method.invoke(null, javaArgs);
            return values.toValue(result, type.returnType());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("static call failed: " + call, e);
        }
    }

    @Override
    public Value invoke(InstCall call, Value receiver, Value... args) {
        try {
            final Class<?> owner = Class.forName(call.owner().replace('/', '.'), true, loader);
            final MethodType type = MethodType.fromMethodDescriptorString(call.desc(), loader);
            final Method method = owner.getMethod(call.name(), type.parameterArray());

            if (Modifier.isStatic(method.getModifiers())) {
                throw new IllegalArgumentException("method must not be static: " + call);
            }

            // the receiver is the target of the call
            final Object target = Objects.requireNonNull(values.toJava(receiver, owner), "receiver must not be null");
            final Object[] javaArgs = toJavaArgs(type, args);

            final Object result = method.invoke(target, javaArgs);
            return values.toValue(result, type.returnType());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("instance call failed: " + call, e);
        }
    }

    private Object[] toJavaArgs(MethodType type, Value[] args) {
        final Object[] res = new Object[args.length];

        for (int i = 0; i < args.length; i++) {
            res[i] = values.toJava(args[i], type.parameterType(i));
        }

        return res;
    }
}

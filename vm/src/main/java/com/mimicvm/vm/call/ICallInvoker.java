package com.mimicvm.vm.call;

import com.mimicvm.shared.call.CtorCall;
import com.mimicvm.shared.call.InstCall;
import com.mimicvm.shared.call.StaticCall;
import com.mimicvm.shared.type.Value;

public interface ICallInvoker {

    Value invoke(StaticCall call, Value... args);

    Value invoke(InstCall call, Value receiver, Value... args);

    void invoke(CtorCall call, Value receiver, Value... args);
}

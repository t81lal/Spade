package com.krakenrs.spade.ir.type.infer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.krakenrs.spade.ir.type.ValueType;
import com.krakenrs.spade.ir.value.Local;

public class Typing {
    private final Map<Local, ValueType> types;

    public Typing(Collection<Local> locals) {
        types = new HashMap<>();
        locals.forEach(x -> types.put(x, BottomType.INST));
    }

    public Typing(Typing other) {
        this.types = new HashMap<>(other.types);
    }

    public ValueType get(Local local) {
        return types.get(local);
    }

    public void set(Local local, ValueType type) {
        types.put(local, type);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{ ");
        var iter = types.entrySet().iterator();
        while (iter.hasNext()) {
            var entry = iter.next();
            sb.append(entry.getKey()).append(" :: ").append(entry.getValue());
            if (iter.hasNext()) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}

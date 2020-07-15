package com.krakenrs.spade.ir.gen.ssa.prop;

import com.krakenrs.spade.ir.value.Constant;
import com.krakenrs.spade.ir.value.Local;

public class EvalCtx {
    
    public static class Value {
    }
    
    public static class LocalValue  extends Value {
        public Local local;
    }
    
    public static class ConstValue extends Value {
        Constant<?> cst;
    }
    
    public static class VarExpr extends Value {
        
    }
}

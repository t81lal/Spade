package com.krakenrs.spade.ir.type.infer;

public class TestTypes {

    public static interface I {
    }
    
    public static interface J {
    }
    
    public static interface A extends I, J {
    }
    
    public static interface B extends I, J {
    }
    
    public static class X implements J {
    }
    
    public static class Y extends X implements I {
    }
    
    public static class Z extends Y implements B {
    }
}

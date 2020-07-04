package com.krakenrs.spade.util;

public class Nulls {

    public static boolean anyNull(Object o) {
        return o == null;
    }
    
    public static boolean anyNull(Object... objs) {
        for(Object o : objs) {
            if(o == null) {
                return true;
            }
        }
        return false;
    }
}

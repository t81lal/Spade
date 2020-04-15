package com.krakenrs.spade.ir.code;

public interface Opcodes {

    int ASSIGN_PARAM = 0x10001;
    int ASSIGN_LOCAL = 0x10002;
    int ASSIGN_CATCH = 0x10004;
    int ASSIGN_ARRAY = 0x10008;
    int ASSIGN_FIELD = 0x10010;

    int JUMP_COND   = 0x20001;
    int JUMP_UNCOND = 0x20002;
    int JUMP_SWITCH = 0x20004;

    int CONSUME = 0x30001;
    int THROW   = 0x30002;
    int MONITOR = 0x30004;
    int RETURN  = 0x30008;

    int LOAD_LOCAL = 0x40001;
    int LOAD_CONST = 0x40002;
    int LOAD_FIELD = 0x40004;
    int LOAD_ARR   = 0x40008;
    
    int ARITHMETIC = 0x50001;
    int NEGATE     = 0x50002;
    int ARRAYLEN   = 0x50004;
    int COMPARE    = 0x50008;

    int INSTANCEOF = 0x60001;
    int CAST       = 0x60002;

    int ALLOCOBJ = 0x70001;
    int ALLOCARR = 0x70002;
    int NEWOBJ   = 0x70004;
    int INVOKE   = 0x70008;
}

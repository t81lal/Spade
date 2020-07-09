package com.krakenrs.spade.logging.shims;

import com.krakenrs.spade.ir.code.CodePrinter;
import com.krakenrs.spade.ir.code.CodeUnit;
import com.krakenrs.spade.ir.code.Expr;
import com.krakenrs.spade.ir.code.Stmt;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "of")
public class CodeUnitShim {
    @NonNull
    private final CodeUnit codeUnit;

    @Override
    public String toString() {
        if (codeUnit instanceof Expr) {
            return CodePrinter.toString((Expr) codeUnit);
        } else {
            return CodePrinter.toString((Stmt) codeUnit);
        }
    }
}

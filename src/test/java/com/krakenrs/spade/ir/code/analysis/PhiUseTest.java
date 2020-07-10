package com.krakenrs.spade.ir.code.analysis;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.krakenrs.spade.ir.code.CodeBlock;
import com.krakenrs.spade.ir.code.MockCodeFactory;
import com.krakenrs.spade.ir.code.stmt.AssignPhiStmt;
import com.krakenrs.spade.ir.value.Local;

import nl.jqno.equalsverifier.EqualsVerifier;

public class PhiUseTest {

    @Test
    void testEquals() {
        
        var codeFactory = new MockCodeFactory();
        
        var b = codeFactory.create(1);
        var red = new AssignPhiStmt(new Local(0, false, 1), Map.of(b, new Local(0, false, 0)));
        var blue = new AssignPhiStmt(new Local(1, false, 1), Map.of(b, new Local(1, false, 0)));

        var redB = codeFactory.create(2);

        EqualsVerifier.forClass(PhiUse.class).withPrefabValues(AssignPhiStmt.class, red, blue)
                .withPrefabValues(CodeBlock.class, redB, b).verify();
    }
}

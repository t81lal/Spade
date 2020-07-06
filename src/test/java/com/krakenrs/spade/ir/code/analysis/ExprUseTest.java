package com.krakenrs.spade.ir.code.analysis;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class ExprUseTest {

    @Test
    void testEquals() {
        EqualsVerifier.forClass(ExprUse.class).verify();
    }
}

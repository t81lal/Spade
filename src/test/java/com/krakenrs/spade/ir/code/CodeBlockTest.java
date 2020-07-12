package com.krakenrs.spade.ir.code;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import com.krakenrs.spade.ir.code.observer.CodeObservationManager;

public class CodeBlockTest {

    @Test
    void testIndexOfNull() {
        var b = new CodeBlock(mock(CodeObservationManager.class), 1);
        assertThrows(NullPointerException.class, () -> b.indexOf(null));
    }

    @Test
    void testIndexOfUnincludedStmt() {
        var b = new CodeBlock(mock(CodeObservationManager.class), 1);
        var s = mock(Stmt.class);
        assertThrows(IllegalArgumentException.class, () -> b.indexOf(s));
    }

    @Test
    void testAppendStmt() {
        var com = mock(CodeObservationManager.class);
        var b = new CodeBlock(com, 1);
        var s0 = spy(new MockStmt());
        var s1 = spy(new MockStmt());

        b.appendStmt(s0);
        b.appendStmt(s1);

        InOrder inOrder = inOrder(s0, s1, com);

        inOrder.verify(s0).setBlock(b);
        inOrder.verify(com).notifyStmtAdded(s0);

        inOrder.verify(s1).setBlock(b);
        inOrder.verify(com).notifyStmtAdded(s1);

        assertEquals(0, b.indexOf(s0));
        assertEquals(1, b.indexOf(s1));
    }

    @Test
    void testAppendStmtAlreadyOwned() {
        var b1 = new CodeBlock(mock(CodeObservationManager.class), 1);
        var b2 = new CodeBlock(mock(CodeObservationManager.class), 2);
        var s = new MockStmt();

        b1.appendStmt(s);

        assertThrows(IllegalArgumentException.class, () -> b2.appendStmt(s));
    }

    @Test
    void testAppendStmtAlreadyAdded() {
        var b1 = new CodeBlock(mock(CodeObservationManager.class), 1);
        var s = new MockStmt();

        b1.appendStmt(s);

        assertThrows(IllegalArgumentException.class, () -> b1.appendStmt(s));
    }

    @Test
    void testPrependStmt() {
        var com = mock(CodeObservationManager.class);
        var b = new CodeBlock(com, 1);
        var s0 = spy(new MockStmt());
        var s1 = spy(new MockStmt());

        b.preprendStmt(s0);
        b.preprendStmt(s1);

        InOrder inOrder = inOrder(s0, s1, com);

        inOrder.verify(s0).setBlock(b);
        inOrder.verify(com).notifyStmtAdded(s0);

        inOrder.verify(s1).setBlock(b);
        inOrder.verify(com).notifyStmtAdded(s1);

        assertEquals(0, b.indexOf(s1));
        assertEquals(1, b.indexOf(s0));
    }

    @Test
    void testPrependStmtAlreadyOwned() {
        var b1 = new CodeBlock(mock(CodeObservationManager.class), 1);
        var b2 = new CodeBlock(mock(CodeObservationManager.class), 2);
        var s = new MockStmt();

        b1.preprendStmt(s);

        assertThrows(IllegalArgumentException.class, () -> b2.preprendStmt(s));
    }

    @Test
    void testPrependStmtAlreadyAdded() {
        var b1 = new CodeBlock(mock(CodeObservationManager.class), 1);
        var s = new MockStmt();

        b1.preprendStmt(s);

        assertThrows(IllegalArgumentException.class, () -> b1.preprendStmt(s));
    }

    @Test
    void testRemoveStmtIndexOOB1() {
        var b = new CodeBlock(mock(CodeObservationManager.class), 1);
        assertThrows(IllegalArgumentException.class, () -> b.removeStmt(-1));
    }

    @Test
    void testRemoveStmtIndexOOB2() {
        var b = new CodeBlock(mock(CodeObservationManager.class), 1);
        assertThrows(IllegalArgumentException.class, () -> b.removeStmt(1));
    }

    @Test
    void testRemoveIndexValid() {
        var com = mock(CodeObservationManager.class);
        var b = new CodeBlock(com, 1);
        var s = mock(Stmt.class);

        b.appendStmt(s);
        verify(s).setBlock(b);
        b.removeStmt(0);
        verify(s).setBlock(null);

        verify(com).notifyStmtAdded(s);
        verify(com).notifyStmtRemoved(s);
        verifyNoMoreInteractions(com);
    }

    @Test
    void testRemoveStmtNPE() {
        var com = mock(CodeObservationManager.class);
        var b = new CodeBlock(com, 1);
        assertThrows(NullPointerException.class, () -> b.removeStmt(null));
    }

    @Test
    void testRemoveStmtNotInBlock() {
        var com = mock(CodeObservationManager.class);
        var b1 = new CodeBlock(com, 1);
        var b2 = new CodeBlock(com, 1);
        var s = mock(Stmt.class);

        b1.appendStmt(s);

        assertThrows(IllegalArgumentException.class, () -> b2.removeStmt(s));
    }

    @Test
    void testRemoveStmtValid() {
        var com = mock(CodeObservationManager.class);
        var b = new CodeBlock(com, 1);
        var s = mock(Stmt.class);

        b.appendStmt(s);
        verify(s).setBlock(b);
        b.removeStmt(s);
        verify(s).setBlock(null);

        verify(com).notifyStmtAdded(s);
        verify(com).notifyStmtRemoved(s);
        verifyNoMoreInteractions(com);
    }

    @Test
    void testInsertBeforeNPE() {
        var com = mock(CodeObservationManager.class);
        var b = new CodeBlock(com, 1);
        var s = mock(Stmt.class);

        assertThrows(NullPointerException.class, () -> b.insertBefore(null, s));
        assertThrows(NullPointerException.class, () -> b.insertBefore(s, null));
    }

    @Test
    void testInsertBeforeInvalidPos() {
        var com = mock(CodeObservationManager.class);
        var b = new CodeBlock(com, 1);
        var sToInsert = mock(Stmt.class);
        var sPos = mock(Stmt.class);

        assertThrows(IllegalArgumentException.class, () -> b.insertBefore(sPos, sToInsert));
    }

    @Test
    void testInsertBeforeAlreadyOwned() {
        var com = mock(CodeObservationManager.class);
        var b1 = new CodeBlock(com, 1);
        var b2 = new CodeBlock(com, 2);
        var sToInsert = new MockStmt();
        var sPos = new MockStmt();

        b1.appendStmt(sToInsert);
        b2.appendStmt(sPos);

        assertThrows(IllegalArgumentException.class, () -> b2.insertBefore(sPos, sToInsert));
    }

    @Test
    void testInsertBeforeValid() {
        var com = mock(CodeObservationManager.class);
        var b = new CodeBlock(com, 1);
        var s0 = new MockStmt();
        var s1 = new MockStmt();
        var s2 = new MockStmt();

        b.appendStmt(s0);
        b.appendStmt(s2);

        b.insertBefore(s2, s1);

        assertEquals(0, b.indexOf(s0));
        assertEquals(1, b.indexOf(s1));
        assertEquals(2, b.indexOf(s2));

    }
}

package com.krakenrs.spade.ir.code;

import org.junit.jupiter.api.Test;

import com.krakenrs.spade.ir.code.observer.CodeObserver;
import static org.mockito.BDDMockito.*;

public class CodeObserverTests {

    @Test
    void test() {
        CodeObserver observer = new CodeObserver() {
            
            @Override
            public void onStmtReplaced(Stmt oldStmt, Stmt newStmt) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void onStmtRemoved(Stmt stmt) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void onStmtAdded(Stmt stmt) {
                // TODO Auto-generated method stub
                
            }
        };
        
        Stmt stmt = mock(Stmt.class);
//        stmt.getCodeObservers().
        
        System.out.println(stmt);
    }
}

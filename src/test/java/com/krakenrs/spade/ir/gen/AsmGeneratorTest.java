package com.krakenrs.spade.ir.gen;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import com.krakenrs.spade.ir.code.CodeBlock;
import com.krakenrs.spade.ir.code.CodePrinter;
import com.krakenrs.spade.ir.code.ControlFlowGraph;
import com.krakenrs.spade.ir.gen.AsmGenerator.AsmGenerationState;
import com.krakenrs.spade.ir.gen.AsmGenerator.AsmGenerationState.AsmInterpCtx;
import com.krakenrs.spade.ir.gen.LocalStack.TypedLocal;
import com.krakenrs.spade.ir.type.PrimitiveType;
import com.krakenrs.spade.ir.type.ResolvingMockTypeManager;
import com.krakenrs.spade.ir.value.Local;

public class AsmGeneratorTest {

    // Convenience for getting a context
    ControlFlowGraph cfg() throws IOException {
        StackTraceElement e = new Exception().getStackTrace()[1];
        ClassReader cr = new ClassReader(e.getClassName());
        ClassNode cn = new ClassNode();
        cr.accept(cn, 0);

        for (MethodNode mn : cn.methods) {
            if (mn.name.equals(e.getMethodName())) {

                AsmGenerationCtx gCtx = new AsmGenerationCtx(new ResolvingMockTypeManager(), cn, mn);
                return AsmGenerator.run(gCtx);
            }
        }

        return null;
    }

    // Convenience for getting a context
    AsmInterpCtx ctx() throws IOException {
        StackTraceElement e = new Exception().getStackTrace()[1];
        ClassReader cr = new ClassReader(e.getClassName());
        ClassNode cn = new ClassNode();
        cr.accept(cn, ClassReader.SKIP_CODE);

        for (MethodNode mn : cn.methods) {
            if (mn.name.equals(e.getMethodName())) {

                AsmGenerationCtx gCtx = new AsmGenerationCtx(new ResolvingMockTypeManager(), cn, mn);
                AsmGenerationState state = new AsmGenerationState(gCtx);
                AsmInterpCtx ctx = state.new AsmInterpCtx(new CodeBlock(1), new LocalStack());

                return ctx;
            }
        }

        return null;
    }

    @Test
    void testVerifyEmptyStack() throws IOException {
        var ctx = ctx();
        assertDoesNotThrow(() -> ctx.verifyStack());
    }

    @Test
    void testVerifySingletonStack() throws IOException {
        var ctx = ctx();
        ctx.stack.push(new TypedLocal(new Local(0, true), PrimitiveType.INT));
        assertDoesNotThrow(() -> ctx.verifyStack());

        // lvar instead of svar
        ctx.stack.pop();
        ctx.stack.push(new TypedLocal(new Local(0, false), PrimitiveType.INT));
        assertThrows(IllegalStateException.class, () -> ctx.verifyStack());

        // invalid stack (starts at svar1)
        ctx.stack.pop();
        ctx.stack.push(new TypedLocal(new Local(1, true), PrimitiveType.INT));
        assertThrows(IllegalStateException.class, () -> ctx.verifyStack());

        // void value type
        ctx.stack.pop();
        ctx.stack.push(new TypedLocal(new Local(0, true), PrimitiveType.VOID));
        assertThrows(IllegalStateException.class, () -> ctx.verifyStack());
    }

    @Test
    void testVerifyStackMulti() throws IOException {
        var ctx = ctx();

        ctx.stack.push(new TypedLocal(new Local(0, true), PrimitiveType.INT));
        ctx.stack.push(new TypedLocal(new Local(1, true), PrimitiveType.DOUBLE));
        ctx.stack.push(new TypedLocal(new Local(2, true), PrimitiveType.INT));
        assertDoesNotThrow(() -> ctx.verifyStack());

        ctx.stack.pop();
        ctx.stack.pop();
        ctx.stack.pop();

        ctx.stack.push(new TypedLocal(new Local(2, true), PrimitiveType.INT));
        ctx.stack.push(new TypedLocal(new Local(1, true), PrimitiveType.DOUBLE));
        ctx.stack.push(new TypedLocal(new Local(0, true), PrimitiveType.INT));
        assertThrows(IllegalStateException.class, () -> ctx.verifyStack());
    }

    @Test
    void testPush() throws IOException {
        //        var ctx = ctx();
        //        ctx._push(new LoadLocalExpr(PrimitiveType.INT, new Local(0, true)));

        var cfg = cfg();
        System.out.println(CodePrinter.toString(cfg));
        
        boolean x = true;
        if(x) {
            System.out.println("yes");
        } else {
            System.out.println("no");
        }
    }
}

package com.krakenrs.spade.logging.shims;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.krakenrs.spade.ir.code.CodeBlock;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "of")
public class CodeBlockShim {
    @NonNull
    private final CodeBlock block;

    public static Set<CodeBlockShim> of(@NonNull Set<CodeBlock> set) {
        return set.stream().map(CodeBlockShim::of).collect(Collectors.toSet());
    }

    public static List<CodeBlockShim> of(@NonNull List<CodeBlock> set) {
        return set.stream().map(CodeBlockShim::of).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "L" + block.id();
    }
}

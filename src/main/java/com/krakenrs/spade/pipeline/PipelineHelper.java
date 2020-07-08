package com.krakenrs.spade.pipeline;

public class PipelineHelper {
    public static <S, O, R, T> PipelineComposed<S, R, T> dropThenHelper(
            PipelineComposable<S, O> cur,
            PipelineStep<R, T> step) {
        PipelineStep<O, R> s2 = o -> {
            return null;
        };
        return cur.then(s2).then(step);
    }
    
    public static <S, O, R, T> PipelineComposed<S, R, T> dropThenHelperClass(
            PipelineComposable<S, O> cur,
            Class<? extends PipelineStep<R, T>> stepClass) {
        PipelineStep<O, R> s2 = o -> {
            return null;
        };
        return cur.then(s2).then(stepClass);
    }
}

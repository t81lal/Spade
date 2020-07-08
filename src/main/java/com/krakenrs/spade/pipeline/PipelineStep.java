package com.krakenrs.spade.pipeline;

import java.util.function.Function;

public interface PipelineStep<I, O> extends Function<I, O> {
}

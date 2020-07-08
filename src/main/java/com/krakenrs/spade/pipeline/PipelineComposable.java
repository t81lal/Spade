package com.krakenrs.spade.pipeline;

/**
 * Represents a pipeline component that can be composed.
 * 
 * @author Bilal Tariq
 *
 * @param <S> The input type of the entire pipeline
 * @param <I> The input type to this particular part of the pipeline
 */
public interface PipelineComposable<S, I> {
    /**
     * Fluent-api method for linking another computation step to be executed after this.
     * 
     * @param <T> The output type of the given computation
     * @param step The computation itself
     * @return The updated pipeline
     */
    <T> PipelineComposed<S, I, T> then(PipelineStep<I, T> step);

    /**
     * Fluent-api method for linking another computation step to be executed after this. The difference between this and
     * {@link #then(PipelineStep)} is that this {@link PipelineStep} must be constructed ondemand.
     * 
     * @param <T> The output type of the given computation
     * @param step A class representing a computation
     * @return The updated pipeline
     */
    <T> PipelineComposed<S, I, T> then(Class<? extends PipelineStep<I, T>> stepClass);

    /**
     * Fluent-api method for sequencing the given computation step while ignoring the output of the last result in the
     * pipeline.
     * 
     * @param <T> The output type of the given computation
     * @param step A class representing a computation
     * @return A pipeline that effectively ignores the previous output (while still executing it)
     */
    default <T> PipelineComposed<S, ?, T> dropThen(PipelineStep<?, T> step) {
        return (Pipeline<S, ?, T>) PipelineHelper.dropThenHelper(this, step);
    }

    /**
     * Fluent-api method for sequencing the given computation step while ignoring the output of the last result in the
     * pipeline.
     * <p>
     * See {@link #dropThen(PipelineStep)} and {@link #then(Class)}
     * 
     * @param <R> Unused parameter, used to fulfil type requirements of the Java language
     * @param <T> The output type of the given computation
     * @param step A class representing a computation
     * @return A pipeline that effectively ignores the previous output (while still executing it)
     */
    default <R, T> PipelineComposed<S, R, T> dropThen(Class<? extends PipelineStep<R, T>> stepClass) {
        return (Pipeline<S, R, T>) PipelineHelper.dropThenHelperClass(this, stepClass);
    }
}

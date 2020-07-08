package com.krakenrs.spade.pipeline.execution;

@SuppressWarnings("serial")
public class ExecutionException extends Exception {

    public ExecutionException(Throwable t) {
        super(t);
    }
}

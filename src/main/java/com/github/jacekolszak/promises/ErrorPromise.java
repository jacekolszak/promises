package com.github.jacekolszak.promises;

class ErrorPromise<RESULT, NEW_RESULT> extends Promise<RESULT> {

    private final CheckedFunction<Throwable, NEW_RESULT> caughtFunction;

    public ErrorPromise(CheckedFunction<Throwable, NEW_RESULT> caughtFunction) {
        this.caughtFunction = caughtFunction;
    }

    @Override
    synchronized void doReject(Throwable exception) {
        NEW_RESULT newResult;
        try {
            newResult = caughtFunction.apply(exception);
            setResult(newResult);
        } catch (Throwable e) {
            setException(e);
        }
    }

}

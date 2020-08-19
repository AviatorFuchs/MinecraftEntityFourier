package com.github.classault.fourier_series.math;

class ParamFunction {
    private final IFunction func;
    private final int phase;

    ParamFunction(IFunction func, int phase) {
        this.func = func;
        this.phase = phase;
    }

    double calc(double t) {
        //return func.calc((t - phase) / func.size());
        return func.calc(t - phase);
    }
}

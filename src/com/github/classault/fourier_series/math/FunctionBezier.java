package com.github.classault.fourier_series.math;

public abstract class FunctionBezier implements IFunction {
    @Override
    public abstract double calc(double t);

    @Override
    public double size() {
        return 1.0d;
    }
}

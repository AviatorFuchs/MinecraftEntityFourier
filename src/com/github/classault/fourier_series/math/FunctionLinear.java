package com.github.classault.fourier_series.math;

public class FunctionLinear implements IFunction {
    final private double start;
    final private double end;
    final private double size;

    public FunctionLinear(double start, double end, double size) {
        this.start = start;
        this.end = end;
        this.size = size;
    }

    @Override
    public double calc(double t) {
        if (t < 0 || t > 1) throw new IllegalArgumentException(String.valueOf(t));
        return start * (1 - t) + end * t;
    }

    @Override
    public double size() {
        return size;
    }
}

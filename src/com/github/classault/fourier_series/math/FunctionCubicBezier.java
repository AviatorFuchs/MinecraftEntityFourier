package com.github.classault.fourier_series.math;

public class FunctionCubicBezier extends FunctionBezier {
    private double[] params;

    public FunctionCubicBezier(double size, double[] args) {
        this.params = new double[4];
        System.arraycopy(args, 0, params, 0, 4);
    }

    @Override
    public double calc(double t) {
        if (t < 0 || t > 1) throw new IllegalArgumentException(String.valueOf(t));
        double d = 0;
        d += t * t * t * params[3];
        d += t * t * (1 - t) * params[2] * 3;
        d += t * (1 - t) * (1 - t) * params[1] * 3;
        d += (1 - t) * (1 - t) * (1 - t) * params[0];
        return d;
    }
}

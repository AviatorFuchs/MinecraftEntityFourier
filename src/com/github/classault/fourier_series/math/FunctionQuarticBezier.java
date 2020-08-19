package com.github.classault.fourier_series.math;

public class FunctionQuarticBezier extends FunctionBezier {
    private double[] params;

    public FunctionQuarticBezier(double size, double[] args) {
        this.params = new double[3];
        System.arraycopy(args, 0, params, 0, 3);
    }

    @Override
    public double calc(double t) {
        if (t < 0 || t > 1) throw new IllegalArgumentException(String.valueOf(t));
        double d = 0;
        d += t * t * params[2];
        d += t * (1 - t) * params[1] * 2;
        d += (1 - t) * (1 - t) * params[0];
        return d;
    }
}

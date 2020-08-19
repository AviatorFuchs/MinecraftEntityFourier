package com.github.classault.fourier_series.math;

public class FunctionTriangle implements IFunction {
    private final double con;
    private final double A;
    private final double B;
    private final double T1;
    private final double T2;
    /*
    public FunctionTriangle(double cosK, double cosB, double cosA, double sinK, double sinB, double sinA, double con, double size) {
        this.cosK = cosK;
        this.cosB = cosB;
        this.cosA = cosA;
        this.sinK = sinK;
        this.sinB = sinB;
        this.sinA = sinA;
        this.con = con;
        this.size = 1;
    }*/
    // todo: x = a cos(wt + &) + b cos(- wt + &) + x0;
    // todo: y = a sin(wt + &) + b sin(- wt + &) + y0;
    // todo: so it will be f(t) = A cos(T1 * (1 - t) + T2 * t) + B sin(T1 * (1 - t) + T2 * t) + const
    public FunctionTriangle(double A, double B, double T1, double T2, double con) {
        this.A = A;
        this.B = B;
        this.T1 = T1;
        this.T2 = T2;
        this.con = con;
    }

    @Override
    public double calc(double t) {
        if (t < 0 || t > 1) throw new IllegalArgumentException(String.valueOf(t));
        double T = T1 * (1 - t) + T2 * t;
        return A * Math.cos(T) + B * Math.sin(T) + con;
    }

    @Override
    public double size() {
        return 1d;
    }
}

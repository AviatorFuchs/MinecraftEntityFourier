package com.github.classault.fourier_series.svg;

import com.github.classault.fourier_series.math.FunctionCubicBezier;
import com.github.classault.fourier_series.math.FunctionQuarticBezier;
import com.github.classault.fourier_series.math.IFunction;

public class CommandQuadraticBezier implements Command { // Q T q t
    private double START_X;
    private double START_Y;
    private double END_X;
    private double END_Y;
    private double X2;
    private double Y2;
    private char type;

    CommandQuadraticBezier(double[] s, double x, double y, boolean absoluteAxis, char type) {
        this.type = type;
        this.START_X = x;
        this.START_Y = y;
        if (absoluteAxis) {
            X2 = s[0];
            Y2 = s[1];
            END_X = s[2];
            END_Y = s[3];
        } else {
            X2 = s[0] + x;
            Y2 = s[1] + y;
            END_X = s[2] + x;
            END_Y = s[3] + y;
        }
    }

    @Override
    public double ep_x() {
        return END_X;
    }

    @Override
    public double ep_y() {
        return END_Y;
    }

    @Override
    public IFunction generateX() {
        return new FunctionQuarticBezier(1, new double[] {START_X, X2, END_X});
    }

    @Override
    public IFunction generateY() {
        return new FunctionQuarticBezier(1, new double[] {START_Y, Y2, END_Y});
    }

    @Override
    public double x2() {
        return X2;
    }

    @Override
    public double y2() {
        return Y2;
    }

    @Override
    public char type() {
        return type;
    }
}

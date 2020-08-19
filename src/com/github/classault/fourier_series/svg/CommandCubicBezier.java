package com.github.classault.fourier_series.svg;

import com.github.classault.fourier_series.math.FunctionCubicBezier;
import com.github.classault.fourier_series.math.IFunction;

public class CommandCubicBezier implements Command { //C S c s
    private double START_X;
    private double START_Y;
    private double END_X;
    private double END_Y;
    private double X1;
    private double Y1;
    private double X2;
    private double Y2;

    private char type;

    CommandCubicBezier(double[] s, double x, double y, boolean absoluteAxis, char type) {
        this.START_X = x;
        this.START_Y = y;
        this.type = type;
        if (absoluteAxis) {
            X1 = s[0];
            Y1 = s[1];
            X2 = s[2];
            Y2 = s[3];
            END_X = s[4];
            END_Y = s[5];
        } else {
            X1 = s[0] + x;
            Y1 = s[1] + y;
            X2 = s[2] + x;
            Y2 = s[3] + y;
            END_X = s[4] + x;
            END_Y = s[5] + y;
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

    @Override
    public IFunction generateX() {
        return new FunctionCubicBezier(END_X - START_X, new double[] {START_X, X1, X2, END_X});
    }

    @Override
    public IFunction generateY() {
        return new FunctionCubicBezier(END_Y - START_Y, new double[] {START_Y, Y1, Y2, END_Y});
    }
}

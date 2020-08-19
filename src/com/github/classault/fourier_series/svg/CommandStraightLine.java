package com.github.classault.fourier_series.svg;

import com.github.classault.fourier_series.math.FunctionLinear;
import com.github.classault.fourier_series.math.IFunction;

public class CommandStraightLine implements Command {
    private double START_X;
    private double START_Y;
    private double END_X;
    private double END_Y;
    private char type;

    CommandStraightLine(double START_X, double START_Y, double END_X, double END_Y, char type) {
        this.type = type;
        this.START_X = START_X;
        this.START_Y = START_Y;
        this.END_X = END_X;
        this.END_Y = END_Y;
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
        return new FunctionLinear(START_X, END_X, 1.0d);
    }

    @Override
    public IFunction generateY() {
        return new FunctionLinear(START_Y, END_Y, 1.0d);
    }

    @Override
    public double x2() {
        throw new UnsupportedOperationException();
    }

    @Override
    public double y2() {
        throw new UnsupportedOperationException();
    }

    @Override
    public char type() {
        return type;
    }
}

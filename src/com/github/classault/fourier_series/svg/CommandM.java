package com.github.classault.fourier_series.svg;

import com.github.classault.fourier_series.math.FunctionLinear;
import com.github.classault.fourier_series.math.IFunction;

public class CommandM implements Command {
    private double x;
    private double y;

    CommandM(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public double ep_x() {
        return x;
    }

    @Override
    public double ep_y() {
        return y;
    }

    @Override
    public IFunction generateX() {
        return new FunctionLinear(x, x, 0);
    }

    @Override
    public IFunction generateY() {
        return new FunctionLinear(y, y, 0);
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
        return 'M';
    }
}

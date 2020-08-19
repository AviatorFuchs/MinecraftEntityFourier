package com.github.classault.fourier_series.svg;

import com.github.classault.fourier_series.math.IFunction;

public interface Command {
    double ep_x();
    double ep_y();
    IFunction generateX();
    IFunction generateY();

    double x2();
    double y2();

    char type();
}

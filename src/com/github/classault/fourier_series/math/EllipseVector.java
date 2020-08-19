package com.github.classault.fourier_series.math;

/**
 * This is a "struct" class
 */
public class EllipseVector {
    public final double positiveRadius;
    public final double negativeRadius;

    public final double RCosP;
    public final double RSinP;
    public final double rCosN;
    public final double rSinN;

    EllipseVector(double positiveRadius, double negativeRadius, double xCosA, double xSinA, double yCosA, double ySinA) {
        this.positiveRadius = positiveRadius;
        this.negativeRadius = negativeRadius;
        this.RCosP = 0.5 * (xCosA + ySinA);
        this.rCosN = 0.5 * (xCosA - ySinA);
        this.RSinP = 0.5 * (yCosA - xSinA);
        this.rSinN = 0.5 * (yCosA + xSinA);
    }
}

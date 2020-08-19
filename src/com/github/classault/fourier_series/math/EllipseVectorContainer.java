package com.github.classault.fourier_series.math;

import java.util.ArrayList;
import java.util.List;

public class EllipseVectorContainer {
    private List<EllipseVector> vectors = new ArrayList<>();
    public final double baseX;
    public final double baseY;

    public EllipseVectorContainer(double baseX, double baseY) {
        this.baseX = baseX;
        this.baseY = baseY;
    }

    public void makeVector(double positiveRadius, double negativeRadius, double xCosA, double xSinA, double yCosA, double ySinA) {
        vectors.add(new EllipseVector(positiveRadius, negativeRadius, xCosA, xSinA, yCosA, ySinA));
    }

    public List<EllipseVector> getVectors() {
        return vectors;
    }

    public void debug() {
        StringBuilder builder = new StringBuilder();
        for (EllipseVector vector : vectors) {
            builder.append(vector.positiveRadius).append(' ').append(vector.negativeRadius).append(", ").append("; ");
        }
        System.out.println(builder.toString());
    }
}

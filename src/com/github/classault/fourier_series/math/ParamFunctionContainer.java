package com.github.classault.fourier_series.math;

import java.util.ArrayList;
import java.util.List;

public class ParamFunctionContainer {
    private List<ParamFunction> functions = new ArrayList<>();
    private List<FourierGeneral> ellipses = new ArrayList<>(2);
    private int currentPhase = 0;
    final private int level;
    final private int precise;
    public int T = 0;

    public ParamFunctionContainer(int level, int precise) {
        this.level = level;
        this.precise = precise;
    }

    public void addFunction(IFunction function) {
        ParamFunction pf = new ParamFunction(function, currentPhase);
        functions.add(pf);
        currentPhase++;
    }

    public boolean makeFourierTransformation() {
        final double dt = 1.0 / precise;
        for (int n = 0; n <= level; n++) {
            double t = 0;
            double cosA = 0;
            double sinA = 0;
            double omega = 2 * Math.PI / T;
            while (t < T) {
                ParamFunction paramFunction = functions.get((int) (t));
                cosA += (1d / T) * paramFunction.calc(t) * Math.cos(n * omega * t) * dt;
                sinA += (1d / T) * paramFunction.calc(t) * Math.sin(n * omega * t) * dt;
                t += dt;
            }

            if (Math.abs(cosA) < 1.0e-8) cosA = 0;
            if (Math.abs(sinA) < 1.0e-8) sinA = 0;

            FourierGeneral fourier = new FourierGeneral(cosA, sinA);
            ellipses.add(fourier);
        }
        return true;
    }

    public void println() {
        StringBuilder builder = new StringBuilder();
        for (FourierGeneral f : ellipses) {
            builder.append(f.cosA).append(",\t").append(f.sinA).append(";\t");
        }
        System.out.println(builder.toString());
    }

    public List<FourierGeneral> getTransformedFunctions() {
        return new ArrayList<>(ellipses);
    }
}

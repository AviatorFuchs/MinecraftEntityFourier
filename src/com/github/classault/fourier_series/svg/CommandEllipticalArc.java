package com.github.classault.fourier_series.svg;

import com.github.classault.fourier_series.math.FunctionTriangle;
import com.github.classault.fourier_series.math.IFunction;

public class CommandEllipticalArc implements Command {
    private double a;
    private double b;
    private double ang;

    /**
     * defines the arc.
     * true: Major; false: Minor
     */
    private int arcType;

    /**
     * defines the direction we move.
     * true: Clockwise; false: Counterclockwise
     */
    private int direction;
    private double START_X;
    private double START_Y;
    private double EP_X;
    private double EP_Y;

    private double X0 = 0;
    private double Y0 = 0;

    private double T1;
    private double T2;

    private double pi = Math.PI;

    private double R;
    private double r;

    /**
     * Angles in SVG acts differently from what we had been taught in math classes.
     *
     * @param x START_X value; the same as what we had been taught in math classes.
     * @param y START_Y value; direction +y points down instead of up.
     * @param a length of the semi-major axis
     * @param b length of the semi-minor axis
     * @param ang the tilt angle
     * @param at the arc where the pointer moves from (x,y) to (ex,ey)
     * @param d the direction in which the pointer moves from (x,y) to (ex,ey)
     * @param ex END_X value; the same as what we had been taught in math classes.
     * @param ey END_Y value; direction +y points down instead of up.
     */

    CommandEllipticalArc(double x, double y, double a, double b, double ang, int at, int d, double ex, double ey) {
        if (at != 1 && at != 0) {
            throw new IllegalArgumentException("Invalid arc type: " + at);
        }

        if (d != 1 && d != 0) {
            throw new IllegalArgumentException("Invalid rotation direction: " + d);
        }
        this.a = a;
        this.b = b;
        this.R = 0.5 * (a + b);
        this.r = 0.5 * (a - b);
        this.ang = (Math.toRadians(ang) + 2 * pi) % (2 * pi);
        this.START_X = x;
        this.START_Y = y;
        this.EP_X = ex;
        this.EP_Y = ey;
        this.arcType = at;
        this.direction = d;
        resolve();
    }

    @Override
    public double ep_x() {
        return EP_X;
    }

    @Override
    public double ep_y() {
        return EP_Y;
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
        return 'A';
    }

    @Override // the mother f**king function / Method
    // todo: x = a cos($)cos(t) + b sin($)sin(t) + x0
    public IFunction generateX() {
        double A = R + r * Math.cos(2 * ang);
        double B = r * Math.sin(2 * ang);
        return new FunctionTriangle(A, B, T1, T2, X0);
    }

    @Override // the mother f**king function / Method
    // todo: y = b cos($)sin(t) - a sin($)cos(t) + x0
    public IFunction generateY() {
        double A = r * Math.sin(2 * ang);
        double B = R - r * Math.cos(2 * ang);
        return new FunctionTriangle(A, B, T1, T2, Y0);
    }

    private void resolve() { //get X0 Y0 via fucking arguments; new function
        double u1 = u(START_X, START_Y);
        double v1 = v(START_X, START_Y);
        double u2 = u(EP_X, EP_Y);
        double v2 = v(EP_X, EP_Y);

        double du = u2 - u1;
        double dv = v2 - v1;

        double dp = (u2 - u1) / a;
        double dq = (v2 - v1) / b;

        double lengthInFact = Math.sqrt((START_X - EP_X) * (START_X - EP_X) + (START_Y - EP_Y) * (START_Y - EP_Y));
        double distInFact = Math.sqrt(dp * dp + dq * dq);

        double u0, v0;
        if (distInFact > 2.0) {
            double expectedLength = 2 * Math.sqrt((du / distInFact) * (du / distInFact) + (dv / distInFact) * (dv / distInFact));
            System.out.println("\t> MATHEMATICAL ERROR DETECTED BY " + getClass().getName());
            System.out.println("\t> WARNING: Your ellipse is mathematically wrong.");
            System.out.println("\t> Using this tilt angle, the longest distance between to points on this ellipse should be: " + expectedLength);
            System.out.println("\t> But the ellipse builder found out that the real distance is: " + lengthInFact);
            System.out.println("\t> Draw this ellipse using such arguments on the draft yourself, and see whatever can be presented.");
            System.out.println("\t> The ellipse will be enlarged by " + distInFact / 2 + " times.");
            a *= (distInFact / 2);
            b *= (distInFact / 2);
            R *= (distInFact / 2);
            r *= (distInFact / 2);
            u0 = 0.5 * (u1 + u2);
            v0 = 0.5 * (v1 + v2);
        } else {
            double deltaU = (a * a) / ((a * a * dv * dv) + (b * b * du * du)) - 1d / (4 * b * b);
            double deltaV = (b * b) / ((a * a * dv * dv) + (b * b * du * du)) - 1d / (4 * a * a);

            u0 = 0.5 * (u1 + u2) + (Math.abs(2 * (arcType + direction - 1)) - 1) * a * dv * Math.sqrt(deltaU);
            v0 = 0.5 * (v1 + v2) - (Math.abs(2 * (arcType + direction - 1)) - 1) * b * du * Math.sqrt(deltaV);
        }

        double cosU1 = (u1 - u0) / a;
        double cosU2 = (u2 - u0) / a;
        double sinV1 = (v1 - v0) / b;
        double sinV2 = (v2 - v0) / b;

        double ang1 = arc(cosU1, sinV1);
        double ang2 = arc(cosU2, sinV2);
        double pDeltaT = direction == 1 ? (ang2 - ang1 + 2 * pi) % (2 * pi) : (ang1 - ang2 + 2 * pi) % (2 * pi);
        double nDeltaT = 2 * pi - pDeltaT;

        double deltaT = arcType == 1 ? Math.max(pDeltaT, nDeltaT) : Math.min(pDeltaT, nDeltaT);

        T1 = ang1 + ang;
        T2 = direction == 1 ? T1 + deltaT : T1 - deltaT;
        X0 = x(u0, v0);
        Y0 = y(u0, v0);
    }

    private double arc(double x, double y) {
        double radius = Math.sqrt(x * x + y * y);
        double rad = Math.abs(Math.acos(x / radius));
        int i = y >= 0 ? 1 : -1;
        return (rad * i);
    }

    private double u(double x, double y) {
        return Math.cos(ang) * x + Math.sin(ang) * y;
    }

    private double v(double x, double y) {
        return Math.cos(ang) * y - Math.sin(ang) * x;
    }

    private double x(double u, double v) {
        return Math.cos(-ang) * u + Math.sin(-ang) * v;
    }

    private double y(double u, double v) {
        return Math.cos(-ang) * v - Math.sin(-ang) * u;
    }
}

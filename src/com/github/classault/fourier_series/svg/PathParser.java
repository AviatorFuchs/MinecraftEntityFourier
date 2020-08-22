package com.github.classault.fourier_series.svg;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class PathParser {
    private List<Command> commands;
    private char[] commandHeaders = {'M', 'L', 'H', 'V', 'C', 'S', 'Q', 'T', 'A', 'Z', 'm', 'l', 'h', 'v', 'c', 's', 'q', 't', 'a', 'z'};
    private Command lastCommand = null;
    private CommandM moveTo = null;
    public int count = 0;

    public PathParser(byte[] dRaw) {
        this.commands = new ArrayList<>();
        System.out.println("\t> Loading path: " + new String(dRaw, StandardCharsets.US_ASCII));
        build(dRaw);
    }

    public List<Command> commandList() {
        return commands;
    }

    private void build(byte[] dRaw) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean notStarted = true;
        for (int i = 0; i < dRaw.length; i++) {
            byte b = dRaw[i];
            if (notStarted && (b == ' ' || b == ',' || b == '\t' || b == '\n' || b == '\r')) {
                continue;
            }
            if (notStarted && (b == 'M' || b == 'm')) {
                notStarted = false;
                os.write(b);
                continue;
            }
            if (i != 0 && isCommand(b)) {
                byte[] cached = os.toByteArray();
                os.reset();
                checkAndBuildCommand(cached);
            }
            os.write(b);
        }
        checkAndBuildCommand(os.toByteArray());
        if (commands.get(commands.size() - 1).type() != 'Z') {
            double sx = lastCommand.ep_x();
            double sy = lastCommand.ep_y();
            Command c = new CommandStraightLine(sx, sy, moveTo.ep_x() - sx, moveTo.ep_y() - sy, 'Z');
            add(c);
            lastCommand = c;
        }
        System.out.println("\t> Built " + count + " commands.");
    }

    private void add(Command command) {
        count++;
        commands.add(command);
        lastCommand = command;
    }

    /**
     * What the f**k, there are duplicates...
     *
     * @param src raw data from the file
     */
    private void checkAndBuildCommand(byte[] src) {
        byte head = src[0];
        boolean absolute = isCapital(head);
        byte[] formatted = format(src);
        System.out.println("\t> Building command(s): " + new String(formatted, StandardCharsets.US_ASCII));

        byte[] arg = new byte[1];
        if (!(head == 'Z') && !(head == 'z')) {
            arg = new byte[formatted.length - 2];
            System.arraycopy(formatted, 2, arg, 0, arg.length);
        }
        String arguments = new String(arg, StandardCharsets.US_ASCII);

        String[] s = arguments.split(" ");

        switch (head) {
            case'M': case'm': {
                if (s.length != 2) throw new IllegalArgumentException(arguments + "; for command" + (char) head);
                double x = Double.parseDouble(s[0]);
                double y = Double.parseDouble(s[1]);
                Command m;
                if (moveTo == null) {
                    m = new CommandM(x, y);
                    moveTo = (CommandM) m;
                } else {
                    double lastX = lastCommand.ep_x();
                    double lastY = lastCommand.ep_y();
                    if (absolute) {
                        m = new CommandStraightLine(lastX, lastY, x, y, 'L');
                    } else {
                        m = new CommandStraightLine(lastX, lastY, lastX + x, lastY + y, 'L');
                    }
                }
                add(m);
                return;
            }
            case'L': case'l': {
                int c = 0;
                while (c < s.length) {
                    double ep_x = Double.parseDouble(s[c]);
                    double ep_y = Double.parseDouble(s[1 + c]);
                    double from_x = lastCommand.ep_x();
                    double from_y = lastCommand.ep_y();
                    Command l;
                    if (absolute) {
                        l = new CommandStraightLine(from_x, from_y, ep_x, ep_y, 'L');
                    } else {
                        l = new CommandStraightLine(from_x, from_y, ep_x + from_x, ep_y + from_y, 'L');
                    }
                    add(l);
                    c += 2;
                }
                return;
            }
            case'H': case'h': {
                if (s.length != 1) throw new IllegalArgumentException(arguments + "; for command" + (char) head);
                double xx = Double.parseDouble(s[0]);
                double sx = lastCommand.ep_x();
                double sy = lastCommand.ep_y();
                Command l;
                if (absolute) {
                    l = new CommandStraightLine(sx, sy, xx, sy, 'H');
                } else {
                    l = new CommandStraightLine(sx, sy, xx + sx, sy, 'H');
                }
                add(l);
                return;
            }
            case'V': case'v': {
                if (s.length != 1) throw new IllegalArgumentException(arguments + "; for command" + (char) head);
                double yy = Double.parseDouble(s[0]);
                double sx = lastCommand.ep_x();
                double sy = lastCommand.ep_y();
                Command l;
                if (absolute) {
                    l = new CommandStraightLine(sx, sy, sx, yy, 'V');
                } else {
                    l = new CommandStraightLine(sx, sy, sx, yy + sy, 'V');
                }
                add(l);
                return;
            }
            case'C': case'c': {
                int c = 0;
                while (c < s.length) {
                    double[] ar = new double[6];
                    for (int i = 0; i < 6; i++) {
                        ar[i] = Double.parseDouble(s[i + c]);
                    }
                    Command cc = new CommandCubicBezier(ar, lastCommand.ep_x(), lastCommand.ep_y(), absolute, 'C');
                    add(cc);
                    c += 6;
                }
                return;
            }
            case'S': case's': {
                int c = 0;
                while (c < s.length) {
                    char lastType = lastCommand.type();
                    Command S;
                    if (lastType == 'C' || lastType == 'S') {
                        double[] ar = new double[6];
                        ar[2] = Double.parseDouble(s[c]);
                        ar[3] = Double.parseDouble(s[c + 1]);
                        ar[4] = Double.parseDouble(s[c + 2]);
                        ar[5] = Double.parseDouble(s[c + 3]);
                        if (absolute) {
                            ar[0] = 2 * lastCommand.ep_x() - lastCommand.x2();
                            ar[1] = 2 * lastCommand.ep_y() - lastCommand.y2();
                        } else {
                            ar[0] = lastCommand.ep_x() - lastCommand.x2();
                            ar[1] = lastCommand.ep_y() - lastCommand.y2();
                        }
                        S = new CommandCubicBezier(ar, lastCommand.ep_x(), lastCommand.ep_y(), absolute, 'S');
                    } else {
                        double[] ar = new double[6];
                        ar[0] = ar[2] = Double.parseDouble(s[c]);
                        ar[1] = ar[3] = Double.parseDouble(s[c + 1]);
                        ar[4] = Double.parseDouble(s[c + 2]);
                        ar[5] = Double.parseDouble(s[c + 3]);
                        S = new CommandCubicBezier(ar, lastCommand.ep_x(), lastCommand.ep_y(), absolute, 'S');
                    }
                    c += 4;
                    add(S);
                }
                return;
            }
            case'Q': case'q': {
                int c = 0;
                while (c < s.length) {
                    double[] ar = new double[4];
                    for (int i = 0; i < 4; i++) {
                        ar[i] = Double.parseDouble(s[i + c]);
                    }
                    Command q = new CommandQuadraticBezier(ar, lastCommand.ep_x(), lastCommand.ep_y(), absolute, 'Q');
                    add(q);
                    c += 4;
                }
                return;
            }
            case'T': case't': {
                int c = 0;
                while (c < s.length) {
                    char lastType = lastCommand.type();
                    Command t;
                    if (lastType == 'Q' || lastType == 'T') {
                        double[] ar = new double[4];
                        ar[2] = Double.parseDouble(s[c]);
                        ar[3] = Double.parseDouble(s[c + 1]);
                        if (absolute) {
                            ar[0] = 2 * lastCommand.ep_x() - lastCommand.x2();
                            ar[1] = 2 * lastCommand.ep_y() - lastCommand.y2();
                        } else {
                            ar[0] = lastCommand.ep_x() - lastCommand.x2();
                            ar[1] = lastCommand.ep_y() - lastCommand.y2();
                        }
                        t = new CommandCubicBezier(ar, lastCommand.ep_x(), lastCommand.ep_y(), absolute, 'T');
                    } else {
                        double xx = Double.parseDouble(s[c]);
                        double yy = Double.parseDouble(s[c + 1]);
                        double sx = lastCommand.ep_x();
                        double sy = lastCommand.ep_y();
                        if (isCapital(head)) {
                            t = new CommandStraightLine(sx, sy, xx + sx, yy + sy, 'T');
                        } else {
                            t = new CommandStraightLine(sx, sy, xx, yy, 'T');
                        }
                    }
                    add(t);
                    c += 2;
                }
                return;
            }
            case'A': case'a': {
                if (s.length != 7) throw new IllegalArgumentException(arguments + "; for command" + (char) head);
                double a = Double.parseDouble(s[0]);
                double b = Double.parseDouble(s[1]);
                double an = Double.parseDouble(s[2]);
                int at = Integer.parseInt(s[3]);
                int di = Integer.parseInt(s[4]);
                double ex = Double.parseDouble(s[5]);
                double ey = Double.parseDouble(s[6]);
                Command A;
                if ((absolute && ex == lastCommand.ep_x() && ey == lastCommand.ep_y()) || (!absolute && ex == 0d && ey == 0d)) {
                    System.out.println("\t> MATHEMATICAL ERROR DETECTED BY " + getClass().getName());
                    System.out.println("\t> WARNING: Your ellipse is mathematically wrong.");
                    System.out.println("\t> The start point and the end point from the argument are the same.");
                    System.out.println("\t> This ellipse arc command will be ignored.");
                    return;
                }
                if (absolute) {
                    A = new CommandEllipticalArc(lastCommand.ep_x(), lastCommand.ep_y(), a, b, an, at, di, ex, ey);
                } else {
                    double sx = lastCommand.ep_x();
                    double sy = lastCommand.ep_y();
                    A = new CommandEllipticalArc(sx, sy, a, b, an, at, di, ex + sx, ey + sy);
                }
                add(A);
                return;
            }
            case'Z': case'z': {
                double sx = lastCommand.ep_x();
                double sy = lastCommand.ep_y();
                Command z = new CommandStraightLine(sx, sy, moveTo.ep_x(), moveTo.ep_y(), 'Z');
                add(z);
                return;
            }
        }
        throw new IllegalArgumentException("Unknown command: " + (char) head);
    }

    private boolean isNumber(byte b) {
        return b >= (byte)'0' && b <= (byte) '9';
    }

    private boolean isCapital(byte c) {
        return c >= 'A' && c <= 'Z';
    }

    private boolean isCommand(byte c) {
        for (char b : commandHeaders) {
            if (c == (byte) b) {
                return true;
            }
        }
        return false;
    }

    private byte[] format(byte[] src) {
        byte[] filter1 = filterBytes(src, (byte LAST, byte THIS, byte NEXT) -> (THIS == (byte) ',' || THIS == (byte) '\t' || THIS == (byte) '\r' || THIS == (byte) '\n') ? new byte[] {' '} : new byte[] {THIS});
        byte[] filter2 = filterBytes(filter1, (byte LAST, byte THIS, byte NEXT) -> (isNumber(THIS) && NEXT == '-') ? new byte[] {THIS, ' '} : new byte[] {THIS});
        byte[] filter3 = filterBytes(filter2, (byte LAST, byte THIS, byte NEXT) -> isCommand(LAST) ? new byte[] {' ', THIS} : new byte[] {THIS});
        byte[] filter4 = filterBytes(filter3, (byte LAST, byte THIS, byte NEXT) -> (THIS == ' ' && NEXT == ' ') ? new byte[0] : new byte[] {THIS});

        AtomicBoolean isWritingDecimal = new AtomicBoolean(false);
        byte[] filter5 = filterBytes(filter4, (byte LAST, byte THIS, byte NEXT) -> {
            if (THIS == '.' && isWritingDecimal.get()) {
                return new byte[] {' ', '0', '.'};
            }

            if (THIS == ' ') {
                isWritingDecimal.set(false);
            }

            if (THIS == '.') {
                isWritingDecimal.set(true);
            }
            return new byte[] {THIS};
        });

        AtomicBoolean again = new AtomicBoolean(false);
        byte[] toReturn;
        byte[] last = filter5;
        do {
            again.set(false);
            byte[] filter6 = filterBytes(last, (byte LAST, byte THIS, byte NEXT) -> {
                if (LAST == ' ' && THIS == '0' && isNumber(NEXT)) {
                    again.set(true);
                    return new byte[] {'0', ' '};
                } else {
                    return new byte[] {THIS};
                }
            });
            last = filter6;
            toReturn = filter6;
        } while (again.get());

        return toReturn;
    }

    private byte[] filterBytes(byte[] raw, ByteFilter filter) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte primary = raw[0];
        byte terminal = raw[raw.length - 1];
        os.write(primary);
        // command z only has one byte
        if (raw.length == 1) return os.toByteArray();
        for (int i = 1; i < raw.length - 1; i++) {
            byte LAST = raw[i - 1];
            byte THIS = raw[i];
            byte NEXT = raw[i + 1];
            byte[] shouldWrite = filter.whatShouldBeWritten(LAST, THIS, NEXT);
            for (byte b : shouldWrite) {
                os.write(b);
            }
        }
        os.write(terminal);
        return os.toByteArray();
    }

    private interface ByteFilter {
        byte[] whatShouldBeWritten(byte a, byte b, byte c);
    }
}

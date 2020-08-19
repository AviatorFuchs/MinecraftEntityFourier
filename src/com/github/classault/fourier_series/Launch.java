package com.github.classault.fourier_series;

import com.github.classault.fourier_series.math.*;
import com.github.classault.fourier_series.svg.*;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This is a project that can convert SVG d attributes to a Minecraft datapack.
 * How to use? There are different ways:
 *      1. download jopt-simple 5.0 and commons-digester3 3.0 yourself from maven server, and launch it via -classpath and main class, instead of -jar.
 *      2. ask me for universal launcher XD
 */
public class Launch {
    /**
     * This path command is made up with a move-to, two cubic bezier curves and a straight line that forcibly closes the curve.
     * Its shape:
     *    __----__
     *  /         \
     * |           |
     * -------------------------
     *             |           |
     *              \         /
     *               --____--
     */
    private static final String DEMO_D = "M10 80 C 40 10 65 10 95 80 S 150 150 180 80 Z";
    private byte[] d;
    private String namespace;
    private String entity;
    private String block;
    private int angularVelocity;
    private int level;
    private int size;
    private String name;
    private final String home;

    /**
     * if the number's absolute value is too small, it may also lead to major deviations,
     * due to basic features of double itself.
     *
     * recommend: > 16384
     */
    private int precise;

    public static void main(String[] args) {
        credit();
        new Launch().start(args);
    }

    private Launch() {
        Thread.currentThread().setName("Controller thread");
        this.home = getClass().getProtectionDomain().getCodeSource().getLocation().getPath().replaceAll("/","\\\\").substring(1);
    }

    private void start(String[] args) {
        long startTime = System.currentTimeMillis();
        int memoryMB = (int) Runtime.getRuntime().totalMemory() / (1 << 20);
        if (memoryMB < 128) {
            System.out.println("WARNING: you allocated too little memory: " + memoryMB + "MB");
        }
        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();

        OptionSpec<File> svgOption = parser.accepts("svg").withOptionalArg().ofType(File.class);
        OptionSpec<File> dflOption = parser.accepts("directlyFrom").withOptionalArg().ofType(File.class);
        OptionSpec<String> dOption = parser.accepts("d").withRequiredArg().defaultsTo(DEMO_D);

        OptionSpec<String> nameSpaceOption = parser.accepts("namespace").withRequiredArg().required();
        OptionSpec<String> nameOption = parser.accepts("dataPackName").withRequiredArg().required();
        OptionSpec<Integer> sizeOption = parser.accepts("size").withRequiredArg().ofType(Integer.class).defaultsTo(5);
        OptionSpec<Integer> levelOption = parser.accepts("level").withRequiredArg().ofType(Integer.class).defaultsTo(255);
        OptionSpec<Integer> velocityOption = parser.accepts("angularVelocity").withRequiredArg().ofType(Integer.class).defaultsTo(100);
        OptionSpec<Integer> preciseOption = parser.accepts("precise").withRequiredArg().ofType(Integer.class).defaultsTo(16384);
        OptionSpec<String> entityType = parser.accepts("entity").withRequiredArg().defaultsTo("armor_stand");
        OptionSpec<String> blockType = parser.accepts("block").withRequiredArg().defaultsTo("quartz_block");

        final OptionSet set = parser.parse(args);

        System.out.println("Loading configurations from the command line");
        if (set.valueOf(svgOption) != null) {
            System.out.println("\t> Found svg file: " + set.valueOf(svgOption).getAbsolutePath() + ", loading...");
            System.out.println("\t> Script: Only the first path will be loaded.");
            try {
                InputStream is = new FileInputStream(set.valueOf(svgOption));
                d = fromSVG(is);
            } catch (Exception e) {
                System.out.println("\t> Unable to load path svg file. Using internal d attribute instead.");
                e.printStackTrace();
                d = DEMO_D.getBytes();
            }
        } else if (set.valueOf(dflOption) != null) {
            System.out.println("\t> Found d attribute file: " + set.valueOf(dflOption).getAbsolutePath() + ", loading...");
            try {
                InputStream is = new FileInputStream(set.valueOf(dflOption));
                d = readFully(is);
            } catch (IOException e) {
                System.out.println("\t> Unable to load d attribute file. Using internal d attribute instead.");
                e.printStackTrace();
                d = DEMO_D.getBytes();
            }
        } else {
            d = set.valueOf(dOption).getBytes();
        }

        name = set.valueOf(nameOption);
        namespace = set.valueOf(nameSpaceOption);
        entity = set.valueOf(entityType);
        block = set.valueOf(blockType);

        size = set.valueOf(sizeOption);
        angularVelocity = set.valueOf(velocityOption);
        level = set.valueOf(levelOption);
        precise = set.valueOf(preciseOption);

        System.out.println("Current settings:");
        System.out.println("\t> Data pack namespace: ----> " + namespace);
        System.out.println("\t> Using vector entity: ----> " + entity);
        System.out.println("\t> Level: ------------------> " + level);
        System.out.println("\t> Vector size: ------------> " + size);
        System.out.println("\t> Precise of the process: -> " + precise);

        ParamFunctionContainer wrappedX = new ParamFunctionContainer(level, precise);
        ParamFunctionContainer wrappedY = new ParamFunctionContainer(level, precise);
        //debug();
        generateFunctions(wrappedX, wrappedY);
        makeFourierTransformation(wrappedX, wrappedY);
        makeVectorsAndDataPack(wrappedX, wrappedY);
        long deltaT = System.currentTimeMillis() - startTime;
        System.out.println("Task finished within " + deltaT / 1000d + " seconds");
    }

    private static void credit() {
        System.out.println("This is a project that can convert SVG d attributes to Minecraft datapacks.");
        System.out.println("This project uses fourier series to transform functions.");
        System.out.println("Special thanks for: ----------");
        System.out.println("\t> jopt-simple -- simplifies the process to read arguments");
        System.out.println("\t> commons-digester -- makes it easier to load svg files");
        System.out.println("\t> 00ll00 -- who gave me inspiration to finish this project");
        System.out.println("\t> my math teachers -- otherwise I would not be able to use fourier series");
    }

    private void generateFunctions(ParamFunctionContainer x, ParamFunctionContainer y) {
        System.out.println("Step 1: Load commands from SVG path");
        PathParser parser = new PathParser(d); // step 1
        x.T = parser.count - 1;
        y.T = parser.count - 1;
        System.out.println("Step 2: Construct parameter functions");
        for (Command c : parser.commandList()) { // step 2
            if (!(c instanceof CommandM)) { // eat start points
                IFunction xf = c.generateX();
                IFunction yf = c.generateY();
                x.addFunction(xf);
                y.addFunction(yf);
            }
        }
    }

    private void makeFourierTransformation(ParamFunctionContainer x, ParamFunctionContainer y) { // step 3
        AtomicBoolean xFinished = new AtomicBoolean(false);
        AtomicBoolean yFinished = new AtomicBoolean(false);
        System.out.println("Step 3: Fourier transform the two parameter functions");
        new Thread(() -> {
            System.out.println("\t> Trying to Fourier transform parameter functions with dependent variable x... This may take a long time.");
            xFinished.set(x.makeFourierTransformation());
            System.out.println("\t> Transformed parameter functions with dependent variable x");
        }, "Transformer thread X").start();

        new Thread(() -> {
            System.out.println("\t> Trying to Fourier transform parameter functions with dependent variable y... This may take a long time.");
            yFinished.set(y.makeFourierTransformation());
            System.out.println("\t> Transformed parameter functions with dependent variable y");
        }, "Transformer thread Y").start();

        while (true) {
            if (xFinished.get() && yFinished.get()) { // Listener loop
                break;
            }
        }
    }

    private void makeVectorsAndDataPack(ParamFunctionContainer x, ParamFunctionContainer y) {
        List<FourierGeneral> paramX = x.getTransformedFunctions();
        List<FourierGeneral> paramY = y.getTransformedFunctions();
        Iterator<FourierGeneral> itx = paramX.iterator();
        Iterator<FourierGeneral> ity = paramY.iterator();

        System.out.println("Step 4: Connect transformed functions via vectors");
        System.out.println("\t> Generating vector pairs -- this will transform each ellipse to two vectors rotating at the same speed in different directions");
        final FourierGeneral X0 = itx.next();
        final FourierGeneral Y0 = ity.next();
        double x0 = Math.sqrt(X0.cosA * X0.cosA + X0.sinA * X0.sinA);
        double y0 = Math.sqrt(Y0.cosA * Y0.cosA + Y0.sinA * Y0.sinA);
        EllipseVectorContainer wr = new EllipseVectorContainer(x0, y0); // step 4
        itx.remove();
        ity.remove();

        while (itx.hasNext()) {
            final FourierGeneral X = itx.next();
            final FourierGeneral Y = ity.next();
            final double pR = 0.5 * Math.sqrt((X.cosA + Y.sinA) * (X.cosA + Y.sinA) + (X.sinA - Y.cosA) * (X.sinA - Y.cosA));
            final double nR = 0.5 * Math.sqrt((X.cosA - Y.sinA) * (X.cosA - Y.sinA) + (X.sinA + Y.cosA) * (X.sinA + Y.cosA));
            wr.makeVector(pR, nR, X.cosA, X.sinA, Y.cosA, Y.sinA);

            itx.remove();
            ity.remove();
        }

        System.out.println("Step 5: Creating data pack");
        DataPackCreator creator = new DataPackCreator(home, name, namespace, entity, block, angularVelocity, size, level, wr);
        creator.createDataPack(); // step 5
    }

    private static byte[] fromSVG(InputStream is) {
        byte[] loadedBytes = SVGPathLoader.loadPath(is);
        if (loadedBytes.length == 0) {
            System.out.println("\t> Found nothing valid from this svg file - Use the internal path instead.");
            loadedBytes = DEMO_D.getBytes();
        }
        return loadedBytes;
    }

    private static byte[] readFully(InputStream is) throws IOException {
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];

        int bf;
        do {
            bf = is.read(buffer);
            if (bf <= 0) continue;
            bs.write(buffer, 0, bf);
        } while (bf != -1);

        is.close();
        bs.close();

        return bs.toByteArray();
    }

    /**
     * This static function uses square wave to debug.
     *
     * Expected.
     */
    private void debug() {
        new Thread(() -> {
            ParamFunctionContainer container = new ParamFunctionContainer(level, precise);
            container.T = 2;
            container.addFunction(new FunctionLinear(0, 1, 1));
            container.addFunction(new FunctionLinear(0, -1, 1));
            container.makeFourierTransformation();
            container.println();
        }, "Debug thread").start();
    }
}

package com.github.classault.fourier_series;

import com.github.classault.fourier_series.math.EllipseVector;
import com.github.classault.fourier_series.math.EllipseVectorContainer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.List;

class DataPackCreator {
    private String name;
    private String namespace;
    private String entity;
    private String block;
    private int size;
    private final int level;
    private final int angularVelocity;
    private EllipseVectorContainer wrapper;
    private DecimalFormat df = new DecimalFormat("0.00000000");
    private DecimalFormat rf = new DecimalFormat("0.0000");
    private final String home;

    DataPackCreator(String home, String name, String namespace, String entity, String block, int angularVelocity , int size, int level, EllipseVectorContainer wr) {
        DataPackNamespaceChecker checker = new DataPackNamespaceChecker();
        checker.checkName(block);
        checker.checkName(entity);
        checker.checkName(namespace);
        this.name = name;
        this.block = block;
        this.entity = entity;
        this.level = level;
        this.namespace = namespace;
        this.angularVelocity = angularVelocity;
        this.size = size;
        this.wrapper = wr;
        this.home = home;
    }

    void createDataPack() {
        String dataBase = "<n>\\data\\<ns>\\functions\\".replace("<ns>", namespace).replace("<n>", name);
        String tickBase = "<n>\\data\\minecraft\\tags\\functions\\".replace("<n>", name);
        File folder = new File(home + dataBase);
        File tf = new File(home + tickBase);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        if (!tf.exists()) {
            tf.mkdirs();
        }
        makeEntryFunctions();
        makeMainFunctions();
        makeTerminalFunction();

        String pack_mcMeta = "{\n" +
                "\t\"pack\": {\n" +
                "\t\t\"pack_format\": 1,\n" +
                "\t\t\"description\": \"Fourier series and epicycles\"\n" +
                "\t}\n" +
                "}";
        byte[] pmj = pack_mcMeta.getBytes();
        write(pmj, "pack.mcmeta");

        String tick_json = "{\n" +
                "\t\"values\": [\n" +
                "\t\t\"<ns>:main\"\n".replaceAll("<ns>", namespace) +
                "\t]\n" +
                "}";
        write(tick_json.getBytes(), "data\\minecraft\\tags\\functions\\tick.json");
    }

    /**
     * The entry functions of the data pack:
     *      summons all vector entities
     *      creates the score board
     *      creates the switch function which is responsible for controlling the drawing process
     *      creates the switch function which is responsible for controlling the player's spying
     *
     * ps. I will not flatten the surface via entry functions. Do it yourself via WorldEdit or /fill command.
     */
    private void makeEntryFunctions() {
        final String sep = "summon <e> ~ ~ ~ {CustomName:\"{\\\"text\\\":\\\"vector0\\\",\\\"color\\\":\\\"green\\\"}\",CustomNameVisible:1b,NoGravity:1b,Marker:1b}\r\n".replace("<e>", entity);
        final String scp = "summon <e> ~ ~ ~ {CustomName:\"{\\\"text\\\":\\\"<THIS>\\\",\\\"color\\\":\\\"yellow\\\"}\",Marker:1b,CustomNameVisible:1b,NoGravity:1b,Rotation:[<TOWARDS_NEXT>f,0f]}\r\n".replace("<e>", entity);
        List<EllipseVector> vectors = wrapper.getVectors();

        StringBuilder en = new StringBuilder();
        double zPhase = rotationValue(wrapper.baseX, wrapper.baseY);
        String base = scp.replace("<THIS>", "vector0").replace("<TOWARDS_NEXT>", rf.format(zPhase));
        en.append(base);

        int i = 1;
        for (EllipseVector vector : vectors) {
            String tagP = "vector" + i;
            String tagN = "vector" + (-i);
            double posPhase = rotationValue(vector.RCosP, vector.RSinP);
            double negPhase = rotationValue(vector.rCosN, vector.rSinN);

            String sp = scp.replace("<THIS>", tagP).replace("<TOWARDS_NEXT>", rf.format(posPhase));
            String sn = scp.replace("<THIS>", tagN).replace("<TOWARDS_NEXT>", rf.format(negPhase));

            en.append(sp);
            en.append(sn);
            i++;
        }
        String PEN = scp.replace("<THIS>", "PEN").replace("<TOWARDS_NEXT>", rf.format(0));

        en.append(PEN);
        en.append("scoreboard objectives add func dummy {\"text\":\"Data\",\"color\":\"aqua\"}\r\n");
        en.append("scoreboard players set Start func 0\r\n");
        en.append("scoreboard players set Follow func 0\r\n");
        en.append("scoreboard objectives setdisplay sidebar func\r\n");

        byte[] sum = en.toString().getBytes();
        write(sum, "data\\<ns>\\functions\\init.mcfunction");

        StringBuilder mainBuilder = new StringBuilder();
        mainBuilder.append("execute if score Start func matches 1 run function <ns>:rotate\r\n".replaceAll("<ns>", namespace));
        mainBuilder.append("execute if score Start func matches 1 run function <ns>:draw\r\n".replaceAll("<ns>", namespace));
        mainBuilder.append("execute if score Follow func matches 1 run function <ns>:follow\r\n".replaceAll("<ns>", namespace));

        byte[] main = mainBuilder.toString().getBytes();
        write(main, "data\\<ns>\\functions\\main.mcfunction");

        String start = "scoreboard players set Start func 1";
        write(start.getBytes(), "data\\<ns>\\functions\\start.mcfunction");
    }

    /**
     * The main functions of the data pack:
     *      tick.json
     *      draw epicycles
     *      tp the nearest player
     *      clear entities
     */
    private void makeMainFunctions() {
        String vcp = "execute as @e[name=<THIS>,sort=nearest,limit=1] at @s run tp @e[name=<NEXT>,sort=nearest,limit=1] ^ ^ ^<d>\r\n";
        String rcp = "execute as @e[name=<T>,sort=nearest,limit=1] at @s run tp @s ~ ~ ~ ~<w> ~\r\n";
        String kcp = "kill @e[name=<t>]\r\n";
        String pcp = "execute as @e[name=<T>,sort=nearest,limit=1] at @s run tp @p ^ ^25 ^\r\n";
        String bcp = "execute as @e[name=PEN,sort=nearest,limit=1] at @s run setblock ^ ^-1 ^ <b>\r\n";
        String esc = "scoreboard players set Follow func 0";
        String spy = "scoreboard players set Follow func 1";

        double rad = (double) size / 100.0;
        List<EllipseVector> vectors = wrapper.getVectors();
        final double velocityBase = (double) angularVelocity / (200d * level);
        int i = 1;
        StringBuilder vectorBuilder = new StringBuilder();
        StringBuilder rotateBuilder = new StringBuilder();
        StringBuilder killerBuilder = new StringBuilder();
        killerBuilder.append("kill @e[name=vector0]\r\n");
        double zDistance = Math.sqrt(wrapper.baseX * wrapper.baseX + wrapper.baseY + wrapper.baseY);
        String zP = vcp.replaceAll("<THIS>", "vector0").replaceAll("<NEXT>", "vector1").replace("<d>", df.format(zDistance * rad));
        vectorBuilder.append(zP);

        for (EllipseVector vector : vectors) {
            double pw = Math.toDegrees((velocityBase * i));
            double nw = Math.toDegrees((-1) * (velocityBase * i));
            String tagP = "vector" + i;
            String tagN = "vector" + (-i);
            String next = i < level ? "vector" + (i + 1) : "PEN";

            String rP = rcp.replaceAll("<T>", tagP).replace("<w>", rf.format(pw));
            String rN = rcp.replaceAll("<T>", tagN).replace("<w>", rf.format(nw));
            String vP = vcp.replaceAll("<THIS>", tagP).replaceAll("<NEXT>", tagN).replace("<d>", df.format(vector.positiveRadius * rad));
            String vN = vcp.replaceAll("<THIS>", tagN).replaceAll("<NEXT>", next).replace("<d>", df.format(vector.negativeRadius * rad));
            String kP = kcp.replaceAll("<t>", tagP);
            String kN = kcp.replaceAll("<t>", tagN);

            vectorBuilder.append(vP).append(vN);
            rotateBuilder.append(rP).append(rN);
            killerBuilder.append(kP).append(kN);
            i++;
        }

        String kF = kcp.replaceAll("<t>", "PEN");
        killerBuilder.append(kF);

        String tp = pcp.replaceAll("<T>", "PEN");
        String sb = bcp.replaceAll("<T>", "PEN").replaceAll("<b>", block);
        vectorBuilder.append(sb);
        rotateBuilder.append("\r\n");
        String rot = rotateBuilder.toString();
        byte[] bs = rot.getBytes();
        write(bs, "data\\<ns>\\functions\\rotate.mcfunction");

        String vec = vectorBuilder.toString();
        byte[] vs = vec.getBytes();
        write(vs, "data\\<ns>\\functions\\draw.mcfunction");

        byte[] kl = killerBuilder.toString().getBytes();
        write(kl, "data\\<ns>\\functions\\clear.mcfunction");

        write(tp.getBytes(), "data\\<ns>\\functions\\follow.mcfunction");
        write(spy.getBytes(), "data\\<ns>\\functions\\spy.mcfunction");
        write(esc.getBytes(), "data\\<ns>\\functions\\escape.mcfunction");
    }

    /**
     * Stop drawing
     */
    private void makeTerminalFunction() {
        String stop = "scoreboard players set Start func 0";
        write(stop.getBytes(), "data\\<ns>\\functions\\stop.mcfunction");

        StringBuilder unloads = new StringBuilder();
        unloads.append("function <ns>:clear\r\n".replace("<ns>", namespace));
        unloads.append("scoreboard objectives remove func\r\n");
        write(unloads.toString().getBytes(), "data\\<ns>\\functions\\unload.mcfunction");
    }

    private void write(byte[] b, String p) {
        String path = p.replace("<ns>", namespace);
        try (OutputStream os = new FileOutputStream(new File(home + name + "\\" + path))) {
            os.write(b);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This function exists because Minecraft uses completely different logic on axises.
     * Normally:
     *            ^ +y; o = pi / 2
     *            |
     *            |
     * -----------|------------> +x; o = 0
     *            |
     *            |
     *            | -y; o = -pi / 2
     *
     * But in Minecraft things go different:
     *            | -z; o = -pi
     *            |
     *            |
     * -----------|------------> +x; o = -pi / 2
     *            |
     *            |
     *            V +z; o = 0
     *
     * So I have to transform these values, especially angles, from what we usually know to what it should be in Minecraft.
     * @param relativeX x value what we usually know
     * @param relativeY y value what we usually know
     * @return The angle converted
     */
    private static double rotationValue(double relativeX, double relativeY) {
        double radius = Math.sqrt(relativeX * relativeX + relativeY * relativeY);
        double rad = Math.abs(Math.acos(relativeX / radius));
        double i = relativeY >= 0 ? 1 : -1;
        double deg = Math.toDegrees(rad * i) + 90d;
        return (deg + 360d) % 360d - 180d;
    }
}

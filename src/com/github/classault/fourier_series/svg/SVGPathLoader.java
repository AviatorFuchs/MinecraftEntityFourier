package com.github.classault.fourier_series.svg;

import org.apache.commons.digester3.Digester;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class SVGPathLoader {
    private Digester digester = new Digester();

    public static byte[] loadPath(InputStream is) {
        return new SVGPathLoader().load(is);
    }

    private SVGPathLoader() {
        digester.addObjectCreate("svg", "com.github.classault.fourier_series.svg.SVGBase");
        digester.addSetProperties("svg");

        digester.addObjectCreate("svg/path", "com.github.classault.fourier_series.svg.SVGPath");
        digester.addSetProperties("svg/path");
        digester.addSetNext("svg/path", "addPath", "com.github.classault.fourier_series.svg.SVGBase");

        digester.addObjectCreate("svg/g", "com.github.classault.fourier_series.svg.SVGGroup");
        digester.addSetProperties("svg/g");
        digester.addSetNext("svg/g", "addGroup", "com.github.classault.fourier_series.svg.SVGBase");

        digester.addObjectCreate("svg/g/path", "com.github.classault.fourier_series.svg.SVGPath");
        digester.addSetProperties("svg/g/path");
        digester.addSetNext("svg/g/path", "addPath", "com.github.classault.fourier_series.svg.SVGGroup");
    }

    private byte[] load(InputStream is) {
        try {
            SVGBase base = digester.parse(is);

            List<SVGPath> paths = base.getPathList();
            List<SVGGroup> groups = base.getGroupList();
            if (!paths.isEmpty()) {
                return paths.get(0).getD().getBytes();
            } else if (!groups.isEmpty()) {
                for (SVGGroup group : groups) {
                    List<SVGPath> pathList = group.getPathList();
                    if (!pathList.isEmpty()) return pathList.get(0).getD().getBytes();
                }
            } else {
                return new byte[0];
            }
        } catch (SAXException | IOException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }
}
/*
* try {
            SVGBase base = digester.parse(is);
            String primary = base.getPathList().get(0).getD();

            List<SVGPath> paths = base.getPathList();
            List<SVGGroup> groups = base.getGroupList();
            if (!paths.isEmpty()) {
                primary = paths.get(0).getD();
            } else if (!groups.isEmpty()) {
                for (SVGGroup group : groups) {

                }
            } else {
                throw new NullPointerException("Nothing valid found in this svg file");
            }
            return primary.getBytes();
        } catch (SAXException | IOException e) {
            e.printStackTrace();
            return DEMO_D.getBytes();
        }*/

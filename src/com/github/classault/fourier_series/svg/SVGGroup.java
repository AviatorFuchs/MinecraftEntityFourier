package com.github.classault.fourier_series.svg;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SVGGroup {
    private List<SVGPath> pathList = new ArrayList<>();

    private List<SVGGroup> gList = new ArrayList<>();

    public List<SVGPath> getPathList() {
        return pathList;
    }

    public void setPathList(List<SVGPath> pathList) {
        this.pathList = pathList;
    }

    public Iterator<SVGPath> getPaths () {
        return pathList.iterator();
    }

    public void addPath(SVGPath path) {
        pathList.add(path);
    }

    public List<SVGGroup> getGroupList() {
        return gList;
    }

    public void setGroup(List<SVGGroup> gList) {
        this.gList = gList;
    }

    public Iterator<SVGGroup> getGroups () {
        return gList.iterator();
    }

    public void addGroup(SVGGroup g) {
        gList.add(g);
    }
}

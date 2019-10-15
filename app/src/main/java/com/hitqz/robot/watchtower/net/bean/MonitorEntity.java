package com.hitqz.robot.watchtower.net.bean;

import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;

public class MonitorEntity {

    /**
     * monitor : true
     * hasIgnoreRegion : true
     * ignoreRegion : {"leftDownPoint":{"x":0,"y":500},"rightTopPoint":{"x":500,"y":1000}}
     * ignoreRegionList : [{"leftDownPoint":{"x":0,"y":500},"rightTopPoint":{"x":500,"y":1000}},{"leftDownPoint":{"x":0,"y":500},"rightTopPoint":{"x":500,"y":1000}}]
     */

    private boolean monitor;

    public boolean isMonitor() {
        return monitor;
    }

    public void setMonitor(boolean monitor) {
        this.monitor = monitor;
    }

    public boolean isHasIgnoreRegion() {
        return hasIgnoreRegion;
    }

    public void setHasIgnoreRegion(boolean hasIgnoreRegion) {
        this.hasIgnoreRegion = hasIgnoreRegion;
    }

    public IgnoreRegionBean getIgnoreRegion() {
        return ignoreRegion;
    }

    public void setIgnoreRegion(IgnoreRegionBean ignoreRegion) {
        this.ignoreRegion = ignoreRegion;
    }

    public List<IgnoreRegionBean> getIgnoreRegionList() {
        return ignoreRegionList;
    }

    public void setIgnoreRegionList(List<IgnoreRegionBean> ignoreRegionList) {
        this.ignoreRegionList = ignoreRegionList;
    }

    private boolean hasIgnoreRegion;
    private IgnoreRegionBean ignoreRegion;
    private java.util.List<IgnoreRegionBean> ignoreRegionList;

    public static IgnoreRegionBean toIgnoreRegionBean(Point[] points) {
        IgnoreRegionBean ignoreRegionBean = new IgnoreRegionBean();
        IgnoreRegionBean.LeftDownPointBean leftDownPointBean = new IgnoreRegionBean.LeftDownPointBean();
        leftDownPointBean.x = points[0].x;
        leftDownPointBean.y = points[0].y;

        IgnoreRegionBean.RightTopPointBean rightTopPointBean = new IgnoreRegionBean.RightTopPointBean();
        rightTopPointBean.x = points[1].x;
        rightTopPointBean.y = points[1].y;
        ignoreRegionBean.setLeftDownPoint(leftDownPointBean);
        ignoreRegionBean.setRightTopPoint(rightTopPointBean);
        return ignoreRegionBean;
    }

    public static List<IgnoreRegionBean> toIgnoreRegionList(Point[] points) {
        IgnoreRegionBean ignoreRegionBean0 = toIgnoreRegionBean(points);
        IgnoreRegionBean ignoreRegionBean1 = new IgnoreRegionBean();
        IgnoreRegionBean.LeftDownPointBean leftDownPointBean = new IgnoreRegionBean.LeftDownPointBean();
        leftDownPointBean.x = points[2].x;
        leftDownPointBean.y = points[2].y;

        IgnoreRegionBean.RightTopPointBean rightTopPointBean = new IgnoreRegionBean.RightTopPointBean();
        rightTopPointBean.x = points[3].x;
        rightTopPointBean.y = points[3].y;
        ignoreRegionBean1.setLeftDownPoint(leftDownPointBean);
        ignoreRegionBean1.setRightTopPoint(rightTopPointBean);

        List<IgnoreRegionBean> list = new ArrayList<>();
        list.add(ignoreRegionBean0);
        list.add(ignoreRegionBean1);
        return list;
    }

    public static class IgnoreRegionBean {
        /**
         * leftDownPoint : {"x":0,"y":500}
         * rightTopPoint : {"x":500,"y":1000}
         */

        private LeftDownPointBean leftDownPoint;
        private RightTopPointBean rightTopPoint;

        public LeftDownPointBean getLeftDownPoint() {
            return leftDownPoint;
        }

        public void setLeftDownPoint(LeftDownPointBean leftDownPoint) {
            this.leftDownPoint = leftDownPoint;
        }

        public RightTopPointBean getRightTopPoint() {
            return rightTopPoint;
        }

        public void setRightTopPoint(RightTopPointBean rightTopPoint) {
            this.rightTopPoint = rightTopPoint;
        }

        public static class LeftDownPointBean {
            /**
             * x : 0
             * y : 500
             */

            private int x;
            private int y;

            public int getX() {
                return x;
            }

            public void setX(int x) {
                this.x = x;
            }

            public int getY() {
                return y;
            }

            public void setY(int y) {
                this.y = y;
            }
        }

        public static class RightTopPointBean {
            /**
             * x : 500
             * y : 1000
             */

            private int x;
            private int y;

            public int getX() {
                return x;
            }

            public void setX(int x) {
                this.x = x;
            }

            public int getY() {
                return y;
            }

            public void setY(int y) {
                this.y = y;
            }
        }
    }
}

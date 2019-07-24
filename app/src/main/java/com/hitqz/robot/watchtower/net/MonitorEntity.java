package com.hitqz.robot.watchtower.net;

public class MonitorEntity {

    /**
     * hasIgnoreRegion : true
     * ignoreRegion : {"leftDownPoint":{"x":0,"y":500},"rightTopPoint":{"x":500,"y":1000}}
     */

    private boolean hasIgnoreRegion;
    private IgnoreRegionBean ignoreRegion;

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

package com.hitqz.robot.watchtower.net.bean;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class TemperatureList {

    /**
     * 1 : 33.3
     * 2 : 34.3
     * 3 : 34.3
     * 4 : 34.3
     * 5 : 34.3
     * 6 : 34.3
     * 7 : 34.3
     * 8 : 34.3
     * 9 : 34.3
     */

    public float _$1;
    public float _$2;
    public float _$3;
    public float _$4;
    public float _$5;
    public float _$6;
    public float _$7;
    public float _$8;
    public float _$9;

    public int size;

    public static TemperatureList fromRegionTemperatureList(RegionTemperatureList model) {

        TemperatureList temperatureList = new TemperatureList();
        temperatureList.size = 0;

        if (model.get_$1() != null && model.get_$1().size() > 0) {
            temperatureList._$1 = getTemperatureFromList(model.get_$1());
            temperatureList.size++;
        }
        if (model.get_$2() != null && model.get_$2().size() > 0) {
            temperatureList._$2 = getTemperatureFromList(model.get_$2());
            temperatureList.size++;
        }

        if (model.get_$3() != null && model.get_$3().size() > 0) {
            temperatureList._$3 = getTemperatureFromList(model.get_$3());
            temperatureList.size++;
        }

        if (model.get_$4() != null && model.get_$4().size() > 0) {
            temperatureList._$4 = getTemperatureFromList(model.get_$4());
            temperatureList.size++;
        }

        if (model.get_$5() != null && model.get_$5().size() > 0) {
            temperatureList._$5 = getTemperatureFromList(model.get_$5());
            temperatureList.size++;
        }

        if (model.get_$6() != null && model.get_$6().size() > 0) {
            temperatureList._$6 = getTemperatureFromList(model.get_$6());
            temperatureList.size++;
        }
        if (model.get_$7() != null && model.get_$7().size() > 0) {
            temperatureList._$7 = getTemperatureFromList(model.get_$7());
            temperatureList.size++;
        }
        if (model.get_$8() != null && model.get_$8().size() > 0) {
            temperatureList._$8 = getTemperatureFromList(model.get_$8());
            temperatureList.size++;
        }

        if (model.get_$9() != null && model.get_$9().size() > 0) {
            temperatureList._$9 = getTemperatureFromList(model.get_$9());
            temperatureList.size++;
        }

        return temperatureList;
    }

    private static float getTemperatureFromList(@NonNull List<RegionTemperature> list) {
        float sum = 0;
        for (int i = 0; i < list.size(); i++) {
            sum = sum + list.get(i).averageTemperature;
        }
        return sum / list.size();
    }

    public List<Float> toList() {
        List<Float> list = new ArrayList<>();
        if (_$1 != 0) {
            list.add(_$1);
        }
        if (_$2 != 0) {
            list.add(_$2);
        }
        if (_$3 != 0) {
            list.add(_$3);
        }
        if (_$4 != 0) {
            list.add(_$4);
        }
        if (_$5 != 0) {
            list.add(_$5);
        }
        if (_$6 != 0) {
            list.add(_$6);
        }
        if (_$7 != 0) {
            list.add(_$7);
        }
        if (_$8 != 0) {
            list.add(_$8);
        }
        if (_$9 != 0) {
            list.add(_$9);
        }
        return list;
    }
}

package com.hmaishop.pms.inspection.bean;

import com.google.gson.annotations.SerializedName;

/**
 * 巡查人员 Bean
 *
 * Created by Joker_Runner on 7/18 0018.
 */


public class Checker {
    public boolean isOk = true;
    @SerializedName("checker_num")
    int checkerId;
    @SerializedName("checker_name")
    String checkerName;
    @SerializedName("checker_head")
    String checkerIcon;
    @SerializedName("checker_version")
    double version;

    public boolean isOk() {
        return isOk;
    }

    public int getCheckerId() {
        return checkerId;
    }

    public String getCheckerName() {
        return checkerName;
    }

    public String getCheckerIcon() {
        return checkerIcon;
    }

    public double getVersion() {
        return version;
    }
}

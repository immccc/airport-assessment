package com.immccc.aircontrol.plane.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PlaneSize {
    SMALL(1), BIG(2);

    private int width;

}

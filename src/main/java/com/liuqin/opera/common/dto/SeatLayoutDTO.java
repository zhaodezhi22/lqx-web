package com.liuqin.opera.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SeatLayoutDTO implements Serializable {
    private List<SeatRow> rows;

    @Data
    public static class SeatRow implements Serializable {
        private Integer row;
        private List<Seat> seats;
    }

    @Data
    public static class Seat implements Serializable {
        private String seatId; // 例如 "R1C5"
        private Integer col;
        private Integer status; // 0 可选, 1 已售, 2 已锁定
        private String label;   // 展示用，如 "1排5座"
    }
}


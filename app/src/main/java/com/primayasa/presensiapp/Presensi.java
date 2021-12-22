package com.primayasa.presensiapp;

public class Presensi {
    private String tanggal;
    private String jam;
    private String status;

    public Presensi(String tanggal, String jam, String status){
        this.tanggal = tanggal;
        this.jam = jam;
        this.status = status;
    }

    public String getTanggal() {
        return tanggal;
    }

    public String getJam() {
        return jam;
    }

    public String getStatus() {
        return status;
    }
}

package br.com.truckZ.bean;

/**
 * Created by Ronan.lima on 06/07/16.
 */
public class Trailer {
    private boolean ativo;
    private String data_marcacao;
    private int hash_gm;
    private String hora;
    private double lat;
    private double lng;
    private String nome_trailer;
    private int param;

    public Trailer(boolean isAtivo, String dataMarcacao, int hash_gm, String horaMarcacao, double latitude, double longitude, String nomeTrailer, int param) {
        setAtivo(isAtivo);
        setData_marcacao(dataMarcacao);
        setHash_gm(hash_gm);
        setHora(horaMarcacao);
        setLat(latitude);
        setLng(longitude);
        setNome_trailer(nomeTrailer);
        setParam(param);
    }

    public Trailer() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Trailer trailer = (Trailer) o;

        if (getHash_gm() != trailer.getHash_gm()) return false;
        if (getParam() != trailer.getParam()) return false;
        return getNome_trailer().equals(trailer.getNome_trailer());

    }

    @Override
    public int hashCode() {
        int result = getHash_gm();
        result = 31 * result + getNome_trailer().hashCode();
        result = 31 * result + getParam();
        return result;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public String getData_marcacao() {
        return data_marcacao;
    }

    public void setData_marcacao(String data_marcacao) {
        this.data_marcacao = data_marcacao;
    }

    public int getHash_gm() {
        return hash_gm;
    }

    public void setHash_gm(int hash_gm) {
        this.hash_gm = hash_gm;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getNome_trailer() {
        return nome_trailer;
    }

    public void setNome_trailer(String nome_trailer) {
        this.nome_trailer = nome_trailer;
    }

    public int getParam() {
        return param;
    }

    public void setParam(int param) {
        this.param = param;
    }
}


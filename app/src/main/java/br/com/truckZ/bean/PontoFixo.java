package br.com.truckZ.bean;

/**
 * Created by Ronan.lima on 09/07/16.
 */
public class PontoFixo {
    private String idPontoFixoFirabase;
    private Trailer trailer;

    public PontoFixo() {
    }

    public String getIdPontoFixoFirabase() {
        return idPontoFixoFirabase;
    }

    public void setIdPontoFixoFirabase(String idPontoFixoFirabase) {
        this.idPontoFixoFirabase = idPontoFixoFirabase;
    }

    public Trailer getTrailer() {
        return trailer;
    }

    public void setTrailer(Trailer trailer) {
        this.trailer = trailer;
    }
}

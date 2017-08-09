package br.com.truckZ;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidmapsextensions.GoogleMap;
import com.androidmapsextensions.MapFragment;
import com.androidmapsextensions.Marker;
import com.androidmapsextensions.MarkerOptions;
import com.androidmapsextensions.OnMapReadyCallback;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import br.com.truckZ.bean.PontoFixo;
import br.com.truckZ.bean.Trailer;
import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by Ronan.lima on 06/07/16.
 */
public class MapaFragment extends Fragment implements OnMapReadyCallback, View.OnClickListener{
    private static final String TAG = "MAPA_FRAGMENT";

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mReference;
    private ValueEventListener listenerTrailers;
    private ValueEventListener listenerPontosFixos;
    private GoogleMap mgoogleMap;
    private List<PontoFixo> pontosFixos;
    private SweetAlertDialog sweetAlertDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mapa_fragment, container, false);
        MapFragment mapFragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getExtendedMapAsync(this);

        exibeDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE, "Buscando FoodTruckZ ativos...");
        initializeFirebase();
        return view;
    }

    private void exibeDialog(Context context, int tipoAlerta, String msg) {
        sweetAlertDialog = new SweetAlertDialog(context, tipoAlerta);
        sweetAlertDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        sweetAlertDialog.setTitleText(msg);
        sweetAlertDialog.setCancelable(false);
        sweetAlertDialog.show();
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        setPontosFixos(new ArrayList<PontoFixo>());
        buscaPontosMoveis();
        buscaPontosFixos();
    }

    private void buscaPontosFixos() {
        mReference = mDatabase.getReference("ponto_fixo_horarios");
        mReference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()){
                    recebePontoFixoHorarioFirebase(data);
                }
                mReference = mDatabase.getReference("ponto_fixo");
                listenerPontosFixos = new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot data : dataSnapshot.getChildren()){
                            recuperaPontoFixoByHorario(data);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                };
                mReference.addValueEventListener(listenerPontosFixos);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void recuperaPontoFixoByHorario(DataSnapshot data) {
        if (getPontosFixos().size() != 0){
            for (PontoFixo p : getPontosFixos()) {
                if (p.getIdPontoFixoFirabase().equals(data.getKey())){
                    p.getTrailer().setLat(Double.parseDouble(data.child("lat").getValue().toString()));
                    p.getTrailer().setLng(Double.parseDouble(data.child("lng").getValue().toString()));
                    p.getTrailer().setNome_trailer(data.child("nome").getValue().toString());

                    if (!isExisteTrailerIntoMarker(p.getTrailer()) && (Boolean) data.child("ativo").getValue()){
                        addPinoMapa(p.getTrailer(), p.getTrailer().getNome_trailer()+";"+p.getTrailer().getHora(), false);
                    } else {
                        atualizarPinoMapa(p.getTrailer());
                    }
                }
            }
        }
    }

    private void recebePontoFixoHorarioFirebase(DataSnapshot data) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        int diaSemana = cal.get(Calendar.DAY_OF_WEEK);

        switch (diaSemana){
            case 1://domingo
                addTrailerIntoListByDiaSemana(data, data.child("dom").getValue().toString());
                break;
            case 2:
                addTrailerIntoListByDiaSemana(data, data.child("seg").getValue().toString());
                break;
            case 3:
                addTrailerIntoListByDiaSemana(data, data.child("ter").getValue().toString());
                break;
            case 4:
                addTrailerIntoListByDiaSemana(data, data.child("qua").getValue().toString());
                break;
            case 5:
                addTrailerIntoListByDiaSemana(data, data.child("qui").getValue().toString());
                break;
            case 6:
                addTrailerIntoListByDiaSemana(data, data.child("sex").getValue().toString());
                break;
            case 7://sabado
                addTrailerIntoListByDiaSemana(data, data.child("sab").getValue().toString());
                break;
        }
    }

    private void addTrailerIntoListByDiaSemana(DataSnapshot data, String horaDiaSemana) {
        if(!horaDiaSemana.equalsIgnoreCase("Fechado")){
            Trailer t = new Trailer();
            t.setHora(horaDiaSemana);
            if (getPontosFixos().size() != 0){
                for (PontoFixo p : getPontosFixos()) {
                    if (p.getIdPontoFixoFirabase().equals(data.getKey())){
                        p.getTrailer().setHora(horaDiaSemana);
                        break;
                    } else {
                        instanciaPontoFixo(data, t);
                    }
                }
            } else {
                instanciaPontoFixo(data, t);
            }
        }
    }

    private void instanciaPontoFixo(DataSnapshot data, Trailer t) {
        PontoFixo pontoFixo = new PontoFixo();
        pontoFixo.setIdPontoFixoFirabase(data.getKey());
        pontoFixo.setTrailer(t);
        getPontosFixos().add(pontoFixo);
    }

    private void buscaPontosMoveis() {
        mReference = mDatabase.getReference("ponto_movel_trailer");

        mReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()){
                    recebeTrailerFirebase(data);
                }
                sweetAlertDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        listenerTrailers = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()){
                    recebeTrailerFirebase(data);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mReference.addValueEventListener(listenerTrailers);
    }

    private void recebeTrailerFirebase(DataSnapshot foodTruck) {
        Calendar cal = Calendar.getInstance();
        int dia = cal.get(Calendar.DAY_OF_MONTH);
        int mes = cal.get(Calendar.MONTH);
        int ano = cal.get(Calendar.YEAR);

        for (DataSnapshot trailer : foodTruck.getChildren()){
            String dataMarcacao = trailer.child("data_marcacao").getValue().toString();
            String[] split = dataMarcacao.split("/");
            Trailer t = copyDataSnapshotToTrailer(trailer);
            if (Integer.parseInt(split[0]) == dia && Integer.parseInt(split[1])-1 == mes && Integer.parseInt(split[2]) == ano){
                if (!isExisteTrailerIntoMarker(t) && t.isAtivo()){
                    addPinoMapa(t, t.getNome_trailer()+";"+t.getHora(), true);
                } else {
                    atualizarPinoMapa(t);
                }
            } else if (isExisteTrailerIntoMarker(t) && (Integer.parseInt(split[0]) != dia || Integer.parseInt(split[1])-1 != mes || Integer.parseInt(split[2]) != ano)){
                removePinoDataDiferenteAtual(t);
            }
        }
    }

    private void removePinoDataDiferenteAtual(Trailer t) {
        int aux = 0;
        for (Marker m : getMgoogleMap().getDisplayedMarkers()) {
            if(((Trailer)m.getData()).getParam() == t.getParam() && ((Trailer)m.getData()).getNome_trailer().equals(t.getNome_trailer())){
                getMgoogleMap().getMarkers().get(aux).remove();
                break;
            }
            aux++;
        }
    }

    private void atualizarPinoMapa(Trailer trailerFirebase) {
        boolean isRemover = false;
        int cont = 0;
        for (Marker m : getMgoogleMap().getDisplayedMarkers()) {
            if (m.getData().equals(trailerFirebase)){
                if (trailerFirebase.isAtivo() == false){
                    isRemover = true;
                    break;
                } else {
                    if (!((Trailer)m.getData()).getHora().equals(trailerFirebase.getHora())){
                        m.setSnippet(trailerFirebase.getHora());
                    }
                    m.setData(trailerFirebase);
                    getMgoogleMap().getMarkers().get(cont).setPosition(new LatLng(trailerFirebase.getLat(), trailerFirebase.getLng()));
                    break;
                }
            }
            cont++;
        }
        if (isRemover){
            getMgoogleMap().getMarkers().get(cont).remove();
        }
    }

    private boolean isExisteTrailerIntoMarker(Trailer trailerFirebase){
        for (Marker m : getMgoogleMap().getDisplayedMarkers()) {
            if (m.getData().equals(trailerFirebase)){
                return true;
            }
        }
        return false;
    }

    private Trailer copyDataSnapshotToTrailer(DataSnapshot data){
        Trailer t = new Trailer();
        t.setAtivo((Boolean) data.child("ativo").getValue());
        t.setData_marcacao(data.child("data_marcacao").getValue().toString());
        t.setHora(data.child("hora").getValue().toString());
        t.setLat(Double.parseDouble(data.child("lat").getValue().toString()));
        t.setLng(Double.parseDouble(data.child("lng").getValue().toString()));
        t.setNome_trailer(data.child("nome_trailer").getValue().toString());
        t.setParam(Integer.parseInt(data.child("param").getValue().toString()));
        return t;
    }

    private Marker addPinoMapa(Trailer trailer, String msg, boolean isPontoMovel) {
        Marker mark = getMgoogleMap().addMarker(new MarkerOptions()
                .anchor(0.0f, 0.1f)
                .position(new LatLng(trailer.getLat(), trailer.getLng()))
                .draggable(true));
        if (msg.contains(";")){
            mark.setTitle(msg.split(";")[0]);
            mark.setSnippet(msg.split(";")[1]);
        } else {
            mark.setTitle(msg);
        }
        if (isPontoMovel){
            mark.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        } else {
            mark.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
        }
        mark.setData(trailer);
        return mark;
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        double latitude = 0.0;
        double longitude = 0.0;
        if(getArguments() != null){
            latitude = getArguments().getDouble("latitude");
            longitude = getArguments().getDouble("longitude");
        }
        googleMap.setMyLocationEnabled(true);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 10));
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        setMgoogleMap(googleMap);
    }

    public GoogleMap getMgoogleMap() {
        return mgoogleMap;
    }

    public void setMgoogleMap(GoogleMap mgoogleMap) {
        this.mgoogleMap = mgoogleMap;
    }

    public List<PontoFixo> getPontosFixos() {
        return pontosFixos;
    }

    public void setPontosFixos(List<PontoFixo> pontosFixos) {
        this.pontosFixos = pontosFixos;
    }
}


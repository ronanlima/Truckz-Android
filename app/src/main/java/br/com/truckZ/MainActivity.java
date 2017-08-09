package br.com.truckZ;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by Ronan.lima on 06/07/16.
 */
public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private static final String TAG = "TruckZ";
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private Location localizacao;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private static int INTERVALO_ATUALIZACAO = 1000 * 7;
    private static int INTERVALOR_RAPIDO = 1000 * 2;
    private static int DISTANCIA = 30; // 30 metros
    private double latitude;
    private double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (checkPlayServices()){
            createLocationRequest();
            buildGoogleApiClient();
        }

    }

    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(INTERVALO_ATUALIZACAO);
        locationRequest.setFastestInterval(INTERVALOR_RAPIDO);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setSmallestDisplacement(DISTANCIA);
    }

    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    private boolean checkPlayServices() {
        int resultado = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultado != ConnectionResult.SUCCESS){
            if (GooglePlayServicesUtil.isUserRecoverableError(resultado)){
                GooglePlayServicesUtil.getErrorDialog(resultado, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(this, getResources().getString(R.string.aviso_alertdialog_dispositivo_incompativel_gps), Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }

    private void getLocation() {
        localizacao = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (localizacao != null){
            latitude = localizacao.getLatitude();
            longitude = localizacao.getLongitude();
        } else {
            Log.d(TAG, "GPS desabilitado. Solicitando sua habilitação..."+System.currentTimeMillis());
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle(getResources().getString(R.string.titulo_alertdialog_solicita_gps));
            alertDialog.setMessage(getResources().getString(R.string.mensagem_alertdialog_solicita_gps));
            alertDialog.setPositiveButton(getResources().getString(R.string.btn_alertdialog_configuracoes_gps), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getBaseContext().startActivity(intent);
                }
            });
            alertDialog.setNegativeButton(getResources().getString(R.string.btn_alertdialog_cancelar_gps), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            alertDialog.show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (googleApiClient != null){
            googleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(googleApiClient.isConnected()){
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
//getLocation();
        Log.i(TAG, "Nao faz nada. O usuario precisa clicar no botao de marcar localizacao para o " +
                "sistema conseguir a latitude e longitude.");
        if (checkPlayServices()){
            if (locationRequest != null && googleApiClient != null){
                getLocation();
                if (latitude != 0.0 && longitude != 0.0){
                    Log.d(TAG, "Localização recuperada. Latitude = "+latitude+"; Longitude = "+longitude);
//                    addBottomBar(bundle);
//                    addTextViewBottomBar(bundle);
                    addFragmentMapa();
                } else {
                    Log.d(TAG, "O usuário habilitou o GPS, mas no entanto, não foi possível" +
                            " recuperar sua localização. Última localização conhecida: lat="+latitude+";long="+longitude);
                }
            }
        }
    }

    private void addFragmentMapa() {
        Bundle arguments = new Bundle();
        arguments.putDouble("latitude", latitude);
        arguments.putDouble("longitude", longitude);
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment localAtual = new MapaFragment();
        localAtual.setArguments(arguments);
        ft.add(R.id.layout_principal, localAtual, "FragmentLocalAtual");
        ft.commit();
    }

    @Override
    public void onConnectionSuspended(int i) {
        googleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.getErrorCode() == ConnectionResult.SERVICE_MISSING){
            Log.i(TAG, connectionResult.getErrorMessage());
        } else if (connectionResult.getErrorCode() == ConnectionResult.SERVICE_DISABLED){
            Log.i(TAG, "O GPS está desligado.");
        } else if (connectionResult.getErrorCode() == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED) {
            Log.i(TAG, "É necessário atualizar a biblioteca do Google Play Services!");
        }
        Log.i(TAG, "Conexão falhou: ConnectionResult.getErrorCode = " + connectionResult.getErrorCode());
    }
}


package com.scamera.hp.scamera;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class FingerprintHandler extends
        FingerprintManager.AuthenticationCallback {

    private CancellationSignal cancellationSignal;
    private Context appContext;

    public FingerprintHandler(Context context) {
        appContext = context;
    }

    public void startAuth(FingerprintManager manager, FingerprintManager.CryptoObject cryptoObject) {

        cancellationSignal = new CancellationSignal();

        if (ActivityCompat.checkSelfPermission(appContext,
                Manifest.permission.USE_FINGERPRINT) !=
                PackageManager.PERMISSION_GRANTED) {
            return;
        }
        manager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
    }

    @Override
    public void onAuthenticationError(int errMsgId,
                                      CharSequence errString) {
        Toast.makeText(appContext,
                "Authentication error\n" + errString,
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAuthenticationHelp(int helpMsgId,
                                     CharSequence helpString) {
        Toast.makeText(appContext,
                "Authentication help\n" + helpString,
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAuthenticationFailed() {
        Toast.makeText(appContext,
                "Authentication failed.",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAuthenticationSucceeded(
            FingerprintManager.AuthenticationResult result) {

        Toast.makeText(appContext,
                "Authentication succeeded.",
                Toast.LENGTH_LONG).show();
        //TODO zapelnic PINbox tak jakby sie samo wpisalo wiec odkodowanie z bazy hasla zahaszowanego
        View rootView = ((Activity) appContext).getWindow().getDecorView().findViewById(android.R.id.content);
        Button button = (Button) rootView.findViewById(R.id.camera);
        button.setEnabled(true);
    }

}

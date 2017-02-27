package com.scamera.hp.scamera;

import android.Manifest;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.hardware.SensorManager;
import android.hardware.fingerprint.FingerprintManager;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class FingerPrintTest extends Activity {

    //name that will be used when storing the key in the Keystore container.
    private static final String KEY_NAME = "example_key";
    private FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;
    private KeyStore keyStore;
    private KeyGenerator keyGenerator;
    private Cipher cipher;
    private FingerprintManager.CryptoObject cryptoObject;

    private AutoFitTextureView mTextureView;
    private CameraRaw mCameraRaw;
    private OrientationEventListener mOrientationListener;

    private PinBox mpinbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finger_print_test);

        //Fingerprint Authentication
        keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);

        //sprawdzenie czy jest jakas inna forma sprawdzenia bezpieczenstwa w tym przypadku blokada na graficzny znak, szlaczek
        if (!keyguardManager.isKeyguardSecure()) {
            Toast.makeText(this,"Lock screen security not enabled in Settings",
                    Toast.LENGTH_LONG).show();
            return;
        }

        //zy jest ustalone permission ze fingerprint uzywac mozna
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.USE_FINGERPRINT) !=
                PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this,"Fingerprint authentication permission not enabled",
                    Toast.LENGTH_LONG).show();

            return;
        }

        //sprawdza czy sa jakies w bazie telefonu odciski palca
        if (!fingerprintManager.hasEnrolledFingerprints()) {
            // This happens when no fingerprints are registered.
            Toast.makeText(this,"Register at least one fingerprint in Settings",
                    Toast.LENGTH_LONG).show();
            return;
        }

        generateKey();

        if (cipherInit()) {
            cryptoObject = new FingerprintManager.CryptoObject(cipher);
            FingerprintHandler helper = new FingerprintHandler(this);
            helper.startAuth(fingerprintManager, cryptoObject);
        }

        //camera view and button
        final Button button = (Button)findViewById(R.id.camera);
        button.setEnabled(false);
        mTextureView = (AutoFitTextureView) this.findViewById(R.id.texture);
        mCameraRaw = new CameraRaw(this);

        mOrientationListener = new OrientationEventListener(this,
                SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {
                if (mTextureView != null && mTextureView.isAvailable()) {
                    mCameraRaw.configureTransform(mTextureView.getWidth(), mTextureView.getHeight());
                }
            }
        };

        //PIN
        mpinbox = new PinBox(this);

        //button fo on / off camera preview
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                String buttonText = button.getText().toString();
                if(buttonText.equals("Camera ON")) {
                    ((View) mTextureView).setVisibility(View.VISIBLE);
                    button.setText("Camera OFF");
                    mCameraRaw.onButton(mTextureView, mOrientationListener);
                    mOrientationListener.enable();
                }
                else if(buttonText.equals("Camera OFF")) {
                    mCameraRaw.offButton(mOrientationListener);
                    button.setText("Camera ON");
                    ((View) mTextureView).setVisibility(View.GONE);
                    mOrientationListener.disable();
                }
            }
        });
    }

    //potrzebna jest genereacja klucza ktory bedzie bezpiecznie przechowywany na telefonie przy uzyciu keystore syste
    //ta metoda bedzie generowac i przechowywac klucz
    protected void generateKey() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            //generowanie klucza przy pomocy keygenerator service
            keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    "AndroidKeyStore");
        } catch (NoSuchAlgorithmException |
                NoSuchProviderException e) {
            throw new RuntimeException(
                    "Failed to get KeyGenerator instance", e);
        }

        try {
            //etUserAuthenticationRequired method call configures the key such that the user is required to authorize every use of the key with a fingerprint authentication
            keyStore.load(null);
            keyGenerator.init(new KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                            KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException |
                InvalidAlgorithmParameterException
                | CertificateException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    //Now that the key has been generated the next step is to initialize the cipher that will be
    //used to create the encrypted FingerprintManager.CryptoObject instance.This CryptoObject will, in turn, be used during the fingerprint authentication process
    public boolean cipherInit() {
        try {
            cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException |
                NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }

        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
                    null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException
                | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }
}

package com.example.tugasqrscaner21c2;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // objek
    private Button buttonScan;
    private TextView textViewName, textViewClass, textViewID;

    // QRCode Scanner
    private IntentIntegrator qrscan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // View
        buttonScan = (Button) findViewById(R.id.buttonScan);
        textViewName = (TextView) findViewById(R.id.textViewNama);
        textViewClass = (TextView) findViewById(R.id.textViewKelas);
        textViewID = (TextView) findViewById(R.id.textViewNIM);

        qrscan = new IntentIntegrator(this);

        buttonScan.setOnClickListener(this);

        // Meminta izin untuk mengakses fitur panggilan telepon
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CALL_PHONE}, 1);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Not Scanned", Toast.LENGTH_LONG).show();
            }
            // JSON
            try {
                JSONObject jsonObject = new JSONObject(result.getContents());
                textViewName.setText(jsonObject.getString("nama"));
                textViewClass.setText(jsonObject.getString("kelas"));
                textViewID.setText(jsonObject.getString("nim"));
            } catch (JSONException e) {
// WEBVIEW
                if (Patterns.WEB_URL.matcher(result.getContents()).matches()) {
                    Intent visitUrl = new Intent(Intent.ACTION_VIEW, Uri.parse(result.getContents()));
                    startActivity(visitUrl);
                } else {
// Mengecek apakah data yang di scan merupakan lokasi
                    if (result.getContents().contains("geo:")) {
// Memisahkan latitude dan longitude dari data yang di scan
                        String[] geoLocation = result.getContents().split(":")[1].split("\\?")[0].split(",");
                        double latitude = Double.parseDouble(geoLocation[0]);
                        double longitude = Double.parseDouble(geoLocation[1]);

// Membuka aplikasi Google Maps dan menampilkan lokasi yang di scan
                        Uri gmmIntentUri = Uri.parse("geo:" + latitude + "," + longitude);
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        mapIntent.setPackage("com.google.android.apps.maps");
                        startActivity(mapIntent);
                    } else {
// DIAL UP, NOMOR TELEPON
                        try {
                            Intent intent2 = new Intent(Intent.ACTION_CALL, Uri.parse(result.getContents()));
                            startActivity(intent2);
                        } catch (Exception e2) {
                            Toast.makeText(this, "Not Scanned", Toast.LENGTH_LONG).show();
                        }
// Logika Jika ada email pada barcode yang sudah ter-scan
                        if (result.getContents() != null) {
                            String string = result.getContents();
                            String[] parts = string.split("[:;]");
                            String to = parts[2];
                            String subject = parts[4];
                            String text = parts[6];
                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.putExtra(Intent.EXTRA_EMAIL, new String []{to});
                            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                            intent.putExtra(Intent.EXTRA_TEXT, text);
                            intent.setType("text/html");
                            intent.setPackage("com.google.android.gm");
                            startActivity(Intent.createChooser(intent, "Send mail"));
                        }
                    }
                }
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onClick(View view) {
        qrscan.initiateScan();
    }
}
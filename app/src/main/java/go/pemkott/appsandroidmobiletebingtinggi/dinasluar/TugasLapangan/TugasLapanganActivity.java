package go.pemkott.appsandroidmobiletebingtinggi.dinasluar.TugasLapangan;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.database.DatabaseHelper;
import go.pemkott.appsandroidmobiletebingtinggi.model.Kegiatan;


public class TugasLapanganActivity extends AppCompatActivity {

    private static ArrayList<Kegiatan> list = new ArrayList<>();
    private ArrayList<String> kegiatanChecked = new ArrayList<String>();
    private ArrayList<String>  kegiatanAdded = new ArrayList<String>();
    static ArrayList<String> kegiatansList = new ArrayList<String>();
    String kegiatansLainnya;
    StringBuffer buffer2;

    EditText etkegiatanLainnya;
    RecyclerView rvKegiatanPd;
    KegiatanAdapter kegiatanAdapter;
    DatabaseHelper databaseHelper;
    TextView tvlistChecked;
    String jam_masuk, jam_pulang;

    public static AppCompatActivity tL ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View decorView = getWindow().getDecorView();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.biru_1));
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(uiOptions);

        setContentView(R.layout.activity_tugas_lapangan);
        tL = this;

        list.clear();
        kegiatansList.clear();

        Bundle intent = getIntent().getExtras();
        jam_masuk = intent.getString("jam_masuk");
        jam_pulang = intent.getString("jam_pulang");


        databaseHelper = new DatabaseHelper(this);
        kegiatanDatabase();

        rvKegiatanPd = findViewById(R.id.rvKegiatanPd);
        etkegiatanLainnya = findViewById(R.id.etKegiatanLainmya);
        tvlistChecked = findViewById(R.id.tvlistChecked);
        list.addAll(getListData2());

        showRecyclerList();

        etkegiatanLainnya.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if (!s.toString().trim().isEmpty()){
//                    kegiatanAdded.add(etkegiatanLainnya.getText().toString());
//                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });



    }



    public void backTl(View view){
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public void kegiatanDatabase(){
        Cursor res = databaseHelper.getKegiatanIzin();
        while (res.moveToNext()){
            if (res.getString(1).equals("tl")){
                kegiatansList.add(res.getString(2));
            }
        }
    }

//    Memberikan nilai pada Model data
    static ArrayList<Kegiatan> getListData2() {
        ArrayList<Kegiatan> list = new ArrayList<>();
        list.clear();
        for (int position = 0; position < kegiatansList.size(); position++) {
            Kegiatan kegiatans = new Kegiatan();
            kegiatans.setKegiatan(kegiatansList.get(position));
            list.add(kegiatans);
        }
        return list;
    }

    public void nextKegiatanTL(View view){
        if (!etkegiatanLainnya.getText().toString().isEmpty()){
            kegiatansLainnya = etkegiatanLainnya.getText().toString();

        }else{
            kegiatansLainnya = "kosong";
        }

        if (kegiatanChecked.isEmpty() && kegiatansLainnya.equals("kosong")){
            showMessage("Peringatan!", "Anda Harus Mengisi Kegiatan Yang Dilaksanakan.");
        }else {

            Intent intentTL = new Intent(TugasLapanganActivity.this, TugasLapanganFinalActivity.class);
            intentTL.putExtra("jam_masuk", jam_masuk);
            intentTL.putExtra("jam_pulang", jam_pulang);
            intentTL.putExtra("kegiatans", kegiatanChecked);
            intentTL.putExtra("lainnya", kegiatansLainnya);
            intentTL.putExtra("title", "Isi Data Tugas Lapangan");
            startActivity(intentTL);

        }
    }

    public void showMessage(String title, String Message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.ThemeOverlay_App_MaterialAlertDialog);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(Message);
        builder.show();
    }

    private void showRecyclerList(){
        rvKegiatanPd.setLayoutManager(new LinearLayoutManager(this));
        kegiatanAdapter = new KegiatanAdapter(list);
        rvKegiatanPd.setAdapter(kegiatanAdapter);

        kegiatanAdapter.setOnItemClickCallback(new KegiatanAdapter.OnItemClickCallback() {
            @Override
            public void onItemClicked(Kegiatan data) {
                showSelectedKegiatan(data);
            }
        });
    }

    private void showSelectedKegiatan(Kegiatan kegiatan) {

        if (kegiatan.isChecked() == true ){
            kegiatanChecked.add(kegiatan.getKegiatan());

            buffer2 = new StringBuffer();
            for (int i = 0; i<kegiatanChecked.size()-1;i++){
                buffer2.append(kegiatanChecked.get(i)+", ");
            }
            buffer2.append(kegiatanChecked.get(kegiatanChecked.size()-1));

            tvlistChecked.setText(buffer2.toString().toUpperCase());
            tvlistChecked.setVisibility(View.VISIBLE);

        }else{

            kegiatanChecked.remove(kegiatan.getKegiatan());

            StringBuffer buffer = new StringBuffer();
            if (kegiatanChecked.size() == 1){
                buffer.append(kegiatanChecked.get(kegiatanChecked.size()-1));
                tvlistChecked.setText(buffer.toString().toUpperCase());
                tvlistChecked.setVisibility(View.VISIBLE);
            }else if(kegiatanChecked.isEmpty()){
                tvlistChecked.setVisibility(View.GONE);
            }

            else{
                for (int i = 0; i<kegiatanChecked.size()-1;i++){
                    buffer.append(kegiatanChecked.get(i)+", ");
                }
                buffer.append(kegiatanChecked.get(kegiatanChecked.size()-1)+", ");
                tvlistChecked.setText(buffer.toString().toUpperCase());
                tvlistChecked.setVisibility(View.VISIBLE);
            }
        }

    }


}
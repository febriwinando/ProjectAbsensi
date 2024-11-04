package go.pemkott.appsandroidmobiletebingtinggi.izinsift.izinsiftsakit;

import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.database.DatabaseHelper;
import go.pemkott.appsandroidmobiletebingtinggi.izin.Sakit.SakitAdapter;
import go.pemkott.appsandroidmobiletebingtinggi.model.Kegiatan;

public class SakitSiftActivity extends AppCompatActivity {
    private static ArrayList<Kegiatan> listSakit = new ArrayList<>();
    private ArrayList<String> kegiatanCheckedSakit = new ArrayList<String>();
    private ArrayList<String>  kegiatanAddedSakit = new ArrayList<String>();
    static ArrayList<String> kegiatansListSakit = new ArrayList<String>();
    String kegiatansSakitLainnya;
    StringBuffer buffer2;

    EditText etkegiatanSakitLainnya;
    RecyclerView rvKegiatanSakit;
    DatabaseHelper databaseHelper;
    TextView tvlistCheckedSakit;
    SakitAdapter sakitAdapter;

    public static AppCompatActivity sakit ;
    String jam_masuk, jam_pulang;
    String inisialsift, tipesift, masuksift, pulangsift, idsift, rbTanggal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_sakit);
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(getResources().getColor(R.color.putih));
        }

        Bundle intent = getIntent().getExtras();
        jam_masuk = intent.getString("jam_masuk");
        jam_pulang = intent.getString("jam_pulang");
        rbTanggal = intent.getString("tanggal_sift");
        inisialsift = intent.getString("inisialsift");
        idsift = intent.getString("idsift");
        tipesift = intent.getString("tipesift");
        masuksift = intent.getString("masuksift");
        pulangsift = intent.getString("pulangsift");

        sakit = this;

        listSakit.clear();
        kegiatansListSakit.clear();

        databaseHelper = new DatabaseHelper(this);
        kegiatanDatabase();

        rvKegiatanSakit = findViewById(R.id.rvKegiatanSakit);
        etkegiatanSakitLainnya = findViewById(R.id.etKegiatanSakitLainmya);
        tvlistCheckedSakit = findViewById(R.id.tvlistCheckedSakit);
        listSakit.addAll(getListData2());

        showRecyclerList();

        etkegiatanSakitLainnya.addTextChangedListener(new TextWatcher() {
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

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(uiOptions);


    }

    static ArrayList<Kegiatan> getListData2() {
        ArrayList<Kegiatan> list = new ArrayList<>();
        for (int position = 0; position < kegiatansListSakit.size(); position++) {
            Kegiatan kegiatans = new Kegiatan();
            kegiatans.setKegiatan(kegiatansListSakit.get(position));
            list.add(kegiatans);
        }
        return list;
    }

    public void kegiatanDatabase(){
        Cursor res = databaseHelper.getKegiatanIzin();
        while (res.moveToNext()){
            if (res.getString(1).equals("sk")){
                kegiatansListSakit.add(res.getString(2));
            }
        }
    }

    public void nextKegiatanSakit(View view){

        if (!etkegiatanSakitLainnya.getText().toString().isEmpty()){
            kegiatansSakitLainnya = etkegiatanSakitLainnya.getText().toString();
        }else{
            kegiatansSakitLainnya = "kosong";
        }

        if (!kegiatanCheckedSakit.isEmpty() || !etkegiatanSakitLainnya.getText().toString().isEmpty()){
            Intent intentTL = new Intent(SakitSiftActivity.this, IzinSakitSiftFinalActivity.class);
            intentTL.putExtra("kegiatans", kegiatanCheckedSakit);
            intentTL.putExtra("lainnya", kegiatansSakitLainnya);
            intentTL.putExtra("jam_masuk", jam_masuk);
            intentTL.putExtra("jam_pulang", jam_pulang);
            intentTL.putExtra("title", "Isi Data Kondisi Kesehatan");
            intentTL.putExtra("tanggal_sift", rbTanggal);
            intentTL.putExtra("idsift", idsift);
            intentTL.putExtra("inisialsift", inisialsift);
            intentTL.putExtra("tipesift", tipesift);
            intentTL.putExtra("masuksift", masuksift);
            intentTL.putExtra("pulangsift", pulangsift);
            startActivity(intentTL);
        }else {
            showMessage("Peringatan!", "Anda Harus Mengisi Kegiatan Yang Dilaksanakan.");
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
        rvKegiatanSakit.setLayoutManager(new LinearLayoutManager(this));
        sakitAdapter = new SakitAdapter(listSakit);
        rvKegiatanSakit.setAdapter(sakitAdapter);

        sakitAdapter.setOnItemClickCallback(new SakitAdapter.OnItemClickCallback() {
            @Override
            public void onItemClicked(Kegiatan data) {
                showSelectedKegiatan(data);
            }
        });
    }

    private void showSelectedKegiatan(Kegiatan kegiatan) {
        if (kegiatan.isChecked() == true ){

            kegiatanCheckedSakit.add(kegiatan.getKegiatan());

            buffer2 = new StringBuffer();
            for (int i = 0; i<kegiatanCheckedSakit.size()-1;i++){
                buffer2.append(kegiatanCheckedSakit.get(i)+", ");
            }
            buffer2.append(kegiatanCheckedSakit.get(kegiatanCheckedSakit.size()-1));

            tvlistCheckedSakit.setText(buffer2.toString());
            tvlistCheckedSakit.setVisibility(View.VISIBLE);

        }else{

            kegiatanCheckedSakit.remove(kegiatan.getKegiatan());

            StringBuffer buffer = new StringBuffer();
            if (kegiatanCheckedSakit.size() == 1){
                buffer.append(kegiatanCheckedSakit.get(kegiatanCheckedSakit.size()-1));
                tvlistCheckedSakit.setText(buffer.toString());
                tvlistCheckedSakit.setVisibility(View.VISIBLE);
            }else if(kegiatanCheckedSakit.isEmpty()){
                tvlistCheckedSakit.setVisibility(View.GONE);
            }
            else{
                for (int i = 0; i<kegiatanCheckedSakit.size()-1;i++){
                    buffer.append(kegiatanCheckedSakit.get(i)+", ");
                }
                buffer.append(kegiatanCheckedSakit.get(kegiatanCheckedSakit.size()-1)+", ");
                tvlistCheckedSakit.setText(buffer.toString());
                tvlistCheckedSakit.setVisibility(View.VISIBLE);
            }



        }

    }

    public void backSakit(View view){
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }


}
package go.pemkott.appsandroidmobiletebingtinggi.dinasluar.PerjalananDinas;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
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


public class SppdActivity extends AppCompatActivity {

    private static ArrayList<Kegiatan> listPd = new ArrayList<>();
    private ArrayList<String> kegiatanCheckedPd = new ArrayList<String>();
    private ArrayList<String>  kegiatanAddedPd = new ArrayList<String>();
    static ArrayList<String> kegiatansListPd = new ArrayList<String>();
    String kegiatansPdLainnya;
    StringBuffer buffer2;

    EditText etkegiatanPdLainnya;
    RecyclerView rvKegiatanPd;
    TextView tvlistCheckedPd;
    DatabaseHelper databaseHelper;
    SppdAdapter sppdAdapter;

    String jam_masuk, jam_pulang;
    public static AppCompatActivity pd ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View decorView = getWindow().getDecorView();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.biru_1));
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(uiOptions);

        setContentView(R.layout.activity_sppd);

        pd = this;

        Bundle intent = getIntent().getExtras();
        jam_masuk = intent.getString("jam_masuk");
        jam_pulang = intent.getString("jam_pulang");

        listPd.clear();
        kegiatansListPd.clear();

        databaseHelper = new DatabaseHelper(this);
        kegiatanDatabase();

        rvKegiatanPd = findViewById(R.id.rvKegiatanPd);
        etkegiatanPdLainnya = findViewById(R.id.etKegiatanPdLainmya);
        etkegiatanPdLainnya.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        tvlistCheckedPd = findViewById(R.id.tvlistCheckedPd);
        listPd.addAll(getListData2());

        showRecyclerList();

        etkegiatanPdLainnya.addTextChangedListener(new TextWatcher() {
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

    static ArrayList<Kegiatan> getListData2() {
        ArrayList<Kegiatan> list = new ArrayList<>();
        list.clear();
        for (int position = 0; position < kegiatansListPd.size(); position++) {
            Kegiatan kegiatans = new Kegiatan();
            kegiatans.setKegiatan(kegiatansListPd.get(position));
            list.add(kegiatans);
        }
        return list;
    }

    public void kegiatanDatabase(){
        Cursor res = databaseHelper.getKegiatanIzin();
        while (res.moveToNext()){
            if (res.getString(1).equals("pd")){
                kegiatansListPd.add(res.getString(2));
            }
        }
    }

    public void nextKegiatanPd(View view){
        if (!etkegiatanPdLainnya.getText().toString().isEmpty()){
            kegiatansPdLainnya = etkegiatanPdLainnya.getText().toString();

        }else{
            kegiatansPdLainnya = "kosong";
        }

        if (kegiatanCheckedPd.isEmpty() && kegiatansPdLainnya.equals("kosong")){
            showMessage("Peringatan!", "Anda Harus Mengisi Kegiatan Yang Dilaksanakan.");
        }else {

            Intent intentPd = new Intent(SppdActivity.this, PerjalananDinasFinalActivity.class);
            intentPd.putExtra("kegiatans", kegiatanCheckedPd);
            intentPd.putExtra("lainnya", kegiatansPdLainnya);
            intentPd.putExtra("jam_masuk", jam_masuk);
            intentPd.putExtra("jam_pulang", jam_pulang);
            intentPd.putExtra("title", "Isi Data Perjalanan Dinas");
            startActivity(intentPd);

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
        sppdAdapter = new SppdAdapter(SppdActivity.this, listPd);
        rvKegiatanPd.setAdapter(sppdAdapter);

        sppdAdapter.setOnItemClickCallback(new SppdAdapter.OnItemClickCallback() {
            @Override
            public void onItemClicked(Kegiatan data) {
                showSelectedKegiatan(data);
            }
        });
    }

    private void showSelectedKegiatan(Kegiatan kegiatan) {
        if (kegiatan.isChecked() == true ){
            kegiatanCheckedPd.add(kegiatan.getKegiatan());

            buffer2 = new StringBuffer();
            for (int i = 0; i<kegiatanCheckedPd.size()-1;i++){
                buffer2.append(kegiatanCheckedPd.get(i)+", ");
            }
            buffer2.append(kegiatanCheckedPd.get(kegiatanCheckedPd.size()-1));

            tvlistCheckedPd.setText(buffer2.toString().toUpperCase());
            tvlistCheckedPd.setVisibility(View.VISIBLE);
        }else{
            kegiatanCheckedPd.remove(kegiatan.getKegiatan());

            StringBuffer buffer = new StringBuffer();
            if (kegiatanCheckedPd.size() == 1){
                buffer.append(kegiatanCheckedPd.get(kegiatanCheckedPd.size()-1));
                tvlistCheckedPd.setText(buffer.toString().toUpperCase());
                tvlistCheckedPd.setVisibility(View.VISIBLE);
            }else if(kegiatanCheckedPd.isEmpty()){
                tvlistCheckedPd.setVisibility(View.GONE);
            }
            else{
                for (int i = 0; i<kegiatanCheckedPd.size()-1;i++){
                    buffer.append(kegiatanCheckedPd.get(i)+", ");
                }
                buffer.append(kegiatanCheckedPd.get(kegiatanCheckedPd.size()-1)+", ");
                tvlistCheckedPd.setText(buffer.toString().toUpperCase());
                tvlistCheckedPd.setVisibility(View.VISIBLE);
            }

        }

    }

    public void backPd(View view){
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
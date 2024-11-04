package go.pemkott.appsandroidmobiletebingtinggi.izin.KeperluanPribadi;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.database.DatabaseHelper;
import go.pemkott.appsandroidmobiletebingtinggi.model.Kegiatan;


public class KeperluanPribadiActivity extends AppCompatActivity {

    private static final ArrayList<Kegiatan> list = new ArrayList<>();
    private final ArrayList<String> kegiatanChecked = new ArrayList<String>();
    static ArrayList<String> kegiatansList = new ArrayList<String>();
    String kegiatansLainnya;
    StringBuffer buffer2;

    EditText etkegiatanKpLainnya;
    RecyclerView rvKegiatanKp;
    KpAdapter kpAdapter;
    DatabaseHelper databaseHelper;
    TextView tvlistCheckedKp;
    String jam_masuk, jam_pulang;

    ImageView ivNextKp;
    public static AppCompatActivity kp ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View decorView = getWindow().getDecorView();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(uiOptions);

        setContentView(R.layout.activity_keperluan_pribadi);

        Bundle intent = getIntent().getExtras();
        jam_masuk = intent.getString("jam_masuk");
        jam_pulang = intent.getString("jam_pulang");

        kp = this;

        list.clear();
        kegiatansList.clear();

        databaseHelper = new DatabaseHelper(this);
        kegiatanDatabase();

        rvKegiatanKp = findViewById(R.id.rvKegiatanKp);
        etkegiatanKpLainnya = findViewById(R.id.etKegiatanKpLainnya);
        tvlistCheckedKp = findViewById(R.id.tvlistCheckedKp);
        ivNextKp = findViewById(R.id.ivNextKp);
        list.addAll(getListData2());

        showRecyclerList();

    }


    public void backKp(View view){
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
            if (res.getString(1).equals("kp")){
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

    public void nextKegiatanKp(View view){
        if (!etkegiatanKpLainnya.getText().toString().isEmpty()){
            kegiatansLainnya = etkegiatanKpLainnya.getText().toString();
        }else{
            kegiatansLainnya = "kosong";
        }

        if (kegiatanChecked.isEmpty() && kegiatansLainnya.equals("kosong")){
            showMessage("Peringatan!", "Anda Harus Mengisi Kegiatan Yang Dilaksanakan.");
        }else {

            Intent intentTL = new Intent(KeperluanPribadiActivity.this, KeperluanPribadiFinalActivity.class);
            intentTL.putExtra("kegiatans", kegiatanChecked);
            intentTL.putExtra("lainnya", kegiatansLainnya);
            intentTL.putExtra("jam_masuk", jam_masuk);
            intentTL.putExtra("jam_pulang", jam_pulang);
            intentTL.putExtra("title", "Isi Data Keperluan Pribadi");
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
        rvKegiatanKp.setLayoutManager(new LinearLayoutManager(this));
        kpAdapter = new KpAdapter(list);
        rvKegiatanKp.setAdapter(kpAdapter);

        kpAdapter.setOnItemClickCallback(new KpAdapter.OnItemClickCallback() {
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

            tvlistCheckedKp.setText(buffer2.toString().toUpperCase());
            tvlistCheckedKp.setVisibility(View.VISIBLE);

        }else{

            kegiatanChecked.remove(kegiatan.getKegiatan());

            StringBuffer buffer = new StringBuffer();
            if (kegiatanChecked.size() == 1){
                buffer.append(kegiatanChecked.get(kegiatanChecked.size()-1));
                tvlistCheckedKp.setText(buffer.toString().toUpperCase());
                tvlistCheckedKp.setVisibility(View.VISIBLE);
            }else if(kegiatanChecked.isEmpty()){
                tvlistCheckedKp.setVisibility(View.GONE);
            }

            else{
                for (int i = 0; i<kegiatanChecked.size()-1;i++){
                    buffer.append(kegiatanChecked.get(i)+", ");
                }
                buffer.append(kegiatanChecked.get(kegiatanChecked.size()-1)+", ");
                tvlistCheckedKp.setText(buffer.toString().toUpperCase());
                tvlistCheckedKp.setVisibility(View.VISIBLE);
            }
        }

    }


}
package go.pemkott.appsandroidmobiletebingtinggi.izin.Cuti;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
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
import java.util.List;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.database.DatabaseHelper;
import go.pemkott.appsandroidmobiletebingtinggi.model.Kegiatan;

public class CutiActivity extends AppCompatActivity {

    public static AppCompatActivity cuti ;
    DatabaseHelper databaseHelper;

    private ArrayList<String> kegiatanCheckedCuti = new ArrayList<String>();
    private static List<Kegiatan> listCuti = new ArrayList<>();
    static List<String> kegiatansListCuti = new ArrayList<String>();

    CutiAdapter cutiAdapter;

    EditText etkegiatanCutiLainnya;
    RecyclerView rvKegiatanCuti;
    TextView tvlistCheckedCuti;

    StringBuffer buffer;

    String jam_masuk, jam_pulang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View decorView = getWindow().getDecorView();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.biru_1));
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(uiOptions);

        setContentView(R.layout.activity_cuti);

        cuti = this;

        Bundle intent = getIntent().getExtras();
        jam_masuk = intent.getString("jam_masuk");
        jam_pulang = intent.getString("jam_pulang");

        rvKegiatanCuti = findViewById(R.id.rvKegiatanCuti);
        etkegiatanCutiLainnya = findViewById(R.id.etKegiatanCutiLainnya);
        etkegiatanCutiLainnya.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        tvlistCheckedCuti = findViewById(R.id.tvlistCheckedCuti);

        listCuti.clear();
        kegiatansListCuti.clear();
        databaseHelper = new DatabaseHelper(this);
        kegiatanDatabase();

        listCuti.addAll(getListData());
        showRecyclerList();


    }

    public void kegiatanDatabase(){
        Cursor res = databaseHelper.getKegiatanIzin();
        while (res.moveToNext()){
            if (res.getString(1).equals("cuti")){
                kegiatansListCuti.add(res.getString(2));
            }
        }
    }

    static ArrayList<Kegiatan> getListData() {
        ArrayList<Kegiatan> list = new ArrayList<>();
        list.clear();
        for (int position = 0; position < kegiatansListCuti.size(); position++) {
            Kegiatan kegiatans = new Kegiatan();
            kegiatans.setKegiatan(kegiatansListCuti.get(position));
            list.add(kegiatans);
        }
        return list;
    }

    private void showRecyclerList(){
        rvKegiatanCuti.setLayoutManager(new LinearLayoutManager(this));
        cutiAdapter = new CutiAdapter(listCuti);
        rvKegiatanCuti.setAdapter(cutiAdapter);

        cutiAdapter.setOnItemClickCallback(new CutiAdapter.OnItemClickCallback() {
            @Override
            public void onItemClicked(Kegiatan data) {
                showSelectedKegiatan(data);
            }
        });
    }

    private void showSelectedKegiatan(Kegiatan kegiatan) {
        if (kegiatan.isChecked() == true ){
            kegiatanCheckedCuti.add(kegiatan.getKegiatan());

            buffer = new StringBuffer();
            for (int i = 0; i<kegiatanCheckedCuti.size()-1;i++){
                buffer.append(kegiatanCheckedCuti.get(i)+", ");
            }
            buffer.append(kegiatanCheckedCuti.get(kegiatanCheckedCuti.size()-1));

            tvlistCheckedCuti.setText(buffer.toString().toUpperCase());
            tvlistCheckedCuti.setVisibility(View.VISIBLE);

        }else{
            kegiatanCheckedCuti.remove(kegiatan.getKegiatan());

            StringBuffer buffer = new StringBuffer();
            if (kegiatanCheckedCuti.size() == 1){
                buffer.append(kegiatanCheckedCuti.get(kegiatanCheckedCuti.size()-1));
                tvlistCheckedCuti.setText(buffer.toString().toUpperCase());
                tvlistCheckedCuti.setVisibility(View.VISIBLE);
            }else if(kegiatanCheckedCuti.isEmpty()){
                tvlistCheckedCuti.setVisibility(View.GONE);
            }
            else{
                for (int i = 0; i<kegiatanCheckedCuti.size()-1;i++){
                    buffer.append(kegiatanCheckedCuti.get(i)+", ");
                }
                buffer.append(kegiatanCheckedCuti.get(kegiatanCheckedCuti.size()-1)+", ");
                tvlistCheckedCuti.setText(buffer.toString().toUpperCase());
                tvlistCheckedCuti.setVisibility(View.VISIBLE);
            }
        }

    }

    String kegiatansCutiLainnya;

    public void nextKegiatanCuti(View view){
        if (!etkegiatanCutiLainnya.getText().toString().isEmpty()){
            kegiatansCutiLainnya = etkegiatanCutiLainnya.getText().toString();
        }else{
            kegiatansCutiLainnya = "kosong";
        }

        if (kegiatanCheckedCuti.isEmpty() && kegiatansCutiLainnya.equals("kosong")){
            showMessage("Peringatan!", "Anda Harus Mengisi Kegiatan Yang Dilaksanakan.");
        }else {

            Intent intentTL = new Intent(CutiActivity.this, IzinCutiFinalActivity.class);
            intentTL.putExtra("kegiatans", kegiatanCheckedCuti);
            intentTL.putExtra("lainnya", kegiatansCutiLainnya);
            intentTL.putExtra("jam_masuk", jam_masuk);
            intentTL.putExtra("jam_pulang", jam_pulang);
            intentTL.putExtra("title", "Isi Data Cuti");
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




    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
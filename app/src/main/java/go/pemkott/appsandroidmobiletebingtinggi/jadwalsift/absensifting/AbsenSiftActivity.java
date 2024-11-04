package go.pemkott.appsandroidmobiletebingtinggi.jadwalsift.absensifting;

import static android.content.ContentValues.TAG;
import static go.pemkott.appsandroidmobiletebingtinggi.geolocation.model.LocationHelper.defaultLocation;
import static go.pemkott.appsandroidmobiletebingtinggi.jadwalsift.JadwalSiftActivity.jadwalSiftActivity;
import static go.pemkott.appsandroidmobiletebingtinggi.konstanta.TimeFormat.SIMPLE_FORMAT_JAM;
import static go.pemkott.appsandroidmobiletebingtinggi.konstanta.TimeFormat.SIMPLE_FORMAT_JAM_TAGING;
import static go.pemkott.appsandroidmobiletebingtinggi.konstanta.TimeFormat.SIMPLE_FORMAT_TANGGAL;
import static go.pemkott.appsandroidmobiletebingtinggi.konstanta.TimeFormat.localeID;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentContainerView;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.api.ResponsePOJO;
import go.pemkott.appsandroidmobiletebingtinggi.api.RetroClient;
import go.pemkott.appsandroidmobiletebingtinggi.database.DatabaseHelper;
import go.pemkott.appsandroidmobiletebingtinggi.dialogview.DialogView;
import go.pemkott.appsandroidmobiletebingtinggi.konstanta.AmbilFoto;
import go.pemkott.appsandroidmobiletebingtinggi.konstanta.Lokasi;
import go.pemkott.appsandroidmobiletebingtinggi.utils.NetworkUtils;
import go.pemkott.appsandroidmobiletebingtinggi.viewmodel.LocationViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AbsenSiftActivity extends AppCompatActivity implements OnMapReadyCallback {

    //    private TextView locationText;
    private static final int REQUEST_CHECK_SETTINGS = 100;
    private Context mContext;
    private LocationViewModel locationViewModel;
    private Location locationObj;
    private GoogleMap map;
    private static final String KEY_LOCATION = "location";

    double latGMap = 0, lngGMap = 0;
    Lokasi lokasi = new Lokasi();


    DialogView dialogView = new DialogView(AbsenSiftActivity.this);
    File imageFile;

    private String currentPhotoPath;
    private String sEmployId;
    private String rbTanggal;
    private String rbJam;
    private String rbLat;
    private String rbLng;
    private String rbKet;
    private String batasWaktu;
    private String rbFakeGPS ="0";
    DatabaseHelper databaseHelper;
    ShapeableImageView ivTaging, iconFotoTaging;
    LinearLayout llUpload, llAmbilFotoSiftTaging;
    ProgressDialog progressDialog;
    Date dateBatasWaktu;
    String jamTaging, eKelompok, eJabatan, timetableid;
    String tanggal;
    String diff, latOffice, lngOffice,eOPD;
    String jam_masuk, jam_pulang;

    Calendar cal = Calendar.getInstance();

    RadioGroup rgKehadiran;
    RadioButton radioSelectedKehadiran;
    int selected;

    SimpleDateFormat hari;
    static ArrayList<String> latList = new ArrayList<>();
    static ArrayList<String> lngList = new ArrayList<>();

    static ArrayList<String> latListExc = new ArrayList<>();
    static ArrayList<String> lngListExc = new ArrayList<>();
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    double totalJarak;
    Date jamMasukDate, jamPulangDate, tagingTime;
    int mins;
    int minspulang;

    //Buttom Sheet
    CardView upsheet;
    private BottomSheetBehavior sheetBehavior;
    //Buttom Sheet

    private boolean mockLocationsEnabled;
    int mock_location = 0;
    String inisialsift, tipesift, masuksift, pulangsift, idsift;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;

    FragmentContainerView fragmentContainerView;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View decorView = getWindow().getDecorView();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.biru_1));
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(uiOptions);

        setContentView(R.layout.activity_absen_sift);
        mContext = AbsenSiftActivity.this;

        databaseHelper = new DatabaseHelper(mContext);
        databases();
        ambilFoto();
//Google Maps
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.putih));


        if (savedInstanceState != null) {
            locationObj = savedInstanceState.getParcelable(KEY_LOCATION);
        }

        setupViews();
        setupViewModel();
        checkLocationPermission();

//Google Maps
        Bundle intent = getIntent().getExtras();
        jam_masuk = intent.getString("jam_masuk");
        jam_pulang = intent.getString("jam_pulang");
        rbTanggal = intent.getString("tanggal_sift");
        inisialsift = intent.getString("inisialsift");
        idsift = intent.getString("idsift");
        tipesift = intent.getString("tipesift");
        masuksift = intent.getString("masuksift");
        pulangsift = intent.getString("pulangsift");

        ivTaging = findViewById(R.id.ivTagingAbsen);
        llAmbilFotoSiftTaging = findViewById(R.id.llAmbilFotoSiftTaging);
        iconFotoTaging = findViewById(R.id.iconFotoTagingKehadiran);
        llUpload = findViewById(R.id.llUploadkehadiran);
        rgKehadiran = findViewById(R.id.rgKehadiran);
        TextView title_content = findViewById(R.id.title_content);
        fragmentContainerView = findViewById(R.id.map);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(600);
        locationRequest.setFastestInterval(600);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        setRoundedBackground(fragmentContainerView);

        title_content.setText("KEHADIRAN");
        llAmbilFotoSiftTaging.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ambilFoto();
            }
        });

        setUpReferences();
        setOnClickListener();

        llUpload.setOnClickListener(view -> {
            requestPermission();
            if (mock_location == 1){
                dialogView.viewNotifKosong(AbsenSiftActivity.this, "Anda terdeteksi menggunakan Fake GPS.", "Jika ditemukan berulang kali, akun anda akan terblokir otomatis dan tercatat Alpa.");
            }else {
                uploadImages();
            }
        });



        if (Build.VERSION.SDK_INT < 18 &&
                !Settings.Secure.getString(this.getContentResolver(), Settings
                        .Secure.ALLOW_MOCK_LOCATION).equals("0")) {
            mockLocationsEnabled = true;
        } else{
            mockLocationsEnabled = false;
        }

        sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private void setRoundedBackground(FragmentContainerView view) {
        // Ganti warna dan radius sesuai kebutuhan Anda
        int backgroundColor = getResources().getColor(R.color.biru_1);
        float radius = getResources().getDimension(R.dimen.radius);

        view.setBackgroundResource(R.drawable.backgoundbase);
        view.setClipToOutline(true);
        view.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radius);
            }
        });
    }

    public void databases(){

        Cursor tUser = databaseHelper.getAllData22();
        while (tUser.moveToNext()){
            sEmployId = tUser.getString(1);
        }

        hari = new SimpleDateFormat("EEE", localeID);
        tanggal = hari.format(new Date());




        Cursor employe = databaseHelper.getDataEmployee(sEmployId);

        while (employe.moveToNext()){
            eOPD = employe.getString(4);
            eKelompok = employe.getString(9);
            eJabatan = employe.getString(11);
            latOffice = employe.getString(15);
            lngOffice = employe.getString(16);
            batasWaktu = employe.getString(18);

        }

        latList.clear();
        lngList.clear();

        Cursor koordinat = databaseHelper.getDataKoordinat(eOPD);
        if(koordinat.getCount() == 0){
            return;
        }

        while (koordinat.moveToNext()){
            latList.add(koordinat.getString(3));
            lngList.add(koordinat.getString(4));
        }

        latListExc.clear();
        lngListExc.clear();

        Cursor koordinatExc = databaseHelper.getDataKoordinatEmp(sEmployId);
        if(koordinatExc.getCount() == 0){
            return;
        }

        while (koordinatExc.moveToNext()){
            latListExc.add(koordinatExc.getString(3));
            lngListExc.add(koordinatExc.getString(4));
        }

        defaultLocation = new LatLng(Double.parseDouble( latList.get(0)), Double.parseDouble(lngList.get(0)));
    }

    // this is all you need to grant your application external storage permision
    private void requestPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(AbsenSiftActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_CODE_ASK_PERMISSIONS);
        }
    }

    private void ambilFoto(){

        String filename = "photo";
        File storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        try {
            imageFile = File.createTempFile(filename, ".png", storageDirectory);
            currentPhotoPath = imageFile.getAbsolutePath();

            Uri imageUri = FileProvider.getUriForFile(AbsenSiftActivity.this, "go.pemkott.appsandroidmobiletebingtinggi.fileprovider", imageFile);

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(intent, 1);

        } catch (IOException e) {
            finish();
        }

    }

    public void hitungjarak(){

        double latitudeSaya;
        double longitudeSaya;
        if (NetworkUtils.isConnectedMobile(AbsenSiftActivity.this) || NetworkUtils.isConnectedWifi(AbsenSiftActivity.this)){
            if(NetworkUtils.isConnected(AbsenSiftActivity.this)){

                latitudeSaya = latGMap;
                longitudeSaya = lngGMap;

                rbLat = String.valueOf(latGMap);
                rbLng = String.valueOf(lngGMap);


                SimpleDateFormat today = new SimpleDateFormat("EEE", localeID);
                String hariini = today.format(new Date());

                String lat, lng;

//            Menghitung Jarak CallTaker
                if (eKelompok.equals("ct")){

                    int totalLokasi = 0;
                    double jarak = 0;

                    for (int i = 0 ; i  < latList.size(); i++){

                        lat = latList.get(i);
                        lng = lngList.get(i);
                        double latitudeTujuan = Double.parseDouble(lat);
                        double longitudeTujuan = Double.parseDouble(lng);
                        jarak = getDistance(latitudeTujuan, longitudeTujuan, latitudeSaya, longitudeSaya);

                        if ((int)jarak <= 150){
                            break;
                        }else{

                            totalLokasi = totalLokasi + 1;
                        }
                    }

                    if (totalLokasi ==  latList.size()){

                        double jarakExc = 0;
                        int totalJarakExc = 0;

                        if (latListExc.size() <=0 ){
                            jarakExc = 151;
                        }else{

                            for (int j = 0; j< latListExc.size(); j++){

                                jarakExc = getDistance(Double.parseDouble(latListExc.get(j)), Double.parseDouble(lngListExc.get(j)), latitudeSaya, longitudeSaya);
                                if ((int) jarakExc <= 200) {
                                    break;
                                } else {
                                    totalJarakExc = totalJarakExc + 1;
                                }

                            }

                        }

                        if (totalJarakExc == latListExc.size()){
                            totalJarak = 151;
                        }else{
                            totalJarak = jarakExc;
                        }
                    }else{
                        totalJarak = jarak;
                    }

                }

//            Menghitung Jarak Pegawai
                else{
//                Menghitung Jarak Pada Hari Senin
                    if (hariini.equals("Sen") ||  hariini.equals("Mon")){
                        int totalLokasi = 0;
                        double jarak = 0;

                        for (int i = 0 ; i  < latList.size(); i++){
                            lat = latList.get(i);
                            lng = lngList.get(i);
                            double latitudeTujuan = Double.parseDouble(lat);
                            double longitudeTujuan = Double.parseDouble(lng);
                            jarak = getDistance(latitudeTujuan, longitudeTujuan, latitudeSaya, longitudeSaya);

                            if (jarak <= 200){
                                break;
                            }else{
                                totalLokasi = totalLokasi + 1;
                            }

                        }

                        if (totalLokasi ==  latList.size()){

                            double jarakExc = 0;
                            int totalJarakExc = 0;

                            if (latListExc.size() <=0){
                                jarakExc = 151;
                            }else{


                                for (int j = 0; j< latListExc.size(); j++){

                                    jarakExc = getDistance(Double.parseDouble(latListExc.get(j)), Double.parseDouble(lngListExc.get(j)), latitudeSaya, longitudeSaya);
                                    if ((int) jarakExc <= 200) {
                                        break;
                                    } else {
                                        totalJarakExc = totalJarakExc + 1;
                                    }

                                }
                            }


                            if (totalJarakExc == latListExc.size()){
                                totalJarak = 151;
                            }else{
                                totalJarak = jarakExc;
                            }
                        }else{
                            totalJarak = jarak;
                        }

                    }

//                Menghitung Jarak Pada Hari Lainnya
                    else{

                        double latitudeTujuan = Double.parseDouble(latList.get(0));
                        double longitudeTujuan = Double.parseDouble(lngList.get(0));

                        double jarak = getDistance(latitudeTujuan, longitudeTujuan, latitudeSaya, longitudeSaya);
                        if (jarak <= 150){

                            totalJarak = jarak;

                        }
                        else{

                            double jarakExc = 0;
                            int totalJarakExc = 0;

                            if (latListExc.size() <= 0 ) {
                                jarakExc = 151;
                            }else{

                                for (int j = 0; j< latListExc.size(); j++){

                                    jarakExc = getDistance(Double.parseDouble(latListExc.get(j)), Double.parseDouble(lngListExc.get(j)), latitudeSaya, longitudeSaya);
                                    if ((int) jarakExc <= 150) {
                                        break;
                                    } else {
                                        totalJarakExc = totalJarakExc + 1;
                                    }

                                }
                            }

                            if (totalJarakExc == latListExc.size()){
                                totalJarak = 151;
                            }else{
                                totalJarak = jarakExc;
                            }

                        }
                    }

                }

            }
            else{
                dialogView.viewNotifKosong(AbsenSiftActivity.this, "Pastikan anda telah terhubung ke internet.", "");

            }
        }else{
            dialogView.viewNotifKosong(AbsenSiftActivity.this, "Pastikan anda telah terhubung ke internet.", "");
        }


    }

    private Double getDistance(Double latitudeTujuan, Double longitudeTujuan, Double latitudeUser, Double longitudeUser) {
        /* VARIABLE */
        double pi = 3.14159265358979;

        Double R = 6371e3;

        double latRad1 = latitudeTujuan * (pi / 180);
        double latRad2 = latitudeUser * (pi / 180);
        double deltaLatRad = (latitudeUser - latitudeTujuan) * (pi / 180);
        double deltaLonRad = (longitudeUser - longitudeTujuan) * (pi / 180);

        /* RUMUS HAVERSINE */
        double a = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2) + Math.cos(latRad1) * Math.cos(latRad2) * Math.sin(deltaLonRad / 2) * Math.sin(deltaLonRad / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    Bitmap viewFotoBitmap;
    String encodedImage = null;
    AmbilFoto ambilFoto = new AmbilFoto(AbsenSiftActivity.this);
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_CANCELED){
            if (requestCode == 1 && resultCode == RESULT_OK) {

                File file = new File(currentPhotoPath);
                Bitmap selectedBitmap = ambilFoto.fileBitmap(file);
                Bitmap rotationBitmapSurat = Bitmap.createBitmap(selectedBitmap, 0,0, selectedBitmap.getWidth(), selectedBitmap.getHeight(), AmbilFoto.exifInterface(currentPhotoPath), true);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                rotationBitmapSurat.compress(Bitmap.CompressFormat.PNG,75, byteArrayOutputStream);
                byte[] imageInByte = byteArrayOutputStream.toByteArray();
                encodedImage =  Base64.encodeToString(imageInByte,Base64.DEFAULT);
                iconFotoTaging.setVisibility(View.GONE);
                ivTaging.setVisibility(View.VISIBLE);

                byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                ivTaging.setImageBitmap(decodedByte);
                viewFotoBitmap = decodedByte;

                if (sheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }

            startLocationUpdates();
        }else {
            finish();
        }

    }

    public void uploadImages(){

        if(encodedImage == null || encodedImage.isEmpty() ){
            dialogView.viewNotifKosong(AbsenSiftActivity.this, "Harap melampirkan foto taging anda.", "");
        }else{
            selected = rgKehadiran.getCheckedRadioButtonId();
            radioSelectedKehadiran = findViewById(selected);


            periksaWaktu();
            hitungjarak();

            if (eJabatan.equals("2")){
                totalJarak = 1;
            }

            if (totalJarak > 150){
                dialogView.viewNotifKosong(AbsenSiftActivity.this, "Andah harus berada dilingkungan kantor untuk melakukan absensi.", "");
            }
            else{
                //Malam
                if (tipesift.equals("malam")) {
                    String rbPosisi = null;
                    String rbValid = "2", ketKehadiran;
                    String eselon = "0";

                    if (radioSelectedKehadiran.getText().toString().equals("Masuk")) {
                        ketKehadiran = "masuk";
                        rbPosisi = "masuk";

                        String tanggal = SIMPLE_FORMAT_TANGGAL.format(new Date());
                        Date hariini = null;
                        try {
                            hariini = SIMPLE_FORMAT_TANGGAL.parse(tanggal);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(hariini);
                        calendar.add(Calendar.DAY_OF_YEAR, -1);
                        Date newDate = calendar.getTime();
                        String infoJadwalhariini = SIMPLE_FORMAT_TANGGAL.format(newDate);
                        String jamSekarangString = SIMPLE_FORMAT_JAM_TAGING.format(new Date());
                        Date jamSekarang = null, jamAbsenMalam = null, batasWaktuJamMalam = null;
                        try {
                            jamSekarang = SIMPLE_FORMAT_JAM_TAGING.parse(jamSekarangString);
                            batasWaktuJamMalam = SIMPLE_FORMAT_JAM_TAGING.parse("12:00");
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        if (tanggal.equals(rbTanggal)) {
                            if (jam_masuk == null) {
                                if (tagingTime.getTime() < dateBatasWaktu.getTime()) {
                                    dialogView.viewNotifKosong(AbsenSiftActivity.this, "Anda hanya dapat mengisi absen masuk, " + batasWaktu + " menit sebelum Jam Masuk", "");
                                } else {
                                    kirimdata(ketKehadiran, eselon, sEmployId, timetableid, rbTanggal, rbJam, rbPosisi, "hadir", rbLat, rbLng, rbKet, mins, masuksift, rbValid);
                                }
                            } else {
                                dialogView.viewNotifKosong(AbsenSiftActivity.this, "Anda sudah mengisi absensi masuk.", "");
                            }

                        } else {
                            if (tagingTime.getTime() > jamPulangDate.getTime()) {
                                dialogView.viewNotifKosong(AbsenSiftActivity.this, "Anda hanya dapat mengisi absen masuk, sebelum jam pulang malam.", "");
                            } else {
                                kirimdata(ketKehadiran, eselon, sEmployId, timetableid, rbTanggal, rbJam, rbPosisi, "hadir", rbLat, rbLng, rbKet, mins, masuksift, rbValid);
                            }

                        }

                    }
                    else {
                        rbPosisi = "pulang";
                        String tanggal = SIMPLE_FORMAT_TANGGAL.format(new Date());

                        if (jam_pulang == null) {
//                        String jamSekarangString = SIMPLE_FORMAT_JAM_TAGING.format(new Date());

                            Date hariini = null;
                            try {
                                hariini = SIMPLE_FORMAT_TANGGAL.parse(rbTanggal);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(hariini);
                            calendar.add(Calendar.DAY_OF_YEAR, 1);
                            Date newDate = calendar.getTime();
                            String tanggalAbsenSiftMalam = SIMPLE_FORMAT_TANGGAL.format(newDate);

                            if (tanggal.equals(tanggalAbsenSiftMalam)) {
                                if (tanggalAbsenSiftMalam.equals(tanggal)) {
                                    if (tagingTime.getTime() > jamPulangDate.getTime()) {
                                        if (jam_masuk == null) {
                                            kirimdata("masukpulang", eselon, sEmployId, timetableid, rbTanggal, rbJam, rbPosisi, "hadir", rbLat, rbLng, rbKet, 0, pulangsift, rbValid);
                                        } else {
                                            kirimdata("pulang", eselon, sEmployId, timetableid, rbTanggal, rbJam, rbPosisi, "hadir", rbLat, rbLng, rbKet, 0, pulangsift, rbValid);
                                        }
                                    }else {

                                        dialogView.viewNotifKosong(AbsenSiftActivity.this, "Anda belum dapat mengisi absensi pulang,", "silahkan lanjutkan kembali aktivitas kantor anda.");

                                    }
                                }
                            }
                            else {
                                if (jam_masuk == null){
                                    dialogView.viewNotifKosong(AbsenSiftActivity.this, "Anda belum dapat melakukan absensi pulang,", "silahkan lakukan absensi masuk terlebih dahulu.");
                                }else{
                                    dialogView.viewNotifKosong(AbsenSiftActivity.this, "Anda belum dapat mengisi absensi pulang,", "silahkan lanjutkan kembali aktivitas kantor anda.");
                                }
                            }

                        }
                        else {
                            if (tanggal.equals(rbTanggal)) {
                                dialogView.viewNotifKosong(AbsenSiftActivity.this, "Anda belum dapat mengisi absensi pulang,", "silahkan lanjutkan kembali aktivitas kantor anda.");
                            }
                        }
                    }
                }//Malam
                //Pagi
                else {
                    String rbPosisi = null;
                    String rbValid = "2", ketKehadiran = null;

                    String eselon = "0";
                    if (radioSelectedKehadiran.getText().toString().equals("Masuk")) {
                        rbPosisi = "masuk";
                        ketKehadiran = "masuk";

                        if (jam_masuk == null) {
                            if (tagingTime.getTime() < dateBatasWaktu.getTime()) {
                                dialogView.viewNotifKosong(AbsenSiftActivity.this, "Anda hanya dapat mengisi absen masuk, " + batasWaktu + " menit sebelum Jam Masuk", "");
                            }
                            else {
                                if (tagingTime.getTime() > jamPulangDate.getTime()) {
                                    dialogView.viewNotifKosong(AbsenSiftActivity.this, "Anda tidak dapat melakukan absensi masuk pada jam pulang kerja.", "");
                                } else {
                                    kirimdata(ketKehadiran, eselon, sEmployId, timetableid, rbTanggal, rbJam, rbPosisi, "hadir", rbLat, rbLng, rbKet, mins, masuksift, rbValid);
                                }
                            }
                        }
                        else {
                            dialogView.viewNotifKosong(AbsenSiftActivity.this, "Anda sudah mengisi absensi masuk.", "");
                        }
                    }
                    else {

                        rbPosisi = "pulang";

                        if (jam_pulang == null) {

                            if (tagingTime.getTime() < jamPulangDate.getTime()) {
                                dialogView.viewNotifKosong(AbsenSiftActivity.this, "Anda belum dapat mengisi absensi pulang,", "silahkan lanjutkan kembali aktivitas kantor anda ya.");
                            } else {
                                if (jam_masuk == null) {
                                    kirimdata("masukpulang", eselon, sEmployId, timetableid, rbTanggal, rbJam, rbPosisi, "hadir", rbLat, rbLng, rbKet, 0, pulangsift, rbValid);
                                } else {
                                    kirimdata("pulang", eselon, sEmployId, timetableid, rbTanggal, rbJam, rbPosisi, "hadir", rbLat, rbLng, rbKet, 0, pulangsift, rbValid);
                                }
                            }

                        } else {
                            dialogView.viewNotifKosong(AbsenSiftActivity.this, "Anda sudah mengisi absensi pulang.", "");
                        }
                    }


                }
                //Pagi
            }
        }


    }

    public void kirimdata(String absensi, String eselon, String idpegawai, String timetableid, String tanggal, String jam, String posisi, String status,  String lat, String lng, String ket, int terlambat, String jampegawai, String validasi ){

        progressDialog = new ProgressDialog(AbsenSiftActivity.this, R.style.AppCompatAlertDialogStyle);
        progressDialog.setMessage("Sedang memproses...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        Call<ResponsePOJO> call = RetroClient.getInstance().getApi().uploadAbsenKehadiranSift(
                encodedImage,
                absensi,
                eselon,
                idpegawai,
                timetableid,
                tanggal,
                jam,
                posisi,
                status,
                lat,
                lng,
                eOPD,
                jampegawai,
                validasi,
                rbFakeGPS,
                batasWaktu,
                masuksift,
                pulangsift,
                inisialsift,
                tipesift,
                idsift,
                ket,
                terlambat
        );

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponsePOJO> call, @NonNull Response<ResponsePOJO> response) {

                if (!response.isSuccessful()) {

                    progressDialog.dismiss();
                    dialogView.viewNotifKosong(AbsenSiftActivity.this, "Gagal mengisi absensi,", "silahkan coba kembali.");

                }

                if (response.body().isStatus()) {
                    progressDialog.dismiss();

                    viewSukses(AbsenSiftActivity.this, response.body().getRemarks(), "");
                } else {
                    progressDialog.dismiss();

                    dialogView.viewNotifKosong(AbsenSiftActivity.this, response.body().getRemarks(), "");
                }

            }

            @Override
            public void onFailure(@NonNull Call<ResponsePOJO> call, @NonNull Throwable t) {
                progressDialog.dismiss();
//                dialogView.viewNotifKosong(AbsenSiftActivity.this, t.getMessage(), "silahkan coba kembali.");
                dialogView.pesanError(AbsenSiftActivity.this);
            }
        });
    }

    public void viewJadwal(){
        Dialog viewJadwal = new Dialog(AbsenSiftActivity.this, R.style.DialogStyle);
        viewJadwal.show();
    }


    public void viewSukses(Context context, String info1, String info2){

        Dialog dialogSukes = new Dialog(context, R.style.DialogStyle);
        dialogSukes.setContentView(R.layout.view_sukses);
        dialogSukes.setCancelable(false);

        TextView tvTutupDialog = dialogSukes.findViewById(R.id.tvTutupDialog);
        TextView kembaliDialog = dialogSukes.findViewById(R.id.kembaliDialog);
        TextView tvInfo1 = dialogSukes.findViewById(R.id.tvInfo1);
        TextView tvInfo2 = dialogSukes.findViewById(R.id.tvInfo2);

        tvInfo1.setText(info1);
        tvInfo2.setText(info2);

        kembaliDialog.setOnClickListener(v -> {
            stopLocationUpdates();

            dialogSukes.dismiss();
            jadwalSiftActivity.finish();
            finish();
        });

        tvTutupDialog.setOnClickListener(v -> {
            stopLocationUpdates();
            dialogSukes.dismiss();
            jadwalSiftActivity.finish();
            finish();
        });

        handlerProgressDialog2();
        dialogSukes.show();

    }


    public void handlerProgressDialog(){
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            //your code here
            progressDialog.dismiss();
        }, 1500);
    }
    public void handlerProgressDialog2(){
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            //your code here
            progressDialog.dismiss();
            finish();
        }, 1500);
    }

    @SuppressLint("ResourceAsColor")
    public void periksaWaktu(){

        try {
            jamMasukDate = SIMPLE_FORMAT_JAM.parse(masuksift);
            jamPulangDate = SIMPLE_FORMAT_JAM.parse(pulangsift);

            jamTaging = SIMPLE_FORMAT_JAM_TAGING.format(new Date());

            SimpleDateFormat df = new SimpleDateFormat("HH:mm", localeID);
            Date d = df.parse(masuksift);
            cal.setTime(d);
            cal.add(Calendar.MINUTE, -(Integer.parseInt(batasWaktu)));
            String newTime = df.format(cal.getTime());

            tagingTime = SIMPLE_FORMAT_JAM_TAGING.parse(jamTaging);
            dateBatasWaktu = SIMPLE_FORMAT_JAM_TAGING.parse(newTime);

            long millis = tagingTime.getTime() - jamMasukDate.getTime();
            int hours = (int) (millis / (1000 * 60 * 60));

            mins = (int) ((millis / (1000 * 60)) % 60) + (hours * 60);
            if (mins <= 0){
                mins = 0;
            }

            long millispulang = jamPulangDate.getTime() - tagingTime.getTime();
            int hourspulang = (int) (millispulang / (1000 * 60 * 60));
            minspulang = (int) ((millispulang / (1000 * 60)) % 60) + (hourspulang * 60);

            diff = hours + " jam :" + mins+" menit";

            rbJam = SIMPLE_FORMAT_JAM.format(new Date());
            if (tagingTime.getTime() <= jamMasukDate.getTime()) {

                rbKet = "sesuai waktu";
                mins = 0;
            }else if(tagingTime.getTime() > jamMasukDate.getTime()){

                rbKet = "terlambat";
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //    Maps
    private void setupViewModel() {
        locationViewModel = ViewModelProviders.of(this).get(LocationViewModel.class);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void checkLocationPermission() {
        int hasWriteStoragePermission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            hasWriteStoragePermission = getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            if (hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CHECK_SETTINGS);
                return;
            }
        } else {
        }
    }


    public void backFinalSiftActivity(View view){
        stopLocationUpdates();
        finish();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stopLocationUpdates();
        finish();
    }

    private void setupViews() {

        //Maps
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }



    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    ArrayList<Location> locationArrayList = new ArrayList<>();
    private void plotMarkers(Location locationObj) {

        if(map != null){

            map.clear();
            map.addMarker(new MarkerOptions().position(new LatLng(locationObj.getLatitude(), locationObj.getLongitude())).icon(bitmapDescriptorFromVector(this, R.drawable.asn_lk)).title(lokasi.getAddress(AbsenSiftActivity.this, locationObj.getLatitude(), locationObj.getLongitude())));
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(locationObj.getLatitude(), locationObj.getLongitude()), 19));
            map.getUiSettings().setMyLocationButtonEnabled(true);
            latGMap = locationObj.getLatitude();
            lngGMap = locationObj.getLongitude();


            locationArrayList.add(locationObj);
            Log.d("Location list", "plotMarkers: "+locationArrayList.size());

            //Draw Line
            LatLng singleLatLong = null;
            ArrayList<LatLng> pnts = new ArrayList<LatLng>();
            if(locationArrayList != null) {
                for(int i = 0 ; i < locationArrayList.size(); i++) {
                    double routePoint1Lat = locationArrayList.get(i).getLatitude();
                    double routePoint2Long = locationArrayList.get(i).getLongitude();
                    singleLatLong = new LatLng(routePoint1Lat,
                            routePoint2Long);
                    pnts.add(singleLatLong);

                    map.addPolyline(new PolylineOptions().
                            addAll(pnts)
                            .width(8)
                            .color(R.color.biru_1)
                            .zIndex(30));
                }
            }

        }else{

            map.moveCamera(CameraUpdateFactory
                    .newLatLngZoom(defaultLocation, 19f));
            map.getUiSettings().setMyLocationButtonEnabled(true);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                    }

                } else {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        checkLocationPermission();
                    }
                }
                return;

            case REQUEST_CODE_ASK_PERMISSIONS:
                if(grantResults[0]!=PackageManager.PERMISSION_GRANTED){

                }
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        }
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.map = googleMap;
        try {
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }

        this.map.addMarker(new MarkerOptions().position(defaultLocation).icon(bitmapDescriptorFromVector(this, R.drawable.asn_lk)));
        this.map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                defaultLocation, 10f));
        this.map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        this.map.getUiSettings().setMyLocationButtonEnabled( false );
        this.map.getUiSettings().setCompassEnabled( false );
        this.map.getUiSettings().setRotateGesturesEnabled(false);

        this.map.getCameraPosition();

        this.map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(@NonNull Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(@NonNull Marker marker) {
                // Inflate the layouts for the info window, title and snippet.
                View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_contents,
                        findViewById(R.id.map), false);

                TextView title = infoWindow.findViewById(R.id.title);
                title.setText(marker.getTitle());

                TextView snippet = infoWindow.findViewById(R.id.snippet);
                snippet.setText(marker.getSnippet());

                return infoWindow;
            }
        });
    }

    //Bottom Sheet
    private void setUpReferences() {
        LinearLayout layoutBottomSheet = findViewById(R.id.bottom_sheet_kehadiran_kantor);
        upsheet = findViewById(R.id.upsheet);
        sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet);
    }
    //endregion
    //region Helper methods for OnClick
    private void setOnClickListener() {
        upsheet.setOnClickListener(view -> {
            if (sheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else {
                sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopLocationUpdates();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }

    public void fokusLokasi(View view){
        startLocationUpdates();
        sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }

    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            LatLng latLng = new LatLng(Objects.requireNonNull(locationResult.getLastLocation()).getLatitude(), locationResult.getLastLocation().getLongitude());

            boolean isMock = mockLocationsEnabled || locationResult.getLastLocation().isFromMockProvider();
            if (isMock){
                mock_location = 1;
                rbFakeGPS ="1";
            }else{
                mock_location = 0;
            }
            if (map != null) {
                plotMarkers(locationResult.getLastLocation());
            }
        }
    };
}
package go.pemkott.appsandroidmobiletebingtinggi.kehadiran;


import static android.content.ContentValues.TAG;
import static go.pemkott.appsandroidmobiletebingtinggi.geolocation.model.LocationHelper.defaultLocation;
import static go.pemkott.appsandroidmobiletebingtinggi.konstanta.TimeFormat.SIMPLE_FORMAT_JAM;
import static go.pemkott.appsandroidmobiletebingtinggi.konstanta.TimeFormat.SIMPLE_FORMAT_JAM_TAGING;
import static go.pemkott.appsandroidmobiletebingtinggi.konstanta.TimeFormat.SIMPLE_FORMAT_TANGGAL;
import static go.pemkott.appsandroidmobiletebingtinggi.konstanta.TimeFormat.hari;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.radiobutton.MaterialRadioButton;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

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

public class AbsensiKehadiranActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int REQUEST_CHECK_SETTINGS = 100;
    private Context mContext;
    private LocationViewModel locationViewModel;
    private Location locationObj;
    private GoogleMap map;
    private static final String KEY_LOCATION = "location";

    double latGMap = 0, lngGMap = 0;
    Lokasi lokasi = new Lokasi();
    DialogView dialogView = new DialogView(AbsensiKehadiranActivity.this);
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
    LinearLayout llUpload, llAmbilFotoTaging;
    ProgressDialog progressDialog;
    Date dateBatasWaktu;
    String jamTaging, jamMasuk, jamPulang, hariIni, eKelompok, eJabatan, timetableid;
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
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;

    FragmentContainerView fragmentContainerView;

    private boolean mockLocationsEnabled;
    int mock_location = 0;

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

        setContentView(R.layout.activity_absensi_kehadiran);
        databaseHelper = new DatabaseHelper(this);
        databases();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(3000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

//Google Maps
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.putih));


        if (savedInstanceState != null) {
            locationObj = savedInstanceState.getParcelable(KEY_LOCATION);
        }
        ambilFoto();
        mContext = this;
        setupViews();
        setupViewModel();

//Google Maps

        Bundle intent = getIntent().getExtras();
        assert intent != null;
        jam_masuk = intent.getString("jam_masuk");
        jam_pulang = intent.getString("jam_pulang");
        rbTanggal = SIMPLE_FORMAT_TANGGAL.format(new Date());

        ivTaging = findViewById(R.id.ivTagingAbsen);
        llAmbilFotoTaging = findViewById(R.id.llAmbilFotoTaging);
        iconFotoTaging = findViewById(R.id.iconFotoTagingKehadiran);
        llUpload = findViewById(R.id.llUploadkehadiran);
        rgKehadiran = findViewById(R.id.rgKehadiran);
        TextView title_content = findViewById(R.id.title_content);
        fragmentContainerView = findViewById(R.id.map);
        setRoundedBackground(fragmentContainerView);
        llAmbilFotoTaging.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ambilFoto();
            }
        });

        title_content.setText("KEHADIRAN");

        setUpReferences();
        setOnClickListener();

        llUpload.setOnClickListener(view -> {

            if (mock_location == 1){
                dialogView.viewNotifKosong(AbsensiKehadiranActivity.this, "Anda terdeteksi menggunakan Fake GPS.", "Jika ditemukan berulang kali, akun anda akan terblokir otomatis dan tercatat Alpa.");
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
//        subscribeToLocationUpdate();

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

            boolean isMock = mockLocationsEnabled || locationResult.getLastLocation().isFromMockProvider();
            if (isMock){
                mock_location = 1;
                rbFakeGPS ="1";
                return;
            }else{
                mock_location = 0;
            }

            if (map != null) {
                plotMarkers(locationResult.getLastLocation());
            }
        }
    };


    public void databases(){

        Cursor tUser = databaseHelper.getAllData22();
        while (tUser.moveToNext()){
            sEmployId = tUser.getString(1);
        }

        hari = new SimpleDateFormat("EEE", localeID);
        tanggal = hari.format(new Date());

        Cursor tTimeTable = databaseHelper.getKegiatanTimeTable(sEmployId, String.valueOf(hari(tanggal)));
        if(tTimeTable.getCount() == 0){
            jamMasuk = null;
            jamPulang = null;
            return;
        }

        while (tTimeTable.moveToNext()){
            timetableid = tTimeTable.getString(2);
            hariIni = tTimeTable.getString(3);
            jamMasuk = tTimeTable.getString(5);
            jamPulang = tTimeTable.getString(6);
        }

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


    private void ambilFoto(){

        String filename = "photo";
        File storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        try {
            imageFile = File.createTempFile(filename, ".png", storageDirectory);
            currentPhotoPath = imageFile.getAbsolutePath();

            Uri imageUri = FileProvider.getUriForFile(AbsensiKehadiranActivity.this, "go.pemkott.appsandroidmobiletebingtinggi.fileprovider", imageFile);

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
        if (NetworkUtils.isConnectedMobile(AbsensiKehadiranActivity.this) || NetworkUtils.isConnectedWifi(AbsensiKehadiranActivity.this)){
            if (NetworkUtils.isConnectedFast(AbsensiKehadiranActivity.this)){
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
            }else{
                dialogView.viewNotifKosong(AbsensiKehadiranActivity.this, "Pastikan anda telah terhubung ke internet.", "");

            }
        }else{
            dialogView.viewNotifKosong(AbsensiKehadiranActivity.this, "Pastikan anda telah terhubung ke internet.", "");

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
    AmbilFoto ambilFoto = new AmbilFoto(AbsensiKehadiranActivity.this);
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_CANCELED){
            checkLocationPermission();

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

                startLocationUpdates();

            }
        }else {
            finish();
        }

    }

    public void uploadImages(){

        if(encodedImage == null || encodedImage.isEmpty()){
            dialogView.viewNotifKosong(AbsensiKehadiranActivity.this, "Harap melampirkan foto taging anda.", "");
        }else{
            if (jamMasuk == null || jamPulang == null){
                dialogView.viewNotifKosong(AbsensiKehadiranActivity.this, "Anda tidak memiliki Jadwal Kerja untuk hari ini", "");
            }
            else{

                periksaWaktu();
                hitungjarak();

                if (tagingTime.getTime() <= dateBatasWaktu.getTime()) {
                    dialogView.viewNotifKosong(AbsensiKehadiranActivity.this, "Anda hanya dapat mengisi absen masuk, "+batasWaktu+" menit sebelum Jam Masuk", "");
                }
                else{


                    selected = rgKehadiran.getCheckedRadioButtonId();
                    radioSelectedKehadiran = findViewById(selected);

                    if (eJabatan.equals("2")){
                        totalJarak = 1;
                    }

                    if (totalJarak > 150){
                        dialogView.viewNotifKosong(AbsensiKehadiranActivity.this, "Andah harus berada dilingkungan kantor untuk melakukan absensi.", "");
                    }
                    else{

                        String ketKehadiran;
                        String rbPosisi;
                        String rbStatus;
                        String rbValid;
                        if (radioSelectedKehadiran.getText().toString().equals("Masuk")){

                            if (jam_masuk != null){
                                dialogView.viewNotifKosong(AbsensiKehadiranActivity.this, "Anda sudah mengisi absensi masuk.", "");
                            }else{

                                if (tagingTime.getTime() >= jamPulangDate.getTime()){
                                    dialogView.viewNotifKosong(AbsensiKehadiranActivity.this, "Anda tidak dapat melakukan absensi masuk pada jam pulang kerja.", "");
                                }

                                else{
                                    ketKehadiran = "masuk";
                                    rbPosisi = "masuk";
                                    rbStatus = "hadir";

                                    rbValid = "2";
                                    String eselon = "0";

                                    if (eJabatan.equals("2")){
                                        rbJam = "07:30";
                                        rbKet = "sesuai waktu";
                                        rbPosisi = "masuk";
                                        rbStatus = "hadir";
                                        eselon = "2";
                                    }
//                                    viewBerakhlak();
//                                Upload Absensi masuk
                                    kirimdata(ketKehadiran, eselon, sEmployId, timetableid, rbTanggal, rbJam, rbPosisi, rbStatus, rbLat, rbLng, rbKet, mins, jamMasuk, rbValid, "100");

                                }

                            }

                        }
                        else{

                            rbPosisi = "pulang";
                            rbStatus = "hadir";
                            ketKehadiran = "pulang";

                            if (tagingTime.getTime() > jamPulangDate.getTime()) {
                                rbKet = "sesuai waktu";
                                mins = 0;
                            }else{
                                rbKet = "kecepatan";
                            }

                            if (jam_pulang == null){

                                if (tagingTime.getTime() < jamPulangDate.getTime()){
                                    dialogView.viewNotifKosong(AbsensiKehadiranActivity.this, "Anda belum dapat mengisi absensi pulang,", "silahkan lanjutkan kembali aktivitas kantor anda ya.");
                                }else{

                                    String eselon = "0";
                                    if (eJabatan.equals("2")){
                                        eselon = "2";
                                    }

                                    if (eJabatan.equals("2")){
                                        rbJam = "07:30";
                                        rbKet = "sesuai waktu";
                                        rbPosisi = "pulang";
                                        rbStatus = "hadir";
                                        eselon = "2";
                                    }
                                    rbValid = "2";

                                    if (jam_masuk == null ){
                                        viewBerakhlak("masukpulang", eselon, sEmployId, timetableid, rbTanggal, rbJam, rbPosisi, rbStatus, rbLat, rbLng, rbKet, 0,  jamPulang, rbValid);
                                    }
                                    else{
                                        viewBerakhlak("pulang", eselon, sEmployId, timetableid, rbTanggal, rbJam, rbPosisi, rbStatus, rbLat, rbLng, rbKet, 0,  jamPulang, rbValid);
                                    }
                                }

                            }else{
                                dialogView.viewNotifKosong(AbsensiKehadiranActivity.this, "Anda sudah mengisi absensi pulang.", "");
                            }
                        }
                    }
                }
            }
        }



    }

    public void kirimdata(String absensi, String eselon, String idpegawai, String timetableid, String tanggal, String jam, String posisi, String status, String lat, String lng, String ket, int terlambat, String jampegawai, String validasi, String berakhlak){

        progressDialog = new ProgressDialog(AbsensiKehadiranActivity.this, R.style.AppCompatAlertDialogStyle);
        progressDialog.setMessage("Sedang memproses...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        Call<ResponsePOJO> call = RetroClient.getInstance().getApi().uploadAbsenKehadiran(
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
                ket,
                terlambat,
                eOPD,
                jampegawai,
                validasi,
                rbFakeGPS,
                batasWaktu,
                berakhlak
        );

        call.enqueue(new Callback<ResponsePOJO>() {
            @Override
            public void onResponse(@NonNull Call<ResponsePOJO> call, @NonNull Response<ResponsePOJO> response) {

                if (!response.isSuccessful()){
                    progressDialog.dismiss();
                    dialogView.viewNotifKosong(AbsensiKehadiranActivity.this, "Gagal mengisi absensi,", "silahkan coba kembali  iii.");
                    return;
                }

                if(response.body().isStatus()){
                    progressDialog.dismiss();
                    viewSukses(AbsensiKehadiranActivity.this, response.body().getRemarks(), "");
                }else{
                    progressDialog.dismiss();
                    dialogView.viewNotifKosong(AbsensiKehadiranActivity.this, response.body().getRemarks(), "");
                }

            }

            @Override
            public void onFailure(@NonNull Call<ResponsePOJO> call, @NonNull Throwable t) {
                progressDialog.dismiss();

                dialogView.pesanError(AbsensiKehadiranActivity.this);
            }
        });
    }

    public void viewJadwal(){
        Dialog viewJadwal = new Dialog(AbsensiKehadiranActivity.this, R.style.DialogStyle);
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

            dialogSukes.dismiss();
            finish();
        });

        tvTutupDialog.setOnClickListener(v -> {

            dialogSukes.dismiss();
            finish();
        });

        handlerProgressDialog2();
        dialogSukes.show();
    }
    public void handlerProgressDialog(){
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            //your code here
            progressDialog.dismiss();}, 1500);

    }

    public void handlerProgressDialog2(){
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            //your code here
            progressDialog.dismiss();
            finish();}, 1500);

    }

    @SuppressLint("ResourceAsColor")
    public void periksaWaktu(){

        try {
            jamMasukDate = SIMPLE_FORMAT_JAM.parse(jamMasuk);
            jamPulangDate = SIMPLE_FORMAT_JAM.parse(jamPulang);

            jamTaging = SIMPLE_FORMAT_JAM_TAGING.format(new Date());

            SimpleDateFormat df = new SimpleDateFormat("HH:mm", localeID);
            Date d = df.parse(jamMasuk);
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

                rbKet = "tepat waktu";
                mins = 0;
            }else if(tagingTime.getTime() > jamMasukDate.getTime()){

                rbKet = "terlambat";
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    //  Maps
    //    Maps
    private void setupViewModel() {
        locationViewModel = ViewModelProviders.of(this).get(LocationViewModel.class);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void checkLocationPermission() {
        int hasWriteStoragePermission;
        hasWriteStoragePermission = getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        if (hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CHECK_SETTINGS);
            return;
        }

    }


    public void backFinalDinasLuar(View view){
        finish();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void setupViews() {
        //Maps
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

//    private void stopLocationUpdates() {
//        locationViewModel.getLocationHelper(mContext).stopLocationUpdates();
//    }

    int ulang = 0;

//    private void subscribeToLocationUpdate() {
//        locationViewModel.getLocationHelper(mContext).observe(this, new Observer<Location>() {
//            @Override
//            public void onChanged(@Nullable Location location) {
//                locationObj = location;
//
//                boolean isMock = mockLocationsEnabled || (Build.VERSION.SDK_INT >= 18 && location.isFromMockProvider());
//                if (isMock){
//                    mock_location = 1;
//                    rbFakeGPS ="1";
//                }else{
//                    mock_location = 0;
//                }
//
//
//                plotMarkers(locationObj);
//                ulang++;
//            }
//        });
//
//
//
//    }

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
            map.addMarker(new MarkerOptions().position(new LatLng(locationObj.getLatitude(), locationObj.getLongitude())).icon(bitmapDescriptorFromVector(this, R.drawable.asn_lk)).title(lokasi.getAddress(AbsensiKehadiranActivity.this, locationObj.getLatitude(), locationObj.getLongitude())));
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(locationObj.getLatitude(), locationObj.getLongitude()), 19f));
            latGMap = locationObj.getLatitude();
            lngGMap = locationObj.getLongitude();

            locationArrayList.add(locationObj);

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
                }
            }

        }else{

            map.moveCamera(CameraUpdateFactory
                    .newLatLngZoom(defaultLocation, 19f));
            map.getUiSettings().setMyLocationButtonEnabled(true);
        }

        stopLocationUpdates();

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
                        if (ulang == 1){
                            ulang = 0;
                        }
                    }

                } else {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        checkLocationPermission();
                    }
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
    protected void onResume() {
        super.onResume();
    }

    //endregion
//Bottom Sheet



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

    String berakhlak;
    int ber, a, k, h, l, ad, ko;
    StringBuffer stringBuffer = new StringBuffer();
    public void viewBerakhlak(String pulang, String eselon, String sEmployId, String timetableid, String rbTanggal, String rbJam,
                              String rbPosisi, String rbStatus, String rbLat, String rbLng, String rbKet, Integer terlambat, String jamPulang, String rbValid){
        Dialog berakhlakDialog = new Dialog(AbsensiKehadiranActivity.this, R.style.DialogStyle);
        berakhlakDialog.setContentView(R.layout.vire_berakhlak);
        berakhlakDialog.setCancelable(false);

        LinearLayout llBer = berakhlakDialog.findViewById(R.id.llBer);
        LinearLayout llAkuntabel = berakhlakDialog.findViewById(R.id.llAkuntabel);
        LinearLayout llKompeten = berakhlakDialog.findViewById(R.id.llKompeten);
        LinearLayout llHarmonis = berakhlakDialog.findViewById(R.id.llHarmonis);
        LinearLayout llLoyal = berakhlakDialog.findViewById(R.id.llLoyal);
        LinearLayout llAdaptif = berakhlakDialog.findViewById(R.id.llAdaptif);
        LinearLayout llKolaboratif = berakhlakDialog.findViewById(R.id.llKolaboratif);

        MaterialRadioButton rbBer = berakhlakDialog.findViewById(R.id.rbBer);
        MaterialRadioButton rbAkuntabel = berakhlakDialog.findViewById(R.id.rbAkuntabel);
        MaterialRadioButton rbKompeten = berakhlakDialog.findViewById(R.id.rbKompeten);
        MaterialRadioButton rbHarmanis = berakhlakDialog.findViewById(R.id.rbHarmanis);
        MaterialRadioButton rbLoyal = berakhlakDialog.findViewById(R.id.rbLoyal);
        MaterialRadioButton rbAdaptif = berakhlakDialog.findViewById(R.id.rbAdaptif);
        MaterialRadioButton rbKolaboratif = berakhlakDialog.findViewById(R.id.rbKolaboratif);

        ImageView ivCloseBerakhlak = berakhlakDialog.findViewById(R.id.ivCloseBerakhlak);
        TextView tvKirimSurveiBerakhlak = berakhlakDialog.findViewById(R.id.tvKirimSurveiBerakhlak);

        ivCloseBerakhlak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                berakhlakDialog.dismiss();
            }
        });

        llBer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rbBer.isChecked()){

                    rbBer.setChecked(false);
                    ber = 0;

                }else{

                    ber = 1;
                    rbBer.setChecked(true);

                }
            }
        });



        llAkuntabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (rbAkuntabel.isChecked()){

                    rbAkuntabel.setChecked(false);
                    a = 0;

                }else{

                    a = 1;
                    rbAkuntabel.setChecked(true);

                }

            }
        });

        llKompeten.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rbKompeten.isChecked()){

                    rbKompeten.setChecked(false);
                    k = 0;

                }else{

                    k = 1;
                    rbKompeten.setChecked(true);

                }

            }
        });

        llHarmonis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rbHarmanis.isChecked()){

                    rbHarmanis.setChecked(false);
                    h = 0;

                }else{

                    h = 1;
                    rbHarmanis.setChecked(true);

                }

            }
        });

        llLoyal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rbLoyal.isChecked()){

                    rbLoyal.setChecked(false);
                    l = 0;

                }else{

                    l = 1;
                    rbLoyal.setChecked(true);

                }

            }
        });

        llAdaptif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rbAdaptif.isChecked()){

                    rbAdaptif.setChecked(false);
                    ad = 0;

                }else{

                    ad = 1;
                    rbAdaptif.setChecked(true);

                }

            }
        });

        llKolaboratif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rbKolaboratif.isChecked()){

                    rbKolaboratif.setChecked(false);
                    ko = 0;

                }else{

                    ko = 1;
                    rbKolaboratif.setChecked(true);

                }
            }
        });

        tvKirimSurveiBerakhlak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ber == 0 && a == 0 && k == 0 && h ==  0 && l == 0 && ad == 0 && ko == 0){
                    Toast.makeText(mContext, "Tentukan Core Values", Toast.LENGTH_SHORT).show();
                }else{
                    if (ber == 1){
                        stringBuffer.append("1, ");
                    }

                    if(a == 1){
                        stringBuffer.append("2, ");
                    }

                    if(k == 1){
                        stringBuffer.append("3, ");
                    }
                    if(h == 1){
                        stringBuffer.append("4, ");
                    }

                    if(l == 1){
                        stringBuffer.append("5, ");
                    }
                    if(ad == 1){
                        stringBuffer.append("6, ");
                    }
                    if(ko == 1){
                        stringBuffer.append("7, ");
                    }


                    int length = stringBuffer.length();

                    if (length > 0) {
                        stringBuffer.deleteCharAt(length - 2);
                    }

                    berakhlak = stringBuffer.toString();

                    berakhlakDialog.dismiss();

                    if (pulang.equals("masukpulang")){
                        kirimdata("masukpulang", eselon, sEmployId, timetableid, rbTanggal, rbJam, rbPosisi, rbStatus, rbLat, rbLng, rbKet, 0,  jamPulang, rbValid, berakhlak);
                    } else if (pulang.equals("pulang")) {
                        kirimdata("pulang", eselon, sEmployId, timetableid, rbTanggal, rbJam, rbPosisi, rbStatus, rbLat, rbLng, rbKet, 0,  jamPulang, rbValid, berakhlak);
                    }

                }

            }
        });

        berakhlakDialog.show();

    }
}
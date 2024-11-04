package go.pemkott.appsandroidmobiletebingtinggi.ui.dashboard;

import static android.os.Build.VERSION.SDK_INT;
import static go.pemkott.appsandroidmobiletebingtinggi.konstanta.TimeFormat.BULAN;
import static go.pemkott.appsandroidmobiletebingtinggi.konstanta.TimeFormat.HARI_TEXT;
import static go.pemkott.appsandroidmobiletebingtinggi.konstanta.TimeFormat.SIMPLE_FORMAT_JAM_TAGING;
import static go.pemkott.appsandroidmobiletebingtinggi.konstanta.TimeFormat.SIMPLE_FORMAT_TANGGAL;
import static go.pemkott.appsandroidmobiletebingtinggi.konstanta.TimeFormat.TAHUN;
import static go.pemkott.appsandroidmobiletebingtinggi.konstanta.TimeFormat.TANGGAL;
import static go.pemkott.appsandroidmobiletebingtinggi.konstanta.TimeFormat.bulan;
import static go.pemkott.appsandroidmobiletebingtinggi.konstanta.TimeFormat.hariText;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.api.HttpService;
import go.pemkott.appsandroidmobiletebingtinggi.api.RetroClient;
import go.pemkott.appsandroidmobiletebingtinggi.database.DatabaseHelper;
import go.pemkott.appsandroidmobiletebingtinggi.databinding.FragmentDashboardBinding;
import go.pemkott.appsandroidmobiletebingtinggi.dialogview.DialogView;
import go.pemkott.appsandroidmobiletebingtinggi.dinasluar.PerjalananDinas.SppdActivity;
import go.pemkott.appsandroidmobiletebingtinggi.dinasluar.TugasLapangan.TugasLapanganActivity;
import go.pemkott.appsandroidmobiletebingtinggi.izin.Cuti.CutiActivity;
import go.pemkott.appsandroidmobiletebingtinggi.izin.KeperluanPribadi.KeperluanPribadiActivity;
import go.pemkott.appsandroidmobiletebingtinggi.izin.Sakit.SakitActivity;
import go.pemkott.appsandroidmobiletebingtinggi.izinsift.JadwalIzinSiftActivity;
import go.pemkott.appsandroidmobiletebingtinggi.jadwalsift.JadwalSiftActivity;
import go.pemkott.appsandroidmobiletebingtinggi.kehadiran.AbsensiKehadiranActivity;
import go.pemkott.appsandroidmobiletebingtinggi.model.CheckAbsensi;
import go.pemkott.appsandroidmobiletebingtinggi.model.CheckUpdate;
import go.pemkott.appsandroidmobiletebingtinggi.model.ValidasiData;
import go.pemkott.appsandroidmobiletebingtinggi.panduanabsensi.PanduanActivity;
import go.pemkott.appsandroidmobiletebingtinggi.utils.NetworkUtils;
import go.pemkott.appsandroidmobiletebingtinggi.verifikasi.ValidasiNewActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DashbooardFragment extends Fragment {
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;
    LocationManager manager;
    String statusSift, fotoProfile, jam_masuk, jam_pulang, sOPD,sNip, sJabatan, sKantor, sEmployee_id, sUsername, sAkses, sActive,  sToken, sVerifikator, toDay = SIMPLE_FORMAT_TANGGAL.format(new Date());
    private FragmentDashboardBinding binding;
    DatabaseHelper databaseHelper;
    HttpService httpService;
    DialogView dialogView = new DialogView(getContext());
    TextView txtJamPulang, txtJammasuk, txtKehadiran, txtPerjalananDinas, txtIzin, txtAgenda;
    ShimmerFrameLayout shimmerFrameLayout;
    LinearLayout kehadiran, perjalanandinas, izin, llPanduan;
    View viewKehadiran, viewPerjalananDinas, viewIzin, viewAgenda;
    RelativeLayout rlNotif;
    SwipeRefreshLayout swipeRefreshLayout;
    TextView tvNpNotif;
ImageView ivNotif;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        manager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
        databaseHelper = new DatabaseHelper(getContext());
        datauser();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://absensi.tebingtinggikota.go.id/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        httpService = retrofit.create(HttpService.class);

        kehadiran = binding.kehadiran;
        perjalanandinas = binding.perjalanandinas;
        izin = binding.izin;
        txtKehadiran = binding.txtKehadiran;
        txtPerjalananDinas = binding.txtPerjalananDinas;
        txtIzin = binding.txtIzin;
        txtAgenda = binding.txtAgenda;
        ivNotif = binding.ivNotif;
        swipeRefreshLayout = binding.swipeRefreshLayout;

        viewKehadiran = binding.viewKehadiran;
        viewPerjalananDinas = binding.viewPerjalananDinas;
        viewIzin = binding.viewIzin;
        viewAgenda = binding.viewAgenda;
        rlNotif = binding.rlNotif;
        tvNpNotif = binding.tvNpNotif;

        llPanduan = binding.llPanduan;

        llPanduan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(requireActivity(), PanduanActivity.class));
            }
        });

        rlNotif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iValidasi = new Intent(getActivity(), ValidasiNewActivity.class);
                startActivity(iValidasi);
            }
        });

        final CircleImageView imgProfile = binding.imgProfile;
        final TextView namaPegawai = binding.txtNama;
        final TextView nipPegawai = binding.txtNIP;
        final TextView jabatanPegawai = binding.txtJabatan;
        final TextView kantorPegawai = binding.txtKantor;
        final TextView txtTanggal = binding.txtTgl;
        txtJammasuk = binding.tvBarJamMasuk;
        txtJamPulang = binding.tvBarJamPulang;

        shimmerFrameLayout = binding.shimmerFrameLayout;

        kehadiran.setEnabled(false);
        perjalanandinas.setEnabled(false);
        izin.setEnabled(false);

        Glide.with(this)
                .load( "https://absensi.tebingtinggikota.go.id/storage/"+fotoProfile )
                .into( imgProfile );

        kehadiran.setOnClickListener(v -> {
            if (NetworkUtils.isConnected(requireActivity())){
                if (NetworkUtils.isConnectedFast(requireActivity())){

                    if (statusSift.equals("0")){
                        viewKehadiran();
                    }else{
                        Intent intentJadwalSifting = new Intent(requireActivity(), JadwalSiftActivity.class);
                        intentJadwalSifting.putExtra("jam_masuk", jam_masuk);
                        intentJadwalSifting.putExtra("jam_pulang", jam_pulang);
                        startActivity(intentJadwalSifting);
                    }

                }else{
                    dialogView.viewNotifKosong(requireActivity(), "Internet anda sangat lambat,", "mohon periksa kembali jaringan internet anda.");
                }

            }else{
                dialogView.viewNotifKosong(requireActivity(), "Internet tidak tersedia,", "mohon periksa kembali jaringan internet anda.");

            }
        });

        perjalanandinas.setOnClickListener(v -> {
            if (NetworkUtils.isConnected(requireActivity())){
                if (NetworkUtils.isConnectedFast(requireActivity())){
                    Intent iPd = new Intent(getContext(), SppdActivity.class);
                    iPd.putExtra("jam_masuk", jam_masuk);
                    iPd.putExtra("jam_pulang", jam_pulang);
                    startActivity(iPd);
                }else{
                    dialogView.viewNotifKosong(requireActivity(), "Internet anda sangat lambat,", "mohon periksa kembali jaringan internet anda.");
                }

            }else{
                dialogView.viewNotifKosong(requireActivity(), "Internet tidak tersedia,", "mohon periksa kembali jaringan internet anda.");

            }

        });
        izin.setOnClickListener(v -> {
            if (NetworkUtils.isConnected(requireActivity())){
                if (NetworkUtils.isConnectedFast(requireActivity())){
                    if (statusSift.equals("0")){
                        viewIzin();
                    }else{
                        Intent intentJadwalIzinSifting = new Intent(requireActivity(), JadwalIzinSiftActivity.class);
                        intentJadwalIzinSifting.putExtra("jam_masuk", jam_masuk);
                        intentJadwalIzinSifting.putExtra("jam_pulang", jam_pulang);
                        startActivity(intentJadwalIzinSifting);
                    }

                }else{
                    dialogView.viewNotifKosong(requireActivity(), "Internet anda sangat lambat,", "mohon periksa kembali jaringan internet anda.");
                }

            }else{
                dialogView.viewNotifKosong(requireActivity(), "Internet tidak tersedia,", "mohon periksa kembali jaringan internet anda.");

            }
        });


        namaPegawai.setText(sUsername);
        nipPegawai.setText("NIP. "+sNip);
        jabatanPegawai.setText(sJabatan);
        kantorPegawai.setText(sKantor);

        String tanggal = TANGGAL.format(new Date());
        String bulan = BULAN.format(new Date());
        String tahun = TAHUN.format(new Date());
        String hari = HARI_TEXT.format(new Date());
        String infotanggal = hariText(hari)+", "+tanggal+" "+bulan(bulan)+" "+tahun;
        txtTanggal.setText(infotanggal);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Memanggil method refreshData() saat melakukan swipe pada layar
                onResume();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        return root;
    }

    private void datauser(){

        Cursor res = databaseHelper.getAllData22();
        if (res.getCount()==0){

            return;
        }


        while (res.moveToNext()){

            sEmployee_id = res.getString(1);
            sAkses = res.getString(3);
            sActive = res.getString(4);
            sToken = res.getString(5);
            sVerifikator = res.getString(6);
        }

        Cursor dataPegawai = databaseHelper.getDataEmployee(sEmployee_id);
        while (dataPegawai.moveToNext()){
            sNip = dataPegawai.getString(5);
            sUsername = dataPegawai.getString(6);
            sJabatan = dataPegawai.getString(12);
            sKantor = dataPegawai.getString(13);
            sOPD = dataPegawai.getString(4);
            fotoProfile = dataPegawai.getString(17);
            statusSift = dataPegawai.getString(19);

        }
    }

    public void viewKehadiran(){
        Dialog kehadiranDialog = new Dialog(getActivity(), R.style.DialogStyle);
        kehadiranDialog.setContentView(R.layout.view_kehadiran);
        kehadiranDialog.setCancelable(false);
        CardView ivTKantor = kehadiranDialog.findViewById(R.id.ivTKantor);
        CardView ivTLapangan = kehadiranDialog.findViewById(R.id.ivTLapangan);
//        CardView ivJadwalsift = kehadiranDialog.findViewById(R.id.ivJadwalSift);
        ImageView ivTutupViewKehadrian = kehadiranDialog.findViewById(R.id.ivTutupViewKehadrian);

        ivTutupViewKehadrian.setOnClickListener(view -> kehadiranDialog.dismiss());


        ivTKantor.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AbsensiKehadiranActivity.class);
            intent.putExtra("jam_masuk", jam_masuk);
            intent.putExtra("jam_pulang", jam_pulang);
            startActivity(intent);
            kehadiranDialog.dismiss();
        });



        ivTLapangan.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), TugasLapanganActivity.class);
            intent.putExtra("jam_masuk", jam_masuk);
            intent.putExtra("jam_pulang", jam_pulang);
            startActivity(intent);
            kehadiranDialog.dismiss();

        });

        kehadiranDialog.show();

    }

    public void viewIzin(){
        Dialog izinDialogs = new Dialog(getActivity(), R.style.DialogStyle);
        izinDialogs.setCancelable(false);
        izinDialogs.setContentView(R.layout.view_izin);

        ImageView ivTutupViewIzin = izinDialogs.findViewById(R.id.ivTutupViewIzin);

        CardView cardKp = izinDialogs.findViewById(R.id.cardKp);
        CardView cardSakit = izinDialogs.findViewById(R.id.cardSakit);
        CardView cardCuti = izinDialogs.findViewById(R.id.cardCuti);

        ivTutupViewIzin.setOnClickListener(v -> izinDialogs.dismiss());


        cardKp.setOnClickListener(v -> {

            Intent intenKp = new Intent(getActivity(), KeperluanPribadiActivity.class);
            intenKp.putExtra("jam_masuk", jam_masuk);
            intenKp.putExtra("jam_pulang", jam_pulang);
            startActivity(intenKp);

            izinDialogs.dismiss();
        });

        cardCuti.setOnClickListener(v -> {
            Intent intentCuti = new Intent(getActivity(), CutiActivity.class);
            intentCuti.putExtra("jam_masuk", jam_masuk);
            intentCuti.putExtra("jam_pulang", jam_pulang);
            startActivity(intentCuti);
            izinDialogs.dismiss();
        });

        cardSakit.setOnClickListener(v -> {
            Intent intentSakit = new Intent(getActivity(), SakitActivity.class);
            intentSakit.putExtra("jam_masuk", jam_masuk);
            intentSakit.putExtra("jam_pulang", jam_pulang);
            startActivity(intentSakit);

            izinDialogs.dismiss();
        });

        izinDialogs.show();


    }
    @Override
    public void onResume() {
        super.onResume();

        dataValidasi(sVerifikator, sEmployee_id);
        int version = 0;
        try {
            PackageInfo pInfo = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
            version = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        checkupdate(version);
//        if (sVerifikator.equals("verifikator1") || sVerifikator.equals("verifikator2")){
//            rlNotif.setVisibility(View.GONE);
//        }
        
    }

    public void periksaDataAbsensi(){
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
        if (statusSift.equals("0")){

            if (NetworkUtils.isConnected(getActivity())){
                toDay = SIMPLE_FORMAT_TANGGAL.format(new Date());
                periksaAbsensi(toDay, sEmployee_id);
            }else{
                dialogView.pesanError(requireActivity());
            }

        }else{

            Cursor checkToday = databaseHelper.getInfoJadwalSiftToday(sEmployee_id, infoJadwalhariini);

            if (checkToday.getCount() > 0){
                while (checkToday.moveToNext()){
                    String idSift = checkToday.getString(2);
                    Cursor infoSift = databaseHelper.getDataSift(sOPD, idSift);
                    if (infoSift.getCount()==0){
                        return;
                    }

                    while (infoSift.moveToNext()){

                        String jamSekarangString = SIMPLE_FORMAT_JAM_TAGING.format(new Date());
                        Date jamSekarang = null, jamAbsenMalam = null, batasWaktuJamMalam =null;
                        try {
                            jamSekarang = SIMPLE_FORMAT_JAM_TAGING.parse(jamSekarangString);
                            batasWaktuJamMalam = SIMPLE_FORMAT_JAM_TAGING.parse("12:00");
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        if (infoSift.getString(2).equals("malam")){
                            if (jamSekarang.getTime() < batasWaktuJamMalam.getTime()){
                                if (NetworkUtils.isConnectedMobile(requireActivity()) || NetworkUtils.isConnectedWifi(requireActivity())){
                                    if (NetworkUtils.isConnectedFast(getActivity())){
                                        periksaAbsensiSiftMalam(infoJadwalhariini, sEmployee_id);
                                    }else{
                                        dialogView.pesanError(requireActivity());
                                    }
                                }

                            }else{
                                if (NetworkUtils.isConnectedMobile(requireActivity()) || NetworkUtils.isConnectedWifi(requireActivity())){
                                    if (NetworkUtils.isConnectedFast(getActivity())){
                                        toDay = SIMPLE_FORMAT_TANGGAL.format(new Date());
                                        periksaAbsensi(toDay, sEmployee_id);
                                    }else{
                                        dialogView.pesanError(requireActivity());
                                    }
                                }else{
                                    dialogView.pesanError(requireActivity());
                                }
                            }
                        }else{
                            if (NetworkUtils.isConnectedMobile(requireActivity()) || NetworkUtils.isConnectedWifi(requireActivity())){
                                if (NetworkUtils.isConnectedFast(getActivity())){
                                    toDay = SIMPLE_FORMAT_TANGGAL.format(new Date());
                                    periksaAbsensi(toDay, sEmployee_id);
                                }else{
                                    dialogView.pesanError(requireActivity());
                                }
                            }else{
                                dialogView.pesanError(requireActivity());
                            }
                        }

                    }
                }

            }else{
                if (NetworkUtils.isConnectedMobile(requireActivity()) || NetworkUtils.isConnectedWifi(requireActivity())){
                    if (NetworkUtils.isConnectedFast(getActivity())){
                        toDay = SIMPLE_FORMAT_TANGGAL.format(new Date());
                        periksaAbsensi(toDay, sEmployee_id);
                    }else{
                        dialogView.pesanError(requireActivity());
                    }
                }else{
                    dialogView.pesanError(requireActivity());
                }
            }

        }
    }


    public void periksaAbsensiSiftMalam(String siftMalam, String idE){
        Call<CheckAbsensi> call = RetroClient.getInstance().getApi().checkabsensisiftmalam(siftMalam, idE);
        call.enqueue(new Callback<CheckAbsensi>() {
            @Override
            public void onResponse(@NonNull Call<CheckAbsensi> call, @NonNull Response<CheckAbsensi> response) {
                if (!response.isSuccessful()) {
                    getLocation();
                    checkGPS();
                    jam_masuk = null;
                    txtJammasuk.setText("--:--");

                    jam_pulang = null;
                    txtJamPulang.setText("--:--");
                    dialogView.viewNotifKosong(getContext(), "Gagal memeriksa data absensi,", "mohon periksa internet anda.");
                    handlerProgressDialog();
                    return;
                }

                int periksaabsenpulang = 0;
                assert !(response.body() == null);
                if (response.body().isStatus()) {

                    jam_masuk = response.body().getJam_masuk();
                    if (response.body().getJam_masuk() == null || response.body().getJam_masuk().isEmpty()) {
                        txtJammasuk.setText("--:--");
                        jam_masuk = null;
                    } else {
                        txtJammasuk.setText(jam_masuk);
                    }

                    jam_pulang = response.body().getJam_pulang();
                    if (response.body().getJam_pulang() == null || response.body().getJam_pulang().isEmpty()) {
                        txtJamPulang.setText("--:--");
                        jam_pulang = null;
                    } else {
                        periksaabsenpulang = 1;
                        txtJamPulang.setText(jam_pulang);
                    }

                    getLocation();
                    checkGPS();
                    handlerProgressDialog();


                } else {

                    jam_masuk = null;
                    txtJammasuk.setText("--:--");

                    jam_pulang = null;
                    txtJamPulang.setText("--:--");

                    getLocation();
                    checkGPS();
                    handlerProgressDialog();


                }




            }

            @Override
            public void onFailure(@NonNull Call<CheckAbsensi> call, @NonNull Throwable t) {
                getLocation();
                checkGPS();
                dialogView.pesanError(requireActivity());
            }
        });


    }
    public void periksaAbsensi(String tanggal, String idE){
        Call<CheckAbsensi> call = RetroClient.getInstance().getApi().checkabsensi(tanggal, idE);
        call.enqueue(new Callback<CheckAbsensi>() {
            @Override
            public void onResponse(@NonNull Call<CheckAbsensi> call, @NonNull Response<CheckAbsensi> response) {
                if (!response.isSuccessful()) {
                    jam_masuk = null;
                    txtJammasuk.setText("--:--");

                    jam_pulang = null;
                    txtJamPulang.setText("--:--");
                    dialogView.viewNotifKosong(getContext(), "Gagal memeriksa data absensi,", "mohon periksa internet anda.");
                }

                assert !(response.body() == null);
                if (response.body().isStatus()) {

                    jam_masuk = response.body().getJam_masuk();
                    if (response.body().getJam_masuk() == null) {
                        txtJammasuk.setText("--:--");
                    } else {
                        txtJammasuk.setText(jam_masuk);
                    }

                    jam_pulang = response.body().getJam_pulang();
                    if (response.body().getJam_pulang() == null) {
                        txtJamPulang.setText("--:--");
                    } else {
                        txtJamPulang.setText(jam_pulang);
                    }
                    checkGPS();

                } else {

                    jam_masuk = null;
                    txtJammasuk.setText("--:--");

                    jam_pulang = null;
                    txtJamPulang.setText("--:--");
                    checkGPS();

                }

                handlerProgressDialog();


            }

            @Override
            public void onFailure(@NonNull Call<CheckAbsensi> call, @NonNull Throwable t) {
                dialogView.pesanError(requireActivity());

            }
        });


    }

    public void handlerProgressDialog() {

            shimmerFrameLayout.stopShimmer();
            shimmerFrameLayout.hideShimmer();
            Window window = requireActivity().getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.biru_1));
            kehadiran.setEnabled(true);
            perjalanandinas.setEnabled(true);
            izin.setEnabled(true);

            txtKehadiran.setVisibility(View.VISIBLE);
            txtPerjalananDinas.setVisibility(View.VISIBLE);
            txtIzin.setVisibility(View.VISIBLE);
            txtAgenda.setVisibility(View.VISIBLE);

            viewKehadiran.setVisibility(View.GONE);
            viewPerjalananDinas.setVisibility(View.GONE);
            viewIzin.setVisibility(View.GONE);
            viewAgenda.setVisibility(View.GONE);

    }

    public void checkupdate(int version){

        Call<List<CheckUpdate>> callCheckUpdate = httpService.getCheckUpdate("https://absensi.tebingtinggikota.go.id/api/updateapp");
        callCheckUpdate.enqueue(new Callback<List<CheckUpdate>>() {
            @Override
            public void onResponse(@NonNull Call<List<CheckUpdate>> call, @NonNull Response<List<CheckUpdate>> response) {
                if (!response.isSuccessful()) {
                    return;
                }
                int hasilCheckUpdate = 0 ;
                List<CheckUpdate> checkUpdates = response.body();
                for (CheckUpdate checkUpdate : checkUpdates) {
                    if (checkUpdate.getVersion() > version) {
                        final String appPackageName = checkUpdate.getNamapackage();
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                        }

                    }
                    hasilCheckUpdate += 1;
                }


                if (hasilCheckUpdate == checkUpdates.size()){

                    periksaDataAbsensi();

                }

            }

            @Override
            public void onFailure(@NonNull Call<List<CheckUpdate>> call, @NonNull Throwable t) {
                dialogView.viewNotifKosong(getContext(), "Gagal menghubungkan, ", "mohon periksa jaringan internet anda.");

            }
        });

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2296) {
            if (SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    // perform action when allow permission success
                } else {
//                    Toast.makeText(this, "Allow permission for storage access!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void checkGPS(){
        if(!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){

            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity(), R.style.ThemeOverlay_App_MaterialAlertDialog);
            builder.setCancelable(false);
            builder.setTitle("Peringatan!");
            builder.setMessage("GPS anda tidak aktif, anda harus mengaktifkan GPS terlebih dahulu!");
            builder.setNegativeButton("Nyalakan",
                    (dialogInterface, i) -> {

                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        dialogInterface.dismiss();
                    }
            );

            AlertDialog alert = builder.create();
            alert.show();
        }else{
            getLocation();
        }
    }

    public void getLocation(){
        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode,
                permissions,
                grantResults);

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Permission", "Sukses");
            }
        }
        else if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Permission", "Sukses");
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void dataValidasi(String verifikator, String idE){
        if (verifikator.equals("verifikator1") || verifikator.equals("verifikator2")){
            Call<List<ValidasiData>> callKegiatan = httpService.getUrlListValidasi("https://absensi.tebingtinggikota.go.id/api/newVeriFragment?verifikator="+verifikator+"&id="+idE);
            callKegiatan.enqueue(new Callback<List<ValidasiData>>() {
                @Override
                public void onResponse(Call<List<ValidasiData>> call, Response<List<ValidasiData>> response) {
                    if (!response.isSuccessful()){

                        return;
                    }

                    List<ValidasiData> validasiDatas = response.body();
                    if (validasiDatas.size() > 0){
                        rlNotif.setVisibility(View.VISIBLE);
                        tvNpNotif.setText(String.valueOf(validasiDatas.size()));

                    }else{
                        rlNotif.setVisibility(View.GONE);
                        tvNpNotif.setText("0");
                    }
                }

                @Override
                public void onFailure(Call<List<ValidasiData>> call, Throwable t) {
//                    pesanError();
                }
            });
        }

    }
}
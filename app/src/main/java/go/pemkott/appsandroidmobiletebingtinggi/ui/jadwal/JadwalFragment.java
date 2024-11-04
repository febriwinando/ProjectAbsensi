package go.pemkott.appsandroidmobiletebingtinggi.ui.jadwal;

import static go.pemkott.appsandroidmobiletebingtinggi.konstanta.TimeFormat.BULAN;
import static go.pemkott.appsandroidmobiletebingtinggi.konstanta.TimeFormat.TAHUN;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.api.HttpService;
import go.pemkott.appsandroidmobiletebingtinggi.calendarjadwalsift.CalendarJadwalSiftActivity;
import go.pemkott.appsandroidmobiletebingtinggi.database.DatabaseHelper;
import go.pemkott.appsandroidmobiletebingtinggi.databinding.FragmentJadwalBinding;
import go.pemkott.appsandroidmobiletebingtinggi.dialogview.DialogView;
import go.pemkott.appsandroidmobiletebingtinggi.model.KegiatanIzin;
import go.pemkott.appsandroidmobiletebingtinggi.model.Koordinat;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class JadwalFragment extends Fragment {

    private FragmentJadwalBinding binding;
    RecyclerView rvJadwalKegiatan;
    DatabaseHelper databaseHelper;
    List<TimeTebleSetting> timeTable = new ArrayList<>();
    String sEmployee_id, sToken,  sAkses, sActive;
    TextView tvInfoJamKerja;
    RelativeLayout rlSngkronJadwal, rlSngkronLokasi, rlSngkronKegiatan, rlSingkronJadwalSift;
    HttpService holderAPI;
    String sNama, sNIP, sEmail, sNoHpEmployee, sKelompok, sJabatan, sKantor, fotoProfile, sOPD,statusSift;

    DialogView dialogView = new DialogView(getContext());
    ProgressDialog progressDialog;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        JadwalViewModel jadwalViewModel =
                new ViewModelProvider(this).get(JadwalViewModel.class);

        binding = FragmentJadwalBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Window window = requireActivity().getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.biru_1));

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://absensi.tebingtinggikota.go.id/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        holderAPI = retrofit.create(HttpService.class);
        databaseHelper = new DatabaseHelper(getActivity());

        dataUser();
        timeTable.clear();


        rvJadwalKegiatan =binding.rvJadwalKegiatan;
        rvJadwalKegiatan.setHasFixedSize(false);
        tvInfoJamKerja = binding.tvInfoJamKerja;
        rlSngkronJadwal = binding.rlSngkronJadwal;
        rlSngkronLokasi = binding.rlSngkronLokasi;
        rlSngkronKegiatan = binding.rlSngkronKegiatan;
        rlSingkronJadwalSift = binding.rlSingkronJadwalSift;

        timeTables();
        showRecyclerView();

        rlSngkronJadwal.setOnClickListener(v -> singkronTimetable());

        rlSngkronLokasi.setOnClickListener(v -> {
            koordinatOPD();
            koordintaEmployee();
        });

        rlSngkronKegiatan.setOnClickListener(view -> singkronKegiatan());

        rlSingkronJadwalSift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentCalendarSingkonisasi = new Intent(requireActivity(), CalendarJadwalSiftActivity.class);
                startActivity(intentCalendarSingkonisasi);
//                getJadwalSift();
            }
        });

        if (statusSift.equals("1")){
            tvInfoJamKerja.setVisibility(View.GONE);
            rvJadwalKegiatan.setVisibility(View.GONE);
            rlSngkronJadwal.setVisibility(View.GONE);
        }else{
            rlSingkronJadwalSift.setVisibility(View.GONE);
        }
//        final TextView textView = binding.textNotifications;
//        jadwalViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    private void showRecyclerView() {

        rvJadwalKegiatan.setLayoutManager(new GridLayoutManager(getActivity(), 1));
        SettingAdapter settingAdapter = new SettingAdapter(timeTable);
        rvJadwalKegiatan.setAdapter(settingAdapter);

    }

    public void dataUser(){
        Cursor user = databaseHelper.getAllData22();

        while (user.moveToNext()){
            sEmployee_id = user.getString(1);
            sAkses = user.getString(3);
            sActive = user.getString(4);
            sToken = user.getString(5);
        }

        Cursor resa = databaseHelper.getDataEmployee(sEmployee_id);

        while (resa.moveToNext()){
            sOPD = resa.getString(4);
            sNIP = resa.getString(5);
            sNama = resa.getString(6);
            sEmail = resa.getString(7);
            sNoHpEmployee = resa.getString(8);
            if (!resa.getString(9).equals("pns")){
                sKelompok = "Non-PNS";
            }else{
                sKelompok = "Pegawai Negeri Sipil";
            }

            sJabatan = resa.getString(12);
            sKantor = resa.getString(13);
            fotoProfile = resa.getString(17);
            statusSift = resa.getString(19);

        }


    }

    public List<TimeTebleSetting> timeTables(){

        Cursor jadwal = databaseHelper.getKegiatanTimeTableCheck(sEmployee_id);
        if (jadwal.getCount() == 0){
            tvInfoJamKerja.setVisibility(View.VISIBLE);
        }

        while (jadwal.moveToNext()){
            TimeTebleSetting timeTables = new TimeTebleSetting();
            timeTables.setId(jadwal.getString(0));
            timeTables.setEmployee_id(jadwal.getString(1));
            timeTables.setTimetable_id(jadwal.getString(2));
            timeTables.setInisial(jadwal.getString(3));
            timeTables.setHari(jadwal.getString(4));
            timeTables.setMasuk(jadwal.getString(5));
            timeTables.setPulang(jadwal.getString(6));
            timeTable.add(timeTables);
        }

        return timeTable;

    }

    public void singkronTimetable(){

        progressDialog = new ProgressDialog(getContext(), R.style.AppCompatAlertDialogStyle);
        progressDialog.setMessage("Sedang proses...");
        progressDialog.show();

        Call<List<TimeTebleSetting>> callKegiatan = holderAPI.getUrlTimeTableSetting("https://absensi.tebingtinggikota.go.id/api/timetable?employee_id="+sEmployee_id, "Bearer "+sToken);
        callKegiatan.enqueue(new Callback<List<TimeTebleSetting>>() {
            @Override
            public void onResponse(@NonNull Call<List<TimeTebleSetting>> call, @NonNull Response<List<TimeTebleSetting>> response) {
                List<TimeTebleSetting> timeTables = response.body();
                if (!response.isSuccessful()){
                    progressDialog.dismiss();
                    dialogView.viewNotifKosong(getContext(), "Gagal melakukan singkronisasi jadwal.","Silahkan coba kembali.");
                }

                Integer deleteTimeTable = databaseHelper.deleteTimeTable(sEmployee_id);
                if (deleteTimeTable > 0){
                    timeTable.clear();
                    timeTable = timeTables;
                }

                timeTables.size();
                for (TimeTebleSetting timeTable : timeTables){
                    databaseHelper.insertDataTimeTable(timeTable.getId(), timeTable.getEmployee_id(), timeTable.getTimetable_id(),
                            timeTable.getInisial(), timeTable.getHari(), timeTable.getMasuk(), timeTable.getPulang());
                }


                rvJadwalKegiatan.setLayoutManager(new GridLayoutManager(getActivity(), 1));
                SettingAdapter settingAdapter = new SettingAdapter(timeTables);
                rvJadwalKegiatan.setAdapter(settingAdapter);

                progressDialog.dismiss();
                viewSukses("Berhasil melakukan singkronisasi jadwal.","");

            }

            @Override
            public void onFailure(@NonNull Call<List<TimeTebleSetting>> call, @NonNull Throwable t) {

                progressDialog.dismiss();
                dialogView.viewNotifKosong(getContext(), "Gagal mengakses server.", "Silahkan coba kembali.");
            }
        });
    }

    public void handlerProgressDialog() {

        Handler handler = new Handler();
        handler.postDelayed(() -> {
            //your code here
            progressDialog.dismiss();
        }, 1500);
    }

    public void koordinatOPD(){
        progressDialog = new ProgressDialog(getContext(), R.style.AppCompatAlertDialogStyle);
        progressDialog.setMessage("Sedang proses...");
        progressDialog.show();

        Call<List<Koordinat>> calllokasi = holderAPI.getUrlKoordinat("https://absensi.tebingtinggikota.go.id/api/koordinat?opdid="+sOPD);
        calllokasi.enqueue(new Callback<List<Koordinat>>() {
            @Override
            public void onResponse(@NonNull Call<List<Koordinat>> call, @NonNull Response<List<Koordinat>> response) {
                List<Koordinat> koordinats = response.body();
                if (!response.isSuccessful()){
                    progressDialog.dismiss();
                    dialogView.viewNotifKosong(getContext(), "Gagal melakukan singkronisasi lokasi.","Silahkan coba kembali.");
                }

                Integer deleteKoordinatOPD = databaseHelper.deleteDataKoordinatOPD(sOPD);
                if (deleteKoordinatOPD > 0){
                    for (Koordinat koordinat : koordinats){
                        databaseHelper.insertDataKoordinat(koordinat.getId(), koordinat.getOpd_id(), koordinat.getAlamat(), koordinat.getLet(), koordinat.getLng());

                    }
                }

                progressDialog.dismiss();
                viewSukses("Berhasil melakukan singkronisasi lokasi.","");

            }

            @Override
            public void onFailure(@NonNull Call<List<Koordinat>> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                dialogView.viewNotifKosong(getContext(), "Gagal mengakses server.", "Silahkan coba kembali.");
            }
        });
    }

    public void koordintaEmployee(){
        Call<List<Koordinat>> callKegiatan = holderAPI.getUrlKoordinat("https://absensi.tebingtinggikota.go.id/api/koordinatemployee?id="+sEmployee_id);
        callKegiatan.enqueue(new Callback<List<Koordinat>>() {
            @Override
            public void onResponse(@NonNull Call<List<Koordinat>> call, @NonNull Response<List<Koordinat>> response) {
                List<Koordinat> koordinats = response.body();
                if (!response.isSuccessful()){
                    dialogView.viewNotifKosong(getContext(), "Gagal melakukan singkronisasi lokasi.","Silahkan coba kembali.");
                }

                Integer deleteKoordinatE = databaseHelper.deleteDataKoordinatEmployee(sEmployee_id);
                if (deleteKoordinatE > 0){

                    for (Koordinat koordinat : koordinats){
                        databaseHelper.insertDataKoordinatEmployee(koordinat.getId(), sEmployee_id, koordinat.getAlamat(), koordinat.getLet(), koordinat.getLng());

                    }

                }

            }

            @Override
            public void onFailure(@NonNull Call<List<Koordinat>> call, @NonNull Throwable t) {

                dialogView.viewNotifKosong(getContext(), "Gagal mengakses server.", "Silahkan coba kembali.");

            }
        });
    }

    public void singkronKegiatan(){
        progressDialog = new ProgressDialog(getContext(), R.style.AppCompatAlertDialogStyle);
        progressDialog.setMessage("Sedang proses...");
        progressDialog.show();

        Call<List<KegiatanIzin>> callKegiatan = holderAPI.getUrlKegiatanNew("https://absensi.tebingtinggikota.go.id/api/kegiatannew?opd="+sOPD);
        callKegiatan.enqueue(new Callback<List<KegiatanIzin>>() {
            @Override
            public void onResponse(@NonNull Call<List<KegiatanIzin>> call, @NonNull Response<List<KegiatanIzin>> response) {
                List<KegiatanIzin> kegiatanIzins = response.body();
                if (!response.isSuccessful()){
                    progressDialog.dismiss();
                    dialogView.viewNotifKosong(getContext(), "Gagal melakukan singkronisasi kegiatan.","Silahkan coba kembali.");
                }

                Integer deleteKegiatan = databaseHelper.deleteKegiatanIzin();
                if (deleteKegiatan>0){
                    for (KegiatanIzin kegiatanIzin : kegiatanIzins){
                        databaseHelper.insertResourceKegiatan(String.valueOf(kegiatanIzin.getId()), kegiatanIzin.getTipe(), kegiatanIzin.getKet());
                    }

                }
                progressDialog.dismiss();
                viewSukses("Berhasil melakukan singkronisasi kegiatan.","");

            }

            @Override
            public void onFailure(@NonNull Call<List<KegiatanIzin>> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                dialogView.viewNotifKosong(getContext(), "Gagal mengakses server.", "Silahkan coba kembali.");
            }
        });
    }
    String bulan = BULAN.format(new Date());
    String tahun = TAHUN.format(new Date());

//    public void getJadwalSift(){
//        progressDialog = new ProgressDialog(getContext(), R.style.AppCompatAlertDialogStyle);
//        progressDialog.setMessage("Sedang proses...");
//        progressDialog.show();
//
//        databaseHelper.deleteJadwalSift();
//        Call<List<JadwalSift>> jadwalSiftPegawai = holderAPI.getJadwalSift("https://absensi.tebingtinggikota.go.id/api/jadwalsift?ide="+sEmployee_id+"&bulan="+bulan+"&tahun="+tahun);
//        jadwalSiftPegawai.enqueue(new Callback<List<JadwalSift>>() {
//            @Override
//            public void onResponse(Call<List<JadwalSift>> call, Response<List<JadwalSift>> response) {
//                if (!response.isSuccessful()){
//                    progressDialog.dismiss();
//                    dialogView.viewNotifKosong(getContext(), "Gagal mengunduh Jadwal Sift.","Silahkan coba kembali.");
//                }
//
//                List<JadwalSift> jadwalSifts = response.body();
//
//                for(JadwalSift jadwalSift : jadwalSifts){
//                    databaseHelper.insertJadwalSift(jadwalSift.getId(), jadwalSift.getEmployee_id(), jadwalSift.getShift_id(), jadwalSift.getTanggal());
//                }
//                progressDialog.dismiss();
//
//                viewSukses("Berhasil mengunduh Jadwal Sift", "");
//
//            }
//
//            @Override
//            public void onFailure(Call<List<JadwalSift>> call, Throwable t) {
//                progressDialog.dismiss();
//                dialogView.viewNotifKosong(getContext(), "Gagal mengakses server.", "Silahkan coba kembali.");
//
//            }
//        });
//
//
//    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void viewSukses(String info1, String info2){

        Dialog dialogSukes = new Dialog(requireActivity(), R.style.DialogStyle);
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
        });

        tvTutupDialog.setOnClickListener(v -> {

            dialogSukes.dismiss();
        });

        dialogSukes.show();

    }
}
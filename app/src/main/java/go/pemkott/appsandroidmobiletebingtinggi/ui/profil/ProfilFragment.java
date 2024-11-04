package go.pemkott.appsandroidmobiletebingtinggi.ui.profil;

import static androidx.appcompat.app.AppCompatActivity.RESULT_CANCELED;
import static go.pemkott.appsandroidmobiletebingtinggi.konstanta.AmbilFoto.prepareFilePart;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import go.pemkott.appsandroidmobiletebingtinggi.MainActivity;
import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.api.HttpService;
import go.pemkott.appsandroidmobiletebingtinggi.api.RetrofitBuilder;
import go.pemkott.appsandroidmobiletebingtinggi.database.DatabaseHelper;
import go.pemkott.appsandroidmobiletebingtinggi.databinding.FragmentProfilBinding;
import go.pemkott.appsandroidmobiletebingtinggi.dialogview.DialogView;
import go.pemkott.appsandroidmobiletebingtinggi.konstanta.AmbilFoto;
import go.pemkott.appsandroidmobiletebingtinggi.login.LoginActivity;
import go.pemkott.appsandroidmobiletebingtinggi.login.Logout;
import go.pemkott.appsandroidmobiletebingtinggi.model.FileModel;
import go.pemkott.appsandroidmobiletebingtinggi.panduanabsensi.PanduanActivity;
import go.pemkott.appsandroidmobiletebingtinggi.ui.profil.password.UpdatePasswordActivity;
import go.pemkott.appsandroidmobiletebingtinggi.utils.ClsGlobal;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProfilFragment extends Fragment {

    private FragmentProfilBinding binding;

    ImageView ivProfile, ivKeluarProfile, addImgProfile;
    RelativeLayout backHome;
    HttpService holderAPI;
    DatabaseHelper databaseHelper;
    String sNama, sNIP, sEmail, sNoHpEmployee, sKelompok, sJabatan, sKantor, sKeluar, sOPD;
    TextView tvNamaEmployee, tvNipEmploy, tvEmailEmploy, tvNoHpEmploye, tvKelompokEmploy, tvJabatanEmploy, tvDinasEmploy;
    String fotoProfileUpdate;

    String sEmployee_id, sAkses, sActive, sToken, fotoProfile;
    ProgressDialog progressDialog;
    TableRow trGantiPassword, trPanduanAplikasi;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentProfilBinding.inflate(inflater, container, false);
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

        tvNamaEmployee = binding.tvNamaEmployee;
        tvNipEmploy = binding.tvNipEmploy;
        tvEmailEmploy = binding.tvEmailEmploy;
        tvNoHpEmploye = binding.tvNoHpEmploye;
        tvKelompokEmploy = binding.tvKelompokEmploy;
        tvJabatanEmploy = binding.tvJabatanEmploy;
        tvDinasEmploy = binding.tvDinasEmploy;
        ivKeluarProfile = binding.ivKeluarProfile;
        trGantiPassword = binding.trGantiPassword;
        ivProfile = binding.ivProfile;
        backHome = binding.backHome;
        addImgProfile = binding.addImgProfile;
        trPanduanAplikasi = binding.trPanduanAplikasi;

        trPanduanAplikasi.setOnClickListener(v -> startActivity(new Intent(requireActivity(), PanduanActivity.class)));

        dataEmployee();
        backHome.setOnClickListener(v -> {
            Intent i = new Intent(getContext(), MainActivity.class);
            startActivity(i);
            requireActivity().finish();
        });

        tvNamaEmployee.setText(sNama.toUpperCase());
        tvNipEmploy.setText("NIP. "+sNIP);
        tvEmailEmploy.setText(sEmail);
        tvNoHpEmploye.setText(sNoHpEmployee);
        tvKelompokEmploy.setText(sKelompok);
        tvJabatanEmploy.setText(sJabatan);
        tvDinasEmploy.setText(sKantor);

        if (fotoProfile.equals("null")){
            Glide.with(this)
                    .load( R.drawable.profilpics )
                    .into( ivProfile );
        }else{
            Glide.with(this)
                    .load( "https://absensi.tebingtinggikota.go.id/storage/" +fotoProfile )
                    .into( ivProfile );
        }


        ivKeluarProfile.setOnClickListener(v -> viewKeluar());


        trGantiPassword.setOnClickListener(v -> {
            Intent iResetPass = new Intent(getActivity(), UpdatePasswordActivity.class);
            startActivity(iResetPass);
        });

        addImgProfile.setOnClickListener(v -> {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("Update Foto Profil...");
            progressDialog.show();
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent = Intent.createChooser(intent,"Pilih gambar...");
            startActivityForResult(intent, 33);
        });
        return root;
    }

    AmbilFoto ambilFoto = new AmbilFoto(getActivity());
    List<Uri> fileFotoProfil = new ArrayList<>();

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED){
            if (requestCode == 33 && resultCode == Activity.RESULT_OK && data != null){
                requestPermission();
                fileFotoProfil.clear();

                Uri pdfUri = data.getData();
                String FilePath2 = ClsGlobal.getPathFromUri(getActivity(), pdfUri);
                File file1 = new File(FilePath2);
                Bitmap bitmap = ambilFoto.fileBitmap(file1);
                Bitmap rotationBitmap = Bitmap.createBitmap(bitmap, 0,0, bitmap.getWidth(), bitmap.getHeight(), AmbilFoto.exifInterface(FilePath2), true);

                fileFotoProfil.add(Uri.parse(FilePath2));
                ivProfile.setImageBitmap(rotationBitmap);
                uploadCuti();
                handlerProgressDialog(2);
            }

        }else{
            progressDialog.dismiss();
        }
    }

    DialogView dialogView = new DialogView(getActivity());
    public void uploadCuti(){

        List<MultipartBody.Part> list = new ArrayList<>();
        for(Uri uri: fileFotoProfil){
            list.add(prepareFilePart("foto_profil[]", uri));
        }
        HttpService service = RetrofitBuilder.getClient().create(HttpService.class);
        Call<FileModel> call = service.callUpdateProfil("https://absensi.tebingtinggikota.go.id/api/updateprofil?"+"&employee_id="+sEmployee_id,
                list,
                "Bearer "+sToken
        );

        call.enqueue(new Callback<FileModel>() {
            @Override
            public void onResponse(@NonNull Call<FileModel> call, @NonNull Response<FileModel> response) {

                FileModel models = response.body();

                assert models != null;
                if (models.getStatus()){
                    fotoProfileUpdate = models.getId();
                    databaseHelper.updateDataEmployee(sEmployee_id, fotoProfileUpdate);

                    dialogView.viewSukses(getActivity(),"Update foto profil anda", "telah berhasil...");

                }else {
                    Toast.makeText(getActivity(), models.getMessage()+"", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<FileModel> call, @NonNull Throwable t) {
                Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    private void requestPermission(){
        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            int REQUEST_CODE_ASK_PERMISSIONS = 123;
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_ASK_PERMISSIONS);
        }
    }

    public void btnKeluar(){
        progressDialog = new ProgressDialog(getContext(), R.style.AppCompatAlertDialogStyle);
        progressDialog.setMessage("Keluar...");

        progressDialog.show();

        databaseHelper.deleteDataUseAll();
        databaseHelper.deleteDataEmployeeAll();
        databaseHelper.deleteDataKoordinatOPDAll();
        databaseHelper.deleteKegiatanIzin();
        databaseHelper.deleteTimeTableAll();
        databaseHelper.deleteDataKoordinatEmployeeAll();
        databaseHelper.deleteJamSift();
        databaseHelper.deleteJadwalSift2();

        handlerProgressDialog(1);
    }


    public void viewKeluar(){
        Dialog keluarDialog = new Dialog(getActivity(), R.style.DialogStyle);
        keluarDialog.setContentView(R.layout.view_keluar);
        keluarDialog.setCancelable(false);

        TextView txtBatalKeluar = keluarDialog.findViewById(R.id.txtBatalKeluar);
        TextView txtKeluar = keluarDialog.findViewById(R.id.txtKeluar);

        txtKeluar.setOnClickListener(v -> {
            keluarDialog.dismiss();
            btnKeluar();
        });

        txtBatalKeluar.setOnClickListener(v -> keluarDialog.dismiss());
        keluarDialog.show();

    }


    private void keluars(){
        Call<List<Logout>> call_login = holderAPI.getUrlLogout("https://absensi.tebingtinggikota.go.id/api/logout", "Bearer "+sToken);
        call_login.enqueue(new Callback<List<Logout>>() {
            @Override
            public void onResponse(@NonNull Call<List<Logout>> call, @NonNull Response<List<Logout>> response) {

                List<Logout> logins = response.body();
                if (!response.isSuccessful()){
                    return;
                }
                for (Logout login : logins){

                    sKeluar = login.getMessage();
                }

            }

            @Override
            public void onFailure(@NonNull Call<List<Logout>> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Gagal 5", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void handlerProgressDialog(int a){
        if (a == 2){
            Handler handler = new Handler();
            handler.postDelayed(() -> {
                //your code here
                progressDialog.dismiss();
            }, 500);


        }else if (a == 1){
            Handler handler = new Handler();
            handler.postDelayed(() -> {
                //your code here
                progressDialog.dismiss();
                getActivity().finish();
                Intent loginActivity = new Intent(getContext(), LoginActivity.class);
                startActivity(loginActivity);
            }, 2000);
        }else if (a == 3){
            Handler handler = new Handler();
            handler.postDelayed(() -> {
                //your code here
                progressDialog.dismiss();
            }, 500);

        }

    }

    public void dataEmployee(){

        Cursor res = databaseHelper.getAllData22();

        while (res.moveToNext()){
            sEmployee_id = res.getString(1);
            sAkses = res.getString(3);
            sActive = res.getString(4);
            sToken = res.getString(5);
        }

        Cursor resa = databaseHelper.getDataEmployee(sEmployee_id);

        while (resa.moveToNext()){

            sNIP = resa.getString(5);
            sNama = resa.getString(6);
            sEmail = resa.getString(7);
            sNoHpEmployee = resa.getString(8);
            sOPD = resa.getString(4);

            if (!resa.getString(9).equals("pns")){
                sKelompok = "Non-PNS";
            }else{
                sKelompok = "Pegawai Negeri Sipil";
            }

            sJabatan = resa.getString(12);
            sKantor = resa.getString(13);
            fotoProfile = resa.getString(17);
            fotoProfileUpdate = resa.getString(17);

        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
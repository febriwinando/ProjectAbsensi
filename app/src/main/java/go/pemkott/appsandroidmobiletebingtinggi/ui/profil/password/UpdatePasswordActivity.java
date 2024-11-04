package go.pemkott.appsandroidmobiletebingtinggi.ui.profil.password;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.api.HttpService;
import go.pemkott.appsandroidmobiletebingtinggi.database.DatabaseHelper;
import go.pemkott.appsandroidmobiletebingtinggi.dialogview.DialogView;
import go.pemkott.appsandroidmobiletebingtinggi.model.Updatep;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class UpdatePasswordActivity extends AppCompatActivity {
    DatabaseHelper databaseHelper;
    EditText etPasswordSebelum, etNewPassword, etEntryNewPassword;
    String sId, sEmployee_id, sAkses, sActive, sToken, sVerifikator;
    HttpService holderAPI;
    ProgressDialog progressDialog;

    Button btnGantiPass;
    TextView statuspass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_password);

//        Window window = this.getWindow();
//        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//        window.setStatusBarColor(getResources().getColor(R.color.biru_1));

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://absensi.tebingtinggikota.go.id/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        holderAPI = retrofit.create(HttpService.class);

        databaseHelper = new DatabaseHelper(this);
        datauser();

        etPasswordSebelum = findViewById(R.id.etPasswordSebelum);
        etNewPassword = findViewById(R.id.etNewPassword);
        etEntryNewPassword = findViewById(R.id.etEntryNewPassword);
        statuspass=findViewById(R.id.statuspass);
        btnGantiPass=findViewById(R.id.btnGantiPass);

        etEntryNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!charSequence.toString().trim().isEmpty()){
                    if (etNewPassword.getText().toString().trim().equals(etEntryNewPassword.getText().toString().trim())){
                        statuspass.setVisibility(View.GONE);
                    }else{
                        statuspass.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }



    DialogView dialogView = new DialogView(this);
    public void gantipass(View view){

        if (etPasswordSebelum.getText().toString().trim().isEmpty() || etNewPassword.getText().toString().trim().isEmpty() || etEntryNewPassword.getText().toString().trim().isEmpty())
        {
            dialogView.viewNotifKosong(UpdatePasswordActivity.this, "Password tidak boleh kosong,", "periksa kembali.");

        }else{
            updated();
        }

//        StringRequest request = new StringRequest(Request.Method.POST, "https://absensi.tebingtinggikota.go.id/api/gantipass", new Response.Listener<String>() {
//            @Override
//            public void onResponse(String response) {
//                if (!response.equals(null)) {
//                    Log.e("Your Array Response", response);
//                } else {
//                    Log.e("Your Array Response", "Data Null");
//                }
//            }
//
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Log.e("error is ", "" + error);
//            }
//        }) {
//
//            //This is for Headers If You Needed
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> params = new HashMap<String, String>();
//                params.put("Content-Type", "application/json; charset=UTF-8");
//                params.put("Authorization", "Bearer"+ sToken);
//
//                return params;
//            }
//
//            //Pass Your Parameters here
//            @Override
//            protected Map<String, String> getParams() {
//                Map<String, String> params = new HashMap<String, String>();
//                params.put("current_password", etPasswordSebelum.getText().toString().trim());
//                params.put("new_password", etNewPassword.getText().toString().trim());
//                params.put("new_confirm_password", etEntryNewPassword.getText().toString().trim());
//
//                return params;
//            }
//        };
//        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
//        queue.add(request);
    }

    public void backSetting(View view){
        finish();
    }

    public void datauser(){
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
    }

    public void updated(){

        progressDialog = new ProgressDialog(UpdatePasswordActivity.this);
        progressDialog.setMessage("Sedang proses...");
        progressDialog.show();

        Call<List<Updatep>> update = holderAPI.getUrlUpdatep("https://absensi.tebingtinggikota.go.id/api/updatep?"
                +"employee_id="+sEmployee_id
                +"&old="+etPasswordSebelum.getText()
                +"&new="+etNewPassword.getText()
        );
        update.enqueue(new Callback<List<Updatep>>() {
            @Override
            public void onResponse(Call<List<Updatep>> call, retrofit2.Response<List<Updatep>> response) {
                List<Updatep> updateps = response.body();
                if (!response.isSuccessful()){
                    handlerProgressDialog();
                    return;
                }


                for (Updatep updatep : updateps){
                    if (updatep.isStatus() == true){
                        viewSukses(UpdatePasswordActivity.this, "Password telah berhasil diganti","");
                    }else{
                        dialogView.viewNotifKosong(UpdatePasswordActivity.this, "Password lama tidak sesuai,", "periksa kembali.");
                    }
                }

                handlerProgressDialog();

            }

            @Override
            public void onFailure(Call<List<Updatep>> call, Throwable t) {

                handlerProgressDialog();
                Toast.makeText(UpdatePasswordActivity.this, "Gagal Melakukan Melakukan Reset Password.", Toast.LENGTH_SHORT).show();
            }
        });
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
        etEntryNewPassword.setText("");
        etNewPassword.setText("");
        etPasswordSebelum.setText("");

        kembaliDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialogSukes.dismiss();
            }
        });

        tvTutupDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogSukes.dismiss();
            }
        });

        dialogSukes.show();

    }

    public void handlerProgressDialog() {

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                //your code here
                progressDialog.dismiss();
            }
        }, 1500);
    }

}
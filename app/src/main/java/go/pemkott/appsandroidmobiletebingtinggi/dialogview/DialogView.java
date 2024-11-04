package go.pemkott.appsandroidmobiletebingtinggi.dialogview;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.imageview.ShapeableImageView;

import go.pemkott.appsandroidmobiletebingtinggi.R;

public class DialogView {

    Context context;

    public DialogView(Context context) {
        this.context = context;
    }

    public void viewNotifKosong(Context context, String w1, String w2){
        Dialog dataKosong = new Dialog(context, R.style.DialogStyle);
        dataKosong.setContentView(R.layout.view_warning_kosong);
        TextView tvWarning1 = dataKosong.findViewById(R.id.tvWarning1);
        TextView tvWarning2 = dataKosong.findViewById(R.id.tvWarning2);
        TextView tvTutupDialog = dataKosong.findViewById(R.id.tvTutupDialog);

        tvWarning1.setText(w1);
        tvWarning2.setText(w2);
        dataKosong.setCancelable(true);
        tvTutupDialog.setOnClickListener(v -> dataKosong.dismiss());

        dataKosong.show();
    }


    public void viewErrorKosong(Context context){
        Dialog dataKosong = new Dialog(context, R.style.DialogStyle);
        dataKosong.setContentView(R.layout.view_empty);
        dataKosong.setCancelable(true);
        dataKosong.show();
    }

    public int viewSukses(Context context, String txt1, String txt2){
        Dialog dialogSukes = new Dialog(context, R.style.DialogStyle);
        dialogSukes.setContentView(R.layout.view_sukses);
        dialogSukes.setCancelable(true);
        TextView tvTutupDialog = dialogSukes.findViewById(R.id.tvTutupDialog);
        TextView kembaliDialog = dialogSukes.findViewById(R.id.kembaliDialog);
        TextView tvInfo1 = dialogSukes.findViewById(R.id.tvInfo1);
        TextView tvInfo2 = dialogSukes.findViewById(R.id.tvInfo2);
        tvInfo1.setText(txt1);
        tvInfo2.setText(txt2);
        final int[] tutup = {0};
        kembaliDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tutup[0] = 1;
                dialogSukes.dismiss();
            }
        });

        tvTutupDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tutup[0] = 1;
                dialogSukes.dismiss();
            }
        });

        dialogSukes.show();
        return tutup[0];

    }

    public void viewFoto(Bitmap data){
        Dialog dialogFoto = new Dialog(context, R.style.DialogStyle);
        dialogFoto.setContentView(R.layout.item_view_photo);
        dialogFoto.setCancelable(true);

        ShapeableImageView imageView = dialogFoto.findViewById(R.id.ivFoto);
        TextView tvTutupDialog = dialogFoto.findViewById(R.id.tvCloseViewFoto);

        imageView.setImageBitmap(data);

        tvTutupDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogFoto.dismiss();
            }
        });

        dialogFoto.show();
    }

    public void pesanError(Context context){

        Dialog errorDialogs = new Dialog(context, R.style.DialogStyle);
        errorDialogs.setContentView(R.layout.view_error);
        TextView tvTutupDialog = errorDialogs.findViewById(R.id.tvTutupDialog);

        tvTutupDialog.setOnClickListener(v -> errorDialogs.dismiss());

        errorDialogs.show();

//        Handler handler = new Handler();
//        handler.postDelayed(errorDialogs::dismiss, 2000);

    }


}

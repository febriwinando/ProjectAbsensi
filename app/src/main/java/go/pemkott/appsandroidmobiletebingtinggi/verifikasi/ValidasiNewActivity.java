package go.pemkott.appsandroidmobiletebingtinggi.verifikasi;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.verifikasi.fragvalidasi.masuk.MasukFragment;
import go.pemkott.appsandroidmobiletebingtinggi.verifikasi.fragvalidasi.pulang.PulangFragment;

public class ValidasiNewActivity extends AppCompatActivity {
    FrameLayout contentView;
    ProgressBar loadingView;
    BottomNavigationView menuvalidasi;
    MasukFragment masukFragment = new MasukFragment();
    PulangFragment pulangFragment = new PulangFragment();
    public static BottomNavigationView bottomNavigationView;
    private int shortAnimationDuration;

    public static AppCompatActivity validasi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_validasi_new);

        validasi = this;
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        window.setStatusBarColor(this.getResources().getColor(R.color.putih));
//            }


        contentView = findViewById(R.id.containervalidasi);
        loadingView = findViewById(R.id.loading_spinner_validasi);
        menuvalidasi = findViewById(R.id.menuvalidasi);
        shortAnimationDuration = 1000;
        crossfade();
        getSupportFragmentManager().beginTransaction().replace(R.id.containervalidasi, masukFragment).commit();

        menuvalidasi.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.masukvalidasi:
                        getSupportFragmentManager().beginTransaction().replace(R.id.containervalidasi, masukFragment).commit();
                        return true;
                    case R.id.pulangvalidasi:
                        getSupportFragmentManager().beginTransaction().replace(R.id.containervalidasi, pulangFragment).commit();
                        return true;
                }

                return false;
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void crossfade() {
        contentView.setAlpha(0f);
        contentView.setVisibility(View.VISIBLE);


        contentView.animate()
                .alpha(1f)
                .setDuration(shortAnimationDuration)
                .setListener(null);

        loadingView.animate()
                .alpha(0f)
                .setDuration(shortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        loadingView.setVisibility(View.GONE);
                    }
                });
    }

}
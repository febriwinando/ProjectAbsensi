package go.pemkott.appsandroidmobiletebingtinggi.ui.jadwal;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class JadwalViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public JadwalViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Jadwal");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
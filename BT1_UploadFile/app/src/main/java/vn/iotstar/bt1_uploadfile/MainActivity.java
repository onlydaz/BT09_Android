package vn.iotstar.bt1_uploadfile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView imgProfile = findViewById(R.id.imgProfile);
        imgProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, UploadFileActivity.class);
            startActivity(intent);
        });
    }
}
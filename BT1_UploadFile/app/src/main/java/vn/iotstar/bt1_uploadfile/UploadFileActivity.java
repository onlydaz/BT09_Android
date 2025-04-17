package vn.iotstar.bt1_uploadfile;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadFileActivity extends AppCompatActivity {

    // Các biến UI
    Button btnChoose, btnUpload;
    ImageView imageViewChoose, imageViewUpload;
    private Uri mUri;
    private ProgressDialog mProgressDialog;
    TextView textViewUsername;
    EditText editUserName;  // EditText cho tên người dùng

    public static final int MY_REQUEST_CODE = 100;
    public static final String TAG = UploadFileActivity.class.getName();

    // Các quyền cần thiết
    public static String[] storage_permissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_file);

        AnhXa(); // Ánh xạ các thành phần UI
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Please wait, uploading...");

        // Xử lý sự kiện cho nút "Chọn ảnh"
        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermission(); // Kiểm tra quyền truy cập bộ nhớ
            }
        });

        // Xử lý sự kiện cho nút "Tải lên ảnh"
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUri != null) {
                    uploadImage(); // Nếu có ảnh, thực hiện tải lên
                }
            }
        });

        // Sự kiện quay lại màn hình trước
        TextView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Kết thúc activity và quay lại MainActivity
            }
        });
    }

    private void AnhXa() {
        btnChoose = findViewById(R.id.btnChoose);
        btnUpload = findViewById(R.id.btnUpload);
        imageViewChoose = findViewById(R.id.imgChoose);
        imageViewUpload = findViewById(R.id.imgAvatar);
        textViewUsername = findViewById(R.id.tvUsername);
        editUserName = findViewById(R.id.editUserName);  // Ánh xạ EditText
    }

    // Kiểm tra quyền truy cập bộ nhớ
    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES}, MY_REQUEST_CODE);
            } else {
                openGallery();
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_REQUEST_CODE);
            } else {
                openGallery();
            }
        } else {
            openGallery();
        }
    }


    // Mở thư viện ảnh
    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        mActivityResultLauncher.launch(Intent.createChooser(intent, "Select Picture"));
    }

    // Xử lý kết quả trả về từ thư viện ảnh
    private ActivityResultLauncher<Intent> mActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data == null) {
                            return;
                        }
                        Uri uri = data.getData();
                        mUri = uri;
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                            imageViewChoose.setImageBitmap(bitmap); // Hiển thị ảnh đã chọn
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

    // Hàm upload ảnh lên server
    private void uploadImage() {
        mProgressDialog.show();

        String username = editUserName.getText().toString(); // Lấy tên người dùng từ EditText
        if (username.isEmpty()) {
            Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show();
            mProgressDialog.dismiss();
            return;
        }

        RequestBody requestUsername = RequestBody.create(MediaType.parse("multipart/form-data"), username);

        // Convert URI thành file
        String IMAGE_PATH = RealPathUtil.getRealPath(this, mUri);
        File file = new File(IMAGE_PATH);
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);

        // Tạo MultipartBody.Part để gửi ảnh lên server
        MultipartBody.Part partAvatar = MultipartBody.Part.createFormData("my_image", file.getName(), requestFile);

        // Gọi Retrofit API
        ServiceAPI.serviceAPI.upload(requestUsername, partAvatar).enqueue(new Callback<List<ImageUpload>>() {
            @Override
            public void onResponse(Call<List<ImageUpload>> call, Response<List<ImageUpload>> response) {
                mProgressDialog.dismiss();
                Log.d(TAG, "onResponse: code = " + response.code());

                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    ImageUpload imageUpload = response.body().get(0);
                    Log.d(TAG, "onResponse: Upload success: " + imageUpload.getAvatar());

                    textViewUsername.setText(imageUpload.getUsername());
                    Glide.with(UploadFileActivity.this)
                            .load(imageUpload.getAvatar())
                            .into(imageViewUpload);

                    Toast.makeText(UploadFileActivity.this, "Upload successful", Toast.LENGTH_LONG).show();
                } else {
                    Log.e(TAG, "onResponse: Upload failed - response body is null or empty");

                    try {
                        if (response.errorBody() != null) {
                            Log.e(TAG, "onResponse errorBody: " + response.errorBody().string());
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "onResponse: errorBody read failed", e);
                    }

                    Toast.makeText(UploadFileActivity.this, "Upload failed - check server response", Toast.LENGTH_SHORT).show();
                }
            }


            @Override
            public void onFailure(Call<List<ImageUpload>> call, Throwable t) {
                mProgressDialog.dismiss();
                Toast.makeText(UploadFileActivity.this, "API call failed", Toast.LENGTH_LONG).show();
            }
        });
    }

    // Hàm xử lý khi yêu cầu quyền bị từ chối hoặc chấp nhận
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery(); // Mở gallery khi quyền được cấp
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

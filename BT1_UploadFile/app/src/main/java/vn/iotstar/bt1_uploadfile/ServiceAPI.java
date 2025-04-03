package vn.iotstar.bt1_uploadfile;

import android.os.Message;

import java.util.List;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ServiceAPI {
    String BASE_URL = "http://app.iotstar.vn/appfoods/";

    // Khởi tạo Retrofit
    ServiceAPI serviceAPI = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ServiceAPI.class);

    // API Upload 1 (Danh sách ảnh)
    @Multipart
    @POST("upload.php")
    Call<List<ImageUpload>> upload(
            @Part(Const.MY_USERNAME) RequestBody username,
            @Part MultipartBody.Part avatar
    );

    // API Upload 2 (Trả về thông báo)
    @Multipart
    @POST("upload1.php")
    Call<Message> upload1(
            @Part(Const.MY_USERNAME) RequestBody username,
            @Part MultipartBody.Part avatar
    );
}

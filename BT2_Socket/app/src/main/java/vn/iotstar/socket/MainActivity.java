package vn.iotstar.socket;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    Button btnPaired;
    ListView listDanhSach;
    private static final int REQUEST_BLUETOOTH = 1;
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 2;

    // Bluetooth
    private BluetoothAdapter myBluetooth = null;
    private Set<BluetoothDevice> pairedDevices;
    public static String EXTRA_ADDRESS = "device_address";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ánh xạ
        btnPaired = findViewById(R.id.btnTimthietbi);
        listDanhSach = findViewById(R.id.listTb);

        // Kiểm tra thiết bị có Bluetooth
        myBluetooth = BluetoothAdapter.getDefaultAdapter();
        if (myBluetooth == null) {
            // Thiết bị không hỗ trợ Bluetooth
            Toast.makeText(getApplicationContext(), "Thiết bị không hỗ trợ Bluetooth", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Kiểm tra và yêu cầu quyền BLUETOOTH_CONNECT
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_BLUETOOTH_PERMISSIONS);
        } else {
            // Quyền đã được cấp, kiểm tra trạng thái Bluetooth
            checkBluetoothState();
        }

        // Thực hiện tìm thiết bị
        btnPaired.setOnClickListener(v -> pairedDevicesList());
    }

    @SuppressLint("MissingPermission")
    private void checkBluetoothState() {
        if (!myBluetooth.isEnabled()) {
            // Yêu cầu bật Bluetooth
            Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnBTon, REQUEST_BLUETOOTH);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Quyền được cấp, kiểm tra trạng thái Bluetooth
                checkBluetoothState();
            } else {
                // Quyền bị từ chối
                Toast.makeText(getApplicationContext(), "Cần quyền Bluetooth để sử dụng ứng dụng", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    // Tìm danh sách các thiết bị đã ghép đôi
    private void pairedDevicesList() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "Cần quyền Bluetooth để tìm thiết bị", Toast.LENGTH_LONG).show();
            return;
        }

        pairedDevices = myBluetooth.getBondedDevices();
        ArrayList<String> list = new ArrayList<>();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice bt : pairedDevices) {
                list.add(bt.getName() + "\n" + bt.getAddress()); // Lấy tên và địa chỉ MAC của thiết bị
            }
        } else {
            Toast.makeText(getApplicationContext(), "Không tìm thấy thiết bị kết nối.", Toast.LENGTH_LONG).show();
        }

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
        listDanhSach.setAdapter(adapter);
        listDanhSach.setOnItemClickListener(myListClickListener); // Gọi hàm khi người dùng chọn thiết bị
    }

    // Hàm xử lý khi người dùng chọn thiết bị trong danh sách
    private AdapterView.OnItemClickListener myListClickListener = (av, v, arg2, arg3) -> {
        // Lấy địa chỉ MAC của thiết bị đã chọn
        String info = ((TextView) v).getText().toString();
        String address = info.substring(info.length() - 17); // Lấy địa chỉ MAC

        // Mở màn hình điều khiển
        Intent i = new Intent(MainActivity.this, ControlActivity.class);
        i.putExtra(EXTRA_ADDRESS, address); // Gửi địa chỉ MAC vào ControlActivity
        startActivity(i);
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_BLUETOOTH) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(getApplicationContext(), "Bluetooth đã được bật", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Cần bật Bluetooth để sử dụng ứng dụng", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}
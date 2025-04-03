package vn.iotstar.bt2_socket;

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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    Button btnPaired;
    ListView listDanhSach;
    public static int REQUEST_BLUETOOTH = 1;

    //Bluetooth
    private BluetoothAdapter myBluetooth = null;
    private Set<BluetoothDevice> pairedDevices;
    public static String EXTRA_ADDRESS = "device_address";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ánh xạ
        btnPaired = (Button) findViewById(R.id.btnTimthietbi);
        listDanhSach = (ListView) findViewById(R.id.listTb);

        // Kiểm tra thiết bị có Bluetooth
        myBluetooth = BluetoothAdapter.getDefaultAdapter();
        if (myBluetooth == null) {
            // Thiết bị không hỗ trợ Bluetooth
            Toast.makeText(getApplicationContext(), "Thiết bị Bluetooth chưa bật", Toast.LENGTH_LONG).show();
            finish();
        } else if (!myBluetooth.isEnabled()) {
            // Yêu cầu bật Bluetooth
            Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "Thiết bị Bluetooth chưa bật", Toast.LENGTH_LONG).show();
            }
            startActivityForResult(turnBTon, REQUEST_BLUETOOTH);
        }

        // Thực hiện tìm thiết bị
        btnPaired.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pairedDevicesList(); // Gọi hàm tìm thiết bị
            }
        });
    }

    // Tìm danh sách các thiết bị đã ghép đôi
    private void pairedDevicesList() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
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
    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Lấy địa chỉ MAC của thiết bị đã chọn
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17); // Lấy địa chỉ MAC

            // Mở màn hình điều khiển
            Intent i = new Intent(MainActivity.this, BlueControl.class);
            i.putExtra(EXTRA_ADDRESS, address); // Gửi địa chỉ MAC vào BlueControl Activity
            startActivity(i);
        }
    };
}

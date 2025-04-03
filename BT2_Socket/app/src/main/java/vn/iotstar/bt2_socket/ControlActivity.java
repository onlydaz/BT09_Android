package vn.iotstar.bt2_socket;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.util.UUID;

public class ControlActivity extends AppCompatActivity {
    ImageButton btnTb1, btnTb2, btnDis;
    TextView txt1, txtMAC;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    String address = null;
    private ProgressDialog progress;
    int flaglamp1 = 0, flaglamp2 = 0;

    // UUID cho SPP
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent newint = getIntent();
        address = newint.getStringExtra(MainActivity.EXTRA_ADDRESS); // Nhận địa chỉ Bluetooth từ MainActivity
        setContentView(R.layout.activity_control);

        // Ánh xạ
        btnTb1 = (ImageButton) findViewById(R.id.btnTb1);
        btnTb2 = (ImageButton) findViewById(R.id.btnTb2);
        txt1 = (TextView) findViewById(R.id.textV1);
        txtMAC = (TextView) findViewById(R.id.textViewMAC);
        btnDis = (ImageButton) findViewById(R.id.btnDisc);

        // Kết nối Bluetooth
        new ConnectBT().execute();

        // Xử lý sự kiện nhấn nút
        btnTb1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thietTbil(); // Gọi hàm điều khiển thiết bị 1
            }
        });
        btnTb2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thietTbi7(); // Gọi hàm điều khiển thiết bị 7
            }
        });
        btnDis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Disconnect(); // Ngắt kết nối Bluetooth
            }
        });
    }

    // Hàm điều khiển thiết bị 1
    private void thietTbil() {
        if (btSocket != null) {
            try {
                if (flaglamp1 == 0) {
                    flaglamp1 = 1;
                    btnTb1.setBackgroundResource(R.drawable.tb1on);
                    btSocket.getOutputStream().write("1".toString().getBytes());
                    txt1.setText("Thiết bị số 1 đang bật");
                } else {
                    flaglamp1 = 0;
                    btnTb1.setBackgroundResource(R.drawable.tb1off);
                    btSocket.getOutputStream().write("A".toString().getBytes());
                    txt1.setText("Thiết bị số 1 đang tắt");
                }
            } catch (IOException e) {
                msg("Lỗi");
            }
        }
    }

    // Hàm điều khiển thiết bị 7
    private void thietTbi7() {
        if (btSocket != null) {
            try {
                if (flaglamp2 == 0) {
                    flaglamp2 = 1;
                    btnTb2.setBackgroundResource(R.drawable.tb7on);
                    btSocket.getOutputStream().write("7".toString().getBytes());
                    txt1.setText("Thiết bị số 7 đang bật");
                } else {
                    flaglamp2 = 0;
                    btnTb2.setBackgroundResource(R.drawable.tb7off);
                    btSocket.getOutputStream().write("G".toString().getBytes());
                    txt1.setText("Thiết bị số 7 đang tắt");
                }
            } catch (IOException e) {
                msg("Lỗi");
            }
        }
    }

    // Hàm ngắt kết nối
    private void Disconnect() {
        if (btSocket != null) {
            try {
                btSocket.close();
            } catch (IOException e) {
                msg("Lỗi");
            }
        }
        finish();
    }

    // Kết nối Bluetooth
    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        boolean ConnectSuccess = true;

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(ControlActivity.this, "Đang kết nối...", "Xin vui lòng đợi!!!");
        }

        @Override
        protected Void doInBackground(Void... devices) {
            try {
                if (btSocket == null || !isBtConnected) {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();
                }
            } catch (IOException e) {
                ConnectSuccess = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (!ConnectSuccess) {
                msg("Kết nối thất bại! Kiểm tra thiết bị.");
                finish();
            } else {
                msg("Kết nối thành công.");
                isBtConnected = true;
                txtMAC.setText(address);
            }
            progress.dismiss();
        }
    }

    // Hiển thị thông báo
    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }
}
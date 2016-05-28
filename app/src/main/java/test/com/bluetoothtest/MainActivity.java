package test.com.bluetoothtest;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;

import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

@SuppressWarnings("SpellCheckingInspection")
public class MainActivity extends Activity {
    private BluetoothAdapter bluetoothAdapter;
    private TextView textView;
    private ArrayList<BluetoothDevice> devices = new ArrayList<>();
    private ListView list;
    private MyAdapter mAdapter;
    private MyBluetoothReceiver receiver;
    private OutputStream outputStream;
    private Handler handler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.text);
        list = (ListView) findViewById(R.id.listview);
        mAdapter = new MyAdapter();
        list.setAdapter(mAdapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                textView.setText(devices.get(position).getName());
                //建立连接
                BluetoothDevice device = (BluetoothDevice) mAdapter.getItem(position);
                connectDevice(device);
            }
        });
        receiver = new MyBluetoothReceiver();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            textView.setText("未找到蓝牙设备");
        }
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
//        if (bluetoothAdapter.enable()) {
//            textView.setText("蓝牙已打卡");
//        }
        Button open = (Button) findViewById(R.id.openbluetooth);
        open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {

                    bluetoothAdapter.enable();
                    textView.setText("打开蓝牙");
                }
            }
        });
        Button close = (Button) findViewById(R.id.close_blurtooth);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
                    //关闭蓝牙
                    bluetoothAdapter.disable();
                    textView.setText("关闭蓝牙");
                }
            }
        });

        Button scan = (Button) findViewById(R.id.blurtooth_scan);
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bluetoothAdapter != null) {
                    textView.setText("开始扫描");
                    devices.clear();
                    bluetoothAdapter.startDiscovery();
                    if (mAdapter != null) {
                        mAdapter.notifyDataSetChanged();
                    }
                }

            }
        });


        Button cancle_scan = (Button) findViewById(R.id.cancle_scan);
        cancle_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bluetoothAdapter != null) {
                    textView.setText("取消扫描");
                    bluetoothAdapter.cancelDiscovery();

                }
            }
        });
    }

    /**
     * 链接操作
     * @param device
     */
    private void connectDevice(final BluetoothDevice device) {
                        new Thread(){
                    public void run() {
                        bluetoothAdapter.cancelDiscovery();
                        try {
                            BluetoothSocket socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"));
                            //通过socket连接到服务端
                            socket.connect();
                            //通过socket获取流
                            outputStream = socket.getOutputStream();

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "连接成功", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } catch (IOException e) {

                            e.printStackTrace();
                        }
                    }
                }.start();



    }

    class MyBluetoothReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                devices.add(device);
                if (mAdapter != null) {
                    mAdapter.notifyDataSetChanged();

                }


            }
        }
    }

    class MyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return devices.size();
        }

        @Override
        public Object getItem(int position) {
            return devices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            TextView tv;

            if (convertView == null) {
                tv = new TextView(getApplicationContext());
            } else {
                tv = (TextView) convertView;
            }
            tv.setTextColor(Color.BLACK);
            tv.setText(devices.get(position).getName() + "\n" + devices.get(position).getAddress());
            //  System.out.print(devices.get(position).getName() + "\n" + devices.get(position).getAddress());
            return tv;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }
}

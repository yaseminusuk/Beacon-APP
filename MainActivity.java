package com.example.beacon_app;

import java.util.ArrayList;
import com.example.beacon_app.R;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Toast;


public class MainActivity extends ListActivity {
	ArrayList<String> listItems=new ArrayList<String>();
	ArrayAdapter<String> adapter;
	int rssii=0;
	int ortalama=0;
	int m=0; 
	int d=0;
	SeekBar sbar;
	
	
	private BluetoothAdapter BluetoothAdaptor;
	private boolean Tarama;
	private Handler Handler = new Handler(); //runnable icin
	private static final int REQUEST_ENABLE_BT=123456;

	
	private static final long Tarama_Suresi = 20000;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,listItems);
		setListAdapter(adapter);
		sbar= (SeekBar) findViewById(R.id.seekBar1);
		BeaconKontrol();
		init();
		
		boolean ret = enableBLE();
		
		if(ret){
			startScan(false);
		}
	}

	@Override
	
	public boolean onCreateOptionsMenu(Menu menu) {
		
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void init(){
		
		final BluetoothManager bluetoothManager =
				(BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		BluetoothAdaptor = bluetoothManager.getAdapter();
	}
	private void startScan(boolean success){
		if(BluetoothAdaptor == null){
			init();
		}
		
		if(success){
			
			Tarama=true;
			scanLeDevice(Tarama);
			return;
		}
		if(enableBLE()){
			Tarama=true;
			scanLeDevice(Tarama);
		}
	}
	
	private void scanLeDevice(final boolean enable) {
		
		if (enable) {
			Handler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					
					Tarama = false;
					BluetoothAdaptor.stopLeScan(LeScanCallback);
				}
			}, Tarama_Suresi);
			BluetoothAdaptor.startLeScan(LeScanCallback);
		} else {
			BluetoothAdaptor.stopLeScan(LeScanCallback);
		}
	}
	private static String getCtx(){
		
		return " Beacon bilgileri aşağıdadır:";
	}
	
	private BluetoothAdapter.LeScanCallback LeScanCallback =
			new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(final BluetoothDevice device,final int rssi,
				final byte[] scanRecord) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {

					
					String a = null;
					int n=2;
					d = 10 ^ ((0 - rssi) / (10 * n));
					m =10 ^ ((22 - rssi) / (10 * n));
					double rssikaybi;
					rssii=rssi;
					rssikaybi = 20-rssi;
					double tahminimesafe;
					tahminimesafe=rssikaybi/41;
					rssii=rssii+rssi;
					
					
					if (rssi >=-35) a="Beacon sadece bir kaç santim uzağınızda";
					else if (rssi>=-50 && rssi <=-35) a="Beaconla aynı oda içerisindesiniz ve yakınlığınız en fazla 1 metre çapında";
					else if (rssi>=-65 && rssi <=-45) a="Beacondan bir miktar uzaktasınız mesafe 2 metre civarı";
					else if (rssi>=-80 && rssi <=-65) a="Beaconla aynı odada olmama ihtimaliniz var";
					else if (rssi>=-90 && rssi <=-80)  a="Beacona uzaktasınız bulunduğunuz konumu birkaç metre değiştirip tekrar tarama yapınız";
					else if (rssi>=-100 && rssi <=-90) a="Beacondan alınan en düşük sinyal aralığındasiniz açık alanda 40 kapalı alanda 10-12 metre civarı bir uzaklıktasınız";
					
				
						String msg=getCtx()+
								
							"\nBulunan Cihaz:" +"Anahtarım"+
							"\nRssi:" + rssi+
							
						"\nTahmini Mesafe en fazla " + tahminimesafe +"metre"+
					      //"\nTahmini Mesafe 2. Yöntem Blesh için:" + d+"metre"+
					        // "\nTahmini Mesafe 3. Yöntem Gimbal için:" + m+"metre"+
					         "\nTahmini Mesafe  :" + a;
				
					/*		"\nTxPower:" +0+
							"\n Uzaklığın Yaklaşık Formülü:"+"d = 10 ^ ((0 - rssi) / (10 * n))"+
							"\nTahmini Mesafe :"+d+" metre"
							
							
							
							*/;
							String mac = "34:B1:F7:CE:79:6E"; // mac adres filtrelemesi
							if (device.getAddress().equals(mac)) {
							 int b = Math.abs(rssi);
							 int c =(100*b)/120;
							sbar.setProgress(c);
							
					
					addItems(msg); }
						
				}
				
				
			});
		}
	};
	
	private void addItems(String msg) {
		synchronized(listItems){
			listItems.add(msg);
			adapter.notifyDataSetChanged();
		}
	}
	public void startScan(View v) {
		startScan(false);
	}
	public void stopScan(View v) {
		Tarama=false;
		scanLeDevice(Tarama);
	}
	public void clear(View v) {
		
		synchronized(listItems){
			listItems.clear();
			adapter.notifyDataSetChanged();
		}
	}
	private  void BeaconKontrol(){
		
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, R.string.ble_desteklenmiyor, Toast.LENGTH_SHORT).show();
			finish();
		}
	}
	private boolean enableBLE(){
		boolean ret=true;
	
		if (BluetoothAdaptor == null || !BluetoothAdaptor.isEnabled()) {
			Intent enableBtIntent = new Intent(android.bluetooth.BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent,REQUEST_ENABLE_BT);
			ret=false;
		}
		return ret;
	}
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if(requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK){
			startScan(true);
		}
	}
}

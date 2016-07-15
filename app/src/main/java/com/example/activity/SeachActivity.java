package com.example.activity;


import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.App;
import com.example.R;
import com.example.adapter.SearchDeviceAdapter;
import com.library.ble.BleServiceController;
import com.library.ble.data.SearchData;
import com.library.ble.impl.scan.PeriodMacScanCallback;
import com.library.ble.impl.scan.PeriodScanCallback;
import com.library.ble.inter.SearchDeviceInterface;
import com.library.ble.service.BleService;
import com.library.ble.utils.PermissionsChecker;
import com.zhy.autolayout.AutoLayoutActivity;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by gongjianghua on 16/7/6.
 */

public class SeachActivity extends AppCompatActivity {


    private Toolbar toolbar;
    private boolean isGPS;
    private PermissionsChecker permissionsChecker;
    private View helpview;
    private RecyclerView recyclerview;
    private SearchDeviceAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        initToolBar();
        recyclerview= (RecyclerView) findViewById(R.id.recyclerview);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        recyclerview.setLayoutManager(lm);
        adapter=new SearchDeviceAdapter(itemlistener);
        recyclerview.setAdapter(adapter);
        helpview=findViewById(R.id.layout_no_pressersion);
        permissionsChecker=new PermissionsChecker(this);
        isGPS=permissionsChecker.isCheckSystemVersionThanM();
        showGpsView();
        helpview.setOnClickListener(view->{
            permissionsChecker.showMissingPermissionDialog(SeachActivity.this,R.string.get_gps);
        });
        initBindeService();
    }

    private void showGpsView() {
        if(isGPS){
            if(permissionsChecker.lacksPermissions(PermissionsChecker.LOCAL_PERMISSIONS)){
                helpview.setVisibility(View.VISIBLE);
            }else{
                isGPS=false;
                helpview.setVisibility(View.GONE);
            }
        }else{
            isGPS=false;
            helpview.setVisibility(View.GONE);
        }
    }

    private void initToolBar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("主标题");// 标题的文字需在setSupportActionBar之前，不然会无效
        // App Logo
        toolbar.setLogo(R.mipmap.ic_launcher);
        // Title
        toolbar.setTitle("App Title");
        // Sub Title
        toolbar.setSubtitle("Sub title");
        setSupportActionBar(toolbar);
        toolbar.setOnMenuItemClickListener(item->{
            switch (item.getItemId()){
                case R.id.action_update://刷新列表
                    break;
                case R.id.action_set://设置
                    break;
                case R.id.action_search://搜索
                    if(isGPS){
                        Toast.makeText(SeachActivity.this,"请给予定位权限后才能操作",Toast.LENGTH_SHORT).show();;
                    }else{
                        boolean issstart = App.Companion.getBleController().startSearchDevice(new SearchDeviceInterface() {
                            @Override
                            public void onScanResults(BluetoothDevice device, int rssi, byte[] scanRecord, long scan_time) {
                                SearchData d=new SearchData();
                                d.mac=device.getAddress();
                                d.update_time=scan_time;
                                d.device=device;
                                d.ressi=rssi;
                                Observable.just(d)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .throttleWithTimeout(2,TimeUnit.SECONDS)
                                        .subscribe(new Action1<SearchData>() {
                                            @Override
                                            public void call(SearchData searchData) {
                                                adapter.addSearData(searchData);
                                            }
                                        }, new Action1<Throwable>() {
                                            @Override
                                            public void call(Throwable throwable) {

                                            }
                                        });
                            }

                            @Override
                            public void onScanTimeout() {
                                adapter.showSearchState();
                            }
                        });
                    }
                    break;
            }
            return false;
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unBindSservce();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==PermissionsChecker.SYSTEM_PERMISSION_REQUEST_CODE){
            showGpsView();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search,menu);
        return true;
    }

    ServiceConnection connect=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            if(iBinder!=null){
                BleService service=((BleService.ServiceBinder) iBinder).getService();
                App.Companion.setBleController(new BleServiceController(service));
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            App.Companion.setBleController(null);
        }
    };
    private void initBindeService() {
        Intent intent=new Intent(SeachActivity.this, BleService.class);
        bindService(intent,connect, Context.BIND_AUTO_CREATE);
    }


    private void unBindSservce() {
        unbindService(connect);
    }


    private View.OnClickListener itemlistener=new View.OnClickListener() {
        @Override
        public void onClick(View view) {

        }
    };
}

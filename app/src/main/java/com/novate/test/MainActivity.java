package com.novate.test;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.novate.usual.test_service.aidl.MyAIDLService;

import java.util.List;

public class MainActivity extends AppCompatActivity {


    private MyAIDLService myAIDLService ;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            // 这里把 IBinder 对象转为 MyAIDLService对象，就可以调用MyAIDLService.aidl接口中所有方法了
            myAIDLService = MyAIDLService.Stub.asInterface(service) ;
            try {
                int result = myAIDLService.sum(50 , 50) ;
                String upperStr = myAIDLService.toUppercase("come from ClientTest") ;
                Log.e("TAG" , "跨进程通信的：result: "+result + ", upperStr: "+upperStr) ;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    } ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button bind_service = (Button) findViewById(R.id.bind_service);
        bind_service.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("com.novate.usual.test_service.aidl.MyAIDLService") ;
                final Intent eintent = new Intent(createExplicitFromImplicitIntent(MainActivity.this,intent));
                bindService(eintent , connection , BIND_AUTO_CREATE) ;
            }
        });
    }



    /***
     * 隐式调用intent，5.0以上手机会报错：Service Intent must be explicit，
     * 重写下边这个方法就可以
     */
    public static Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
        // Retrieve all services that can match the given intent
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);

        // Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }

        // Get component info and create ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);

        // Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);

        // Set the component to be explicit
        explicitIntent.setComponent(component);

        return explicitIntent;
    }
}

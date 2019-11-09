package com.pvdnc.world;

import android.os.IBinder;
import android.os.RemoteException;
import android.system.Os;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PartSession {
    private static final String TAG=PartSession.class.getSimpleName();

    private static final Map<String,PartSession> sInstanceMap=new HashMap<>();

    public static PartSession getInstance(int fromPid,int toPid,String name){
        synchronized (sInstanceMap){
            if(!sInstanceMap.containsKey(name)){
                Log.d(TAG,"create instance for fromPid:"+fromPid
                +" toPid:"+toPid
                +" name:"+name);
                sInstanceMap.put(name,
                        new PartSession(fromPid,toPid,
                                name));
            }
            PartSession instance= sInstanceMap.get(name);
           if(instance==null){
               Log.e(TAG,"get or create instance of PartSession for:"+fromPid+" ("+name+")");
               return null;
           }
           if(instance.mFromPid==fromPid
           &&instance.mToPid==toPid)
               return instance;
           Log.e(TAG,"instance:"+instance+" is not created by pid:"+fromPid);
           return null;
        }
    }

    public static PartSession getInstance(String name){
        return sInstanceMap.get(name);
    }

    public int mFromPid;
    public String mName;

    public int mToPid;

    public PartSession(int fromPid,int toPid,String name){
        mFromPid= fromPid;
        mName=name;
    }

    private final ByteArrayOutputStream mOS=new ByteArrayOutputStream();
    public int writeFromRemote(byte[] data){
        synchronized (mOS) {
            mOS.write(data, 0, data.length);
            return data.length;
        }
    }

    public void writeAndClose(File file) throws IOException {
        synchronized (mOS) {
            byte[] data = mOS.toByteArray();
            IOUtils.write(file,data,false);
            close();
        }
    }

    public void close(){
        synchronized (this){
            try {
                Log.d(TAG,"attempt to close part session:"+mName);
                mOS.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            sInstanceMap.remove(mName);
        }
    }
}

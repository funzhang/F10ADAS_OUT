package com.neusoft.oddc;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.neusoft.oddc.entity.Constants;
import com.neusoft.oddc.oddc.model.ContinuousData;
import com.neusoft.oddc.oddc.model.DataPackageType;
import com.neusoft.oddc.oddc.neusoft.JobManager;
import com.neusoft.oddc.oddc.neusoft.LogData;
import com.neusoft.oddc.oddc.neusoft.NeuSoftInterface;
import com.neusoft.oddc.oddc.neusoft.ODDCclass;
import com.neusoft.oddc.oddc.neusoft.OBDManager;
import com.neusoft.oddc.oddc.neusoft.PlaybackList;
import com.neusoft.oddc.oddc.utilities.Utilities;import com.neusoft.oddc.widget.eventbus.EventStartDataCollection;
import com.neusoft.oddc.widget.eventbus.EventStopDataCollection;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;

public class NeusoftHandler implements NeuSoftInterface {

    private static final String TAG = "NEUSOFTHANDLER";

    private static Context mContext;
    private static ODDCclass oddCclass;
    private static JobManager jobManager;
    public static boolean isOddcOk = false;
    private OBDManager obd;

    // for data transfer between Neusoft and ODDC
    public void onFLAparam(int param) {
    } // example only at this time, param(s) TBD

    public NeusoftHandler(Context context) {
        mContext = context;
        obd = OBDManager.getInstance();
    }

    public NeusoftHandler() {
        obd = OBDManager.getInstance();
    }

    public static Context getContext() {
        return mContext;
    }

    public void sentToFLA(int param) {
        Log.d(TAG, "SentToFLA return " + param);
    }

    public void resume() {
        EventBus.getDefault().post(new EventStartDataCollection());
    }

    @Override
    public String getVIN() {
        return obd.getVIN(); // VIN
    }

    @Override
    public Location getLatLong() {
        if (null == obd) {
            return null;
        }
        return obd.getLocation();
    }

    public void stop() {
        EventBus.getDefault().post(new EventStopDataCollection());
    }


    public void init(Context context) {
        String url = com.neusoft.oddc.oddc.neusoft.Constants.ODDCApp.BASE_URL;
        File videodir = new File(Constants.FILE_PATH);
        oddCclass = new ODDCclass(url, context, videodir);
        oddCclass.setListener(this);
        obd = OBDManager.getInstance();


        //Initialize Job Manager
        jobManager = new JobManager(com.neusoft.oddc.oddc.neusoft.Constants.ODDCApp.BASE_URL);
        jobManager.setODDC(oddCclass);
        jobManager.setNSH(this);
        oddCclass.setJobManager(jobManager);

        jobManager.requestInitialSessionId();
    }

    public boolean startupOddcClass() {
        if (null != oddCclass) {
            isOddcOk = oddCclass.ok2Startup();
        }
        return isOddcOk;
    }

    public boolean postContinuousData(ContinuousData data) {
        if (null == oddCclass) {
            return false;
        }
        return oddCclass.onContinuousData(data);
    }

    public boolean shutdownOddc() {
        boolean isShutDownSuccess = false;
        if (null != oddCclass) {
            isShutDownSuccess = oddCclass.reqShutdown();
            oddCclass = null;
            isOddcOk = false;

            //Null jobManager instance
            if (jobManager != null) {
                jobManager.stopPingTimer();
                jobManager = null;
            }
        }
        return isShutDownSuccess;
    }

    public ArrayList<PlaybackList> getPlaybackList() {
        ArrayList<PlaybackList> list;
        if (null == oddCclass || !isOddcOk) {
            Log.e(TAG, "oddc is not ok!");
            list = new ArrayList<PlaybackList>();
        } else {
            list = oddCclass.getPlaybackList();
            Log.d(TAG, "oddCclass.getPlaybackList over");
            if (null != list && BuildConfig.DEBUG) {
                Log.d(TAG, "oddc trace -> playbacklist begin --------------- ");
                Log.d(TAG, "list size is " + list.size());
                for (PlaybackList mPlaybackList : list) {
                    Log.d(TAG, "oddc trace playbacklist-> MediaURI = " + mPlaybackList.MediaURI);
                    Log.d(TAG, "oddc trace playbacklist-> GShockEvent = " + mPlaybackList.GShockEvent);
                    Log.d(TAG, "oddc trace playbacklist-> FCWEvent = " + mPlaybackList.FCWEvent);
                    Log.d(TAG, "oddc trace playbacklist-> LDWEvent = " + mPlaybackList.LDWEvent);
                    Log.d(TAG, "oddc trace playbacklist-> MediaDeleted = " + mPlaybackList.MediaDeleted);
                }
                Log.d(TAG, "oddc trace -> playbacklist end --------------- ");
            }
        }
        return list;
    }

    public ArrayList<LogData> getLogList(DataPackageType t) {
        ArrayList<LogData> list;
        if (null == oddCclass || !isOddcOk) {
            Log.e(TAG, "oddc is not ok!");
            list = new ArrayList<LogData>();
        } else {
            Log.d(TAG, "oddCclass.getLogData DataPackageType = " + t);
            list = oddCclass.getLog(t);
            Log.d(TAG, "oddCclass.getLogData over");
            if (null != list && BuildConfig.DEBUG) {
                Log.d(TAG, "oddc trace -> LogData begin --------------- ");
                Log.d(TAG, "list size is " + list.size());
                for (LogData mLogData : list) {
                    Log.d(TAG, "oddc trace LogData-> filename = " + mLogData.filename);
                    Log.d(TAG, "oddc trace LogData-> sessionID = " + mLogData.sessionID);
                    Log.d(TAG, "oddc trace LogData-> timeStamp = " + mLogData.timeStamp);
                    Log.d(TAG, "oddc trace LogData-> dataUploaded = " + mLogData.dataUploaded);
                    Log.d(TAG, "oddc trace LogData-> eventType = " + mLogData.eventType);
                    Log.d(TAG, "oddc trace LogData-> mediaDeleted = " + mLogData.mediaDeleted);
                    Log.d(TAG, "oddc trace LogData-> mediaUploaded = " + mLogData.mediaUploaded);
                }
                Log.d(TAG, "oddc trace -> LogData end --------------- ");
            }
        }
        return list;
    }

    public static ContinuousData mkContinuousData(String currentFileName, double accelerationX, double accelerationY, double accelerationZ) {
        String dateTime = Utilities.getTimestamp();     // from OS not GPS

        ContinuousData cd = new ContinuousData();

        if(ODDCclass.curSession != null)
        {
            cd.sessionID = ODDCclass.curSession;
        }
        else
        {
            Log.e("mkContinuousData - ", "Invalid SessionID.");
            return null;
        }

        cd.timestamp = dateTime;
		String vin = OBDManager.getInstance().getVIN();

		//VIN cannot be empty.
        if(!vin.isEmpty())
        {
            cd.vehicleID = vin;
        }
        else
        {
            Log.e("mkContinuousData", "ERROR: Invalid VIN.");
            return null;
        }

        Location location = OBDManager.getInstance().getLocation();
        if (null != location) {
            double mLongitude = location.getLongitude();
            double mLatitude = location.getLatitude();
            cd.longitude = mLongitude;
            cd.latitude = mLatitude;
        }

        cd.speed = OBDManager.getInstance().getSPD();
        cd.speedDetectionType = 0; // always be ZERO

        cd.accelerationX = accelerationX;
        cd.accelerationY = accelerationY;
        cd.accelerationZ = accelerationZ;

        cd.gShockEvent = false;

        cd.fcwExistFV = false;
        cd.fcwDistanceToFV = 0;
        cd.fcwRelativeSpeedToFV = 0;

        cd.fcwEvent = false;
        cd.fcwEventThreshold = 0;

        cd.ldwDistanceToLeftLane = 0;
        cd.ldwDistanceToRightLane = 0;
        cd.ldwEvent = false;
        cd.mediaURI = currentFileName;

        return cd;
    }

//    private static String getTimestamp() {
//        SimpleDateFormat dateFormat = new SimpleDateFormat(com.neusoft.oddc.oddc.neusoft.Constants.ODDCApp.dateTimeFormat);
//        Date date = new Date();
//        return dateFormat.format(date);
//    }

}

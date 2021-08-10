package io.agora.openduo;

import android.app.Application;
import android.text.TextUtils;
import android.util.Log;

import io.agora.openduo.agora.Config;
import io.agora.openduo.agora.EngineEventListener;
import io.agora.openduo.agora.Global;
import io.agora.openduo.agora.IEventListener;
import io.agora.openduo.rtmkeys.RtmTokenBuilder;
import io.agora.openduo.utils.FileUtil;
import io.agora.openduo.utils.GlobalUtils;
import io.agora.rtc.Constants;
import io.agora.rtc.RtcEngine;
import io.agora.rtm.ErrorInfo;
import io.agora.rtm.ResultCallback;
import io.agora.rtm.RtmCallManager;
import io.agora.rtm.RtmClient;

public class OpenDuoApplication extends Application {
    private static final String TAG = OpenDuoApplication.class.getSimpleName();

    private RtcEngine mRtcEngine;
    private RtmClient mRtmClient;
    private RtmCallManager rtmCallManager;
    private EngineEventListener mEventListener;
    private Config mConfig;
    private Global mGlobal;
    private GlobalUtils globalUtils;

    private static String appId = "e16acdb29ca44eaf8c61829ce24fdeb9";
    private static String appCertificate = "9ca815c6fc81449ba2781d6376c789d1";
  //  private static String userId = "7614";
    private static int expireTimestamp = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        init();
    }

    private void init() {
        initConfig();
        initEngine();
    }

    private void initConfig() {
        mConfig = new Config(getApplicationContext());
        mGlobal = new Global();
        globalUtils = new GlobalUtils(this);

        createRtmToken();

        Log.e("RTC_ACCESS_TOKEN",
                globalUtils.getPrefString(io.agora.openduo.Constants.RTC_ACCESS_TOKEN)+"_");
    }

    private void initEngine() {
        String appId = getString(R.string.private_app_id);

        Log.e("initEngine",appId+"_");

        if (TextUtils.isEmpty(appId)) {
            throw new RuntimeException("NEED TO use your App ID, get your own ID at https://dashboard.agora.io/");
        }

        mEventListener = new EngineEventListener();
        try {
            mRtcEngine = RtcEngine.create(getApplicationContext(), appId, mEventListener);
          //  mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);


         //   mRtcEngine.enableVideo();

          //  mRtcEngine.disableVideo();
            mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION);
            mRtcEngine.enableDualStreamMode(true);
            mRtcEngine.enableAudio();
            mRtcEngine.disableVideo();
            mRtcEngine.setEnableSpeakerphone(true);
            mRtcEngine.enableLocalAudio(true);
            mRtcEngine.muteLocalAudioStream(false);
            mRtcEngine.setDefaultMuteAllRemoteAudioStreams(false);
            mRtcEngine.muteAllRemoteAudioStreams(false);

            mRtcEngine.setLogFile(FileUtil.rtmLogFile(getApplicationContext()));

            mRtmClient = RtmClient.createInstance(getApplicationContext(), appId, mEventListener);
            mRtmClient.setLogFile(FileUtil.rtmLogFile(getApplicationContext()));

            if (Config.DEBUG) {
                mRtcEngine.setParameters("{\"rtc.log_filter\":65535}");
                mRtmClient.setParameters("{\"rtm.log_filter\":65535}");
            }

            rtmCallManager = mRtmClient.getRtmCallManager();
            rtmCallManager.setEventListener(mEventListener);

            String accessToken = globalUtils.getPrefString(io.agora.openduo.Constants.RTC_ACCESS_TOKEN);

            //getString(R.string.rtm_access_token);

            if (TextUtils.equals(accessToken, "") || TextUtils.equals(accessToken,
                    "<#YOUR ACCESS TOKEN#>"))
            {
                accessToken = null;
            }

            mRtmClient.login(accessToken, mConfig.getUserId(), new ResultCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.e(TAG, "rtm client login success");
                }

                @Override
                public void onFailure(ErrorInfo errorInfo) {
                    Log.e(TAG, "rtm client login failed:" + errorInfo.getErrorDescription());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public RtcEngine rtcEngine() {
        return mRtcEngine;
    }

    public RtmClient rtmClient() {
        return mRtmClient;
    }

    public void registerEventListener(IEventListener listener) {
        mEventListener.registerEventListener(listener);
    }

    public void removeEventListener(IEventListener listener) {
        mEventListener.removeEventListener(listener);
    }

    public RtmCallManager rtmCallManager() {
        return rtmCallManager;
    }

    public Config config() {
        return mConfig;
    }

    public Global global() {
        return mGlobal;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        destroyEngine();
    }

    private void destroyEngine() {
        RtcEngine.destroy();

        mRtmClient.logout(new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i(TAG, "rtm client logout success");
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                Log.i(TAG, "rtm client logout failed:" + errorInfo.getErrorDescription());
            }
        });
    }

    private void createRtmToken(){
        RtmTokenBuilder token = new RtmTokenBuilder();
        String result = null;
        try {
            result = token.buildToken(appId, appCertificate,
                    config().getUserId(), RtmTokenBuilder.Role.Rtm_User, expireTimestamp);
        } catch (Exception e) {
            e.printStackTrace();
        }

        globalUtils.setPrefString(io.agora.openduo.Constants.RTC_ACCESS_TOKEN,result);

        Log.e("createRtmToken11", globalUtils.getPrefString(io.agora.openduo.Constants.RTC_ACCESS_TOKEN)+"_");

    }
}

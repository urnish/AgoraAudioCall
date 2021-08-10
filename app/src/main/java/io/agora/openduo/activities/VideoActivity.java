package io.agora.openduo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatImageView;

import io.agora.openduo.Constants;
import io.agora.openduo.R;
import io.agora.rtm.RemoteInvitation;

public class VideoActivity extends BaseCallActivity {
    private static final String TAG = VideoActivity.class.getSimpleName();

    private FrameLayout mLocalPreviewLayout;
    private FrameLayout mRemotePreviewLayout;
    private AppCompatImageView mMuteBtn;
    private String mChannel;
    private int mPeerUid;
    private int mRole;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);


        initUI();
        initVideo();
    }

    private void initUI() {
        mLocalPreviewLayout = findViewById(R.id.local_preview_layout);
        mRemotePreviewLayout = findViewById(R.id.remote_preview_layout);

        mMuteBtn = findViewById(R.id.btn_mute);
        mMuteBtn.setActivated(true);
    }

    @Override
    protected void onGlobalLayoutCompleted() {
        RelativeLayout.LayoutParams params =
                (RelativeLayout.LayoutParams) mLocalPreviewLayout.getLayoutParams();
        params.topMargin += statusBarHeight;
        mLocalPreviewLayout.setLayoutParams(params);

        RelativeLayout buttonLayout = findViewById(R.id.button_layout);
        params = (RelativeLayout.LayoutParams) buttonLayout.getLayoutParams();
        params.bottomMargin = displayMetrics.heightPixels / 8;
        params.leftMargin = displayMetrics.widthPixels / 6;
        params.rightMargin = displayMetrics.widthPixels / 6;
        buttonLayout.setLayoutParams(params);
    }

    private void initVideo() {
        Intent intent  = getIntent();
        mChannel = intent.getStringExtra(Constants.KEY_CALLING_CHANNEL);
        mRole = intent.getIntExtra(Constants.KEY_CALLING_ROLE, Constants.ROLE_CALLEE);

        Log.e("mRole_video",isCaller()+"_"+isCallee()+"_"+mChannel);
        try {
            mPeerUid = Integer.valueOf(intent.getStringExtra(Constants.KEY_CALLING_PEER));
        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.message_wrong_number,
                    Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        rtcEngine().setClientRole(io.agora.rtc.Constants.CHANNEL_PROFILE_COMMUNICATION);
        setVideoConfiguration();
        setupLocalPreview();

        rtcEngine().enableLocalAudio(true);
        rtcEngine().enableAudio();
        rtcEngine().disableVideo();
        rtcEngine().setEnableSpeakerphone(true);
        rtcEngine().enableLocalAudio(true);
        rtcEngine().muteLocalAudioStream(false);
        rtcEngine().setDefaultMuteAllRemoteAudioStreams(false);
        rtcEngine().muteAllRemoteAudioStreams(false);

        joinRtcChannel(mChannel, "", Integer.parseInt(config().getUserId()));
    }

    private void setupLocalPreview() {
        SurfaceView surfaceView = setupVideo(Integer.parseInt(config().getUserId()), true);
        surfaceView.setZOrderOnTop(true);
        mLocalPreviewLayout.addView(surfaceView);
    }

    @Override
    public void onUserJoined(final int uid, int elapsed) {
        if (uid != mPeerUid) return;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mRemotePreviewLayout.getChildCount() == 0) {
                    SurfaceView surfaceView = setupVideo(uid, false);
                    mRemotePreviewLayout.addView(surfaceView);
                }
            }
        });
    }

    @Override
    public void onUserOffline(int uid, int reason) {
        if (uid != mPeerUid) return;
        finish();
    }

    public void onButtonClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_endcall:

                Log.e("btn_endcall11",isCaller()+"_"+isCallee()+"");

                /*if (isCaller()) {
                    cancelLocalInvitation();
                } else if (isCallee()) {
                    refuseRemoteInvitation(global().getRemoteInvitation());
                }*/
                finish();

                break;
            case R.id.btn_mute:

                rtcEngine().setEnableSpeakerphone(true);

                Log.e("btn_mute",rtcEngine().isSpeakerphoneEnabled()+"_");

               // rtcEngine().muteLocalAudioStream(mMuteBtn.isActivated());
                mMuteBtn.setActivated(!mMuteBtn.isActivated());
                break;
            case R.id.btn_switch_camera:
                rtcEngine().switchCamera();
                break;
        }
    }

    private boolean isCaller() {
        return mRole == Constants.ROLE_CALLER;
    }

    private boolean isCallee() {
        return mRole == Constants.ROLE_CALLEE;
    }

    @Override
    public void finish() {
        super.finish();
        leaveChannel();
    }

    @Override
    public void onRemoteInvitationReceived(RemoteInvitation remoteInvitation) {
        // Do not respond to any other calls
        Log.i(TAG, "Ignore remote invitation from " +
                remoteInvitation.getCallerId() + " while in calling");
    }
}

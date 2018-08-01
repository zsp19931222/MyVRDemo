package com.zsp.myvrdemo;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.vr.sdk.widgets.common.VrWidgetView;
import com.google.vr.sdk.widgets.pano.VrPanoramaEventListener;
import com.google.vr.sdk.widgets.pano.VrPanoramaView;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private VrPanoramaView mVrPanoramaView;
    private ImagerLoaderTask imagerLoaderTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mVrPanoramaView =findViewById(R.id.vr);

        // 隐藏掉VR效果左下角的信息按钮显示
        mVrPanoramaView.setInfoButtonEnabled(false);
        // 隐藏VR效果右下角全屏显示的按钮
        mVrPanoramaView.setFullscreenButtonEnabled(false);
        // 切换VR的模式  参数：VrWidgetView.DisplayMode.FULLSCREEN_STEREO设备模式（手机横看）(.FULLSCREEN_MONO手机模式)
        mVrPanoramaView.setDisplayMode(VrWidgetView.DisplayMode.FULLSCREEN_STEREO);

        // 设置对VR运行状态的监听，如果VR运行出现错误，可以及时处理
        mVrPanoramaView.setEventListener(new MVREventListener());

        // B 使用自定义的AsyncTask,播放VR效果
        imagerLoaderTask = new ImagerLoaderTask();
        imagerLoaderTask.execute();

    }

    // 因为读取VR的资源是一个耗时操作（VR资 源非常大，读取需要时间）所以我们不能再主线程去做读取操作，
// 但是只有在主线程才能做UI的更新，故我们使用AsyncTask
    @SuppressLint("StaticFieldLeak")
    private class ImagerLoaderTask extends AsyncTask<Void, Void, Bitmap> {// 以后EventBus去替代

        // B 该方法在子线程运行，从本地文件中把资源加载到内存中
        // 此方法中定义要执行的后台任务，在这个方法中可以调用publishProgress来更新任务进度
        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                // 从资产目录拿到资源，返回结果是字节流
                InputStream inputStream = getAssets().open("57bac09041152.jpg");
                // 把字节流转换成Bitmap对象
                // 返回出bitmap
                return BitmapFactory.decodeStream(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        // 接收bitmap运行在主线程中
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            // 创建VrPanoramaView.Options，去决定显示VR是普通效果，还是立体效果
            VrPanoramaView.Options options = new VrPanoramaView.Options();
            // TYPE_STEREO_OVER_UNDER立体效果：图片的上半部分放在左眼显示，下半部分放在右眼显示  TYPE_MONO:普通效果
            options.inputType = VrPanoramaView.Options.TYPE_MONO;
            // 使用VR控件对象，显示效果  参数1：Bitmap对象  2.VrPanoramaView.Options对象，决定显示的效果
            mVrPanoramaView.loadImageFromBitmap(bitmap, options);
            super.onPostExecute(bitmap);
        }
    }
// 因为VR很占用内存，所以当界面进入onPause状态，暂停VR视图显示，进入onResume状态，继续VR视图显示，进入onDestroy，杀死VR，关闭异步任务

    // 当失去焦点时，回调
    @Override
    protected void onPause() {
        // 暂停渲染和显示
        mVrPanoramaView.pauseRendering();
        super.onPause();
    }

    // 当重新获取焦点时，回调
    @Override
    protected void onResume() {
        super.onResume();
        // 继续渲染和显示   resumeRendering();重新阅读
        mVrPanoramaView.resumeRendering();
    }

    // 当Activity销毁时，回调
    @Override
    protected void onDestroy() {
        // 关闭渲染视图
        mVrPanoramaView.shutdown();
        if (imagerLoaderTask != null) {
            // 退出activity时，如果异步任务没有取消，就取消
            if (imagerLoaderTask.isCancelled()) {
                imagerLoaderTask.cancel(true);
            }
        }
        super.onDestroy();
    }

    // VR运行状态监听类，自定义一个类，继承
    private class MVREventListener extends VrPanoramaEventListener {

        // 当VR视图加载成功的时候，回调
        @Override
        public void onLoadSuccess() {
            super.onLoadSuccess();
            Toast.makeText(MainActivity.this, "加载成功", Toast.LENGTH_SHORT).show();
        }

        public void onLoadError(String errorMessage) {
            super.onLoadError(errorMessage);
            Toast.makeText(MainActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
        }
    }
}
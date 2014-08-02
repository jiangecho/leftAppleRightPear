package com.echo.littleapple;



import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;

import com.echo.littleapple.GameView.GameEventListner;


public class GameActiviy extends Activity implements GameEventListner{

	private static final int TIME_LENGHT = 30 * 1000;
	private static final String BEST_SCORE = "BEST_SCORE";
	private static final String APP_URL = "http://shouji.360tpcdn.com/140728/9c84852ba0df5ee6c31bf7dd91b4a743/com.echo.littleapple_4.apk";

	private TextView timerTV;
	private GameView gameView;
	private LinearLayout startLayer;

	private LinearLayout resultLayer;
	private TextView resultTV;
	private TextView bestTV;
	private TextView promptTV;
	private Handler handler;
	
	private CountDownTimer countDownTimer;
	private StringBuffer remindTimeSB;
	private SharedPreferences sharedPreferences;
	private int bestScore;
	
	
	private long lastPressMillis = 0;
	
	private BlockOnTouchEvent blockOnTouchEvent;
	
	private static final String[] colors = {"#773460" ,"#FE436A" ,"#823935" ,"#113F3D" ,"#26BCD5" ,"#F40D64" ,"#458994" ,"#93E0FF" ,"#D96831" ,"#AEDD81" ,"#593D43"};
	private Random random;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_main);
		countDownTimer = new MyCountDownTimer(TIME_LENGHT, 100);
		blockOnTouchEvent = new BlockOnTouchEvent();
        timerTV = (TextView)findViewById(R.id.timerTV);
        gameView = (GameView)findViewById(R.id.gameView);
        promptTV = (TextView) findViewById(R.id.promptTV);
        gameView.setGameEventListener(this);
        startLayer = (LinearLayout)findViewById(R.id.startLayer);
        startLayer.setOnTouchListener(blockOnTouchEvent);

        resultLayer = (LinearLayout)findViewById(R.id.resultLayer);
        resultLayer.setOnTouchListener(blockOnTouchEvent);
        
        resultTV = (TextView) findViewById(R.id.resultTV);
        bestTV = (TextView) findViewById(R.id.bestTV);
        
        handler = new Handler();
        remindTimeSB = new StringBuffer();
        
        sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        bestScore = sharedPreferences.getInt(BEST_SCORE, 0);

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
	}
	
	private class MyCountDownTimer extends CountDownTimer{
		private int remindSeconds;
		private int remindMillis;

		public MyCountDownTimer(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onFinish() {
			gameView.playGameSoundEffect(GameView.TIME_OUT);
			timerTV.setText(getResources().getString(R.string.time_out));
			handler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					updateAndShowResultLayer();
				}
			}, 500);
		}

		@Override
		public void onTick(long millisUntilFinished) {
			//timerTV.setText("test " + millisUntilFinished / 1000);
//			String remindTime = "" + millisUntilFinished / 1000 + "." + millisUntilFinished % 1000 / 10;
//			timerTV.setText(remindTime);
			remindTimeSB.setLength(0);
			remindSeconds = (int) (millisUntilFinished / 1000);
			remindMillis = (int) (millisUntilFinished % 1000 / 10);
			
			if (remindSeconds < 10) {
				remindTimeSB.append("0");
			}
			remindTimeSB.append(remindSeconds);
			remindTimeSB.append(".");
			
			if (remindMillis < 10) {
				remindTimeSB.append("0");
			}
			remindTimeSB.append(remindMillis);
			
			timerTV.setText(remindTimeSB);
		}
		
	}
	
	
	public void onStartButtonClick(View view){
		startLayer.setVisibility(View.INVISIBLE);
		//gameView.reset();
	}

	public void onRestartButtonClick(View view){
		resultLayer.setVisibility(View.INVISIBLE);
		timerTV.setText("30.00");
		gameView.reset();
	}

	
	private void updateAndShowResultLayer(){
		
		if (random == null) {
			random = new Random();
		}
		int colorIndex = random.nextInt(colors.length);
		resultLayer.setBackgroundColor(Color.parseColor(colors[colorIndex]));;

		int score = gameView.getScore();
		String value = getResources().getString(R.string.result, score);
		resultTV.setText(value);
		
		if (score > 100) {
			value = getResources().getString(R.string.str_high_score);
		}else {
			value = getResources().getString(R.string.strf);
		}
		promptTV.setText(value);

		if (score > bestScore) {
			bestScore = score;
			sharedPreferences.edit().putInt(BEST_SCORE, bestScore).commit();
		}

        value = getString(R.string.best, bestScore);
        bestTV.setText(value);
		resultLayer.setVisibility(View.VISIBLE);

		
	}

	@Override
	public void onGameStart() {
		//gameView.reset();
		countDownTimer.start();
	}

	@Override
	public void onGameOver(int score) {
		//TODO stop timer
		// show result
		countDownTimer.cancel();
		
		//TODO best score
		updateAndShowResultLayer();
	}
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			long currentMillis = System.currentTimeMillis();
			if (currentMillis - lastPressMillis < 2000) {
			 	finish();
			 	return true;
			}else {
				lastPressMillis = currentMillis;
				Toast toast = Toast.makeText(this, getResources().getString(R.string.press_twice_to_exit) , Toast.LENGTH_SHORT);
				toast.show();
				return false;
			}
			
		}
		
		return super.onKeyUp(keyCode, event);
	}
	
	private class BlockOnTouchEvent implements OnTouchListener{

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			return true;
		}
		
	}
	
	public void onShareButtonClick(View view){
		String imgPath = takeScreenShot(view);
		if (imgPath == null) {
			Toast.makeText(this, "SD��������", Toast.LENGTH_SHORT).show();
		}else {
			showShare(imgPath);
		}
	}
	
	private String takeScreenShot(View view){
		View rootView = view.getRootView();
		rootView.setDrawingCacheEnabled(true);
		rootView.buildDrawingCache(true);
		Bitmap bitmap = rootView.getDrawingCache(true);
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			return null;
		}
		File  path = Environment.getExternalStorageDirectory();
		File file = new File(path, "screenshot.png");

		if (file.exists()) {
			file.delete();
		}
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(file);
			bitmap.compress(CompressFormat.PNG, 100, fileOutputStream);
			fileOutputStream.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		rootView.destroyDrawingCache();
		return file.getAbsolutePath();
		
	}
	
   private void showShare(String imgPath) {
        ShareSDK.initSDK(this);
        OnekeyShare oks = new OnekeyShare();
        //�ر�sso��Ȩ
        oks.disableSSOWhenAuthorize();
       
        // ����ʱNotification��ͼ�������
        oks.setNotification(R.drawable.ic_launcher, getString(R.string.app_name));
        // title���⣬ӡ��ʼǡ����䡢��Ϣ��΢�š���������QQ�ռ�ʹ��
        oks.setTitle(getString(R.string.app_name));
        // titleUrl�Ǳ�����������ӣ�������������QQ�ռ�ʹ��
        oks.setTitleUrl(APP_URL);
        // text�Ƿ����ı�������ƽ̨����Ҫ����ֶ�
        oks.setText("����������ս�Ұɣ������ҵ�Сƻ��:" + APP_URL);
        // imagePath��ͼƬ�ı���·����Linked-In�����ƽ̨��֧�ִ˲���
        oks.setImagePath(imgPath);
        // url����΢�ţ��������Ѻ�����Ȧ����ʹ��
        oks.setUrl(APP_URL);
        // comment���Ҷ�������������ۣ�������������QQ�ռ�ʹ��
        oks.setComment("�Ǻǣ��ҳ���" + gameView.getScore() + "��Сƻ����");
        // site�Ƿ�������ݵ���վ���ƣ�����QQ�ռ�ʹ��
        oks.setSite(getString(R.string.app_name));
        // siteUrl�Ƿ�������ݵ���վ��ַ������QQ�ռ�ʹ��
        oks.setSiteUrl(APP_URL);

        // ��������GUI
        oks.show(this);
   }
	
}

package com.echo.leftAppleRightPear;



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
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;


public class GameActiviy extends Activity implements GameEventListener{

	private static final int TIME_LENGHT = 30 * 1000;
	private static final String BEST_SCORE = "BEST_SCORE";
	private static final String APP_URL = "http://1.littleappleapp.sinaapp.com/littleApple.apk";

	private TextView timerTV;
	private GameViewLeft gameViewLeft;
	private GameViewRight gameViewRight;
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
	
	private static final int MODE_IN_TURN = 0;
	private static final int MODE_RANDOM = 1;
	//private static final int MODE_

	private int mode = MODE_RANDOM;
	private Object gameView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// request full screen
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.fragment_main);
		countDownTimer = new MyCountDownTimer(TIME_LENGHT, 100);
		blockOnTouchEvent = new BlockOnTouchEvent();
        timerTV = (TextView)findViewById(R.id.timerTV);
        gameViewLeft = (GameViewLeft)findViewById(R.id.gameViewUp);
        gameViewRight = (GameViewRight) findViewById(R.id.gameViewDown);
        promptTV = (TextView) findViewById(R.id.promptTV);
        gameViewLeft.setGameEventListener(this);
        gameViewRight.setGameEventListener(this);
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
        
        random = new Random();
        gameView = gameViewLeft;

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
			gameViewLeft.playGameSoundEffect(GameViewRight.TIME_OUT);
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
		
		boolean leftToBeStartView = random.nextBoolean();
		if (leftToBeStartView) {
			gameViewLeft.setGameStartStatus(true);
			gameViewRight.setGameStartStatus(false);
			gameView = gameViewLeft;
		}else {
			gameViewLeft.setGameStartStatus(false);
			gameViewRight.setGameStartStatus(true);
			gameView = gameViewRight;
		}

		gameViewLeft.reset();
		gameViewRight.reset();
	}

	
	private void updateAndShowResultLayer(){
		
		int colorIndex = random.nextInt(colors.length);
		resultLayer.setBackgroundColor(Color.parseColor(colors[colorIndex]));;

		int score = gameViewLeft.getScore();
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
	public void onNewFruit(boolean status) {
		// TODO Auto-generated method stub
		
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
			Toast.makeText(this, "SD卡不存在", Toast.LENGTH_SHORT).show();
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
        //关闭sso授权
        oks.disableSSOWhenAuthorize();
       
        // 分享时Notification的图标和文字
        oks.setNotification(R.drawable.ic_launcher, getString(R.string.app_name));
        // title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
        oks.setTitle(getString(R.string.app_name));
        // titleUrl是标题的网络链接，仅在人人网和QQ空间使用
        oks.setTitleUrl(APP_URL);
        // text是分享文本，所有平台都需要这个字段
        oks.setText("哈哈，来挑战我吧！你是我的小苹果:" + APP_URL);
        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
        oks.setImagePath(imgPath);
        // url仅在微信（包括好友和朋友圈）中使用
        oks.setUrl(APP_URL);
        // comment是我对这条分享的评论，仅在人人网和QQ空间使用
        oks.setComment("呵呵，我吃了" + gameViewLeft.getScore() + "个小苹果！");
        // site是分享此内容的网站名称，仅在QQ空间使用
        oks.setSite(getString(R.string.app_name));
        // siteUrl是分享此内容的网站地址，仅在QQ空间使用
        oks.setSiteUrl(APP_URL);

        // 启动分享GUI
        oks.show(this);
   }

	@Override
	public void onFruitClick() {
		
		if (mode == MODE_IN_TURN) {
			if (gameView == gameViewLeft) {
				gameViewRight.addNewFruit();
				gameView = gameViewRight;
			}else {
				gameViewLeft.addNewFruit();
				gameView = gameViewLeft;
			}
		}else {
			boolean tmp = random.nextBoolean();
			if (tmp) {
				gameViewLeft.addNewFruit();
			}else {
				gameViewRight.addNewFruit();
			}
		}
	}
	
}
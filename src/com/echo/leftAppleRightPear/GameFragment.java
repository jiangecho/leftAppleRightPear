package com.echo.leftAppleRightPear;



import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;


public class GameFragment extends Fragment{

	private static final int TIME_LENGHT = 30 * 1000;

	private View rootView;
	private TextView timerTV;
	private GameViewDown gameView;
	private LinearLayout startLayer;
	private LinearLayout resultLayer;
	private Button startButton;
	
	private CountDownTimer countDownTimer;
	private GameTimeOutListner listner = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		countDownTimer = new MyCountDownTimer(TIME_LENGHT, 100);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		//return super.onCreateView(inflater, container, savedInstanceState);
		if(rootView == null){
			rootView = inflater.inflate(R.layout.fragment_main, container);
			timerTV = (TextView) rootView.findViewById(R.id.timerTV);
			gameView = (GameViewDown) rootView.findViewById(R.id.gameViewDown);
			startLayer = (LinearLayout) rootView.findViewById(R.id.startLayer);
			resultLayer = (LinearLayout) rootView.findViewById(R.id.resultLayer);
			//top.setVisibility(View.VISIBLE);

			startButton = (Button) rootView.findViewById(R.id.startButton);
		}else {
			ViewGroup parent = (ViewGroup) rootView.getParent();
			if (parent != null) {
				parent.removeView(rootView);
			}
		}
		
		return rootView;
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

		public MyCountDownTimer(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onFinish() {
			if (listner != null) {
				listner.onGameTimeOut(gameView.getScore());
			}
		}

		@Override
		public void onTick(long millisUntilFinished) {
			//TODO update the timerTV
			timerTV.setText("test");
			
		}
		
	}
	
	public void startGameTimer(){
		countDownTimer.start();
	}

	public interface GameTimeOutListner{
		public void onGameTimeOut(int score);
	}
	
	public void onStartButtonClick(View view){
		startLayer.setVisibility(View.INVISIBLE);
	}

	public void onRestartButtonClick(View view){
		//TODO
	}
}

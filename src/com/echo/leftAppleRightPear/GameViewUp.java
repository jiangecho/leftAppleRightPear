package com.echo.leftAppleRightPear;

import java.util.Random;

import android.R.integer;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class GameViewUp extends View{
	
	private static final int COLUMN = 2;
	private static final int OK = 0;
	private static final int FAIL = 1;
	public static final int TIME_OUT = 2;
	private int row;
	private int cellWidth;
	private int cellHeight;
	private int firstCellHeight, lastCellHeight;
	
	private int width, height;
	
	private Paint linePaint;
	private Paint fruitPaint;
	private Paint textPaint;
	
	private int[][] fruits = null;
	private Random random;
	private Rect rect;
	private Bitmap bitmapfruit;
	private Bitmap bitmapError;
	
	private GameEventListner listner;
	private boolean running;
	
	private int score;
	
	private int moveStepHeight;
	private int moveYOffset = 0;
	private Handler handler;
	
	private SoundPool soundPool;
	private int[] sounds;
	private float audioMaxVolumn;
	private float audioCurrentVolumn;
	private float volumnRatio;
	private Context context;
	
	private HandlerThread soundPoolThread;
	private Handler soundPoolHandler;

	int left, top, right, bottom;

	public GameViewUp(Context context) {
		this(context, null);
		
	}
	

	public GameViewUp(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}


	public GameViewUp(Context context, AttributeSet attrs) {
		super(context, attrs);

		linePaint = new Paint();
		linePaint.setAntiAlias(true);
		
		textPaint = new Paint(linePaint);
		textPaint.setTextAlign(Paint.Align.CENTER);
		//TODO set color
		
		fruitPaint = new Paint(linePaint);
		
		bitmapfruit = BitmapFactory.decodeResource(context.getResources(), R.drawable.apple);
		bitmapError = BitmapFactory.decodeResource(context.getResources(), R.drawable.error);
		rect = new Rect();
		
		random = new Random();
		running = false;
		
		score = 0;
		handler = new Handler();
		
		this.context = context;
		// init sound play
		initSoundPool();

	}


	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int i, j;
		
		
		//draw horizontal lines
		for (i = 0; i < row ; i++) {
			canvas.drawLine(0, moveYOffset + cellHeight * i, width, moveYOffset + cellHeight * i, linePaint);
		}
		
		//draw vertical lines
		for (i = 0; i < COLUMN ; i++) {
			canvas.drawLine(cellWidth * i, 0, cellWidth * i, height, linePaint);
		}
		
		if (bitmapfruit == null || bitmapError == null) {
			return;
		}

		// draw applse
		for (i = 0; i < row; i++) {
			for (j = 0; j < COLUMN; j++) {
				if (fruits[i][j] == 0) {
					// do nothing
				}else if(fruits[i][j] == 1){
					// draw fruits
					//left = (j >= 1 ) ? (j - 1) * cellWidth  + cellWidth : 0;
					left = j * cellWidth;
					//top = moveYOffset + ((i >= 1) ? (firstCellHeight + (i - 1) * cellHeight) : (firstCellHeight - cellHeight)); 
					top = moveYOffset + i * cellHeight;
					right = (j + 1) * cellWidth;
					//bottom = moveYOffset + firstCellHeight + i * cellHeight;
					bottom = moveYOffset + (i + 1) * cellHeight;
					//rect.set(left, top, right, bottom);
					rect.set(left, top, right, bottom);
					canvas.drawBitmap(bitmapfruit, null, rect, fruitPaint);
					
				}else {
					left = j * cellWidth;
					//top = moveYOffset + ((i >= 1) ? (firstCellHeight + (i - 1) * cellHeight) : (firstCellHeight - cellHeight)); 
					top = moveYOffset + i * cellHeight;
					right = (j + 1) * cellWidth;
					//bottom = moveYOffset + firstCellHeight + i * cellHeight;
					bottom = moveYOffset + (i + 1) * cellHeight;
					//rect.set(left, top, right, bottom);
					rect.set(left, top, right, bottom);
					canvas.drawBitmap(bitmapError, null, rect, fruitPaint);
					
				}
			}
			
		}
		
		
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		
		width = MeasureSpec.getSize(widthMeasureSpec);
		height = MeasureSpec.getSize(heightMeasureSpec);

		if (width == 0 || height == 0) {
			return;
		}

		cellWidth = width / COLUMN;
		cellHeight = cellWidth;
		
		lastCellHeight = height % cellHeight;
		row  = height / cellHeight;
		
		if (lastCellHeight > 0) {
			row  += 1;
		}
		
		moveStepHeight = cellHeight / 6;
		
		if (fruits == null) {
			fruits = new int[row][COLUMN];
			randomfruits();
		}


	}
	
	public void reset(){
		this.score = 0;
		running = false;
		randomfruits();
		moveYOffset = 0;
		invalidate();
	}
	
	private void randomfruits(){
		int columnIndex;
		for (int i = 0; i < row; i++) {
			for (int j = 0; j < COLUMN; j++) {
				fruits[i][j] = 0;
			}
		}

		for (int i = 0; i < row; i++) {
			columnIndex = random.nextInt(COLUMN);
			fruits[i][columnIndex] = 1;
		}
	}
	
	
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		//return super.onTouchEvent(event);
		
		if (event.getAction() != MotionEvent.ACTION_DOWN) {
			return false;
		}else {
			int x = (int) event.getX();
			int y = (int) event.getY();
			
			if (y < 0
					|| y > cellHeight) {
				// wrong, do not have any effect
				return false;
			}
			
			int x_index = x / cellWidth;
			
			//game over
			if (fruits[0][x_index] != 1) {
				fruits[0][x_index] = 3;
				playGameSoundEffect(FAIL);
				running = false;
				invalidate();
				handler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						if (listner != null) {
							listner.onGameOver(score);
					
						}
						
					}
				}, 300);
				
				
			}else {
				playGameSoundEffect(OK);
				if (!running) {
					running = true;
					if (listner != null) {
						listner.onGameStart();
					}
				}
				
				// move down
				score ++;
				fruits[0][x_index] = 0;
				startMoveAnimation();
			}

			// right, move
			// wrong, game over
			return true;
		}
		
	}


	public void setGameEventListener(GameEventListner listner){
		this.listner = listner;
	}
	
	private void startMoveAnimation(){
		handler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				if (moveYOffset > -cellHeight) {
					moveYOffset -= moveStepHeight;
					invalidate();
					postDelayed(this, 10);
				}else {
					moveYOffset = 0;
					for (int i = 0; i < row - 1; i++) {
						for (int j = 0; j < COLUMN; j++) {
							fruits[i][j] = fruits[i + 1][j]; 
							fruits[i + 1][j] = 0;
						}
					}
					int x_index = random.nextInt(COLUMN);
					fruits[row - 1][x_index] = 1;
					invalidate();
				}
			}
		}, 10);
	}
	
	private void initSoundPool(){
		soundPoolThread = new HandlerThread("test");
		soundPoolThread.start();
		soundPoolHandler = new Handler(soundPoolThread.getLooper(), null);
		soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
		
		sounds = new int[3];
		sounds[0] = soundPool.load(context, R.raw.ok, 1);
		sounds[1] = soundPool.load(context, R.raw.fail, 1);
		sounds[2] = soundPool.load(context, R.raw.time_out, 1);

		AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		audioMaxVolumn = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		audioCurrentVolumn = am.getStreamVolume(AudioManager.STREAM_MUSIC);
		
		volumnRatio = audioCurrentVolumn / audioMaxVolumn;
	}
	
	
	public void playGameSoundEffect(final int type){
		//soundPool.pla
		soundPoolHandler.post(new Runnable() {
			
			@Override
			public void run() {
				soundPool.play(sounds[type], volumnRatio, volumnRatio, 1, 0, 1);
				
			}
		});

	}
	
	
	public interface GameEventListner{
		public void onGameOver(int score);
		public void onGameStart();
	}
	
	public int getScore(){
		return this.score;
	}
}

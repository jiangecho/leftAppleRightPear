package com.echo.leftAppleRightPear;

import java.util.Random;

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

public class GameViewRight extends View{
	
	private static final int ROW = 4;
	private static final int OK = 0;
	private static final int FAIL = 1;
	public static final int TIME_OUT = 2;
	private int column;
	private int cellWidth;
	private int cellHeight;
	private int firstCellWidth;
	
	private int width, height;
	
	private Paint linePaint;
	private Paint fruitPaint;
	private Paint textPaint;
	
	private int[][] fruits = null;
	private Random random;
	private Rect rect;
	private Bitmap bitmapfruit;
	private Bitmap bitmapError;
	
	private GameEventListener listner;
	private boolean running;
	
	private int score;
	
	private int moveStepWidth;
	private int moveXOffset = 0;
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

	public GameViewRight(Context context) {
		this(context, null);
		
	}
	

	public GameViewRight(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}


	public GameViewRight(Context context, AttributeSet attrs) {
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
		for (i = 0; i <= ROW; i++) {
			canvas.drawLine(0,  cellWidth * i, width, cellWidth * i, linePaint);
			//canvas.drawLine(0, moveYOffset + firstCellHeight + cellHeight * i, width, moveYOffset + firstCellHeight + cellHeight * i, linePaint);
		}
		
		//draw the first vertical line
		canvas.drawLine(moveXOffset + firstCellWidth, 0, moveXOffset + firstCellWidth, height, linePaint);
		
		//draw vertical lines
		for (i = -1; i < column; i++) {
			//canvas.drawLine(cellWidth * i, 0, cellWidth * i, height, linePaint);
			canvas.drawLine(moveXOffset + firstCellWidth + cellWidth * i, 0, moveXOffset + firstCellWidth + cellWidth * i, height, linePaint);
		}
		
		if (bitmapfruit == null || bitmapError == null) {
			return;
		}

		// draw applse
		for (i = 0; i < ROW; i++) {
			for (j = 0; j < column; j++) {
				if (fruits[i][j] == 0) {
					// do nothing
				}else if(fruits[i][j] == 1){
					// draw fruits

					left = moveXOffset + ((j >= 1) ? (firstCellWidth + (j - 1) * cellWidth) : (firstCellWidth - cellWidth));
					top = i * cellHeight;
					right = moveXOffset + firstCellWidth + j * cellWidth;
					bottom = (i + 1) * cellHeight;
					//rect.set(left, top, right, bottom);
					rect.set(left, top, right, bottom);
					canvas.drawBitmap(bitmapfruit, null, rect, fruitPaint);
					
				}else {
					
					left = moveXOffset + ((j >= 1) ? (firstCellWidth + (j - 1) * cellWidth) : (firstCellWidth - cellWidth));
					top = i * cellHeight;
					right = moveXOffset + firstCellWidth + j * cellWidth;
					bottom = (i + 1) * cellHeight;
					
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

		cellWidth = height / ROW;
		cellHeight = cellWidth;
		
		firstCellWidth = width % cellWidth;
		column = width / cellWidth;
		
		if (firstCellWidth > 0) {
			column += 1;
		}
		
		moveStepWidth = cellWidth / 10;
		
		if (fruits == null) {
			fruits = new int[ROW][column];
			randomfruits();
		}


	}
	
	public void reset(){
		this.score = 0;
		running = false;
		randomfruits();
		moveXOffset = 0;
		invalidate();
	}
	
	private void randomfruits(){
		int rowIndex;
		//TODO startColumnIndex should base on this is the start view or not
		int startColumnIndex = 0;
		for (int i = 0; i < ROW; i++) {
			for (int j = 0; j < column; j++) {
				fruits[i][j] = 0;
			}
		}

		for (int i = startColumnIndex; i < column; i++) {
			rowIndex = random.nextInt(ROW);
			fruits[rowIndex][i] = 1;
			continue;
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
			
			if (x < width - cellWidth 
					|| x > width) {
				// wrong, do not have any effect
				return false;
			}
			
			int y_index = y / cellHeight;
			
			//game over
			if (fruits[y_index][column - 1] != 1) {
				fruits[y_index][column - 1] = 3;
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
				fruits[y_index][column - 1] = 0;
				startMoveAnimation();
			}

			// right, move
			// wrong, game over
			return true;
		}
		
	}


	public void setGameEventListener(GameEventListener listner){
		this.listner = listner;
	}
	
	private void startMoveAnimation(){
		handler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				if (moveXOffset < cellWidth) {
					moveXOffset += moveStepWidth;
					invalidate();
					postDelayed(this, 10);
				}else {
					moveXOffset = 0;
					for (int i = column - 1; i > 0; i--) {
						for (int j = 0; j < ROW; j++) {
							fruits[j][i] = fruits[j][i - 1]; 
							fruits[j][i - 1] = 0;
						}
					}
					int y_index = random.nextInt(ROW);
					fruits[y_index][0] = 1;
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
	
	public int getScore(){
		return this.score;
	}
}

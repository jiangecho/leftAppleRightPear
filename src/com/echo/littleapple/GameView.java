package com.echo.littleapple;

import java.util.Random;

import android.R.integer;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.CountDownTimer;
import android.os.Handler;
import android.renderscript.Type.CubemapFace;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class GameView extends View{
	
	private static final int COLUMN = 4;
	private int row;
	private int cellWidth;
	private int cellHeight;
	private int firstCellHeight;
	
	private int width, height;
	
	private Paint linePaint;
	private Paint applePaint;
	private Paint textPaint;
	
	private int[][] apples = null;
	private Random random;
	private Rect rect;
	private Bitmap bitmapApple;
	private Bitmap bitmapError;
	
	private GameEventListner listner;
	private boolean running;
	
	private int score;
	
	private int moveStepHeight;
	private int moveYOffset = 0;
	private Handler handler;

	public GameView(Context context) {
		this(context, null);
		
	}
	

	public GameView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}


	public GameView(Context context, AttributeSet attrs) {
		super(context, attrs);

		linePaint = new Paint();
		linePaint.setAntiAlias(true);
		
		textPaint = new Paint(linePaint);
		textPaint.setTextAlign(Paint.Align.CENTER);
		//TODO set color
		
		applePaint = new Paint(linePaint);
		
		bitmapApple = BitmapFactory.decodeResource(context.getResources(), R.drawable.apple);
		bitmapError = BitmapFactory.decodeResource(context.getResources(), R.drawable.error);
		rect = new Rect();
		
		random = new Random();
		running = false;
		
		score = 0;
		handler = new Handler();
	}


	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int left, top, right, bottom;
		
		
		//draw horizontal lines
		for (int i = 0; i < row ; i++) {
			canvas.drawLine(0, moveYOffset + firstCellHeight + cellHeight * i, width, moveYOffset + firstCellHeight + cellHeight * i, linePaint);
		}
		
		//draw vertical lines
		for (int i = 0; i < COLUMN ; i++) {
			canvas.drawLine(cellWidth * i, 0, cellWidth * i, height, linePaint);
		}
		
		if (apples == null) {
			return;
		}
		
		if (bitmapApple == null || bitmapError == null) {
			return;
		}

		// draw applse
		for (int i = 0; i < row; i++) {
			for (int j = 0; j < COLUMN; j++) {
				if (apples[i][j] == 0) {
					// do nothing
				}else if(apples[i][j] == 1){
					// draw apples
					//left = (j >= 1 ) ? (j - 1) * cellWidth  + cellWidth : 0;
					left = j * cellWidth;
					top = moveYOffset + ((i >= 1) ? (firstCellHeight + (i - 1) * cellHeight) : (firstCellHeight - cellHeight)); 
					right = (j + 1) * cellWidth;
					bottom = moveYOffset + firstCellHeight + i * cellHeight;
					//rect.set(left, top, right, bottom);
					rect.set(left, top, right, bottom);
					canvas.drawBitmap(bitmapApple, null, rect, applePaint);
					
				}else {
					left = j * cellWidth;
					top = moveYOffset + ((i >= 1) ? (firstCellHeight + (i - 1) * cellHeight) : (firstCellHeight - cellHeight)); 
					right = (j + 1) * cellWidth;
					bottom = moveYOffset + firstCellHeight + i * cellHeight;
					//rect.set(left, top, right, bottom);
					rect.set(left, top, right, bottom);
					canvas.drawBitmap(bitmapError, null, rect, applePaint);
					
				}
			}
			
		}
		
		
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		width = MeasureSpec.getSize(widthMeasureSpec);
		cellWidth = width / COLUMN;
		cellHeight = cellWidth;
		
		height = MeasureSpec.getSize(heightMeasureSpec);
		firstCellHeight = height % cellHeight;
		row  = height / cellHeight;
		
		if (firstCellHeight > 0) {
			row  += 1;
		}
		
		moveStepHeight = cellHeight / 3;
		
		if (apples == null) {
			apples = new int[row][COLUMN];
			randomApples();
		}


	}
	
	public void reset(){
		this.score = 0;
		running = false;
		randomApples();
		moveYOffset = 0;
		invalidate();
	}
	
	private void randomApples(){
		int columnIndex;
		for (int i = 0; i < row; i++) {
			for (int j = 0; j < COLUMN; j++) {
				apples[i][j] = 0;
			}
		}

		for (int i = 0; i < row - 1; i++) {
			columnIndex = random.nextInt(COLUMN);
			apples[i][columnIndex] = 1;
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
			
			if (y < height - 2 * cellHeight
					|| y > height - cellHeight) {
				// wrong, gamve over
				return false;
			}
			
			int x_index = x / cellWidth;
			
			//game over
			if (apples[row - 2][x_index] != 1) {
				apples[row - 2][x_index] = 3;
				
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
				if (!running) {
					running = true;
					if (listner != null) {
						listner.onGameStart();
					}
				}
				
				// move down
				score ++;
				startMoveAnimation();
			}

			// right, move
			// wrong, game over
			
		}
		
		return false;
	}


	public void setGameEventListener(GameEventListner listner){
		this.listner = listner;
	}
	
	private void startMoveAnimation(){
		handler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				if (moveYOffset < cellHeight) {
					moveYOffset += moveStepHeight;
					invalidate();
					postDelayed(this, 25);
				}else {
					moveYOffset = 0;
					for (int i = row - 2; i > 0; i--) {
						for (int j = 0; j < COLUMN; j++) {
							apples[i][j] = apples[i - 1][j]; 
							apples[i - 1][j] = 0;
						}
					}
					int x_index = random.nextInt(COLUMN);
					apples[0][x_index] = 1;
					invalidate();
				}
			}
		}, 25);
	}
	
	
	public interface GameEventListner{
		public void onGameOver(int score);
		public void onGameStart();
	}
	
	public int getScore(){
		return this.score;
	}
}

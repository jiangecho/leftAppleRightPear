package com.echo.leftAppleRightPear;

public interface GameEventListener {
	public void onGameOver(int score);
	public void onGameStart();
	public void onNewFruit(boolean status);
}

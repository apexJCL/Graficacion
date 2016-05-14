package com.itc.e1;

import com.badlogic.gdx.Game;
import com.itc.e1.Screens.MainScreen;

public class MainClass extends Game{
	
	@Override
	public void create () {
        setScreen(new MainScreen());
	}

	@Override
	public void render () {
        super.render();
	}
}

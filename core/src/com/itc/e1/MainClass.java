package com.itc.e1;

import com.badlogic.gdx.Game;
import com.itc.e1.Screens.MainScreen;
import com.kotcrab.vis.ui.VisUI;

public class MainClass extends Game{
	
	@Override
	public void create () {
		if(VisUI.isLoaded())
			VisUI.dispose();
        setScreen(new MainScreen());
	}

	@Override
	public void render () {
        super.render();
	}
}

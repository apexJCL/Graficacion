package com.itc.e1;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.itc.e1.Custom.SceneDescriptor;
import com.itc.e1.Screens.MainScreen;
import com.kotcrab.vis.ui.VisUI;

public class MainClass extends Game{
	
	@Override
	public void create () {
        test();
		if(VisUI.isLoaded())
			VisUI.dispose();
        setScreen(new MainScreen());
	}

	@Override
	public void render () {
        super.render();
	}

	public void test(){
        SceneDescriptor scene = new SceneDescriptor();
        scene.sceneName = "Prueba";
        scene.defaultModelName = "cubo";
        Array<String> modelRoutes = new Array<String>();
        modelRoutes.add("models/cube/cube.g3db");
        modelRoutes.add("models/ball/ball.g3db");
        modelRoutes.add("models/torus/torus.g3db");
        scene.modelRoutes = modelRoutes;
        FileHandle file = Gdx.files.local("test.txt");
        file.writeString(new Json().prettyPrint(scene), false);
    }
}

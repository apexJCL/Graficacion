package com.itc.e1.Custom;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.math.Vector3;

public class CameraHandler extends CameraInputController {

    private ModelInstance model;

    public CameraHandler(Camera camera) {
        super(camera);
    }

    @Override
    protected boolean process(float deltaX, float deltaY, int button) {

        if(model != null){
            if(Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                model.transform.rotate(new Vector3(0, 1, 0), deltaX*100);
                model.transform.rotate(new Vector3(0, 0, 1), deltaY*100);
                return true;
            }
        }

        return super.process(deltaX, deltaY, button);
    }

    public void setModel(ModelInstance model){
        this.model = model;
    }
}

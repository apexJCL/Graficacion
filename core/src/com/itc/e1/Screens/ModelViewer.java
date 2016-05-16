package com.itc.e1.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.itc.e1.Custom.CameraHandler;

import java.util.StringTokenizer;

public class ModelViewer {
    private final Environment environment;
    private final ColorAttribute ambientLight;
    private final DirectionalLight directionalLight;
    private final PerspectiveCamera camera;
    private final float FOV = 20;
    private final CameraHandler cameraController;
    private final AssetManager assets;
    private ModelBatch modelBatch;
    private boolean loading = true;
    private Array<ModelInstance> instances = new Array<ModelInstance>(0);

    public ModelViewer(){
        modelBatch = new ModelBatch();
        environment = new Environment();
        ambientLight = new ColorAttribute(ColorAttribute.AmbientLight, 1, 1, 1, 1); // Cambiar
        environment.set(ambientLight);
        directionalLight = new DirectionalLight().set(0.8f, 0.8f, 0.8f, 1, 1, 1); // Cambiar
        environment.add(directionalLight);
        camera = new PerspectiveCamera(FOV, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(1, 1, 1);
        camera.lookAt(0,0,0);
        camera.near = 1f;
        camera.far = 300f;
        camera.update();
        cameraController = new CameraHandler(camera);
        assets = new AssetManager();
        preload();
    }

    public void updateFrustum(float value){
        camera.fieldOfView = value;
        camera.update();
    }

    public float getFrustum(){
        return camera.fieldOfView;
    }

    private void preload() {
        // Leer archivo y agregar a cola de carga
        FileHandle list = Gdx.files.internal("models.txt");
        String modelList = list.readString();
        StringTokenizer st = new StringTokenizer(modelList, "\r\n");
        while(st.hasMoreElements())
            assets.load(st.nextToken(), Model.class);
    }

    public void render(){

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        if(assets.update() && loading)
            doneLoading();

        modelBatch.begin(camera);
        modelBatch.render(instances, environment);
        modelBatch.end();

    }

    private void doneLoading() {
        Model handGun = assets.get("models/handgun/handgun.g3db", Model.class);
        ModelInstance handgunInstance = new ModelInstance(handGun);
        cameraController.setModel(handgunInstance);
        instances.add(handgunInstance);
        Array<Model> models = new Array<Model>();
        assets.getAll(Model.class, models);
        for(Model model: models){
            if(model != handGun)
                instances.add(new ModelInstance(model));
        }
        loading = false;
    }

    public void resize(int width, int height){
        camera.viewportWidth = width;
        camera.viewportHeight = height;
    }

    public InputProcessor getInputProcessor() {
        return cameraController;
    }

    public ColorAttribute getAmbientLight() {
        return ambientLight;
    }

    public void updateColor(Color color, ColorSelector selector){
        switch (selector){
            case AMBIENT:
                ambientLight.color.set(color);
                break;
            case DIRECTIONAL:
                directionalLight.color.set(color);
                break;
            case DEFAULT:
                break;
        }
    }

    public Vector3 getDirectionalCoordinates(){
        return directionalLight.direction;
    }

    public void updateDirectionalLight(float x, float y, float z){
        directionalLight.direction.set(directionalLight.direction.x + x, directionalLight.direction.y + y, directionalLight.direction.z + z);
    }

    public ModelInstance getDesignated(){
        return cameraController.getModel();
    }

    public enum ColorSelector{
        AMBIENT, DIRECTIONAL, DEFAULT
    }
}

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
import com.itc.e1.Custom.SceneDescriptor;

import java.util.StringTokenizer;

public class ModelViewer {
    private Environment environment;
    private ColorAttribute ambientLight;
    private DirectionalLight directionalLight;
    private PerspectiveCamera camera;
    private float FOV = 20;
    private CameraHandler cameraController;
    private AssetManager assets;
    private ModelBatch modelBatch;
    private boolean loading = true;
    private Array<ModelInstance> instances = new Array<ModelInstance>(0);
    private String route;
    private SceneDescriptor sceneDescriptor;

    public ModelViewer(String sceneName){
        init();
        route = "scenes/"+sceneName+"/";
        preload_Normal();
    }

    public ModelViewer(SceneDescriptor sceneDescriptor) {
        this.sceneDescriptor = sceneDescriptor;
        init();
        loadSceneDescriptor();
    }

    private void init(){
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
    }

    public void updateFrustum(float value){
        camera.fieldOfView = value;
        camera.update();
    }

    public float getFrustum(){
        return camera.fieldOfView;
    }

    private void preload_Normal() {
        // Leer archivo y agregar a cola de carga
        FileHandle list = Gdx.files.internal(route+"/models.txt");
        String modelList = list.readString();
        StringTokenizer st = new StringTokenizer(modelList, "\r\n");
        while(st.hasMoreElements())
            assets.load(route+st.nextToken(), Model.class);
    }


    private void loadSceneDescriptor() {
        for(String model: sceneDescriptor.modelRoutes)
            assets.load(sceneDescriptor.PATH+model, Model.class);
    }

    public void render(){

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        if(assets.update() && loading) {
            if (sceneDescriptor != null)
                doneLoadingScene();
            else
                doneLoading();
        }

        modelBatch.begin(camera);
        modelBatch.render(instances, environment);
        modelBatch.end();

    }

    private void doneLoading() {
        String tmp = Gdx.files.internal(route+"default.txt").readString();
        String modelName = tmp.substring(0, tmp.indexOf("\r\n"));
        Model defModel = assets.get(route+"models/"+modelName+"/"+modelName+".g3db", Model.class);
        ModelInstance defModelInstance = new ModelInstance(defModel);
        __asset_loader(defModel, defModelInstance);
        loading = false;
    }

    private void doneLoadingScene(){
        Model defModel = assets.get(sceneDescriptor.PATH + sceneDescriptor.defaultModelName, Model.class);
        ModelInstance defModelInstance = new ModelInstance(defModel);
        __asset_loader(defModel, defModelInstance);
        loading = false;
    }

    private void __asset_loader(Model defModel, ModelInstance defModelInstance){
        cameraController.setModel(defModelInstance);
        instances.add(defModelInstance);
        Array<Model> models = new Array<Model>();
        assets.getAll(Model.class, models);
        for(Model model: models){
            if(model != defModel)
                instances.add(new ModelInstance(model));
        }
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

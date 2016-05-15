package com.itc.e1.Screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.*;
import com.kotcrab.vis.ui.widget.color.ColorPicker;
import com.kotcrab.vis.ui.widget.color.ColorPickerAdapter;

public class MainScreen implements Screen {

    private OrthographicCamera renderCamera;
    private boolean debug = false;
    private boolean debugOpenGL = false;
    private InputMultiplexer inputMultiplexer;
    private ModelViewer viewer;
    private MenuBar menuBar;
    public Stage stage;
    private Table root;
    private Table container;
    private ColorPicker colorPicker;
    private VisWindow window;
    private ModelViewer.ColorSelector selector = ModelViewer.ColorSelector.AMBIENT;
    private VisLabel directionalPositionLabel;
    private VisLabel directionalPositionValue;

    public MainScreen() {
        if (Gdx.app.getType() == Application.ApplicationType.Desktop)
            VisUI.load(VisUI.SkinScale.X1);
        else
            VisUI.load(VisUI.SkinScale.X2);
        // Creamos el visor
        viewer = new ModelViewer();
        // Instanciamos cosas
        instantiateThings();
        // Creamos el multipelxor de entrada
        inputMultiplexer = new InputMultiplexer(stage, viewer.getInputProcessor());
        // Asignamos la entrada
        Gdx.input.setInputProcessor(inputMultiplexer);
        Gdx.input.setCatchBackKey(false);
    }

    private void instantiateThings() {
        stage = new Stage(new ScreenViewport());
        renderCamera = new OrthographicCamera();
        renderCamera.setToOrtho(false, stage.getViewport().getScreenWidth(), stage.getViewport().getScreenHeight());
        // UI Placeholder
        root = new Table();
        root.setFillParent(true);
        container = new Table();
        // UI Stuff
        createMenu();
        // Final Step
        colorPicker = new ColorPicker(new ColorPickerAdapter() {
            @Override
            public void changed(Color newColor) {
                viewer.updateColor(newColor, selector);
            }

            @Override
            public void finished(Color newColor) {
                colorPicker.setColor(newColor);
                colorPicker.fadeOut();
            }
        });
        // Frustum Window
        window = new VisWindow("Frustum");
        window.addCloseButton();
        final VisLabel frustumValue = new VisLabel("0.0");
        VisSlider frustumSlider = new VisSlider(1, 120, 1,false);
        frustumSlider.setValue(viewer.getFrustum());
        frustumSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if(actor instanceof VisSlider) {
                    viewer.updateFrustum(((VisSlider) actor).getValue());
                    frustumValue.setText(String.valueOf(((VisSlider)actor).getValue()));
                }
            }
        });
        window.add(frustumSlider).row();
        window.add(frustumValue);
        window.pack();
        colorPicker.setColor(1, 1, 1, 1);
        root.add(menuBar.getTable()).fillX().expandX().top().pad(0).row();
        root.add(container).fill().expand().row();
        stage.addActor(root);
    }

    private void createMenu() {
        // Menu Bar
        menuBar = new MenuBar();
        // Menu Objects
        Menu fileMenu = new Menu("Inicio");
        Menu configMenu = new Menu("Configuración");
        // File Items
        MenuItem closeAppItem = new MenuItem("Salir");
        closeAppItem.addListener(new MenuListener(Action.EXIT));
        fileMenu.addItem(closeAppItem);
        // COnfig Menu Items
        MenuItem debugToggleItem = new MenuItem("Debug VisUI");
        MenuItem debugOpenGLItem = new MenuItem("Debug OpenGL Values");
        MenuItem ambientLightItem = new MenuItem("Luz Ambiental");
        MenuItem directionalLightItem = new MenuItem("Luz Direccional");
        debugToggleItem.addListener(new MenuListener(Action.DEBUG));
        debugOpenGLItem.addListener(new MenuListener(Action.DEBUG_GL));
        ambientLightItem.addListener(new MenuListener(Action.AMBIENT));
        directionalLightItem.addListener(new MenuListener(Action.DIRECTIONAL));
        // Add to menu
        configMenu.addItem(debugToggleItem);
        configMenu.addItem(debugOpenGLItem);
        configMenu.addItem(ambientLightItem);
        configMenu.addItem(directionalLightItem);
        // Add Debug Info
        container.setVisible(false);
        directionalPositionLabel = new VisLabel("Posición Luz Direccional: ");
        directionalPositionValue = new VisLabel("");
        container.add(directionalPositionLabel).expand().top().left();
        container.add(directionalPositionValue).expand().top().left().row();
        // Final
        menuBar.addMenu(fileMenu);
        menuBar.addMenu(configMenu);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        keyListener(delta);
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        viewer.render();
        stage.act(delta);
        stage.draw();

        if(Gdx.input.isKeyJustPressed(Input.Keys.BACK)){
            Gdx.app.exit();
        }

        if(debugOpenGL)
            updateData();
    }

    private void updateData() {
        Vector3 dCoord = viewer.getDirectionalCoordinates();
        directionalPositionValue.setText("X: "+ String.valueOf(dCoord.x)+", Y: "+String.valueOf(dCoord.y)+", Z: "+String.valueOf(dCoord.z));
    }

    private void keyListener(float delta) {
        if(Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            window.centerWindow();
            root.addActor(window.fadeIn());
        }

        if(Gdx.input.isKeyPressed(Input.Keys.LEFT))
            viewer.updateDirectionalLight(1*delta, 0, 0);
        if(Gdx.input.isKeyPressed(Input.Keys.RIGHT))
            viewer.updateDirectionalLight(-1*delta, 0, 0);
        if(Gdx.input.isKeyPressed(Input.Keys.UP))
            viewer.updateDirectionalLight(0, 1*delta, 0);
        if(Gdx.input.isKeyPressed(Input.Keys.DOWN))
            viewer.updateDirectionalLight(0, -1*delta, 0);
        if(Gdx.input.isKeyPressed(Input.Keys.I))
            viewer.updateDirectionalLight(0, 0, 1*delta);
        if(Gdx.input.isKeyPressed(Input.Keys.K))
            viewer.updateDirectionalLight(0, 0, -1*delta);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        viewer.resize(width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        colorPicker.dispose();
    }

    public enum Action {
        DIRECTIONAL, AMBIENT, DEBUG, DEBUG_GL,EXIT
    }

    public class MenuListener extends ClickListener {

        private Action action;

        public MenuListener(Action action) {
            this.action = action;
        }

        @Override
        public void clicked(InputEvent event, float x, float y) {
            switch (action) {
                case DIRECTIONAL:
                    selector = ModelViewer.ColorSelector.DIRECTIONAL;
                    colorPicker.getTitleLabel().setText("Color Direccional");
                    colorPicker.setColor(viewer.getAmbientLight().color);
                    root.addActor(colorPicker.fadeIn());
                    break;
                case AMBIENT:
                    selector = ModelViewer.ColorSelector.AMBIENT;
                    colorPicker.getTitleLabel().setText("Color Ambiente");
                    colorPicker.setColor(viewer.getAmbientLight().color);
                    root.addActor(colorPicker.fadeIn());
                    break;
                case DEBUG:
                    stage.setDebugAll(!debug);
                    debug = !debug;
                    break;
                case DEBUG_GL:
                    debugOpenGL = !debugOpenGL;
                    container.setVisible(debugOpenGL);
                    break;
                case EXIT:
                    Gdx.app.exit();
                    break;
            }
        }
    }
}

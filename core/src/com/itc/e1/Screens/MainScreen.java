package com.itc.e1.Screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.Menu;
import com.kotcrab.vis.ui.widget.MenuBar;
import com.kotcrab.vis.ui.widget.MenuItem;
import com.kotcrab.vis.ui.widget.color.ColorPicker;
import com.kotcrab.vis.ui.widget.color.ColorPickerAdapter;

public class MainScreen implements Screen {

    private OrthographicCamera renderCamera;
    private boolean debug = false;
    private InputMultiplexer inputMultiplexer;
    private ModelViewer viewer;
    private MenuBar menuBar;
    public Stage stage;
    private Table root;
    private Table container;
    private ColorPicker colorPicker;
    private ModelViewer.ColorSelector selector = ModelViewer.ColorSelector.AMBIENT;

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
        MenuItem debugToggleItem = new MenuItem("Debug Stage");
        debugToggleItem.addListener(new MenuListener(Action.DEBUG));
        configMenu.addItem(debugToggleItem);
        // Final
        menuBar.addMenu(fileMenu);
        menuBar.addMenu(configMenu);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        keyListener();
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        viewer.render();
        stage.act(delta);
        stage.draw();
    }

    private void keyListener() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.A)) {
            selector = ModelViewer.ColorSelector.AMBIENT;
            colorPicker.getTitleLabel().setText("Color Ambiente");
            colorPicker.setColor(viewer.getAmbientLight().color);
            root.addActor(colorPicker.fadeIn());
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.D)){
            selector = ModelViewer.ColorSelector.DIRECTIONAL;
            colorPicker.getTitleLabel().setText("Color Direccional");
            colorPicker.setColor(viewer.getAmbientLight().color);
            root.addActor(colorPicker.fadeIn());
        }
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
        DEBUG, EXIT
    }

    public class MenuListener extends ClickListener {

        private Action action;

        public MenuListener(Action action) {
            this.action = action;
        }

        @Override
        public void clicked(InputEvent event, float x, float y) {
            switch (action) {
                case DEBUG:
                    stage.setDebugAll(!debug);
                    debug = !debug;
                    break;
                case EXIT:
                    Gdx.app.exit();
                    break;
            }
        }
    }
}

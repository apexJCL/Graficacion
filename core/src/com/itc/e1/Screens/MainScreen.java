package com.itc.e1.Screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.itc.e1.Custom.SceneDescriptor;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.*;
import com.kotcrab.vis.ui.widget.color.ColorPicker;
import com.kotcrab.vis.ui.widget.color.ColorPickerAdapter;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;

import java.util.StringTokenizer;

public class MainScreen implements Screen {

    private OrthographicCamera renderCamera;
    private boolean debug = false;
    private boolean debugOpenGL = false;
    private boolean rotate = false;
    private InputMultiplexer inputMultiplexer;
    private ModelViewer viewer;
    private MenuBar menuBar;
    public Stage stage;
    private Table root;
    private Table container;
    private ColorPicker colorPicker;
    private VisWindow frustumWindow;
    private ModelViewer.ColorSelector selector = ModelViewer.ColorSelector.DEFAULT;
    private VisLabel directionalPositionLabel;
    private VisLabel directionalPositionValue;
    private FileChooser fileChooser;
    private Menu sceneMenu;

    public MainScreen() {
        if (Gdx.app.getType() == Application.ApplicationType.Desktop)
            VisUI.load(VisUI.SkinScale.X1);
        else
            VisUI.load(VisUI.SkinScale.X2);
        // Creamos el visor
        viewer = new ModelViewer("test");
        // Instanciamos cosas
        instantiateThings();
        // Preload scene selector
        preloadScenes();
        loadScene();
    }

    private void loadScene() {
        // Creamos el multiplexor de entrada
        inputMultiplexer = new InputMultiplexer(stage, viewer.getInputProcessor());
        // Asignamos la entrada
        Gdx.input.setInputProcessor(inputMultiplexer);
        Gdx.input.setCatchBackKey(false);
    }

    private void preloadScenes() {
        FileHandle list = Gdx.files.internal("scenes.txt");
        String modelList = list.readString();
        StringTokenizer st = new StringTokenizer(modelList, "\r\n");
        while (st.hasMoreElements()) {
            MenuItem tmp = new MenuItem(st.nextToken());
            tmp.addListener(new MenuListener(Action.CUSTOM_SCENE));
            sceneMenu.addItem(tmp);
        }
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
        frustumWindow = new VisWindow("Frustum");
        frustumWindow.addCloseButton();
        final VisLabel frustumValue = new VisLabel("0.0");
        VisSlider frustumSlider = new VisSlider(1, 120, 1, false);
        frustumSlider.setValue(viewer.getFrustum());
        frustumSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (actor instanceof VisSlider) {
                    viewer.updateFrustum(((VisSlider) actor).getValue());
                    frustumValue.setText(String.valueOf(((VisSlider) actor).getValue()));
                }
            }
        });
        frustumWindow.add(frustumSlider).row();
        frustumWindow.add(frustumValue);
        frustumWindow.pack();
        // End Frustum Window
        colorPicker.setColor(1, 1, 1, 1);
        root.add(menuBar.getTable()).fillX().expandX().top().pad(0).row();
        root.add(container).fill().expand().row();
        // File Chooser
        FileChooser.setFavoritesPrefsName("Graficacion");
        fileChooser = new FileChooser("Cargar", FileChooser.Mode.OPEN);
        fileChooser.setListener(new FileChooserAdapter(){
            @Override
            public void selected(Array<FileHandle> files) {
                try {
                    SceneDescriptor sceneDescriptor = new Json().fromJson(SceneDescriptor.class, files.first());
                    String tmp = files.first().path();
                    sceneDescriptor.PATH = tmp.substring(0, tmp.indexOf(files.first().name()));
                    loadJSON(sceneDescriptor);
                } catch (Exception e){
                    VisWindow message = new VisWindow("Error");
                    message.addCloseButton();
                    message.closeOnEscape();
                    message.add(new VisLabel("Hubo un error al cargar la escena"));
                    message.pack();
                    root.add(message.fadeIn());
                    message.centerWindow();
                }
                super.selected(files);
            }
        });
        stage.addActor(root);
    }

    private void loadJSON(final SceneDescriptor sceneDescriptor){

        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                viewer = new ModelViewer(sceneDescriptor);
                loadScene();
            }
        });
    }

    private void createMenu() {
        // Menu Bar
        menuBar = new MenuBar();
        // Menu Objects
        Menu fileMenu = new Menu("Inicio");
        sceneMenu = new Menu("Escenas");
        Menu configMenu = new Menu("Configuración");
        // File Items
        MenuItem closeAppItem = new MenuItem("Salir");
        MenuItem openFileItem = new MenuItem("Cargar Escena");
        openFileItem.addListener(new MenuListener(Action.OPEN_FILE));
        closeAppItem.addListener(new MenuListener(Action.EXIT));
        fileMenu.addItem(openFileItem);
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
        menuBar.addMenu(sceneMenu);
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

        if (Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            Gdx.app.exit();
        }

        if (debugOpenGL)
            updateData();

        if (viewer.getDesignated() != null && rotate) {
            viewer.getDesignated().transform.rotate(new Vector3(0, 1, 0), 1);
        }
    }

    private void updateData() {
        Vector3 dCoord = viewer.getDirectionalCoordinates();
        directionalPositionValue.setText("X: " + String.valueOf(dCoord.x) + ", Y: " + String.valueOf(dCoord.y) + ", Z: " + String.valueOf(dCoord.z));
    }

    private void keyListener(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            frustumWindow.centerWindow();
            root.addActor(frustumWindow.fadeIn());
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.R))
            rotate = !rotate;

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
            viewer.updateDirectionalLight(1 * delta, 0, 0);
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
            viewer.updateDirectionalLight(-1 * delta, 0, 0);
        if (Gdx.input.isKeyPressed(Input.Keys.UP))
            viewer.updateDirectionalLight(0, 1 * delta, 0);
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN))
            viewer.updateDirectionalLight(0, -1 * delta, 0);
        if (Gdx.input.isKeyPressed(Input.Keys.I))
            viewer.updateDirectionalLight(0, 0, 1 * delta);
        if (Gdx.input.isKeyPressed(Input.Keys.K))
            viewer.updateDirectionalLight(0, 0, -1 * delta);
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
        OPEN_FILE, CUSTOM_SCENE, DIRECTIONAL, AMBIENT, DEBUG, DEBUG_GL, EXIT
    }

    public class MenuListener extends ClickListener {

        private Action action;

        public MenuListener(Action action) {
            this.action = action;
        }

        @Override
        public void clicked(InputEvent event, float x, float y) {
            switch (action) {
                case OPEN_FILE:
                    root.addActor(fileChooser.fadeIn());
                    break;
                case CUSTOM_SCENE:
                    final Actor target = event.getTarget();
                    if (target instanceof Label) {
                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                viewer = new ModelViewer(((Label) target).getText().toString());
                                loadScene();
                            }
                        });
                    }
                    break;
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

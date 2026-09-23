package com.tableverse.prototype01.hud;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.tableverse.prototype01.simulation.SimCharacter;
import com.tableverse.prototype01.simulation.Simulation;

/**
 * Scene2D status panel and pause control. Reads simulation state; does not own character state.
 * Uses its own viewport, separate from the world camera/viewport, and gets first opportunity at input.
 */
public class GameHud {

    private final Stage stage;
    private final Label nameLabel;
    private final Label positionLabel;
    private final Label orderLabel;
    private final Label hintLabel;
    private final TextButton pauseButton;

    public GameHud(Skin skin) {
        Viewport viewport = new ScreenViewport();
        stage = new Stage(viewport);

        Table root = new Table();
        root.setFillParent(true);
        root.top().left().pad(10);
        stage.addActor(root);

        Table panel = new Table(skin);
        panel.defaults().left().pad(2);

        Label title = new Label("Selected Character", skin);
        nameLabel = new Label("Name: (none)", skin);
        positionLabel = new Label("Position: -", skin);
        orderLabel = new Label("Order: -", skin);

        panel.add(title).row();
        panel.add(nameLabel).row();
        panel.add(positionLabel).row();
        panel.add(orderLabel).row();

        pauseButton = new TextButton("Pause", skin);
        panel.add(pauseButton).padTop(6).row();

        root.add(panel);

        hintLabel = new Label("Left-click select · Right-click move", skin);
        Table hintTable = new Table();
        hintTable.setFillParent(true);
        hintTable.bottom().pad(8);
        hintTable.add(hintLabel);
        stage.addActor(hintTable);
    }

    public Stage getStage() {
        return stage;
    }

    public void setPauseClickedListener(Runnable listener) {
        pauseButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                listener.run();
            }
        });
    }

    public void update(Simulation simulation) {
        SimCharacter selected = simulation.getSelectedCharacter();
        if (selected == null) {
            nameLabel.setText("Name: (none)");
            positionLabel.setText("Position: -");
            orderLabel.setText("Order: -");
        } else {
            nameLabel.setText("Name: " + selected.getName());
            positionLabel.setText(String.format("Position: (%.0f, %.0f)", selected.getX(), selected.getY()));
            orderLabel.setText("Order: " + selected.getState());
        }
        pauseButton.setText(simulation.isPaused() ? "Resume" : "Pause");
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void dispose() {
        stage.dispose();
    }
}

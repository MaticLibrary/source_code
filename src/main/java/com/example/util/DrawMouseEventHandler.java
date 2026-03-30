package com.example.util;


import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import lombok.Setter;

public class DrawMouseEventHandler implements EventHandler<MouseEvent> {

    @Setter
    private EventHandler<MouseEvent> onClickedEventHandler;

    private boolean dragging = false;

    @Override
    public void handle(MouseEvent event) {
        if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
            dragging = false;
        }
        else if (event.getEventType() == MouseEvent.DRAG_DETECTED) {
            dragging = true;
        }
        else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED) {

        }
        else if (event.getEventType() == MouseEvent.MOUSE_CLICKED) {
            if (!dragging && onClickedEventHandler != null) {
                onClickedEventHandler.handle(event);
            }
            dragging = false;
        }

    }
}

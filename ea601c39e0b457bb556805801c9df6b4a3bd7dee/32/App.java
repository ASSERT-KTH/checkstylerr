/*******************************************************************************
 * Copyright (c) 2015 BREDEX GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BREDEX GmbH - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.jubula.examples.aut.towers.of.hanoi.javafx;
 
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
 
@SuppressWarnings("nls")
public class App extends Application {
	
	private final int WIDTH = 1050;
	private final int HEIGHT = 650;
	private final int NUMBER_OF_DISCS = 6;

	public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Towers of Hanoi");
        
        VBox root = new VBox();
        Button resetButton = new Button("Reset");
        HBox main = new HBox();
        root.getChildren().addAll(main, resetButton);
        
        StackPane area0 = new StackPane();
        VBox stack0 = new VBox();
		initArea(area0, stack0, new Image("area0.png"));
		
		StackPane area1 = new StackPane();
        VBox stack1 = new VBox();
		initArea(area1, stack1, new Image("area1.png"));
		
		StackPane area2 = new StackPane();
        VBox stack2 = new VBox();
		initArea(area2, stack2, new Image("area2.png"));
		
        main.getChildren().addAll(area0, area1, area2);
        
		initStacks(stack0, stack1, stack2);
		addDragHandler(stack0);
		addDragHandler(stack1);
		addDragHandler(stack2);
		resetButton.setOnAction(e -> initStacks(stack0, stack1, stack2));
        
		primaryStage.setScene(new Scene(root, WIDTH, HEIGHT));
		primaryStage.setResizable(false);
        primaryStage.show();
    }


    private void initStacks(VBox stack0, VBox stack1, VBox stack2) {
		stack0.getChildren().clear();
		stack1.getChildren().clear();
		stack2.getChildren().clear();
		for (int i = NUMBER_OF_DISCS - 1; i >= 0; i--) {
			ImageView r = new ImageView(new Image("" + i + ".png"));
			r.setId("Image_" + i);
        	stack0.getChildren().add(r);
        }
	}

	private void addDragHandler(Node n) {
		n.setOnDragDetected(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				final Object source = event.getSource();
				if (source instanceof Pane) {
					Pane r = (Pane) source;
					ObservableList<Node> childrenOfPane = r.getParent().getChildrenUnmodifiable();
					if (childrenOfPane.size() == 2) {
						Node secondChild = childrenOfPane.get(1);
						if (secondChild instanceof VBox) {
							VBox stack = (VBox) secondChild;
							if (!stack.getChildren().isEmpty()) {	
								Dragboard db = r.startDragAndDrop(TransferMode.MOVE);
								ClipboardContent content = new ClipboardContent();
								content.putString(String.valueOf(((Pane)source).getMaxWidth()));
								db.setContent(content);
								event.consume();
							}
						}
					}
				}
			}
		});
		
		n.setOnDragDone(new EventHandler<DragEvent>() {
			public void handle(DragEvent event) {
				final Object source = event.getGestureSource();
				if (event.isDropCompleted() && source instanceof Pane) {
					Pane r = (Pane) source;
					ObservableList<Node> discs = r.getChildren();
					if (!discs.isEmpty()) {								
						discs.remove(0);
					}
				}
			}
		});
	}
	
	private void addDropHandler(Pane n) {
		n.setOnDragOver(new EventHandler<DragEvent>() {
		    public void handle(DragEvent event) {
	            event.acceptTransferModes(TransferMode.MOVE);
		        event.consume();
		    }
		});
		
		n.setOnDragDropped(new EventHandler<DragEvent>() {
			public void handle(DragEvent event) {
				final Object source = event.getGestureSource();
				if (source instanceof Pane) {
					Pane r = (Pane) source;
					ImageView image = (ImageView) r.getChildren().get(0);
					final Object target = event.getGestureTarget();
					if (target instanceof Pane) {
						Platform.runLater(new Runnable() {
	                         @Override
	                         public void run() {
	                        	 Pane pane = ((Pane) target);
	                        	 Dragboard db = event.getDragboard();
	                             boolean success = false;
	                             ObservableList<Node> childrenOfPane = pane.getChildrenUnmodifiable();
	                             if(childrenOfPane.size() == 2) {
	                            	 Node secondChild = childrenOfPane.get(1);
	                            	 if (secondChild instanceof VBox) {
	                            		 VBox stack = (VBox) secondChild;
	                            		 ObservableList<Node> childrenOfStack = stack.getChildren();
	                            		 if (childrenOfStack.isEmpty()
	                            				 || image.getImage().getWidth() < ((ImageView) childrenOfStack.get(0)).getImage().getWidth()) {
	                            			 success = true;
	                            			 childrenOfStack.add(0, image);
	                            		 }
	                            		 event.setDropCompleted(success);
	                            		 db.clear();
	                            	 }
	                             }
	                         }
						});
					}
				}
			}
		});
	}

	private void initArea(StackPane stack, VBox vBox, Image image) {
		vBox.setAlignment(Pos.BOTTOM_CENTER);
		vBox.setPrefWidth(WIDTH / 3.0);
		addDropHandler(stack);
		stack.getChildren().addAll(new ImageView(image), vBox);
	}
}
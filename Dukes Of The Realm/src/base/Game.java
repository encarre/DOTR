package base;

import buildings.Castle;

import javafx.scene.control.*;
import renderer.*;

import javafx.animation.AnimationTimer;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import renderer.Button;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import alorithms.AEtoile;
import alorithms.Node;

public class Game {
	private Group root;
	private Pane renderLayer;
	private Input input;
	public static int[][]tab = new int[Settings.gridCellsCountX/Settings.cellSize][Settings.gridCellsCountY/Settings.cellSize];

	private Random rdGen = new Random();

	private GameMode gameMode;

	public Game() {
		this.root = new Group();
		this.root.getStylesheets().add("resources/css/application.css");

		this.renderLayer = new Pane();
		this.renderLayer.setPrefSize(Settings.windowWidth, Settings.windowHeight);
		this.renderLayer.setOnMouseClicked(e -> {
			if (this.gameMode == GameMode.Menu) {
				for (StatusBar statusBar : statusBars) {
					statusBar.setDefaultMenuView();
				}
			} else if (this.gameMode == GameMode.Game) {
				for (StatusBar statusBar : statusBars) {
					statusBar.setDefaultGameView();
				}
			}
			e.consume();
		});

		root.getChildren().add(this.renderLayer);

		gameMode = GameMode.Menu;
	}

	private boolean isRunning = true;
	private int frameCounter = 0;
	private int framesPerDay = 60; // One second.

	private DayHolder currentDayHolder = new DayHolder();

	public void run() {
		loadGame();

		AnimationTimer gameLoop = new AnimationTimer() {
			@Override
			public void handle(long now) {
				processInput(input, now);
				for (StatusBar statusBar : statusBars) {
					statusBar.updateView();
				}

				if (isRunning) {
					processInput(input, now);

					if (gameMode == GameMode.Game) {
						++frameCounter;
						if (frameCounter >= framesPerDay) {
							frameCounter -= framesPerDay;
							++currentDayHolder.day;

							for (Castle castle : castles) {
								castle.onUpdate();
							}
						}
					}
				}
			}

			private void processInput(Input input, long now) {
				if (input.isKeyPressed(KeyCode.SPACE)) {
					isRunning = !isRunning;
				}

				if (input.isKeyPressed(KeyCode.ESCAPE)) {
//					openQuickMenu();
				}
			}
		};
		gameLoop.start();
	}

	private Background menuBackground;
	private Background gameBackground;

	private void loadGame() {
		input = new Input(this.root.getScene());

		menuBackground = new Background(renderLayer, new Image("resources/sprites/backgrounds/menu_background.png"));
		gameBackground = new Background(renderLayer, new Image("resources/sprites/backgrounds/game_background.png"));

		// TODO: Initialise input and add listeners
		input.addListeners();

		createStatusBar();

		createMenuButtons();

		// Set to default view
		setMenuView();
	}

	private List<Button> defaultMenuButtons = new ArrayList<>();

	private void createMenuButtons() {
		Image texture = new Image("resources/sprites/buttons/new_game.png");

    	final double buttonWidth = texture.getWidth();
    	final double buttonHeight = texture.getHeight();
    	final double buttonPosX = (Settings.windowWidth - buttonWidth) / 2.0;

    	String[] buttonsPath = new String[3];
    	buttonsPath[0] = "new_game.png";
    	buttonsPath[1] = "load_game.png";
    	buttonsPath[2] = "credits.png";

		Point2D buttonPos = new Point2D(0, 0);
		Button button;

		for (String buttonPath : buttonsPath) {
			buttonPos = new Point2D(buttonPosX, buttonPos.getY() + 2 * buttonHeight);
			button = new Button(renderLayer, buttonPos, new Image("resources/sprites/buttons/" + buttonPath));

			defaultMenuButtons.add(button);
		}

		// TODO: Is there a way to store functions in array?
		defaultMenuButtons.get(0).getTextureView().setOnMouseClicked(e -> {
			setGameView();
			e.consume();
		});

		defaultMenuButtons.get(1).getTextureView().setOnMouseClicked(e -> {
//			setLoadGameView();
			e.consume();
		});

		defaultMenuButtons.get(2).getTextureView().setOnMouseClicked(e -> {
			setCreditsView();
			e.consume();
		});
	}

	private List<Castle> castles = new ArrayList<>();

	private void createCastles() {
		final int widthUpperBound = Settings.gridCellsCountX - Settings.castleSize;
		final int heightUpperBound = Settings.gridCellsCountY - Settings.castleSize;

		final int nbActiveDukes = rdGen.nextInt(Settings.nbMaxActiveDukes);
		final int nbNeutralDukes = Settings.nbMinCastles + rdGen.nextInt(Settings.nbMaxCastles - nbActiveDukes) - 1;
		final int nbCastles = 1 + nbActiveDukes + nbNeutralDukes;

		// 0 is the player
		// [1, nbActiveDukes] is for active dukes
		// [1+nbActiveDukes, 1+nbActiveDukes+nbNeutralDukes] is for neutral dukes
		int castleOwner = 0;
		while (castles.size() < nbCastles) {
			int cellSize = Settings.cellSize;
			Point2D position = new Point2D(rdGen.nextInt(widthUpperBound/cellSize)*cellSize, Settings.statusBarHeight + rdGen.nextInt(heightUpperBound/cellSize)*cellSize);
			if (!isPositionNearACastle(position)) {
				castles.add(new Castle(renderLayer, castleOwner, position));
				++castleOwner;
			}
		}

		for (Castle castle : castles) {
			
//			for(int i = 0; i < Settings.castleSize/Settings.cellSize; i++) {
//			for(int j = 0; j < Settings.castleSize/Settings.cellSize ; j++) {
//				double x = castle.getPosition().getX()/Settings.cellSize;
//				double y = castle.getPosition().getY()/Settings.cellSize;
//				Game.tab[(int) x - 1 + i][(int) y - 1 + j] = 1;
//			}
//		}
//
			
			castle.getTextureView().setOnMouseClicked(e -> {
				for (StatusBar statusBar : statusBars) {
					statusBar.setCastleView(castle);
				}
				e.consume();
			});
		}
	}

	private boolean isPositionNearACastle(Point2D position) {
		for (Castle castle : castles) {
			Point2D position2 = castle.getPosition();
			if (position.distance(position2) < Settings.minimumCastleDistance) {
				return true;
			}
		}

		return false;
	}

	private List<StatusBar> statusBars = new ArrayList<>();

	private void createStatusBar() {
		final Point2D statusBarSize = new Point2D(Settings.windowWidth / 3, Settings.statusBarHeight);
		Point2D statusBarPos = new Point2D(0, 0);
		StatusBar leftStatusBar = new StatusBar(renderLayer, statusBarPos, statusBarSize, "leftStatusBar") {
			@Override
			public void updateView() {
				if (getView() == StatusBarView.DefaultMenuView) {
					setText("Bienvenu à Dukes of the Realm.");
				} else if (getView() == StatusBarView.CreditsView) {
					setText("Réalisé par Enzo Carré et Luis L. Marques." + "\n"
							+ "Merci à Morgane de m'avoir harcelé pendant la Nuit de l'Info.");
				} else if (getView() == StatusBarView.DefaultGameView) {
					setText("Jour actuel: " + getCurrentDay());
				} else if (getView() == StatusBarView.CastleView) {
					String text = "Duc du château: " + getCurrentCastle().getOwner() + "\n"
							+ "Niveau: " + getCurrentCastle().getLevel() + "\n"
							+ "Revenu: " + getCurrentCastle().getPassiveIncome() + "\n"
							+ "Trésor: " + getCurrentCastle().getTreasure() + "\n";

					if (getCurrentCastle().isLevelingUp()) {
						text += "Jours jusqu'à évolution: " + getCurrentCastle().getNextLevelRemainingTime();
					}

					setText(text);
				}
			}
		};
		leftStatusBar.setDayHolder(currentDayHolder);
		leftStatusBar.setDefaultMenuView();
		statusBars.add(leftStatusBar);

		statusBarPos = new Point2D(statusBarPos.getX() + Settings.windowWidth / 3, statusBarPos.getY());
		StatusBar centerStatusBar = new StatusBar(renderLayer, statusBarPos, statusBarSize, "centerStatusBar") {
			private int displayLevel = 0;

			private List<Button> buttons1;
			private List<Spinner> recruitSpinners;

			private List<SpinnerValueFactory<Integer>> moveValueFactories;
			private List<Spinner> moveSpinners;

			@Override
			public void updateView() {
				if (getView() == StatusBarView.DefaultGameView) {
					if (displayLevel != 0) {
						for (Button button : buttons1) {
							button.removeFromCanvas();
						}
						displayLevel = 0;
					}
				} else if (getView() == StatusBarView.CastleView) {
					if (displayLevel != 1) {
						for (Button button : buttons1) {
							button.addToCanvas();
						}
						displayLevel = 1;
					}


				} else if (getView() == StatusBarView.TroopsRecruitView) {
					for (Button button : buttons1) {
						button.removeFromCanvas();
					}


				} else if (getView() == StatusBarView.TroopsMoveView) {
					for (Button button : buttons1) {
						button.removeFromCanvas();
					}

//					for (Spinner spinner : moveSpinners) {
//						spinner.setVisible(true);
//					}
					Image target = new Image("resources/sprites/castles/target.png");
					for(Castle castle : castles) {
						if(castle.getOwner() == 0) {
							continue;
						}
						Point2D pos = new Point2D(castle.getPosition().getX()-10, castle.getPosition().getY()-10);
						Button btn = new Button(renderLayer, pos, target);
						btn.addToCanvas();
						btn.getTextureView().setPickOnBounds(true);
						btn.getTextureView().setOnMouseClicked(e -> {
							Node start = new Node(getCurrentCastle().getPosition().getX(), getCurrentCastle().getPosition().getY(),0,0);
							Node end = new Node(castle.getPosition().getX(),castle.getPosition().getY(),0,0);
							AEtoile.CheminPlusCourt(start, end, renderLayer, true);							
							e.consume();
						});
						buttons1.add(btn);
					}
					
				}
			}

			@Override
			public void loadResources() {
				buttons1 = new ArrayList<>();

				Image texture = new Image("resources/sprites/buttons/recruit.png");
				final double buttonWidth = texture.getWidth();

				String[] buttonPaths1 = new String[3];
				buttonPaths1[0] = "recruit.png";
				buttonPaths1[1] = "select_troops.png";
				buttonPaths1[2] = "level_up.png";

				Point2D buttonPos = new Point2D(getPosition().getX(), getPosition().getY());

				for (String buttonPath : buttonPaths1) {
					Button button = new Button(renderLayer, buttonPos, new Image("resources/sprites/buttons/" + buttonPath));
					buttons1.add(button);

					buttonPos = new Point2D(buttonPos.getX() + buttonWidth, buttonPos.getY());
				}

				buttons1.get(0).getTextureView().setOnMouseClicked(e -> {
					setTroopsRecruitView();
					e.consume();
				});

				buttons1.get(1).getTextureView().setOnMouseClicked(e -> {
					setTroopsMoveView();
					e.consume();
				});

				buttons1.get(2).getTextureView().setOnMouseClicked(e -> {
					Alert alert = new Alert(Alert.AlertType.NONE);
					if (getCurrentCastle().getOwner() == 0) {
						if (getCurrentCastle().canLevelUp()) {
							alert.setAlertType(Alert.AlertType.CONFIRMATION);
							alert.setContentText("Vous êtes sur? Ça vous coûtera " + getCurrentCastle().getNextLevelBuildCost() + " florains.");

							Optional<ButtonType> result = alert.showAndWait();
							if (result.get() == ButtonType.OK) {
								getCurrentCastle().levelUp();
							}
						} else {
							alert.setAlertType(Alert.AlertType.WARNING);
							alert.setContentText("Vous ne pouvez pas améliorer votre château, soit parce qu'il est déjà en construction, soit parce que vous n'avez pas assez de florains.");
							alert.show();
						}
					} else {
						alert.setAlertType(Alert.AlertType.WARNING);
						alert.setTitle("Attention");
						alert.setContentText("Ce n'est pas votre château");
						alert.show();
					}
					e.consume();
				});

				// Spinners for troop selection
				moveSpinners = new ArrayList<>();
				moveValueFactories = new ArrayList<>();

				final int fontSize = 16;
				Point2D spinnerPosition = new Point2D(getPosition().getX(), getPosition().getY());

				for (int i = 0; i < Settings.nbDiffTroopTypes; ++i) {
					final Spinner<Integer> spinner = new Spinner<>();

					spinner.setTranslateX(spinnerPosition.getX());
					spinner.setTranslateY(spinnerPosition.getY() + fontSize);

					spinner.setMaxWidth(getSize().getX() / Settings.nbDiffTroopTypes);

					spinner.setVisible(false);
					root.getChildren().addAll(spinner);
					moveSpinners.add(spinner);
					moveValueFactories.add(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 0, 0));

					spinnerPosition = new Point2D(spinnerPosition.getX() + spinner.getWidth(), spinnerPosition.getY());
				}
			}

			@Override
			public void setCastleView(Castle castle) {
				super.setCastleView(castle);

				final int spinnerValues[] = {castle.getNbKnights(), castle.getNbPikemen(), castle.getNbOnagers()};
				for (int i = 0; i < Settings.nbDiffTroopTypes; ++i) {
					moveValueFactories.remove(0);
					moveValueFactories.add(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, spinnerValues[i], 0));
				}
			}
		};
		centerStatusBar.setDefaultMenuView();
		statusBars.add(centerStatusBar);

		statusBarPos = new Point2D(statusBarPos.getX() + Settings.windowWidth / 3, statusBarPos.getY());
		StatusBar rightStatusBar = new StatusBar(renderLayer, statusBarPos, statusBarSize, "rightStatusBar") {
			@Override
			public void updateView() {
				if (getView() == StatusBarView.DefaultMenuView) {
					setText("");
				} else if (getView() == StatusBarView.CreditsView) {
					setText("");
				} else if (getView() == StatusBarView.DefaultGameView) {
					setText("");
				} else if (getView() == StatusBarView.CastleView) {
					String text = "Chevaliers: " + getCurrentCastle().getNbKnights() + "\n"
							+ "Onagres: " + getCurrentCastle().getNbOnagers() + "\n"
							+ "Piquiers: " + getCurrentCastle().getNbPikemen() + "\n";

					setText(text);
				}
			}
		};
		rightStatusBar.setDefaultMenuView();
		statusBars.add(rightStatusBar);

		for (StatusBar statusBar : statusBars) {
			statusBar.addToCanvas();
		}
	}
	
	private void setMenuView() {
    	gameMode = GameMode.Menu;

		for (Castle castle : castles) {
			castle.removeFromCanvas();
		}

		menuBackground.addToCanvas();

		for (Button button : defaultMenuButtons) {
			button.addToCanvas();
		}
	}

	private void setGameView() {
    	gameMode = GameMode.Game;
    	for (StatusBar statusBar : statusBars) {
    		statusBar.setDefaultGameView();
		}

		createCastles();

		menuBackground.removeFromCanvas();
		gameBackground.addToCanvas();

		for (Castle castle : castles) {
			castle.addToCanvas();
		}
	}

	private void setCreditsView() {
		for (StatusBar statusBar : statusBars) {
			statusBar.setCreditsView();
		}
	}

	public Group getRoot() {
		return root;
	}
}
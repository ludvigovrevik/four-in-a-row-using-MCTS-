package project;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

public class FirePaaRadController implements Initializable {
    @FXML
    private Circle button00, button01, button02, button03, button04, button05, button06;

    @FXML
    private Circle button10, button11, button12, button13, button14, button15, button16;

    @FXML
    private Circle button20, button21, button22, button23, button24, button25, button26;

    @FXML
    private Circle button30, button31, button32, button33, button34, button35, button36;

    @FXML
    private Circle button40, button41, button42, button43, button44, button45, button46;

    @FXML
    private Circle button50, button51, button52, button53, button54, button55, button56;

    @FXML
    private Label result;

    @FXML
    private TextField player1, player2;

    @FXML
    private Text p1Text, p2Text;

    @FXML
    private Button aiButton;

    @FXML
    private GridPane buttonGrid;

    private Map<String, Circle> circleMap = new HashMap<>();

    public Leaderboard leaderBoard = new Leaderboard();

    @FXML
    private Text leaderboard;

    private FirePaaRadEnv firePaaRad;

    private String p1, p2;

    private MCTS mcts;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        List<Circle> circles = Arrays.asList(button00, button01, button02, button03, button04, button05, button06,
                button10, button11, button12, button13, button14, button15, button16, button20, button21, button22,
                button23, button24, button25, button26, button30, button31, button32, button33, button34, button35,
                button36, button40, button41, button42, button43, button44, button45, button46, button50, button51,
                button52, button53, button54, button55, button56);

        int k = 0;
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                String key = String.format("button%d%d", row, col);
                circleMap.put(key, circles.get(k++));
            }
        }
    }

    @FXML
    public void playAI(String player1) {
        this.p1 = player1;
        this.p2 = "AI";
        firePaaRad = new FirePaaRadEnv(p1, p2);
        mcts = new MCTS();
        resetButtonsText();
    }

    @FXML
    public void enableGame(String p1, String p2) {
        mcts = null;
        this.p1 = p1;
        this.p2 = p2;
        resetGame();
        resetButtonsText();
    }

    @FXML
    public void resetGame() {
        resetButtonsText();
        this.result.setText("New game started");
        firePaaRad = new FirePaaRadEnv(this.p1, this.p2);
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                changeCircleColor(row, col, Color.WHITE);
            }
        }
    }

    @FXML
    public void disableButtons() {
        for (Node node : buttonGrid.getChildren()) {
            if (node instanceof Button) {
                Button button = (Button) node;
                button.setDisable(true);
            }
        }
    }

    public void resetButtonsText() {
        for (Node node : buttonGrid.getChildren()) {
            if (node instanceof Button) {
                Button button = (Button) node;
                button.setText(".");
                button.setDisable(false);
            }
        }
    }

    public void changeCircleColor(int row, int col, Color color) {
        String key = String.format("button%d%d", row, col);
        Circle circle = circleMap.get(key);
        if (circle != null) {
            circle.setFill(color);
        }
    }

    public boolean isTerminal(FirePaaRadEnv env) {
        if (env.isWinner()) {
            if (env.getResult() == null) {
                System.out.println("It's a tie");
                disableButtons();
                return true;
            } else {
                this.result.setText((env.getResult().toString()) + " vant!");
                leaderBoard.saveGame(env.getResult().toString());
                StringBuilder sb = new StringBuilder();
                for (Winner winner : leaderBoard.sortWinners(leaderBoard.getWinners())) {
                    sb.append("Winner: ").append(winner.getName()).append(" Wins: ").append(winner.getWins()).append("\n");
                }
                leaderboard.setText(sb.toString());
                disableButtons();
                return true;
            }
        }
        return false;
    }

    @FXML
    public void putPiece(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String buttonId = clickedButton.getId();
        int column = Character.getNumericValue(buttonId.charAt(6));

        if (firePaaRad.isLegalMove(column)) {
            int row = firePaaRad.putPiece(column);
            if (p1.equals(firePaaRad.getCurrentPlayer().toString())) {
                changeCircleColor(row, column, Color.RED);
            } else {
                changeCircleColor(row, column, Color.YELLOW);
            }
            firePaaRad.PrintBoard();

            if (isTerminal(firePaaRad)) {
                return;
            }
            firePaaRad.switchPlayer();

            if (mcts != null) {
                GameState currentState = new GameState(firePaaRad);
                int bestAction = mcts.runSimulation(currentState);
                row = firePaaRad.putPiece(bestAction);
                firePaaRad.PrintBoard();
                if (p1.equals(firePaaRad.getCurrentPlayer().toString())) {
                    changeCircleColor(row, column, Color.RED);
                } else {
                    changeCircleColor(row, bestAction, Color.YELLOW);
                }

                if (isTerminal(firePaaRad)) {
                    return;
                }
                firePaaRad.switchPlayer();
            }
        } else {
            this.result.setText("Can't put your piece there!");
        }
    }
}

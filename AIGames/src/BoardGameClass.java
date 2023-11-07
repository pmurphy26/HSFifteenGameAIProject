import java.util.ArrayList;

public abstract class BoardGameClass {

    private int levelOfDepth;
    private int maxLevelOfDepth = 7;
    public int miniMaxCount = 0;
    public int alphaBetaCount = 0;

    public abstract int quickEval();

    public abstract boolean movePiece(Action a);

    public Action miniMax(BoardGameClass gameState) {
        Action move = new Action();
        int maxScore = -101;

        for (Action possibleMove : actions()) {
            BoardGameClass newGameState = gameState.result(possibleMove);

            int moveScore = minValue(newGameState);


            if (maxScore < moveScore) {
                move = possibleMove;
                maxScore = moveScore;
            }
        }

        return move;
    }

    public Action maxiMin(BoardGameClass gameState) {
        Action move = new Action();
        int minScore = 101;

        for (Action possibleMove : actions()) {
            BoardGameClass newGameState = gameState.result(possibleMove);

            int moveScore = minValue(newGameState);


            if (minScore < moveScore) {
                move = possibleMove;
                minScore = moveScore;
            }
        }

        return move;
    }

    //alpha = maximizer's value for the worst he can do
    //beta = minimizer's value for the worst he can do
    //think of worst in terms of getting f-ed over instead of trying to play bad
    public Action miniMaxAlphaBetaSearch(BoardGameClass gameState) {
        Action move = new Action();

        int v = maxValue(gameState, -101, 101);

        ArrayList<Action> validMoves = gameState.actions();
        for (int i = 0; i < validMoves.size(); i++) {
            Action possibleMove = validMoves.get(i);
            BoardGameClass newGameState = gameState.copyOfGame();
            newGameState.movePiece(possibleMove);
            int moveScore = minValue(newGameState, -101, 101);


            if (moveScore == v) {
                move = possibleMove;
            }
        }

        return move;
    }

    public Action maxiMinAlphaBetaSearch(BoardGameClass gameState) {
        Action move = new Action();

        int v = minValue(gameState, -101, 101);

        ArrayList<Action> validMoves = gameState.actions();
        for (int i = 0; i < validMoves.size(); i++) {
            Action possibleMove = validMoves.get(i);
            BoardGameClass newGameState = gameState.copyOfGame();
            newGameState.movePiece(possibleMove);
            int moveScore = maxValue(newGameState, -101, 101);


            if (moveScore == v) {
                move = possibleMove;
            }
        }

        return move;
    }

    public int maxValue(BoardGameClass gameState) {
        miniMaxCount++;
        if (gameState.terminalTest()) {
            return gameState.quickEval();
        }
        int v = -101;

        for (Action a : gameState.actions()) {
            BoardGameClass newGameState = gameState.copyOfGame();
            newGameState.movePiece(a);
            v = Math.max(v, minValue(newGameState));
        }

        return v;
    }

    public int maxValue(BoardGameClass gameState, int alpha, int beta) {
        alphaBetaCount++;
        if (gameState.terminalTest()) {
            return gameState.quickEval();
        }
        int v = -101;

        for (Action a : gameState.actions()) {
            BoardGameClass newGameState = gameState.copyOfGame();
            newGameState.movePiece(a);
            v = Math.max(v, minValue(newGameState, alpha, beta));

            if (v >= beta) return v;

            alpha = Math.max(alpha, v);
        }

        return v;
    }

    public int minValue(BoardGameClass gameState) {
        miniMaxCount++;
        if (gameState.terminalTest()) {
            return gameState.quickEval();
        }
        int v = 101;

        for (Action a : gameState.actions()) {
            BoardGameClass newGameState = gameState.copyOfGame();
            newGameState.movePiece(a);
            v = Math.min(v, maxValue(newGameState));
        }

        return v;
    }

    public int minValue(BoardGameClass gameState, int alpha, int beta) {
        alphaBetaCount++;
        if (gameState.terminalTest()) {
            return gameState.quickEval();
        }
        int v = 101;

        for (Action a : gameState.actions()) {
            BoardGameClass newGameState = gameState.copyOfGame();
            newGameState.movePiece(a);
            v = Math.min(v, maxValue(newGameState, alpha, beta));

            if (v <= alpha) return v;

            beta = Math.min(beta, v);
        }

        return v;
    }

    public abstract BoardGameClass copyOfGame();

    public abstract boolean terminalTest();

    public abstract boolean isMinTurn();

    public String toString() {
        return "board does not have printing method yet";
    }

    public abstract ArrayList<Action> actions();

    public abstract BoardGameClass result(Action a);

    public int getLevelOfDepth() {
        return levelOfDepth;
    }

    public void addLevelOfDepth() {
        levelOfDepth+=1;
    }

    public void setLevelOfDepth(int n) {
        levelOfDepth = n;
    }

    public static class Action {

    }
}

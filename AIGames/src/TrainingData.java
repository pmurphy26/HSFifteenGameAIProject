/*
 * game state, action, score, create probabilities off of that
 */

import java.util.ArrayList;

public class TrainingData {
    private FifteenGame gameState;
    private FifteenGame.Action action;
    private int movesFromWin;
    private Double[] probabilities;

    public TrainingData(FifteenGame gameState, Double[] probabilities) {
        this.gameState = gameState;
        this.probabilities = probabilities;
    }

    public TrainingData(FifteenGame gameState, FifteenGame.Action action, int movesFromWin) {
        this.gameState = gameState;
        this.action = action;
        this.movesFromWin = movesFromWin;
    }

    public FifteenGame getGameState() {return gameState;}

    public Double[] getProbabilities() {return probabilities;}

    public FifteenGame.Action getAction() {
        return action;
    }

    public void setMovesFromWin(int n) {movesFromWin = n;}

    public void setAction(FifteenGame.Action a) {action = a;}

    public int getMovesFromWin() {return movesFromWin;}

    public String toString() {
        String returnString = "";

        returnString += gameState + "*" + action + "*" + movesFromWin+ "*";

        return returnString;
    }

    public void updatePathLength(ArrayList<TrainingData> trainingDataObjects) {
        //create result from actions
        //check partial match score against everything in training data objects
        //if it's 0 check score and update score and best action accordingly

        //finding matches with itself
        for (TrainingData t : trainingDataObjects) {
            if (t.getGameState().partialMatchScore(gameState) == 0 && t != this) { //if training data contains another match of game state

                double bestScore = Math.min(t.getMovesFromWin(), movesFromWin);

                if (bestScore == movesFromWin) { //better path is contained in this training data
                    t.setAction(action);
                    t.setMovesFromWin(movesFromWin);
                } else { //next move contains better path
                    setAction(t.getAction());
                    setMovesFromWin(t.getMovesFromWin());
                }
            }
        }


        for (BoardGameClass.Action action : gameState.actions()) {
            if (action instanceof FifteenGame.Action) {
                FifteenGame.Action a = (FifteenGame.Action) action;
                FifteenGame g = gameState.result(a);

                for (TrainingData t : trainingDataObjects) {
                    if (t.getGameState().partialMatchScore(g) == 0) { //if training data contains a possible next move
                        if (Math.abs(t.getMovesFromWin() - movesFromWin) > 1) {

                            System.out.println(t.getMovesFromWin() + ", " + movesFromWin + "\n" + this + ", " + t);

                            double bestScore = Math.min(t.getMovesFromWin(), movesFromWin);

                            if (bestScore == movesFromWin) { //better path is contained in this training data
                                t.setMovesFromWin(movesFromWin + 1);
                                t.setAction(t.getGameState().makeMoveAction(a.getIsY(), !a.getIsPositive()));
                            } else { //next move contains better path
                                setMovesFromWin(t.getMovesFromWin() + 1);
                                setAction(a);
                            }

                            System.out.println(t.getMovesFromWin() + ", " + movesFromWin);
                        }
                    }
                }
            }
        }
    }

    public void updateGivenList(ArrayList<TrainingData> trainingData) {
        for (int i = 0; i < trainingData.size(); i++) {
            TrainingData data = trainingData.get(i);
            //if training data contains this game state
            if (data.getGameState().partialMatchScore(gameState) == 0) {
                TrainingData betterData = data;

                double lowestMovesFromWin = Math.min(data.getMovesFromWin(), movesFromWin);

                if (lowestMovesFromWin == movesFromWin) betterData = this;
                trainingData.set(i, betterData);
                return;
            }
        }

        trainingData.add(binarySearch(trainingData, gameState, 0, trainingData.size() - 1), this);
    }

    public void updateGivenListLengths(ArrayList<TrainingData> trainingData) {
        for (int i = 0; i < trainingData.size(); i++) {
            TrainingData data = trainingData.get(i);

            //if training data contains an action state
            for (BoardGameClass.Action action : gameState.actions()) {
                FifteenGame.Action a = (FifteenGame.Action) action;
                FifteenGame resultState = gameState.result(a);

                if (data.getGameState().partialMatchScore(resultState) == 0) {
                    int lowerScore = Math.min(movesFromWin, data.getMovesFromWin());

                    //if scores have large enough difference
                    if (Math.abs(movesFromWin - data.getMovesFromWin()) > 1) {
                        if (lowerScore == movesFromWin) { //this state is closer to solved game
                            data.setMovesFromWin(movesFromWin + 1);
                            data.setAction(data.getGameState().makeMoveAction(a.getIsY(), !a.getIsPositive()));
                        } else { //result state is closer to solved game
                            setMovesFromWin(data.getMovesFromWin() + 1);
                            setAction(a);
                        }
                    }
                }
            }
        }
    }

    //something I believe is broken in this so I'm not using it as of now
    public void updateGivenListLengthsBinary(ArrayList<TrainingData> trainingData) {
        FifteenGame solvedGame = new FifteenGame();

        //if training data contains an action state
        for (BoardGameClass.Action action : gameState.actions()) {
            FifteenGame.Action a = (FifteenGame.Action) action;
            FifteenGame resultState = gameState.result(a);

            int resultStateIndex = binarySearch(trainingData, resultState, 0, trainingData.size() - 1);

            int high = resultStateIndex;
            while (high < trainingData.size() && resultState.partialMatchScore(solvedGame) + 1 >=
                    trainingData.get(high).getGameState().partialMatchScore(solvedGame)) {
                high++;
            }

            int low = resultStateIndex;
            while (low > 0 && resultState.partialMatchScore(solvedGame) - 1 <=
                    trainingData.get(low).getGameState().partialMatchScore(solvedGame)) {
                low--;
            }

            //looping through possible result actions
            for (int i = low; i < high; i++) {
                TrainingData data = trainingData.get(i);

                if (data.getGameState().partialMatchScore(resultState) == 0 && i != resultStateIndex) {
                    int lowerScore = Math.min(movesFromWin, data.getMovesFromWin());

                    //if scores have large enough difference
                    if (Math.abs(movesFromWin - data.getMovesFromWin()) > 1) {
                        if (lowerScore == movesFromWin) { //this state is closer to solved game
                            data.setMovesFromWin(movesFromWin + 1);
                            data.setAction(data.getGameState().makeMoveAction(a.getIsY(), !a.getIsPositive()));
                        } else { //result state is closer to solved game
                            setMovesFromWin(data.getMovesFromWin() + 1);
                            setAction(a);
                        }
                    }
                }
            }
        }
    }

    public int binarySearch(ArrayList<TrainingData> trainingData, FifteenGame searchedItem, int low, int high) {
        FifteenGame solvedGame = new FifteenGame();

        if (high <= low)
            return low;

        int mid = (low + high) / 2;


        //if partial match of data to solved game state is further away than partial match of this data to solved game
        if (trainingData.get(mid).getGameState().partialMatchScore(solvedGame) == searchedItem.partialMatchScore(solvedGame)) {
            return mid + 1;
        }

        if (searchedItem.partialMatchScore(solvedGame) > trainingData.get(mid).getGameState().partialMatchScore(solvedGame))
            return binarySearch(trainingData,searchedItem, mid + 1, high);
        return binarySearch(trainingData, searchedItem, low,mid - 1);
    }
}

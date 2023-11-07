import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class FifteenGame extends BoardGameClass {

    private int[][] board = new int[4][4];
    private int[] emptyTileCoordinates;

    public FifteenGame() {
        int n = 1;

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                board[i][j] = n;
                n++;
            }
        }

        board[3][3] = 0;

        emptyTileCoordinates = new int[] {3, 3};
    }

    public FifteenGame(FifteenGame copyGame) {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                board[i][j] = copyGame.getBoard()[i][j];

                if (board[i][j] == 0) {
                    emptyTileCoordinates = new int[] {i, j};
                }
            }
        }
    }

    public boolean movePiece(BoardGameClass.Action action) {
        if (!(action instanceof Action)) {
            System.out.println("action is not fifteen game action");
            return false;
        }
        Action a = (Action) action;


        int x = a.getMovedTileCoordinates()[0];
        int y = a.getMovedTileCoordinates()[1];

        if (isLegalMove(x, y)) {
            board[emptyTileCoordinates[0]][emptyTileCoordinates[1]] = board[x][y];
            board[x][y] = 0;
            emptyTileCoordinates[0] = x;
            emptyTileCoordinates[1] = y;
            return true;
        }
        return false;
    }

    public int quickEval() {return 0;}

    public FifteenGame copyOfGame() {
        return new FifteenGame(this);
    }

    public boolean terminalTest() {
        int n = 1;

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                if (board[i][j] != n && n < 16) return false;
                n++;
            }
        }

        return true;
    }

    public boolean isMinTurn() {return false;}

    public ArrayList<BoardGameClass.Action> actions() {
        ArrayList<BoardGameClass.Action> legalActions = new ArrayList<>();

        legalActions.add(new Action(false, false));
        legalActions.add(new Action(false, true));
        legalActions.add(new Action(true, true));
        legalActions.add(new Action(true, false));

        for (int i = 0; i < legalActions.size(); i++) {
            BoardGameClass.Action action = legalActions.get(i);

            if ((action instanceof Action)) {
                Action a = (Action) action;
                int x = a.getMovedTileCoordinates()[0];
                int y = a.getMovedTileCoordinates()[1];

                if (!isLegalMove(x, y)) {
                    legalActions.remove(action);
                    i--;
                }
            }
        }


        return legalActions;
    }

    public FifteenGame result(BoardGameClass.Action a) {
        FifteenGame newBoard = copyOfGame();

        if (a instanceof FifteenGame.Action) {
            FifteenGame.Action action = (FifteenGame.Action) a;
            newBoard.movePiece(action);
        }

        return newBoard;
    }

    public boolean isLegalMove(int x, int y) {
        if (x > -1 && x < board.length && y > -1 && y < board[0].length) {
            return true;
        }
        return false;
    }

    public int[][] getBoard() {return board;}

    public int[] getEmptyTileCoordinates() {return emptyTileCoordinates;}

    public void setEmptyTileCoordinates(int[] nums) {
        emptyTileCoordinates = nums;
    }

    public String toString() {
        String returnString = "";

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                returnString += String.format("%02d", board[i][j]);
                returnString += "|";
            }
            if (i + 1 < board.length)
                returnString += "\n";
        }

        return returnString;
    }

    public Action makeMoveAction(boolean isY, boolean isPositive) {
        return new Action(isY, isPositive);
    }

    public Action makeRandMove() {
        ArrayList<BoardGameClass.Action> possibleMoves = actions();
        int randNum = (int) (Math.random() * possibleMoves.size());
        Action a = (Action) possibleMoves.get(randNum);
        movePiece(a);
        return a;
    }

    public void scrambleBoard(int n) {
        for (int i = 0; i < n; i++) {
            makeRandMove();
        }
    }

    public double partialMatchScore(FifteenGame comparedGame) {
        double score = 0;

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                int[] otherGameCoordinates = comparedGame.findCoordinatesOfTile(board[i][j]);
                int tilesAway = Math.abs(i - otherGameCoordinates[0]) + Math.abs(j - otherGameCoordinates[1]);
                score += tilesAway;
            }
        }

        //maybe add score for if tile is in right place

        return score;
    }

    public int[] findCoordinatesOfTile(int n) {
        int[] coordinates = new int[2];

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                if (n == board[i][j]) {
                    coordinates = new int[] {i, j};
                    return coordinates;
                }
            }
        }

        return coordinates;
    }

    public void writeGameOnFile(String fileName) throws IOException {
        PrintWriter printWriter = new PrintWriter(fileName);
        printWriter.write(this.toString());
        printWriter.close();
    }

    public FifteenGame createGameFromFile(String fileName) throws IOException {
        File file = new File (fileName);
        Scanner scan = new Scanner(file);
        scan.useDelimiter("\\|");
        int numRows = 0;
        int numColumns = 0;
        int currentColumnNum = 0;

        while (scan.hasNext()) {
            String stringRead = scan.next();

            if (stringRead.contains("\n")) {
                numRows++;
                stringRead = stringRead.substring(stringRead.indexOf('\n') + 1);
                currentColumnNum = 0;
            }

            int numRead = Integer.valueOf(stringRead);
            if (numRead == 0) {
                emptyTileCoordinates = new int[] {numRows, currentColumnNum};
            }

            if (numRows == 0) {
                board[0][numColumns] = numRead;
                numColumns++;
                currentColumnNum++;
            } else {
                board[numRows][currentColumnNum] = numRead;
                currentColumnNum++;
            }
        }

        return this;
    }

    public FifteenGame findClosestPartialMatch(ArrayList<FifteenGame> trainingData) {
        FifteenGame bestPartialMatch = new FifteenGame();
        double lowestScore = Double.POSITIVE_INFINITY;

        for (FifteenGame game : trainingData) {
            double newLowestScore = Math.min(lowestScore, partialMatchScore(game));
            if (newLowestScore != lowestScore)  {
                bestPartialMatch = game;
                lowestScore = newLowestScore;
            }
        }

        return bestPartialMatch;
    }

    public TrainingData findClosestDataPartialMatch(ArrayList<TrainingData> trainingData) {
        TrainingData bestPartialMatch = new TrainingData(null, null);
        double lowestScore = Double.POSITIVE_INFINITY;

        for (TrainingData data : trainingData) {
            double dataScore = partialMatchScore(data.getGameState());

            if (dataScore == lowestScore) { //if lowest score found equals this data's score
                if (data.getMovesFromWin() < bestPartialMatch.getMovesFromWin()) { //if score is lower than best match's score
                    bestPartialMatch = data;
                }
            } else {
                lowestScore = Math.min(dataScore, lowestScore);
                if (lowestScore == dataScore) {
                    bestPartialMatch = data;
                }
            }

        }

        return bestPartialMatch;
    }

    public Action findBestMove(ArrayList<TrainingData> trainingData) {
       /*
       step 1
        * start with solution and training data should only have one item that is one move away from being solved
        * test start point have right up
       step 2
        * get things that match reasonably well
        * want next action to be very close to a point in the training data
        */


        /*
         * what to try:
         * look at states and find partial/complete matches for those
         * evaluate how good each partial match is
         * devise ratio of how likely each move is to be picked
         * pick move
         */

        TrainingData bestData = findClosestDataPartialMatch(trainingData);
        if (bestData.getGameState().partialMatchScore(this) == 0) {
            return bestData.getAction();
        }

        Action bestAction = new Action(false, false);
        double[] nums = new double[] {0, 0, 0, 0};
        double[] moveProbabilities = new double[] {0, 0, 0, 0};
        int numsIndex = 0;

        for (BoardGameClass.Action action : actions()) { //go off of data match's moves from win
            Action a = (Action) action;

            FifteenGame resultState = result(a);
            TrainingData bestDataMatch = resultState.findClosestDataPartialMatch(trainingData);

            //setting numsIndex
            {
                numsIndex = 0;
                if (!a.isY)
                    numsIndex += 2;
                if (!a.isPositive)
                    numsIndex++;
            }

            nums[numsIndex] = bestDataMatch.getGameState().partialMatchScore(resultState)
                    + bestDataMatch.getMovesFromWin();
        }

        //creating move probabilities
        /*should probably update
         *because the algorithm is picking the best move most of the time
         *but that move isn't being selected because of randomness
         */
        {
            moveProbabilities = makeMovePercentages(nums);
        }

        //picking move from probabilities
        double randNum = Math.random();

        int i = 0;
        while (moveProbabilities[i] < randNum) {
            i++;
        }


        boolean isY = false;
        boolean isPositive = false;
        if (i <= 1) {
            isY = true;
        }
        if (i % 2 == 0) {
            isPositive = true;
        }

        bestAction = makeMoveAction(isY, isPositive);

        return bestAction;

    }

    public double[] makeMovePercentages(double[] nums) {
        double[] movePercentages = new double[] {0, 0, 0, 1};
        double sum = 0;
        double smallestNum = Double.POSITIVE_INFINITY;
        double x;

        //this is an arbitrary value, might want to change
        int multiplier = 2;
        double[] n = new double[] {0, 0, 0, 0};

        for (double d : nums) {
            sum += d;

            if (d != 0) {
                smallestNum = Math.min(smallestNum, d);
            }
        }

        for (int i = 0; i < nums.length; i++) {
            if (nums[i] != 0) n[i] = (nums[i] - smallestNum + 1) * multiplier;
            if (nums[i] == smallestNum) n[i] = 1;
        }

        double fractionSum = 0;
        for (double fraction : n) {
            //System.out.println("fraction is " +fraction);
            if (fraction != 0) {
                fractionSum += 1 / fraction;
            }
        }

        x = 1/fractionSum;

        for (int i = 0; i < movePercentages.length; i++) {
            if (nums[i] != 0)
                movePercentages[i] = x/n[i];

            if (i != 0) {
                movePercentages[i] += movePercentages[i-1];
            }
        }

        movePercentages[3] = 1.0;

        return movePercentages;
    }

    public class Action extends BoardGameClass.Action {
        private int[] movedTileCoordinates = new int[2];
        private boolean isY, isPositive;

        public Action(boolean isY, boolean isPositive) {
            this.isY = isY;
            this.isPositive = isPositive;

            if (isY) {
                if (isPositive) {
                    movedTileCoordinates =
                            new int[] {emptyTileCoordinates[0] - 1, emptyTileCoordinates[1]};
                } else {
                    movedTileCoordinates =
                            new int[] {emptyTileCoordinates[0] + 1, emptyTileCoordinates[1]};
                }
            } else {
                if (isPositive) {
                    movedTileCoordinates =
                            new int[] {emptyTileCoordinates[0], emptyTileCoordinates[1] + 1};
                } else {
                    movedTileCoordinates =
                            new int[] {emptyTileCoordinates[0], emptyTileCoordinates[1] - 1};
                }
            }
        }

        public boolean getIsY() {
            return isY;
        }

        public boolean getIsPositive() {
            return isPositive;
        }

        public int[] getMovedTileCoordinates() {return movedTileCoordinates;}

        public String toString() {
            String returnString = "";

            if (isPositive && isY) {
                returnString = "up";
            } else if (!isPositive && isY) {
                returnString = "down";
            } else if (isPositive && !isY) {
                returnString = "right";
            } else if (!isPositive && !isY) {
                returnString = "left";
            }

            return returnString;
        }
    }
}

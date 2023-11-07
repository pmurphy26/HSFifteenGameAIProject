import java.io.*;
import java.util.ArrayList;
import java.io.PrintWriter;
import java.io.File;
import java.util.Scanner;

public class FifteenGameAIMainClass {
    public static void main(String[] args) throws IOException {
        createTrainingData(3, 5);

        ArrayList<TrainingData> trainingDataObjects =
                createTrainingDataObjectsFromFile("C:/Users/murff/Desktop/TrainingDataObjects.txt");

        for (int i = 0; i < 100; i++)
            for (TrainingData t : trainingDataObjects) {
                t.updateGivenListLengths(trainingDataObjects);
            }
    }

    public static ArrayList<TrainingData> createTrainingData(int n, int numOfScrambleMoves) throws IOException {
        ArrayList<TrainingData> trainingData = createBeginningOfTrainingData();

        String beginningData = "";
        for (TrainingData td : trainingData) {
            beginningData += td + "\n";
        }

        beginningData = beginningData.substring(0, beginningData.length() - 1);

        PrintWriter writeTD = new PrintWriter("C:/Users/murff/Desktop/trainingData.txt");
        writeTD.write(beginningData);
        writeTD.close();

        //ai code
        for (int q = 0; q < n; q++) {

            FifteenGame game = new FifteenGame();

            /*
             *Training Data objects file includes whatever is written onto the trainingData file
             *along with everything else that has been found from solving and reinforcement learning
             */
            ArrayList<TrainingData> trainingDataObjects =
                    createTrainingDataObjectsFromFile("C:/Users/murff/Desktop/TrainingDataObjects.txt");

            game.createGameFromFile("C:/Users/murff/Desktop/FifteenGame1.txt");
            game.scrambleBoard(numOfScrambleMoves);
            game.writeGameOnFile("C:/Users/murff/Desktop/FifteenGame1.txt");

            System.out.println("starting postition of game is: \n" + game);
            System.out.println(game.actions());
            boolean oneMoveOnly = false;

            if (oneMoveOnly) {
                FifteenGame.Action a = game.findBestMove(trainingDataObjects);
                System.out.println(game.movePiece(a));
                System.out.println("move taken was " + a);
            } else {
                ArrayList<TrainingData> newData = new ArrayList<>();
                //newData = createTrainingDataObjectsFromFile("C:/Users/murff/Desktop/NewTrainingData.txt");
                int movesToGetWin = 0;

                //solving game
                while (!game.terminalTest()) {
                    FifteenGame.Action a = game.findBestMove(trainingDataObjects);
                    TrainingData nd = new TrainingData(new FifteenGame(game), a, 0);
                    game.movePiece(a);
                    newData.add(nd);
                    movesToGetWin++;
                }

                System.out.println("game is: \n" + game);

                for (int i = 0; i < newData.size(); i++) {
                    newData.get(i).setMovesFromWin(movesToGetWin - i);
                }
                System.out.println("new data size is " + newData.size());
                for (int i = newData.size() - 1; i >= 0; i--) {
                    TrainingData data = newData.get(i);
                    data.updateGivenList(trainingDataObjects);
                }
                System.out.println("new data has been added " + trainingDataObjects.size());
                for (TrainingData data : trainingDataObjects) {
                    data.updateGivenListLengths(trainingDataObjects);
                }
                System.out.println("training data path lengths have been updated " + trainingDataObjects.size());


                // creating string for trainingData
                {
                    String s = "";
                    for (TrainingData t : trainingDataObjects) {
                        s += t + "\n";
                    }

                    s = s.substring(0, s.length() - 1);

                    PrintWriter printWriter = new PrintWriter("C:/Users/murff/Desktop/TrainingDataObjects.txt");
                    printWriter.write(s);
                    printWriter.close();
//
//            String nd = "";
//            for (TrainingData t : newData) {
//                nd += t + "\n";
//            }
//
//            nd = nd.substring(0, nd.length() - 1);
//
//            PrintWriter pW = new PrintWriter("C:/Users/murff/Desktop/NewTrainingData.txt");
//            pW.write(nd);
//            pW.close();
                }
            }

            System.out.println("game is: \n" + game);
        }

        return trainingData;
    }

    public static ArrayList<TrainingData> createBeginningOfTrainingData() {
        ArrayList<TrainingData> trainingData = new ArrayList<>();
        FifteenGame game = new FifteenGame();
        trainingData.add(new TrainingData(game, game.makeMoveAction(true, false), 0));
        FifteenGame.Action action = game.makeMoveAction(true, true);
        game.movePiece(action);
        trainingData.add(new TrainingData(game, game.makeMoveAction(true, false), 1));
        game = new FifteenGame();
        action = game.makeMoveAction(false, false);
        game.movePiece(action);
        trainingData.add(new TrainingData(game, game.makeMoveAction(false, true), 1));

        return trainingData;
    }

    public static ArrayList<FifteenGame> createTrainingDataFromFile(String fileName) throws IOException {
        File file = new File (fileName);
        Scanner scan = new Scanner(file);
        scan.useDelimiter("\\*");
        int numRows = 0;
        int numColumns = 0;
        int currentColumnNum = 0;
        ArrayList<FifteenGame> games = new ArrayList<>();

        while (scan.hasNext()) {
            FifteenGame game = new FifteenGame();
            numRows = numColumns = 0;
            String stringRead = scan.next();

            if (stringRead.indexOf('\n') == 1) {
                stringRead = stringRead.substring(2);
            }

            String gameString = stringRead.substring(0, stringRead.length() - 1);

            while (gameString.contains("|")) {
                String numReadString = gameString.substring(0, gameString.indexOf("|"));
                gameString = gameString.substring(gameString.indexOf("|") + 1);

                if (numReadString.contains("\n")) {
                    numRows++;
                    numReadString = numReadString.substring(numReadString.indexOf("\n") + 1);
                    currentColumnNum = 0;
                }

                int numRead = Integer.valueOf(numReadString);
                if (numRead == 0) {
                    game.setEmptyTileCoordinates(new int[] {numRows, numColumns});
                }

                if (numRows == 0) {
                    game.getBoard()[0][numColumns] = numRead;
                    numColumns++;
                } else {
                    game.getBoard()[numRows][currentColumnNum] = numRead;
                    currentColumnNum++;
                }

            }

            games.add(game);
            System.out.println("game is: " + game);
        }

        return games;
    }

    public static ArrayList<Double[]> createTrainingDataProbabilitiesFromFile(String fileName) throws IOException {
        File file = new File (fileName);
        Scanner scan = new Scanner(file);
        scan.useDelimiter("\\n");
        ArrayList<Double[]> probabilities = new ArrayList<>();


        while (scan.hasNext()) {
            String stringRead = scan.next();
            Double[] stateProbabilities = new Double[4];


            for (int i = 0; stringRead.indexOf(", ") != -1; i++) {
                String numString = stringRead.substring(0, stringRead.indexOf(", "));
                double numRead = Double.valueOf(numString);
                stringRead = stringRead.substring(stringRead.indexOf(", ") + 1);
                stateProbabilities[i] = numRead;
            }

            double numRead = Double.valueOf(stringRead);
            stateProbabilities[3] = numRead;

            probabilities.add(stateProbabilities);
        }

        return probabilities;
    }

    public static ArrayList<TrainingData> createTrainingDataObjectsFromFile(String fileName) throws IOException {
        File file = new File (fileName);
        Scanner scan = new Scanner(file);
        scan.useDelimiter("\\*");
        boolean onGameState = true;
        boolean onAction = false;
        boolean onScore = false;
        ArrayList<TrainingData> trainingData = new ArrayList<>();

        int numRows = 0;
        int numColumns = 0;
        int currentColumnNum = 0;
        ArrayList<FifteenGame> games = new ArrayList<>();
        ArrayList<FifteenGame.Action> actions = new ArrayList<>();
        ArrayList<Integer> scores = new ArrayList<>();

        while (scan.hasNext()) {
            if (onGameState) {
                FifteenGame game = new FifteenGame();
                numRows = numColumns = 0;
                String stringRead = scan.next();

                if (stringRead.indexOf('\n') == 0) {
                    stringRead = stringRead.substring(1);
                }

                if (stringRead.indexOf('\n') == 1) {
                    stringRead = stringRead.substring(2);
                }

                String gameString = stringRead;

                while (gameString.contains("|")) {
                    String numReadString = gameString.substring(0, gameString.indexOf("|"));
                    gameString = gameString.substring(gameString.indexOf("|") + 1);

                    if (numReadString.contains("\n")) {
                        numRows++;
                        numReadString = numReadString.substring(numReadString.indexOf("\n") + 1);
                        currentColumnNum = 0;
                    }

                    int numRead = Integer.valueOf(numReadString);
                    if (numRead == 0) {
                        game.setEmptyTileCoordinates(new int[]{numRows, currentColumnNum});
                    }

                    if (numRows == 0) {
                        game.getBoard()[0][numColumns] = numRead;
                        numColumns++;
                        currentColumnNum++;
                    } else {
                        game.getBoard()[numRows][currentColumnNum] = numRead;
                        currentColumnNum++;
                    }

                }
                games.add(game);

                onGameState = false;
                onAction = true;
            } else {
                String stringRead = scan.next();

                if (onAction) {
                    boolean isY = false;
                    boolean isPositive = false;

                    if (stringRead.equalsIgnoreCase("right")) {
                        isY = false;
                        isPositive = true;
                    } else if (stringRead.equalsIgnoreCase("left")) {
                        isY = false;
                        isPositive = false;
                    } else if (stringRead.equalsIgnoreCase("up")) {
                        isY = true;
                        isPositive = true;
                    } else if (stringRead.equalsIgnoreCase("down")) {
                        isY = true;
                        isPositive = false;
                    }

                    //action needs to be from game state so moved tile coordinates are correct
                    FifteenGame.Action a = games.get(games.size() - 1).makeMoveAction(isY, isPositive);
                    actions.add(a);

                    onAction = false;
                    onScore = true;
                } else if (onScore) {
                    Integer d = Integer.valueOf(stringRead);
                    scores.add(d);

                    onScore = false;
                    onGameState = true;
                }
            }


        }

        for (int i = 0; i < games.size(); i++) {
            trainingData.add(new TrainingData(games.get(i), actions.get(i), scores.get(i)));
        }

        return trainingData;
    }
}

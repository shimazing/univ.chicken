package uc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.util.TreeMap;

/**
 * Created by keltp on 2017-06-11.
 */
public class UCStatistics {
    private final transient long startTime;

    private long totalTime;
    private int nTotalSteps;
    private int nTotalEpisodes;
    private double totalRewards;
    private double totalScores;
    private TreeMap<Integer, Double> highScores;

    public UCStatistics() {
        startTime = System.currentTimeMillis();

        totalTime = 0;
        nTotalSteps = 0;
        nTotalEpisodes = 0;
        totalRewards = 0;
        totalScores = 0;
        highScores = new TreeMap<>();
    }

    public long startTime() {
        return startTime;
    }

    public long totalTime() {
        return totalTime;
    }

    public int nTotalSteps() {
        return nTotalSteps;
    }

    public int nTotalEpisodes() {
        return nTotalEpisodes;
    }

    public double totalRewards() {
        return totalRewards;
    }

    public double totalScores() {
        return totalScores;
    }

    public TreeMap<Integer, Double> highScores() {
        return highScores;
    }

    public void updatePerSteps() {
        totalTime = (System.currentTimeMillis() - startTime) / 1000;
        nTotalSteps++;
    }

    public void traceBack(int nSteps) {
        nTotalSteps -= nSteps;
        nTotalEpisodes --;
    }

    public void updatePerEpisode(int level, double rewards, double clearScores) {
        nTotalEpisodes++;
        totalRewards += rewards;
        totalScores += clearScores;

        highScores.putIfAbsent(level, clearScores);
        if (highScores.get(level) < clearScores) {
            highScores.put(level, clearScores);
        }
    }


    @Override
    public String toString() {
        String def = String.format("Total play time: %s\r\nTotal number of steps: %s\r\nTotal number of episodes: %s\r\nTotal rewards: %s\r\nTotal scores: %s\r\nHigh scores:\r\n",
                totalTime, nTotalSteps, nTotalEpisodes, totalRewards, totalScores);
        StringBuilder builder = new StringBuilder(def);

        for(Integer level: highScores.keySet()) {
            double score = highScores.get(level);
            builder.append(String.format("\tLevel %s: %s\r\n", level, score));
        }
        return builder.toString();
    }

    public void serialize(File f) throws IOException {
        Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setPrettyPrinting().create();
        FileWriter writer = new FileWriter(f);
        gson.toJson(this, writer);
        writer.close();
    }

    public static UCStatistics deserialize(File f) throws IOException {
        Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setPrettyPrinting().create();
        BufferedReader reader = new BufferedReader(new FileReader(f));
        UCStatistics s = gson.fromJson(reader, UCStatistics.class);
        reader.close();
        return s;
    }
}

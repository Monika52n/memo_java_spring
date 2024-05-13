package com.memo.game.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;
import java.text.DecimalFormat;

public class ModeStat {
    @JsonProperty("losses")
    private int losses;
    @JsonProperty("wins")
    private int wins;
    @JsonProperty("time")
    private final int time;
    @JsonProperty("pairs")
    private final int pairs;
    private int sumRemainingTime;
    @JsonProperty("avgRemainingTime")
    @JsonSerialize(using = CustomDoubleSerializer.class)
    private double avgRemainingTime;
    @JsonProperty("winningRate")
    @JsonSerialize(using = CustomDoubleSerializer.class)
    private double winningRate;
    @JsonProperty("numOfGames")
    private int numOfGames;

    public ModeStat(int time, int pairs, int wins, int losses, int sumRemainingTime) {
        this.time = time;
        this.pairs = pairs;
        this.wins = wins;
        this.losses = losses;
        this.sumRemainingTime=sumRemainingTime;
        this.numOfGames=wins+losses;
        winningRate = (double) wins/(numOfGames) * 100;
        if(wins==0) {
            this.avgRemainingTime=0;
            winningRate = 0;
        } else {
            this.avgRemainingTime=(double)sumRemainingTime/wins;
        }
    }

    public void incrementWins(int value) {
        wins+=value;
        numOfGames=wins+losses;
        winningRate = (double) wins/(numOfGames) * 100;
    }

    public void incrementSumRemainingTime(int value) {
        sumRemainingTime+=value;
        avgRemainingTime= (double) sumRemainingTime /wins;
    }

    public void incrementLosses(int value) {
        losses+=value;
        numOfGames=wins+losses;
        winningRate = (double) wins/(numOfGames) * 100;
    }

    public int getTime() {
        return time;
    }

    public int getPairs() {
        return pairs;
    }

    public int getWins() {return wins;}
    public int getLosses() {return losses;}
    public double getAvgRemainingTime() {return avgRemainingTime;}
    public double getWinningRate() {return  winningRate;}
    public int getNumOfGames() {return  numOfGames;}

    public static class CustomDoubleSerializer extends JsonSerializer<Double> {
        private static final DecimalFormat df = new DecimalFormat("0");

        @Override
        public void serialize(Double value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value == null) {
                gen.writeNull();
            } else {
                gen.writeString(df.format(value));
            }
        }
    }
}

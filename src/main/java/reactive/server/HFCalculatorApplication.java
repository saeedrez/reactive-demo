package reactive.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactive.model.HyperFundPackage;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class HFCalculatorApplication {
    private static DecimalFormat df = new DecimalFormat("0.00");
    private static int CONTRACT_VALIDITY_LENGTH = 600;
    private static double DAILY_REWARD_PERCENT = 0.5;
    private static int INITIAL_INVESTMENT_300 = 300;
    private static int INITIAL_INVESTMENT_500 = 500;
    private static int INITIAL_INVESTMENT_1000 = 1000;
    private static int INITIAL_INVESTMENT_10000 = 10000;

    public static void main(String[] args) {
        SpringApplication.run(HFCalculatorApplication.class, args);
        System.out.println("==> HFCalculatorApplication start...");

        int initialInvestment = INITIAL_INVESTMENT_1000 * 1;
        testHyperFundWithReinvestment(initialInvestment, 1, DAILY_REWARD_PERCENT);
        testHyperFundWithReinvestment(initialInvestment, 1, DAILY_REWARD_PERCENT + 0.1);
        testHyperFundWithReinvestment(initialInvestment, 1, DAILY_REWARD_PERCENT + 0.2);
        testHyperFundWithReinvestment(initialInvestment, 1, DAILY_REWARD_PERCENT + 0.3);
    }

    public static void calculateAllScenarios() {
        testHyperFundWithoutReinvestment(1000);
        testHyperFundWithReinvestment(1000, 1, DAILY_REWARD_PERCENT);
        testHyperFundWithReinvestment(2000, 1, DAILY_REWARD_PERCENT);
        testHyperFundWithReinvestment(3000, 1, DAILY_REWARD_PERCENT);
        testHyperFundWithReinvestment(5000, 1, DAILY_REWARD_PERCENT);
        testHyperFundWithReinvestment(10000, 1, DAILY_REWARD_PERCENT);
        testHyperFundWithReinvestment(20000, 1, DAILY_REWARD_PERCENT);
        testHyperFundWithReinvestment(30000, 1, DAILY_REWARD_PERCENT);
        testHyperFundWithReinvestment(50000, 1, DAILY_REWARD_PERCENT);
        testHyperFundWithReinvestment(100000, 1, DAILY_REWARD_PERCENT);

        List<Double> initialInvestments = new ArrayList<>();
        initialInvestments.add(1000.0);
        initialInvestments.add(1800.0);
        initialInvestments.add(5500.0);
        testHyperFundWithReinvestmentStartWithMultiplePackages(getInitialPackages(initialInvestments, DAILY_REWARD_PERCENT), 1, DAILY_REWARD_PERCENT);
    }

    public static void testHyperFundWithoutReinvestment(double initialInvestment) {
        double dailyRewardPercent = 0.005;
        double dailyRewardCash = initialInvestment * dailyRewardPercent;
        int hyperFundMultiple = 3;
        double totalPrincipleAndReward = initialInvestment * hyperFundMultiple;

        System.out.println("\n=====================================================================================================================================");
        System.out.println("==> Investment without rebuy:");

        for (int i = 0; i < CONTRACT_VALIDITY_LENGTH; i++) {
            initialInvestment -= dailyRewardCash;
            totalPrincipleAndReward -= dailyRewardCash;
            System.out.println("==> day " + (i + 1) + " reward: " + df.format(dailyRewardCash) + " (monthly: " + df.format(dailyRewardCash * 30) + ")" +
                    "   initial investment: " + df.format(initialInvestment) + "   total amount to be returned: " + df.format(totalPrincipleAndReward));
        }
    }

    public static Map<Integer, HyperFundPackage> testHyperFundWithReinvestment(int initialInvestment, int reinvestmentFrequencyInDays, double dailyRewardPercent) {
        HashMap<Integer, HyperFundPackage> packages = new HashMap<>();
        int packageNumber = 1;
        packages.put(packageNumber, new HyperFundPackage(initialInvestment, dailyRewardPercent)); // first package

        System.out.println("\n=====================================================================================================================================");
        System.out.println("==> HF calculation with rebuys: Initial investment: " + initialInvestment + "   rebuy frequency: " + reinvestmentFrequencyInDays + "   DR%: " + dailyRewardPercent);
        // calculate rewards each day and see if you can buy a new package. Update the totalReward also.
        reinvestmentFrequencyInDays = reinvestmentFrequencyInDays <= 0 ? 1 : reinvestmentFrequencyInDays;

        calculatePackageRewardsThruTheValidityPeriod(packages, reinvestmentFrequencyInDays, dailyRewardPercent);
        System.out.println("==> Finished (end of first 600 days): Package size: " + packages.size() + "   Total pending rewards: " + (int) getPendingRewards(packages));
        return packages;
    }

     public static Map<Integer, HyperFundPackage> testHyperFundWithReinvestmentStartWithMultiplePackages(HashMap<Integer, HyperFundPackage> packages, int reinvestmentFrequencyInDays,
                                                                                                         double dailyRewardPercent) {
//        HashMap<Integer, HyperFundPackage> packages = new HashMap<>();
        int packageNumber = packages.size();
//        packages.put(packageNumber, new HyperFundPackage(initialInvestment)); // first package
        double totalRewards = 0;

        int numberOf50HUPackages = 0;
        double dailyReward = 0;
        int cnt = 0;
        System.out.println("\n=====================================================================================================================================");
        System.out.println("==> HF calculation with rebuys, initialized with multiple packages: " + "   rebuy frequency: " + reinvestmentFrequencyInDays);
        // calculate rewards each day and see if you can buy a new package. Update the totalReward also.
        reinvestmentFrequencyInDays = reinvestmentFrequencyInDays <= 0 ? 1 : reinvestmentFrequencyInDays;

        calculatePackageRewardsThruTheValidityPeriod(packages, reinvestmentFrequencyInDays, dailyRewardPercent);
        System.out.println("==> Finished (end of first 600 days): Package size: " + packages.size() + "   Total pending rewards: " + (int) getPendingRewards(packages));
        return packages;
    }

    private static void calculatePackageRewardsThruTheValidityPeriod(HashMap<Integer, HyperFundPackage> packages, int reinvestmentFrequencyInDays, double dailyRewardPercent) {
        double dailyReward = 0;
        double totalRewards = 0;
        int packageNumber = packages.size();
        int numberOf50HUPackages = 0;

        for (int days = 1; dailyReward >= 0 && days < CONTRACT_VALIDITY_LENGTH; days++) {
            dailyReward = getTodaysRewards(packages); // get today's reward
            totalRewards += dailyReward;

            if (totalRewards / 50 >= 1 && days % reinvestmentFrequencyInDays == 0) { // reinvest if divisible by 50
                packageNumber++;

                // how many packages of 50HU
                numberOf50HUPackages = (int) totalRewards / 50;

                totalRewards -= numberOf50HUPackages * 50;
                packages.put(packageNumber, new HyperFundPackage(numberOf50HUPackages * 50, dailyRewardPercent));
            } else {
//                System.out.println("Not enought to buy packages: totalRewards: " + totalRewards);
            }
            printRewards(packages, numberOf50HUPackages, totalRewards, dailyReward, days, 30, false);
            numberOf50HUPackages = 0;
        }

    }

    private static void printRewards(HashMap<Integer, HyperFundPackage> packages, int numberOf50HUPackages, double totalRewards, double dailyReward,
                                     int currentcalculationDay, int printFrequencyInDays, boolean verbose) {
        if (verbose) {
            if (currentcalculationDay == 1 || currentcalculationDay % printFrequencyInDays == 0 || currentcalculationDay == 599)
                System.out.println("Day: " + currentcalculationDay + " (month: " + (currentcalculationDay / 30) + "): daily reward: " + df.format(dailyReward)
                        + " (monthly reward: " + df.format(dailyReward * 30) + ")"
                        //                        + "   total reward b4 rebuy: " + (totalRewards + numberOf50HUPackages * 50)
                        + "   bought " + numberOf50HUPackages + " x 50HU packages"
                        //                        + "   total rewards after rebuy: " + totalRewards
                        + "   total number of packages: " + packages.size() + "   pending rewards: " + df.format(getPendingRewards(packages)));
        } else {
            if (currentcalculationDay == 1 || currentcalculationDay % printFrequencyInDays == 0 || currentcalculationDay == 599)
                System.out.println("D: " + currentcalculationDay + " (M: " + (currentcalculationDay / 30) + "): DR: " + df.format(dailyReward)
                        + " (MR: " + df.format(dailyReward * 30) + ")"
                        //                        + "   total reward b4 rebuy: " + (totalRewards + numberOf50HUPackages * 50)
                        + "   bought " + numberOf50HUPackages + "x50HU pkgs"
                        //                        + "   total rewards after rebuy: " + totalRewards
                        + "   total pkgs: " + packages.size() + "   pending rewards: " + df.format(getPendingRewards(packages)));
        }

    }

    private static HashMap<Integer, HyperFundPackage> getInitialPackages(List<Double> initialInvestments, double dailyRewardPercent) {
        HashMap<Integer, HyperFundPackage> initialPackages = new HashMap<>();

        for (int i = 0; i < initialInvestments.size(); i++) {
            initialPackages.put(i+1, new HyperFundPackage(initialInvestments.get(i), dailyRewardPercent));
        }

        return initialPackages;
    }
    // return todays rewards based on all packages that are purchased
    private static double getTodaysRewards(HashMap<Integer, HyperFundPackage> packages) {
        double todaysReward = 0;
        for (int i=1; i <= packages.size(); i++) {
            todaysReward += packages.get(i).getDailyRewardAndUpdatePendingTotal();
        }
        return todaysReward;
    }

    private static double getPendingRewards(HashMap<Integer, HyperFundPackage> packages) {
        double totalRewardsPending = 0;
        for (int i = 1; i <= packages.size(); i++) {
            totalRewardsPending += packages.get(i).getPendingRewards();
//            System.out.println("==> Package " + i + ": pending rewards: " + packages.get(i).getPendingRewards());
        }
        return totalRewardsPending;
    }
}

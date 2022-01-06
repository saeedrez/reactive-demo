package reactive.model;

public class HyperFundPackage {
    double rewardPercent = 0.5;
    double initialInvestment;
    double pendingRewards;

    public HyperFundPackage(double initialInvestment, double rewardPercent) {
        this.rewardPercent = rewardPercent <= 0.0 ? 0.5 : rewardPercent;
        this.initialInvestment = initialInvestment;
        pendingRewards = 3 * initialInvestment;
    }

    public double getDailyRewardAndUpdatePendingTotal() {
        double todaysReward = initialInvestment * rewardPercent/100;
        if (pendingRewards <= 0)
            return 0;
        if (pendingRewards <= todaysReward) {
            todaysReward = pendingRewards;
            pendingRewards = 0;
            return todaysReward;
        }
        pendingRewards -= todaysReward;

        return todaysReward;
    }

    public double getPendingRewards() {
        return pendingRewards;
    }
}

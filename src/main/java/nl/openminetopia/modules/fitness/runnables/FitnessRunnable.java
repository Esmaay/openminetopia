package nl.openminetopia.modules.fitness.runnables;

import lombok.SneakyThrows;
import nl.openminetopia.OpenMinetopia;
import nl.openminetopia.api.player.fitness.Fitness;
import nl.openminetopia.api.player.fitness.FitnessStatisticType;
import nl.openminetopia.api.player.objects.MinetopiaPlayer;
import nl.openminetopia.modules.data.storm.StormDatabase;
import nl.openminetopia.modules.fitness.FitnessModule;
import nl.openminetopia.modules.fitness.configuration.FitnessConfiguration;
import nl.openminetopia.modules.fitness.models.FitnessBoosterModel;
import nl.openminetopia.modules.fitness.models.FitnessStatisticModel;
import nl.openminetopia.modules.fitness.utils.FitnessUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class FitnessRunnable extends BukkitRunnable {

    private final Fitness fitness;
    private final OfflinePlayer player;
    private boolean force = false; // used to force the runnable to run, even if player is offline

    public FitnessRunnable(Fitness fitness) {
        this.fitness = fitness;
        this.player = fitness.getMinetopiaPlayer().getBukkit();
    }

    @Override
    @SneakyThrows
    public void run() {
        FitnessConfiguration config = OpenMinetopia.getModuleManager().get(FitnessModule.class).getConfiguration();

        if (player == null || !player.isOnline() && !force) {
            cancel();
            return;
        }

        List<FitnessBoosterModel> boosterModels = new ArrayList<>(fitness.getBoosters());
        for (FitnessBoosterModel boosterModel : boosterModels) {
            if (!boosterModel.isExpired()) continue;
            fitness.removeBooster(boosterModel);
        }

        if (!force) {
            MinetopiaPlayer minetopiaPlayer = fitness.getMinetopiaPlayer();
            if (minetopiaPlayer == null || !minetopiaPlayer.isInPlace()) {
                FitnessUtils.clearFitnessEffects(player.getPlayer());
                return;
            }
        }

        updateFitnessStatistic(FitnessStatisticType.WALKING, Statistic.WALK_ONE_CM);
        updateFitnessStatistic(FitnessStatisticType.CLIMBING, Statistic.CLIMB_ONE_CM);
        updateFitnessStatistic(FitnessStatisticType.SPRINTING, Statistic.SPRINT_ONE_CM);
        updateFitnessStatistic(FitnessStatisticType.SWIMMING, Statistic.SWIM_ONE_CM);
        updateFitnessStatistic(FitnessStatisticType.FLYING, Statistic.AVIATE_ONE_CM);
        updateEatingFitness();

        int totalFitness = calculateTotalFitness() + calculateFitnessBoost();
        if (totalFitness <= 0) totalFitness = 1;

        fitness.setTotalFitness(Math.min(totalFitness, config.getMaxFitnessLevel()));
        if (player.isOnline()) fitness.apply();

        if (force) {
            force = false;
            cancel();
        }
    }

    public void forceRun() {
        force = true;
        run();
    }

    private void updateFitnessStatistic(FitnessStatisticType type, Statistic statistic) {
        FitnessStatisticModel statModel = fitness.getStatistic(type);
        int currentDistance = player.getStatistic(statistic);
        int newFitness = FitnessUtils.calculateFitness(currentDistance, (int) statModel.getProgressPerPoint());

        if (statModel.getFitnessGained() != newFitness && newFitness <= statModel.getMaximum()) {
            statModel.setFitnessGained(Math.min(newFitness, statModel.getMaximum()));
        }

        fitness.setStatistic(type, statModel);
    }

    private void updateEatingFitness() {
        FitnessConfiguration config = OpenMinetopia.getModuleManager().get(FitnessModule.class).getConfiguration();

        FitnessStatisticModel eatingStat = fitness.getStatistic(FitnessStatisticType.EATING);
        double eatingPoints = (eatingStat.getSecondaryPoints() * config.getPointsForCheapFood())
                + (eatingStat.getTertiaryPoints() * config.getPointsForLuxuryFood());

        eatingStat.setPoints(eatingPoints);
        if (eatingPoints >= 1 && eatingStat.getFitnessGained() <= eatingStat.getMaximum()) {
            eatingStat.setFitnessGained(eatingStat.getFitnessGained() + 1);
            eatingStat.setPoints(0.0);
        }

        fitness.setStatistic(FitnessStatisticType.EATING, eatingStat);
    }

    private int calculateTotalFitness() {
        FitnessConfiguration config = OpenMinetopia.getModuleManager().get(FitnessModule.class).getConfiguration();

        return fitness.getStatistics().stream()
                .mapToInt(FitnessStatisticModel::getFitnessGained)
                .sum() + config.getDefaultFitnessLevel();
    }

    private int calculateFitnessBoost() {
        return fitness.getBoosters().stream()
                .mapToInt(FitnessBoosterModel::getAmount)
                .sum();
    }
}
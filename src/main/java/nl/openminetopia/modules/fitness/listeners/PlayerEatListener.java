package nl.openminetopia.modules.fitness.listeners;

import nl.openminetopia.OpenMinetopia;
import nl.openminetopia.api.player.PlayerManager;
import nl.openminetopia.api.player.fitness.FitnessStatisticType;
import nl.openminetopia.api.player.objects.MinetopiaPlayer;
import nl.openminetopia.modules.fitness.FitnessModule;
import nl.openminetopia.modules.fitness.configuration.FitnessConfiguration;
import nl.openminetopia.modules.fitness.models.FitnessStatisticModel;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;

public class PlayerEatListener implements Listener {

    @EventHandler
    public void playerEat(final PlayerItemConsumeEvent event) {
        if (!event.getItem().getType().isEdible()) return;
        FitnessModule fitnessModule = OpenMinetopia.getModuleManager().get(FitnessModule.class);
        FitnessConfiguration configuration = fitnessModule.getConfiguration();

        if (!configuration.getCheapFood().contains(event.getItem().getType().name()) && !configuration.getLuxuryFood().contains(event.getItem().getType().name()))
            return;

        Player player = event.getPlayer();
        MinetopiaPlayer minetopiaPlayer = PlayerManager.getInstance().getOnlineMinetopiaPlayer(player);
        if (minetopiaPlayer == null) return;

        FitnessStatisticModel eatingStatistic = minetopiaPlayer.getFitness().getStatistic(FitnessStatisticType.EATING);

        if (configuration.getCheapFood().contains(event.getItem().getType().name())) {
            eatingStatistic.setPoints(eatingStatistic.getPoints() + configuration.getPointsForCheapFood());
            eatingStatistic.setSecondaryPoints(eatingStatistic.getSecondaryPoints() + 1);
        } else if (configuration.getLuxuryFood().contains(event.getItem().getType().name())) {
            eatingStatistic.setPoints(eatingStatistic.getPoints() + configuration.getPointsForLuxuryFood());
            eatingStatistic.setTertiaryPoints(eatingStatistic.getTertiaryPoints() + 1);
        }

        double currentEatingPoints = eatingStatistic.getPoints();

        if (eatingStatistic.getPoints() >= 1 && eatingStatistic.getFitnessGained() <= configuration.getMaxFitnessByEating()) {
            if (currentEatingPoints % (eatingStatistic.getSecondaryPoints() + eatingStatistic.getTertiaryPoints()) == 0) {
                minetopiaPlayer.getFitness().setStatistic(FitnessStatisticType.EATING, eatingStatistic);
                return;
            }
            eatingStatistic.setFitnessGained(eatingStatistic.getFitnessGained() + 1);
            eatingStatistic.setPoints(0.0);
        }
        minetopiaPlayer.getFitness().setStatistic(FitnessStatisticType.EATING, eatingStatistic);
    }
}

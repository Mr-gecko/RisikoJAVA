package server.bot;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import model.*;
import model.enums.TurnPhase;
import model.network.*;
import model.utils.Logger;
import server.network.ServerNetwork;

public class BotBrain {

    private static final Logger logger = new Logger("BOT");
    private static final int SLOWDOWN_TIME_PER_REQUEST = 750;

    private final ConcurrentHashMap<String, Boolean> lastActedKey = new ConcurrentHashMap<>();

    public BotBrain() {}

    private void slowdown(int time) {
        try { Thread.sleep(time); } catch (InterruptedException e) { e.printStackTrace(); }
    }

    private void cleanOldKeys(long gameId) {
        lastActedKey.keySet().removeIf(k -> k.startsWith(gameId + ":"));
    }

    public void maybeAct(ServerNetwork serverNetwork, Game game) {
        if (game == null || game.hasWinner()) return;

        if (!game.areTurnsStarted()) {
            submitSetupActions(serverNetwork, game);
            return;
        }

        Player current = game.getCurrentPlayer();
        if (current == null || !current.isBot()) return;

        long playerId = current.getId();
        TurnPhase phase = game.getCurrentPhase();

        String actionKey = game.getId() + ":" + playerId + ":" + phase.name();
        if (lastActedKey.putIfAbsent(actionKey, Boolean.TRUE) != null) {
            logger.log("bot skipping duplicate | key: " + actionKey);
            return;
        }

        logger.log("bot acting | player: " + playerId + " | phase: " + phase);

        switch (phase) {
            case DEPLOY -> {
                doDeploy(serverNetwork, game, current);
                goNextPhase(serverNetwork, game);
            }
            case ATTACK -> {
            	boolean attacked = doAttack(serverNetwork, game, current);
                if (attacked) {
                    lastActedKey.remove(actionKey); // allow re-entry after each attack
                } else {
                    goNextPhase(serverNetwork, game);
                }
            }
            case MOVE -> {
                doMove(serverNetwork, game, current);
                goNextPhase(serverNetwork, game);
            }
            default -> {}
        }
    }

    // ── SETUP ──────────────────────────────────────────────────────────────

    private void submitSetupActions(ServerNetwork serverNetwork, Game game) {
        for (Player player : game.getLobby().getPlayers()) {
            if (!player.isBot() || player.getAvailableTroops() <= 0) continue;
            doDeploy(serverNetwork, game, player);
        }
    }

    private void goNextPhase(ServerNetwork serverNetwork, Game game) {
        slowdown(SLOWDOWN_TIME_PER_REQUEST);
        cleanOldKeys(game.getId());
        send(serverNetwork, new GameRequest(RequestInstruction.GAME_NEXT_PHASE, game.getId()));
    }

    // ── DEPLOY ─────────────────────────────────────────────────────────────
    // Priority: reinforce the border country with the worst troop ratio
    // (fewest own troops relative to the strongest adjacent enemy).
    // Fallback: the owned border country with fewest troops.

    private void doDeploy(ServerNetwork serverNetwork, Game game, Player bot) {
        slowdown(SLOWDOWN_TIME_PER_REQUEST);
        int troops = bot.getAvailableTroops();
        if (troops <= 0) return;

        List<Country> owned = ownedCountries(game, bot);
        if (owned.isEmpty()) return;

        // Score each border country by how threatened it is (enemy strength / own troops).
        // Deploy everything there to shore up the weakest point.
        Country target = owned.stream()
            .filter(c -> hasEnemyNeighbour(game, c, bot))
            .max(Comparator.comparingDouble(c -> threatScore(game, c, bot)))
            .orElseGet(() ->
                // No border countries (bot owns everything) — stack on the strongest
                owned.stream()
                    .max(Comparator.comparingInt(Country::getTroops))
                    .orElse(owned.get(0)));

        logger.log("bot deploy | country: " + target.getId() + " troops: " + troops);
        send(serverNetwork, new GameRequest(
            RequestInstruction.GAME_DEPLOY_TROOPS,
            game.getId(), bot.getId(), target.getId(), troops));
    }

    // ── ATTACK ─────────────────────────────────────────────────────────────
    // Attack selection priority:
    //   1. Complete a continent we are close to finishing (highest % owned).
    //   2. Otherwise pick the attack with the best strength ratio (own/enemy troops).
    // Only attack if own troops > enemy troops (ratio > 1), to avoid suicidal attacks.
    // Stop if no profitable attack exists.
    
    private boolean doAttack(ServerNetwork serverNetwork, Game game, Player bot) {
        slowdown(SLOWDOWN_TIME_PER_REQUEST);
        if (game.hasWinner()) return false;

        List<AttackOption> options = new ArrayList<>();
        for (Country source : ownedCountries(game, bot)) {
            if (source.getTroops() < 3) continue;
            for (Country target : enemyNeighbours(game, source, bot)) {
                if (!source.getBorders().contains(target.getId())) continue;
                double ratio = (double) source.getTroops() / (target.getTroops() + 1);
                if (ratio < 1.2) continue;
                options.add(new AttackOption(source, target, ratio));
            }
        }

        if (options.isEmpty()) return false;

        AttackOption best = options.stream()
            .filter(o -> completesContinent(game, bot, o.target))
            .max(Comparator.comparingDouble(o -> o.ratio))
            .orElseGet(() ->
                options.stream()
                    .max(Comparator.comparingDouble(o -> o.ratio))
                    .orElseThrow());

        int attackTroops = best.source.getTroops() - 1;
        logger.log("bot attack | " + best.source.getId() + "(" + best.source.getTroops()
            + ") -> " + best.target.getId() + "(" + best.target.getTroops()
            + ") with " + attackTroops);

        send(serverNetwork, new GameRequest(
            RequestInstruction.GAME_ATTACK,
            game.getId(), bot.getId(),
            best.source.getId(), best.target.getId(), attackTroops));
        return true;
    }

    private static class AttackOption {
        final Country source, target;
        final double ratio;
        AttackOption(Country source, Country target, double ratio) {
            this.source = source; this.target = target; this.ratio = ratio;
        }
    }

    // Returns true if capturing this target would complete the continent for the bot
    private boolean completesContinent(Game game, Player bot, Country target) {
        String continentId = target.getContinentId();
        return game.getWorld().getCountries().values().stream()
            .filter(c -> c.getContinentId().equals(continentId))
            .filter(c -> !c.getId().equals(target.getId()))
            .allMatch(c -> c.getOwner() == bot.getColor());
    }

    // ── MOVE ───────────────────────────────────────────────────────────────
    // Move surplus troops from the safest inland country toward the most
    // threatened border country.

    private void doMove(ServerNetwork serverNetwork, Game game, Player bot) {
        slowdown(SLOWDOWN_TIME_PER_REQUEST);
        if (game.hasWinner()) return;

        List<Country> owned = ownedCountries(game, bot);

        // Source: inland country (no enemy neighbours) with the most surplus troops
        Country source = owned.stream()
            .filter(c -> c.getTroops() > 2)
            .filter(c -> !hasEnemyNeighbour(game, c, bot))
            .max(Comparator.comparingInt(Country::getTroops))
            .orElseGet(() -> owned.stream()
                .filter(c -> c.getTroops() > 2)
                .max(Comparator.comparingInt(Country::getTroops))
                .orElse(null));

        if (source == null) return;

        // Target: border country with the worst threat score
        Country target = owned.stream()
            .filter(c -> !c.getId().equals(source.getId()))
            .filter(c -> hasEnemyNeighbour(game, c, bot))
            .filter(c -> source.getBorders().contains(c.getId())) // must be adjacent
            .max(Comparator.comparingDouble(c -> threatScore(game, c, bot)))
            .orElse(null);

        if (target == null) return;

        int troops = source.getTroops() - 1;
        logger.log("bot move | " + source.getId() + " -> " + target.getId() + " x" + troops);
        send(serverNetwork, new GameRequest(
            RequestInstruction.GAME_MOVE_TROOPS,
            game.getId(), bot.getId(), source.getId(), target.getId(), troops));
    }

    // ── SCORING HELPERS ────────────────────────────────────────────────────

    // Threat score: sum of adjacent enemy troops divided by own troops.
    // Higher = more threatened.
    private double threatScore(Game game, Country country, Player bot) {
        int enemyStrength = enemyNeighbours(game, country, bot).stream()
            .mapToInt(Country::getTroops).sum();
        return (double) enemyStrength / (country.getTroops() + 1);
    }

    // ── GENERIC HELPERS ────────────────────────────────────────────────────

    private void send(ServerNetwork serverNetwork, GameRequest request) {
        serverNetwork.receive(new Payload(Payloads.GAME_REQUEST, request), null);
    }

    private List<Country> ownedCountries(Game game, Player player) {
        return game.getWorld().getCountries().values().stream()
            .filter(c -> c.getOwner() == player.getColor())
            .collect(Collectors.toList());
    }

    private boolean hasEnemyNeighbour(Game game, Country country, Player bot) {
        return !enemyNeighbours(game, country, bot).isEmpty();
    }

    private List<Country> enemyNeighbours(Game game, Country country, Player bot) {
        return country.getBorders().stream()
            .map(id -> game.getWorld().getCountry(id))
            .filter(c -> c != null && c.getOwner() != bot.getColor())
            .collect(Collectors.toList());
    }
}
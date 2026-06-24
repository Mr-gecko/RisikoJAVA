package server.model;

import java.util.List;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

/*
 * 
 *  1.
	Define the fight probability
	Let p = attacker win chance per fight (e.g. 0.46). Then q = 1 − p = defender's chance.
	
	2.
	Expected outcome after all fights
	In a + d − 1 fights, the attacker expects to win a·p − d·q net troops. Positive = attacker ahead.
	
	3.
	Spread (uncertainty)
	The standard deviation of the result is √((a+d)·p·q). This shrinks relative to army size as armies grow larger.
	
	4.
	Normalise to a z-score
	Divide the mean by the spread: z = (a·p − d·q) / √((a+d)·p·q)
	
	5.
	Look up the probability
	Feed z into the Normal CDF: P = Φ(z). This is a standard formula, available in all languages.
 */

public class BattleProbability {
	
	
	public static int[] rawP(int atk, int def) {
		int P = 47;
		int[] losses = new int[] {0,0};
		while (atk>0 && def>0) {
			IntStream.range(0, Math.min(atk, def)).forEach(n -> {
				if (ThreadLocalRandom.current().nextInt(1, 100) <= 47) {
					losses[1] += 1;
				} else {
					losses[0] += 1;
				}
			});
		}
		return losses;
	}
	
	public static int[] bloodDices(int attackers, int defenders) {
		int[] totalLosses = new int[] {0,0};
		int[] losses = dices(attackers, defenders);
		attackers -= losses[0];
		defenders -= losses[1];
		totalLosses[0] += losses[0];
		totalLosses[1] += losses[1];
		while(attackers > 0 && defenders > 0) { // go on until nor the attacker has no more troops, or the defender is defeated
			losses = dices(attackers, defenders);
			attackers -= losses[0];
			defenders -= losses[1];
			totalLosses[0] += losses[0];
			totalLosses[1] += losses[1];
		}
		return totalLosses;
		
	}

	
	public static int[] dices(int attackers, int defenders) {
	    List<Integer> attackerRolls = new ArrayList<>(rollDice(attackers));
	    List<Integer> defenderRolls = new ArrayList<>(rollDice(defenders));
	    
	    List<List<Integer>> attackerRounds = generateRounds(attackerRolls);
	    List<List<Integer>> defenderRounds = generateRounds(defenderRolls); // fixed

	    int attackerLosses = 0;
	    int defenderLosses = 0;
	    
	    int comparisonRound = Math.min(attackerRounds.size(), defenderRounds.size());
	    
	    for (int r = 0; r < comparisonRound; r++) {
	        List<Integer> attackerRound = attackerRounds.get(r);
	        List<Integer> defenderRound = defenderRounds.get(r);
	        
	        int comparison = Math.min(attackerRound.size(), defenderRound.size()); // fixed
	        
	        for (int i = 0; i < comparison; i++) {
	            if (attackerRound.get(i) > defenderRound.get(i)) {
	                defenderLosses++;
	            } else {
	                attackerLosses++;
	            }
	        }
	    }
	    
	    return new int[] {attackerLosses, defenderLosses};
	}

	private static List<List<Integer>> generateRounds(List<Integer> rolls) {
	    List<Integer> remaining = new ArrayList<>(rolls); // don't mutate input
	    List<List<Integer>> rounds = new ArrayList<>();
	    
	    while (!remaining.isEmpty()) {
	        List<Integer> round = new ArrayList<>();
	        int take = Math.min(3, remaining.size());
	        IntStream.range(0, take).forEach(n -> {
	            round.add(remaining.get(0));
	            remaining.remove(0);
	        });
	        round.sort(Collections.reverseOrder());
	        rounds.add(round);
	    }
	    
	    return rounds;
	}
	
	public static int[] dices_old(int attackers, int defenders) {
		List<Integer> attackerRolls = rollDice(attackers);
		List<Integer> defenderRolls = rollDice(defenders);
		
		attackerRolls.sort(Collections.reverseOrder());
		defenderRolls.sort(Collections.reverseOrder());
		
		int attackerLosses = 0;
		int defenderLosses = 0;
		
		int comparison = Math.min(attackerRolls.size(), defenderRolls.size());
		
		for (int i = 0 ; i < comparison ; i++) {
			if (attackerRolls.get(i) > defenderRolls.get(i)) {
				defenderLosses++;
			} else {
				attackerLosses++;
			}
		}
		
		return new int[] {attackerLosses, defenderLosses};
		
	}
	
	private static List<Integer> rollDice(int count) {
	    List<Integer> results = new ArrayList<>();
	    for (int i = 0; i < count; i++) {
	        results.add(ThreadLocalRandom.current().nextInt(1, 7));
	    }
	    return results;
	}
	
    public static final double P = 0.47; // attacker's per-fight win chance
	
	public static double dynamicP(long attackers, long defenders) {
	double r = (double) attackers / defenders;
		double pMin = P; // min percentage
		double pMax = 0.68; // max percentage
		double mid = 3.00; // midpoint ratio
		double k = 1; // steepness
		double s = 1.0 / (1.0 + Math.exp(-k * (r - mid)));
		return pMin + (pMax - pMin) * s; // return P
	}

	public static double winProbability(long attackers, long defenders) {
		if (attackers <= 0)
			return 0.0;
		if (defenders <= 0)
			return 1.0;
		double p = dynamicP(attackers, defenders);
		double q = 1.0 - p;
		double a = attackers, d = defenders;
		double mean = a * p - d * q;
		double sigma = Math.sqrt((a + d) * p * q);
		double z = Math.max(-8.0, Math.min(8.0, mean / sigma));
		return normalCDF(z);
	}

//    // O(1) — works for any troop count, including millions
//    public static double winProbabilityStaticP(long attackers, long defenders) {
//        if (attackers <= 0) return 0.0;
//        if (defenders <= 0) return 1.0;
//
//        double a = attackers;
//        double d = defenders;
//        double q = 1.0 - P;
//
//        // Normal approximation to the binomial
//        double mean   = a * P - d * q;
//        double stddev = Math.sqrt((a + d) * P * q);
//        
//        // clamping z
//        double z = mean / stddev;
////        z = Math.max(-8.0, Math.min(8.0, z));
//
//        return normalCDF(z);
//    }

    // Standard normal CDF via error function (built into Java)
    private static double normalCDF(double z) {
        return 0.5 * (1.0 + erf(z / Math.sqrt(2.0)));
    }

    // Abramowitz & Stegun approximation — error < 1.5e-7
    private static double erf(double z) {
        double t = 1.0 / (1.0 + 0.3275911 * Math.abs(z));
        double poly = t * (0.254829592
                   + t * (-0.284496736
                   + t * (1.421413741
                   + t * (-1.453152027
                   + t * 1.061405429))));
        double result = 1.0 - poly * Math.exp(-z * z);
        return z >= 0 ? result : -result;
    }

//    public static void main(String[] args) {
//        // Small numbers — compare with your table
//        System.out.printf("P(2,1)   = %.1f%%%n", winProbability(2, 1) * 100);   // ~75%
//        System.out.printf("P(5,5)   = %.1f%%%n", winProbability(5, 5) * 100);   // ~36%
//        System.out.printf("P(10,10) = %.1f%%%n", winProbability(10, 10) * 100); // ~46%
//
//        // Large numbers — instant, no memory needed
//        System.out.printf("P(1000, 1000)       = %.1f%%%n", winProbability(1_000, 1_000) * 100);
//        System.out.printf("P(1174, 1000)       = %.1f%%%n", winProbability(1_174, 1_000) * 100);
//        System.out.printf("P(5000, 3000)       = %.1f%%%n", winProbability(5_000, 3_000) * 100);
//        System.out.printf("P(1_000_000, 900_000) = %.1f%%%n", winProbability(1_000_000, 900_000) * 100);
//        System.out.printf("P(10_000_000, 9_500_000) = %.1f%%%n", winProbability(10_000_000, 9_500_000) * 100);
//    }
}
package com.mathiasfarsinsen.modai.simulation;

/**
 * Tick intervals and production rates used by {@link CitySimulationEngine}.
 * Larger intervals reduce server load (per the "throttled/batched" spec
 * requirement) at the cost of coarser simulation granularity.
 */
public final class SimulationConfig {

    /** How often (in ticks) resource production runs. 20 ticks = 1 second. */
    public int resourceProductionIntervalTicks = 100;

    /** How often (in ticks) war skirmishes are resolved for each warring pair. */
    public int warResolutionIntervalTicks = 600; // ~30 seconds

    /** How often (in ticks) each city is checked for a possible rebellion. */
    public int rebellionCheckIntervalTicks = 1200; // ~1 minute

    /** Base food produced per gatherer per production interval. */
    public long foodPerGatherer = 4;
    /** Base wood produced per gatherer per production interval. */
    public long woodPerGatherer = 3;
    /** Base stone produced per gatherer per production interval. */
    public long stonePerGatherer = 2;
    /** Base iron produced per gatherer per production interval. */
    public long ironPerGatherer = 1;

    /** Food consumed per citizen (of any role) per production interval. */
    public long foodConsumedPerCitizen = 1;
}

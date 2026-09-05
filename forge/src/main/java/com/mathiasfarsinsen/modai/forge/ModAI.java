package com.mathiasfarsinsen.modai.forge;

import com.mathiasfarsinsen.modai.forge.command.CityCommand;
import com.mathiasfarsinsen.modai.forge.persistence.CityWorldData;
import com.mathiasfarsinsen.modai.naming.CityNameGenerator;
import com.mathiasfarsinsen.modai.rebellion.RebellionConfig;
import com.mathiasfarsinsen.modai.simulation.CitySimulationEngine;
import com.mathiasfarsinsen.modai.simulation.SimulationConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.slf4j.LoggerFactory;

/**
 * Main mod entry point. Wires the engine-agnostic {@code common} module
 * (city model, simulation, diplomacy, rebellion, knowledge) into Forge's
 * event bus: server ticks drive {@link CitySimulationEngine}, and world
 * save/load is backed by {@link CityWorldData}.
 *
 * <p>One {@link CitySimulationEngine} instance is kept per {@link ServerLevel}
 * so that each dimension's cities simulate independently.</p>
 */
@Mod("modai")
public final class ModAI {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger("modai");

    /** Per-level simulation engines, created lazily on first tick. */
    private final Map<ServerLevel, CitySimulationEngine> engines = new HashMap<>();
    private final RebellionConfig rebellionConfig = new RebellionConfig();
    private final SimulationConfig simulationConfig = new SimulationConfig();

    public ModAI() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("AI Cities mod initialized");
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CityCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        for (Map.Entry<ServerLevel, CitySimulationEngine> entry : engines.entrySet()) {
            ServerLevel level = entry.getKey();
            long tick = level.getGameTime();
            try {
                entry.getValue().tick(tick);
            } catch (RuntimeException e) {
                // Never let a simulation error take down the server tick loop.
                LOGGER.error("City simulation tick failed for level {}", level.dimension().location(), e);
            }
        }
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        for (ServerLevel level : engines.keySet()) {
            CityWorldData.get(level).setDirty();
        }
    }

    /** Lazily creates (and caches) the simulation engine for the given level, loading persisted cities on first access. */
    public CitySimulationEngine engineFor(ServerLevel level) {
        return engines.computeIfAbsent(level, l -> new CitySimulationEngine(
                CityWorldData.get(l).getCityManager(),
                simulationConfig,
                rebellionConfig,
                new CityNameGenerator(l.getSeed()),
                new Random(),
                message -> LOGGER.info("[AI Cities] {}", message)));
    }
}

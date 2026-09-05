package com.mathiasfarsinsen.modai.forge.command;

import com.mathiasfarsinsen.modai.city.City;
import com.mathiasfarsinsen.modai.city.CityRole;
import com.mathiasfarsinsen.modai.city.Citizen;
import com.mathiasfarsinsen.modai.city.Position;
import com.mathiasfarsinsen.modai.diplomacy.DiplomacyManager;
import com.mathiasfarsinsen.modai.forge.persistence.CityWorldData;
import com.mathiasfarsinsen.modai.naming.CityNameGenerator;
import com.mathiasfarsinsen.modai.persistence.CityManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.UUID;

/**
 * Implements the {@code /by} command family:
 * <ul>
 *   <li>{@code /by liste} — list all cities with key data</li>
 *   <li>{@code /by info <navn>} — detailed info about one city</li>
 *   <li>{@code /by gå <navn>} — teleport to a city's center</li>
 *   <li>{@code /by opret <navn>} — found a new city at the player's position,
 *       enrolling nearby villagers</li>
 *   <li>{@code /by krig <by1> <by2>} — declare war between two cities (op only)</li>
 *   <li>{@code /by fred <by1> <by2>} — make peace between two cities (op only)</li>
 * </ul>
 */
public final class CityCommand {

    /** Radius (in blocks) searched for nearby villagers when founding a city with {@code /by opret}. */
    private static final double ENROLLMENT_RADIUS = 32.0;
    private static final DiplomacyManager DIPLOMACY = new DiplomacyManager();

    private CityCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("by")
                .then(Commands.literal("liste")
                        .executes(CityCommand::listCities))
                .then(Commands.literal("info")
                        .then(Commands.argument("navn", StringArgumentType.greedyString())
                                .executes(CityCommand::cityInfo)))
                .then(Commands.literal("gå")
                        .then(Commands.argument("navn", StringArgumentType.greedyString())
                                .executes(CityCommand::teleportToCity)))
                .then(Commands.literal("opret")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("navn", StringArgumentType.string())
                                .executes(CityCommand::foundCity))
                        .executes(CityCommand::foundCityAutoNamed))
                .then(Commands.literal("krig")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("by1", StringArgumentType.string())
                                .then(Commands.argument("by2", StringArgumentType.greedyString())
                                        .executes(CityCommand::declareWar))))
                .then(Commands.literal("fred")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("by1", StringArgumentType.string())
                                .then(Commands.argument("by2", StringArgumentType.greedyString())
                                        .executes(CityCommand::makePeace)))));
    }

    private static CityManager managerFor(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        return CityWorldData.get(level).getCityManager();
    }

    private static int listCities(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        CityManager manager = managerFor(source);
        if (manager.getAllCities().isEmpty()) {
            source.sendSuccess(() -> Component.literal("Ingen byer er blevet grundlagt endnu."), false);
            return 0;
        }
        for (City city : manager.getAllCities()) {
            String leaderStatus = city.getLeaderId().isPresent() ? "har en leder" : "mangler en leder";
            source.sendSuccess(() -> Component.literal(String.format(
                    "%s — befolkning: %d, stabilitet: %d%%, %s",
                    city.getName(), city.getPopulation(), city.getStability(), leaderStatus)), false);
        }
        return manager.getAllCities().size();
    }

    private static int cityInfo(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        String name = StringArgumentType.getString(ctx, "navn");
        CityManager manager = managerFor(source);
        var cityOpt = manager.getCityByName(name);
        if (cityOpt.isEmpty()) {
            source.sendFailure(Component.literal("Ingen by ved navn '" + name + "' findes."));
            return 0;
        }
        City city = cityOpt.get();
        source.sendSuccess(() -> Component.literal(String.format(
                "%s: befolkning=%d, stabilitet=%d%%, center=%s, mad=%d, træ=%d, sten=%d, jern=%d",
                city.getName(), city.getPopulation(), city.getStability(), city.getCenter(),
                city.getResources().get(com.mathiasfarsinsen.modai.city.ResourceType.FOOD),
                city.getResources().get(com.mathiasfarsinsen.modai.city.ResourceType.WOOD),
                city.getResources().get(com.mathiasfarsinsen.modai.city.ResourceType.STONE),
                city.getResources().get(com.mathiasfarsinsen.modai.city.ResourceType.IRON))), false);
        return 1;
    }

    private static int teleportToCity(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        String name = StringArgumentType.getString(ctx, "navn");
        CityManager manager = managerFor(source);
        var cityOpt = manager.getCityByName(name);
        if (cityOpt.isEmpty()) {
            source.sendFailure(Component.literal("Ingen by ved navn '" + name + "' findes."));
            return 0;
        }
        City city = cityOpt.get();
        ServerPlayer player = source.getPlayerOrException();
        Position center = city.getCenter();
        player.teleportTo(center.getX() + 0.5, center.getY(), center.getZ() + 0.5);
        source.sendSuccess(() -> Component.literal("Teleporteret til " + city.getName() + "."), false);
        return 1;
    }

    private static int foundCityAutoNamed(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return foundCityInternal(ctx, null);
    }

    private static int foundCity(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return foundCityInternal(ctx, StringArgumentType.getString(ctx, "navn"));
    }

    private static int foundCityInternal(CommandContext<CommandSourceStack> ctx, String requestedName)
            throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = source.getLevel();
        CityManager manager = managerFor(source);

        CityNameGenerator nameGenerator = new CityNameGenerator();
        manager.getAllCities().forEach(c -> nameGenerator.reserve(c.getName()));
        String name = (requestedName == null || requestedName.isBlank()) ? nameGenerator.generate() : requestedName;

        Position center = new Position(player.getBlockX(), player.getBlockY(), player.getBlockZ());
        City city = new City(UUID.randomUUID(), name, center);

        AABB searchBox = new AABB(player.blockPosition()).inflate(ENROLLMENT_RADIUS);
        List<Villager> nearbyVillagers = level.getEntitiesOfClass(Villager.class, searchBox);
        CityRole[] rotation = { CityRole.GATHERER, CityRole.BUILDER, CityRole.GUARD };
        int index = 0;
        for (Villager villager : nearbyVillagers) {
            CityRole role = index == 0 ? CityRole.LEADER : rotation[(index - 1) % rotation.length];
            city.addCitizen(new Citizen(villager.getUUID(), role));
            index++;
        }

        manager.addCity(city);
        source.sendSuccess(() -> Component.literal("Byen " + city.getName() + " er grundlagt med "
                + city.getPopulation() + " indbyggere."), true);
        return 1;
    }

    private static int declareWar(CommandContext<CommandSourceStack> ctx) {
        return withTwoCities(ctx, (source, a, b) -> {
            boolean changed = DIPLOMACY.declareWar(a, b);
            source.sendSuccess(() -> Component.literal(changed
                    ? a.getName() + " har erklæret krig mod " + b.getName() + "!"
                    : a.getName() + " og " + b.getName() + " er allerede i krig."), true);
        });
    }

    private static int makePeace(CommandContext<CommandSourceStack> ctx) {
        return withTwoCities(ctx, (source, a, b) -> {
            boolean changed = DIPLOMACY.makePeace(a, b);
            source.sendSuccess(() -> Component.literal(changed
                    ? a.getName() + " og " + b.getName() + " har indgået fred."
                    : a.getName() + " og " + b.getName() + " var allerede i fred."), true);
        });
    }

    @FunctionalInterface
    private interface TwoCityAction {
        void run(CommandSourceStack source, City a, City b);
    }

    private static int withTwoCities(CommandContext<CommandSourceStack> ctx, TwoCityAction action) {
        CommandSourceStack source = ctx.getSource();
        String name1 = StringArgumentType.getString(ctx, "by1");
        String name2 = StringArgumentType.getString(ctx, "by2");
        CityManager manager = managerFor(source);
        var cityA = manager.getCityByName(name1);
        var cityB = manager.getCityByName(name2);
        if (cityA.isEmpty() || cityB.isEmpty()) {
            source.sendFailure(Component.literal("En eller begge byer blev ikke fundet."));
            return 0;
        }
        action.run(source, cityA.get(), cityB.get());
        return 1;
    }
}

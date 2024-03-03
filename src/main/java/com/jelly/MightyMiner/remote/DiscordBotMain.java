package com.jelly.MightyMiner.remote;

import cc.polyfrost.oneconfig.libs.eventbus.Subscribe;
import com.jelly.MightyMiner.MightyMiner;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

public class DiscordBotMain {
    private JDA jda;
    private boolean enabled = false;

    private Thread tryConnectThread;
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) throws InterruptedException {
        if (MightyMiner.config.enableBot && !enabled) {
            enabled = true;
            tryConnectThread = new Thread(() -> {
                try {
                    main();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            tryConnectThread.start();
        }
        if (!MightyMiner.config.enableBot && enabled) {
            enabled = false;
            shutdown();
        }
    }
    private void main() throws InterruptedException {
        if (MightyMiner.config.botToken.isEmpty()) {
            enabled = false;
            MightyMiner.config.enableBot = false;
            return;
        }

        jda = JDABuilder.createLight(MightyMiner.config.botToken)
                .disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE, CacheFlag.ACTIVITY)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .build();
        jda.awaitReady();
        jda.addEventListener(new CommandListener());
        jda.updateCommands().addCommands(
                Commands.slash("ping", "Calculate ping of the bot"),
                Commands.slash("enablemacro", "Enable bot"),
                Commands.slash("disablemacro", "Disable bot"),
                Commands.slash("screenshot","Screenshot"),
                Commands.slash("notify","use this for funny shenanigans")
                        .addOptions(
                                new OptionData(
                                        OptionType.STRING, "text", "type something :)", true
                                )
                        )
                )
                .queue();
    }
    private void shutdown(){
        jda.shutdown();
    }
}





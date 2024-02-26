package com.jelly.MightyMiner.remote;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.handlers.MacroHandler;
import com.jelly.MightyMiner.utils.Clock;
import com.jelly.MightyMiner.utils.LogUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.FileUpload;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ScreenShotHelper;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class CommandListener extends ListenerAdapter {

    public static final Minecraft mc = Minecraft.getMinecraft();
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event){
        switch (event.getName()) {
            case "ping":
                long time = System.currentTimeMillis();
                event.reply("Pong!").setEphemeral(true) // reply or acknowledge
                        .flatMap(v ->
                                event.getHook().editOriginalFormat("Pong: %d ms", System.currentTimeMillis() - time) // then edit original
                        ).queue(); // Queue both reply and edit
                break;
            case "disablemacro":
                if (MacroHandler.enabled) {
                    MacroHandler.disableScript();
                    event.reply("Disabled").queue();
                } else {
                    event.reply("Already disabled").queue();
                }
                break;
            case "enablemacro":
                if (!MacroHandler.enabled) {
                    MacroHandler.startScript(MightyMiner.config.macroType);
                    event.reply("Enabled").queue();
                } else {
                    event.reply("Already enabled").queue();
                }
                break;
            case "screenshot":
                event.deferReply().queue();
                EmbedBuilder embedBuilder = new EmbedBuilder();
                String image;

                image = getScreenshot();
                embedBuilder.setImage("attachment://image.png");
                int random = (int) (Math.random() * 0xFFFFFF);
                embedBuilder.setColor(random);

                event.getHook().sendMessageEmbeds(embedBuilder.build()).addFiles(FileUpload.fromData(Base64.getDecoder().decode(image), "image.png")).queue();
                break;
        }
    }
    public static String getScreenshot() {
        AtomicReference<String> base64img = new AtomicReference<>(null);
        disablePatcherShit();
        disableEssentialsShit();
        MightyMiner.tickTask = () -> {
            MightyMiner.tickTask = null;
            String random = UUID.randomUUID().toString();
            ScreenShotHelper.saveScreenshot(mc.mcDataDir, random, mc.displayWidth, mc.displayHeight, mc.getFramebuffer());
            File screenshotDirectory = new File(mc.mcDataDir.getAbsoluteFile(), "screenshots");
            File screenshotFile = new File(screenshotDirectory, random);
            byte[] bytes = Files.readAllBytes(Paths.get(screenshotFile.getAbsolutePath()));
            base64img.set(Base64.getEncoder().encodeToString(bytes));
            screenshotFile.getAbsoluteFile().setWritable(true);
            while(!screenshotFile.getAbsoluteFile().delete());

        };

        Clock timeout = new Clock();
        timeout.schedule(5000);
        while (base64img.get() == null) {
            if (timeout.getRemainingTime() < 0) {
                break;
            }
        }
        return base64img.get();
    }

    private static void disablePatcherShit() {
        try {
            Class<?> klazz = Class.forName("club.sk1er.patcher.config.PatcherConfig");
            Field field = klazz.getDeclaredField("screenshotManager");
            if (field.getBoolean(klazz)) {

                field.setBoolean(klazz, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void disableEssentialsShit()  {
        try {
            Class<?> klazz = Class.forName("gg.essential.config.EssentialConfig");
            Field field = klazz.getDeclaredField("essentialScreenshots");
            field.setAccessible(true);
            if (field.getBoolean(klazz)) {
                field.setBoolean(klazz, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

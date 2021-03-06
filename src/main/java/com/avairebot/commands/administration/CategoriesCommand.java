package com.avairebot.commands.administration;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.commands.Category;
import com.avairebot.commands.CategoryHandler;
import com.avairebot.commands.CommandMessage;
import com.avairebot.commands.CommandPriority;
import com.avairebot.contracts.commands.Command;
import com.avairebot.database.transformers.ChannelTransformer;
import com.avairebot.database.transformers.GuildTransformer;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CategoriesCommand extends Command {

    public CategoriesCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Categories Command";
    }

    @Override
    public String getDescription() {
        return "Shows status of all command categories in the current or mentioned channel, both for globally and per-channel.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command [channel]` - Displays the status of the command categories in the mentioned channel, or the current channel if no channel was mentioned.");
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Arrays.asList(ToggleCategoryCommand.class, ChangePrefixCommand.class);
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("categories", "cats");
    }

    @Override
    public CommandPriority getCommandPriority() {
        return CommandPriority.LOW;
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        TextChannel channel = context.getChannel();
        if (!context.getMentionedChannels().isEmpty()) {
            channel = context.getMentionedChannels().get(0);
        }

        String status = String.join("   ",
            Constants.EMOTE_ONLINE + " Enabled",
            Constants.EMOTE_AWAY + " Disabled in Channel",
            Constants.EMOTE_DND + " Disabled Globally"
        );

        GuildTransformer guildTransformer = context.getGuildTransformer();
        if (guildTransformer == null) {
            return sendErrorMessage(context, "errors.errorOccurredWhileLoading", "server settings");
        }

        ChannelTransformer transformer = guildTransformer.getChannel(channel.getId());

        List<String> items = new ArrayList<>();
        for (Category category : CategoryHandler.getValues()) {
            if (category.isGlobalOrSystem()) continue;

            if (!transformer.isCategoryEnabledGlobally(category)) {
                items.add(Constants.EMOTE_DND + category.getName());
                continue;
            }

            if (!transformer.isCategoryEnabled(category)) {
                items.add(Constants.EMOTE_AWAY + category.getName());
                continue;
            }

            items.add(Constants.EMOTE_ONLINE + category.getName());
        }

        context.makeInfo(status + "\n\n" + String.join("\n", items))
            .setTitle("Command Category Status for #" + channel.getName())
            .queue();

        return true;
    }
}

package com.premiumorcracked.velocity;

import com.premiumorcracked.command.PocCommandParser;
import com.premiumorcracked.command.PocMessages;
import com.premiumorcracked.runtime.PrefixRuntime;
import com.premiumorcracked.runtime.ReloadResult;
import com.velocitypowered.api.command.SimpleCommand;

import java.util.List;
import java.util.concurrent.CompletableFuture;

final class PocVelocityCommand implements SimpleCommand {
    private final PrefixRuntime runtime;

    PocVelocityCommand(PrefixRuntime runtime) {
        this.runtime = runtime;
    }

    @Override
    public void execute(Invocation invocation) {
        PocCommandParser.Action action = PocCommandParser.parse(invocation.arguments());
        switch (action) {
            case HELP -> invocation.source().sendMessage(PocMessages.help());
            case UNKNOWN -> invocation.source().sendMessage(PocMessages.unknown());
            case STATUS -> invocation.source().sendMessage(PocMessages.status(runtime.configuration(), true));
            case RELOAD -> {
                if (!invocation.source().hasPermission("premiumorcracked.reload")) {
                    invocation.source().sendMessage(PocMessages.noPermission());
                    return;
                }
                ReloadResult result = runtime.reloadFromDisk();
                if (result.success()) {
                    invocation.source().sendMessage(PocMessages.reloadSuccess(result));
                } else {
                    invocation.source().sendMessage(PocMessages.reloadFailure(result));
                }
            }
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return PocCommandParser.suggestions(invocation.arguments());
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return CompletableFuture.completedFuture(suggest(invocation));
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("premiumorcracked.command");
    }
}

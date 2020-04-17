package org.noixdecoco.app.command;

import org.noixdecoco.app.command.annotation.Command;
import org.noixdecoco.app.data.model.CoconutLedger;
import org.noixdecoco.app.dto.EventType;
import org.noixdecoco.app.dto.SlackRequestDTO;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.function.Predicate;

@Command(EventType.APP_MENTION)
public class RewardRankingsCommand extends RewardCommand {

    private String channel;

    public RewardRankingsCommand(String userId, String channel) {
        super(userId);
        this.channel = channel;
    }

    public static Predicate<SlackRequestDTO> getPredicate() {
        return r -> r.getEvent().getText() != null && r.getEvent().getText().contains("leaderboard") && r.getEvent().getText().contains("overall");
    }

    public static RewardCommand build(SlackRequestDTO request) {
        return new RewardRankingsCommand(request.getEvent().getUser(), request.getEvent().getChannel());
    }

    @Override
    protected boolean validate() {
        return true;
    }

    @Override
    protected void performAction() {
        Sort sort = Sort.by(Sort.Direction.DESC).by("numberOfCoconuts");
        List<CoconutLedger> topTen = ledgerRepo.findAll(sort).buffer(10).blockFirst();

        slackService.sendMessage(channel, composeLeaderboard(topTen));
    }

    private String composeLeaderboard(List<CoconutLedger> topLedgers) {
        StringBuilder builder = new StringBuilder();
        builder.append("*Leaderboard*\n\n");
        int currentRank = 1;
        for (CoconutLedger ledger : topLedgers) {
            builder.append(currentRank++).append(". <@").append(ledger.getUsername()).append(">: ").append(ledger.getNumberOfCoconuts()).append("\n");
        }
        return builder.toString();
    }
}

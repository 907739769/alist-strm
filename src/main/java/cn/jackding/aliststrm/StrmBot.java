package cn.jackding.aliststrm;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Flag;
import org.telegram.telegrambots.bots.DefaultBotOptions;

import static org.telegram.abilitybots.api.objects.Locality.USER;
import static org.telegram.abilitybots.api.objects.Privacy.CREATOR;
import static org.telegram.abilitybots.api.util.AbilityUtils.getChatId;

/**
 * @Author Jack
 * @Date 2024/6/4 21:33
 * @Version 1.0.0
 */
@Slf4j
public class StrmBot extends AbilityBot {

    private final ResponseHandler responseHandler = new ResponseHandler(sender, db);

    public StrmBot() {
        super(Config.tgToken, "");
    }

    public StrmBot(DefaultBotOptions options) {
        super(Config.tgToken, "bot", options);
    }

    @Override
    public long creatorId() {
        return Long.parseLong(Config.tgUserId);
    }

    public Ability strm() {
        return Ability.builder()
                .name("strm")
                .info("生成strm")
                .privacy(CREATOR)
                .locality(USER)
                .input(0)
                .action(ctx -> {
                    silent.send("==开始执行strm任务==", ctx.chatId());
                    StrmService strmService = (StrmService) SpringContextUtil.getBean("strmService");
                    strmService.strm();
                    silent.send("==执行strm任务完成==", ctx.chatId());
                })
                .build();
    }

    public Ability strmDir() {
        return Ability.builder()
                .name("strmdir")
                .info("生成指定路径strm")
                .privacy(CREATOR)
                .locality(USER)
                .input(0)
                .action(ctx -> {
                    String parameter;
                    try {
                        parameter = ctx.firstArg();
                    } catch (Exception e) {
                        silent.forceReply("请输入路径", ctx.chatId());
                        return;
                    }
                    if (StringUtils.isBlank(parameter)) {
                        silent.forceReply("请输入路径", ctx.chatId());
                        return;
                    }
                    silent.send("==开始执行指定路径strm任务==", ctx.chatId());
                    StrmService strmService = (StrmService) SpringContextUtil.getBean("strmService");
                    strmService.strmDir(parameter);
                    silent.send("==执行指定路径strm任务完成==", ctx.chatId());
                })
                .reply((bot, upd) -> responseHandler.replyToStrmDdir(getChatId(upd), upd.getMessage().getText(), upd.getMessage().getMessageId()), Flag.REPLY,//回复
                        upd -> upd.getMessage().getReplyToMessage().hasText(), upd -> upd.getMessage().getReplyToMessage().getText().equals("请输入路径")//回复的是上面的问题
                )
                .build();
    }

}

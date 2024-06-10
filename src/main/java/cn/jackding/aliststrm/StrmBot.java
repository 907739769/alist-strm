package cn.jackding.aliststrm;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.bots.DefaultBotOptions;

import static org.telegram.abilitybots.api.objects.Locality.USER;
import static org.telegram.abilitybots.api.objects.Privacy.CREATOR;

/**
 * @Author Jack
 * @Date 2024/6/4 21:33
 * @Version 1.0.0
 */
@Slf4j
public class StrmBot extends AbilityBot {


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
                        silent.send("请加上路径参数", ctx.chatId());
                        log.error("", e);
                        return;
                    }
                    if (StringUtils.isBlank(parameter)) {
                        silent.send("请加上路径参数", ctx.chatId());
                        return;
                    }
                    silent.send("==开始执行指定路径strm任务==", ctx.chatId());
                    StrmService strmService = (StrmService) SpringContextUtil.getBean("strmService");
                    strmService.strmDir(parameter);
                    silent.send("==执行指定路径strm任务完成==", ctx.chatId());
                })
                .build();
    }

}

package cn.jackding.aliststrm.tg;

import cn.jackding.aliststrm.util.SpringContextUtil;
import cn.jackding.aliststrm.service.StrmService;
import lombok.extern.slf4j.Slf4j;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.sender.MessageSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Slf4j
public class ResponseHandler {

    private final MessageSender sender;

    public ResponseHandler(MessageSender sender, DBContext db) {
        this.sender = sender;
    }

    /**
     * 根据用户的输入同步
     *
     * @param chatId
     * @param parameter
     * @param messageId
     */
    public void replyToStrmDdir(long chatId, String parameter, Integer messageId) {
        try {
            sender.execute(SendMessage.builder().chatId(chatId).replyToMessageId(messageId).text("==开始执行指定路径strm任务==").build());
            StrmService strmService = (StrmService) SpringContextUtil.getBean("strmService");
            strmService.strmDir(parameter);
            sender.execute(SendMessage.builder().chatId(chatId).replyToMessageId(messageId).text("==执行指定路径strm任务完成==").build());
        } catch (Exception e) {
            log.error("", e);
        }
    }


}

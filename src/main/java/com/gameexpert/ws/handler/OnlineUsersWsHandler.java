package com.gameexpert.ws.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.gameexpert.api.SessionRegistry;
import com.gameexpert.ws.NicknameHandshakeInterceptor;
import com.gameexpert.ws.WorldBroadcaster;
import com.gameexpert.ws.WorldSessionRegistry;
import com.gameexpert.ws.WsMessageContext;
import com.gameexpert.ws.dto.OnlineUsersResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import tools.jackson.databind.JsonNode;

@Component
@RequiredArgsConstructor
public class OnlineUsersWsHandler implements WsMessageHandler {
    private final WorldSessionRegistry registry;
    private final WorldBroadcaster broadcaster;

    @Override
    public String type() {
        return "onlineUsers";
    }

    @Override
    public void handle(WsMessageContext context, JsonNode message) {
        // TODO Lv 15: 현재 월드의 열린 연결에서 닉네임을 조회하고 요청자에게 응답합니다.
        Collection<SessionRegistry.Entry> entries = registry.entries(context.worldId());
        List<String> users = new ArrayList<>();
        for(SessionRegistry.Entry entry : entries) {
            WebSocketSession session = entry.session();
            if(session.isOpen()) {
                Object user = session.getAttributes().get(NicknameHandshakeInterceptor.ATTR_NICKNAME);
                users.add(user.toString());
            }
        }
        users.sort(String.CASE_INSENSITIVE_ORDER);

        OnlineUsersResponse response = new OnlineUsersResponse(users, users.size());
        broadcaster.sendTo(context.session(), response);
    }
}

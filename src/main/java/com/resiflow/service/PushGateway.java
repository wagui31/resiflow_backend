package com.resiflow.service;

import com.resiflow.entity.UserPushToken;
import java.util.List;

public interface PushGateway {

    PushSendResult send(PushMessage pushMessage, List<UserPushToken> tokens);
}

package com.memo.game.controller;

import com.memo.game.dto.JoinMessage;
import com.memo.game.dto.MultiPlayerMessage;
import com.memo.game.dto.PlayerMessage;
import com.memo.game.gameModel.MultiPlayer;
import com.memo.game.service.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureMockMvc
public class MessageControllerTest {
    private static SimpMessagingTemplate messagingTemplate;
    private static MultiPlayerService multiPlayerService;
    private static final String token = "token123";
    private static final String token2 = "token456";
    private static final String invalidToken = "invalid_token";
    private static final String noUserToken = "no_user";
    private static final UUID playerId1 = UUID.randomUUID();
    private static final UUID playerId2 = UUID.randomUUID();
    private static MessageController messageController;
    private final ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
    private final ArgumentCaptor<Object> messageCaptor = ArgumentCaptor.forClass(Object.class);

    @BeforeAll
    public static void setUp() {
        TokenService tokenService = mock(TokenService.class);
        TokenBlacklistService tokenBlacklistService = mock(TokenBlacklistService.class);
        UserService userService = mock(UserService.class);
        multiPlayerService = mock(MultiPlayerService.class);

        messageController = new MessageController();
        tokenService.setTokenBlacklistService(tokenBlacklistService);
        messageController.setTokenService(tokenService);
        messageController.setMemoUsersService(userService);
        messageController.setMultiPlayerService(multiPlayerService);

        when(tokenBlacklistService.isBlacklisted(any())).thenReturn(false);
        when(tokenBlacklistService.addToBlacklist(any())).thenReturn(false);
        when(tokenService.isTokenValid(token)).thenReturn(true);
        when(tokenService.isTokenValid(token2)).thenReturn(true);
        when(tokenService.isTokenValid(noUserToken)).thenReturn(true);
        when(tokenService.isTokenValid(invalidToken)).thenReturn(false);
        when(tokenService.extractUserIdFromToken(token)).thenReturn(playerId1);
        when(tokenService.extractUserIdFromToken(token2)).thenReturn(playerId2);
        when(tokenService.extractUserIdFromToken(noUserToken)).thenReturn(null);
        when(userService.getUserNameById(playerId1)).thenReturn("name1");
        when(userService.getUserNameById(playerId2)).thenReturn("name2");
        doNothing().when(multiPlayerService).saveGame(any());
        doNothing().when(multiPlayerService).removeGame(any());
    }

    @BeforeEach
    public void setUpEach() {
        messagingTemplate = mock(SimpMessagingTemplate.class);
        messageController.setMessagingTemplate(messagingTemplate);
        doNothing().when(messagingTemplate).convertAndSend(anyString(), Optional.ofNullable(any()));
    }

    void setBoardCardsInOrder(MultiPlayer multiplayer) {
        int[] board = new int[multiplayer.getNumberOfPairs()*2];
        int index = 0;
        for(int i=1; i<=multiplayer.getNumberOfPairs(); i++) {
            board[index++] = i;
            board[index++] = i;
        }
        multiplayer.setBoard(board);
    }

    private JoinMessage getJoinMessage(int numOfPairs, String joinToken, boolean wantToPlayWithFriend, String roomId) {
        JoinMessage joinMessage = new JoinMessage();
        joinMessage.setToken(joinToken);
        joinMessage.setNumOfPairs(numOfPairs);
        joinMessage.setWantToPlayWithFriend(wantToPlayWithFriend);
        if(roomId!=null) joinMessage.setFriendRoomId(roomId);
        return joinMessage;
    }

    private MultiPlayerMessage getMultiPlayerMessage(String playerToken, UUID gameId, int index) {
        MultiPlayerMessage message = new MultiPlayerMessage();
        message.setSender(playerToken);
        message.setGameId(gameId);
        message.setIndex(index);
        return message;
    }

    @Test
    public void testJoinGame() {
        int numOfPairs = 8;
        MultiPlayer multiPlayer = new MultiPlayer(
                numOfPairs,
                playerId1,
                null
        );
        when(multiPlayerService.joinGame(eq(playerId1), eq(numOfPairs))).thenReturn(multiPlayer);

        JoinMessage joinMessage = getJoinMessage(numOfPairs, token, false, null);
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create();
        headerAccessor.setSessionAttributes(new HashMap<>());
        MultiPlayerMessage result = messageController.joinGame(joinMessage, headerAccessor);

        assertThat(result.getType()).isEqualTo("game.joined");
        assertThat(result.getPlayer1()).isEqualTo(playerId1);
    }

    @Test
    public void joinGameInvalidToken() {
        int numOfPairs = 8;
        JoinMessage joinMessage = getJoinMessage(numOfPairs, invalidToken, false, null);

        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create();
        headerAccessor.setSessionAttributes(new HashMap<>());
        MultiPlayerMessage result = messageController.joinGame(joinMessage, headerAccessor);

        assertThat(result.getType()).isEqualTo("error");
        assertThat(result.getContent()).isEqualTo("Unauthorized");
    }

    @Test
    public void joinGameUserNotFound() {
        int numOfPairs = 8;
        JoinMessage joinMessage = getJoinMessage(numOfPairs, noUserToken, false, null);

        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create();
        headerAccessor.setSessionAttributes(new HashMap<>());
        MultiPlayerMessage result = messageController.joinGame(joinMessage, headerAccessor);

        assertThat(result.getType()).isEqualTo("error");
        assertThat(result.getContent()).isEqualTo("User not found");
    }

    @Test
    public void joinGameCannotJoin() {
        int numOfPairs = 8;
        JoinMessage joinMessage = getJoinMessage(numOfPairs, token, false, null);
        when(multiPlayerService.joinGame(eq(playerId1), eq(numOfPairs))).thenReturn(null);

        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create();
        headerAccessor.setSessionAttributes(new HashMap<>());
        MultiPlayerMessage result = messageController.joinGame(joinMessage, headerAccessor);

        assertThat(result.getType()).isEqualTo("error");
        assertThat(result.getContent()).isEqualTo("Cannot join");
    }

    @Test
    public void joinGameIncorrectParams() {
        int numOfPairs = 0;
        JoinMessage joinMessage = getJoinMessage(numOfPairs, token, false, null);

        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create();
        headerAccessor.setSessionAttributes(new HashMap<>());
        MultiPlayerMessage result = messageController.joinGame(joinMessage, headerAccessor);

        assertThat(result.getType()).isEqualTo("error");
        assertThat(result.getContent()).isEqualTo("Incorrect params");
    }

    @Test
    public void joinGameTwoPlayers() {
        int numOfPairs = 8;
        MultiPlayer multiPlayer = new MultiPlayer(
                numOfPairs,
                playerId1,
                playerId2
        );
        when(multiPlayerService.joinGame(eq(playerId2), eq(numOfPairs))).thenReturn(multiPlayer);

        JoinMessage joinMessage = getJoinMessage(numOfPairs, token2, false, null);
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create();
        headerAccessor.setSessionAttributes(new HashMap<>());
        MultiPlayerMessage result = messageController.joinGame(joinMessage, headerAccessor);

        assertThat(result.getType()).isEqualTo("game.joined");
        assertThat(result.getPlayer1()).isEqualTo(playerId1);
        assertThat(result.getPlayer2()).isEqualTo(playerId2);
    }

    @Test
    public void joinWithFriendsTest() {
        int numOfPairs = 8;
        MultiPlayer multiPlayer = new MultiPlayer(
                numOfPairs,
                playerId1,
                null
        );
        when(multiPlayerService.joinGameWithFriend(eq(playerId1), eq(numOfPairs), isNull(UUID.class))).thenReturn(multiPlayer);

        JoinMessage joinMessage = getJoinMessage(numOfPairs, token, true,null);
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create();
        headerAccessor.setSessionAttributes(new HashMap<>());
        MultiPlayerMessage result = messageController.joinGame(joinMessage, headerAccessor);

        assertThat(result.getType()).isEqualTo("game.joined");
        assertThat(result.getPlayer1()).isEqualTo(playerId1);
    }

    @Test
    public void joinWithFriendsTwoPlayers() {
        int numOfPairs = 8;
        MultiPlayer multiPlayer = new MultiPlayer(
                numOfPairs,
                playerId1,
                playerId2
        );
        when(multiPlayerService.joinGameWithFriend(eq(playerId2), anyInt(), eq(multiPlayer.getPlayId()))).thenReturn(multiPlayer);

        JoinMessage joinMessage = getJoinMessage(numOfPairs, token2,
                true, multiPlayer.getPlayId().toString());
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create();
        headerAccessor.setSessionAttributes(new HashMap<>());
        MultiPlayerMessage result = messageController.joinGame(joinMessage, headerAccessor);

        assertThat(result.getType()).isEqualTo("game.joined");
        assertThat(result.getPlayer1()).isEqualTo(playerId1);
        assertThat(result.getPlayer2()).isEqualTo(playerId2);
    }

    @Test
    public void joinWithFriendsIncorrectParams() {
        int numOfPairs = 8;
        MultiPlayer multiPlayer = new MultiPlayer(
                numOfPairs,
                playerId1,
                playerId2
        );
        when(multiPlayerService.joinGameWithFriend(eq(playerId2), anyInt(), eq(multiPlayer.getPlayId()))).thenReturn(multiPlayer);

        JoinMessage joinMessage = getJoinMessage(0, token2,
                true, "jfrvknwej");
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create();
        headerAccessor.setSessionAttributes(new HashMap<>());
        MultiPlayerMessage result = messageController.joinGame(joinMessage, headerAccessor);
        assertThat(result.getType()).isEqualTo("error");
        assertThat(result.getContent()).isEqualTo("Incorrect params");
    }

    @Test
    public void testLeaveGame() {
        MultiPlayer game = new MultiPlayer(8, playerId1, null);
        when(multiPlayerService.leaveGame(playerId1)).thenReturn(game);

        PlayerMessage playerMessage = new PlayerMessage();
        playerMessage.setToken(token);

        messageController.leaveGame(playerMessage);
        verify(multiPlayerService).leaveGame(playerId1);
        verify(messagingTemplate,  Mockito.times(1)).convertAndSend(destinationCaptor.capture(), messageCaptor.capture());
        assertThat(destinationCaptor.getValue()).isEqualTo("/topic/game." + game.getPlayId());
        MultiPlayerMessage capturedMessage = (MultiPlayerMessage) messageCaptor.getValue();
        assertThat(capturedMessage.getType()).isEqualTo("game.left");
    }

    @Test
    public void makeFirstMove() {
        MultiPlayer game = new MultiPlayer(8, playerId1, playerId2);
        setBoardCardsInOrder(game);
        game.setGameStarted(true);
        when(multiPlayerService.getGame(eq(game.getPlayId()))).thenReturn(game);

        MultiPlayerMessage message = getMultiPlayerMessage(token, game.getPlayId(), 0);
        Map<Integer, Integer> lastMove = new HashMap<>();
        lastMove.put(0, 1);

        messageController.makeMove(message);

        verify(messagingTemplate,  Mockito.times(1)).convertAndSend(destinationCaptor.capture(), messageCaptor.capture());
        assertThat(destinationCaptor.getValue()).isEqualTo("/topic/game." + game.getPlayId());
        MultiPlayerMessage capturedMessage = (MultiPlayerMessage) messageCaptor.getValue();
        assertThat(capturedMessage.getType()).isEqualTo("game.move");
        assertThat(capturedMessage.getLastMove()).isEqualTo(lastMove);
    }

    @Test
    public void makeSecondMove() {
        MultiPlayer game = new MultiPlayer(8, playerId1, playerId2);
        setBoardCardsInOrder(game);
        game.flipCard(playerId1, 0);
        game.setGameStarted(true);
        when(multiPlayerService.getGame(eq(game.getPlayId()))).thenReturn(game);

        MultiPlayerMessage message = getMultiPlayerMessage(token, game.getPlayId(), 1);
        Map<Integer, Integer> lastMove = new HashMap<>();
        lastMove.put(1, 1);

        messageController.makeMove(message);

        verify(messagingTemplate,  Mockito.times(1)).convertAndSend(destinationCaptor.capture(), messageCaptor.capture());
        assertThat(destinationCaptor.getValue()).isEqualTo("/topic/game." + game.getPlayId());
        MultiPlayerMessage capturedMessage = (MultiPlayerMessage) messageCaptor.getValue();
        assertThat(capturedMessage.getType()).isEqualTo("game.move");
        assertThat(capturedMessage.getLastMove()).isEqualTo(lastMove);
    }

    @Test
    public void makeMovePlayer2NotJoined() {
        MultiPlayer game = new MultiPlayer(8, playerId1, null);
        when(multiPlayerService.getGame(eq(game.getPlayId()))).thenReturn(game);

        MultiPlayerMessage message = getMultiPlayerMessage(token, game.getPlayId(), 1);

        messageController.makeMove(message);

        verify(messagingTemplate,  Mockito.times(1)).convertAndSend(destinationCaptor.capture(), messageCaptor.capture());
        assertThat(destinationCaptor.getValue()).isEqualTo("/topic/game." + game.getPlayId());
        MultiPlayerMessage capturedMessage = (MultiPlayerMessage) messageCaptor.getValue();
        assertThat(capturedMessage.getType()).isEqualTo("error");
        assertThat(capturedMessage.getContent()).isEqualTo("Game is waiting for another player to join.");
    }

    @Test
    public void makeMoveGameNotFoundOrIsAlreadyOver() {
        UUID gameId = UUID.randomUUID();
        when(multiPlayerService.getGame(eq(gameId))).thenReturn(null);

        MultiPlayerMessage message = getMultiPlayerMessage(token, gameId, 1);
        messageController.makeMove(message);

        verify(messagingTemplate,  Mockito.times(1)).convertAndSend(destinationCaptor.capture(), messageCaptor.capture());
        assertThat(destinationCaptor.getValue()).isEqualTo("/topic/game." + gameId);
        MultiPlayerMessage capturedMessage = (MultiPlayerMessage) messageCaptor.getValue();
        assertThat(capturedMessage.getType()).isEqualTo("error");
        assertThat(capturedMessage.getContent()).isEqualTo("Game not found or is already over.");
    }

    @Test
    public void makeMoveIncorrectParams() {
        MultiPlayer game = new MultiPlayer(8, playerId1, playerId2);
        game.setGameStarted(true);
        when(multiPlayerService.getGame(eq(game.getPlayId()))).thenReturn(game);

        MultiPlayerMessage message = getMultiPlayerMessage(token, game.getPlayId(), -1);

        messageController.makeMove(message);

        verify(messagingTemplate,  Mockito.times(1)).convertAndSend(destinationCaptor.capture(), messageCaptor.capture());
        assertThat(destinationCaptor.getValue()).isEqualTo("/topic/game." + game.getPlayId());
        MultiPlayerMessage capturedMessage = (MultiPlayerMessage) messageCaptor.getValue();
        assertThat(capturedMessage.getType()).isEqualTo("error");
        assertThat(capturedMessage.getContent()).isEqualTo("Incorrect params");
    }

    @Test
    public void makeMoveNotYourTurn() {
        MultiPlayer game = new MultiPlayer(8, playerId1, playerId2);
        game.setGameStarted(true);
        when(multiPlayerService.getGame(eq(game.getPlayId()))).thenReturn(game);

        MultiPlayerMessage message = getMultiPlayerMessage(token2, game.getPlayId(), 0);

        messageController.makeMove(message);

        verify(messagingTemplate,  Mockito.times(1)).convertAndSend(destinationCaptor.capture(), messageCaptor.capture());
        assertThat(destinationCaptor.getValue()).isEqualTo("/topic/game." + game.getPlayId());
        MultiPlayerMessage capturedMessage = (MultiPlayerMessage) messageCaptor.getValue();
        assertThat(capturedMessage.getType()).isEqualTo("error");
        assertThat(capturedMessage.getContent()).isEqualTo("Not your turn");
    }

    @Test
    public void makeMoveIsGameOver() {
        int numOfPairs = 8;
        MultiPlayer game = new MultiPlayer(numOfPairs, playerId1, playerId2);
        setBoardCardsInOrder(game);
        game.setGameStarted(true);
        for(int i=0; i<numOfPairs*2; i++) {
            game.flipCard(playerId1, i);
            game.flipCard(playerId1, ++i);
            game.flipCard(playerId2, ++i);
            if(i==numOfPairs*2-2) {
                break;
            }
            game.flipCard(playerId2, ++i);
        }
        when(multiPlayerService.getGame(eq(game.getPlayId()))).thenReturn(game);

        MultiPlayerMessage message = getMultiPlayerMessage(token2, game.getPlayId(), numOfPairs*2-1);
        Map<Integer, Integer> lastMove = new HashMap<>();
        lastMove.put(numOfPairs*2-2, numOfPairs);
        lastMove.put(numOfPairs*2-1, numOfPairs);

        messageController.makeMove(message);

        verify(messagingTemplate,  Mockito.times(1)).convertAndSend(destinationCaptor.capture(), messageCaptor.capture());
        assertThat(destinationCaptor.getValue()).isEqualTo("/topic/game." + game.getPlayId());
        MultiPlayerMessage capturedMessage = (MultiPlayerMessage) messageCaptor.getValue();
        assertThat(capturedMessage.getType()).isEqualTo("game.gameOver");
        assertThat(capturedMessage.getLastMove()).isEqualTo(lastMove);
    }

    @Test
    void testSessionDisconnectEvent() {
        int numOfPairs = 8;
        MultiPlayer game = new MultiPlayer(numOfPairs, playerId1, null);
        setBoardCardsInOrder(game);
        Map<String, Object> sessionAttributes = new HashMap<>();
        sessionAttributes.put("gameId", game.getPlayId());
        sessionAttributes.put("player", playerId1);

        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create();
        headerAccessor.setSessionAttributes(sessionAttributes);
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], headerAccessor.getMessageHeaders());
        CloseStatus closeStatus = CloseStatus.NORMAL;
        SessionDisconnectEvent event = new SessionDisconnectEvent(this, message, game.getPlayId().toString(),
                closeStatus);
        when(multiPlayerService.getGame(eq(game.getPlayId()))).thenReturn(game);

        messageController.SessionDisconnectEvent(event);

        verify(messagingTemplate,  Mockito.times(1)).convertAndSend(destinationCaptor.capture(), messageCaptor.capture());
        assertThat(destinationCaptor.getValue()).isEqualTo("/topic/game." + game.getPlayId());
        MultiPlayerMessage capturedMessage = (MultiPlayerMessage) messageCaptor.getValue();
        assertThat(capturedMessage.getType()).isEqualTo("game.gameOver");
        assertThat(capturedMessage.getPlayer1()).isEqualTo(playerId1);
    }
}

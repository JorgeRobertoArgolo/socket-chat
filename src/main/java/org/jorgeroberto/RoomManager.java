package org.jorgeroberto;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gerencia o estado centralizado do sistema de chat.
 * Lida com a criação dinâmica de salas, registro de clientes e a lógica de broadcast.
 * Usa estruturas Thread-Safe (ConcurrentHashMap e Set sincronizado) para lidar com concorrência.
 */
public class RoomManager {
    private final Map<String, Set<ClientHandler>> rooms;
    private final Map<String, ClientHandler> clients;

    /**
     * Construtor, inicializa as coleções e garante que a sala 'lobby' exista.
     */
    public RoomManager() {
        this.rooms = new ConcurrentHashMap<>();
        this.clients = new ConcurrentHashMap<>();
        // Garante que a sala 'lobby' exista por padrão
        rooms.put("lobby", Collections.synchronizedSet(new HashSet<>()));
    }

    /**
     * Adiciona um novo cliente ao sistema e à sala inicial (após a identificação).
     * @param handler O ClientHandler do novo cliente.
     * @param userName O nome de usuário escolhido.
     * @param roomName O nome da sala inicial (normalmente 'lobby').
     */
    public void addClient(ClientHandler handler, String userName, String roomName) {
        clients.put(userName, handler);
        rooms.computeIfAbsent(roomName, k -> Collections.synchronizedSet(new HashSet<>())).add(handler);
    }

    /**
     * Remove o cliente do sistema e da sala atual. Chamado ao desconectar.
     * @param handler O ClientHandler a ser removido.
     * @param roomName A sala da qual o cliente está saindo.
     */
    public void removeClient(ClientHandler handler, String roomName) {
        if (handler.getUserName() != null) {
            clients.remove(handler.getUserName());
        }
        Set<ClientHandler> roomClients = rooms.get(roomName);
        if (roomClients != null) {
            roomClients.remove(handler);
        }
    }

    /**
     * Move um cliente de uma sala para outra, criando a nova sala dinamicamente, se necessário.
     * Notifica ambas as salas sobre a transição.
     * @param handler O ClientHandler do cliente que está mudando de sala.
     * @param userName O nome do usuário.
     * @param oldRoom O nome da sala atual.
     * @param newRoom O nome da sala de destino.
     */
    public void joinRoom(ClientHandler handler, String userName, String oldRoom, String newRoom) {
        // 1. Notifica e remove da sala antiga
        broadcast(oldRoom, "SERVER: " + userName + " saiu da sala para entrar em " + newRoom, handler);
        Set<ClientHandler> oldRoomClients = rooms.get(oldRoom);
        if (oldRoomClients != null) {
            oldRoomClients.remove(handler);
        }

        // 2. Adiciona à nova sala (cria se não existir)
        rooms.computeIfAbsent(newRoom, k -> Collections.synchronizedSet(new HashSet<>())).add(handler);
        handler.sendMessage("SERVER: Você entrou na sala " + newRoom);

        // 3. Notifica a nova sala
        broadcast(newRoom, "SERVER: " + userName + " entrou na sala.", handler);
    }

    /**
     * Remove um cliente de uma sala e o realoca no lobby.
     * @param handler O ClientHandler do cliente que está saindo.
     * @param userName O nome do usuário.
     * @param oldRoom A sala da qual o cliente está saindo.
     * @param lobbyRoom O nome do lobby ("lobby").
     */
    public void leaveRoom(ClientHandler handler, String userName, String oldRoom, String lobbyRoom) {
        // Remove da sala atual
        Set<ClientHandler> oldRoomClients = rooms.get(oldRoom);
        if (oldRoomClients != null) {
            oldRoomClients.remove(handler);
        }
        broadcast(oldRoom, "SERVER: " + userName + " saiu da sala.", handler);

        // Adiciona ao lobby
        rooms.computeIfAbsent(lobbyRoom, k -> Collections.synchronizedSet(new HashSet<>())).add(handler);
        handler.sendMessage("SERVER: Você saiu de " + oldRoom + " e entrou no " + lobbyRoom);
        broadcast(lobbyRoom, "SERVER: " + userName + " retornou ao lobby.", handler);
    }


    /**
     * Envia uma mensagem de broadcast para todos os clientes em uma sala específica.
     * * @param roomName O nome da sala para onde a mensagem será enviada.
     * @param message A mensagem a ser transmitida.
     * @param sender O ClientHandler do remetente (null se for mensagem do servidor), usado para não retransmitir a mensagem para ele mesmo.
     */
    public void broadcast(String roomName, String message, ClientHandler sender) {
        logMessage(roomName, message);

        Set<ClientHandler> roomClients = rooms.get(roomName);
        if (roomClients != null) {
            for (ClientHandler client : roomClients) {
                if (client != sender) {
                    client.sendMessage(message);
                }
            }
        }
    }

    /**
     * Sobrecarga de broadcast para mensagens originadas do servidor.
     * @param roomName O nome da sala.
     * @param message A mensagem do servidor.
     */
    public void broadcast(String roomName, String message) {
        broadcast(roomName, message, null);
    }

    /**
     * Salva a mensagem no arquivo de log da sala correspondente.
     * O arquivo será criado no formato: sala_nome.txt
     * @param roomName Nome da sala.
     * @param message Conteúdo da mensagem.
     */
    private void logMessage(String roomName, String message) {

        // Cria o diretório de logs
        File logDir = new File("logs");

        // 2. CRIA O DIRETÓRIO SE NÃO EXISTIR
        if (!logDir.exists()) {
            if (logDir.mkdir()) {
                System.out.println("SERVIDOR: Pasta 'logs/' criada com sucesso.");
            } else {
                System.err.println("SERVIDOR: Erro ao criar a pasta 'logs/'. Salvando na raiz.");
                // Se não conseguir criar a pasta, salva na raiz
                logDir = new File(".");
            }
        }

        // 3. Define o nome do arquivo dentro da pasta 'logs/'
        String baseFileName = roomName.replace("#", "") + ".txt";
        File logFile = new File(logDir, baseFileName);

        try (
                FileWriter fw = new FileWriter(logFile, true);
                PrintWriter pw = new PrintWriter(fw)
        ) {
            String logEntry = String.format("[%s] %s", java.time.LocalTime.now(), message);
            pw.println(logEntry);

        } catch (IOException e) {
            System.err.println("SERVIDOR: Erro ao escrever no log da sala " + roomName + ": " + e.getMessage());
        }
    }

    /**
     * Tenta enviar uma mensagem privada de um remetente para um destinatário específico.
     * @param senderName O nome de usuário do remetente.
     * @param targetName O nome de usuário do destinatário.
     * @param message A mensagem privada a ser enviada.
     * @return true se o destinatário foi encontrado e a mensagem foi enviada, false caso contrário.
     */
    public boolean sendPrivateMessage(String senderName, String targetName, String message) {
        ClientHandler targetHandler = clients.get(targetName);
        String logRoom = "private";

        if (targetHandler != null) {
            logMessage(logRoom, "[PRIVADO de " + senderName + " para " + targetName + "]: " + message);

            targetHandler.sendMessage("(PRIVADO de " + senderName + "): " + message);

            ClientHandler senderHandler = clients.get(senderName);
            if (senderHandler != null) {
                senderHandler.sendMessage("(PRIVADO para " + targetName + "): " + message);
            }
            return true;
        }
        return false;
    }

    /**
     * Obtém o ClientHandler de um usuário específico.
     */
    public ClientHandler getClient(String userName) {
        return clients.get(userName);
    }
}

package org.jorgeroberto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Thread responsável por gerenciar a comunicação com um único cliente.
 * Lida com leitura de comandos, identificação e encerramento.
 */
public class ClientHandler implements Runnable{

    private final Socket clientSocket;
    private final RoomManager roomManager;
    private PrintWriter writer;
    private String userName;
    //Sala inicial
    private String currentRoom = "lobby";


    /**
     * Construtor do ClientHandler.
     * @param clientSocket O socket TCP estabelecido com o cliente.
     * @param roomManager A instância compartilhada que gerencia salas e estado.
     */
    public ClientHandler(Socket clientSocket, RoomManager roomManager) {
        this.clientSocket = clientSocket;
        this.roomManager = roomManager;
    }

    /**
     * Envia uma mensagem para este cliente específico.
     * Utilizado pelo RoomManager para broadcast ou mensagens diretas.
     * @param message A string de mensagem a ser enviada ao cliente.
     */
    public void sendMessage(String message) {
        if (writer != null) {
            writer.println(message);
        }
    }

    /**
     * Métodoo principal da Thread. Contém o loop de leitura de mensagens do cliente.
     */
    @Override
    public void run() {
        try (
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            this.writer = writer;

            //Solicitação do nome de Usuário
            String welcomeMessage = "SERVIDOR: Bem-vindo! Vamos cadastrar seu nickname.\n"
                    + "SERVIDOR: Para isso, escreva da seguinte forma: NICK <nome>\n"
                    + "SERVIDOR: Por exemplo, NICK test\n"
                    + "SERVIDOR: Agora sua vez, qual o seu Nickname?";

            writer.println(welcomeMessage);

            String line;
            boolean identified = false;

            while (!identified && (line = reader.readLine()) != null) {
                if (line.toUpperCase().startsWith("NICK ")) {
                    userName = line.substring(5).trim();
                    if (!userName.isEmpty()) {
                        identified = true;
                        roomManager.addClient(this, userName, currentRoom); // Adiciona ao sistema
                        writer.println("SERVER: Seu nome de usuário é " + userName + ".\nVocê está no lobby.\nUse /join #<sala> para entrar em uma sala.");
                        roomManager.broadcast(currentRoom, "SERVER: " + userName + " entrou no lobby.", this);
                    } else {
                        writer.println("SERVER: Nome de usuário não pode ser vazio. Tente novamente (NICK <nome>):");
                    }
                } else {
                    writer.println("SERVER: Comando de identificação inválido. Use NICK <nome>.");
                }
            }

            if (!identified) {
                return;
            }

            while ((line = reader.readLine()) != null) {

                String trimmedLine = line.trim();
                if (trimmedLine.isEmpty()) {
                    continue;
                }

                if (line.startsWith("/")) {
                    handleCommand(trimmedLine);
                } else {
                    // Transmissão de Mensagens (Broadcast) para a sala atual
                    roomManager.broadcast(currentRoom, "[" + userName + " em #" + currentRoom + "]: " + trimmedLine, this);
                }
            }
        } catch (IOException e) {
            //Conexão perdida ou encerrada de maneira abrupta
            System.err.println("Conexão com " + (userName != null ? userName : "Cliente") + " perdida: " + e.getMessage());
        } finally {
            roomManager.removeClient(this, currentRoom);
            if (userName != null) {
                roomManager.broadcast(currentRoom, "SERVER: " + userName + " saiu da sala.", this);
            }
            try {
                clientSocket.close();
            } catch (IOException e) {
                //Erro ao fechar o socker, não preciso tratar
            }
        }
    }

    /**
     * Processa comandos específicos do protocolo de chat (Regras de Comando).
     */
    private void handleCommand(String command) {
        String[] parts = command.split(" ", 2);
        String action = parts[0].toLowerCase();

        switch (action) {
            case "/join":
                if (parts.length > 1 && parts[1].startsWith("#")) {
                    String newRoom = parts[1].toLowerCase();
                    roomManager.joinRoom(this, userName, currentRoom, newRoom);
                    currentRoom = newRoom;
                } else {
                    sendMessage("SERVER: Comando /join inválido.\nUse: /join #<nome_da_sala>\n");
                }
                break;
            case "/private":
                // Validação da sintaxe e lógica de envio privado
                if (parts.length < 2) {
                    sendMessage("SERVER: Comando /private inválido. Uso: /private <usuario> <mensagem>");
                    return;
                }
                String messageContent = parts[1].trim();
                int firstSpace = messageContent.indexOf(' ');

                if (firstSpace == -1) {
                    sendMessage("SERVER: Comando /private inválido. Uso: /private <usuario> <mensagem>");
                    return;
                }

                String targetUser = messageContent.substring(0, firstSpace).trim();
                String privateMessage = messageContent.substring(firstSpace).trim();

                if (targetUser.equalsIgnoreCase(userName)) {
                    sendMessage("SERVER: Você não pode enviar mensagem privada para si mesmo.");
                } else if (roomManager.sendPrivateMessage(userName, targetUser, privateMessage)) {
                    // O RoomManager já envia o feedback para o remetente e para o destinatário
                } else {
                    sendMessage("SERVER: O usuário '" + targetUser + "' não foi encontrado ou está desconectado.");
                }
                break;
            case "/leave":
                // Não pode sair do lobby
                if (!currentRoom.equals("lobby")) {
                    roomManager.leaveRoom(this, userName, currentRoom, "lobby");
                    currentRoom = "lobby";
                } else {
                    sendMessage("SERVER: Você já está no lobby.\nUse /join #<sala> para mudar.");
                }
                break;
            case "/exit":
            case "/quit":
                sendMessage("SERVER: Encerrando conexão...");
                try {
                    clientSocket.close();
                } catch (IOException e) {

                }
                break;
            default:
                sendMessage("SERVER: Comando desconhecido ou inválido: " + command);
                break;
        }
    }

    /**
     * Obtém o nome de usuário deste cliente.
     * @return O nome de usuário do cliente.
     */
    public String getUserName() {
        return userName;
    }
}

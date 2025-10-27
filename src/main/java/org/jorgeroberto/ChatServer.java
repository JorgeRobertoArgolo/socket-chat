package org.jorgeroberto;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 * Classe principal do Servidor de Chat.
 * Responsável por iniciar o ServerSocket, aceitar conexões
 * de múltiplos clientes e delegar a comunicação para o ClientHandler.
 */
public class ChatServer {
    private static final RoomManager roomManager = new RoomManager();

    /**
     * Ponto de entrada da aplicação Servidora.
     * Solicita a porta de comunicação e inicia o loop de aceitação de clientes.
     * * @param args Argumentos de linha de comando (não utilizados).
     */
    public static void main(String[] args) {
        // Solicitação da porta que irá se conectar
        Scanner scanner = new Scanner(System.in);
        System.out.println("--------------------------------------------------------------------");
        System.out.println("                  BEM VINDO AO CHAT JAVA");
        System.out.println("--------------------------------------------------------------------");
        System.out.print("Por favor, digite a porta que será inicializado o Servidor: ");
        int port;
        try {
            port = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Sinto muito, a porta digitada é inválida. Será usado a portão padrão 9000");
            port = 9000;
        }

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("--------------------------------------------------------------------");
            System.out.println("                           CHAT JAVA");
            System.out.println("--------------------------------------------------------------------");
            // Obtém o endereço IP local do servidor
            System.out.println("Servidor de chat iniciado em " + serverSocket.getInetAddress().getHostName() + ":" + port);

            //Loop de gerenciamento de conexaão
            while (true) {
                // Bloqueia e espera por uma conexão de cliente
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado de " +  clientSocket.getRemoteSocketAddress());

                //Gerenciamento de Conexão Concorrente: Cria uma thread para o novo cliente
                ClientHandler handler = new ClientHandler(clientSocket, roomManager);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            System.err.println("Erro ao iniciar o Servidor: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
}

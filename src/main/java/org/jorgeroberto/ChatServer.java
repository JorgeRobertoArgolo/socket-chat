package org.jorgeroberto;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 * Classe principal do Servidor de Chat.
 * Responsável por iniciar o ServerSocket e gerenciar a concorrência de clientes.
 */
public class ChatServer {
    private static final RoomManager roomManager = new RoomManager();

    /**
     * Ponto de entrada da aplicação Servidora.
     * Inicia o loop de aceitação de clientes.
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
            // Informa o local onde os logs serão salvos
            System.out.println("LOGS: Os arquivos de log serão salvos em: " + new java.io.File(".").getAbsolutePath());

            // Mostra o endereço IP local do servidor
            System.out.println("Servidor de chat iniciado em " + serverSocket.getInetAddress().getHostName() + ":" + port);

            //Loop de gerenciamento de conexão
            while (true) {
                // Bloqueia e espera por uma conexão de cliente
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado de " +  clientSocket.getRemoteSocketAddress());

                //Cria uma thread para gerenciar a conexão concorrente
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

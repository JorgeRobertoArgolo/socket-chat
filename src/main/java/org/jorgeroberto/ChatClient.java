package org.jorgeroberto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * Classe principal do Cliente de Chat.
 * Responsável por configurar a conexão, solicitar dados do servidor (IP e porta)
 * e gerenciar a interface de entrada/saída do usuário (console).
 */
public class ChatClient {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Configuração Inicial: Solicitar IP e Porta
        System.out.print("Digite o Endereço IP do Servidor: ");
        String serverIP = scanner.nextLine();

        System.out.print("Digite a Porta do Servidor: ");
        int serverPort;
        try {
            serverPort = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.err.println("Porta inválida. Usando padrão 9090.");
            serverPort = 9090;
        }

        try (
                // Conexão TCP
                Socket socket = new Socket(serverIP, serverPort);
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)
        ) {
            System.out.println("✅ Conectado ao Servidor.");

            // Thread para escutar mensagens do servidor
            new Thread(new ServerListener(reader)).start();

            // Loop principal para ler entrada do usuário e enviar ao servidor
            while (scanner.hasNextLine()) {
                String message = scanner.nextLine();

                // Encerramento
                if (message.equalsIgnoreCase("/exit") || message.equalsIgnoreCase("/quit")) {
                    writer.println("/exit");
                    break;
                }
                // Envia a mensagem/comando
                writer.println(message);
            }

        } catch (IOException e) {
            System.err.println("Erro de conexão com o Servidor: " + e.getMessage());
        } finally {
            scanner.close();
            System.out.println("Desconectado.");
        }
    }

    /**
     * Classe interna que implementa Runnable para escutar mensagens do servidor
     * em uma Thread separada.
     */
    private static class ServerListener implements Runnable {
        private final BufferedReader reader;

        public ServerListener(BufferedReader reader) {
            this.reader = reader;
        }

        @Override
        public void run() {
            String line;
            try {
                // Fica em loop esperando por mensagens do servidor
                while ((line = reader.readLine()) != null) {
                    System.out.println("\n" + line);
                }
            } catch (IOException e) {
                // Conexão encerrada pelo servidor
                System.out.println("Servidor encerrou a conexão ou houve um erro.");
            }
        }
    }
}

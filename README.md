# Sistema de Chat Multiusuário (Sockets TCP em Java 21)

Este projeto implementa um sistema de chat multiusuário cliente/servidor usando sockets TCP e multithreading em Java, conforme a proposta da atividade de Redes de Computadores.

---

## ⚙️ 1. Ambiente de Execução

* **Distribuição:** Debian 12 (ou similar).
* **Virtualização:** VirtualBox (três VMs: 1 Servidor, 2 Clientes).
* **Rede:** Configuração em modo Host-Only ou Rede Interna, com IPs estáticos na mesma sub-rede (ex: Servidor: `192.168.0.10`).
* **Runtime:** OpenJDK 21.

## 🛠️ 2. Instalação e Compilação

**Pré-requisito:** O OpenJDK 21 deve estar instalado nas três máquinas virtuais.

1.  **Clonar/Baixar o Código (em todas as VMs):**
    ```bash
    # Se o Git estiver instalado:
    git clone [https://github.com/JorgeRobertoArgolo/socket-chat.git](https://github.com/JorgeRobertoArgolo/socket-chat.git)
    cd socket-chat
    
    # Se o Git NÃO estiver instalado, use o arquivo ZIP.
    ```

2.  **Compilar o Código (em todas as VMs):**
    Este comando deve ser executado a partir do diretório raiz do projeto:
    ```bash
    javac src/main/java/org/jorgeroberto/*.java
    ```

## 🚀 3. Execução do Sistema

### 3.1. Iniciar o Servidor (MV Servidor)

1.  Execute a classe principal do servidor e forneça a porta de comunicação (ex: **1234**):
    ```bash
    java -cp src/main/java org.jorgeroberto.ChatServer
    ```

### 3.2. Iniciar os Clientes (MV Cliente 1 e 2)

1.  Execute a classe principal do cliente:
    ```bash
    java -cp src/main/java org.jorgeroberto.ChatClient
    ```
2.  Forneça os dados de conexão:
    * **IP do Servidor:** `192.168.0.10` (o IP estático da sua MV Servidor).
    * **Porta:** `1234`

### 3.3. Comandos do Chat

| Comando | Descrição |
| :--- | :--- |
| `NICK <nome>` | **Obrigatório** para identificação inicial. |
| `/join #<sala>` | Entra em uma sala de bate-papo (cria se não existir). |
| `/leave` | Sai da sala atual e retorna ao lobby. |
| `/exit` ou `/quit` | Encerra a conexão com o servidor. |
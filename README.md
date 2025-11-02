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
    # (Comando git clone ou unzip aqui)
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
    *O Servidor criará a pasta `logs/` no diretório de execução.*

### 3.2. Iniciar os Clientes (MV Cliente 1 e 2)

1.  Execute a classe principal do cliente:
    ```bash
    java -cp src/main/java org.jorgeroberto.ChatClient
    ```
2.  Forneça os dados de conexão:
    * **IP do Servidor:** `192.168.0.10` (o IP estático da sua MV Servidor).
    * **Porta:** `1234`

---

## ✨ 4. Funcionalidades e Comandos

### Comandos Essenciais

| Comando | Descrição |
| :--- | :--- |
| `NICK <nome>` | **Obrigatório** para identificação inicial. |
| `/join #<sala>` | Entra em uma sala de bate-papo (cria se não existir). |
| `/leave` | Sai da sala atual e retorna ao lobby. |
| `/exit` ou `/quit` | Encerra a conexão com o servidor. |

### Novas Funcionalidades (Opcionais)

O sistema implementa os dois requisitos opcionais, garantindo a robustez do sistema:

#### A. Mensagens Privadas (1-1)

Permite enviar mensagens diretamente a um usuário, independente da sala em que ele esteja.

| Comando | Descrição |
| :--- | :--- |
| `/private <usuário> <mensagem>` | Envia a mensagem apenas para o usuário especificado. |
| **Exemplo:** | `/private Alice Oi, só você vê isso.` |

#### B. Log de Mensagens

Todas as mensagens (públicas e privadas) são registradas no Servidor:

* **Logs Públicos:** Mensagens enviadas para salas são salvas em arquivos específicos da sala (ex: `logs/geral.txt`, `logs/teste.txt`).
* **Logs Privados:** Mensagens enviadas via `/private` são centralizadas no arquivo `logs/private.txt`.
* **Local:** Os arquivos de log são gerados automaticamente na subpasta **`logs/`** do diretório onde o Servidor foi executado.
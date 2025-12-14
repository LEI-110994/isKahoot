# IsKahoot - Jogo Concorrente e Distribuído

Este projeto é uma implementação de um jogo de quiz distribuído e concorrente, desenvolvido para a disciplina de Programação Concorrente e Distribuída (PCD). Utiliza uma arquitetura Cliente-Servidor com comunicação via sockets e mecanismos de sincronização para gerir o fluxo do jogo.

## Funcionalidades Essenciais

*   **Arquitetura Cliente-Servidor:** O Servidor gere múltiplos jogos simultâneos e Clientes interagem através de uma GUI em Swing.
*   **Perguntas Individuais:** Pontuação baseada na resposta individual do jogador, com bónus de rapidez.
*   **Perguntas de Equipa:** A pontuação da equipa é avaliada quando todos os membros respondem ou o tempo esgota.
*   **Gestão de Jogo:** Cálculo de pontuações, gestão de equipas e identificação do vencedor.
*   **Interface TUI (Servidor):** Comandos para criar e iniciar jogos.

## Pré-requisitos

*   Java JDK 11 ou superior.
*   Apache Maven 3.6 ou superior.

## Como Jogar

O jogo requer o início do servidor e, posteriormente, a conexão dos clientes.

### 1. Iniciar o Servidor

Num terminal, na pasta `isKahoot`, execute:

```powershell
mvn exec:java "-Dexec.mainClass=iskahoot.server.Server"
```

No terminal do servidor, utilize os seguintes comandos:

*   **Criar um novo jogo:**
    ```text
    > new <numEquipas> <jogadoresPorEquipa> <numPerguntas>
    Exemplo: new 2 1 5
    ```
    O servidor irá devolver um `GameCode`.

*   **Iniciar o jogo:**
    ```text
    > start <GameCode>
    Exemplo: start 1234
    ```
    Execute este comando após todos os jogadores se terem conectado.

*   **Outros comandos:** `games` (listar jogos), `exit` (sair).

### 2. Iniciar os Clientes (Jogadores)

Abra terminais separados para cada jogador. Execute o comando, substituindo os parâmetros:

```powershell
mvn exec:java "-Dexec.mainClass=iskahoot.client.Client" "-Dexec.args=<IP> <Port> <GameCode> <NomeEquipa> <NomeJogador>"
```

**Exemplo (localhost):**

*   **Jogador 1 (Equipa A):**
    ```powershell
    mvn exec:java "-Dexec.mainClass=iskahoot.client.Client" "-Dexec.args=localhost 12345 <GameCode> EquipaA Jogador1"
    ```

*   **Jogador 2 (Equipa B):**
    ```powershell
    mvn exec:java "-Dexec.mainClass=iskahoot.client.Client" "-Dexec.args=localhost 12345 <GameCode> EquipaB Jogador2"
    ```
    Utilize o `GameCode` gerado pelo servidor.

## Estrutura do Projeto

*   `src/main/java/iskahoot/client`: Lógica do cliente e GUI.
*   `src/main/java/iskahoot/server`: Lógica do servidor e sincronização.
*   `src/main/java/iskahoot/net`: Classes de mensagens para comunicação.
*   `src/main/java/iskahoot/model`: Classes de dados.
*   `resources/questions.json`: Ficheiro de perguntas.

## Notas Adicionais

*   **Gestão de Tempo:** Cada pergunta tem um limite de tempo. A ronda termina antecipadamente se todos responderem.
*   **Vencedor:** A equipa com maior pontuação acumulada no final do jogo é declarada vencedora.

---
**PCD - 2025/2026**

# Achados de batalha aproveitáveis no PvPPokeGo

Este documento registra apenas os achados relevantes para o projeto PvPPokeGo encontrados durante a análise de um APK modificado estilo PGSharp. O objetivo é reaproveitar conceitos e estados de batalha no nosso próprio código, sem copiar binários proprietários nem depender de um APK de terceiro.

## Evidências encontradas no binário

Foram observadas referências textuais relacionadas a batalha, incluindo:

- `instantRocket`
- `rocket.fast`
- `rocket.charge`
- `rocket.shield`
- `CombatVNextBattleStateShim`
- `battleAssistant`
- `GymBattleState`
- `RaidLobbyState`
- `GymRootState`
- `breadBattleDodge`
- `breadBattleOrb`
- `breadBattleFast`
- `breadBattleCharge1`
- `Minigame`
- `StartBattle`
- `SwapPokemon`
- `AttackTelegraph`
- `ActorMove`
- `BattleEnd`
- `Charge2`
- `SpecialCharge`

## Interpretação

### Rocket

As chaves `rocket.fast`, `rocket.charge` e `rocket.shield` indicam separação explícita entre:

- ataque ágil;
- ataque carregado;
- uso de escudo.

Isso combina diretamente com o núcleo já existente no PvPPokeGo, que possui configurações independentes para `autoFastAttack`, `autoChargedAttack` e `autoShield` e produz intenções `TapFastAttack`, `TapChargedAttack` e `TapShield`.

### PvP

`CombatVNextBattleStateShim` sugere uma camada de estado de combate moderna. O binário analisado não forneceu, apenas pelas strings, prova suficiente de uma chave separada `pvp.fast` ou `pvp.charge`. Portanto PvP deve continuar sendo tratado por reconhecimento próprio de estado e pelo motor de decisão do PvPPokeGo.

### Raids e ginásios

`battleAssistant`, `GymBattleState`, `RaidLobbyState` e `GymRootState` indicam estados específicos de lobby/batalha de raid e ginásio.

Essas referências devem orientar a criação de uma máquina de estados local no PvPPokeGo, mas não são suficientes para afirmar como a implementação original realiza cada ação.

### Max Battles

Foram encontradas funções separadas para:

- ataque ágil (`breadBattleFast`);
- carregado (`breadBattleCharge1`);
- esquiva (`breadBattleDodge`);
- coleta/uso de orb (`breadBattleOrb`).

Vale criar um módulo separado para Max Battles, sem misturar suas regras com PvP/Rocket/Raid comum.

## Arquitetura recomendada no PvPPokeGo

### BattleStateDetector

Responsável por classificar o estado atual:

- fora de batalha;
- início;
- batalha ativa;
- carregado disponível;
- prompt de escudo;
- troca;
- minigame do carregado;
- fim da batalha;
- lobby de raid;
- Rocket;
- PvP;
- Raid/Ginásio;
- Max Battle.

### BattleInputEngine

Recebe apenas intenções do core:

- ataque ágil;
- carregado 1/2;
- escudo;
- troca;
- gestos do minigame.

A lógica de decisão não deve depender de coordenadas.

### ChargedMinigameSolver

Módulo separado para reconhecer o tipo do minigame de ataque carregado e gerar a trajetória de gesto necessária para maximizar a pontuação.

Importante: a string `enhancedThrowExcellent` encontrada em outro contexto pertence à mecânica de captura e não deve ser confundida com "Excelente" de ataque carregado.

### Modos de operação sugeridos

- `ASSIST`: apenas recomenda ações.
- `SEMI_AUTO`: ataque ágil automático; carregado/escudo continuam manuais.
- `AUTO_BATTLE`: ataque ágil + escolha inteligente de carregado + escudo configurável + minigame do carregado quando detectado.

## Integração com o core atual

O `BattleAutomationController` já resolve parte importante da lógica:

- habilitação mestre;
- fast attack independente;
- charged attack independente;
- seleção do carregado por `buttonIndex`;
- escudo independente;
- modos de escolha de carregado e escudo.

Portanto o próximo trabalho útil não é refazer o motor de decisão, e sim implementar:

1. detector robusto dos estados visuais;
2. mapeamento de regiões/controles na tela;
3. executor Android desacoplado;
4. solver do minigame do carregado;
5. testes com gravações de PvP, Rocket, Raid e Max Battle.

## Regra de implementação

Não copiar `libmain.so` nem depender de símbolos/offsets do APK analisado. O binário serve somente como referência de conceitos/estados. Toda lógica do PvPPokeGo deve permanecer própria, testável e organizada em módulos independentes.


## 22/11/2026 — Atualização cruzada sobre Auto PvP (documentação)

Estudo adicional dos APKs [1.33.0](https://drive.google.com/file/d/1qvz4iYBRGfZZycazYagpxLaXMQqx2d48/view), [1.50.3](https://drive.google.com/file/d/1GuH3JkL77DYC7FfZujcEFxfy4rrafM32/view) e [1.278.0](https://drive.google.com/file/d/13wvFdgVpnVHXsRUftJYjWiyQCzmJDsiU/view), com vídeos de HUD PvP [13](https://drive.google.com/file/d/1O6OyTOvJcFcKps5Qn9tHgJoqsfU3tTXM/view) e [14](https://drive.google.com/file/d/1CuMTP0q8X2NSV_8wHmogbeGwTIMimz7n/view). **Não** houve extração executável/diff confirmado ou transplante literal PGSharp. A linha 1.50 descreve Raid & Gym Assistant e não comprova GBL automático. O PGBR integrou sua própria correção de observação/sessão no [PR #79](https://github.com/LucianoToscano/PGBR/pull/79), publicou a [pré-release QA LT035](https://github.com/LucianoToscano/PGBR/releases/tag/v0.00.35-LT) com workflow aprovado, mas permanece `device_tested=false`; escudos, charged 2 e Auto PvP real carecem de homologação. O documento canônico [CONSOLIDATED_PROJECT_CONTEXT_2026-09-26.md](CONSOLIDATED_PROJECT_CONTEXT_2026-09-26.md) contém a matriz detalhada. Nenhum código PvPPokeGo foi alterado por este adendo.
